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

import static org.awaitility.Awaitility.await;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

import io.quarkus.maven.it.RunAndCheckWithAgentMojoTestBase;
import io.quarkus.test.devmode.util.DevModeClient;

class GroovyRemoteDevModeIT extends RunAndCheckWithAgentMojoTestBase {

    private final DevModeClient devModeClient = new DevModeClient();

    @Test
    void testThatTheApplicationIsReloadedOnGroovyChange()
            throws MavenInvocationException, IOException {
        testDir = initProject("projects/classic-groovy", "projects/project-classic-run-groovy-change-remote");
        agentDir = initProject("projects/classic-groovy", "projects/project-classic-run-groovy-change-local");
        runAndCheck();

        // Edit the "Hello" message.
        File source = new File(agentDir, "src/main/groovy/org/acme/HelloResource.groovy");
        String uuid = UUID.randomUUID().toString();
        filter(source, Map.of("'hello'", "'" + uuid + "'"));

        // Wait until we get "uuid"
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(3, TimeUnit.MINUTES).until(() -> devModeClient.getHttpResponse("/app/hello").contains(uuid));

        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(source::isFile);

        filter(source, Map.of(uuid, "carambar"));

        // Wait until we get "carambar"
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(3, TimeUnit.MINUTES).until(() -> devModeClient.getHttpResponse("/app/hello").contains("carambar"));
    }
}
