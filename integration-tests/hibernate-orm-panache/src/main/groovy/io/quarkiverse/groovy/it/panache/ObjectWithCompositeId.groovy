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

import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass

import io.quarkiverse.groovy.hibernate.orm.panache.PanacheEntityBase

@Entity
@IdClass(ObjectWithCompositeId.ObjectKey.class)
class ObjectWithCompositeId extends PanacheEntityBase {
    @Id
    public String part1
    @Id
    public String part2
    public String description

    @EqualsAndHashCode(includeFields=true)
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
