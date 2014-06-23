package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.multiFuzz.FuzzGap;

public class HttpFuzzGap extends
		FuzzGap<HttpMessage, HttpFuzzLocation, HttpPayload> {

	HttpFuzzGap(HttpMessage msg, HttpFuzzLocation loc) {
		super(msg, loc);
	}

}
