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
package io.quarkiverse.groovy.hibernate.reactive.panache.deployment;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Id;

import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntity;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheRepository;
import io.quarkiverse.groovy.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem;
import io.quarkus.arc.processor.Annotations;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.builder.BuildException;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.Feature;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.AdditionalApplicationArchiveMarkerBuildItem;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.util.JandexUtil;
import io.quarkus.hibernate.orm.deployment.AdditionalJpaModelBuildItem;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithSessionOnDemand;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.deployment.HibernateEnhancersRegisteredBuildItem;
import io.quarkus.panache.common.deployment.PanacheJpaEntityOperationsEnhancer;
import io.quarkus.panache.common.deployment.PanacheMethodCustomizer;
import io.quarkus.panache.common.deployment.PanacheMethodCustomizerBuildItem;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public final class PanacheHibernateResourceProcessor {

    static final DotName DOTNAME_PANACHE_REPOSITORY_BASE = DotName.createSimple(PanacheRepositoryBase.class.getName());
    private static final DotName DOTNAME_PANACHE_REPOSITORY = DotName.createSimple(PanacheRepository.class.getName());
    static final DotName DOTNAME_PANACHE_ENTITY_BASE = DotName.createSimple(PanacheEntityBase.class.getName());
    private static final DotName DOTNAME_PANACHE_ENTITY = DotName.createSimple(PanacheEntity.class.getName());

    private static final DotName DOTNAME_REACTIVE_SESSION = DotName.createSimple(Mutiny.Session.class.getName());

    private static final DotName DOTNAME_ID = DotName.createSimple(Id.class.getName());
    protected static final String META_INF_PANACHE_ARCHIVE_MARKER = "META-INF/panache-archive.marker";

    private static final DotName WITH_SESSION_ON_DEMAND = DotName.createSimple(WithSessionOnDemand.class.getName());
    private static final DotName WITH_SESSION = DotName.createSimple(WithSession.class.getName());
    private static final DotName WITH_TRANSACTION = DotName.createSimple(WithTransaction.class.getName());
    private static final DotName UNI = DotName.createSimple(Uni.class.getName());
    private static final DotName DOTNAME_UNI = DotName.createSimple(Uni.class.getName());
    private static final DotName DOTNAME_MULTI = DotName.createSimple(Multi.class.getName());

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem(Feature.HIBERNATE_REACTIVE_PANACHE);
    }

    @BuildStep
    AdditionalJpaModelBuildItem produceModel() {
        // only useful for the index resolution: hibernate will register it to be transformed, but BuildMojo
        // only transforms classes from the application jar, so we do our own transforming
        return new AdditionalJpaModelBuildItem("io.quarkiverse.groovy.hibernate.reactive.panache.PanacheEntity");
    }

    @BuildStep
    UnremovableBeanBuildItem ensureBeanLookupAvailable() {
        return UnremovableBeanBuildItem.beanTypes(DOTNAME_REACTIVE_SESSION);
    }

    @BuildStep
    AdditionalApplicationArchiveMarkerBuildItem marker() {
        return new AdditionalApplicationArchiveMarkerBuildItem(META_INF_PANACHE_ARCHIVE_MARKER);
    }

    @BuildStep
    void collectEntityClasses(CombinedIndexBuildItem index, BuildProducer<PanacheEntityClassBuildItem> entityClasses) {
        // NOTE: we don't skip abstract/generic entities because they still need accessors
        for (ClassInfo panacheEntityBaseSubclass : index.getIndex().getAllKnownSubclasses(DOTNAME_PANACHE_ENTITY_BASE)) {
            // FIXME: should we really skip PanacheEntity or all MappedSuperClass?
            if (!panacheEntityBaseSubclass.name().equals(DOTNAME_PANACHE_ENTITY)) {
                entityClasses.produce(new PanacheEntityClassBuildItem(panacheEntityBaseSubclass));
            }
        }
    }

    @BuildStep
    @Consume(HibernateEnhancersRegisteredBuildItem.class)
    void build(CombinedIndexBuildItem index,
            BuildProducer<BytecodeTransformerBuildItem> transformers,
            List<PanacheEntityClassBuildItem> entityClasses,
            List<PanacheMethodCustomizerBuildItem> methodCustomizersBuildItems) throws Exception {

        List<PanacheMethodCustomizer> methodCustomizers = methodCustomizersBuildItems.stream()
                .map(bi -> bi.getMethodCustomizer()).collect(Collectors.toList());

        PanacheJpaRepositoryEnhancer daoEnhancer = new PanacheJpaRepositoryEnhancer(index.getIndex());
        Set<String> daoClasses = new HashSet<>();
        for (ClassInfo classInfo : index.getIndex().getAllKnownImplementors(DOTNAME_PANACHE_REPOSITORY_BASE)) {
            // Skip PanacheRepository
            if (classInfo.name().equals(DOTNAME_PANACHE_REPOSITORY))
                continue;
            if (daoEnhancer.skipRepository(classInfo))
                continue;
            daoClasses.add(classInfo.name().toString());
        }
        for (ClassInfo classInfo : index.getIndex().getAllKnownImplementors(DOTNAME_PANACHE_REPOSITORY)) {
            if (daoEnhancer.skipRepository(classInfo))
                continue;
            daoClasses.add(classInfo.name().toString());
        }
        for (String daoClass : daoClasses) {
            transformers.produce(new BytecodeTransformerBuildItem(daoClass, daoEnhancer));
        }

        PanacheJpaEntityOperationsEnhancer entityOperationsEnhancer = new PanacheJpaEntityOperationsEnhancer(index.getIndex(),
                methodCustomizers,
                ReactiveGroovyJpaTypeBundle.BUNDLE);
        for (PanacheEntityClassBuildItem entityClass : entityClasses) {
            String entityClassName = entityClass.get().name().toString();
            transformers.produce(new BytecodeTransformerBuildItem(true, entityClassName, entityOperationsEnhancer));
        }
    }

    @BuildStep
    ValidationPhaseBuildItem.ValidationErrorBuildItem validate(ValidationPhaseBuildItem validationPhase,
            CombinedIndexBuildItem index) throws BuildException {
        // we verify that no ID fields are defined (via @Id) when extending PanacheEntity
        for (AnnotationInstance annotationInstance : index.getIndex().getAnnotations(DOTNAME_ID)) {
            ClassInfo info = JandexUtil.getEnclosingClass(annotationInstance);
            if (JandexUtil.isSubclassOf(index.getIndex(), info, DOTNAME_PANACHE_ENTITY)) {
                BuildException be = new BuildException("You provide a JPA identifier via @Id inside '" + info.name() +
                        "' but one is already provided by PanacheEntity, " +
                        "your class should extend PanacheEntityBase instead, or use the id provided by PanacheEntity",
                        Collections.emptyList());
                return new ValidationPhaseBuildItem.ValidationErrorBuildItem(be);
            }
        }
        return null;
    }

    @BuildStep
    void transformResourceMethods(CombinedIndexBuildItem index, Capabilities capabilities,
            BuildProducer<AnnotationsTransformerBuildItem> annotationsTransformer) {
        if (capabilities.isPresent(Capability.RESTEASY_REACTIVE)) {
            // Custom request method designators are not supported
            List<DotName> designators = List.of(DotName.createSimple("jakarta.ws.rs.GET"),
                    DotName.createSimple("jakarta.ws.rs.HEAD"),
                    DotName.createSimple("jakarta.ws.rs.DELETE"), DotName.createSimple("jakarta.ws.rs.OPTIONS"),
                    DotName.createSimple("jakarta.ws.rs.PATCH"), DotName.createSimple("jakarta.ws.rs.POST"),
                    DotName.createSimple("jakarta.ws.rs.PUT"));
            List<DotName> bindings = List.of(WITH_SESSION, WITH_SESSION_ON_DEMAND, WITH_TRANSACTION);

            // Collect all panache entities
            Set<DotName> entities = new HashSet<>();
            for (ClassInfo subclass : index.getIndex().getAllKnownSubclasses(DOTNAME_PANACHE_ENTITY_BASE)) {
                if (!subclass.name().equals(DOTNAME_PANACHE_ENTITY)) {
                    entities.add(subclass.name());
                }
            }
            Set<DotName> entityUsers = new HashSet<>();
            for (DotName entity : entities) {
                for (ClassInfo user : index.getIndex().getKnownUsers(entity)) {
                    entityUsers.add(user.name());
                }
            }

            annotationsTransformer.produce(new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
                @Override
                public boolean appliesTo(AnnotationTarget.Kind kind) {
                    return kind == AnnotationTarget.Kind.METHOD;
                }

                @Override
                public void transform(TransformationContext context) {
                    MethodInfo method = context.getTarget().asMethod();
                    Collection<AnnotationInstance> annotations = context.getAnnotations();
                    if (method.isSynthetic()
                            || Modifier.isStatic(method.flags())
                            || method.declaringClass().isInterface()
                            || !method.returnType().name().equals(UNI)
                            || !entityUsers.contains(method.declaringClass().name())
                            || !Annotations.containsAny(annotations, designators)
                            || Annotations.containsAny(annotations, bindings)) {
                        return;
                    }
                    // Add @WithSessionOnDemand to a method that
                    // - is not static
                    // - is not synthetic
                    // - returns Uni
                    // - is declared in a class that uses a panache entity
                    // - is annotated with @GET, @POST, @PUT, @DELETE ,@PATCH ,@HEAD or @OPTIONS
                    // - is not annotated with @ReactiveTransactional, @WithSession, @WithSessionOnDemand, or @WithTransaction
                    context.transform().add(WITH_SESSION_ON_DEMAND).done();
                }
            }));
        }
    }

    private static final String CHECK_RETURN_VALUE_BINARY_NAME = "io/smallrye/common/annotation/CheckReturnValue";
    private static final String CHECK_RETURN_VALUE_SIGNATURE = "L" + CHECK_RETURN_VALUE_BINARY_NAME + ";";

    @BuildStep
    PanacheMethodCustomizerBuildItem mutinyReturnTypes() {
        return new PanacheMethodCustomizerBuildItem(new PanacheMethodCustomizer() {
            @Override
            public void customize(Type entityClassSignature, MethodInfo method, MethodVisitor mv) {
                DotName returnType = method.returnType().name();
                if (returnType.equals(DOTNAME_UNI) || returnType.equals(DOTNAME_MULTI)) {
                    mv.visitAnnotation(CHECK_RETURN_VALUE_SIGNATURE, true);
                }
            }
        });
    }
}
