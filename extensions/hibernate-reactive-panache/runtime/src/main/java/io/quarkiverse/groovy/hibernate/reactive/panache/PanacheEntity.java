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

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * <p>
 * Represents an entity with a generated ID field {@link #id} of type {@link Long}. If your
 * Hibernate entities extend this class they gain the ID field and auto-generated accessors
 * to all their public fields (unless annotated with {@link Transient}), as well as all
 * the useful methods from {@link PanacheEntityBase}.
 * </p>
 * <p>
 * If you want a custom ID type or strategy, you can directly extend {@link PanacheEntityBase}
 * instead, and write your own ID field. You will still get auto-generated accessors and
 * all the useful methods.
 * </p>
 *
 * @see PanacheEntityBase
 */
@MappedSuperclass
public abstract class PanacheEntity extends PanacheEntityBase {

    /**
     * The auto-generated ID field. This field is set by Hibernate ORM when this entity
     * is persisted.
     *
     * @see #persist()
     */
    @Id
    @GeneratedValue
    public Long id;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "<" + id + ">";
    }
}
