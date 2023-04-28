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
package io.quarkiverse.groovy.hibernate.orm.panache;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkiverse.groovy.hibernate.orm.panache.runtime.JpaOperations;
import io.quarkus.panache.common.Parameters;

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
     * Persist this entity in the database, if not already persisted. This will set your ID field if it is not already set.
     *
     * @see #isPersistent()
     * @see PanacheRepository#persist(Iterable)
     * @see PanacheRepository#persist(Stream)
     * @see PanacheRepository#persist(Object, Object...)
     */
    public void persist() {
        JpaOperations.INSTANCE.persist(this);
    }

    /**
     * Persist this entity in the database, if not already persisted. This will set your ID field if it is not already set.
     * Then flushes all pending changes to the database.
     *
     * @see #isPersistent()
     * @see PanacheRepository#persist(Iterable)
     * @see PanacheRepository#persist(Stream)
     * @see PanacheRepository#persist(Object, Object...)
     */
    public void persistAndFlush() {
        JpaOperations.INSTANCE.persist(this);
        JpaOperations.INSTANCE.flush(this);
    }

    /**
     * Delete this entity from the database, if it is already persisted.
     *
     * @see #isPersistent()
     * @see PanacheRepository#deleteByQuery(String, Object...)
     * @see PanacheRepository#deleteByQuery(String, Map)
     * @see PanacheRepository#deleteByQuery(String, Parameters)
     * @see PanacheRepository#deleteAll()
     */
    public void delete() {
        JpaOperations.INSTANCE.delete(this);
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
        return JpaOperations.INSTANCE.isPersistent(this);
    }

}
