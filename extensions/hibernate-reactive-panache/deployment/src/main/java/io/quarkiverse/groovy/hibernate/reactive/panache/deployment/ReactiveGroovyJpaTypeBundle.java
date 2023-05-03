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
package io.quarkiverse.groovy.hibernate.reactive.panache.deployment;

import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntity;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheQuery;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheRepository;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkiverse.groovy.hibernate.reactive.panache.runtime.JpaOperations;
import io.quarkus.panache.common.deployment.ByteCodeType;
import io.quarkus.panache.common.deployment.TypeBundle;

public class ReactiveGroovyJpaTypeBundle implements TypeBundle {

    public static final TypeBundle BUNDLE = new ReactiveGroovyJpaTypeBundle();

    @Override
    public ByteCodeType entity() {
        return new ByteCodeType(PanacheEntity.class);
    }

    @Override
    public ByteCodeType entityBase() {
        return new ByteCodeType(PanacheEntityBase.class);
    }

    @Override
    public ByteCodeType operations() {
        return new ByteCodeType(JpaOperations.class);
    }

    @Override
    public ByteCodeType queryType() {
        return new ByteCodeType(PanacheQuery.class);
    }

    @Override
    public ByteCodeType repository() {
        return new ByteCodeType(PanacheRepository.class);
    }

    @Override
    public ByteCodeType repositoryBase() {
        return new ByteCodeType(PanacheRepositoryBase.class);
    }
}