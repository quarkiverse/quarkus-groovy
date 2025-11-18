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

import jakarta.persistence.EntityManager;
import jakarta.transaction.TransactionManager;

import org.hibernate.Session;

import io.quarkiverse.groovy.hibernate.orm.panache.runtime.JpaOperations;
import io.quarkus.hibernate.orm.panache.common.runtime.AbstractJpaOperations;
import io.quarkus.panache.common.Parameters;

/**
 * Utility class for Panache.
 */
public class Panache {

    /**
     * Returns the default {@link EntityManager}
     *
     * @return {@link EntityManager}
     */
    public static EntityManager getEntityManager() {
        return JpaOperations.INSTANCE.getSession();
    }

    /**
     * Returns the default {@link Session}
     *
     * @return {@link Session}
     */
    public static Session getSession() {
        return JpaOperations.INSTANCE.getSession();
    }

    /**
     * Returns the {@link EntityManager} for the given {@link Class<?> entity}
     *
     * @param clazz the entity class corresponding to the entity manager persistence unit.
     * @return {@link EntityManager}
     */
    public static EntityManager getEntityManager(Class<?> clazz) {
        return JpaOperations.INSTANCE.getSession(clazz);
    }

    /**
     * Returns the {@link Session} for the given {@link Class<?> entity}
     *
     * @param clazz the entity class corresponding to the entity manager persistence unit.
     * @return {@link Session}
     */
    public static Session getSession(Class<?> clazz) {
        return JpaOperations.INSTANCE.getSession(clazz);
    }

    /**
     * Returns the {@link EntityManager} for the given persistence unit
     *
     * @param persistenceUnit the persistence unit for this entity manager.
     * @return {@link EntityManager}
     */
    public static EntityManager getEntityManager(String persistenceUnit) {
        return JpaOperations.INSTANCE.getSession(persistenceUnit);
    }

    /**
     * Returns the {@link Session} for the given persistence unit
     *
     * @param persistenceUnit the persistence unit for this entity manager.
     * @return {@link Session}
     */
    public static Session getSession(String persistenceUnit) {
        return JpaOperations.INSTANCE.getSession(persistenceUnit);
    }

    /**
     * Returns the current {@link TransactionManager}
     *
     * @return the current {@link TransactionManager}
     */
    public static TransactionManager getTransactionManager() {
        return AbstractJpaOperations.getTransactionManager();
    }

    /**
     * Executes a database update operation and return the number of rows operated on.
     *
     * @param query a normal HQL query
     * @param params optional list of indexed parameters
     * @return the number of rows operated on.
     */
    public static int executeUpdate(String query, Object... params) {
        return JpaOperations.INSTANCE.executeUpdate(query, params);
    }

    /**
     * Executes a database update operation and return the number of rows operated on.
     *
     * @param query a normal HQL query
     * @param params {@link Map} of named parameters
     * @return the number of rows operated on.
     */
    public static int executeUpdate(String query, Map<String, Object> params) {
        return JpaOperations.INSTANCE.executeUpdate(query, params);
    }

    /**
     * Executes a database update operation and return the number of rows operated on.
     *
     * @param query a normal HQL query
     * @param params {@link Parameters} of named parameters
     * @return the number of rows operated on.
     */
    public static int executeUpdate(String query, Parameters params) {
        return JpaOperations.INSTANCE.executeUpdate(query, params.map());
    }

    /**
     * Marks the current transaction as "rollback-only", which means that it will not be
     * committed: it will be rolled back at the end of this transaction lifecycle.
     */
    public static void setRollbackOnly() {
        AbstractJpaOperations.setRollbackOnly();
    }

    /**
     * Flushes all pending changes to the database using the default entity manager.
     */
    public static void flush() {
        getSession().flush();
    }

    /**
     * Flushes all pending changes to the database using the entity manager for the given {@link Class<?> entity}
     *
     * @param clazz the entity class corresponding to the entity manager persistence unit.
     */
    public static void flush(Class<?> clazz) {
        getSession(clazz).flush();
    }

    /**
     * Flushes all pending changes to the database using the entity manager for the given persistence unit
     *
     * @param persistenceUnit the persistence unit for this entity manager.
     */
    public static void flush(String persistenceUnit) {
        getSession(persistenceUnit).flush();
    }
}
