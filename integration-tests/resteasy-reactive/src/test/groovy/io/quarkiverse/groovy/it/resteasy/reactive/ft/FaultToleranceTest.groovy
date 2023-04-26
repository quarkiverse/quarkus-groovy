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
package io.quarkiverse.groovy.it.resteasy.reactive.ft

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.CoreMatchers.equalTo

@QuarkusTest
class FaultToleranceTest {
    @Test
    void test() {
        given()
            .when()
            .post("/ft/hello/fail")
            .then()
            .statusCode(204)
        given()
            .when()
            .get("/ft/client")
            .then()
            .statusCode(200)
            .body(equalTo("fallback"))
        given()
            .when()
            .post("/ft/hello/heal")
            .then()
            .statusCode(204)
        given()
            .when()
            .get("/ft/client")
            .then()
            .statusCode(200)
            .body(equalTo("Hello, world!"))
    }
}
