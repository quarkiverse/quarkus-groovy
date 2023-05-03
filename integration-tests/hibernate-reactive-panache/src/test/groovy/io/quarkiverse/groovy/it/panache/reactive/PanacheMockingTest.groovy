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

import groovy.transform.CompileStatic

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

import jakarta.inject.Inject
import jakarta.persistence.LockModeType
import jakarta.ws.rs.WebApplicationException

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.Mockito

import io.quarkiverse.groovy.hibernate.reactive.panache.Panache
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheRepositoryBase
import io.quarkus.panache.mock.PanacheMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.smallrye.mutiny.Uni

// Required to use PanacheMock.verify without side effects
@CompileStatic
@QuarkusTest
class PanacheMockingTest {

    @SuppressWarnings("static-access")
    @Test
    @RunOnVertxContext
    @Order(1)
    void testPanacheMocking(UniAsserter asserter) {
        String key = "person"
        def mockRepository = Mockito.mock(PersonRepository.class)
        asserter.execute({ PanacheMock.mock(Person.class) })
        asserter.assertEquals({ mockRepository.count() }, 0l)

        asserter.execute({ Mockito.when(mockRepository.count()).thenReturn(Uni.createFrom().item(23l)) })
        asserter.assertEquals({ mockRepository.count() }, 23l)

        asserter.execute({ Mockito.when(mockRepository.count()).thenReturn(Uni.createFrom().item(42l)) })
        asserter.assertEquals({ mockRepository.count() }, 42l)

        asserter.execute({ Mockito.when(mockRepository.count()).thenCallRealMethod() })
        asserter.assertEquals({ mockRepository.count() }, 0l)

        asserter.execute({
            // use block lambda here, otherwise mutiny fails with NPE
            Mockito.verify(mockRepository, Mockito.times(4)).count()
        })

        asserter.execute({
            Person p = new Person()
            Mockito.when(mockRepository.findById(12l)).thenReturn(Uni.createFrom().item(p))
            asserter.putData(key, p)
        })
        asserter.assertThat({ mockRepository.findById(12l) }, { p -> Assertions.assertSame(p, asserter.getData(key)) })
        asserter.assertNull({ mockRepository.findById(42l) })

        asserter.execute({ mockRepository.persist(asserter.getData(key) as Person) })
        asserter.execute({ assertNull(((Person) asserter.getData(key)).id) })

        asserter.execute({ Mockito.when(mockRepository.findById(12l)).thenThrow(new WebApplicationException()) })
        asserter.assertFailedWith( {
            try {
                return mockRepository.findById(12l)
            } catch (Exception e) {
                return Uni.createFrom().failure(e)
            }
        }, { t -> assertEquals(WebApplicationException.class, t.getClass()) })

        asserter.execute({ Mockito.when(Person.findOrdered()).thenReturn(Uni.createFrom().item(List.<Person> of())) })
        asserter.assertThat({ Person.findOrdered() }, { List list -> list.isEmpty() })

        asserter.execute({
            PanacheMock.verify(Person.class).findOrdered()
            Mockito.verify(mockRepository, Mockito.atLeastOnce()).findById(Mockito.any())
            Assertions.assertEquals(0, Person.methodWithPrimitiveParams(true as boolean, 0 as byte, 0 as short, 0 as int, 2 as int, 2.0f as float, 2.0 as double, 'c' as char))
        })

        // Execute the asserter within a reactive session
        asserter.surroundWith({ u -> Panache.withSession({ u }) })
    }

    @Test
    @Order(2)
    void testPanacheMockingWasCleared() {
        Assertions.assertFalse(PanacheMock.IsMockEnabled)
    }

    @InjectMock
    MockablePersonRepository mockablePersonRepository

    @RunOnVertxContext
    @Test
    void testPanacheRepositoryMocking(UniAsserter asserter) throws Throwable {
        String key = "person"

        asserter.assertEquals({ mockablePersonRepository.count() }, 0l)

        asserter.execute({ Mockito.when(mockablePersonRepository.count()).thenReturn(Uni.createFrom().item(23l)) })
        asserter.assertEquals({ mockablePersonRepository.count() }, 23l)

        asserter.execute({ Mockito.when(mockablePersonRepository.count()).thenReturn(Uni.createFrom().item(42l)) })
        asserter.assertEquals({ mockablePersonRepository.count() }, 42l)

        asserter.execute({ Mockito.when(mockablePersonRepository.count()).thenCallRealMethod() })
        asserter.assertEquals({ mockablePersonRepository.count() }, 0l)

        asserter.execute({
            // use block lambda here, otherwise mutiny fails with NPE
            Mockito.verify(mockablePersonRepository, Mockito.times(4)).count()
        })

        asserter.execute({
            Person p = new Person()
            Mockito.when(mockablePersonRepository.findById(12l)).thenReturn(Uni.createFrom().item(p))
            asserter.putData(key, p)
        })

        asserter.assertThat({ mockablePersonRepository.findById(12l) }, { p -> Assertions.assertSame(p, asserter.getData(key)) })
        asserter.assertNull({ mockablePersonRepository.findById(42l) })

        asserter.execute({ mockablePersonRepository.persist((Person) asserter.getData(key)) })
        asserter.execute({ assertNull(((Person) asserter.getData(key)).id) })

        asserter.execute({ Mockito.when(mockablePersonRepository.findById(12l)).thenThrow(new WebApplicationException()) })
        asserter.assertFailedWith( {
            try {
                return mockablePersonRepository.findById(12l)
            } catch (Exception e) {
                return Uni.createFrom().failure(e)
            }
        }, { t -> assertEquals(WebApplicationException.class, t.getClass()) })

        asserter.execute({ Mockito.when(mockablePersonRepository.findOrdered())
                .thenReturn(Uni.createFrom().item(List.<Person>of()))})
        asserter.assertThat({ mockablePersonRepository.findOrdered() } , {List list -> list.isEmpty()})

        asserter.execute({
            Mockito.verify(mockablePersonRepository).findOrdered()
            Mockito.verify(mockablePersonRepository, Mockito.atLeastOnce()).findById(Mockito.any())
            Mockito.verify(mockablePersonRepository).persist(Mockito.<Person> any())
            Mockito.verifyNoMoreInteractions(mockablePersonRepository)
        })

        // Execute the asserter within a reactive session
        asserter.surroundWith({ u -> Panache.withSession({ u }) })
    }

    @Inject
    PersonRepository realPersonRepository

    @SuppressWarnings([ "unchecked", "rawtypes" ])
    @RunOnVertxContext
    @Test
    void testPanacheRepositoryBridges(UniAsserter asserter) {
        // normal method call
        asserter.assertNull({ realPersonRepository.findById(0l) })
        // bridge call
        asserter.assertNull({ ((PanacheRepositoryBase) realPersonRepository).findById(0l) })
        // normal method call
        asserter.assertNull({ realPersonRepository.findById(0l, LockModeType.NONE) })
        // bridge call
        asserter.assertNull({ ((PanacheRepositoryBase) realPersonRepository).findById(0l, LockModeType.NONE) })

        // normal method call
        asserter.assertFalse({ realPersonRepository.deleteById(0l) })
        // bridge call
        asserter.assertFalse({ ((PanacheRepositoryBase) realPersonRepository).deleteById(0l) })

        // Execute the asserter within a reactive session
        asserter.surroundWith({ u -> Panache.withSession({ u }) })
    }
}
