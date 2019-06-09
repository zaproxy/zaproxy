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
package org.parosproxy.paros.network;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.zaproxy.zap.network.HttpBodyTestUtils;

/** Unit test for {@link HtmlParameter}. */
public class HtmlParameterUnitTest extends HttpBodyTestUtils {

    private static final HtmlParameter.Type NON_NULL_TYPE = HtmlParameter.Type.url;
    private static final String NON_NULL_NAME = "name";
    private static final String NON_NULL_VALUE = "value";

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateHtmlParameterWithNullCookieLine() {
        // Given / When
        new HtmlParameter(null);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateHtmlParameterWithNullType() {
        // Given / When
        new HtmlParameter(null, NON_NULL_NAME, NON_NULL_VALUE);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateHtmlParameterWithNullName() {
        // Given / When
        new HtmlParameter(NON_NULL_TYPE, null, NON_NULL_VALUE);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateHtmlParameterWithNullValue() {
        // Given / When
        new HtmlParameter(NON_NULL_TYPE, NON_NULL_NAME, null);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToSetNullType() {
        // Given
        HtmlParameter parameter = new HtmlParameter(NON_NULL_TYPE, NON_NULL_NAME, NON_NULL_VALUE);
        // When
        parameter.setType(null);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToSetNullName() {
        // Given
        HtmlParameter parameter = new HtmlParameter(NON_NULL_TYPE, NON_NULL_NAME, NON_NULL_VALUE);
        // When
        parameter.setName(null);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToSetNullValue() {
        // Given
        HtmlParameter parameter = new HtmlParameter(NON_NULL_TYPE, NON_NULL_NAME, NON_NULL_VALUE);
        // When
        parameter.setValue(null);
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldCreateEmptyCookieWithEmptyCookieLine() {
        // Given
        String cookieLine = "";
        // When
        HtmlParameter parameter = new HtmlParameter(cookieLine);
        // Then
        assertThat(parameter.getType(), is(equalTo(HtmlParameter.Type.cookie)));
        assertThat(parameter.getName(), is(equalTo("")));
        assertThat(parameter.getValue(), is(equalTo("")));
    }

    @Test
    public void shouldCreateCookieWithEmptyNameIfCookieLineHasJustValue() {
        // Given
        String cookieLine = "value";
        // When
        HtmlParameter parameter = new HtmlParameter(cookieLine);
        // Then
        assertThat(parameter.getType(), is(equalTo(HtmlParameter.Type.cookie)));
        assertThat(parameter.getName(), is(equalTo("")));
        assertThat(parameter.getValue(), is(equalTo("value")));
    }

    @Test
    public void shouldCreateCookieWithEmptyValueIfCookieLineHasJustName() {
        // Given
        String cookieLine = "name=";
        // When
        HtmlParameter parameter = new HtmlParameter(cookieLine);
        // Then
        assertThat(parameter.getType(), is(equalTo(HtmlParameter.Type.cookie)));
        assertThat(parameter.getName(), is(equalTo("name")));
        assertThat(parameter.getValue(), is(equalTo("")));
    }

    @Test
    public void shouldCreateEmptyCookieIfIfCookieLineHasEmptyNameAndValue() {
        // Given
        String cookieLine = "=";
        // When
        HtmlParameter parameter = new HtmlParameter(cookieLine);
        // Then
        assertThat(parameter.getType(), is(equalTo(HtmlParameter.Type.cookie)));
        assertThat(parameter.getName(), is(equalTo("")));
        assertThat(parameter.getValue(), is(equalTo("")));
    }

    @Test
    public void shouldCreateCookieWithNameAndValueFromCookieLine() {
        // Given
        String cookieLine = "name=value";
        // When
        HtmlParameter parameter = new HtmlParameter(cookieLine);
        // Then
        assertThat(parameter.getType(), is(equalTo(HtmlParameter.Type.cookie)));
        assertThat(parameter.getName(), is(equalTo("name")));
        assertThat(parameter.getValue(), is(equalTo("value")));
    }

    @Test
    public void shouldCreateCookieWithFlagsFromAttributesInCookieLine() {
        // Given
        String cookieLine = "name=value; attribute1; attribute2=value2";
        // When
        HtmlParameter parameter = new HtmlParameter(cookieLine);
        // Then
        assertThat(parameter.getType(), is(equalTo(HtmlParameter.Type.cookie)));
        assertThat(parameter.getName(), is(equalTo("name")));
        assertThat(parameter.getValue(), is(equalTo("value")));
        assertThat(parameter.getFlags(), containsInAnyOrder("attribute1", "attribute2=value2"));
    }
}
