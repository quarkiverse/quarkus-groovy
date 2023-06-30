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

package io.quarkiverse.groovy.it.resteasy

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.CoreMatchers.is

@QuarkusTest
class GroovyExtensionModuleResourceTest {

    @Test
    void testStaticExtension() {
        given()
                .when()
                .get("/extension/static")
                .then()
                .statusCode(200)
                .body(is("Hello, world!"))
    }

    @Test
    void testInstanceExtension() {
        given()
                .when()
                .get("/extension/instance")
                .then()
                .statusCode(200)
                .body(is("Tried 5 times"))
    }
}
