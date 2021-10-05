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
package org.zaproxy.zap.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.testutils.TestUtils;

/**
 * Validates that the translated vulnerabilities files have expected content (e.g. same number of
 * vulnerabilities as the source file).
 */
class ValidateTranslatedVulnerabilitiesFilesUnitTest extends TestUtils {

    private static final Path DIRECTORY =
            getResourcePath(
                    "/" + Constant.LANG_DIR, ValidateTranslatedVulnerabilitiesFilesUnitTest.class);
    private static final String FILE_NAME = "vulnerabilities";
    private static final String FILE_EXTENSION = ".xml";
    private static final String SOURCE_FILE = FILE_NAME + FILE_EXTENSION;

    private VulnerabilitiesLoader loader =
            new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, FILE_EXTENSION);

    @Test
    void shouldLoadAllVulnerabilitiesFilesAvailable() {
        // Given
        Map<String, Vulnerability> mainVulns =
                loadFile(getResourcePath("/org/zaproxy/zap/resources/" + SOURCE_FILE));
        List<String> translations = loader.getListOfVulnerabilitiesFiles();
        translations.remove(SOURCE_FILE);
        // When
        for (String file : translations) {
            Map<String, Vulnerability> vulns = loadFile(DIRECTORY.resolve(file));
            // Then
            assertThat(file, vulns.values(), hasSize(mainVulns.size()));
            mainVulns.forEach(
                    (k, v) -> {
                        Vulnerability vuln = vulns.get(k);
                        assertThat("Missing " + k + " in " + file, vuln, is(notNullValue()));
                        assertThat(
                                "Wrong number of references in " + file + " for " + k,
                                vuln.getReferences(),
                                hasSize(v.getReferences().size()));
                    });
        }
    }

    private Map<String, Vulnerability> loadFile(Path file) {
        List<Vulnerability> vulnerabilities = loader.loadVulnerabilitiesFile(file);
        assertThat("File " + file + " is not wellformed.", vulnerabilities, is(notNullValue()));

        Map<String, Vulnerability> map = new HashMap<>();
        for (Vulnerability vulnerability : vulnerabilities) {
            map.put(vulnerability.getId(), vulnerability);
        }
        return map;
    }
}
