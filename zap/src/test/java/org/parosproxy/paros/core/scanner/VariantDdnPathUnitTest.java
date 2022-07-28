/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link VariantDdnPath}. */
class VariantDdnPathUnitTest {
    @BeforeEach
    void init() {
        Model model = mock(Model.class);
        Control.initSingletonForTesting(model);
        Model.setSingletonForTesting(model);
    }

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantDdnPath variantDdnPath = new VariantDdnPath();
        // When
        List<NameValuePair> parameters = variantDdnPath.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldFailToExtractParametersFromUndefinedMessage() {
        // Given
        VariantDdnPath variantDdnPath = new VariantDdnPath();
        HttpMessage undefinedMessage = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> variantDdnPath.setMessage(undefinedMessage));
    }

    private static Stream<Arguments> shouldExtractSingleDdnAsParam() {
        return Stream.of(
                Arguments.of("/«param»", "/example", 1),
                Arguments.of("/user/«param»", "/user/example", 2),
                Arguments.of("/user/«param»/account", "/user/example/account", 2));
    }

    @ParameterizedTest
    @MethodSource
    void shouldExtractSingleDdnAsParam(String ddnPath, String actualPath, int ddnPosition) {
        // Given
        VariantDdnPath variantDdnPath = new VariantDdnPath();
        // When
        addParamsFromPath(variantDdnPath, ddnPath, actualPath);
        // Then
        assertThat(variantDdnPath.getParamList().size(), is(1));
        NameValuePair param = variantDdnPath.getParamList().get(0);
        assertThat(param.getName(), is("«param»"));
        assertThat(param.getValue(), is("example"));
        assertThat(param.getPosition(), is(ddnPosition));
    }

    @Test
    void shouldExtractMultipleDdnsAsParams() {
        // Given
        VariantDdnPath variantDdnPath = new VariantDdnPath();
        String ddnPath = "/user/«param1»/account/«param2»/«param3»";
        String actualPath = "/user/value1/account/value2/value3";
        // When
        addParamsFromPath(variantDdnPath, ddnPath, actualPath);
        // Then
        assertThat(variantDdnPath.getParamList().size(), is(3));
        assertThat(variantDdnPath.getParamList().get(0).getName(), is("«param1»"));
        assertThat(variantDdnPath.getParamList().get(0).getValue(), is("value1"));
        assertThat(variantDdnPath.getParamList().get(1).getName(), is("«param2»"));
        assertThat(variantDdnPath.getParamList().get(1).getValue(), is("value2"));
        assertThat(variantDdnPath.getParamList().get(2).getName(), is("«param3»"));
        assertThat(variantDdnPath.getParamList().get(2).getValue(), is("value3"));
    }

    @Test
    void shouldReplaceDdnWithPayload() throws Exception {
        // Given
        VariantDdnPath variantDdnPath = new VariantDdnPath();
        HttpMessage msg = createMessageWithPath("/user/username");
        NameValuePair nameValuePair =
                new NameValuePair(NameValuePair.TYPE_URL_PATH, "«username»", "example", 2);
        // When
        variantDdnPath.setEscapedParameter(msg, nameValuePair, "", "../../etc/passwd");
        // Then
        assertThat(msg.getRequestHeader().getURI().getPath(), is("/user/../../etc/passwd"));
    }

    private static void addParamsFromPath(
            VariantDdnPath variantDdnPath, String ddnPath, String actualPath) {
        variantDdnPath.addParamsFromTreePath(Arrays.asList(ddnPath.split("/")), actualPath);
    }

    private static HttpMessage createMessageWithPath(String path) {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET http://example.com" + path + " HTTP/1.1\r\n");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}
