/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Helper class that reads a {@code ZapAddOn.xml} file.
 * 
 * @since 2.4.0
 */
public class ZapAddOnXmlFile {

    private static final Logger LOGGER = Logger.getLogger(ZapAddOnXmlFile.class);

    private static final String NAME_ELEMENT = "name";
    private static final String VERSION_ELEMENT = "version";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String AUTHOR_ELEMENT = "author";
    private static final String URL_ELEMENT = "url";
    private static final String CHANGES_ELEMENT = "changes";
    private static final String NOT_BEFORE_VERSION_ELEMENT = "not-before-version";
    private static final String NOT_FROM_VERSION_ELEMENT = "not-from-version";

    private static final String EXTENSION_ELEMENT = "extension";
    private static final String EXTENSIONS_ALL_ELEMENTS = "extensions." + EXTENSION_ELEMENT;
    private static final String ASCANRULE_ELEMENT = "ascanrule";
    private static final String ASCANRULES_ALL_ELEMENTS = "ascanrules." + ASCANRULE_ELEMENT;
    private static final String PSCANRULE_ELEMENT = "pscanrule";
    private static final String PSCANRULES_ALL_ELEMENTS = "pscanrules." + PSCANRULE_ELEMENT;
    private static final String FILE_ELEMENT = "file";
    private static final String FILES_ALL_ELEMENTS = "files." + FILE_ELEMENT;

    private final String name;
    private final int version;
    private final String description;
    private final String author;
    private final String url;
    private final String changes;

    private final List<String> extensions;
    private final List<String> ascanrules;
    private final List<String> pscanrules;
    private final List<String> files;

    private final String notBeforeVersion;
    private final String notFromVersion;

    public ZapAddOnXmlFile(InputStream is) throws IOException {
        ZapXmlConfiguration zapAddOnXml = new ZapXmlConfiguration();
        try {
            zapAddOnXml.load(is);
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }

        name = zapAddOnXml.getString(NAME_ELEMENT, "");
        version = zapAddOnXml.getInt(VERSION_ELEMENT, 0);
        description = zapAddOnXml.getString(DESCRIPTION_ELEMENT, "");
        author = zapAddOnXml.getString(AUTHOR_ELEMENT, "");
        url = zapAddOnXml.getString(URL_ELEMENT, "");
        changes = zapAddOnXml.getString(CHANGES_ELEMENT, "");

        extensions = getStrings(zapAddOnXml, EXTENSIONS_ALL_ELEMENTS, EXTENSION_ELEMENT);
        ascanrules = getStrings(zapAddOnXml, ASCANRULES_ALL_ELEMENTS, ASCANRULE_ELEMENT);
        pscanrules = getStrings(zapAddOnXml, PSCANRULES_ALL_ELEMENTS, PSCANRULE_ELEMENT);
        files = getStrings(zapAddOnXml, FILES_ALL_ELEMENTS, FILE_ELEMENT);

        notBeforeVersion = zapAddOnXml.getString(NOT_BEFORE_VERSION_ELEMENT, "");
        notFromVersion = zapAddOnXml.getString(NOT_FROM_VERSION_ELEMENT, "");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public int getVersion() {
        return version;
    }

    public String getChanges() {
        return changes;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public List<String> getAscanrules() {
        return ascanrules;
    }

    public List<String> getPscanrules() {
        return pscanrules;
    }

    public List<String> getFiles() {
        return files;
    }

    public String getNotBeforeVersion() {
        return notBeforeVersion;
    }

    public String getNotFromVersion() {
        return notFromVersion;
    }

    private List<String> getStrings(ZapXmlConfiguration zapAddOnXml, String element, String elementName) {
        String[] fields = zapAddOnXml.getStringArray(element);
        ArrayList<String> strings = new ArrayList<>(fields.length);
        for (String field : fields) {
            if (!field.isEmpty()) {
                strings.add(field);
            } else {
                LOGGER.warn("Ignoring empty \"" + elementName + "\" entry in add-on \"" + name + "\".");
            }
        }
        strings.trimToSize();
        return strings;
    }
}
