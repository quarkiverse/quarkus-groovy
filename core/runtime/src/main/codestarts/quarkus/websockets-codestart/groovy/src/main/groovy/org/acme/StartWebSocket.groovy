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

import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint

@ServerEndpoint("/start-websocket/{name}")
@ApplicationScoped
class StartWebSocket {

    @OnOpen
    void onOpen(Session session, @PathParam("name") String name) {
        println "onOpen> ${name}"
    }

    @OnClose
    void onClose(Session session, @PathParam("name") String name) {
        println "onClose> ${name}"
    }

    @OnError
    void onError(Session session, @PathParam("name") String name, Throwable throwable) {
        println "onError> ${name}: ${throwable}"
    }

    @OnMessage
    void onMessage(String message, @PathParam("name") String name) {
        println "onMessage> ${name}: ${message}"
    }
}
