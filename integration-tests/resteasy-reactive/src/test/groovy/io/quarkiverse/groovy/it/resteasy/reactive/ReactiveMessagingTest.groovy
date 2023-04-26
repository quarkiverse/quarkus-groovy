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

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import java.time.Duration

import static io.restassured.RestAssured.given
import static org.awaitility.Awaitility.await
import static org.hamcrest.CoreMatchers.is

@QuarkusTest
@QuarkusTestResource(KafkaTestResource.class)
class ReactiveMessagingTest {

    @Test
    void test() {
        assertCountries(6)

        given()
            .when()
            .post("/country/kafka/dummy")
            .then()
            .statusCode(200)

        assertCountries(8)
    }

    private static def assertCountries(int num) {
        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(5)).untilAsserted {
            given()
                .when()
                .get("/country/resolved")
                .then()
                .body("size()", is(num))
        }
    }
}
