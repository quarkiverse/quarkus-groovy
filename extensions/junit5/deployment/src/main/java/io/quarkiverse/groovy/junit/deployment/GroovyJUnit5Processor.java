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
package io.quarkiverse.groovy.junit.deployment;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.function.ThrowingSupplier;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;

class GroovyJUnit5Processor {

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("groovy-junit5");
    }

    @BuildStep(onlyIf = NativeBuild.class)
    List<NativeImageProxyDefinitionBuildItem> registerProxies() {
        List<NativeImageProxyDefinitionBuildItem> proxies = new ArrayList<>();
        proxies.add(new NativeImageProxyDefinitionBuildItem(Executable.class.getName()));
        proxies.add(new NativeImageProxyDefinitionBuildItem(ThrowingConsumer.class.getName()));
        proxies.add(new NativeImageProxyDefinitionBuildItem(ThrowingSupplier.class.getName()));
        return proxies;
    }
}
