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
import io.quarkus.runtime.annotations.RegisterForReflection
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.rest.client.inject.RestClient

import java.util.concurrent.ConcurrentHashMap

@RegisterForReflection // Needed to allow the closure accessing {@code resolvedCounties}
@CompileStatic
@ApplicationScoped
class CountryNameConsumer {

    @Inject
    @RestClient
    private CountriesGateway countryGateway

    Set<Country> resolvedCounties = ConcurrentHashMap.newKeySet()

    @Incoming("countries-t2-in")
    Uni<Void> consume(String countryName) {
        Uni.combine().all().unis(countryGateway.byName("fake$countryName"), countryGateway.byName(countryName))
            .asTuple().onItem().transform {
                resolvedCounties.addAll(it.getItem1())
                resolvedCounties.addAll(it.getItem2())
            }
            .replaceWithVoid()
    }
}
