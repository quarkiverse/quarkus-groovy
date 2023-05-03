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
import static org.hamcrest.Matchers.is

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

import io.quarkus.builder.Version
import io.quarkus.test.ProdBuildResults
import io.quarkus.test.ProdModeTestResults
import io.quarkus.test.QuarkusProdModeTest
import io.restassured.RestAssured

/**
 * Verifies that DDL scripts are generated when script generation is configured in application.properties.
 */
class DDLGenerationPMT {

    @RegisterExtension
    static final QuarkusProdModeTest config = new QuarkusProdModeTest()
            .withApplicationRoot {jar -> jar
                    .addClasses(PageItem.class, NoPagingTestEndpoint.class, PageItemRepository.class) }
            .setApplicationName("ddl-generation")
            .setApplicationVersion(Version.getVersion())
            .setRun(true)
            .setLogFileName("ddl-generation-test.log")
            .withConfigurationResource("ddlgeneration.properties")

    @ProdBuildResults
    ProdModeTestResults prodModeTestResults

    @Test
    void test() {
        RestAssured.when().get("/no-paging-test").then().body(is("OK"))

        assertThat(prodModeTestResults.getBuildDir().resolve("quarkus-app/create.sql").toFile()).exists()
        assertThat(prodModeTestResults.getBuildDir().resolve("quarkus-app/drop.sql").toFile()).exists()
    }

}
