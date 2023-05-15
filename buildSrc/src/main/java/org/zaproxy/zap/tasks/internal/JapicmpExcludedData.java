/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.zaproxy.zap.tasks.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JapicmpExcludedData {

    private final List<String> packageExcludes = List.of();
    private final List<String> fieldExcludes = List.of();
    private final List<String> classExcludes = List.of();
    private final List<String> methodExcludes = List.of();

    public List<String> getPackageExcludes() {
        return packageExcludes;
    }

    public List<String> getFieldExcludes() {
        return fieldExcludes;
    }

    public List<String> getClassExcludes() {
        return classExcludes;
    }

    public List<String> getMethodExcludes() {
        return methodExcludes;
    }

    public static JapicmpExcludedData from(String file) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(file))) {
            return new ObjectMapper(new YAMLFactory()).readValue(reader, JapicmpExcludedData.class);
        }
    }
}
