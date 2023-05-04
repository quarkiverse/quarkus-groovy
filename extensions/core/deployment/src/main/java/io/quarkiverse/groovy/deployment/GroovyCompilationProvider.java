/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.groovy.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.GroovyClass;
import org.jboss.logging.Logger;

import groovy.lang.GroovyClassLoader;
import io.quarkus.deployment.dev.CompilationProvider;
import io.quarkus.paths.PathCollection;

public class GroovyCompilationProvider implements CompilationProvider {

    private static final Logger log = Logger.getLogger(GroovyCompilationProvider.class);
    private static final String GROOVY_PROVIDER_KEY = "groovy";
    private static final Pattern OPTION_PATTERN = Pattern.compile("([^=]+)=(.*)");

    @Override
    public String getProviderKey() {
        return GROOVY_PROVIDER_KEY;
    }

    @Override
    public Set<String> handledExtensions() {
        return Set.of(".groovy");
    }

    @Override
    public void compile(Set<File> filesToCompile, Context context) {
        CompilerConfiguration cc = getCompilerConfiguration(context);
        cc.setSourceEncoding(context.getSourceEncoding().name());
        cc.setTargetBytecode(context.getTargetJvmVersion());
        cc.setTargetDirectory(context.getOutputDirectory().getAbsolutePath());
        try (URLClassLoader parent = createNewClassLoader(Stream.of(context.getClasspath(), context.getReloadableClasspath())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
                GroovyClassLoader groovyClassLoader = new GroovyClassLoader(parent, cc);
                GroovyClassLoader transformLoader = new GroovyClassLoader(parent)) {
            CompilationUnit unit = new CompilationUnit(cc, null, groovyClassLoader, transformLoader);
            filesToCompile.forEach(unit::addSource);
            unit.compile();
            // log compiled classes
            List<GroovyClass> classes = unit.getClasses();
            log.infof("Compiled %d file%s.", classes.size(), classes.size() > 1 ? "s" : "");
        } catch (CompilationFailedException e) {
            // Convert the CompilationFailedException into a RuntimeException to prevent serialization issues in remote
            // dev mode
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Extracts from the given context the compiler options, and if they exist, it provides an instance of
     * {@code CompilerConfiguration} with the options applied, an empty {@code CompilerConfiguration} otherwise.
     *
     * @param context the context from which the compiler options are extracted.
     * @return an {@code CompilerConfiguration} corresponding to the given context.
     */
    private static CompilerConfiguration getCompilerConfiguration(Context context) {
        final Collection<String> compilerOptions = context.getCompilerOptions(GROOVY_PROVIDER_KEY);
        CompilerConfiguration cc;
        if (compilerOptions != null && !compilerOptions.isEmpty()) {
            Properties properties = new Properties(compilerOptions.size());
            for (String rawOption : compilerOptions) {
                final Matcher matcher = OPTION_PATTERN.matcher(rawOption);
                if (!matcher.matches()) {
                    log.warnf("Groovy compiler option %s is invalid", rawOption);
                }
                properties.setProperty(matcher.group(1), matcher.group(2));
            }
            cc = new CompilerConfiguration(properties);
        } else {
            cc = new CompilerConfiguration();
        }
        return cc;
    }

    /**
     * @param classpath the compilation classpath from which the {@code URLClassLoader} is built.
     * @return an {@code URLClassLoader} with the System ClassLoader as parent and the classpath as URLs from which to
     *         load classes and resources.
     * @throws MalformedURLException if one {@code File} from the classpath cannot be converted into an {@code URL}.
     */
    private static URLClassLoader createNewClassLoader(final List<File> classpath) throws MalformedURLException {
        List<URL> urlsList = new ArrayList<>();
        for (File file : classpath) {
            urlsList.add(file.toURI().toURL());
        }
        return new URLClassLoader(urlsList.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
    }

    @Override
    public Path getSourcePath(Path classFilePath, PathCollection sourcePaths, String classesPath) {
        // return same class so it is not removed
        return classFilePath;
    }
}
