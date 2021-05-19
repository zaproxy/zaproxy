/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link VariantAbstractQuery}. */
class VariantAbstractQueryUnitTest {

    private static final int NAME_VALUE_PAIR_TYPE = -1;

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        // When
        List<NameValuePair> parameters = variantAbstractQuery.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldNotAllowToModifyReturnedParametersList() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        NameValuePair param = param("Name", "Value", 0);
        // When / Then
        assertThrows(
                UnsupportedOperationException.class,
                () -> variantAbstractQuery.getParamList().add(param));
    }

    @Test
    void shouldFailToProcessUndefinedParameters() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        List<org.zaproxy.zap.model.NameValuePair> undefinedParameters = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        variantAbstractQuery.setParameters(
                                NAME_VALUE_PAIR_TYPE, undefinedParameters));
    }

    @Test
    void shouldCreateOneDummyNameValuePairIfNoParametersProvidedAndAddQueryParamTrue() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        variantAbstractQuery.setAddQueryParam(true);
        List<org.zaproxy.zap.model.NameValuePair> noParameters = parameters();
        // When
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, noParameters);
        // Then
        assertThat(
                variantAbstractQuery.getParamList(),
                contains(param(NAME_VALUE_PAIR_TYPE, "query", "query", 0)));
    }

    @Test
    void shouldNotCreateOneDummyNameValuePairIfNoParametersProvidedAndAddQueryParamFalse() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        variantAbstractQuery.setAddQueryParam(false);
        List<org.zaproxy.zap.model.NameValuePair> noParameters = parameters();
        // When
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, noParameters);
        // Then
        assertThat(variantAbstractQuery.getParamList(), is(empty()));
    }

    @Test
    void shouldCreateNameValuePairsFromProvidedParameters() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter("a", "b"), parameter("c", "d"));
        // When
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        // Then
        assertThat(
                variantAbstractQuery.getParamList(),
                contains(
                        param(NAME_VALUE_PAIR_TYPE, "a", "b", 0),
                        param(NAME_VALUE_PAIR_TYPE, "c", "d", 1)));
    }

    @Test
    void shouldCreateNameValuePairsFromProvidedParametersWithNullNameOrValue() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter(null, "b"), parameter("c", null));
        // When
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        // Then
        assertThat(
                variantAbstractQuery.getParamList(),
                contains(
                        param(NAME_VALUE_PAIR_TYPE, "", "b", 0),
                        param(NAME_VALUE_PAIR_TYPE, "c", "", 1)));
    }

    @Test
    void shouldCreateNameValuePairsWithIndexedNamesFromProvidedArrayParameters() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(
                        parameter("a[]", "b"),
                        parameter("a[]", "d"),
                        parameter("e", "f"),
                        parameter("g[]", "h"),
                        parameter("i", "j"));
        // When
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        // Then
        assertThat(
                variantAbstractQuery.getParamList(),
                contains(
                        param(NAME_VALUE_PAIR_TYPE, "a[0]", "b", 0),
                        param(NAME_VALUE_PAIR_TYPE, "a[1]", "d", 1),
                        param(NAME_VALUE_PAIR_TYPE, "e", "f", 2),
                        param(NAME_VALUE_PAIR_TYPE, "g[0]", "h", 3),
                        param(NAME_VALUE_PAIR_TYPE, "i", "j", 4)));
    }

    @Test
    void shouldNotAccumulateProvidedParameters() {
        // Given
        VariantAbstractQuery variantAbstractQuery = new VariantAbstractQueryImpl();
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter("a", "b"), parameter("c", "d"));
        List<org.zaproxy.zap.model.NameValuePair> otherParameters =
                parameters(parameter("e", "f"), parameter("g", "h"));
        int otherType = -2;
        // When
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        variantAbstractQuery.setParameters(otherType, otherParameters);
        // Then
        assertThat(
                variantAbstractQuery.getParamList(),
                contains(param(otherType, "e", "f", 0), param(otherType, "g", "h", 1)));
    }

    @Test
    void shouldNotCallGetEscapedValueForInjectedValueIfEscapedWhenSettingParameter() {
        // Given
        List<String> values = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedValue(HttpMessage msg, String value) {
                        values.add(value);
                        return value;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter("a", "b"), parameter("c", "d"), parameter("e", "f"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setEscapedParameter(message, param("a", "b", 0), "y", "escaped");
        // Then
        assertThat(values, contains("d", "f"));
    }

    @Test
    void shouldCallGetEscapedValueForInjectedValueIfNotEscapedWhenSettingParameter() {
        // Given
        List<String> values = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedValue(HttpMessage msg, String value) {
                        values.add(value);
                        return value;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter("a", "b"), parameter("c", "d"), parameter("e", "f"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("a", "b", 0), "y", "not-escaped");
        // Then
        assertThat(values, contains("not-escaped", "d", "f"));
    }

    @Test
    void shouldCallGetEscapedNameForEachNameWhenSettingParameter() {
        // Given
        List<String> names = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedName(HttpMessage msg, String name) {
                        names.add(name);
                        return name;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter("a", "b"), parameter("c", "d"), parameter("e", "f"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("a", "b", 0), "y", "z");
        // Then
        assertThat(names, contains("y", "c", "e"));
    }

    @Test
    void shouldUseOriginalNamesForArraysWhenSettingParameter() {
        // Given
        List<String> names = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedName(HttpMessage msg, String name) {
                        names.add(name);
                        return name;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(
                        parameter("a[]", "b"),
                        parameter("a[]", "d"),
                        parameter("e", "f"),
                        parameter("g[]", "h"),
                        parameter("i", "j"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("e", "f", 2), "e", "f");
        // Then
        assertThat(names, contains("a[]", "a[]", "e", "g[]", "i"));
    }

    @Test
    void shouldUseInjectedNameWhenSettingArrayParameter() {
        // Given
        List<String> names = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedName(HttpMessage msg, String name) {
                        names.add(name);
                        return name;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters = parameters(parameter("a[]", "b"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("a[]", "b", 0), "y", "z");
        // Then
        assertThat(names, contains("y"));
    }

    @Test
    void shouldUseInjectedNameEvenIfEqualToIndexedNameWhenSettingArrayParameter() {
        // Given
        List<String> names = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedName(HttpMessage msg, String name) {
                        names.add(name);
                        return name;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters = parameters(parameter("a[]", "b"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("a[]", "b", 0), "a[0]", "z");
        // Then
        assertThat(names, contains("a[0]"));
    }

    @Test
    void shouldSetNameAndValueOfDummyParameter() {
        // Given
        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected String getEscapedName(HttpMessage msg, String name) {
                        names.add(name);
                        return name;
                    }

                    @Override
                    protected String getEscapedValue(HttpMessage msg, String value) {
                        values.add(value);
                        return value;
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> noParameters = parameters();
        variantAbstractQuery.setAddQueryParam(true);
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, noParameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("query", "query", 0), "y", "z");
        // Then
        assertThat(names, contains("y"));
        assertThat(values, contains("z"));
    }

    @Test
    void shouldCallBuildMessageWhenSettingParameter() {
        // Given
        MutableBoolean buildMessageCalled = new MutableBoolean();
        VariantAbstractQuery variantAbstractQuery =
                new VariantAbstractQueryImpl() {

                    @Override
                    protected void buildMessage(HttpMessage msg, String query) {
                        buildMessageCalled.setValue(true);
                    }
                };
        List<org.zaproxy.zap.model.NameValuePair> parameters =
                parameters(parameter("a", "b"), parameter("c", "d"));
        variantAbstractQuery.setParameters(NAME_VALUE_PAIR_TYPE, parameters);
        HttpMessage message = createMessage();
        // When
        variantAbstractQuery.setParameter(message, param("a", "b", 0), "y", "z");
        // Then
        assertTrue(buildMessageCalled.isTrue());
    }

    private static HttpMessage createMessage() {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static NameValuePair param(String name, String value, int position) {
        return param(NAME_VALUE_PAIR_TYPE, name, value, position);
    }

    private static NameValuePair param(int type, String name, String value, int position) {
        return new NameValuePair(type, name, value, position);
    }

    private static org.zaproxy.zap.model.NameValuePair parameter(String name, String value) {
        return new org.zaproxy.zap.model.NameValuePair() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }

    private static List<org.zaproxy.zap.model.NameValuePair> parameters(
            org.zaproxy.zap.model.NameValuePair... parameters) {
        if (parameters == null || parameters.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(parameters);
    }

    private static class VariantAbstractQueryImpl extends VariantAbstractQuery {

        @Override
        public void setMessage(HttpMessage msg) {
            // Nothing to do.
        }

        @Override
        protected void buildMessage(HttpMessage msg, String query) {
            // Nothing to do.
        }

        @Override
        protected String getEscapedValue(HttpMessage msg, String value) {
            return value;
        }

        @Override
        protected String getUnescapedValue(String value) {
            return value;
        }
    }
}
