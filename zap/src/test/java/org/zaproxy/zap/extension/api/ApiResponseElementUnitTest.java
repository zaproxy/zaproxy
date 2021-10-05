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
package org.zaproxy.zap.extension.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Unit test for {@link ApiResponseElement}. */
class ApiResponseElementUnitTest {

    @Test
    void shouldReturnNullJsonObjectWithANullValue() throws ApiException {
        // Given
        String name = "response";
        String value = null;
        ApiResponseElement apiResponse = new ApiResponseElement(name, value);
        // When
        String jsonResponse = apiResponse.toJSON().toString();
        // Then
        assertThat(jsonResponse, is(equalTo("{\"" + name + "\":" + value + "}")));
    }

    @Test
    void shouldReturnStringJsonObjectWithNonNullValue() throws ApiException {
        // Given
        String name = "response";
        String value = "value";
        ApiResponseElement apiResponse = new ApiResponseElement(name, value);
        // When
        String jsonResponse = apiResponse.toJSON().toString();
        // Then
        assertThat(jsonResponse, is(equalTo("{\"" + name + "\":\"" + value + "\"}")));
    }

    @Test
    void shouldReturnEmptyXmlElementWithANullValue() throws ApiException {
        // Given
        String name = null;
        String value = null;
        ApiResponseElement apiResponse = new ApiResponseElement(name, value);
        Document document = createDocument();
        Element element = createElement(document, "response");
        // When
        apiResponse.toXML(document, element);
        // Then
        assertThat(element.getChildNodes().getLength(), is(equalTo(1)));
        assertThat(element.getFirstChild().getNodeValue(), is(equalTo("")));
    }

    @Test
    void shouldReturnXmlElementWithTextNodeForNonNullValue() throws ApiException {
        // Given
        String name = null;
        String value = "value";
        ApiResponseElement apiResponse = new ApiResponseElement(name, value);
        Document document = createDocument();
        Element element = createElement(document, "response");
        // When
        apiResponse.toXML(document, element);
        // Then
        assertThat(element.getChildNodes().getLength(), is(equalTo(1)));
        assertThat(element.getFirstChild().getNodeValue(), is(equalTo(value)));
    }

    @Test
    void shouldReturnCorrectJsonObjectWithJsonStringValues() throws ApiException {
        // Given
        String name = "name";
        String value = "{\"key\":\"value\"}";
        ApiResponseElement apiResponse = new ApiResponseElement(name, value);
        // When
        String jsonResponse = apiResponse.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"name\":\"{\\\"key\\\":\\\"value\\\"}\"}");
    }

    @Test
    void shouldReturnCorrectJsonObjectWithMapJsonStringValues() throws ApiException {
        // Given
        String name = "name";
        String value = "{\"key\":\"value\"}";
        Map<String, String> map = new HashMap<>();
        map.put(name, value);
        ApiResponseSet<String> apiRespSet = new ApiResponseSet<>("test", map);
        ApiResponseElement apiResponse = new ApiResponseElement(apiRespSet);
        // When
        String jsonResponse = apiResponse.toJSON().toString();
        // Then
        assertEquals(jsonResponse, "{\"test\":{\"name\":\"{\\\"key\\\":\\\"value\\\"}\"}}");
    }

    private static Document createDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element createElement(Document document, String elementName) {
        Element element = document.createElement(elementName);
        document.appendChild(element);
        return element;
    }
}
