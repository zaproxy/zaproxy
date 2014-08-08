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

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.multiFuzz.FuzzResultProcessor;

public class AntiCSRFResultProcessor implements
		FuzzResultProcessor<HttpFuzzResult> {

	private AntiCsrfToken acsrfToken;
	private boolean showTokenRequests;

	public AntiCSRFResultProcessor(AntiCsrfToken token, boolean show) {
		this.acsrfToken = token;
		this.showTokenRequests = show;
	}

	@Override
	public HttpFuzzResult process(HttpFuzzResult result) {
		if (showTokenRequests) {
			HttpMessage tokenMsg = this.acsrfToken.getMsg().cloneAll();
			List<HttpMessage> tokenRequests = new ArrayList<>();
			tokenRequests.add(tokenMsg);
			result.setTokenRequestMessages(tokenRequests);
		}
		return result;
	}

}
