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
package io.quarkiverse.groovy.hibernate.orm.panache.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.Session;

import io.quarkus.hibernate.orm.panache.common.runtime.AbstractJpaOperations;
import io.quarkus.panache.common.Parameters;

public class JpaOperations extends AbstractJpaOperations<PanacheQueryImpl<?>> {
    /**
     * Provides the default implementations for quarkus to wire up. Should not be used by third party developers.
     */
    public static final JpaOperations INSTANCE = new JpaOperations();

    @Override
    protected PanacheQueryImpl<?> createPanacheQuery(Session session, String query, String originalQuery, String orderBy,
            Object paramsArrayOrMap) {
        return new PanacheQueryImpl<>(session, query, originalQuery, orderBy, paramsArrayOrMap);
    }

    @Override
    public List<?> list(PanacheQueryImpl<?> query) {
        return query.list();
    }

    @Override
    public Stream<?> stream(PanacheQueryImpl<?> query) {
        return query.stream();
    }

    // Avoid method call clashing with delete(Entity) by renaming the delete methods to deleteByQuery
    public long deleteByQuery(Class<?> entityClass, String query, Object... params) {
        return delete(entityClass, query, params);
    }

    public long deleteByQuery(Class<?> entityClass, String query, Map<String, Object> params) {
        return delete(entityClass, query, params);
    }

    public long deleteByQuery(Class<?> entityClass, String query, Parameters params) {
        return delete(entityClass, query, params);
    }
}
