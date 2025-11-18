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
package io.quarkus.groovy.maven.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.shared.invoker.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

import io.quarkus.maven.it.QuarkusPlatformAwareMojoTestBase;
import io.quarkus.maven.utilities.MojoUtils;

class GroovyCreateMavenProjectIT extends QuarkusPlatformAwareMojoTestBase {

    private Invoker invoker;
    private File testDir;

    @Disabled("Quarkiverse Language Support is required")
    @Test
    void testProjectGenerationFromScratchForGroovy() throws MavenInvocationException, IOException {
        testDir = initEmptyProject("projects/project-generation-groovy");
        assertThat(testDir).isDirectory();
        invoker = initInvoker(testDir);

        Properties properties = new Properties();
        properties.put("projectGroupId", "org.acme");
        properties.put("projectArtifactId", "acme");
        properties.put("projectVersion", "1.0-SNAPSHOT");
        properties.put("extensions",
                String.format("io.quarkiverse.groovy:quarkus-groovy:%s,resteasy-jsonb", getExtensionVersion()));
        setup(properties);

        // As the directory is not empty (log) navigate to the artifactID directory
        testDir = new File(testDir, "acme");

        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(new File(testDir, "src/main/groovy")).isDirectory();
        assertThat(new File(testDir, "src/main/resources/application.properties")).isFile();

        String config = Files
                .asCharSource(new File(testDir, "src/main/resources/application.properties"), StandardCharsets.UTF_8)
                .read();
        assertThat(config).isEmpty();

        assertThat(new File(testDir, "src/main/docker/Dockerfile.native")).isFile();
        assertThat(new File(testDir, "src/main/docker/Dockerfile.jvm")).isFile();

        Model model = loadPom(testDir);
        final DependencyManagement dependencyManagement = model.getDependencyManagement();
        final List<Dependency> dependencies = dependencyManagement.getDependencies();
        assertThat(dependencies.stream()
                .anyMatch(d -> d.getArtifactId().equals(MojoUtils.TEMPLATE_PROPERTY_QUARKUS_PLATFORM_ARTIFACT_ID_VALUE)
                        && d.getVersion().equals(MojoUtils.TEMPLATE_PROPERTY_QUARKUS_PLATFORM_VERSION_VALUE)
                        && d.getScope().equalsIgnoreCase("import")
                        && d.getType().equalsIgnoreCase("pom")))
                .isTrue();

        assertThat(
                model.getDependencies().stream().anyMatch(d -> d.getArtifactId().equalsIgnoreCase("quarkus-resteasy")
                        && d.getVersion() == null))
                .isTrue();
        assertThat(
                model.getDependencies().stream().anyMatch(d -> d.getArtifactId().equalsIgnoreCase("quarkus-groovy")
                        && d.getVersion() == null))
                .isTrue();

        assertThat(model.getProfiles()).hasSize(1);
        assertThat(model.getProfiles().get(0).getId()).isEqualTo("native");
    }

    private void setup(Properties params)
            throws MavenInvocationException, FileNotFoundException {

        params.setProperty("platformGroupId", getBomGroupId());
        params.setProperty("platformArtifactId", getBomArtifactId());
        params.setProperty("platformVersion", getBomVersion());

        InvocationRequest request = new DefaultInvocationRequest();
        request.setBatchMode(true);
        request.setGoals(Collections.singletonList(
                String.format("%s:%s:%s:create", getMavenPluginGroupId(), getMavenPluginArtifactId(),
                        getMavenPluginVersion())));
        request.setProperties(params);
        request.setShowErrors(true);
        File log = new File(testDir, String.format("build-create-%s.log", testDir.getName()));
        PrintStreamLogger logger = new PrintStreamLogger(
                new PrintStream(new FileOutputStream(log), false, StandardCharsets.UTF_8),
                InvokerLogger.DEBUG);
        invoker.setLogger(logger);
        invoker.execute(request);
    }

    private String getExtensionVersion() {
        return System.getProperty("extension.version");
    }
}
