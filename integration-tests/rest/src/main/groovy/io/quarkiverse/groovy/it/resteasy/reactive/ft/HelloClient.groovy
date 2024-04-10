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

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.faulttolerance.ExecutionContext
import org.eclipse.microprofile.faulttolerance.Fallback
import org.eclipse.microprofile.faulttolerance.FallbackHandler
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/ft/hello")
@RegisterRestClient(configKey = "ft-hello")
interface HelloClient {
    @GET @Fallback(HelloFallbackHandler.class) String hello()

    // Need to use a {@code FallbackHandler} instead of initially {@code fallbackMethod} since default methods
    // are not supported in Groovy
    class HelloFallbackHandler implements FallbackHandler<String> {

        @Override
        String handle(ExecutionContext executionContext) {
            "fallback"
        }
    }
}
