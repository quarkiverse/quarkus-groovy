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
package io.quarkiverse.groovy.it.resteasy

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/extension")
class GroovyExtensionModuleResource {

    @GET
    @Path("/static")
    @Produces(MediaType.TEXT_PLAIN)
    def staticExtension() {
        String.greeting()
    }

    @GET
    @Path("/instance")
    @Produces(MediaType.TEXT_PLAIN)
    def instanceExtension() {
        int i=0
        5.maxRetries {
            i++
        }
        assert i == 1
        i=0
        try {
            5.maxRetries {
                i++
                throw new RuntimeException("oops")
            }
        } catch (RuntimeException e) {
            assert i == 5
        }
        "Tried $i times"
    }
}
