/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Vector;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Unit test for {@link VariantCookie}.
 */
public class VariantCookieUnitTest {

    @Test
    public void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        // When
        List<NameValuePair> parameters = variantCookie.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowToModifyReturnedParametersList() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        // When
        variantCookie.getParamList().add(cookie("Name", "Value", 0));
        // Then = UnsupportedOperationException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToExtractParametersFromUndefinedMessage() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage undefinedMessage = null;
        // When
        variantCookie.setMessage(undefinedMessage);
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldNotExtractAnyParameterIfThereAreNoCookieHeaders() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithoutCookies();
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList(), is(empty()));
    }

    @Test
    public void shouldNotExtractAnyParameterIfTheCookieHeadersDontHaveCoookies() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("", "");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList(), is(empty()));
    }

    @Test
    public void shouldExtractParametersFromWellformedCookieHeader() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("a=b; c=\"d\"; e=f");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(3)));
        assertThat(variantCookie.getParamList(), contains(cookie("a", "b", 0), cookie("c", "\"d\"", 1), cookie("e", "f", 2)));
    }

    @Test
    public void shouldExtractParametersFromWellformedCookieHeaders() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("a=b; c=d; e=f", "g=h; i=j; k=l");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(6)));
        assertThat(variantCookie.getParamList(), contains(
                cookie("a", "b", 0),
                cookie("c", "d", 1),
                cookie("e", "f", 2),
                cookie("g", "h", 3),
                cookie("i", "j", 4),
                cookie("k", "l", 5)));
    }

    @Test
    public void shouldExtractParametersFromMalformedCookieHeader() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("a=; =d;e;g=\"h; i=j\"");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(5)));
        assertThat(variantCookie.getParamList(), contains(
                cookie("a", "", 0),
                cookie("", "d", 1),
                cookie("e", null, 2),
                cookie("g", "\"h", 3),
                cookie("i", "j\"", 4)));
    }

    @Test
    public void shouldExtractParametersFromMalformedCookieHeaders() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("a=;=d; e", "g; =j;l=", "n=\"", "=\"");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(8)));
        assertThat(
                variantCookie.getParamList(),
                contains(
                        cookie("a", "", 0),
                        cookie("", "d", 1),
                        cookie("e", null, 2),
                        cookie("g", null, 3),
                        cookie("", "j", 4),
                        cookie("l", "", 5),
                        cookie("n", "\"", 6),
                        cookie("", "\"", 7)));
    }

    @Test
    public void shouldDecodeValueFromExtractedParameters() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("a=b; c=d; e=%26%27%28%29%2A", "=%27");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(4)));
        assertThat(
                variantCookie.getParamList(),
                contains(cookie("a", "b", 0), cookie("c", "d", 1), cookie("e", "&'()*", 2), cookie("", "'", 3)));
    }

    @Test
    public void shouldNotDecodeNameFromExtractedParameters() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("%29=b; c=d; e=f", "%26");
        // When
        variantCookie.setMessage(messageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(4)));
        assertThat(
                variantCookie.getParamList(),
                contains(cookie("%29", "b", 0), cookie("c", "d", 1), cookie("e", "f", 2), cookie("%26", null, 3)));
    }

    @Test
    public void shouldNotAccumulateExtractedParameters() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage messageWithCookies = createMessageWithCookies("a=b; c=d; e=f");
        HttpMessage otherMessageWithCookies = createMessageWithCookies("g=h; i=j; k=l");
        // When
        variantCookie.setMessage(messageWithCookies);
        variantCookie.setMessage(otherMessageWithCookies);
        // Then
        assertThat(variantCookie.getParamList().size(), is(equalTo(3)));
        assertThat(variantCookie.getParamList(), contains(cookie("g", "h", 0), cookie("i", "j", 1), cookie("k", "l", 2)));
    }

    @Test
    public void shouldInjectCookieModificationOnWellformedHeader() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("a", "b", 0), "y", "z");
        // Then
        assertThat(injectedCookie, is(equalTo("y=z")));
        assertThat(message, containsCookieHeader("y=z; c=d; e=f"));
    }

    @Test
    public void shouldInjectCookieModificationOnMalformedHeader() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a; =b; =d; e=;");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("b", null, 1), "y", "z");
        // Then
        assertThat(injectedCookie, is(equalTo("y=z")));
        assertThat(message, containsCookieHeader("a; y=z; =d; e="));
    }

    @Test
    public void shouldInjectUnescapedCookieValueModification() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("a", "b", 0), "y", "&'()");
        // Then
        assertThat(injectedCookie, is(equalTo("y=%26%27%28%29")));
        assertThat(message, containsCookieHeader("y=%26%27%28%29; c=d; e=f"));
    }

    @Test
    public void shouldInjectEscapedCookieValueModification() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setEscapedParameter(message, cookie("a", "b", 0), "y", "%26%27%28%29");
        // Then
        assertThat(injectedCookie, is(equalTo("y=%26%27%28%29")));
        assertThat(message, containsCookieHeader("y=%26%27%28%29; c=d; e=f"));
    }

    @Test
    public void shouldIgnorePreviouslyInjectCookieModifications() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String previouslyInjectedCookie = variantCookie.setParameter(message, cookie("a", "b", 0), "y", "z");
        String injectedCookie = variantCookie.setParameter(message, cookie("e", "f", 2), "i", "j");
        // Then
        assertThat(previouslyInjectedCookie, is(equalTo("y=z")));
        assertThat(injectedCookie, is(equalTo("i=j")));
        assertThat(message, containsCookieHeader("a=b; c=d; i=j"));
    }

    @Test
    public void shouldMergeCookieHeadersWhenInjectingCookieModifications() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b", "c=d", "e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("c", "d", 1), "y", "z");
        // Then
        assertThat(injectedCookie, is(equalTo("y=z")));
        assertThat(message, containsCookieHeader("a=b; y=z; e=f"));
    }

    @Test
    public void shouldIgnoreNameOfCookieAndUsePositionWhenInjectingCookieModifications() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("DifferentName", "d", 1), "y", "z");
        // Then
        assertThat(injectedCookie, is(equalTo("y=z")));
        assertThat(message, containsCookieHeader("a=b; y=z"));
    }

    @Test
    public void shouldIgnoreValueOfOriginalCookieAndUsePositionWhenInjectingCookieModifications() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("c", "DifferentValue", 1), "y", "z");
        // Then
        assertThat(injectedCookie, is(equalTo("y=z")));
        assertThat(message, containsCookieHeader("a=b; y=z"));
    }

    @Test
    public void shouldNotInjectCookieModificationsIfPositionOfCookieDoesNotExist() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d");
        variantCookie.setMessage(message);
        // When
        variantCookie.setParameter(message, cookie("c", "d", 3), "y", "z");
        // Then
        assertThat(message, containsCookieHeader("a=b; c=d"));
    }

    @Test
    public void shouldRemoveCookieNameIfNameNotInjected() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("e", "f", 2), null, "z");
        // Then
        assertThat(injectedCookie, is(equalTo("=z")));
        assertThat(message, containsCookieHeader("a=b; c=d; =z"));
    }

    @Test
    public void shouldRemoveCookieValueIfValueNotInjected() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("c", "d", 1), "c", null);
        // Then
        assertThat(injectedCookie, is(equalTo("c")));
        assertThat(message, containsCookieHeader("a=b; c; e=f"));
    }

    @Test
    public void shouldRemoveCookieIfNameAndValueAreNotInjected() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b; c=d; e=f");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("c", "d", 1), null, null);
        // Then
        assertThat(injectedCookie, is(nullValue()));
        assertThat(message, containsCookieHeader("a=b; e=f"));
    }

    @Test
    public void shouldRemoveCookieHeaderIfOnlyCookieIsRemoved() {
        // Given
        VariantCookie variantCookie = new VariantCookie();
        HttpMessage message = createMessageWithCookies("a=b");
        variantCookie.setMessage(message);
        // When
        String injectedCookie = variantCookie.setParameter(message, cookie("a", "b", 0), null, null);
        // Then
        assertThat(injectedCookie, is(nullValue()));
        assertThat(message, hasNoCookieHeaders());
    }

    private static HttpMessage createMessageWithoutCookies() {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static HttpMessage createMessageWithCookies(String... cookieHeaders) {
        HttpMessage message = new HttpMessage();
        try {
            StringBuilder requestHeaderBuilder = new StringBuilder("GET / HTTP/1.1\r\nHost: example.com\r\n");
            for (String cookieHeader : cookieHeaders) {
                requestHeaderBuilder.append("Cookie: ");
                requestHeaderBuilder.append(cookieHeader);
                requestHeaderBuilder.append("\r\n");
            }
            message.setRequestHeader(requestHeaderBuilder.toString());
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static NameValuePair cookie(String name, String value, int position) {
        return new NameValuePair(NameValuePair.TYPE_COOKIE, name, value, position);
    }

    private static Matcher<HttpMessage> containsCookieHeader(final String cookies) {
        return new BaseMatcher<HttpMessage>() {

            @Override
            public boolean matches(Object actualValue) {
                HttpMessage message = (HttpMessage) actualValue;
                Vector<String> cookieLines = message.getRequestHeader().getHeaders(HttpHeader.COOKIE);
                if (cookieLines == null || cookieLines.size() != 1) {
                    return false;
                }
                return cookies.equals(cookieLines.get(0));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cookie header ").appendValue(cookies);
            }

            public void describeMismatch(Object item, Description description) {
                HttpMessage message = (HttpMessage) item;
                Vector<String> cookieLines = message.getRequestHeader().getHeaders(HttpHeader.COOKIE);
                if (cookieLines == null) {
                    description.appendText("has no cookie headers");
                } else if (cookieLines.size() == 1) {
                    description.appendText("was ").appendValue(cookieLines.get(0));
                } else {
                    description.appendText("has multiple cookie headers ").appendValue(cookieLines);
                }
            }
        };
    }

    private static Matcher<HttpMessage> hasNoCookieHeaders() {
        return new BaseMatcher<HttpMessage>() {

            @Override
            public boolean matches(Object actualValue) {
                HttpMessage message = (HttpMessage) actualValue;
                Vector<String> cookieLines = message.getRequestHeader().getHeaders(HttpHeader.COOKIE);
                if (cookieLines == null || cookieLines.isEmpty()) {
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("no cookie header");
            }

            public void describeMismatch(Object item, Description description) {
                HttpMessage message = (HttpMessage) item;
                Vector<String> cookieLines = message.getRequestHeader().getHeaders(HttpHeader.COOKIE);
                if (cookieLines.size() == 1) {
                    description.appendText("has one cookie header ").appendValue(cookieLines.get(0));
                } else {
                    description.appendText("has multiple cookie headers ").appendValue(cookieLines);
                }
            }
        };
    }
}
