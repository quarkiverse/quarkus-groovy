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
import io.quarkus.hibernate.reactive.panache.common.runtime.CommonPanacheQueryImpl;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;

public class PanacheQueryImpl<Entity> implements PanacheQuery<Entity> {

    private final CommonPanacheQueryImpl<Entity> delegate;

    PanacheQueryImpl(Uni<Mutiny.Session> em, String query, String originalQuery, String orderBy, Object paramsArrayOrMap) {
        this.delegate = new CommonPanacheQueryImpl<>(em, query, originalQuery, orderBy, paramsArrayOrMap);
    }

    protected PanacheQueryImpl(CommonPanacheQueryImpl<Entity> delegate) {
        this.delegate = delegate;
    }

    // Builder

    @Override
    public <T> PanacheQuery<T> project(Class<T> type) {
        return new PanacheQueryImpl<>(delegate.project(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> PanacheQuery<T> page(Page page) {
        delegate.page(page);
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> page(int pageIndex, int pageSize) {
        delegate.page(pageIndex, pageSize);
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> nextPage() {
        delegate.nextPage();
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> previousPage() {
        delegate.previousPage();
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> firstPage() {
        delegate.firstPage();
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Uni<PanacheQuery<T>> lastPage() {
        return delegate.lastPage().map(v -> (PanacheQuery<T>) this);
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
    public <T extends Entity> PanacheQuery<T> range(int startIndex, int lastIndex) {
        delegate.range(startIndex, lastIndex);
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> withLock(LockModeType lockModeType) {
        delegate.withLock(lockModeType);
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> withHint(String hintName, Object value) {
        delegate.withHint(hintName, value);
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> filter(String filterName, Parameters parameters) {
        delegate.filter(filterName, parameters.map());
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> filter(String filterName, Map<String, Object> parameters) {
        delegate.filter(filterName, parameters);
        return (PanacheQuery<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> PanacheQuery<T> filter(String filterName) {
        delegate.filter(filterName, Collections.emptyMap());
        return (PanacheQuery<T>) this;
    }

    // Results

    @Override
    public Uni<Long> count() {
        return delegate.count();
    }

    @Override
    public <T extends Entity> Uni<List<T>> list() {
        return delegate.list();
    }

    @Override
    public <T extends Entity> Uni<T> firstResult() {
        return delegate.firstResult();
    }

    @Override
    public <T extends Entity> Uni<T> singleResult() {
        return delegate.singleResult();
    }
}
