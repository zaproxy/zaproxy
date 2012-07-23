/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.junit;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;

/**
 * This test loads the image http://code.google.com/p/zaproxy/logo
 * to assert a working proxy.
 */
public class ProxyThreadTest extends BaseZapProxyTest {
	
	@Test
	public void receiveImage() throws Exception {
		URL url = new URL("http://code.google.com/p/zaproxy/logo");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		assertEquals("image/png", connection.getContentType());
		
		connection.disconnect();
	}

	@Test
	public void receiveSecureImage() throws Exception {
		URL url = new URL("https://code.google.com/p/zaproxy/logo");
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		
		assertEquals("image/png", connection.getContentType());
		
		connection.disconnect();
	}
}
