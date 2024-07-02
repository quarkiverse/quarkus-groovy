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

import groovy.transform.CompileStatic
import io.quarkiverse.groovy.hibernate.orm.panache.PanacheRepositoryBase
import io.quarkus.test.InjectMock
import jakarta.inject.Inject
import jakarta.persistence.LockModeType
import jakarta.ws.rs.WebApplicationException

import org.hibernate.Session
import org.hibernate.query.Query
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.Mockito

import io.quarkus.panache.mock.PanacheMock
import io.quarkus.test.junit.QuarkusTest

// Needed to be able to use PanacheMock
@CompileStatic
@QuarkusTest
class PanacheMockingTest {

    @InjectMock
    Session session

    @BeforeEach
    void setup() {
        Query mockQuery = Mockito.mock(Query.class)
        Mockito.doNothing().when(session).persist(Mockito.any())
        Mockito.when(session.createSelectionQuery(Mockito.anyString(), Mockito.any())).thenReturn(mockQuery)
        Mockito.when(mockQuery.getSingleResult()).thenReturn(0l)
    }


    @Test
    @Order(1)
    void testPanacheMocking() {
        PanacheMock.mock(Person.class)

        def mock = PanacheMock.getMock(Person.class)

        // does not throw (defaults to doNothing)
        mock.voidMethod()

        // make it throw our exception

        Mockito.doThrow(new RuntimeException("Stef")).when(mock).voidMethod()
        try {
            mock.voidMethod()
            Assertions.fail()
        } catch (RuntimeException x) {
            Assertions.assertEquals("Stef", x.getMessage())
        }

        // change our exception
        Mockito.doThrow(new RuntimeException("Stef2")).when(mock).voidMethod()
        try {
            mock.voidMethod()
            Assertions.fail()
        } catch (RuntimeException x) {
            Assertions.assertEquals("Stef2", x.getMessage())
        }

        // back to doNothing
        Mockito.doNothing().when(mock).voidMethod()
        mock.voidMethod()

        // make it be called
        Mockito.doCallRealMethod().when(mock).voidMethod()
        try {
            mock.voidMethod()
            Assertions.fail()
        } catch (RuntimeException x) {
            Assertions.assertEquals("void", x.getMessage())
        }

        def mockRepository = Mockito.mock(PersonRepository.class)

        Assertions.assertEquals(0, mockRepository.count())

        Mockito.when(mockRepository.count()).thenReturn(23l)
        Assertions.assertEquals(23, mockRepository.count())

        Mockito.when(mockRepository.count()).thenReturn(42l)
        Assertions.assertEquals(42, mockRepository.count())

        Mockito.when(mockRepository.count()).thenCallRealMethod()
        Assertions.assertEquals(0, mockRepository.count())

        Mockito.verify(mockRepository, Mockito.times(4)).count()

        Person p = new Person()
        Mockito.when(mockRepository.findById(12l)).thenReturn(p)
        Assertions.assertSame(p, mockRepository.findById(12l))
        Assertions.assertNull(mockRepository.findById(42l))

        mockRepository.persist(p)
        Assertions.assertNull(p.id)
        // mocked via EntityManager mocking
        p.persist()
        Assertions.assertNull(p.id)

        Mockito.when(mockRepository.findById(12l)).thenThrow(new WebApplicationException())
        try {
            mockRepository.findById(12l)
            Assertions.fail()
        } catch (WebApplicationException x) {
        }

        Mockito.when(Person.findOrdered()).thenReturn(Collections.emptyList() as List<Person>)
        Assertions.assertTrue(Person.findOrdered().isEmpty())

        Mockito.verify(mock, Mockito.atLeast(5)).voidMethod()
        Mockito.verify(mock).findOrdered()
        Mockito.verify(mockRepository).persist(Mockito.<Person> any())
        Mockito.verify(mockRepository, Mockito.atLeastOnce()).findById(Mockito.any())
        Mockito.verify(session, Mockito.times(1)).persist(Mockito.any())
        Assertions.assertEquals(0, Person.methodWithPrimitiveParams(true as boolean, 0 as byte, 0 as short, 0 as int, 2 as long, 2.0f as float, 2.0 as double, 'c' as char))
    }

    @Test
    @Order(2)
    void testPanacheMockingWasCleared() {
        Assertions.assertFalse(PanacheMock.IsMockEnabled)
    }

    @InjectMock
    MockablePersonRepository mockablePersonRepository

    @Test
    void testPanacheRepositoryMocking() throws Throwable {
        Assertions.assertEquals(0, mockablePersonRepository.count())

        Mockito.when(mockablePersonRepository.count()).thenReturn(23l)
        Assertions.assertEquals(23, mockablePersonRepository.count())

        Mockito.when(mockablePersonRepository.count()).thenReturn(42l)
        Assertions.assertEquals(42, mockablePersonRepository.count())

        Mockito.when(mockablePersonRepository.count()).thenCallRealMethod()
        Assertions.assertEquals(0, mockablePersonRepository.count())

        Mockito.verify(mockablePersonRepository, Mockito.times(4)).count()

        Person p = new Person()
        Mockito.when(mockablePersonRepository.findById(12l)).thenReturn(p)
        Assertions.assertSame(p, mockablePersonRepository.findById(12l))
        Assertions.assertNull(mockablePersonRepository.findById(42l))

        mockablePersonRepository.persist(p)
        Assertions.assertNull(p.id)

        Mockito.when(mockablePersonRepository.findById(12l)).thenThrow(new WebApplicationException())
        try {
            mockablePersonRepository.findById(12l)
            Assertions.fail()
        } catch (WebApplicationException x) {
        }

        Mockito.when(mockablePersonRepository.findOrdered()).thenReturn(Collections.emptyList() as List<Person>)
        Assertions.assertTrue(mockablePersonRepository.findOrdered().isEmpty())

        Mockito.verify(mockablePersonRepository).findOrdered()
        Mockito.verify(mockablePersonRepository, Mockito.atLeastOnce()).findById(Mockito.any())
        Mockito.verify(mockablePersonRepository).persist(Mockito.<Person> any())
        Mockito.verifyNoMoreInteractions(mockablePersonRepository)
    }

    @Inject
    PersonRepository realPersonRepository

    @Test
    void testPanacheRepositoryBridges() {
        // normal method call
        Assertions.assertNull(realPersonRepository.findById(0l))
        // bridge call
        Assertions.assertNull(((PanacheRepositoryBase) realPersonRepository).findById(0l))
        // normal method call
        Assertions.assertNull(realPersonRepository.findById(0l, LockModeType.NONE))
        // bridge call
        Assertions.assertNull(((PanacheRepositoryBase) realPersonRepository).findById(0l, LockModeType.NONE))

        // normal method call
        Assertions.assertEquals(Optional.empty(), realPersonRepository.findByIdOptional(0l))
        // bridge call
        Assertions.assertEquals(Optional.empty(), ((PanacheRepositoryBase) realPersonRepository).findByIdOptional(0l))
        // normal method call
        Assertions.assertEquals(Optional.empty(), realPersonRepository.findByIdOptional(0l, LockModeType.NONE))
        // bridge call
        Assertions.assertEquals(Optional.empty(),
                ((PanacheRepositoryBase) realPersonRepository).findByIdOptional(0l, LockModeType.NONE))

        // normal method call
        Assertions.assertEquals(false, realPersonRepository.deleteById(0l))
        // bridge call
        Assertions.assertEquals(false, ((PanacheRepositoryBase) realPersonRepository).deleteById(0l))
    }
}
