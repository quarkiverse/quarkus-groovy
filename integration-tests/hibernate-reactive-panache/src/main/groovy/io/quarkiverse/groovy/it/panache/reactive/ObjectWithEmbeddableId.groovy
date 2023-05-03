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

import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity

import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntityBase

@Entity
class ObjectWithEmbeddableId extends PanacheEntityBase {
    @EmbeddedId
    public ObjectKey key
    public String description

    @EqualsAndHashCode(includeFields=true)
    @Embeddable
    static class ObjectKey implements Serializable {
        private String part1
        private String part2

        ObjectKey() {
        }

        ObjectKey(part1, part2) {
            this.part1 = part1
            this.part2 = part2
        }
    }
}
