/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkiverse.groovy.hibernate.reactive.panache.runtime;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.common.runtime.CommonPanacheQueryImpl;
import io.smallrye.mutiny.Uni;

//TODO this class is only needed by the Spring Data JPA module and would not be placed there if it weren't for a dev-mode classloader issue
// see https://github.com/quarkusio/quarkus/issues/6214
public class CustomCountPanacheQuery<Entity> extends PanacheQueryImpl<Entity> {

    public CustomCountPanacheQuery(Uni<Mutiny.Session> em, String query, String customCountQuery,
            Object paramsArrayOrMap) {
        super(new CommonPanacheQueryImpl<Entity>(em, query, null, null, paramsArrayOrMap) {
            {
                this.customCountQueryForSpring = customCountQuery;
            }
        });
    }
}
