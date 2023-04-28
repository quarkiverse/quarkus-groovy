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

import jakarta.inject.Inject
import jakarta.transaction.Transactional

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer.MethodName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest

/**
 * Tests that @TestTransaction works as expected when only used for a single test method
 */
@QuarkusTest
@TestMethodOrder(MethodName.class)
class TestTransactionOnSingleMethodTest {

    @Inject
    BeerRepository beerRepository

    @Test
    @TestTransaction
    void test1() {
        Assertions.assertEquals(0, beerRepository.find("name", "Lager").count())
        Beer b = new Beer()
        b.name = "Lager"
        beerRepository.persist(b)
    }

    @Test
    @Transactional
    void test2() {
        Assertions.assertEquals(0, beerRepository.find("name", "Lager").count())
        Beer b = new Beer()
        b.name = "Lager"
        beerRepository.persist(b)
    }

    @Test
    @Transactional
    void test3() {
        Assertions.assertEquals(1, beerRepository.find("name", "Lager").count())
        beerRepository.deleteAll()
    }
}
