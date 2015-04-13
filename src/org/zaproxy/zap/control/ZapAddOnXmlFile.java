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
 * Helper class that reads a {@code ZapAddOn.xml} file.
 * 
 * @since 2.4.0
 */
public class ZapAddOnXmlFile extends BaseZapAddOnXmlData {

    private static final String ASCANRULE_ELEMENT = "ascanrule";
    private static final String ASCANRULES_ALL_ELEMENTS = "ascanrules/" + ASCANRULE_ELEMENT;
    private static final String PSCANRULE_ELEMENT = "pscanrule";
    private static final String PSCANRULES_ALL_ELEMENTS = "pscanrules/" + PSCANRULE_ELEMENT;
    private static final String FILE_ELEMENT = "file";
    private static final String FILES_ALL_ELEMENTS = "files/" + FILE_ELEMENT;

    private List<String> ascanrules;
    private List<String> pscanrules;
    private List<String> files;

    public ZapAddOnXmlFile(InputStream is) throws IOException {
        super(is);
    }

    @Override
    protected void readAdditionalData(HierarchicalConfiguration zapAddOnXml) {
        ascanrules = getStrings(zapAddOnXml, ASCANRULES_ALL_ELEMENTS, ASCANRULE_ELEMENT);
        pscanrules = getStrings(zapAddOnXml, PSCANRULES_ALL_ELEMENTS, PSCANRULE_ELEMENT);
        files = getStrings(zapAddOnXml, FILES_ALL_ELEMENTS, FILE_ELEMENT);
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
}
