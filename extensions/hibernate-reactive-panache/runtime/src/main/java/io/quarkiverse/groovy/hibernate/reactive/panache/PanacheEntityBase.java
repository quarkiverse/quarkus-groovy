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
package io.quarkiverse.groovy.hibernate.reactive.panache;

import static io.quarkiverse.groovy.hibernate.reactive.panache.runtime.JpaOperations.INSTANCE;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Transient;

import org.hibernate.reactive.mutiny.Mutiny;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.impl.GenerateBridge;
import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * <p>
 * Represents an entity. If your Hibernate entities extend this class they gain auto-generated accessors
 * to all their public fields (unless annotated with {@link Transient}), as well as a lot of useful
 * methods. Unless you have a custom ID strategy, you should not extend this class directly but extend
 * {@link PanacheEntity} instead.
 * </p>
 *
 * @see PanacheEntity
 */
public abstract class PanacheEntityBase {

    /**
     * Returns the current {@link Mutiny.Session}
     *
     * @return the current {@link Mutiny.Session}
     */
    public static Uni<Mutiny.Session> getSession() {
        return INSTANCE.getSession();
    }

    /**
     * Persist this entity in the database, if not already persisted. This will set your ID field if it is not already set.
     *
     * @see #isPersistent()
     * @see #persist(Iterable)
     * @see #persist(Stream)
     * @see #persist(Object, Object...)
     */
    @CheckReturnValue
    public <T extends PanacheEntityBase> Uni<T> persist() {
        return INSTANCE.persist(this).map(v -> (T) this);
    }

    /**
     * Persist this entity in the database, if not already persisted. This will set your ID field if it is not already set.
     * Then flushes all pending changes to the database.
     *
     * @see #isPersistent()
     * @see #persist(Iterable)
     * @see #persist(Stream)
     * @see #persist(Object, Object...)
     */
    @CheckReturnValue
    public <T extends PanacheEntityBase> Uni<T> persistAndFlush() {
        return INSTANCE.persist(this)
                .flatMap(v -> INSTANCE.flush())
                .map(v -> (T) this);
    }

    /**
     * Delete this entity from the database, if it is already persisted.
     *
     * @see #isPersistent()
     * @see PanacheRepository#delete(String, Object...)
     * @see PanacheRepository#delete(String, Map)
     * @see PanacheRepository#delete(String, Parameters)
     * @see PanacheRepository#deleteAll()
     */
    @CheckReturnValue
    public Uni<Void> delete() {
        return INSTANCE.delete(this);
    }

    /**
     * Returns true if this entity is persistent in the database. If yes, all modifications to
     * its persistent fields will be automatically committed to the database at transaction
     * commit time.
     *
     * @return true if this entity is persistent in the database.
     */
    @JsonbTransient
    // @JsonIgnore is here to avoid serialization of this property with jackson
    @JsonIgnore
    public boolean isPersistent() {
        return INSTANCE.isPersistent(this);
    }

    /**
     * Flushes all pending changes to the database.
     */
    @CheckReturnValue
    public Uni<Void> flush() {
        return INSTANCE.flush();
    }

    /**
     * Persist all given entities.
     *
     * @param entities the entities to persist
     * @see #persist()
     * @see #persist(Stream)
     * @see #persist(Object,Object...)
     */
    @CheckReturnValue
    @GenerateBridge(callSuperMethod = true)
    public static Uni<Void> persist(Iterable<?> entities) {
        return INSTANCE.persist(entities);
    }

    /**
     * Persist all given entities.
     *
     * @param entities the entities to persist
     * @see #persist()
     * @see #persist(Iterable)
     * @see #persist(Object,Object...)
     */
    @CheckReturnValue
    @GenerateBridge(callSuperMethod = true)
    public static Uni<Void> persist(Stream<?> entities) {
        return INSTANCE.persist(entities);
    }

    /**
     * Persist all given entities.
     *
     * @param entities the entities to persist
     * @see #persist()
     * @see #persist(Stream)
     * @see #persist(Iterable)
     */
    @CheckReturnValue
    @GenerateBridge(callSuperMethod = true)
    public static Uni<Void> persist(Object firstEntity, Object... entities) {
        return INSTANCE.persist(firstEntity, entities);
    }
}
