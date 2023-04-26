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

import groovy.transform.CompileStatic

import jakarta.enterprise.context.ApplicationScoped

import io.quarkus.vertx.web.Route
import io.quarkus.vertx.web.RoutingExchange

@CompileStatic
@ApplicationScoped 
class MyDeclarativeRoutes {
    // neither path nor regex is set - match a path derived from the method name (ie helloRoute => /hello-route )
    @Route(methods = Route.HttpMethod.GET)
    def helloRoute(RoutingExchange ex) { 
        ex.ok('Hello ' + ex.getParam('name').orElse('Reactive Route') + ' !!')
    }
}
