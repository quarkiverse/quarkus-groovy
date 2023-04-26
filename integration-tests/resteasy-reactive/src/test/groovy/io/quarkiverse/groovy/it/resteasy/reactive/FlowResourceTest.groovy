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

import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.sse.SseEventSource
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

import static io.restassured.RestAssured.given
import static org.assertj.core.api.Assertions.assertThat

@QuarkusTest
class FlowResourceTest {

    @TestHTTPResource("/flow")
    String flowPath

    @Test
    void testSseStrings() {
        testSse("str", 5) { assertThat(it).containsExactly("HELLO", "FROM", "KOTLIN", "FLOW") }
    }

    @Test
    void testSuspendSseStrings() {
        testSse("suspendStr", 5) {
            assertThat(it).containsExactly("HELLO", "FROM", "KOTLIN", "FLOW")
        }
    }

    @Test
    void testResponseStatusAndHeaders() {
        given()
            .when()
            .get("/flow/str")
            .then()
            .statusCode(201)
            .headers(["foo": "bar"])
    }

    @Test
    void testSeeJson() {
        testSse("json", 10) {
            assertThat(it)
                .containsExactly(
                    "{\"name\":\"Barbados\",\"capital\":\"Bridgetown\"}",
                    "{\"name\":\"Mauritius\",\"capital\":\"Port Louis\"}",
                    "{\"name\":\"Fiji\",\"capital\":\"Suva\"}"
                )
        }
    }

    private testSse(String path, long timeout, Consumer<List<String>> assertion) {
        var client = ClientBuilder.newBuilder().build()
        WebTarget target = client.target("$flowPath/$path")
        try (SseEventSource eventSource = SseEventSource.target(target)
            .reconnectingEvery(Integer.MAX_VALUE.toLong(), TimeUnit.SECONDS)
            .build()) {
                var res = new CompletableFuture<List<String>>()
                var collect = Collections.synchronizedList(new ArrayList<String>())
                eventSource.register(
                    { inboundSseEvent -> collect.add(inboundSseEvent.readData()) },
                    { throwable -> res.completeExceptionally(throwable) }
                ) {
                    res.complete(collect)
                }
                eventSource.open()
                var list = res.get(timeout, TimeUnit.SECONDS)
                assertion(list)
            }
    }
}
