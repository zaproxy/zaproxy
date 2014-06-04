package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.io.IOException;

import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.multiFuzz.FuzzMessageProcessor;;

public class AntiCSRFProcessor implements FuzzMessageProcessor<HttpMessage>{

	private HttpSender httpSender;
	private ExtensionAntiCSRF extAntiCSRF;
	private AntiCsrfToken acsrfToken;
	private Encoder encoder;
	public AntiCSRFProcessor(HttpSender send, ExtensionAntiCSRF extACSRF, AntiCsrfToken token){
		this.httpSender = send;
		this. extAntiCSRF = extACSRF;
		this.acsrfToken = token;
		this.encoder = new Encoder();
	}
	@Override
	public HttpMessage process(HttpMessage orig) {
		HttpMessage msg = orig.cloneRequest();
		String tokenValue = null;
		// This currently just supports a single token in one page
		// To support wizards etc need to loop back up the messages for previous tokens
		HttpMessage tokenMsg = this.acsrfToken.getMsg().cloneAll();
		try {
			httpSender.sendAndReceive(tokenMsg);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If we've got a token value here then the AntiCSRF extension must have been registered
		tokenValue = extAntiCSRF.getTokenValue(tokenMsg, acsrfToken.getName());

		if (tokenValue != null) {
            // Replace token value - only supported in the body right now
            String replaced = msg.getRequestBody().toString();
            replaced = replaced.replace(encoder.getURLEncode(acsrfToken.getValue()), encoder.getURLEncode(tokenValue));
            msg.setRequestBody(replaced);
        }
        // Correct the content length for the above changes
        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
		return msg;
	}
}
