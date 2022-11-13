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
package org.zaproxy.zap.extension.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.View;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.network.DomainMatcher;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link API}. */
@ExtendWith(MockitoExtension.class)
class APIUnitTest {

    private static final String CUSTOM_API_PATH = "/custom/api/";

    @Test
    void shouldBeEnabledWhenInDaemonMode() {
        // Given
        API api = new API();
        // When
        View.setDaemon(true);
        // Then
        assertThat(api.isEnabled(), is(equalTo(true)));
    }

    @Test
    void shouldAcceptCallbackIfNoAddressesSet() throws Exception {
        // Given
        API api = new API();
        api.setOptionsParamApi(createOptionsParamApi());
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {127, 0, 0, 1}, "example.com", requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldAcceptCallbackEvenIfAddressNotSet() throws Exception {
        // Given
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setPermittedAddresses(createPermittedAddresses("127.0.0.1"));
        api.setOptionsParamApi(apiOptions);
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {10, 0, 0, 2}, "example.com", requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldAcceptCallbackEvenIfHostnameNotSet() throws Exception {
        // Given
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setPermittedAddresses(createPermittedAddresses("127.0.0.1", "localhost"));
        api.setOptionsParamApi(apiOptions);
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {127, 0, 0, 1}, "example.com", requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldAcceptAddressAndHostnameSet() throws Exception {
        // Given
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setPermittedAddresses(createPermittedAddresses("10.0.0.8", "example.com"));
        api.setOptionsParamApi(apiOptions);
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {10, 0, 0, 8}, "example.com", requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldCreateBaseUrlWithHttpSchemeAndZapAddressIfProxyingAndNotSecure() {
        // Given
        boolean proxying = true;
        boolean secure = false;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        // When
        String baseUrl = api.getBaseURL(proxying);
        // Then
        assertThat(baseUrl, is(equalTo("http://zap/")));
    }

    @Test
    void shouldCreateBaseUrlWithHttpsSchemeAndZapAddressIfProxyingAndSecure() {
        // Given
        boolean proxying = true;
        boolean secure = true;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        // When
        String baseUrl = api.getBaseURL(proxying);
        // Then
        assertThat(baseUrl, is(equalTo("https://zap/")));
    }

    @Test
    void shouldCreateBaseUrlWithHttpsSchemeAndProxyAddressIfNotProxyingAndNotSecure() {
        // Given
        boolean proxying = false;
        boolean secure = false;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        api.setProxyParam(createProxyParam("127.0.0.1", 8080));
        // When
        String baseUrl = api.getBaseURL(proxying);
        // Then
        assertThat(baseUrl, is(equalTo("http://127.0.0.1:8080/")));
    }

    @Test
    void shouldCreateBaseUrlWithHttpsSchemeAndProxyAddressIfNotProxyingAndSecure() {
        // Given
        boolean proxying = false;
        boolean secure = true;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        api.setProxyParam(createProxyParam("127.0.0.1", 8080));
        // When
        String baseUrl = api.getBaseURL(proxying);
        // Then
        assertThat(baseUrl, is(equalTo("https://127.0.0.1:8080/")));
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpSchemeZapAddressAndApiNonceIfProxyingNotSecureAndNotView() {
        // Given
        boolean proxying = true;
        boolean secure = false;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.action, "test", proxying);
        // Then
        assertThat(baseUrl, startsWith("http://zap/JSON/test/action/test/?apinonce="));
        assertThat(baseUrl, endsWith("&"));
        assertApiNonceMatch(api, baseUrl);
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpsSchemeZapAddressAndApiNonceIfProxyingIsSecureAndNotView() {
        // Given
        boolean proxying = true;
        boolean secure = true;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.action, "test", proxying);
        // Then
        assertThat(baseUrl, startsWith("https://zap/JSON/test/action/test/?apinonce="));
        assertThat(baseUrl, endsWith("&"));
        assertApiNonceMatch(api, baseUrl);
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpSchemeProxyAddressAndApiNonceIfNotProxyingNotSecureAndNotView() {
        // Given
        boolean proxying = false;
        boolean secure = false;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        api.setProxyParam(createProxyParam("127.0.0.1", 8080));
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.action, "test", proxying);
        // Then
        assertThat(baseUrl, startsWith("http://127.0.0.1:8080/JSON/test/action/test/?apinonce="));
        assertThat(baseUrl, endsWith("&"));
        assertApiNonceMatch(api, baseUrl);
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpsSchemeProxyAddressAndApiNonceIfNotProxyingIsSecureAndNotView() {
        // Given
        boolean proxying = false;
        boolean secure = true;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        api.setProxyParam(createProxyParam("127.0.0.1", 8080));
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.action, "test", proxying);
        // Then
        assertThat(baseUrl, startsWith("https://127.0.0.1:8080/JSON/test/action/test/?apinonce="));
        assertThat(baseUrl, endsWith("&"));
        assertApiNonceMatch(api, baseUrl);
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpSchemeZapAddressAndNoApiNonceIfProxyingNotSecureAndView() {
        // Given
        boolean proxying = true;
        boolean secure = false;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.view, "test", proxying);
        // Then
        assertThat(baseUrl, is(equalTo("http://zap/JSON/test/view/test/")));
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpsSchemeZapAddressAndNoApiNonceIfProxyingIsSecureAndView() {
        // Given
        boolean proxying = true;
        boolean secure = true;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.view, "test", proxying);
        // Then
        assertThat(baseUrl, is(equalTo("https://zap/JSON/test/view/test/")));
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpSchemeProxyAddressAndNoApiNonceIfNotProxyingNotSecureAndView() {
        // Given
        boolean proxying = false;
        boolean secure = false;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        api.setProxyParam(createProxyParam("127.0.0.1", 8080));
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.view, "test", proxying);
        // Then
        assertThat(baseUrl, is(equalTo("http://127.0.0.1:8080/JSON/test/view/test/")));
    }

    @Test
    void
            shouldCreateBaseUrlWithApiRequestWithHttpsSchemeProxyAddressAndNoApiNonceIfNotProxyingIsSecureAndView() {
        // Given
        boolean proxying = false;
        boolean secure = true;
        API api = new API();
        OptionsParamApi apiOptions = createOptionsParamApi();
        apiOptions.setSecureOnly(secure);
        api.setOptionsParamApi(apiOptions);
        api.setProxyParam(createProxyParam("127.0.0.1", 8080));
        // When
        String baseUrl =
                api.getBaseURL(API.Format.JSON, "test", API.RequestType.view, "test", proxying);
        // Then
        assertThat(baseUrl, is(equalTo("https://127.0.0.1:8080/JSON/test/view/test/")));
    }

    @Test
    void shouldGenerateOneTimeApiNonce() throws Exception {
        // Given
        API api = new API();
        api.setOptionsParamApi(createOptionsParamApi());
        // When
        String nonce = api.getOneTimeNonce(CUSTOM_API_PATH);
        // Then
        assertThat(nonce, is(notNullValue()));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(true)));
        // Should not be valid more than once
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(false)));
    }

    @Test
    void shouldConsumeButNotAcceptOneTimeApiNonceIfPathMismatch() throws Exception {
        // Given
        API api = new API();
        api.setOptionsParamApi(createOptionsParamApi());
        // When
        String nonce = api.getOneTimeNonce(CUSTOM_API_PATH);
        // Then
        assertThat(nonce, is(notNullValue()));
        assertThat(isApiNonceValid(api, "/not/same/path/", nonce), is(equalTo(false)));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(false)));
    }

    @Test
    void shouldNotAcceptOneTimeApiNonceIfExpired() throws Exception {
        // Given
        int ttlInSecs = 1;
        OptionsParamApi optionsParamApi = Mockito.mock(OptionsParamApi.class);
        when(optionsParamApi.getNonceTimeToLiveInSecs()).thenReturn(ttlInSecs);
        API api = new API();
        api.setOptionsParamApi(optionsParamApi);
        String nonce = api.getOneTimeNonce(CUSTOM_API_PATH);
        // When
        Thread.sleep(TimeUnit.SECONDS.toMillis(ttlInSecs + 1));
        // Then
        assertThat(nonce, is(notNullValue()));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(false)));
    }

    @Test
    void shouldExpireExistingOneTimeApiNonces() throws Exception {
        // Given
        int ttlInSecs = 1;
        OptionsParamApi optionsParamApi = Mockito.mock(OptionsParamApi.class);
        when(optionsParamApi.getNonceTimeToLiveInSecs()).thenReturn(ttlInSecs);
        API api = new API();
        api.setOptionsParamApi(optionsParamApi);
        String nonce1 = api.getOneTimeNonce(CUSTOM_API_PATH + "1");
        String nonce2 = api.getOneTimeNonce(CUSTOM_API_PATH + "2");
        String nonce3 = api.getOneTimeNonce(CUSTOM_API_PATH + "2");
        // When
        Thread.sleep(TimeUnit.SECONDS.toMillis(ttlInSecs + 1));
        // Then
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH + "1", nonce1), is(equalTo(false)));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH + "2", nonce2), is(equalTo(false)));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH + "3", nonce3), is(equalTo(false)));
    }

    @Test
    void shouldGenerateLongLivedApiNonce() {
        // Given
        API api = new API();
        api.setOptionsParamApi(createOptionsParamApi());
        // When
        String nonce = api.getLongLivedNonce(CUSTOM_API_PATH);
        // Then
        assertThat(nonce, is(notNullValue()));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(true)));
        // Should be valid more than once
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(true)));
    }

    @Test
    void shouldNotExpireLongLivedApiNonce() throws Exception {
        // Given
        int ttlInSecs = 1;
        OptionsParamApi optionsParamApi = Mockito.mock(OptionsParamApi.class);
        when(optionsParamApi.getNonceTimeToLiveInSecs()).thenReturn(ttlInSecs);
        API api = new API();
        api.setOptionsParamApi(optionsParamApi);
        String nonce = api.getLongLivedNonce(CUSTOM_API_PATH);
        // When
        Thread.sleep(TimeUnit.SECONDS.toMillis(ttlInSecs + 1));
        // Then
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(true)));
    }

    @Test
    void shouldNotAcceptLongLivedApiNonceIfPathMismatch() throws Exception {
        // Given
        API api = new API();
        api.setOptionsParamApi(createOptionsParamApi());
        // When
        String nonce = api.getLongLivedNonce(CUSTOM_API_PATH);
        // Then
        assertThat(nonce, is(notNullValue()));
        assertThat(isApiNonceValid(api, "/not/same/path/", nonce), is(equalTo(false)));
        assertThat(isApiNonceValid(api, CUSTOM_API_PATH, nonce), is(equalTo(true)));
    }

    @Test
    void shouldNotHandleShortcutIfNotForcedRequest() throws Exception {
        // Given
        API api = createApiWithoutKey();
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String shortcut = "shortcut";
        apiImplementor.addApiShortcut(shortcut);
        api.registerApiImplementor(apiImplementor);
        String requestUri = "/" + shortcut;
        boolean forced = false;
        // When
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {127, 0, 0, 1}, "localhost", requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream(),
                        forced);
        // Then
        assertThat(requestHandled, is(nullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(false)));
    }

    @Test
    void shouldHandleShortcutIfForcedRequest() throws Exception {
        // Given
        API api = createApiWithoutKey();
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String shortcut = "shortcut";
        apiImplementor.addApiShortcut(shortcut);
        api.registerApiImplementor(apiImplementor);
        String requestUri = "/" + shortcut;
        boolean forced = true;
        // When
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {127, 0, 0, 1}, "localhost", requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream(),
                        forced);
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldHandleShortcutIfZapRequest() throws Exception {
        // Given
        API api = createApiWithoutKey();
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String shortcut = "shortcut";
        apiImplementor.addApiShortcut(shortcut);
        api.registerApiImplementor(apiImplementor);
        String requestUri = "/" + shortcut;
        // When
        HttpMessage requestHandled =
                api.handleApiRequest(
                        createApiRequest(new byte[] {127, 0, 0, 1}, API.API_DOMAIN, requestUri),
                        createMockedHttpInputStream(),
                        createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldHandleShortcutIfSecureZapRequest() throws Exception {
        // Given
        API api = createApiWithoutKey();
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String shortcut = "shortcut";
        apiImplementor.addApiShortcut(shortcut);
        api.registerApiImplementor(apiImplementor);
        String requestUri = "/" + shortcut;
        HttpRequestHeader apiRequest =
                createApiRequest(new byte[] {127, 0, 0, 1}, API.API_DOMAIN, requestUri);
        apiRequest.setSecure(true);
        // When
        HttpMessage requestHandled =
                api.handleApiRequest(
                        apiRequest, createMockedHttpInputStream(), createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(notNullValue()));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    @Test
    void shouldFailToGetXmlFromResponseWithNullEndpointName() throws ApiException {
        // Given
        String endpointName = null;
        ApiResponse response = ApiResponseTest.INSTANCE;
        // When
        ApiException e =
                assertThrows(ApiException.class, () -> API.responseToXml(endpointName, response));
        // Then
        assertThat(e.getType(), is(equalTo(ApiException.Type.INTERNAL_ERROR)));
    }

    @Test
    void shouldFailToGetXmlFromResponseWithEmptyEndpointName() throws ApiException {
        // Given
        String endpointName = "";
        ApiResponse response = ApiResponseTest.INSTANCE;
        // When
        ApiException e =
                assertThrows(ApiException.class, () -> API.responseToXml(endpointName, response));
        // Then
        assertThat(e.getType(), is(equalTo(ApiException.Type.INTERNAL_ERROR)));
    }

    @Test
    void shouldFailToGetXmlFromResponseWithInvalidXmlEndpointName() throws ApiException {
        // Given
        String endpointName = "<";
        ApiResponse response = ApiResponseTest.INSTANCE;
        // When
        ApiException e =
                assertThrows(ApiException.class, () -> API.responseToXml(endpointName, response));
        // Then
        assertThat(e.getType(), is(equalTo(ApiException.Type.INTERNAL_ERROR)));
    }

    @Test
    void shouldFailToGetXmlFromNullResponse() throws ApiException {
        // Given
        String endpointName = "Name";
        ApiResponse response = null;
        // When
        ApiException e =
                assertThrows(ApiException.class, () -> API.responseToXml(endpointName, response));
        // Then
        assertThat(e.getType(), is(equalTo(ApiException.Type.INTERNAL_ERROR)));
    }

    @Test
    void shouldGetXmlFromResponse() throws ApiException {
        // Given
        String endpointName = "Name";
        ApiResponse response = ApiResponseTest.INSTANCE;
        // When
        String xmlResponse = API.responseToXml(endpointName, response);
        // Then
        assertThat(
                xmlResponse,
                is(
                        equalTo(
                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Name>XML</Name>")));
    }

    @Test
    void shouldGetHtmlFromResponse() {
        // Given
        ApiResponse response = ApiResponseTest.INSTANCE;
        // When
        String htmlResponse = API.responseToHtml(response);
        // Then
        assertThat(htmlResponse, is(equalTo("<head>\n</head>\n<body>\nHTML</body>\n")));
    }

    private static class ApiResponseTest extends ApiResponse {

        private static final ApiResponseTest INSTANCE = new ApiResponseTest("");

        ApiResponseTest(String name) {
            super(name);
        }

        @Override
        public JSON toJSON() {
            return null;
        }

        @Override
        public void toXML(Document doc, Element rootElement) {
            rootElement.appendChild(doc.createTextNode("XML"));
        }

        @Override
        public void toHTML(StringBuilder sb) {
            sb.append("HTML");
        }

        @Override
        public String toString(int indent) {
            return null;
        }
    }

    private static void assertApiNonceMatch(API api, String baseUrl) {
        try {
            // Given
            URI uri = new URI(baseUrl, true);
            String hostHeader = uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
            TestApiImplementor apiImplementor = new TestApiImplementor();
            api.registerApiImplementor(apiImplementor);
            // When
            HttpMessage requestHandled =
                    api.handleApiRequest(
                            createApiRequest(new byte[] {127, 0, 0, 1}, hostHeader, baseUrl),
                            createMockedHttpInputStream(),
                            createMockedHttpOutputStream(),
                            true);
            // Then
            assertThat(requestHandled, is(notNullValue()));
            assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isApiNonceValid(API api, String requestPath, String nonce) {
        try {
            URI requestUri = Mockito.mock(URI.class);
            when(requestUri.getPath()).thenReturn(requestPath);
            HttpRequestHeader request =
                    Mockito.mock(
                            HttpRequestHeader.class, withSettings().strictness(Strictness.LENIENT));
            when(request.getURI()).thenReturn(requestUri);
            when(request.getHeader(HttpHeader.X_ZAP_API_NONCE)).thenReturn(nonce);
            when(request.getSenderAddress())
                    .thenReturn(Inet4Address.getByAddress(new byte[] {127, 0, 0, 1}));
            return api.hasValidKey(request, new JSONObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<DomainMatcher> createPermittedAddresses(String... addresses) {
        if (addresses == null || addresses.length == 0) {
            return new ArrayList<>();
        }

        List<DomainMatcher> permittedAddresses = new ArrayList<>();
        for (String address : addresses) {
            permittedAddresses.add(new DomainMatcher(address));
        }
        return permittedAddresses;
    }

    private static HttpRequestHeader createApiRequest(
            byte[] remoteAddress, String hostname, String requestUri) throws Exception {
        HttpRequestHeader httpRequestHeader =
                new HttpRequestHeader(
                        "GET " + requestUri + " HTTP/1.1\r\n" + "Host: " + hostname + "\r\n");
        httpRequestHeader.setSenderAddress(Inet4Address.getByAddress(remoteAddress));
        return httpRequestHeader;
    }

    private static OptionsParamApi createOptionsParamApi() {
        OptionsParamApi optionsParamApi = new OptionsParamApi();
        optionsParamApi.load(new ZapXmlConfiguration());
        return optionsParamApi;
    }

    @SuppressWarnings("deprecation")
    private static org.parosproxy.paros.core.proxy.ProxyParam createProxyParam(
            String proxyAddress, int proxyPort) {
        org.parosproxy.paros.core.proxy.ProxyParam proxyParam =
                new org.parosproxy.paros.core.proxy.ProxyParam();
        proxyParam.load(new ZapXmlConfiguration());
        proxyParam.setProxyIp(proxyAddress);
        proxyParam.setProxyPort(proxyPort);
        return proxyParam;
    }

    private static HttpInputStream createMockedHttpInputStream() {
        return Mockito.mock(HttpInputStream.class);
    }

    private static HttpOutputStream createMockedHttpOutputStream() {
        return Mockito.mock(HttpOutputStream.class);
    }

    private static API createApiWithoutKey() {
        OptionsParamApi options = createOptionsParamApi();
        options.setDisableKey(true);
        API api = new API();
        api.setOptionsParamApi(options);
        return api;
    }

    private static class TestApiImplementor extends ApiImplementor {

        static final String PREFIX = "test";

        private boolean used;

        @Override
        public String getPrefix() {
            return PREFIX;
        }

        @Override
        public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
            used = true;
            return new ApiResponseElement(name, "value");
        }

        @Override
        public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
            used = true;
            return new ApiResponseElement(name, "value");
        }

        @Override
        public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params)
                throws ApiException {
            used = true;
            return new HttpMessage();
        }

        @Override
        public String handleCallBack(HttpMessage msg) throws ApiException {
            used = true;
            return "response";
        }

        @Override
        public ApiResponse handleApiOptionView(String name, JSONObject params) throws ApiException {
            used = true;
            return new ApiResponseElement(name, "value");
        }

        @Override
        public ApiResponse handleApiOptionAction(String name, JSONObject params)
                throws ApiException {
            used = true;
            return new ApiResponseElement(name, "value");
        }

        @Override
        public HttpMessage handleShortcut(HttpMessage msg) throws ApiException {
            used = true;
            return new HttpMessage();
        }

        boolean wasUsed() {
            return used;
        }
    }
}
