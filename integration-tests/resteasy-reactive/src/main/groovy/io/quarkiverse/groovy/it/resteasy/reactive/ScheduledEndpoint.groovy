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

import groovy.transform.CompileStatic
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.ScheduledExecution
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@CompileStatic
@Path("scheduled")
class ScheduledEndpoint {

    private AtomicInteger num1 = new AtomicInteger(0)
    private AtomicInteger num2 = new AtomicInteger(0)

    @Scheduled(every = "0.5s")
    Uni<Void> scheduled1() {
        Uni.createFrom().nullItem()
            .onItem().delayIt().by(Duration.ofMillis(100))
            .onItem().invoke {
                num1.compareAndSet(0, 1)
            }
            .replaceWithVoid()
    }

    @Scheduled(every = "0.5s")
    Uni<Void> scheduled2(ScheduledExecution scheduledExecution) {
        Uni.createFrom().nullItem()
            .onItem().delayIt().by(Duration.ofMillis(100))
            .onItem().invoke {
                num2.compareAndSet(0, 1)
            }
            .replaceWithVoid()
    }

    @Path("num1") @GET def num1() { Response.status(200 + num1.get()).build() }

    @Path("num2") @GET def num2() { Response.status(200 + num2.get()).build() }
}
