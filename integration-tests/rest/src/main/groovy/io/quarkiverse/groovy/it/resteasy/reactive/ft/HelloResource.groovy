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
package io.quarkiverse.groovy.it.resteasy.reactive.ft

import groovy.transform.CompileStatic
import jakarta.ws.rs.GET
import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path

import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic
@Path("/ft/hello")
class HelloResource {
    private final AtomicBoolean fail = new AtomicBoolean(false)

    @GET
    String get() {
        if (fail.get()) {
            throw new InternalServerErrorException()
        }
        'Hello, world!'
    }

    @POST
    @Path("/fail")
    void startFailing() {
        fail.set(true)
    }

    @POST
    @Path("/heal")
    void stopFailing() {
        fail.set(false)
    }
}
