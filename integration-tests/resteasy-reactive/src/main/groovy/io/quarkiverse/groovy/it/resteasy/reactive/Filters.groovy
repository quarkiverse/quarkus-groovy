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
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.jboss.resteasy.reactive.server.ServerRequestFilter
import org.jboss.resteasy.reactive.server.ServerResponseFilter
import org.jboss.resteasy.reactive.server.SimpleResourceInfo

import java.security.SecureRandom
import java.time.Duration
import java.util.function.Function

@CompileStatic
class Filters {

    private SecureRandom secureRandom = new SecureRandom()

    @ServerRequestFilter
    Uni<Void> addHeader(UriInfo uriInfo, ContainerRequestContext context) {
        Uni.createFrom().nullItem()
                .onItem().delayIt().by(Duration.ofMillis(100))
                .onItem().transform({ context.headers.add("firstName", "foo") } as Function)
                .onItem().delayIt().by(Duration.ofMillis(100))
                .replaceWithVoid()
    }

    @ServerRequestFilter
    Uni<Response> addHeaderOrAbort(ContainerRequestContext context)  {
        Uni.createFrom().nullItem()
            .onItem().delayIt().by(Duration.ofMillis(100))
            .onItem().transform(
                {
                    if (context.headers.containsKey("abort")) {
                        return Response.noContent().header("random", "" + secureRandom.nextInt()).build()
                    }
                    context.headers.add("lastName", "bar")
                    null
                }
            )
            .onItem().delayIt().by(Duration.ofMillis(100))
    }

    @ServerResponseFilter
    Uni<Void> addResponseHeader(
            ContainerResponseContext context,
            SimpleResourceInfo simpleResourceInfo,
            ResourceInfo resourceInfo
    ) {
        Uni.createFrom().nullItem()
            .onItem().delayIt().by(Duration.ofMillis(100))
            .onItem().invoke {
                context.headers.add("method", simpleResourceInfo.methodName)
                context.headers.add("method2", resourceInfo.resourceMethod.name)
            }
            .onItem().delayIt().by(Duration.ofMillis(100))
            .replaceWithVoid()
    }
}
