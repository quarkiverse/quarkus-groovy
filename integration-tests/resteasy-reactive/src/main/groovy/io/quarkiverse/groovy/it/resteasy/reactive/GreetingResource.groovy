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
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.jboss.resteasy.reactive.RestHeader

@RegisterForReflection(targets = [HttpHeaders.class])
@Path("/greeting")
class GreetingResource {

    @Inject
    HttpHeaders headers

    @GET
    Greeting testSuspend(@RestHeader("firstName") String firstName) {
        var lastName = headers.getHeaderString("lastName")
        new Greeting("hello $firstName $lastName")
    }

    @GET @Path("noop") def noop() {}

    @POST
    @Path("body/{name}")
    def body(
        @PathParam(value = "name") String name,
        Greeting greeting,
        @Context UriInfo uriInfo
    ) {
        Response.ok(greeting).build()
    }
}

@RegisterForReflection
class Greeting {
    String message
    Greeting(String message) {
        this.message = message
    }
}
