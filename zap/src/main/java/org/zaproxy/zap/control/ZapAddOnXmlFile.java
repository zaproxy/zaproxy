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
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Helper class that reads a {@link AddOn#MANIFEST_FILE_NAME manifest file}.
 *
 * @since 2.4.0
 */
public class ZapAddOnXmlFile extends BaseZapAddOnXmlData {

    private static final String ASCANRULE_ELEMENT = "ascanrule";
    private static final String ASCANRULES_ALL_ELEMENTS = "ascanrules." + ASCANRULE_ELEMENT;
    private static final String PSCANRULE_ELEMENT = "pscanrule";
    private static final String PSCANRULES_ALL_ELEMENTS = "pscanrules." + PSCANRULE_ELEMENT;
    private static final String FILE_ELEMENT = "file";
    private static final String FILES_ALL_ELEMENTS = "files." + FILE_ELEMENT;
    private static final String LIB_ELEMENT = "lib";
    private static final String LIBS_ALL_ELEMENTS = "libs." + LIB_ELEMENT;
    private static final String BUNDLE_ELEMENT = "bundle";
    private static final String BUNDLE_PREFIX_ATT = "bundle[@prefix]";
    private static final String HELPSET_ELEMENT = "helpset";
    private static final String HELPSET_LOCALE_TOKEN_ATT = "helpset[@localetoken]";

    private List<String> ascanrules;
    private List<String> pscanrules;
    private List<String> files;
    private List<String> libs;

    private String bundleBaseName;
    private String bundlePrefix;
    private String helpSetBaseName;
    private String helpSetLocaleToken;

    public ZapAddOnXmlFile(InputStream is) throws IOException {
        super(is);
    }

    @Override
    protected void readAdditionalData(HierarchicalConfiguration zapAddOnXml) {
        ascanrules = getStrings(zapAddOnXml, ASCANRULES_ALL_ELEMENTS, ASCANRULE_ELEMENT);
        pscanrules = getStrings(zapAddOnXml, PSCANRULES_ALL_ELEMENTS, PSCANRULE_ELEMENT);
        files = getStrings(zapAddOnXml, FILES_ALL_ELEMENTS, FILE_ELEMENT);
        libs = getStrings(zapAddOnXml, LIBS_ALL_ELEMENTS, LIB_ELEMENT);

        bundleBaseName = zapAddOnXml.getString(BUNDLE_ELEMENT, "");
        bundlePrefix = zapAddOnXml.getString(BUNDLE_PREFIX_ATT, "");
        helpSetBaseName = zapAddOnXml.getString(HELPSET_ELEMENT, "");
        helpSetLocaleToken = zapAddOnXml.getString(HELPSET_LOCALE_TOKEN_ATT, "");
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

    /**
     * Gets the libraries of the add-on.
     *
     * @return the libraries, never {@code null}.
     * @since 2.9.0
     */
    public List<String> getLibs() {
        return libs;
    }

    /**
     * Gets the base name of the bundle.
     *
     * @return the base name of the bundle, never {@code null}.
     * @since 2.8.0
     */
    public String getBundleBaseName() {
        return bundleBaseName;
    }

    /**
     * Gets the prefix of the bundle.
     *
     * @return the prefix of the bundle, never {@code null}.
     * @since 2.8.0
     */
    public String getBundlePrefix() {
        return bundlePrefix;
    }

    /**
     * Gets the base name of the HelpSet file.
     *
     * @return the base name of the HelpSet file, never {@code null}.
     * @since 2.8.0
     */
    public String getHelpSetBaseName() {
        return helpSetBaseName;
    }

    /**
     * Gets the locale token for the HelpSet file.
     *
     * @return the locale token for the HelpSet file, never {@code null}.
     * @since 2.8.0
     */
    public String getHelpSetLocaleToken() {
        return helpSetLocaleToken;
    }
}
