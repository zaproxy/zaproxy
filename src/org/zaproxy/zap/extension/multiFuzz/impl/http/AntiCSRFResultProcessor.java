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
