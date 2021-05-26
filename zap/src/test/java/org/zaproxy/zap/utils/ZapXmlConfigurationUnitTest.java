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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ZapXmlConfiguration}. */
class ZapXmlConfigurationUnitTest {

    private static final String INDENTED_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                    + "<a>\n"
                    + "    <b>1</b>\n"
                    + "    <c/>\n"
                    + "    <d>\n"
                    + "        <e/>\n"
                    + "        <f>2</f>\n"
                    + "    </d>\n"
                    + "</a>\n";

    @Test
    void shouldSaveNewConfigurationIndented() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setRootElementName("a");
        conf.setProperty("b", "1");
        conf.setProperty("c", "");
        conf.setProperty("d.e", "");
        conf.setProperty("d.f", "2");
        // When
        conf.save(outputStream);
        // Then
        assertThat(contents(outputStream), is(equalTo(INDENTED_XML)));
    }

    @Test
    void shouldSaveLoadedConfigurationIndented() throws Exception {
        // Given
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(INDENTED_XML.getBytes(StandardCharsets.UTF_8));
        ZapXmlConfiguration conf = new ZapXmlConfiguration(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // When
        conf.save(outputStream);
        // Then
        assertThat(contents(outputStream), is(equalTo(INDENTED_XML)));
    }

    private static String contents(ByteArrayOutputStream outputStream)
            throws UnsupportedEncodingException {
        return outputStream
                .toString(StandardCharsets.UTF_8.name())
                .replaceAll(System.lineSeparator(), "\n");
    }
}
