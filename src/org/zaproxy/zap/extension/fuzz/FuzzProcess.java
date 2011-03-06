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
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;

public class FuzzProcess  implements Runnable {

	private HttpSender httpSender;
	private HttpMessage msg;
	private boolean fuzzHeader;
	private int startOffset;
	private int endOffset;
	private String fuzz;
	private List<FuzzerListener> listenerList = new ArrayList<FuzzerListener>();
    private static Log log = LogFactory.getLog(FuzzProcess.class);

	public FuzzProcess(HttpSender httpSender, HttpMessage msg, boolean fuzzHeader, int startOffset,
			int endOffset, String fuzz) {
		this.httpSender = httpSender;
		this.msg = msg.cloneAll();
		this.fuzzHeader = fuzzHeader;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.fuzz = fuzz;
	}

	@Override
	public void run() {
		for (FuzzerListener listener : listenerList) {
			listener.notifyFuzzProcessStarted(this);
		}
		// Inject the payload
		try {
			String orig;
			if (fuzzHeader) {
				orig = msg.getRequestHeader().toString();
			} else {
				orig = msg.getRequestBody().toString();
			}
			String changed = orig.substring(0, startOffset) + fuzz + orig.substring(endOffset);
			if (fuzzHeader) {
				msg.setRequestHeader(changed);
			} else {
				msg.setRequestBody(changed);
				msg.getRequestHeader().setContentLength(changed.length());
			}
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

}
