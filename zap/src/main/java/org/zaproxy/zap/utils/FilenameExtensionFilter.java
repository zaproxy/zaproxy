/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * A {@link FilenameFilter} that allows to filter by an extension.
 *
 * @since 1.2.0
 */
public class FilenameExtensionFilter implements FilenameFilter {

    String ext;
    boolean ignoreCase = false;

    /**
     * Constructs a {@code FilenameExtensionFilter} with the given extension and if the case should
     * be ignored.
     *
     * @param ext the extension that the files must have.
     * @param ignoreCase if the case should be ignored.
     */
    public FilenameExtensionFilter(String ext, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        this.ext = ignoreCase ? ext.toLowerCase(Locale.ROOT) : ext;
    }

    @Override
    public boolean accept(File dir, String name) {
        if (ignoreCase) {
            return name.toLowerCase(Locale.ROOT).endsWith(ext);
        }
        return name.endsWith(ext);
    }
}
