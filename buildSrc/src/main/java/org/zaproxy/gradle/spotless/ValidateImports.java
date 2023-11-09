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
package org.zaproxy.gradle.spotless;

import com.diffplug.spotless.FormatterFunc;
import java.util.Map;
import java.util.Objects;

public class ValidateImports implements FormatterFunc {

    private final Map<String, String> invalidImports;

    public ValidateImports(Map<String, String> invalidImports) {
        this.invalidImports = Objects.requireNonNull(invalidImports);
    }

    @Override
    public String apply(String input) throws Exception {
        for (var entry : invalidImports.entrySet()) {
            if (input.contains(entry.getKey())) {
                throw new InvalidImportException(entry.getValue());
            }
        }
        return input;
    }

    private static final class InvalidImportException extends AssertionError {

        private static final long serialVersionUID = 1L;

        InvalidImportException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
