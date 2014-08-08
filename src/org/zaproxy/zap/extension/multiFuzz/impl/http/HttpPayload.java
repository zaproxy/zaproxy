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

public class HttpPayload implements Payload {
	String data;
	Type type;
	int length = -1;
	int limit = 0;
	boolean recursive = false;

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		if (type.equals(Payload.Type.FILE) || type.equals(Payload.Type.REGEX) || length == -1) {
			return data;
		}
		StringBuilder build = new StringBuilder();
		while (build.length() < length) {
			build.append(data);
		}
		return build.substring(0, length);
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "#" + type + ": " + data;
	}

	@Override
	public int getLength() {
		return this.length;
	}

	@Override
	public void setLength(int len) {
		this.length = len;
	}

	@Override
	public boolean getRecursive() {
		return recursive;
	}

	@Override
	public void setRecursive(boolean rec) {
		this.recursive = rec;
	}

	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public void setLimit(int l) {
		this.limit = l;
	}
}
