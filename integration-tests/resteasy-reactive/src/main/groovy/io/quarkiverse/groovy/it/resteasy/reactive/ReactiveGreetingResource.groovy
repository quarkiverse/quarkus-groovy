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

import io.quarkus.runtime.annotations.RegisterForReflection
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.groups.UniCreate
import io.smallrye.mutiny.groups.UniOnItem
import io.smallrye.mutiny.groups.UniOnItemDelay
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

import java.time.Duration

// Could change the code to allow static compilation but the idea is to show that the dynamic compilation can be used
// as long as the reflection is properly configured
@RegisterForReflection(targets= [Uni.class, UniCreate.class, UniOnItem.class, UniOnItemDelay.class])
@Path("/hello-resteasy-reactive")
class ReactiveGreetingResource {

    @Inject
    RequestScopedGroovyClass req

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> hello() {
        Uni.createFrom().nullItem()
            .onItem().invoke {
                req.message = "msg"
            }
            .onItem().delayIt().by(Duration.ofMillis(50))
            .onItem().invoke {
                if (req.message != "msg") {
                    throw new Throwable("Request scoped data was lost")
                }
            }
            .onItem().transform {
               "Hello RestEASY Reactive"
            }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/standard")
    String standard() {
        "Hello RestEASY Reactive"
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{name}")
    Uni<String> hello(String name) {
        Uni.createFrom().item("Hello $name")
            .onItem().delayIt().by(Duration.ofMillis(50))

    }
}
