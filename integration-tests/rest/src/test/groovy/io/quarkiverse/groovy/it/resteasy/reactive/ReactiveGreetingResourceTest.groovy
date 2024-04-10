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
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.CoreMatchers.is

@QuarkusTest
class ReactiveGreetingResourceTest {

    @Test
    void testResource() {
        given()
            .when()
            .get("/test.txt")
            .then()
            .statusCode(200)
    }

    @Test
    void testHello() {
        given()
            .when()
            .get("/hello-rest/")
            .then()
            .statusCode(200)
            .body(is("Hello Rest"))
    }

    @Test
    void testStandard() {
        given()
            .when()
            .get("/hello-rest/standard")
            .then()
            .statusCode(200)
            .body(is("Hello Rest"))
    }

    @Test
    void testNamedHello() {
        given()
            .when()
            .get("/hello-rest/Bob")
            .then()
            .statusCode(200)
            .body(is("Hello Bob"))
    }
}
