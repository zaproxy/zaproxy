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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
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
    void shouldBeInstantiatedWithDelimeterParsingDisabledAndSetNullChar() {
        // Given
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        // When
        boolean isParsingDisabled = config.isDelimiterParsingDisabled();
        char delimChar = config.getListDelimiter();
        // Then
        assertThat(isParsingDisabled).isTrue();
        assertThat(delimChar).isEqualTo('\0');
    }

    @Test
    void shouldSetVariousPropertiesAsExpected()
            throws ConfigurationException, UnsupportedEncodingException {
        // Given
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        String aString = "Some string 🙂.";
        String bString = "";
        String cString = null;
        int aInt = 120;
        boolean aBoolean = true;
        // When
        config.setProperty("aString", aString);
        config.setProperty("bString", bString);
        config.setProperty("cString", cString);
        config.setProperty("aInt", aInt);
        config.setProperty("aBoolean", aBoolean);
        // Then
        List<Object> aStringList = config.getList("aString");
        assertThat(aStringList).hasSize(1);
        assertThat(aStringList.get(0)).isEqualTo(aString);

        List<Object> bStringList = config.getList("bString");
        assertThat(bStringList).hasSize(1);
        assertThat(bStringList.get(0)).isEqualTo(bString);

        List<Object> cStringList = config.getList("cString");
        assertThat(cStringList).hasSize(0);

        List<Object> aIntList = config.getList("aInt");
        int aActualInt = config.getInt("aInt");
        assertThat(aIntList).hasSize(1);
        // Int is string when handled via list
        assertThat(aIntList.get(0)).isEqualTo("120");
        assertThat(aActualInt).isEqualTo(120);

        List<Object> aBooleanList = config.getList("aBoolean");
        boolean aActualBoolean = config.getBoolean("aBoolean");
        assertThat(aBooleanList).hasSize(1);
        // Boolean is string when handled via list
        assertThat(aBooleanList.get(0)).isEqualTo("true");
        assertThat(aActualBoolean).isTrue();
    }

    @Test
    void shouldSetListOfPropertiesAsExpected() {
        // Given
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        String aString = "Some string 🙂.{2,2}";
        String bString = "Some other string";
        List<String> aList = List.of(aString, bString);
        // When
        config.setProperty("aList", aList);
        // Then
        List<Object> readList = config.getList("aList");
        assertThat(readList).hasSize(2);
        assertThat(readList.get(0)).isEqualTo(aString);
    }

    @Test
    void shouldSetListOfNonPrimitivesAsExpected() throws IOException, ConfigurationException {
        // Given
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        CustomObject coOne = new CustomObject("coOne");
        String stringTwo = "foo,bar";
        CustomObject coTwo = new CustomObject(stringTwo);
        List<CustomObject> customObjects = List.of(coOne, coTwo);
        // When
        config.setProperty("customObjects", customObjects);
        // Then
        List<Object> readList = config.getList("customObjects");
        assertThat(readList).hasSize(2);
        assertThat(((CustomObject) readList.get(1)).getValue()).isEqualTo(stringTwo);
    }

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
        assertThat(contents(outputStream)).isEqualTo(INDENTED_XML);
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
        assertThat(contents(outputStream)).isEqualTo(INDENTED_XML);
    }

    private static String contents(ByteArrayOutputStream outputStream)
            throws UnsupportedEncodingException {
        return outputStream
                .toString(StandardCharsets.UTF_8.name())
                .replaceAll(System.lineSeparator(), "\n");
    }

    private static class CustomObject {
        private final String value;

        CustomObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
