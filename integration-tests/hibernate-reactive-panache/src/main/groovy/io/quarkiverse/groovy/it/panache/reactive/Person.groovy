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
package io.quarkiverse.groovy.it.panache.reactive


import static io.quarkiverse.groovy.hibernate.reactive.panache.runtime.JpaOperations.INSTANCE

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.NamedQueries
import jakarta.persistence.NamedQuery
import jakarta.persistence.OneToMany
import jakarta.persistence.Transient
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlTransient

import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef

import com.fasterxml.jackson.annotation.JsonProperty

import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntity
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntityBase
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheQuery
import io.smallrye.mutiny.Uni

@XmlRootElement
@Entity(name = "Person2")
@NamedQueries([
        @NamedQuery(name = "Person.getByName", query = "from Person2 where name = :name"),
        @NamedQuery(name = "Person.countAll", query = "select count(*) from Person2"),
        @NamedQuery(name = "Person.countByName", query = "select count(*) from Person2 where name = :name"),
        @NamedQuery(name = "Person.countByName.ordinal", query = "select count(*) from Person2 where name = ?1"),
        @NamedQuery(name = "Person.updateAllNames", query = "Update Person2 p set p.name = :name"),
        @NamedQuery(name = "Person.updateNameById", query = "Update Person2 p set p.name = :name where p.id = :id"),
        @NamedQuery(name = "Person.updateNameById.ordinal", query = "Update Person2 p set p.name = ?1 where p.id = ?2"),
        @NamedQuery(name = "Person.deleteAll", query = "delete from Person2"),
        @NamedQuery(name = "Person.deleteById", query = "delete from Person2 p where p.id = :id"),
        @NamedQuery(name = "Person.deleteById.ordinal", query = "delete from Person2 p where p.id = ?1"),
])
@FilterDef(name = "Person.hasName", defaultCondition = "name = :name", parameters = @ParamDef(name = "name", type = String.class))
@FilterDef(name = "Person.isAlive", defaultCondition = "status = 'LIVING'")
@Filter(name = "Person.isAlive")
@Filter(name = "Person.hasName")
class Person extends PanacheEntity {

    public String name
    @Column(unique = true)
    public String uniqueName
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Address address
    @Enumerated(EnumType.STRING)
    public Status status
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Dog> dogs = new ArrayList<>()

    // note that this annotation is automatically added for mapped fields, which is not the case here
    // so we do it manually to emulate a mapped field situation
    @XmlTransient
    @Transient
    public int serialisationTrick

    static Uni<List<Person>> findOrdered() {
        find("ORDER BY name").list()
    }

    // For https://github.com/quarkusio/quarkus/issues/9635
    static <T extends PanacheEntityBase> PanacheQuery<T> find(String query, Object... params) {
        (PanacheQuery<T>) INSTANCE.find(Person.class, query, params)
    }

    // For JAXB: both getter and setter are required
    // Here we make sure the field is not used by Hibernate, but the accessor is used by jaxb, jsonb and jackson
    @JsonProperty
    int getSerialisationTrick() {
        ++serialisationTrick
    }

    void setSerialisationTrick(int serialisationTrick) {
        this.serialisationTrick = serialisationTrick
    }

    static long methodWithPrimitiveParams(boolean b, byte bb, short s, int i, long l, float f, double d, char c) {
        0
    }
}
