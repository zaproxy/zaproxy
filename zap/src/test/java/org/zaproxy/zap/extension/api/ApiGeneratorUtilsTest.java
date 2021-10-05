/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.control.CoreFunctionality;

/**
 * Checks that all the {@link ApiImplementor}s and its options have been added to {@link
 * ApiGeneratorUtils}.
 */
class ApiGeneratorUtilsTest extends WithConfigsTest {

    private Map<String, ApiImplementor> coreApis = new HashMap<>();

    @BeforeEach
    void loadCoreApis() throws Exception {
        Control.initSingletonForTesting(Model.getSingleton());
        ExtensionHook hook =
                new ExtensionHook(Model.getSingleton(), null) {
                    @Override
                    public void addApiImplementor(ApiImplementor apiImplementor) {
                        coreApis.put(apiImplementor.getPrefix(), apiImplementor);
                    }
                };
        ExtensionLoader extLoader = Control.getSingleton().getExtensionLoader();
        CoreFunctionality.getBuiltInExtensions()
                .forEach(
                        ext -> {
                            extLoader.addExtension(ext);
                            ext.init();
                            ext.initModel(Model.getSingleton());
                            ext.hook(hook);
                        });
    }

    @Test
    void shouldHaveAllCoreApisInApiGeneratorUtils() {
        // Given / When
        Map<String, ApiImplementor> generatorApis =
                ApiGeneratorUtils.getAllImplementors().stream()
                        .collect(Collectors.toMap(ApiImplementor::getPrefix, Function.identity()));
        // Then
        for (Entry<String, ApiImplementor> entry : coreApis.entrySet()) {
            ApiImplementor api = generatorApis.remove(entry.getKey());
            assertThat(
                    String.format("The API %s was not added to ApiGeneratorUtils.", entry.getKey()),
                    api,
                    is(notNullValue()));
            assertThat(
                    String.format("The API %s has no options in generator.", entry.getKey()),
                    api.hasApiOptions(),
                    is(entry.getValue().hasApiOptions()));
        }
        assertThat(
                "API(s) found in generator not present in ZAP.",
                generatorApis.keySet(),
                is(empty()));
    }
}
