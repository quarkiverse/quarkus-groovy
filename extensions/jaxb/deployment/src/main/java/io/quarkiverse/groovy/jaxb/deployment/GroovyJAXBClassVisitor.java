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
package io.quarkiverse.groovy.jaxb.deployment;

import jakarta.xml.bind.annotation.XmlTransient;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import io.quarkiverse.groovy.deployment.GroovyUtil;
import io.quarkus.gizmo.Gizmo;

class GroovyJAXBClassVisitor extends ClassVisitor {

    private static final String ANNOTATION_TYPE_NAME = String.format("L%s;", XmlTransient.class.getName().replace('.', '/'));

    GroovyJAXBClassVisitor(ClassVisitor outputClassVisitor) {
        super(Gizmo.ASM_API_VERSION, outputClassVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (GroovyUtil.isGetMetaClassMethod(name, descriptor)) {
            methodVisitor.visitAnnotation(ANNOTATION_TYPE_NAME, true).visitEnd();
        }
        return methodVisitor;
    }
}
