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
package org.acme

import org.eclipse.microprofile.config.inject.ConfigProperty

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/hello")
class HelloResource {

    @Inject
    GreetingService greetingService
    @Inject
    @ConfigProperty(name = "greeting")
    String greeting

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    def hello() {
        'hello'
    }

    @GET
    @Path("/greeting")
    @Produces(MediaType.TEXT_PLAIN)
    def greeting() {
        greeting
    }

    @GET
    @Path("/bean")
    @Produces(MediaType.TEXT_PLAIN)
    def greetingFromBean() {
        greetingService.greet()
    }
}
