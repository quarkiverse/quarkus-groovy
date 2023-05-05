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
package io.quarkiverse.groovy.deployment;

import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

/**
 * {@code GroovyUtil} is a utility class that provides methods that are specific to the Groovy language.
 */
public final class GroovyUtil {

    /**
     * The {@code DotName} corresponding to the {@link GroovyObject}.
     */
    public static final DotName DOTNAME_GROOVY_OBJECT = DotName.createSimple(GroovyObject.class.getName());
    /**
     * The name of the specific method allowing to get the meta class.
     */
    public static final String METHOD_GET_META_CLASS_NAME = "getMetaClass";
    /**
     * The descriptor of the method {@code getMetaClass()} in asm terminology.
     */
    public static final String METHOD_GET_META_CLASS_DESCRIPTOR = String.format("()L%s;",
            MetaClass.class.getName().replace('.', '/'));
    private static final List<String> GROOVY_PACKAGE_NAMES = List.of("org.codehaus.groovy.", "org.apache.groovy.", "groovy");

    private GroovyUtil() {
    }

    /**
     * @param classInfo the class to test.
     * @return {@code true} if the given class is a Groovy object, {@code false} otherwise.
     */
    public static boolean isGroovyObject(ClassInfo classInfo) {
        for (Type interfaceType : classInfo.interfaceTypes()) {
            if (DOTNAME_GROOVY_OBJECT.equals(interfaceType.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param name the name of the method to test.
     * @param descriptor the descriptor of the method to test in asm terminology.
     * @return {@code true} if the given method name and description match with the {@link GroovyObject#getMetaClass()},
     *         {@code false} otherwise.
     */
    public static boolean isGetMetaClassMethod(String name, String descriptor) {
        return METHOD_GET_META_CLASS_NAME.equals(name) && METHOD_GET_META_CLASS_DESCRIPTOR.equals(descriptor);
    }

    /**
     * @param name the full qualified name of the class to test.
     * @return {@code true} if the given class is part of the groovy library, {@code false} otherwise.
     */
    public static boolean isGroovyClass(String name) {
        for (String prefix : GROOVY_PACKAGE_NAMES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
