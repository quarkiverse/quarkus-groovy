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

import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.objectweb.asm.ClassVisitor;

import io.quarkiverse.groovy.hibernate.orm.panache.deployment.visitors.PanacheEntityClassOperationGenerationVisitor;
import io.quarkus.panache.common.deployment.PanacheEntityEnhancer;
import io.quarkus.panache.common.deployment.PanacheMethodCustomizer;
import io.quarkus.panache.common.deployment.TypeBundle;

public class PanacheJpaEntityOperationsEnhancer extends PanacheEntityEnhancer {
    private final TypeBundle typeBundle;

    public PanacheJpaEntityOperationsEnhancer(IndexView index, List<PanacheMethodCustomizer> methodCustomizers,
            TypeBundle typeBundle) {
        super(index, methodCustomizers);
        this.typeBundle = typeBundle;
    }

    @Override
    public ClassVisitor apply(String className, ClassVisitor outputClassVisitor) {
        ClassInfo entityInfo = indexView.getClassByName(DotName.createSimple(className));
        return new PanacheEntityClassOperationGenerationVisitor(outputClassVisitor, typeBundle,
                entityInfo, methodCustomizers, indexView);
    }
}
