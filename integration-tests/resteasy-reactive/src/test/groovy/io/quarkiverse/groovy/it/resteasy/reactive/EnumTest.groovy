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
import static org.hamcrest.CoreMatchers.equalTo

@QuarkusTest
class EnumTest {

    @Test
    void testNoStates() {
        given()
            .when()
            .get("/enum")
            .then()
            .statusCode(200)
            .body(equalTo("States: []"))
    }

    @Test
    void testSingleState() {
        given()
                .when()
                .get("/enum?state=State1")
                .then()
                .statusCode(200)
                .body(equalTo("States: [State1]"))
    }

    @Test
    void testMultipleStates() {
        given()
                .when()
                .get("/enum?state=State1&state=State2&state=State3")
                .then()
                .statusCode(200)
                .body(equalTo("States: [State1, State2, State3]"))
    }
}
