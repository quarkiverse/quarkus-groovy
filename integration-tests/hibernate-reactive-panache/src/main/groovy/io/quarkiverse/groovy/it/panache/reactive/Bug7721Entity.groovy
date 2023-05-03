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


import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Bug7721Entity extends Bug7721EntitySuperClass {
    @Column(nullable = false)
    public String foo = "default"

    Bug7721Entity() {
        foo = "default" // same as init
        this.foo = "default" // qualify
        superField = "default"
        this.superField = "default"
        super.superField = "default"

        Bug7721OtherEntity otherEntity = new Bug7721OtherEntity()
        otherEntity.foo = "bar" // we want to make sure the setter gets called because it's not our hierarchy
        if (otherEntity.foo != "BAR")
            throw new AssertionError("setter was not called", null)
    }

    void setFoo(String foo) {
        Objects.requireNonNull(foo)
        // should never be null
        Objects.requireNonNull(this.foo)
        this.foo = foo
    }
}
