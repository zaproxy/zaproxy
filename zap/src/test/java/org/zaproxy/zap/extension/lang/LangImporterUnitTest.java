/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.extension.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Unit test for {@link LangImporter}. */
class LangImporterUnitTest {

    @Test
    void shouldIncludeMessagesAndVulnerabilitiesFiles() {
        // Given
        String[] resourceFiles = {
            "Messages.properties",
            "Messages_en.properties",
            "Messages_en_GB.properties",
            "Messages_ar_SA.properties",
            "Messages_fil_PH.properties",
            "Messages_zh_CN.properties",
            "vulnerabilities.xml",
            "vulnerabilities_en.xml",
            "vulnerabilities_en_GB.xml",
            "vulnerabilities_ar_SA.xml",
            "vulnerabilities_fil_PH.xml",
            "vulnerabilities_zh_CN.xml"
        };
        // When
        Pattern pattern = LangImporter.createIncludedFilesPattern();
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(true)));
        }
    }

    @Test
    void shouldNotMatchOtherFilesThanMessagesAndVulnerabilitiesFiles() {
        // Given
        String[] resourceFiles = {
            "Vulnerabilities.xml",
            "Vulnerabilities_en_GB.xml",
            "OtherFile_ar_SA.properties",
            "messages.properties"
        };
        // When
        Pattern pattern = LangImporter.createIncludedFilesPattern();
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(false)));
        }
    }
}
