/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.multiFuzz.FuzzMessagePreProcessor;

public class AntiCSRFProcessor implements FuzzMessagePreProcessor<HttpMessage, HttpFuzzLocation, HttpPayload> {

	private static final Logger logger = Logger
			.getLogger(AntiCSRFProcessor.class);
	private HttpSender httpSender;
	private ExtensionAntiCSRF extAntiCSRF;
	private AntiCsrfToken acsrfToken;
	private Encoder encoder;

	public AntiCSRFProcessor(HttpSender send, ExtensionAntiCSRF extACSRF,
			AntiCsrfToken token) {
		this.httpSender = send;
		this.extAntiCSRF = extACSRF;
		this.acsrfToken = token;
		this.encoder = new Encoder();
	}

	@Override
	public HttpMessage process(HttpMessage orig, Map<HttpFuzzLocation, HttpPayload> payMap) {
		HttpMessage msg = orig.cloneRequest();
		String tokenValue = null;
		// This currently just supports a single token in one page
		// To support wizards etc need to loop back up the messages for previous
		// tokens
		HttpMessage tokenMsg = this.acsrfToken.getMsg().cloneAll();
		try {
			httpSender.sendAndReceive(tokenMsg);
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}

		// If we've got a token value here then the AntiCSRF extension must have
		// been registered
		tokenValue = extAntiCSRF.getTokenValue(tokenMsg, acsrfToken.getName());

		if (tokenValue != null) {
			// Replace token value - only supported in the body right now
			String replaced = msg.getRequestBody().toString();
			replaced = replaced.replace(
					encoder.getURLEncode(acsrfToken.getValue()),
					encoder.getURLEncode(tokenValue));
			msg.setRequestBody(replaced);
		}
		// Correct the content length for the above changes
		msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
		return msg;
	}

}
