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
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.rest.client.inject.RestClient

import java.time.Duration

@CompileStatic
@Path("country")
class CountriesEndpoint {

    @Inject
    @RestClient
    private CountriesGateway countriesGateway
    @Inject
    private CountryNameConsumer countryNameConsumer
    @Inject
    @Channel("countries-emitter")
    private Emitter<String> countryEmitter

    @GET
    @Path("/name/{name}")
    Uni<Set<Country>> byName(String name) {
        countriesGateway.byName(name).onItem().delayIt().by(Duration.ofMillis(50))
    }

    @POST
    @Path("/kafka/{name}")
    Uni<String> sendCountryNameToKafka(String name) {
        Uni.createFrom().completionStage(countryEmitter.send(name))
            .onItem().delayIt().by(Duration.ofMillis(50))
            .replaceWith(name);
    }

    @GET
    @Path("/resolved")
    Set<Country> resolvedCountries() {
        countryNameConsumer.resolvedCounties
    }
}
