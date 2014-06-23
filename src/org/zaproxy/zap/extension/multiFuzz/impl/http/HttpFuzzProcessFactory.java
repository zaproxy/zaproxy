/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.util.HashMap;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.multiFuzz.FuzzProcessFactory;

public class HttpFuzzProcessFactory implements
		FuzzProcessFactory<HttpFuzzProcess, HttpPayload, HttpFuzzLocation> {

	private HttpSender httpSender;
	private HttpMessage msg;

	public HttpFuzzProcessFactory(HttpMessage fuzzableMessage,
			boolean followRedirects) {

		msg = fuzzableMessage;
		httpSender = new HttpSender(Model.getSingleton().getOptionsParam()
				.getConnectionParam(), true, HttpSender.FUZZER_INITIATOR);
		httpSender.setFollowRedirect(followRedirects);
	}

	@Override
	public HttpFuzzProcess getFuzzProcess(
			HashMap<HttpFuzzLocation, HttpPayload> subs) {
		HttpFuzzProcess fuzzProcess = new HttpFuzzProcess(httpSender, msg);
		fuzzProcess.setPayload(subs);
		return fuzzProcess;
	}

	public HttpMessage getMessage() {
		return this.msg;
	}

	public HttpSender getSender() {
		return this.httpSender;
	}
}
