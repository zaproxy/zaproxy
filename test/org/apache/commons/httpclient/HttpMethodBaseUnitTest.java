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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.httpclient;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class HttpMethodBaseUnitTest {

	@Test
	public void testParseCookieHeaderEmpty() {
		List<Cookie> cookies = HttpMethodBase.parseCookieHeader("example.com", "");
		assertThat(cookies.size(), is(0));
	}

	@Test
	public void testParseCookieHeaderWithOneCookie() {
		List<Cookie> cookies = HttpMethodBase.parseCookieHeader("example.com",
		        "JSESSIONID=5DFA94B903A0063839E0440118808875");
		assertThat(cookies.size(), is(1));
	}

	@Test
	public void testParseCookieHeaderWithTwoCookie() {
		List<Cookie> cookies = HttpMethodBase.parseCookieHeader("example.com",
		        "has_js=1;JSESSIONID=5DFA94B903A0063839E0440118808875");
		assertThat(cookies.size(), is(2));
		cookies = HttpMethodBase.parseCookieHeader("example.com",
		        "has_js=1; JSESSIONID=5DFA94B903A0063839E0440118808875");
		assertThat(cookies.size(), is(2));
		// empty value
		cookies = HttpMethodBase.parseCookieHeader("example.com",
		        "has_js=;JSESSIONID=5DFA94B903A0063839E0440118808875");
		assertThat(cookies.size(), is(2));
	}

}
