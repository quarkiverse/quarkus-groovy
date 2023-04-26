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
package io.quarkiverse.groovy.it.shared

import groovy.transform.CompileStatic
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MultivaluedMap
import jakarta.ws.rs.ext.MessageBodyReader
import jakarta.ws.rs.ext.MessageBodyWriter

import java.lang.annotation.Annotation
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

@CompileStatic
@Produces(["application/json", "text/plain"])
@Consumes(["application/json", "text/plain"])
class SharedProvider implements MessageBodyReader<Shared>, MessageBodyWriter<Shared> {

    @Override
    boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        type == Shared.class
    }

    @Override
    Shared readFrom(Class<Shared> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        new Shared("canned")
    }

    @Override
    boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        type == Shared.class
    }

    @Override
    void writeTo(Shared shared, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream?.write(
                sprintf("{\"message\": \"canned+%s\"}", shared?.message)
                        .getBytes(StandardCharsets.UTF_8)
        )
    }
}
