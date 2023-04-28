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
package io.quarkiverse.groovy.it.panache.resources

import groovy.transform.CompileStatic
import io.quarkiverse.groovy.it.panache.UserRepository

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

// Needed to be able to call the panache repository with proper types
@CompileStatic
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
class UserResource {
    @Inject
    UserRepository userRepository

    @GET
    @Path("/{id}")
    Response get(@PathParam("id") final String id) {
        userRepository.find(id)
                .map({ user -> Response.ok(user).build() })
                .orElse(Response.status(NOT_FOUND).build())
    }
}
