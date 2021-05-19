/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;

/** Unit test for {@link ExtensionFactory}. */
class ExtensionFactoryUnitTest {

    @Test
    void shouldLoadExtensionWithoutDependencies() {
        // Given
        Extension extension = new Extension1();
        Map<Class<? extends Extension>, Extension> availableExtensions = Collections.emptyMap();
        // When
        boolean canLoad = ExtensionFactory.canBeLoaded(availableExtensions, extension);
        // Then
        assertThat(canLoad, is(equalTo(true)));
    }

    @Test
    void shouldLoadExtensionWithDependencies() {
        // Given
        Extension ext1 = new Extension1();
        Extension ext2 = new Extension2();
        Extension ext3 = new Extension3(ext1, ext2);
        Map<Class<? extends Extension>, Extension> availableExtensions = createMap(ext1, ext2);
        // When
        boolean canLoad = ExtensionFactory.canBeLoaded(availableExtensions, ext3);
        // Then
        assertThat(canLoad, is(equalTo(true)));
    }

    @Test
    void shouldLoadExtensionWithCommonDependencies() {
        // Given
        Extension ext1 = new Extension1();
        Extension ext2 = new Extension2(ext1);
        Extension ext3 = new Extension3(ext1, ext2);
        Map<Class<? extends Extension>, Extension> availableExtensions = createMap(ext1, ext2);
        // When
        boolean canLoad = ExtensionFactory.canBeLoaded(availableExtensions, ext3);
        // Then
        assertThat(canLoad, is(equalTo(true)));
    }

    @Test
    void shouldNotLoadExtensionWithDirectCyclicDependencies() {
        // Given
        ExtensionImpl ext1 = new Extension1();
        Extension ext2 = new Extension2(ext1);
        ext1.addDependency(ext2.getClass());
        Map<Class<? extends Extension>, Extension> availableExtensions = createMap(ext1, ext2);
        // When
        boolean canLoad = ExtensionFactory.canBeLoaded(availableExtensions, ext2);
        // Then
        assertThat(canLoad, is(equalTo(false)));
    }

    @Test
    void shouldNotLoadExtensionWithIndirectCyclicDependencies() {
        // Given
        ExtensionImpl ext1 = new Extension1();
        Extension ext2 = new Extension2(ext1);
        Extension ext3 = new Extension2(ext2);
        ext1.addDependency(ext3.getClass());
        Map<Class<? extends Extension>, Extension> availableExtensions =
                createMap(ext1, ext2, ext3);
        // When
        boolean canLoad = ExtensionFactory.canBeLoaded(availableExtensions, ext1);
        // Then
        assertThat(canLoad, is(equalTo(false)));
    }

    @Test
    void shouldNotLoadExtensionWithMissingDependencies() {
        // Given
        Extension ext1 = new Extension1();
        Extension ext2 = new Extension2(ext1);
        Map<Class<? extends Extension>, Extension> availableExtensions = Collections.emptyMap();
        // When
        boolean canLoad = ExtensionFactory.canBeLoaded(availableExtensions, ext2);
        // Then
        assertThat(canLoad, is(equalTo(false)));
    }

    private static Map<Class<? extends Extension>, Extension> createMap(Extension... extensions) {
        if (extensions == null || extensions.length == 0) {
            return Collections.emptyMap();
        }

        Map<Class<? extends Extension>, Extension> map = new HashMap<>();
        for (Extension ext : extensions) {
            map.put(ext.getClass(), ext);
        }
        return map;
    }

    private static class ExtensionImpl extends ExtensionAdaptor {

        private final List<Class<? extends Extension>> dependencies = new ArrayList<>();

        ExtensionImpl(Extension... dependencies) {
            if (dependencies != null && dependencies.length != 0) {
                for (Extension ext : dependencies) {
                    this.dependencies.add(ext.getClass());
                }
            }
        }

        void addDependency(Class<? extends Extension> dependency) {
            dependencies.add(dependency);
        }

        @Override
        public List<Class<? extends Extension>> getDependencies() {
            return dependencies;
        }

        @Override
        public String getAuthor() {
            return null;
        }
    }

    private static class Extension1 extends ExtensionImpl {

        Extension1(Extension... dependencies) {
            super(dependencies);
        }
    }

    private static class Extension2 extends ExtensionImpl {

        Extension2(Extension... dependencies) {
            super(dependencies);
        }
    }

    private static class Extension3 extends ExtensionImpl {

        Extension3(Extension... dependencies) {
            super(dependencies);
        }
    }
}
