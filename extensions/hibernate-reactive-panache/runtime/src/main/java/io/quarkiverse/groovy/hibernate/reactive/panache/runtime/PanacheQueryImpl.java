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
package io.quarkiverse.groovy.hibernate.reactive.panache.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.persistence.LockModeType;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.runtime.CommonManagedPanacheQueryImpl;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

public class PanacheQueryImpl<Entity> implements PanacheQuery<Entity> {

    private final CommonManagedPanacheQueryImpl<Entity> delegate;

    PanacheQueryImpl(Uni<Mutiny.Session> em, Class<?> entityClass, String query, String originalQuery, Sort sort,
            Object paramsArrayOrMap) {
        this.delegate = new CommonManagedPanacheQueryImpl<>(em, entityClass, query, originalQuery, sort,
                paramsArrayOrMap);
    }

    protected PanacheQueryImpl(CommonManagedPanacheQueryImpl<Entity> delegate) {
        this.delegate = delegate;
    }

    // Builder

    @Override
    public <T> PanacheQuery<T> project(Class<T> type) {
        return new PanacheQueryImpl<>(delegate.project(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public PanacheQuery<Entity> page(Page page) {
        delegate.page(page);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> page(int pageIndex, int pageSize) {
        delegate.page(pageIndex, pageSize);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> nextPage() {
        delegate.nextPage();
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> previousPage() {
        delegate.previousPage();
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> firstPage() {
        delegate.firstPage();
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Uni<PanacheQuery<Entity>> lastPage() {
        return delegate.lastPage().map(v -> this);
    }

    @Override
    public Uni<Boolean> hasNextPage() {
        return delegate.hasNextPage();
    }

    @Override
    public boolean hasPreviousPage() {
        return delegate.hasPreviousPage();
    }

    @Override
    public Uni<Integer> pageCount() {
        return delegate.pageCount();
    }

    @Override
    public Page page() {
        return delegate.page();
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> range(int startIndex, int lastIndex) {
        delegate.range(startIndex, lastIndex);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> withLock(LockModeType lockModeType) {
        delegate.withLock(lockModeType);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> withHint(String hintName, Object value) {
        delegate.withHint(hintName, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> filter(String filterName, Parameters parameters) {
        delegate.filter(filterName, parameters.map());
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> filter(String filterName, Map<String, Object> parameters) {
        delegate.filter(filterName, parameters);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PanacheQuery<Entity> filter(String filterName) {
        delegate.filter(filterName, Collections.emptyMap());
        return this;
    }

    // Results

    @Override
    public Uni<Long> count() {
        return delegate.count();
    }

    @Override
    public Uni<List<Entity>> list() {
        return delegate.list();
    }

    @Override
    public Uni<Entity> firstResult() {
        return delegate.firstResult();
    }

    @Override
    public Uni<Entity> singleResult() {
        return delegate.singleResult();
    }
}
