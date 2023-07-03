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
package io.quarkiverse.groovy.runtime;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.util.URLStreams;
import org.jboss.logging.Logger;

import groovy.lang.GroovySystem;
import groovy.lang.MetaMethod;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class GroovyRecorder {

    private static final Logger LOG = Logger.getLogger(GroovyRecorder.class);

    public void initExtensionModules() {
        if (GroovySystem.getMetaClassRegistry() instanceof MetaClassRegistryImpl) {
            scanModulesFrom(ExtensionModuleScanner.MODULE_META_INF_FILE);
            scanModulesFrom(ExtensionModuleScanner.LEGACY_MODULE_META_INF_FILE);
        }
    }

    private static void scanModulesFrom(String moduleMetaInfFile) {
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(moduleMetaInfFile);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                scanExtensionModuleFromMetaInf(url);
            }
        } catch (Exception e) {
            LOG.warnf("An error occurred while scanning the extension modules '%s': %s", moduleMetaInfFile, e.getMessage());
        }
    }

    private static void scanExtensionModuleFromMetaInf(final URL metadata) {
        try (InputStream inStream = URLStreams.openUncachedStream(metadata)) {
            Properties properties = new Properties();
            properties.load(inStream);
            registerExtensionModuleFromProperties(properties);
        } catch (Exception e) {
            LOG.warnf("An error occurred while registering the extension module '%s': %s", metadata, e.getMessage());
        }
    }

    private static void registerExtensionModuleFromProperties(Properties properties) {
        Map<CachedClass, List<MetaMethod>> metaMethods = new HashMap<>();
        MetaClassRegistryImpl registry = (MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry();
        registry.registerExtensionModuleFromProperties(properties, Thread.currentThread().getContextClassLoader(), metaMethods);
        for (Map.Entry<CachedClass, List<MetaMethod>> entry : metaMethods.entrySet()) {
            CachedClass c = entry.getKey();
            Set<CachedClass> classesToBeUpdated = new HashSet<>();
            classesToBeUpdated.add(c);
            ClassInfo.onAllClassInfo(
                    info -> {
                        if (c.getTheClass().isAssignableFrom(info.getCachedClass().getTheClass())) {
                            classesToBeUpdated.add(info.getCachedClass());
                        }
                    });
            classesToBeUpdated.forEach(cc -> cc.addNewMopMethods(entry.getValue()));
        }
    }
}
