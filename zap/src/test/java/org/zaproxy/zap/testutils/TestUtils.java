/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.testutils;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Class with utility/helper methods for general tests. */
public class TestUtils {

    /**
     * Gets the (file system) path to the given resource.
     *
     * <p>The resource path is obtained with the caller class using {@link
     * Class#getResource(String)}.
     *
     * @param resourcePath the path to the resource.
     * @return the path, never {@code null}.
     */
    protected Path getResourcePath(String resourcePath) {
        return getResourcePath(resourcePath, getClass());
    }

    /**
     * Gets the (file system) path to the given resource using the given class.
     *
     * <p>The resource path is obtained with the given class using {@link
     * Class#getResource(String)}.
     *
     * @param resourcePath the path to the resource.
     * @param clazz the class used to obtain the resource.
     * @return the path, never {@code null}.
     */
    protected static <T> Path getResourcePath(String resourcePath, Class<T> clazz) {
        try {
            URL url = clazz.getResource(resourcePath);
            if (url == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
