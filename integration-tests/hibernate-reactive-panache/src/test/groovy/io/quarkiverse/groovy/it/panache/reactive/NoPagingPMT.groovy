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
package io.quarkiverse.groovy.it.panache.reactive

import static org.assertj.core.api.Assertions.assertThat
import static org.awaitility.Awaitility.await
import static org.hamcrest.Matchers.is

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

import io.quarkus.builder.Version
import io.quarkus.test.LogFile
import io.quarkus.test.QuarkusProdModeTest
import io.restassured.RestAssured

/**
 * Test if PanacheQuery is using unnecessary SQL for limiting the number of output rows, the log output is tested for the
 * presence of <code>offset</code> or <code>limit</code> in the SQL.
 */
class NoPagingPMT {

    @RegisterExtension
    static final QuarkusProdModeTest config = new QuarkusProdModeTest()
            .withApplicationRoot { jar -> jar
                        .addClasses(PageItem.class, NoPagingTestEndpoint.class, PageItemRepository.class) }
            .setApplicationName("no-paging-test")
            .setApplicationVersion(Version.getVersion())
            .setRun(true)
            .setLogFileName("no-paging-test.log")
            .withConfigurationResource("nopaging.properties")

    @LogFile
    private Path logfile

    @Test
    void test() {
        assertThat(logfile).isRegularFile().hasFileName("no-paging-test.log")

        RestAssured.when().get("/no-paging-test").then().body(is("OK"))

        // the logs might not be flushed to disk immediately, so wait a few seconds before giving up completely
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(this::checkLog)
    }

    private void checkLog() {
        final List<String> lines
        try {
            lines = Files.readAllLines(logfile)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }

        /*
         * Test the SQL was logged, this could fail if Hibernate decides to change how it logs the generated SQL, here in order
         * to not silently skip the following test
         */
        final boolean sqlFound = lines.stream()
                .filter { line -> line.matches(".*select .* from PageItem .*") }
                .findAny()
                .isPresent()
        assertThat(sqlFound)
                .as("Hibernate query wasn't logged")
                .isTrue()

        // Search for the presence of a SQL with a limit or offset
        final boolean wrongSqlFound = lines.stream()
                .filter { line -> line.matches(".*select .* limit .*") || line.matches(".*select .* offset .*") }
                .findAny()
                .isPresent()
        assertThat(wrongSqlFound)
                .as("PanacheQuery is generating SQL with limits and/or offsets when no paging has been requested")
                .isFalse()
    }
}
