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
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

import io.quarkus.maven.it.RunAndCheckMojoTestBase;
import io.quarkus.test.devmode.util.DevModeTestUtils;

class GroovyDevModeIT extends RunAndCheckMojoTestBase {

    @Test
    void testThatTheApplicationIsReloadedOnGroovyChange() throws MavenInvocationException, IOException {
        testDir = initProject("projects/classic-groovy", "projects/project-classic-run-groovy-change");
        runAndCheck(true);

        // Edit the "Hello" message.
        File jaxRsResource = new File(testDir, "src/main/groovy/org/acme/HelloResource.groovy");
        String uuid = UUID.randomUUID().toString();
        filter(jaxRsResource, Map.of("'hello'", "'" + uuid + "'"));

        // Wait until we get "uuid"
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> DevModeTestUtils.getHttpResponse("/app/hello").contains(uuid));

        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(jaxRsResource::isFile);

        filter(jaxRsResource, Map.of(uuid, "carambar"));

        // Wait until we get "carambar"
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> DevModeTestUtils.getHttpResponse("/app/hello").contains("carambar"));

        File greetingService = new File(testDir, "src/main/groovy/org/acme/GreetingService.groovy");
        String newUuid = UUID.randomUUID().toString();
        filter(greetingService, Map.of("'hello'", "'" + newUuid + "'"));

        // Wait until we get "newUuid"
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> DevModeTestUtils.getHttpResponse("/app/hello/bean").contains(newUuid));
    }

    @Test
    public void testThatTheApplicationIsReloadedOnGroovyChangeWithCustomCompilerArgs()
            throws MavenInvocationException, IOException {
        testDir = initProject("projects/groovy-compiler-args", "projects/groovy-compiler-args-change");
        runAndCheck(true);

        // Edit the "Hello" message.
        File jaxRsResource = new File(testDir, "src/main/groovy/org/acme/HelloResource.groovy");
        String uuid = UUID.randomUUID().toString();
        filter(jaxRsResource, Map.of("'hello'", "'" + uuid + "'"));

        // Wait until we get "uuid"
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> DevModeTestUtils.getHttpResponse("/app/hello").contains(uuid));
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> DevModeTestUtils.getHttpResponse("/app/hello/name").contains("someName"));

        File helloService = new File(testDir, "src/main/groovy/org/acme/Hello.groovy");
        filter(helloService, Map.of("someName", "otherName"));
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> DevModeTestUtils.getHttpResponse("/app/hello/name").contains("otherName"));
    }

    @Test
    public void testExternalGroovyReloadableArtifacts() throws Exception {
        final String rootProjectPath = "projects/external-reloadable-artifacts";

        // Set up the external project
        final File externalJarDir = initProject(rootProjectPath + "/external-lib");

        // Clean and install the external JAR in local repository (.m2)
        install(externalJarDir, true);

        // Set up the main project that uses the external dependency
        this.testDir = initProject(rootProjectPath + "/app");

        // Run quarkus:dev process
        run(true);

        await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> DevModeTestUtils.getHttpResponse("/hello").contains("Hello"));

        final File greetingGroovy = externalJarDir.toPath().resolve("src").resolve("main")
                .resolve("groovy").resolve("org").resolve("acme").resolve("lib")
                .resolve("groovy").resolve("Greeting.groovy").toFile();
        assertThat(greetingGroovy).exists();

        // Uncomment the method bonjour() in Greeting.groovy
        filter(greetingGroovy, Map.of("/*-", "", "-*/", ""));
        install(externalJarDir, false);

        final File greetingResourceGroovy = this.testDir.toPath().resolve("src").resolve("main")
                .resolve("groovy").resolve("org").resolve("acme")
                .resolve("groovy").resolve("GreetingResource.groovy").toFile();
        assertThat(greetingResourceGroovy).exists();

        // Update the GreetingResource.groovy to call the Greeting.bonjour() method
        final String greetingBonjourCall = "Greeting.bonjour()";
        filter(greetingResourceGroovy, Map.of("Greeting.hello()", greetingBonjourCall));

        await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> DevModeTestUtils.getHttpResponse("/hello").contains("Bonjour"));

        // Change bonjour() method content in Greeting.groovy
        filter(greetingGroovy, Map.of("Bonjour", "Bonjour!"));
        install(externalJarDir, false);

        // Change GreetingResource.kt endpoint response to upper case letters
        filter(greetingResourceGroovy, Map.of(greetingBonjourCall, greetingBonjourCall.concat(".toUpperCase()")));

        await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> DevModeTestUtils.getHttpResponse("/hello").contains("BONJOUR!"));
    }
}