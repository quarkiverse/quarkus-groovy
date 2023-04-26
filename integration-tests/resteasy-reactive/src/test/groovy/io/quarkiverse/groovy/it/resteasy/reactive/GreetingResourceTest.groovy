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
package io.quarkiverse.groovy.it.resteasy.reactive

import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.notNullValue

@QuarkusTest
class GreetingResourceTest {

    @Test
    void testDataClassAndCustomFilters() {
        given()
            .when()
            .get("/greeting")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", is("hello foo bar"))
            .header("method", "testSuspend")
            .header("method2", "testSuspend")
    }

    @Test
    void testAbortingCustomFilters() {
        given()
            .header("abort", "true")
            .when()
            .get("/greeting")
            .then()
            .statusCode(204)
            .header("random", notNullValue())
    }

    @Test
    void testNoopCoroutine() {
        given()
            .when()
            .get("/greeting/noop")
            .then()
            .statusCode(204)
    }
}
