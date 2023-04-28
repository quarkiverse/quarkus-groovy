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
package io.quarkiverse.groovy.it.panache

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatCode

import jakarta.inject.Inject

import org.junit.jupiter.api.Test

import io.quarkus.panache.common.Parameters
import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
class DuplicateMethodTest {

    @Inject
    DuplicateRepository repository

    @Test
    void shouldNotDuplicateMethodsInRepository() {
        assertThat(repository.findById(1)).isNotNull()
    }

    @Test
    void shouldNotDuplicateMethodsInEntity() {
        DuplicateEntity entity = DuplicateEntity.findById(1)
        assertThat(entity).isNotNull()
        assertThatCode({ entity.persist() }).doesNotThrowAnyException()
        assertThatCode({ DuplicateEntity.update("foo", Parameters.with("a", 1)) })
                .doesNotThrowAnyException()
    }
}
