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
package org.zaproxy.zap.utils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.zaproxy.zap.extension.api.ApiException;

import net.sf.json.JSONObject;

/**
 * Unit test for {@link ApiUtils}.
 */
public class ApiUtilsUnitTest {

    private static final String HOST = "example.com";

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenGettingIntFromNullParams() throws Exception {
        // Given
        JSONObject params = null;
        // When
        ApiUtils.getIntParam(params, "name");
        // Then = NullPointerException
    }

    @Test
    public void shouldThrowMissingParameterWhenGettingIntIfMissingParam() {
        // Given
        String name = "ParamNotInObject";
        JSONObject params = new JSONObject();
        try {
            // When
            ApiUtils.getIntParam(params, name);
        } catch (ApiException e) {
            // Then
            assertThat(e.getType(), is(equalTo(ApiException.Type.MISSING_PARAMETER)));
        }
    }

    @Test
    public void shouldThrowIllegalParameterWhenGettingIntIfParamNotInt() {
        // Given
        String name = "ParamNotInt";
        JSONObject params = new JSONObject();
        params.put(name, "String");
        try {
            // When
            ApiUtils.getIntParam(params, name);
        } catch (ApiException e) {
            // Then
            assertThat(e.getType(), is(equalTo(ApiException.Type.ILLEGAL_PARAMETER)));
        }
    }

    @Test
    public void shouldReturnIntValueWhenGettingInt() throws Exception {
        // Given
        String name = "ParamInt";
        int value = 0;
        JSONObject params = new JSONObject();
        params.put(name, value);
        // When
        int obtainedValue = ApiUtils.getIntParam(params, name);
        // Then
        assertThat(obtainedValue, is(equalTo(value)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenGettingAuthorityFromNullSite() {
        // Given
        String nullSite = null;
        // When
        ApiUtils.getAuthority(nullSite);
        // Then = NullPointerException
    }

    @Test
    public void shouldReturnEmptySiteWhenGettingAuthorityFromEmptySite() {
        // Given
        String emptySite = "";
        // When
        String authority = ApiUtils.getAuthority(emptySite);
        // Then
        assertThat(authority, is(equalTo(emptySite)));
    }

    @Test
    public void shouldNotRemovePortWhenGettingAuthorityFromSite() {
        // Given
        String siteWithPort = HOST + ":8080";
        // When
        String authority = ApiUtils.getAuthority(siteWithPort);
        // Then
        assertThat(authority, is(equalTo(siteWithPort)));
    }

    @Test
    public void shouldRemoveHttpSchemeAndAddDefaultPortWhenGettingAuthorityFromSite() {
        // Given
        String site = "http://" + HOST;
        // When
        String authority = ApiUtils.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST + ":80")));
    }

    @Test
    public void shouldRemoveSecureHttpSchemeAndKeepNonDefaultPortWhenGettingAuthorityFromSite() {
        // Given
        String site = "https://" + HOST + ":8443";
        // When
        String authority = ApiUtils.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST + ":8443")));
    }

    @Test
    public void shouldRemoveSecureHttpSchemeAndAddDefaultPortWhenGettingAuthorityFromSite() {
        // Given
        String site = "https://" + HOST;
        // When
        String authority = ApiUtils.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST + ":443")));
    }

    @Test
    public void shouldIgnoreEmptyPathComponentWhenGettingAuthorityFromSite() {
        // Given
        String site = HOST;
        // When
        String authority = ApiUtils.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST + ":80")));
    }

    @Test
    public void shouldRemoveNonEmptyPathComponentWhenGettingAuthorityFromSite() {
        // Given
        String site = HOST + "/path";
        // When
        String authority = ApiUtils.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST + ":80")));
    }
}
