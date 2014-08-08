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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.multiFuzz.FuzzLocation;
import org.zaproxy.zap.extension.multiFuzz.TextFuzzLocation;

public class HttpFuzzLocation implements TextFuzzLocation<HttpMessage> {

	public final int start;
	public final int end;

	public HttpFuzzLocation(int s, int e) {
		start = s;
		end = e;
	}

	@Override
	public int begin() {
		return start;
	}

	@Override
	public int end() {
		return end;
	}

	@Override
	public int compareTo(FuzzLocation<HttpMessage> loc2) {
		if (loc2 instanceof TextFuzzLocation<?>) {
			return start - ((TextFuzzLocation<HttpMessage>) loc2).begin();
		}
		return 0;
	}

	@Override
	public String getRepresentation(HttpMessage msg) {
		int headLen = msg.getRequestHeader().toString().length();
		String rep = "";
		if (start > headLen) {
			rep = msg.getRequestBody().toString()
					.substring(start - headLen - 1, end - headLen - 1);
		} else {
			rep = msg.getRequestHeader().toString().substring(start, end);
		}
		if(rep.equals("")){
			rep = ((start > headLen) ? (Constant.messages.getString("fuzz.http.comp.body") + (start - headLen - 1)) : (Constant.messages.getString("fuzz.http.comp.head") + start));
		}
		return rep;
	}

	@Override
	public boolean overlap(FuzzLocation<HttpMessage> loc) {
		if (loc instanceof TextFuzzLocation<?>) {
			if (begin() <= ((TextFuzzLocation<HttpMessage>) loc).begin()) {
				return end() > ((TextFuzzLocation<HttpMessage>) loc).begin();
			}
			return begin() < ((TextFuzzLocation<HttpMessage>) loc).end();
		}
		return true;
	}

}
