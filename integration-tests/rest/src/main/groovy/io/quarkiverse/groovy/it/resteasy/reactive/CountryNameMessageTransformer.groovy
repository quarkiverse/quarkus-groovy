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
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Outgoing

import java.time.Duration

@CompileStatic
@ApplicationScoped
class CountryNameMessageTransformer {

    @Incoming("countries-t1-in")
    @Outgoing("countries-t2-out")
    Uni<Message<String>> transform(Message<String> input) {
        Uni.createFrom().item(input.withPayload(input.payload.toLowerCase()))
                .onItem().delayIt().by(Duration.ofMillis(100))
    }
}
