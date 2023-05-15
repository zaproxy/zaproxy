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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ProjectProperties {

    private final Path file;
    private final Properties properties;

    public ProjectProperties(Path file) throws IOException {
        this.file = file;
        this.properties = new OrderedProperties();

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void store() throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            properties.store(writer, null);
        }
    }

    private static class OrderedProperties extends Properties {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("serial")
        private Map<Object, Object> properties;

        @Override
        public synchronized void load(Reader reader) throws IOException {
            properties = new LinkedHashMap<>();
            super.load(reader);
        }

        @Override
        public synchronized void load(InputStream inStream) throws IOException {
            properties = new LinkedHashMap<>();
            super.load(inStream);
        }

        @Override
        public synchronized Object setProperty(String key, String value) {
            return properties.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return (String) get(key);
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            return properties.put(key, value);
        }

        @Override
        public void store(Writer writer, String comments) throws IOException {
            super.store(new BufferedWriterSkipLine(writer), comments);
        }

        @Override
        public synchronized Object get(Object key) {
            return properties.get(key);
        }

        @Override
        public Set<Object> keySet() {
            return properties.keySet();
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return properties.entrySet();
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(properties.keySet());
        }

        private static class BufferedWriterSkipLine extends BufferedWriter {

            private boolean skipLine = true;

            BufferedWriterSkipLine(Writer out) {
                super(out);
            }

            @Override
            public void write(String str) throws IOException {
                if (skipLine) {
                    return;
                }
                super.write(str);
            }

            @Override
            public void newLine() throws IOException {
                if (skipLine) {
                    skipLine = false;
                    return;
                }
                super.newLine();
            }
        }
    }
}
