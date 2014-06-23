package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.zaproxy.zap.extension.multiFuzz.Payload;

public class HttpPayload implements Payload {
	String data;
	String type;
	int length = -1;
	int limit = 0;
	boolean recursive = false;

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		if (type.equals("FILE") || type.equals("REGEX") || length == -1) {
			return data;
		}
		StringBuilder build = new StringBuilder();
		while (build.length() < length) {
			build.append(data);
		}
		return build.substring(0, length);
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
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
