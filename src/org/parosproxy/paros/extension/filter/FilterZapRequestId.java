/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 mawoki@ymail.com
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
package org.parosproxy.paros.extension.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 * Adding a request ID to each HTTP header.
 * See <a href="https://github.com/zaproxy/zaproxy/issues/68">Issue 68</a>
 * 
 * @author MaWoKi
 */
public class FilterZapRequestId extends FilterAdaptor {

	private static final AtomicLong requestCounter = new AtomicLong(1);
	private static final Logger log = Logger.getLogger(FilterZapRequestId.class);
	
	private Model model = null;
	
	@Override
	public void init(Model model) {
		super.init(model);
		this.model = model;
	}

	@Override
	public int getId() {
		return getName().hashCode();
	}

	@Override
	public String getName() {
		return Constant.messages.getString("filter.request.header.send.id");
	}

	@Override
	public void onHttpRequestSend(HttpMessage httpMessage) {
		HttpRequestHeader rhead = httpMessage.getRequestHeader();
		if (rhead.getHeader(HttpHeader.X_ZAP_REQUESTID) == null) {
			String sessname = "zap-session";
			try {
				sessname = URLEncoder.encode(model.getSession().getSessionName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("UTF8 is not supported?! Using fallback session name.", e);
			}
			rhead.addHeader(HttpHeader.X_ZAP_REQUESTID, sessname + "-" + Long.toString(requestCounter.getAndIncrement()));
		}
	}

	@Override
	public void onHttpResponseReceive(HttpMessage httpMessage) {
		// nothing to do
	}
	
}
