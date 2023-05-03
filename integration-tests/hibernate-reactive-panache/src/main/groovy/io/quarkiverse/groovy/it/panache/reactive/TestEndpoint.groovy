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

import java.util.function.Function

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

import java.lang.reflect.Method
import java.util.function.Supplier
import java.util.stream.Stream

import jakarta.inject.Inject
import jakarta.persistence.LockModeType
import jakarta.persistence.NoResultException
import jakarta.persistence.NonUniqueResultException
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

import org.hibernate.engine.spi.SelfDirtinessTracker
import org.junit.jupiter.api.Assertions

import io.quarkiverse.groovy.hibernate.reactive.panache.Panache
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheQuery
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.panache.common.Page
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.quarkus.panache.common.exception.PanacheQueryException
import io.smallrye.mutiny.Uni

// Only to avoid big reflection configuration in Native mode.
@CompileStatic
/**
 * Various tests covering Panache functionality. All tests should work in both standard JVM and in native mode.
 */
@Path("test")
class TestEndpoint {

    @Inject
    PersonRepository personDao
    @Inject
    DogDao dogDao
    @Inject
    AddressDao addressDao
    @Inject
    NamedQueryRepository namedQueryRepository
    @Inject
    NamedQueryWith2QueriesRepository namedQueryWith2QueriesRepository
    @Inject
    CatRepository catRepository
    @Inject
    CatOwnerRepository catOwnerRepository
    @Inject
    FruitRepository fruitRepository
    @Inject
    ObjectWithCompositeIdRepository objectWithCompositeIdRepository
    @Inject
    ObjectWithEmbeddableIdRepository objectWithEmbeddableIdRepository

    @WithTransaction
    @GET
    @Path("model")
    Uni<String> testModel() {
        personDao.findAll().list()
                .flatMap({ persons ->
                    Assertions.assertEquals(0, persons.size())
                    personDao.listAll()
                }).flatMap({persons ->
                    Assertions.assertEquals(0, persons.size())
                    assertThrows(NoResultException.class, { personDao.findAll().singleResult() },
                            "singleResult should have thrown")
                }).flatMap({ personDao.findAll().firstResult() })
                .flatMap({result ->
                    Assertions.assertNull(result)
                    makeSavedPerson()
                }).flatMap({ person ->
                    Assertions.assertNotNull(person.id)

                    personDao.count()
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name = ?1", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name = :name", Parameters.with("name", "stef").map())
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name = :name", Parameters.with("name", "stef"))
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                dogDao.count()
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                // FIXME: fetch
                                Assertions.assertEquals(1, person.dogs.size())

                                personDao.findAll().list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.listAll()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.findAll().firstResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.findAll().singleResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.find("name = ?1", "stef").list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                // full form
                                personDao.find("FROM Person2 WHERE name = ?1", "stef").list()
                            }).flatMap({persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = ?1", "stef").withLock(LockModeType.PESSIMISTIC_READ).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                // FIXME: not supported yet
                                //                                // next calls to this query will be cached
                                //                                personDao.find("name = ?1", "stef").withHint(QueryHints.HINT_CACHEABLE, "true").list()
                                //                            }).flatMap({ persons ->
                                //                                Assertions.assertEquals(1, persons.size())
                                //                                Assertions.assertEquals(person, persons.get(0))

                                personDao.list("name = ?1", "stef")
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = :name", Parameters.with("name", "stef").map()).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = :name", Parameters.with("name", "stef")).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.list("name = :name", Parameters.with("name", "stef").map())
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = :name", Parameters.with("name", "stef").map()).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.list("name = :name", Parameters.with("name", "stef"))
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name", "stef").list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name", "stef").firstResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.find("name", "stef").singleResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.find("name", "stef").singleResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                //named query
                                personDao.list("#Person.getByName", Parameters.with("name", "stef"))
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                assertThrows(PanacheQueryException.class,
                                        { personDao.find("#Person.namedQueryNotFound").list() },
                                        "singleResult should have thrown")
                            }).flatMap({
                        assertThrows(IllegalArgumentException.class,
                                { personDao.list("#Person.getByName", Sort.by("name"), Parameters.with("name", "stef")) },
                                "Should have thrown sort exception")
                    })
                            .flatMap({ namedQueryRepository.list("#NamedQueryMappedSuperClass.getAll") })
                            .flatMap({ namedQueryRepository.list("#NamedQueryEntity.getAll") })
                            .flatMap({ namedQueryWith2QueriesRepository.list("#NamedQueryWith2QueriesEntity.getAll1") })
                            .flatMap({ namedQueryWith2QueriesRepository.list("#NamedQueryWith2QueriesEntity.getAll2") })
                            .flatMap({
                                //empty query
                                personDao.find("").list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find(null as String).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.findById(person.id)
                            }).flatMap({ byId ->
                                Assertions.assertEquals(person, byId)
                                Assertions.assertEquals("Person<" + person.id + ">", byId.toString())

                                personDao.findById(person.id, LockModeType.PESSIMISTIC_READ)
                            }).flatMap({ byId ->
                                Assertions.assertEquals(person, byId)
                                Assertions.assertEquals("Person<" + person.id + ">", byId.toString())

                                person.delete()
                            }).flatMap({ personDao.count() })
                            .flatMap({count ->
                                Assertions.assertEquals(0, count)

                                makeSavedPerson()
                            })
                }).flatMap({ person ->
                    personDao.count()
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name = ?1", "emmanuel")
                            }).flatMap({ count ->
                                Assertions.assertEquals(0, count)

                                dogDao.deleteByQuery("owner = ?1", person)
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPerson()
                            })
                }).flatMap({ person ->

                    dogDao.deleteByQuery("owner = :owner", Parameters.with("owner", person).map())
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPerson()
                            })
                }).flatMap({ person ->

                    dogDao.deleteByQuery("owner = :owner", Parameters.with("owner", person))
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPerson()
                            })
                }).flatMap({ person ->

                    // full form
                    dogDao.deleteByQuery("FROM Dog WHERE owner = :owner", Parameters.with("owner", person))
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("FROM Person2 WHERE name = ?1", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPerson()
                            })
                }).flatMap({ person ->

                    // full form
                    dogDao.deleteByQuery("DELETE FROM Dog WHERE owner = :owner", Parameters.with("owner", person))
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("DELETE FROM Person2 WHERE name = ?1", "stef")
                            }).map({ count ->
                                Assertions.assertEquals(1, count)

                                null
                            })
                })
                .flatMap({ personDao.deleteAll() })
                .flatMap({ count ->
                    Assertions.assertEquals(0, count)

                    makeSavedPerson()
                }).flatMap({ person ->

                    dogDao.deleteAll()
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteAll()
                            }).map({ count ->
                                Assertions.assertEquals(1, count)

                                null
                            })
                })
                .flatMap({ testPersist(PersistTest.ENTITY_CLASS_Iterable) })
                .flatMap({ testPersist(PersistTest.ENTITY_CLASS_Stream) })
                .flatMap({ testPersist(PersistTest.ENTITY_CLASS_Variadic) })
                .flatMap({ personDao.deleteAll() })
                .flatMap({ count ->
                    Assertions.assertEquals(6, count)

                    testSorting()
                })
                // paging
                .flatMap({ makeSavedPerson("0") })
                .flatMap({ makeSavedPerson("1") })
                .flatMap({ makeSavedPerson("2") })
                .flatMap({ makeSavedPerson("3") })
                .flatMap({ makeSavedPerson("4") })
                .flatMap({ makeSavedPerson("5") })
                .flatMap({ makeSavedPerson("6") })
                .flatMap({ testPaging(personDao.findAll()) })
                .flatMap({ testPaging(personDao.find("ORDER BY name")) })
                // range
                .flatMap({ testRange(personDao.findAll()) })
                .flatMap({ testRange(personDao.find("ORDER BY name")) })
                .flatMap({
                    assertThrows(NonUniqueResultException.class,
                            { personDao.findAll().singleResult() },
                            "singleResult should have thrown")
                })
                .flatMap({ personDao.findAll().firstResult() })
                .flatMap({ person ->
                    Assertions.assertNotNull(person)

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(7, count)

                    testUpdate()
                }).flatMap({
                    //delete by id
                    Person toRemove = new Person()
                    toRemove.name = "testDeleteById"
                    toRemove.uniqueName = "testDeleteByIdUnique"
                    toRemove.persist().flatMap(v2 -> personDao.deleteById(toRemove.id))
                }).flatMap({ deleted ->
                    assertTrue(deleted)

                    personDao.deleteById(666L) //not existing
                }).flatMap({ deleted ->
                    assertFalse(deleted)

                    // persistAndFlush
                    Person person1 = new Person()
                    person1.name = "testFLush1"
                    person1.uniqueName = "unique"
                    person1.persist()
                    // FIXME: https://github.com/hibernate/hibernate-reactive/issues/281
                    //                }).flatMap({
                    //                    Person person2 = new Person()
                    //                    person2.name = "testFLush2"
                    //                    person2.uniqueName = "unique"
                    //
                    //                    // FIXME should be PersistenceException see https://github.com/hibernate/hibernate-reactive/issues/280
                    //                    assertThrows(PgException.class,
                    //                            () -> person2.persistAndFlush(),
                    //                            "Should have failed")
                }).flatMap({ personDao.deleteAll() })
                .map({ "OK" })
    }

    private Uni<Void> testUpdate() {
        makeSavedPerson("p1")
                .flatMap({ makeSavedPerson("p2") })
                .flatMap({ personDao.update("update from Person2 p set p.name = 'stefNEW' where p.name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("update from Person2 p set p.name = 'stefNEW' where p.name = :pName",
                            Parameters.with("pName", "stefp2").map())
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPerson("p1")
                }).flatMap({ makeSavedPerson("p2") })
                .flatMap({ personDao.update("from Person2 p set p.name = 'stefNEW' where p.name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("from Person2 p set p.name = 'stefNEW' where p.name = :pName",
                            Parameters.with("pName", "stefp2").map())

                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPerson("p1")
                }).flatMap({ makeSavedPerson("p2") })
                .flatMap({ personDao.update("set name = 'stefNEW' where name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("set name = 'stefNEW' where name = :pName",
                            Parameters.with("pName", "stefp2").map())
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPerson("p1")
                }).flatMap({ makeSavedPerson("p2") })
                .flatMap({ personDao.update("name = 'stefNEW' where name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("name = 'stefNEW' where name = :pName",
                            Parameters.with("pName", "stefp2").map())
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPerson("p1")
                }).flatMap({ makeSavedPerson("p2") })
                .flatMap({ personDao.update("name = 'stefNEW' where name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("name = 'stefNEW' where name = :pName",
                            Parameters.with("pName", "stefp2"))
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    assertThrows(PanacheQueryException.class, { personDao.update(null) },
                            "PanacheQueryException should have thrown")
                }).flatMap({
            assertThrows(PanacheQueryException.class, { personDao.update(" ") },
                    "PanacheQueryException should have thrown")
        })
    }

    private Uni<Void> testUpdateDao() {
        makeSavedPersonDao("p1")
                .flatMap({ makeSavedPersonDao("p2") })
                .flatMap({ personDao.update("update from Person2 p set p.name = 'stefNEW' where p.name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("update from Person2 p set p.name = 'stefNEW' where p.name = :pName",
                            Parameters.with("pName", "stefp2").map())
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPersonDao("p1")
                }).flatMap({ makeSavedPersonDao("p2") })
                .flatMap({ personDao.update("from Person2 p set p.name = 'stefNEW' where p.name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("from Person2 p set p.name = 'stefNEW' where p.name = :pName",
                            Parameters.with("pName", "stefp2").map())

                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPersonDao("p1")
                }).flatMap({ makeSavedPersonDao("p2") })
                .flatMap({ personDao.update("set name = 'stefNEW' where name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("set name = 'stefNEW' where name = :pName",
                            Parameters.with("pName", "stefp2").map())
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPersonDao("p1")
                }).flatMap({ makeSavedPersonDao("p2") })
                .flatMap({ personDao.update("name = 'stefNEW' where name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("name = 'stefNEW' where name = :pName",
                            Parameters.with("pName", "stefp2").map())
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    makeSavedPersonDao("p1")
                }).flatMap({ makeSavedPersonDao("p2") })
                .flatMap({ personDao.update("name = 'stefNEW' where name = ?1", "stefp1") })
                .flatMap({ updateByIndexParameter ->
                    Assertions.assertEquals(1, updateByIndexParameter, "More than one Person updated")

                    personDao.update("name = 'stefNEW' where name = :pName",
                            Parameters.with("pName", "stefp2"))
                }).flatMap({ updateByNamedParameter ->
                    Assertions.assertEquals(1, updateByNamedParameter, "More than one Person updated")

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(2, count)

                    assertThrows(PanacheQueryException.class, { personDao.update(null) },
                            "PanacheQueryException should have thrown")
                }).flatMap({
            assertThrows(PanacheQueryException.class, { personDao.update(" ") },
                    "PanacheQueryException should have thrown")
        })
    }

    private Uni<Void> assertThrows(Class<? extends Throwable> exceptionClass,
            Supplier<Uni<?>> f,
            String message) {
        Uni<?> uni
        try {
            uni = f.get()
        } catch (Throwable t) {
            uni = Uni.createFrom().failure(t)
        }
        uni
                .onItem().invoke({ Assertions.fail(message) })
                .onFailure(exceptionClass)
                .recoverWithItem({ (Void) null } as Function)
                .map({ (Void) null })
    }

    private Uni<Void> testSorting() {
        Person person1 = new Person()
        person1.name = "stef"
        person1.status = Status.LIVING

        Person person2 = new Person()
        person2.name = "stef"
        person2.status = Status.DECEASED

        Person person3 = new Person()
        person3.name = "emmanuel"
        person3.status = Status.LIVING

        person1.persist()
                .flatMap({ person2.persist() })
                .flatMap({ person3.persist() })
                .flatMap({
                    Sort sort1 = Sort.by("name", "status")
                    List<Person> order1 = Arrays.asList(person3, person2, person1)

                    Sort sort2 = Sort.descending("name", "status")
                    List<Person> order2 = Arrays.asList(person1, person2)

                    personDao.findAll(sort1).list()
                            .flatMap({ list ->
                                Assertions.assertEquals(order1, list)

                                personDao.listAll(sort1)
                            }).flatMap({ list ->
                                Assertions.assertEquals(order1, list)

                                personDao.find("name", sort2, "stef").list()
                            }).flatMap({ list ->
                                Assertions.assertEquals(order2, list)

                                personDao.list("name", sort2, "stef")
                            }).flatMap({ list ->
                                Assertions.assertEquals(order2, list)

                                personDao.find("name = :name", sort2, Parameters.with("name", "stef").map()).list()
                            }).flatMap({ list ->
                                Assertions.assertEquals(order2, list)

                                personDao.list("name = :name", sort2, Parameters.with("name", "stef").map())
                            }).flatMap({ list ->
                                Assertions.assertEquals(order2, list)

                                personDao.find("name = :name", sort2, Parameters.with("name", "stef")).list()
                            }).flatMap({ list ->
                                Assertions.assertEquals(order2, list)

                                personDao.list("name = :name", sort2, Parameters.with("name", "stef"))
                            })
                }).flatMap({ personDao.deleteAll() })
                .map({ count ->
                    Assertions.assertEquals(3, count)

                    null
                })
    }

    private Uni<Person> makeSavedPerson(String suffix) {
        Person person = new Person().tap {
            name = "stef${suffix}"
            status = Status.LIVING
            address = new Address('stef street')
        }
        person.address.persist()
                .flatMap({ person.persist() })
    }

    private Uni<Person> makeSavedPersonDao(String suffix) {
        Person person = new Person().tap {
            name = "stef${suffix}"
            status = Status.LIVING
            address = new Address('stef street')
        }
        addressDao.persist(person.address)
                .flatMap({  personDao.persist(person) })
    }

    private Uni<Person> makeSavedPerson() {
        Uni<Person> uni = makeSavedPerson("")

        uni.flatMap({ person ->
            Dog dog = new Dog("octave", "dalmatian")
            dog.owner = person
            person.dogs.add(dog)
            dog.persist().map({ person })
        })
    }

    private Uni<Person> makeSavedPersonDao() {
        Uni<Person> uni = makeSavedPersonDao("")

        uni.flatMap({ person ->
            Dog dog = new Dog("octave", "dalmatian")
            dog.owner = person
            person.dogs.add(dog)
            dog.persist().map({ person })
        })
    }

    private Uni<Void> testPersist(PersistTest persistTest) {
        Person person1 = new Person().tap {
            name = "stef1"
        }
        Person person2 = new Person().tap {
            name = "stef2"
        }

        assertFalse(person1.isPersistent())
        assertFalse(person2.isPersistent())
        Uni<Void> persist
        switch (persistTest) {
            case PersistTest.ENTITY_CLASS_Iterable:
                persist = Person.persist(Arrays.asList(person1, person2))
                break
            case PersistTest.ENTITY_CLASS_Stream:
                persist = Person.persist(Stream.of(person1, person2))
                break
            case PersistTest.ENTITY_CLASS_Variadic:
                persist = Person.persist(person1, person2)
                break
            default:
                throw new RuntimeException("Ouch")
        }
        persist.map({
            assertTrue(person1.isPersistent())
            assertTrue(person2.isPersistent())
            (Void) null
        })
    }

    private Uni<Void> testPersistDao(PersistTest persistTest) {
        Person person1 = new Person()
        person1.name = "stef1"
        Person person2 = new Person()
        person2.name = "stef2"

        assertFalse(personDao.isPersistent(person1))
        assertFalse(personDao.isPersistent(person2))
        Uni<Void> persist
        switch (persistTest) {
            case PersistTest.DAO_Iterable:
                persist = personDao.persist(Arrays.asList(person1, person2))
                break
            case PersistTest.DAO_Stream:
                persist = personDao.persist(Stream.of(person1, person2))
                break
            case PersistTest.DAO_Variadic:
                persist = personDao.persist(person1, person2)
                break
            default:
                throw new RuntimeException("Ouch")
        }
        persist.map({
            assertTrue(personDao.isPersistent(person1))
            assertTrue(personDao.isPersistent(person2))
            null
        }).replaceWithVoid()
    }

    @WithTransaction
    @GET
    @Path("model-dao")
    Uni<String> testModelDao() {
        personDao.findAll().list()
                .flatMap({ persons ->
                    Assertions.assertEquals(0, persons.size())

                    personDao.listAll()
                }).flatMap({ persons ->

                    Assertions.assertEquals(0, persons.size())
                    assertThrows(NoResultException.class, () -> personDao.findAll().singleResult(),
                            "singleResult should have thrown")
                }).flatMap({ personDao.findAll().firstResult() })
                .flatMap({ result ->
                    Assertions.assertNull(result)

                    makeSavedPersonDao()
                }).flatMap({ person ->
                    Assertions.assertNotNull(person.id)

                    personDao.count()
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name = ?1", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name = :name", Parameters.with("name", "stef").map())
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name = :name", Parameters.with("name", "stef"))
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                personDao.count("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                dogDao.count()
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)
                                // FIXME: fetch
                                Assertions.assertEquals(1, person.dogs.size())

                                personDao.findAll().list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.listAll()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.findAll().firstResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.findAll().singleResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.find("name = ?1", "stef").list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = ?1", "stef").withLock(LockModeType.PESSIMISTIC_READ).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                // FIXME: not supported yet
                                //                                // next calls to this query will be cached
                                //                                personDao.find("name = ?1", "stef").withHint(QueryHints.HINT_CACHEABLE, "true").list()
                                //                            }).flatMap({ persons ->
                                //                                Assertions.assertEquals(1, persons.size())
                                //                                Assertions.assertEquals(person, persons.get(0))

                                personDao.list("name = ?1", "stef")
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = :name", Parameters.with("name", "stef").map()).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = :name", Parameters.with("name", "stef")).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.list("name = :name", Parameters.with("name", "stef").map())
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name = :name", Parameters.with("name", "stef").map()).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.list("name = :name", Parameters.with("name", "stef"))
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name", "stef").list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find("name", "stef").firstResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.find("name", "stef").singleResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                personDao.find("name", "stef").singleResult()
                            }).flatMap({ personResult ->
                                Assertions.assertEquals(person, personResult)

                                //named query
                                personDao.list("#Person.getByName", Parameters.with("name", "stef"))
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                assertThrows(PanacheQueryException.class,
                                        { personDao.find("#Person.namedQueryNotFound").list() },
                                        "singleResult should have thrown")
                            }).flatMap({
                        assertThrows(IllegalArgumentException.class,
                                { personDao.list("#Person.getByName", Sort.by("name"), Parameters.with("name", "stef")) },
                                "Should have thrown sort exception")
                    })
                            .flatMap({ namedQueryRepository.list("#NamedQueryMappedSuperClass.getAll") })
                            .flatMap({ namedQueryRepository.list("#NamedQueryEntity.getAll") })
                            .flatMap({ namedQueryWith2QueriesRepository.list("#NamedQueryWith2QueriesEntity.getAll1") })
                            .flatMap({ namedQueryWith2QueriesRepository.list("#NamedQueryWith2QueriesEntity.getAll2") })
                            .flatMap({
                                //empty query
                                personDao.find("").list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.find(null as String).list()
                            }).flatMap({ persons ->
                                Assertions.assertEquals(1, persons.size())
                                Assertions.assertEquals(person, persons.get(0))

                                personDao.findById(person.id)
                            }).flatMap({ byId ->
                                Assertions.assertEquals(person, byId)
                                Assertions.assertEquals("Person<" + person.id + ">", byId.toString())

                                personDao.findById(person.id, LockModeType.PESSIMISTIC_READ)
                            }).flatMap({ byId ->
                                Assertions.assertEquals(person, byId)
                                Assertions.assertEquals("Person<" + person.id + ">", byId.toString())

                                person.delete()
                            }).flatMap({ personDao.count() })
                            .flatMap({ count ->
                                Assertions.assertEquals(0, count)

                                makeSavedPersonDao()
                            })

                            .flatMap({
                                personDao.count("#Person.countAll")
                                        .flatMap({ count ->
                                            Assertions.assertEquals(1, count)
                                            personDao.count("#Person.countByName", Parameters.with("name", "stef").map())
                                        }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    personDao.count("#Person.countByName", Parameters.with("name", "stef"))
                                }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    personDao.count("#Person.countByName.ordinal", "stef")
                                }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    Uni.createFrom().voidItem()
                                })
                            })
                            .flatMap({
                                personDao.update("#Person.updateAllNames", Parameters.with("name", "stef2").map())
                                        .flatMap({count ->
                                            Assertions.assertEquals(1, count)
                                            personDao.find("#Person.getByName", Parameters.with("name", "stef2")).list()
                                        }).flatMap({persons ->
                                    Assertions.assertEquals(1, persons.size())
                                    personDao.update("#Person.updateAllNames", Parameters.with("name", "stef3"))
                                }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    personDao.find("#Person.getByName", Parameters.with("name", "stef3")).list()
                                }).flatMap({ persons ->
                                    Assertions.assertEquals(1, persons.size())
                                    personDao.update("#Person.updateNameById",
                                            Parameters.with("name", "stef2").and("id", ((Person) persons.get(0)).id).map())
                                }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    personDao.find("#Person.getByName", Parameters.with("name", "stef2")).list()
                                }).flatMap({ persons ->
                                    Assertions.assertEquals(1, persons.size())
                                    personDao.update("#Person.updateNameById",
                                            Parameters.with("name", "stef3").and("id", ((Person) persons.get(0)).id))
                                }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    personDao.find("#Person.getByName", Parameters.with("name", "stef3")).list()
                                }).flatMap({ persons ->
                                    Assertions.assertEquals(1, persons.size())
                                    personDao.update("#Person.updateNameById.ordinal", "stef",
                                            ((Person) persons.get(0)).id)
                                }).flatMap({ count ->
                                    Assertions.assertEquals(1, count)
                                    personDao.find("#Person.getByName", Parameters.with("name", "stef")).list()
                                }).flatMap({ persons ->
                                    Assertions.assertEquals(1, persons.size())
                                    Uni.createFrom().voidItem()
                                })
                            })
                            .flatMap({
                                dogDao.deleteAll()
                                        .flatMap({ personDao.deleteByQuery("#Person.deleteAll") })
                                        .flatMap({ count ->
                                            Assertions.assertEquals(1, count)
                                            personDao.find("").list()
                                        })
                                        .flatMap({ persons ->
                                            Assertions.assertEquals(0, persons.size())
                                            Uni.createFrom().voidItem()
                                        })
                            })
                            .flatMap({
                                makeSavedPerson().flatMap({ personToDelete ->
                                    dogDao.deleteAll()
                                            .flatMap({ personDao.find("").list() })
                                            .flatMap({ persons ->
                                                Assertions.assertEquals(1, persons.size())
                                                personDao.deleteByQuery("#Person.deleteById",
                                                        Parameters.with("id", personToDelete.id).map())
                                            })
                                            .flatMap({ count ->
                                                Assertions.assertEquals(1, count)
                                                personDao.find("").list()
                                            })
                                            .flatMap({ persons ->
                                                Assertions.assertEquals(0, persons.size())
                                                Uni.createFrom().voidItem()
                                            })
                                })
                            })
                            .flatMap({
                                makeSavedPerson().flatMap({ personToDelete ->
                                    dogDao.deleteAll()
                                            .flatMap({ personDao.find("").list() })
                                            .flatMap({ persons ->
                                                Assertions.assertEquals(1, persons.size())
                                                personDao.deleteByQuery("#Person.deleteById", Parameters.with("id", personToDelete.id))
                                            })
                                            .flatMap({ count ->
                                                Assertions.assertEquals(1, count)
                                                personDao.find("").list()
                                            })
                                            .flatMap({ persons ->
                                                Assertions.assertEquals(0, persons.size())
                                                Uni.createFrom().voidItem()
                                            })
                                })
                            })
                            .flatMap({
                                makeSavedPerson().flatMap({ personToDelete ->
                                    dogDao.deleteAll()
                                            .flatMap({ personDao.find("").list() })
                                            .flatMap({ persons ->
                                                Assertions.assertEquals(1, persons.size())
                                                personDao.deleteByQuery("#Person.deleteById.ordinal", personToDelete.id)
                                            })
                                            .flatMap({ count ->
                                                Assertions.assertEquals(1, count)
                                                personDao.find("").list()
                                            })
                                            .flatMap({ persons ->
                                                Assertions.assertEquals(0, persons.size())
                                                makeSavedPersonDao()
                                            })
                                })
                            })

                }).flatMap({ person ->

                    personDao.count()
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name = ?1", "emmanuel")
                            }).flatMap({ count ->
                                Assertions.assertEquals(0, count)

                                dogDao.deleteByQuery("owner = ?1", person)
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPersonDao()
                            })
                }).flatMap({ person ->

                    dogDao.deleteByQuery("owner = :owner", Parameters.with("owner", person).map())
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPersonDao()
                            })
                }).flatMap({ person ->

                    dogDao.deleteByQuery("owner = :owner", Parameters.with("owner", person))
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("name", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPersonDao()
                            })
                }).flatMap({ person ->

                    // full form
                    dogDao.deleteByQuery("FROM Dog WHERE owner = :owner", Parameters.with("owner", person))
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("FROM Person2 WHERE name = ?1", "stef")
                            }).flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                makeSavedPersonDao()
                            })
                }).flatMap({ person ->

                    dogDao.deleteByQuery("DELETE FROM Dog WHERE owner = :owner", Parameters.with("owner", person))
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteByQuery("DELETE FROM Person2 WHERE name = ?1", "stef")
                            }).map({ count ->
                                Assertions.assertEquals(1, count)

                                null
                            })
                })
                .flatMap({ personDao.deleteAll() })
                .flatMap({ count ->
                    Assertions.assertEquals(0, count)

                    makeSavedPersonDao()
                }).flatMap({ person ->

                    dogDao.deleteAll()
                            .flatMap({ count ->
                                Assertions.assertEquals(1, count)

                                personDao.deleteAll()
                            }).map({ count ->
                                Assertions.assertEquals(1, count)

                                null
                            })
                })
                .flatMap({ testPersistDao(PersistTest.DAO_Iterable) })
                .flatMap({ testPersistDao(PersistTest.DAO_Stream) })
                .flatMap({ testPersistDao(PersistTest.DAO_Variadic) })
                .flatMap({ personDao.deleteAll() })
                .flatMap({ count ->
                    Assertions.assertEquals(6, count)

                    testSorting()
                })
                // paging
                .flatMap({ makeSavedPersonDao("0") })
                .flatMap({ makeSavedPersonDao("1") })
                .flatMap({ makeSavedPersonDao("2") })
                .flatMap({ makeSavedPersonDao("3") })
                .flatMap({ makeSavedPersonDao("4") })
                .flatMap({ makeSavedPersonDao("5") })
                .flatMap({ makeSavedPersonDao("6") })
                .flatMap({ testPaging(personDao.findAll()) })
                .flatMap({ testPaging(personDao.find("ORDER BY name")) })
                // range
                .flatMap({ testRange(personDao.findAll()) })
                .flatMap({ testRange(personDao.find("ORDER BY name")) })
                .flatMap({
                    assertThrows(NonUniqueResultException.class,
                            { personDao.findAll().singleResult() },
                            "singleResult should have thrown")
                })
                .flatMap({ personDao.findAll().firstResult() })
                .flatMap({ person ->
                    Assertions.assertNotNull(person)

                    personDao.deleteAll()
                }).flatMap({ count ->
                    Assertions.assertEquals(7, count)

                    testUpdateDao()
                }).flatMap({
                    //delete by id
                    Person toRemove = new Person()
                    toRemove.name = "testDeleteById"
                    toRemove.uniqueName = "testDeleteByIdUnique"
                    toRemove.persist().flatMap({
                        personDao.deleteById(toRemove.id)
                    })
                }).flatMap({ deleted ->
                    assertTrue(deleted)

                    personDao.deleteById(666L) //not existing
                }).flatMap({ deleted ->
                    assertFalse(deleted)

                    // persistAndFlush
                    Person person1 = new Person()
                    person1.name = "testFLush1"
                    person1.uniqueName = "unique"
                    personDao.persistAndFlush(person1)
                    // FIXME: https://github.com/hibernate/hibernate-reactive/issues/281
                    //                }).flatMap({
                    //                    Person person2 = new Person()
                    //                    person2.name = "testFLush2"
                    //                    person2.uniqueName = "unique"
                    //
                    //                    // FIXME should be PersistenceException see https://github.com/hibernate/hibernate-reactive/issues/280
                    //                    assertThrows(PgException.class,
                    //                            () -> personDao.persistAndFlush(person2),
                    //                            "Should have failed")
                }).flatMap({
                    personDao.deleteAll()
                })
                .map({
                    "OK"
                })
    }

    enum PersistTest {
        DAO_Iterable,
        DAO_Variadic,
        DAO_Stream,
        ENTITY_CLASS_Iterable,
        ENTITY_CLASS_Variadic,
        ENTITY_CLASS_Stream
    }

    private Uni<Void> testPaging(PanacheQuery<Person> query) {
        // No paging allowed until a page is setup
        Assertions.assertThrows(UnsupportedOperationException.class, { query.firstPage() },
                "UnsupportedOperationException should have thrown")
        Assertions.assertThrows(UnsupportedOperationException.class, { query.previousPage() },
                "UnsupportedOperationException should have thrown")
        Assertions.assertThrows(UnsupportedOperationException.class, { query.nextPage() },
                "UnsupportedOperationException should have thrown")
        //        Assertions.assertThrows(UnsupportedOperationException.class, { query.lastPage() },
        //                "UnsupportedOperationException should have thrown")
        Assertions.assertThrows(UnsupportedOperationException.class, { query.hasNextPage() },
                "UnsupportedOperationException should have thrown")
        Assertions.assertThrows(UnsupportedOperationException.class, { query.hasPreviousPage() },
                "UnsupportedOperationException should have thrown")
        Assertions.assertThrows(UnsupportedOperationException.class, { query.page() },
                "UnsupportedOperationException should have thrown")
        Assertions.assertThrows(UnsupportedOperationException.class, { query.pageCount() },
                "UnsupportedOperationException should have thrown")

        // ints
        query.page(0, 3).<Person>list()
                .flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef0", persons.get(0).name)
                    Assertions.assertEquals("stef1", persons.get(1).name)
                    Assertions.assertEquals("stef2", persons.get(2).name)

                    query.page(1, 3).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef3", persons.get(0).name)
                    Assertions.assertEquals("stef4", persons.get(1).name)
                    Assertions.assertEquals("stef5", persons.get(2).name)

                    query.page(2, 3).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(1, persons.size())
                    Assertions.assertEquals("stef6", persons.get(0).name)

                    query.page(2, 4).list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(0, persons.size())

                    // page
                    Page page = new Page(3)
                    query.page(page).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef0", persons.get(0).name)
                    Assertions.assertEquals("stef1", persons.get(1).name)
                    Assertions.assertEquals("stef2", persons.get(2).name)

                    Page page = new Page(1, 3)
                    query.page(page).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef3", persons.get(0).name)
                    Assertions.assertEquals("stef4", persons.get(1).name)
                    Assertions.assertEquals("stef5", persons.get(2).name)

                    Page page = new Page(2, 3)
                    query.page(page).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(1, persons.size())
                    Assertions.assertEquals("stef6", persons.get(0).name)

                    Page page = new Page(3, 3)
                    query.page(page).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(0, persons.size())

                    // query paging
                    Page page = new Page(3)
                    query.page(page).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef0", persons.get(0).name)
                    Assertions.assertEquals("stef1", persons.get(1).name)
                    Assertions.assertEquals("stef2", persons.get(2).name)
                    query.hasNextPage()
                }).flatMap({ hasNextPage ->
                    assertTrue(hasNextPage)
                    assertFalse(query.hasPreviousPage())

                    query.nextPage().<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(1, query.page().index)
                    Assertions.assertEquals(3, query.page().size)
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef3", persons.get(0).name)
                    Assertions.assertEquals("stef4", persons.get(1).name)
                    Assertions.assertEquals("stef5", persons.get(2).name)
                    query.hasNextPage()
                }).flatMap({ hasNextPage ->
                    assertTrue(hasNextPage)
                    assertTrue(query.hasPreviousPage())

                    query.nextPage().<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(1, persons.size())
                    Assertions.assertEquals("stef6", persons.get(0).name)

                    query.hasNextPage()
                }).flatMap({ hasNextPage ->
                    assertFalse(hasNextPage)
                    assertTrue(query.hasPreviousPage())

                    query.nextPage().list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(0, persons.size())

                    query.count()
                }).flatMap({ count ->
                    Assertions.assertEquals(7, count)

                    query.pageCount()
                }).flatMap({ count ->
                    Assertions.assertEquals(3, count)

                    // mix page with range
                    query.page(0, 3).range(0, 1).<Person> list()
                }).map({ persons ->
                    Assertions.assertEquals(2, persons.size())
                    Assertions.assertEquals("stef0", persons.get(0).name)
                    Assertions.assertEquals("stef1", persons.get(1).name)

                    null
                })
    }

    private Uni<Void> testRange(PanacheQuery<Person> query) {
        query.range(0, 2).<Person>list()
                .flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef0", persons.get(0).name)
                    Assertions.assertEquals("stef1", persons.get(1).name)
                    Assertions.assertEquals("stef2", persons.get(2).name)

                    query.range(3, 5).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef3", persons.get(0).name)
                    Assertions.assertEquals("stef4", persons.get(1).name)
                    Assertions.assertEquals("stef5", persons.get(2).name)

                    query.range(6, 8).<Person>list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(1, persons.size())
                    Assertions.assertEquals("stef6", persons.get(0).name)

                    query.range(8, 12).list()
                }).flatMap({ persons ->
                    Assertions.assertEquals(0, persons.size())

                    // mix range with page
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).nextPage() })
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).previousPage() })
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).pageCount() })
                    //                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).lastPage() })
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).firstPage() })
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).hasPreviousPage() })
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).hasNextPage() })
                    Assertions.assertThrows(UnsupportedOperationException.class, { query.range(0, 2).page() })

                    // this is valid as we switch from range to page
                    query.range(0, 2).page(0, 3).<Person>list()
                }).map( { persons ->
                    Assertions.assertEquals(3, persons.size())
                    Assertions.assertEquals("stef0", persons.get(0).name)
                    Assertions.assertEquals("stef1", persons.get(1).name)
                    Assertions.assertEquals("stef2", persons.get(2).name)

                    null
                })
    }

    @GET
    @Path("accessors")
    String testAccessors() throws NoSuchMethodException, SecurityException {
        checkMethod(AccessorEntity.class, "getString", String.class)
        checkMethod(AccessorEntity.class, "isBool", boolean.class)
        checkMethod(AccessorEntity.class, "getC", char.class)
        checkMethod(AccessorEntity.class, "getS", short.class)
        checkMethod(AccessorEntity.class, "getI", int.class)
        checkMethod(AccessorEntity.class, "getL", long.class)
        checkMethod(AccessorEntity.class, "getF", float.class)
        checkMethod(AccessorEntity.class, "getD", double.class)
        checkMethod(AccessorEntity.class, "getT", Object.class)
        checkMethod(AccessorEntity.class, "getT2", Object.class)

        checkMethod(AccessorEntity.class, "setString", void.class, String.class)
        checkMethod(AccessorEntity.class, "setBool", void.class, boolean.class)
        checkMethod(AccessorEntity.class, "setC", void.class, char.class)
        checkMethod(AccessorEntity.class, "setS", void.class, short.class)
        checkMethod(AccessorEntity.class, "setI", void.class, int.class)
        checkMethod(AccessorEntity.class, "setL", void.class, long.class)
        checkMethod(AccessorEntity.class, "setF", void.class, float.class)
        checkMethod(AccessorEntity.class, "setD", void.class, double.class)
        checkMethod(AccessorEntity.class, "setT", void.class, Object.class)
        checkMethod(AccessorEntity.class, "setT2", void.class, Object.class)

        try {
            checkMethod(AccessorEntity.class, "getTrans2", Object.class)
            Assertions.fail("transient field should have no getter: trans2")
        } catch (NoSuchMethodException x) {
        }

        try {
            checkMethod(AccessorEntity.class, "setTrans2", void.class, Object.class)
            Assertions.fail("transient field should have no setter: trans2")
        } catch (NoSuchMethodException x) {
        }

        // Now check that accessors are called
        AccessorEntity entity = new AccessorEntity()
        @SuppressWarnings("unused")
        byte b = entity.b
        Assertions.assertEquals(1, entity.getBCalls)
        entity.i = 2
        Assertions.assertEquals(1, entity.setICalls)
// TODO:  Add Transient support
//        Object trans = entity.trans
//        Assertions.assertEquals(0, entity.getTransCalls)
//        entity.trans = trans
//        Assertions.assertEquals(0, entity.setTransCalls)

        // accessors inside the entity itself
        entity.method()
        Assertions.assertEquals(2, entity.getBCalls)
        Assertions.assertEquals(2, entity.setICalls)

        "OK"
    }

    private void checkMethod(Class<?> klass, String name, Class<?> returnType, Class<?>... params)
            throws NoSuchMethodException, SecurityException {
        Method method = klass.getMethod(name, params)
        Assertions.assertEquals(returnType, method.getReturnType())
    }

    @WithTransaction
    @GET
    @Path("model1")
    Uni<String> testModel1() {
        personDao.count()
                .flatMap({ count ->
                    Assertions.assertEquals(0, count)

                    makeSavedPerson("")
                }).flatMap({ person ->
                    SelfDirtinessTracker trackingPerson = (SelfDirtinessTracker) person

                    String[] dirtyAttributes = trackingPerson.$$_hibernate_getDirtyAttributes()
                    Assertions.assertEquals(0, dirtyAttributes.length)

                    person.name = "1"

                    dirtyAttributes = trackingPerson.$$_hibernate_getDirtyAttributes()
                    Assertions.assertEquals(1, dirtyAttributes.length)

                    personDao.count()
                }).map({ count ->
                    Assertions.assertEquals(1, count)

                    "OK"
                })
    }

    @WithTransaction
    @GET
    @Path("model2")
    Uni<String> testModel2() {
        personDao.count()
                .flatMap({ count ->
                    Assertions.assertEquals(1, count)

                    personDao.findAll().<Person> firstResult()
                }).map({ person ->
                    Assertions.assertEquals("1", person.name)

                    person.name = "2"
                    "OK"
                })
    }

    @WithTransaction
    @GET
    @Path("projection1")
    Uni<String> testProjection() {
        personDao.count()
                .flatMap({ count ->
                    Assertions.assertEquals(1, count)

                    personDao.findAll().project(PersonName.class).<PersonName> firstResult()
                }).flatMap({ person ->
                    Assertions.assertEquals("2", person.name)

                    personDao.find("name", "2").project(PersonName.class).<PersonName> firstResult()
                }).flatMap({ person ->
                    Assertions.assertEquals("2", person.name)

                    personDao.find("name = ?1", "2").project(PersonName.class).<PersonName> firstResult()
                }).flatMap({ person ->
                    Assertions.assertEquals("2", person.name)

                    personDao.find("name = :name", Parameters.with("name", "2")).project(PersonName.class)
                            .<PersonName> firstResult()
                }).flatMap({ person ->
                    Assertions.assertEquals("2", person.name)

                    PanacheQuery<PersonName> query = personDao.findAll().project(PersonName.class).page(0, 2)
                    query.list()
                            .flatMap({ results ->
                                Assertions.assertEquals(1, results.size())

                                query.nextPage()
                                query.list()
                            }).flatMap({ results ->
                                Assertions.assertEquals(0, results.size())

                                personDao.findAll().project(PersonName.class).count()
                            }).map({ count ->
                                Assertions.assertEquals(1, count)

                                "OK"
                            })
                })
    }

    @WithTransaction
    @GET
    @Path("projection2")
    Uni<String> testProjection2() {
        String ownerName = "Julie"
        String catName = "Bubulle"
        Double catWeight = 8.5d
        CatOwner catOwner = new CatOwner(ownerName)
        catOwner.persist()
                .flatMap { new Cat(catName, catOwner, catWeight).persist() }
                .flatMap {
                    catRepository.find("name", catName)
                            .project(CatDto.class)
                            .<CatDto>firstResult()
                }
                .flatMap { CatDto cat ->
                    Assertions.assertEquals(catName, cat?.name)
                    Assertions.assertEquals(ownerName, cat?.ownerName)
                    catRepository.find("select c.name, c.owner.name as ownerName from Cat c where c.name = :name",
                            Parameters.with("name", catName))
                            .project(CatProjectionBean.class)
                            .<CatProjectionBean>singleResult()
                }
                .flatMap { CatProjectionBean catView ->
                    Assertions.assertEquals(catName, catView?.name)
                    Assertions.assertEquals(ownerName, catView?.ownerName)
                    Assertions.assertNull(catView?.weight)
                    catRepository.find("select 'fake_cat', 'fake_owner', 12.5D from Cat c")
                            .project(CatProjectionBean.class)
                            .<CatProjectionBean>firstResult()
                }
                .map { CatProjectionBean catView ->
                    Assertions.assertEquals("fake_cat", catView?.name)
                    Assertions.assertEquals("fake_owner", catView?.ownerName)
                    Assertions.assertEquals(12.5d, catView?.weight)
                    catRepository.find(
                            "   SELECT c.name, cast(null as string), SUM(c.weight) from Cat c where name = :name group by name  ",
                            Parameters.with("name", catName))
                            .project(CatProjectionBean.class)
                }
                .flatMap { PanacheQuery<CatProjectionBean> projectionQuery ->
                    projectionQuery
                            .<CatProjectionBean>firstResult()
                            .map { CatProjectionBean catView ->
                                Assertions.assertEquals(catName, catView?.name)
                                Assertions.assertNull(catView?.ownerName)
                                Assertions.assertEquals(catWeight, catView?.weight)
                            }
                            projectionQuery.count()
                }
                .map { count ->
                    Assertions.assertEquals(1L, count)
                    // The spaces at the beginning are intentional
                    catRepository.find(
                            "   SELECT   disTINct  c.name, cast(null as string), SUM(c.weight) from Cat c where name = :name group by name  ",
                            Parameters.with("name", catName))
                            .project(CatProjectionBean.class)
                }
                .flatMap { PanacheQuery<CatProjectionBean> projectionQuery ->
                    projectionQuery
                            .<CatProjectionBean>firstResult()
                            .map { CatProjectionBean catView ->
                                Assertions.assertEquals(catName, catView.name)
                                Assertions.assertNull(catView?.ownerName)
                                Assertions.assertEquals(catWeight, catView?.weight)
                            }
                            .flatMap { projectionQuery.count() }
                            .map {
                                count -> Assertions.assertEquals(1L, count)
                            }
                }
                .invoke {
                    PanacheQueryException exception = Assertions.assertThrows(PanacheQueryException.class,
                            {
                                catRepository.find("select new FakeClass('fake_cat', 'fake_owner', 12.5) from Cat c")
                                        .project(CatProjectionBean.class)
                            })
                    Assertions.assertTrue(
                            exception.getMessage().startsWith("Unable to perform a projection on a 'select new' query"))
                }
                .flatMap {
                    catRepository
                            .find("   SELECT   disTINct  'GARFIELD', 'JoN ArBuCkLe' from Cat c where name = :NamE group by name  ",
                                    Parameters.with("NamE", catName))
                            .project(CatProjectionBean.class)
                            .<CatProjectionBean>firstResult()
                }
                .map { CatProjectionBean catView ->
                    // Must keep the letter case
                    Assertions.assertEquals("GARFIELD", catView?.name)
                    Assertions.assertEquals("JoN ArBuCkLe", catView?.ownerName)
                    "OK"
                }
    }

    @WithTransaction
    @GET
    @Path("model3")
    Uni<String> testModel3() {
        personDao.count()
                .flatMap({ count ->
                    Assertions.assertEquals(1, count)

                    personDao.findAll().<Person> firstResult()
                })
                .flatMap({ person ->
                    Assertions.assertEquals("2", person.name)

                    dogDao.deleteAll()
                }).flatMap({ personDao.deleteAll() })
                .flatMap({ addressDao.deleteAll() })
                .flatMap({ personDao.count() })
                .map({ count ->
                    Assertions.assertEquals(0, count)

                    "OK"
                })
    }

    @GET
    @Path("ignored-properties")
    Person ignoredProperties() throws NoSuchMethodException, SecurityException {
        Person.class.getMethod('$$_hibernate_read_id')
        Person.class.getMethod('$$_hibernate_read_name')
        try {
            Person.class.getMethod('$$_hibernate_read_persistent')
            Assertions.fail()
        } catch (NoSuchMethodException e) {
        }

        // no need to persist it, we can fake it
        new Person().tap {
            id = 666l
            name = "Eddie"
            status = Status.DECEASED
        }
    }

    @Inject
    Bug5274EntityRepository bug5274EntityRepository

    @GET
    @Path("5274")
    Uni<String> testBug5274() {
        bug5274EntityRepository.count()
                .map({ "OK" })
    }

    @Inject
    Bug5885EntityRepository bug5885EntityRepository

    @GET
    @Path("5885")
    Uni<String> testBug5885() {
        bug5885EntityRepository.findById(1L)
                .map({ "OK" })
    }

    @WithTransaction
    @GET
    @Path("composite")
    Uni<String> testCompositeKey() {
        ObjectWithCompositeId obj = new ObjectWithCompositeId().tap {
            part1 = "part1"
            part2 = "part2"
            description = "description"
        }
        obj.persist()
                .flatMap({
                    ObjectWithCompositeId.ObjectKey key = new ObjectWithCompositeId.ObjectKey("part1", "part2")
                    objectWithCompositeIdRepository.findById(key)
                            .flatMap({ result ->
                                assertNotNull(result)

                                objectWithCompositeIdRepository.deleteById(key)
                            }).flatMap({ deleted ->
                        assertTrue(deleted)

                        ObjectWithCompositeId.ObjectKey notExistingKey = new ObjectWithCompositeId.ObjectKey(
                                "notexist1",
                                "notexist2")
                        objectWithCompositeIdRepository.deleteById(key)
                    }).flatMap({ deleted ->
                        assertFalse(deleted)

                        ObjectWithEmbeddableId.ObjectKey embeddedKey = new ObjectWithEmbeddableId.ObjectKey("part1",
                                "part2")
                        ObjectWithEmbeddableId embeddable = new ObjectWithEmbeddableId()
                        embeddable.key = embeddedKey
                        embeddable.description = "description"
                        embeddable.persist()
                                .flatMap({ objectWithEmbeddableIdRepository.findById(embeddedKey) })
                                .flatMap({ embeddableResult ->
                                    assertNotNull(embeddableResult)

                                    objectWithEmbeddableIdRepository.deleteById(embeddedKey)
                                }).flatMap({ deleted2 ->
                            assertTrue(deleted2)

                            ObjectWithEmbeddableId.ObjectKey notExistingEmbeddedKey = new ObjectWithEmbeddableId.ObjectKey(
                                    "notexist1",
                                    "notexist2")
                            objectWithEmbeddableIdRepository.deleteById(notExistingEmbeddedKey)
                        }).map({ deleted2 ->
                            assertFalse(deleted2)

                            "OK"
                        })
                    })
                })
    }

    @GET
    @Path("7721")
    Uni<String> testBug7721() {
        Bug7721Entity entity = new Bug7721Entity()
        Panache.withTransaction({
            entity.persist()
                    .flatMap({ entity.delete() })
                    .map({ "OK" })
        })
    }

    @WithTransaction
    @GET
    @Path("8254")
    Uni<String> testBug8254() {
        CatOwner owner = new CatOwner("8254")
        owner.persist()
                .flatMap({ new Cat(owner).persist() })
                .flatMap({ new Cat(owner).persist() })
                .flatMap({ new Cat(owner).persist() })
                // This used to fail with an invalid query "SELECT COUNT(*) SELECT DISTINCT cat.owner FROM Cat cat WHERE cat.owner = ?1"
                // Should now result in a valid query "SELECT COUNT(DISTINCT cat.owner) FROM Cat cat WHERE cat.owner = ?1"
                .flatMap({ catOwnerRepository.find("SELECT DISTINCT cat.owner FROM Cat cat WHERE cat.owner = ?1", owner).count() })
                .flatMap({ count ->
                    assertEquals(1L, count)

                    // This used to fail with an invalid query "SELECT COUNT(*) SELECT cat.owner FROM Cat cat WHERE cat.owner = ?1"
                    // Should now result in a valid query "SELECT COUNT(cat.owner) FROM Cat cat WHERE cat.owner = ?1"
                    catOwnerRepository.find("SELECT cat.owner FROM Cat cat WHERE cat.owner = ?1", owner).count()
                }).flatMap({ count ->
                    assertEquals(3L, count)

                    // This used to fail with an invalid query "SELECT COUNT(*) SELECT cat FROM Cat cat WHERE cat.owner = ?1"
                    // Should now result in a valid query "SELECT COUNT(cat) FROM Cat cat WHERE cat.owner = ?1"
                    catRepository.find("SELECT cat FROM Cat cat WHERE cat.owner = ?1", owner).count()
                }).flatMap({ count ->
                    assertEquals(3L, count)

                    // This didn't use to fail. Make sure it still doesn't.
                    catRepository.find("FROM Cat WHERE owner = ?1", owner).count()
                }).flatMap({ count ->
                    assertEquals(3L, count)

                    catRepository.find("owner", owner).count()
                }).flatMap({ count ->
                    assertEquals(3L, count)

                    catOwnerRepository.find("name = ?1", "8254").count()
                }).map({ count ->
                    assertEquals(1L, count)

                    "OK"
                })
    }

    @WithTransaction
    @GET
    @Path("9025")
    Uni<String> testBug9025() {
        Fruit apple = new Fruit("apple", "red")
        Fruit orange = new Fruit("orange", "orange")
        Fruit banana = new Fruit("banana", "yellow")

        fruitRepository.persist(apple, orange, banana)
                .flatMap({
                    PanacheQuery<Fruit> query = fruitRepository.find(
                            "select name, color from Fruit").page(Page.ofSize(1))

                    query.list()
                            .flatMap({ query.pageCount() })
                            .map({ "OK" })
                })
    }

    @WithTransaction
    @GET
    @Path("9036")
    Uni<String> testBug9036() {
        personDao.deleteAll()
                .flatMap({ new Person().persist() })
                .flatMap({
                    Person deadPerson = new Person()
                    deadPerson.name = "Stef"
                    deadPerson.status = Status.DECEASED
                    deadPerson.persist()
                }).flatMap({
                    Person livePerson = new Person()
                    livePerson.name = "Stef"
                    livePerson.status = Status.LIVING
                    livePerson.persist()
                }).flatMap({ personDao.count() })
                .flatMap({ count ->
                    assertEquals(3, count)

                    personDao.listAll()
                }).flatMap({ list ->
                    assertEquals(3, list.size())

                    personDao.find("status", Status.LIVING).firstResult()
                }).flatMap({ livePerson ->
                    // should be filtered
                    PanacheQuery<Person> query = personDao.findAll(Sort.by("id")).filter("Person.isAlive").filter("Person.hasName",
                            Parameters.with("name", "Stef"))

                    query.count()
                            .flatMap({ count ->
                                assertEquals(1, count)

                                query.list()
                            }).flatMap({ list ->
                                assertEquals(1, list.size())

                                assertEquals(livePerson, list.get(0))
                                query.firstResult()
                            }).flatMap({ result ->
                                assertEquals(livePerson, result)

                                query.singleResult()
                            }).flatMap({ result ->
                                assertEquals(livePerson, result)

                                // these should be unaffected
                                personDao.count()
                            }).flatMap({ count ->
                                assertEquals(3, count)

                                personDao.listAll()
                            }).flatMap({ list ->
                                assertEquals(3, list.size())

                                personDao.deleteAll()
                            }).map({ "OK" })
                })
    }

    @GET
    @Path("testSortByNullPrecedence")
    @WithTransaction
    Uni<String> testSortByNullPrecedence() {
        personDao.deleteAll()
                .flatMap({
                    Person stefPerson = new Person()
                    stefPerson.name = "Stef"
                    stefPerson.uniqueName = "stef"

                    Person josePerson = new Person()
                    josePerson.name = null
                    josePerson.uniqueName = "jose"
                    Person.persist(stefPerson, josePerson)
                }).flatMap({ p ->
                    personDao.findAll(Sort.by("name", Sort.NullPrecedence.NULLS_FIRST)).list()
                }).flatMap({ list ->
                    assertEquals("jose", ((Person) list.get(0)).uniqueName)

                    personDao.findAll(Sort.by("name", Sort.NullPrecedence.NULLS_LAST)).list()
                }).flatMap({ list ->
                    assertEquals("jose", ((Person) list.get(list.size() - 1)).uniqueName)

                    personDao.deleteAll()
                }).map({ "OK" })
    }
}
