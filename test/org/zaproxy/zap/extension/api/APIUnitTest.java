/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.network.DomainMatcher;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

import net.sf.json.JSONObject;

/**
 * Unit test for {@link API}.
 */
@RunWith(MockitoJUnitRunner.class)
public class APIUnitTest {

    @BeforeClass
    public static void setUp() throws Exception {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    @Test
    public void shouldBeEnabledWhenInDaemonMode() {
        // Given
        API api = new API();
        // When
        View.setDaemon(true);
        // Then
        assertThat(api.isEnabled(), is(equalTo(true)));
    }

    @Test
    public void shouldDenyAllAddressesIfNoneSet() throws Exception {
        // Given
        API api = new API();
        api.setOptionsParamApi(new OptionsParamApi());
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        // When
        boolean requestHandled = api.handleApiRequest(
                createApiRequest(new byte[] { 127, 0, 0, 1 }, "example.com", requestUri),
                createMockedHttpInputStream(),
                createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(equalTo(true)));
        assertThat(apiImplementor.wasUsed(), is(equalTo(false)));
    }

    @Test
    public void shouldDenyAddressNotSet() throws Exception {
        // Given
        API api = new API();
        OptionsParamApi apiOptions = new OptionsParamApi();
        apiOptions.load(new ZapXmlConfiguration());
        apiOptions.setPermittedAddresses(createPermittedAddresses("127.0.0.1"));
        api.setOptionsParamApi(apiOptions);
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        // When
        boolean requestHandled = api.handleApiRequest(
                createApiRequest(new byte[] { 10, 0, 0, 2 }, "example.com", requestUri),
                createMockedHttpInputStream(),
                createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(equalTo(true)));
        assertThat(apiImplementor.wasUsed(), is(equalTo(false)));
    }

    @Test
    public void shouldDenyHostnameNotSet() throws Exception {
        // Given
        API api = new API();
        OptionsParamApi apiOptions = new OptionsParamApi();
        apiOptions.load(new ZapXmlConfiguration());
        apiOptions.setPermittedAddresses(createPermittedAddresses("127.0.0.1", "localhost"));
        api.setOptionsParamApi(apiOptions);
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        // When
        boolean requestHandled = api.handleApiRequest(
                createApiRequest(new byte[] { 127, 0, 0, 1 }, "example.com", requestUri),
                createMockedHttpInputStream(),
                createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(equalTo(true)));
        assertThat(apiImplementor.wasUsed(), is(equalTo(false)));
    }

    @Test
    public void shouldAcceptAddressAndHostnameSet() throws Exception {
        // Given
        API api = new API();
        OptionsParamApi apiOptions = new OptionsParamApi();
        apiOptions.load(new ZapXmlConfiguration());
        apiOptions.setPermittedAddresses(createPermittedAddresses("10.0.0.8", "example.com"));
        api.setOptionsParamApi(apiOptions);
        TestApiImplementor apiImplementor = new TestApiImplementor();
        String requestUri = api.getCallBackUrl(apiImplementor, "http://example.com");
        // When
        boolean requestHandled = api.handleApiRequest(
                createApiRequest(new byte[] { 10, 0, 0, 8 }, "example.com", requestUri),
                createMockedHttpInputStream(),
                createMockedHttpOutputStream());
        // Then
        assertThat(requestHandled, is(equalTo(true)));
        assertThat(apiImplementor.wasUsed(), is(equalTo(true)));
    }

    private List<DomainMatcher> createPermittedAddresses(String... addresses) {
        if (addresses == null || addresses.length == 0) {
            return new ArrayList<>();
        }

        List<DomainMatcher> permittedAddresses = new ArrayList<>();
        for (String address : addresses) {
            permittedAddresses.add(new DomainMatcher(address));
        }
        return permittedAddresses;
    }

    private HttpRequestHeader createApiRequest(byte[] remoteAddress, String hostname, String requestUri) throws Exception {
        HttpRequestHeader httpRequestHeader = new HttpRequestHeader(
                "GET " + requestUri + " HTTP/1.1\r\n" + "Host: " + hostname + "\r\n");
        httpRequestHeader.setSenderAddress(Inet4Address.getByAddress(remoteAddress));
        return httpRequestHeader;
    }

    private HttpInputStream createMockedHttpInputStream() {
        return Mockito.mock(HttpInputStream.class);
    }

    private HttpOutputStream createMockedHttpOutputStream() {
        return Mockito.mock(HttpOutputStream.class);
    }

    private static class TestApiImplementor extends ApiImplementor {

        public static final String PREFIX = "test";

        private boolean used;

        @Override
        public String getPrefix() {
            return PREFIX;
        }

        @Override
        public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
            used = true;
            return new ApiResponseElement(name);
        }

        @Override
        public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
            used = true;
            return new ApiResponseElement(name);
        }

        @Override
        public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params) throws ApiException {
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
            return new ApiResponseElement(name);
        }

        @Override
        public ApiResponse handleApiOptionAction(String name, JSONObject params) throws ApiException {
            used = true;
            return new ApiResponseElement(name);
        }

        @Override
        public HttpMessage handleShortcut(HttpMessage msg) throws ApiException {
            used = true;
            return new HttpMessage();
        }

        public boolean wasUsed() {
            return used;
        }
    }

}
