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
package io.quarkiverse.groovy.hibernate.orm.panache.deployment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import io.quarkus.hibernate.orm.deployment.JpaModelPersistenceUnitMappingBuildItem;

public final class EntityToPersistenceUnitUtil {

    private EntityToPersistenceUnitUtil() {
    }

    /**
     * Given the candidate entities, return a map with the persistence unit that single contains them
     * or throw an exception if any of the candidates is part of more than one persistence unit
     */
    public static Map<String, String> determineEntityPersistenceUnits(
            Optional<JpaModelPersistenceUnitMappingBuildItem> jpaModelPersistenceUnitMapping,
            Set<String> candidates, String source) {
        if (jpaModelPersistenceUnitMapping.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        Map<String, Set<String>> collectedEntityToPersistenceUnits = jpaModelPersistenceUnitMapping.get()
                .getEntityToPersistenceUnits();

        Map<String, Set<String>> violatingEntities = new TreeMap<>();

        for (Map.Entry<String, Set<String>> entry : collectedEntityToPersistenceUnits.entrySet()) {
            String entityName = entry.getKey();
            Set<String> selectedPersistenceUnits = entry.getValue();
            boolean isCandidate = candidates.contains(entityName);

            if (!isCandidate) {
                continue;
            }

            if (selectedPersistenceUnits.size() == 1) {
                result.put(entityName, selectedPersistenceUnits.iterator().next());
            } else {
                violatingEntities.put(entityName, selectedPersistenceUnits);
            }
        }

        if (violatingEntities.size() > 0) {
            StringBuilder message = new StringBuilder(
                    String.format("%s entities do not support being attached to several persistence units:\n", source));
            for (Map.Entry<String, Set<String>> violatingEntityEntry : violatingEntities
                    .entrySet()) {
                message.append("\t- ").append(violatingEntityEntry.getKey()).append(" is attached to: ")
                        .append(String.join(",", violatingEntityEntry.getValue()));
                throw new IllegalStateException(message.toString());
            }
        }

        return result;
    }
}
