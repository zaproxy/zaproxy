package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.zaproxy.zap.extension.multiFuzz.PayloadFactory;

public class HttpPayloadFactory implements PayloadFactory<HttpPayload> {

	@Override
	public boolean isSupported(String type) {
		return type.equals("String") || type.equals("FILE")
				|| type.equals("REGEX") || type.equals("SCRIPT");
	}

	@Override
	public HttpPayload createPayload(String data) {
		HttpPayload pay = new HttpPayload();
		pay.setData(data);
		pay.setType("String");
		pay.setLength(-1);
		return pay;
	}

	@Override
	public HttpPayload createPayload(String type, String data) {
		if (type.equals("String")) {
			return createPayload(data);
		} else if (type.equals("FILE")) {
			HttpPayload pay = new HttpPayload();
			pay.setType("FILE");
			pay.setData(data);
			pay.setLength(-1);
			return pay;
		} else if (type.equals("REGEX")) {
			HttpPayload pay = new HttpPayload();
			pay.setType("REGEX");
			pay.setData(data);
			pay.setLength(-1);
			pay.setLimit(1000);
			return pay;
		} else if (type.equals("SCRIPT")) {
			HttpPayload pay = new HttpPayload();
			pay.setType("SCRIPT");
			pay.setData(data);
			pay.setLength(-1);
			return pay;
		}
		return null;
	}

	@Override
	public HttpPayload createPayload(String type, String data, int limit) {
		if (type.equals("String")) {
			return createPayload(data);
		} else if (type.equals("FILE")) {
			HttpPayload pay = new HttpPayload();
			pay.setType("FILE");
			pay.setData(data);
			pay.setLength(-1);
			return pay;
		} else if (type.equals("REGEX")) {
			HttpPayload pay = new HttpPayload();
			pay.setType("REGEX");
			pay.setData(data);
			pay.setLength(-1);
			pay.setLimit(limit);
			return pay;
		}
		return null;
	}

}
