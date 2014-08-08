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

import org.zaproxy.zap.extension.multiFuzz.Payload;
import org.zaproxy.zap.extension.multiFuzz.PayloadFactory;

public class HttpPayloadFactory implements PayloadFactory<HttpPayload> {

	@Override
	public HttpPayload createPayload(String data) {
		HttpPayload pay = new HttpPayload();
		pay.setData(data);
		pay.setType(Payload.Type.STRING);
		pay.setLength(-1);
		return pay;
	}

	@Override
	public HttpPayload createPayload(Payload.Type type, String data) {
		if (type.equals(Payload.Type.STRING)) {
			return createPayload(data);
		} else if (type.equals(Payload.Type.FILE)) {
			HttpPayload pay = new HttpPayload();
			pay.setType(Payload.Type.FILE);
			pay.setData(data);
			pay.setLength(-1);
			return pay;
		} else if (type.equals(Payload.Type.REGEX)) {
			HttpPayload pay = new HttpPayload();
			pay.setType(Payload.Type.REGEX);
			pay.setData(data);
			pay.setLength(-1);
			pay.setLimit(1000);
			return pay;
		} else if (type.equals(Payload.Type.SCRIPT)) {
			HttpPayload pay = new HttpPayload();
			pay.setType(Payload.Type.SCRIPT);
			pay.setData(data);
			pay.setLength(-1);
			return pay;
		}
		return null;
	}

	@Override
	public HttpPayload createPayload(Payload.Type type, String data, int limit) {
		if (type.equals(Payload.Type.STRING)) {
			return createPayload(data);
		} else if (type.equals(Payload.Type.FILE)) {
			HttpPayload pay = new HttpPayload();
			pay.setType(Payload.Type.FILE);
			pay.setData(data);
			pay.setLength(-1);
			return pay;
		} else if (type.equals(Payload.Type.REGEX)) {
			HttpPayload pay = new HttpPayload();
			pay.setType(Payload.Type.REGEX);
			pay.setData(data);
			pay.setLength(-1);
			pay.setLimit(limit);
			return pay;
		}
		return null;
	}

}
