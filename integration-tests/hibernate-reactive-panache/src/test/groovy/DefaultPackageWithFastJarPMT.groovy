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
import static org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

import io.quarkus.builder.Version
import io.quarkus.test.ProdBuildResults
import io.quarkus.test.ProdModeTestResults
import io.quarkus.test.QuarkusProdModeTest

class DefaultPackageWithFastJarPMT {

    @RegisterExtension
    static final QuarkusProdModeTest config = new QuarkusProdModeTest()
            .withApplicationRoot { jar -> jar
                        .addClasses(PackagelessCat.class)
            }
            .setApplicationName("default-package")
            .setApplicationVersion(Version.getVersion())
            .withConfigurationResource("application.properties")
            .overrideConfigKey("quarkus.package.type", "fast-jar")

    @ProdBuildResults
    private ProdModeTestResults prodModeTestResults

    @Test
    void testJarCreated() {
        assertThat(prodModeTestResults.getResults()).hasSize(1)
        assertThat(prodModeTestResults.getResults().get(0).getPath()).exists()
    }
}
