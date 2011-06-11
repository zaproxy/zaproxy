/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.fuzz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;

public class FuzzProcess  implements Runnable {

	private ConnectionParam connectionParam;
	private HttpMessage msg;
	private boolean fuzzHeader;
	private int startOffset;
	private int endOffset;
	private String fuzz;
	private AntiCsrfToken acsrfToken;
	private List<FuzzerListener> listenerList = new ArrayList<FuzzerListener>();
    private static Log log = LogFactory.getLog(FuzzProcess.class);
    private List<HttpMessage> tokenRequests = new ArrayList<HttpMessage>();
	private Encoder encoder = new Encoder();
	private boolean showTokenRequests = false;
	private ExtensionAntiCSRF extAntiCSRF = null; 

	public FuzzProcess(ConnectionParam connectionParam, HttpMessage msg,
			boolean fuzzHeader, int startOffset, int endOffset,
			String fuzz, AntiCsrfToken acsrfToken, boolean showTokenRequests) {
		this.connectionParam = connectionParam;
		this.msg = msg.cloneAll();
		this.fuzzHeader = fuzzHeader;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.fuzz = fuzz;
		this.acsrfToken = acsrfToken;
		this.showTokenRequests = showTokenRequests;
		this.extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

	}

	@Override
	public void run() {
		String tokenValue = null;
		for (FuzzerListener listener : listenerList) {
			listener.notifyFuzzProcessStarted(this);
		}
		
		if (this.acsrfToken != null) {
			// This currently just supports a single token in one page
			// To support wizards etc need to loop back up the messages for previous tokens
			try {
				HttpMessage tokenMsg = this.acsrfToken.getMsg().cloneAll();
				HttpSender httpSender = new HttpSender(connectionParam, true);

				httpSender.sendAndReceive(tokenMsg);

				// If we've got a token value here then the AntiCSRF extension must have been registered
				tokenValue = extAntiCSRF.getTokenValue(tokenMsg, acsrfToken.getName());

				this.tokenRequests.add(tokenMsg);
				
			} catch (HttpException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		// Inject the payload
		try {
			String orig;
			if (fuzzHeader) {
				orig = msg.getRequestHeader().toString();
			} else {
				orig = msg.getRequestBody().toString();
			}
			String changed = orig.substring(0, startOffset) + encoder.getURLEncode(fuzz) + orig.substring(endOffset);
			if (fuzzHeader) {
				msg.setRequestHeader(changed);
			} else {
				msg.setRequestBody(changed);
			}
			if (tokenValue != null) {
				// Replace token value - only supported in the body right now
				String replaced = msg.getRequestBody().toString();
				replaced = replaced.replace(encoder.getURLEncode(acsrfToken.getValue()), encoder.getURLEncode(tokenValue));
				msg.setRequestBody(replaced);
			}
			// Correct the content length for the above changes
	        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
			
			HttpSender httpSender = new HttpSender(connectionParam, true);
			httpSender.sendAndReceive(msg);
			
		} catch (HttpException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		for (FuzzerListener listener : listenerList) {
			listener.notifyFuzzProcessComplete(this);
		}
	}

	public void addFuzzerListener(FuzzerListener listener) {
		listenerList.add(listener);		
	}

	public void removeFuzzerListener(FuzzerListener listener) {
		listenerList.remove(listener);
	}
	
	public HttpMessage getHttpMessage() {
		return this.msg;
	}

	public List<HttpMessage> getTokenRequests() {
		return tokenRequests;
	}

	public boolean isShowTokenRequests() {
		return showTokenRequests;
	}

	public void setShowTokenRequests(boolean showTokenRequests) {
		this.showTokenRequests = showTokenRequests;
	}

}
