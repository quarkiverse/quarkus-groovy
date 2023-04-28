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
package io.quarkiverse.groovy.it.panache

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/persistence-unit")
class PanacheTestResource {

    @Inject
    FirstRepository firstRepository
    @Inject
    SecondRepository secondRepository

    @GET
    @Path("/first/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    Long createWithFirstPuAndReturnCount(@PathParam("name") String name) {
        FirstEntity entity = new FirstEntity()
        entity.name = name
        entity.persistAndFlush()
        firstRepository.count()
    }

    @GET
    @Path("/second/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    Long createWithSecondPUAndReturnCount(@PathParam("name") String name) {
        SecondEntity entity = new SecondEntity()
        entity.name = name
        entity.persistAndFlush()
        secondRepository.count()
    }
}
