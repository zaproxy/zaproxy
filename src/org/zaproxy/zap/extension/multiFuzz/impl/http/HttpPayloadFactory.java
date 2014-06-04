package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.zaproxy.zap.extension.multiFuzz.PayloadFactory;

public class HttpPayloadFactory implements PayloadFactory<HttpPayload> {

	@Override
	public boolean isSupported(String type) {
		return type.equals("String") || type.equals("FILE");
	}

	@Override
	public HttpPayload createPayload(String data) {
		HttpPayload pay = new HttpPayload();
		pay.setData(data);
		pay.setType("String");
		return pay;
	}

	@Override
	public HttpPayload createPayload(String type, String data) {
		if(type.equals("String")){
			return createPayload(data);
		}
		else if(type.equals("FILE")){
			HttpPayload pay = new HttpPayload();
			pay.setType("FILE");
			pay.setData(data);
			return pay;
		}
		return null;
	}

}
