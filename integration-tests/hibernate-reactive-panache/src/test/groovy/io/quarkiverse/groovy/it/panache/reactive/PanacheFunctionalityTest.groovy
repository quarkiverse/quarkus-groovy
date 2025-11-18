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

import jakarta.inject.Inject
import org.hibernate.reactive.mutiny.Mutiny
import org.junit.jupiter.api.Disabled

import static org.hamcrest.Matchers.is
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

import jakarta.json.bind.Jsonb
import jakarta.json.bind.JsonbBuilder
import jakarta.persistence.PersistenceException

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

import io.quarkiverse.groovy.hibernate.reactive.panache.Panache
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional
import io.quarkus.test.TestReactiveTransaction
import io.quarkus.test.junit.DisabledOnIntegrationTest
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.smallrye.mutiny.Uni

/**
 * Test various Panache operations running in Quarkus
 */
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class PanacheFunctionalityTest {

    PersonRepository personDao

    /**
     * Tests that direct use of the entity in the test class does not break transformation
     *
     * see https://github.com/quarkusio/quarkus/issues/1724
     */
    @SuppressWarnings("unused")
    Person p = new Person()

    @Inject
    void setPersonDao(PersonRepository personDao) {
        this.personDao = personDao
    }

    @Test
    void testPanacheFunctionality() throws Exception {
        RestAssured.when().get("/test/model-dao").then().body(is("OK"))
        RestAssured.when().get("/test/model").then().body(is("OK"))
        RestAssured.when().get("/test/accessors").then().body(is("OK"))

        RestAssured.when().get("/test/model1").then().body(is("OK"))
        RestAssured.when().get("/test/model2").then().body(is("OK"))
        // TODO: Fix behavior change since Quarkus 3.26.0 ref https://github.com/quarkiverse/quarkus-groovy/issues/296
        //RestAssured.when().get("/test/projection1").then().body(is("OK"))
        RestAssured.when().get("/test/projection2").then().body(is("OK"))
        RestAssured.when().get("/test/model3").then().body(is("OK"))
    }

    @Test
    void testPanacheSerialisation() {
        RestAssured.given().accept(ContentType.JSON)
                .when().get('/test/ignored-properties')
                .then()
                .body(is('{"id":666,"dogs":[],"name":"Eddie","serialisationTrick":1,"status":"DECEASED"}'))
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    void testPanacheInTest(UniAsserter asserter) {
        asserter.assertEquals({ Panache.withSession({ personDao.count() }) }, 0l)
    }

    @Disabled("https://github.com/quarkiverse/quarkus-groovy/issues/329")
    @Test
    void testBug5274() {
        RestAssured.when().get("/test/5274").then().body(is("OK"))
    }

    @Disabled("https://github.com/quarkiverse/quarkus-groovy/issues/329")
    @Test
    void testBug5885() {
        RestAssured.when().get("/test/5885").then().body(is("OK"))
    }

    /**
     * _PanacheEntityBase_ has the method _isPersistent_. This method is used by Jackson to serialize the attribute *persistent*
     * in the JSON which is not intended. This test ensures that the attribute *persistent* is not generated when using Jackson.
     *
     * This test does not interact with the Quarkus application itself. It is just using the Jackson ObjectMapper with a
     * PanacheEntity. Thus this test is disabled in native mode. The test code runs the JVM and not native.
     */
    @DisabledOnIntegrationTest
    @Test
    void jacksonDeserializationIgnoresPersistentAttribute() throws JsonProcessingException {
        // set Up
        Person person = new Person()
        person.name = "max"
        // do
        ObjectMapper objectMapper = new ObjectMapper()
        // make sure the Jaxb module is loaded
        objectMapper.findAndRegisterModules()
        String personAsString = objectMapper.writeValueAsString(person)
        // check
        // hence no 'persistence'-attribute
        assertEquals(
                "{\"id\":null,\"name\":\"max\",\"uniqueName\":null,\"address\":null,\"status\":null,\"dogs\":[],\"serialisationTrick\":1}",
                personAsString)
    }

    /**
     * This test is disabled in native mode as there is no interaction with the quarkus integration test endpoint.
     */
    @DisabledOnIntegrationTest
    @Test
    void jsonbDeserializationHasAllFields() throws JsonProcessingException {
        // set Up
        Person person = new Person()
        person.name = "max"
        // do

        Jsonb jsonb = JsonbBuilder.create()
        String json = jsonb.toJson(person)
        assertEquals(
                "{\"dogs\":[],\"name\":\"max\",\"serialisationTrick\":1}",
                json)
    }

    @Test
    void testCompositeKey() {
        RestAssured.when()
                .get("/test/composite")
                .then()
                .body(is("OK"))
    }

    @Test
    void testBug7721() {
        RestAssured.when().get("/test/7721").then().body(is("OK"))
    }

    @Test
    void testBug8254() {
        RestAssured.when().get("/test/8254").then().body(is("OK"))
    }

    @Test
    void testBug9025() {
        RestAssured.when().get("/test/9025").then().body(is("OK"))
    }

    @Test
    void testBug9036() {
        RestAssured.when().get("/test/9036").then().body(is("OK"))
    }

    @Test
    void testSortByNullPrecedence() {
        RestAssured.when().get("/test/testSortByNullPrecedence").then().body(is("OK"))
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    void testTransaction(UniAsserter asserter) {
        asserter.assertNotNull({ Panache.withTransaction({ Panache.currentTransaction() }) })
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    void testNoTransaction(UniAsserter asserter) {
        asserter.assertNull({ Panache.withSession({ Panache.currentTransaction() }) })
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    void testBug7102(UniAsserter asserter) {
        asserter.execute({ createBug7102()
                .flatMap { person ->
                    getBug7102(person.id)
                            .flatMap { person1 ->
                                Assertions.assertEquals("pero", person1.name)
                                updateBug7102(person.id)
                            }
                            .flatMap{ v -> getBug7102(person.id) }
                            .map { person2 ->
                                Assertions.assertEquals("jozo", person2.name)
                                null
                            }
                }.flatMap { v -> Panache.withTransaction({ personDao.deleteAll() }) }})
    }

    @WithTransaction
    Uni<Person> createBug7102() {
        Person personPanache = new Person().tap {
            name = "pero"
        }
        personPanache.persistAndFlush().map({ v -> personPanache })
    }

    @WithTransaction
    Uni<Void> updateBug7102(Long id) {
        personDao.findById(id)
                .map({person ->
                    person.name = "jozo"
                    null
                })
                .replaceWithVoid()
    }

    @WithSession
    Uni<Person> getBug7102(Long id) {
        personDao.findById(id)
    }

    @DisabledOnIntegrationTest
    @TestReactiveTransaction
    @Test
    @Order(100)
    void testTestTransaction(UniAsserter asserter) {
        asserter.assertNotNull({ Panache.currentTransaction() })
        asserter.assertEquals({ personDao.count() }, 0l)
        asserter.assertNotNull({ new Person().persist() })
        asserter.assertEquals({ personDao.count() }, 1l)
    }

    @DisabledOnIntegrationTest
    @TestReactiveTransaction
    @Test
    @Order(101)
    void testTestTransaction2(UniAsserter asserter) {
        asserter.assertNotNull({ Panache.currentTransaction() })
        // make sure the previous one was rolled back
        asserter.assertEquals({ personDao.count() }, 0l)
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    @Order(200)
    void testReactiveTransactional(UniAsserter asserter) {
        asserter.assertEquals({ reactiveTransactional() }, 1l)
    }

    @WithTransaction
    Uni<Long> reactiveTransactional() {
        Panache.currentTransaction()
                .map { tx -> assertNotNull(tx) }
                .flatMap { tx -> personDao.count() }
                .map { count -> assertEquals(0l, count) }
                .flatMap { new Person().persist() }
                .flatMap { tx -> personDao.count() }
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    @Order(201)
    void testReactiveTransactional2(UniAsserter asserter) {
        asserter.assertTrue( {reactiveTransactional2()})
    }

    @WithTransaction
    Uni<Boolean> reactiveTransactional2() {
        Panache.currentTransaction()
                .map { tx -> assertNotNull(tx) }
                .flatMap { tx -> personDao.count() }
                .map { count -> assertEquals(1l, count) }
                .flatMap { personDao.deleteAll() }
                .map { count -> assertEquals(1l, count) }
                .flatMap { Panache.currentTransaction() }
                .map { Mutiny.Transaction tx -> tx.markForRollback() }
                .map { tx -> true }
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    @Order(202)
    void testReactiveTransactional3(UniAsserter asserter) {
        asserter.assertEquals({ testReactiveTransactional3() }, 1l)
    }

    @ReactiveTransactional
    Uni<Long> testReactiveTransactional3() {
        Panache.currentTransaction()
                .map { tx -> assertNotNull(tx) }
                .flatMap({ tx -> personDao.count() })
                // make sure it was rolled back
                .map { count -> assertEquals(1l, count) }
                .flatMap { personDao.deleteAll() }
    }

    @DisabledOnIntegrationTest
    @RunOnVertxContext
    @Test
    @Order(300)
    void testPersistenceException(UniAsserter asserter) {
        asserter.assertFailedWith({ Panache.withTransaction({ new Person().delete() }) }, PersistenceException.class)
    }
}
