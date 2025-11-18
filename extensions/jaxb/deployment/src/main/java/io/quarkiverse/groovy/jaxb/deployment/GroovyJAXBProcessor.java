package io.quarkiverse.groovy.jaxb.deployment;

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
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.quarkiverse.groovy.deployment.GroovyUtil;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.jaxb.deployment.JaxbClassesToBeBoundBuildItem;

class GroovyJAXBProcessor {

    private static final Logger log = Logger.getLogger(GroovyJAXBProcessor.class);

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("groovy-jaxb");
    }

    @BuildStep
    void transform(CombinedIndexBuildItem index,
            BuildProducer<BytecodeTransformerBuildItem> transformers,
            List<JaxbClassesToBeBoundBuildItem> jaxbClasses) {
        IndexView view = index.getIndex();
        Set<String> classesToTransform = new HashSet<>();
        Set<String> classesProcessed = new HashSet<>();
        for (JaxbClassesToBeBoundBuildItem item : jaxbClasses) {
            for (String className : item.getClasses()) {
                addClassesToTransform(view, className, classesToTransform, classesProcessed);
            }
        }
        if (classesToTransform.isEmpty()) {
            return;
        }
        GroovyJAXBEnhancer enhancer = new GroovyJAXBEnhancer();
        classesToTransform.forEach(name -> transformers.produce(new BytecodeTransformerBuildItem(name, enhancer)));
    }

    private void addClassesToTransform(IndexView view, String className, Set<String> classesToTransform,
            Set<String> classesProcessed) {
        if (classesProcessed.add(className) && !GroovyUtil.isGroovyClass(className) && !isJDKClass(className)) {
            ClassInfo classInfo = view.getClassByName(className);
            if (classInfo == null) {
                log.warnf("The class %s could not be found in the index", className);
                return;
            }
            if (GroovyUtil.isGroovyObject(classInfo)) {
                classesToTransform.add(className);
            }
            for (FieldInfo fieldInfo : classInfo.fields()) {
                if (!Modifier.isStatic(fieldInfo.flags())) {
                    addClassesToTransform(view, fieldInfo.type(), classesToTransform, classesProcessed);
                }
            }
        }
    }

    private void addClassesToTransform(IndexView view, Type type, Set<String> classesToTransform,
            Set<String> classesProcessed) {
        if (type.kind() == Type.Kind.CLASS) {
            addClassesToTransform(view, type.name().toString(), classesToTransform, classesProcessed);
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            for (Type pType : type.asParameterizedType().arguments()) {
                addClassesToTransform(view, pType, classesToTransform, classesProcessed);
            }
        } else if (type.kind() == Type.Kind.ARRAY) {
            addClassesToTransform(view, type.asArrayType().constituent(), classesToTransform, classesProcessed);
        }
    }

    private static boolean isJDKClass(String className) {
        return className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("jdk.");
    }
}
