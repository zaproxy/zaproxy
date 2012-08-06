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
package org.zaproxy.zap.extension.anticsrf;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

public class ExtensionAntiCSRF extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionAntiCSRF"; 
	public static final String TAG = "AntiCSRF"; 
	
	private Map<String, AntiCsrfToken> valueToToken = new HashMap<String, AntiCsrfToken>();
	private Map<String, AntiCsrfToken> urlToToken = new HashMap<String, AntiCsrfToken>();
	
	private OptionsAntiCsrfPanel optionsAntiCsrfPanel = null;
	
	private Encoder encoder = new Encoder();

	private static Logger log = Logger.getLogger(ExtensionAntiCSRF.class);

	public ExtensionAntiCSRF() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
        this.setOrder(50);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    extensionHook.addSessionListener(this);

	    if (getView() != null) {
	        extensionHook.getHookView().addOptionPanel(getOptionsAntiCsrfPanel());
	    }

	}

	private OptionsAntiCsrfPanel getOptionsAntiCsrfPanel() {
		if (optionsAntiCsrfPanel == null) {
			optionsAntiCsrfPanel = new OptionsAntiCsrfPanel();
		}
		return optionsAntiCsrfPanel;
	}
	
	protected AntiCsrfParam getParam() {
        return Model.getSingleton().getOptionsParam().getAntiCsrfParam();
	}
	
	public List<String> getAntiCsrfTokenNames() {
		return this.getParam().getTokens();
	}
	
	public void addAntiCsrfTokenName(String token) {
		this.getParam().addToken(token);
	}

	public void removeAntiCsrfTokenName(String token) {
		this.getParam().removeToken(token);
	}


	public void registerAntiCsrfToken(AntiCsrfToken token) {
		log.debug("registerAntiCsrfToken " + token.getMsg().getRequestHeader().getURI().toString() + " " + token.getValue());
		valueToToken.put(encoder.getURLEncode(token.getValue()), token);
		urlToToken.put(token.getMsg().getRequestHeader().getURI().toString(), token);
	}

	public boolean requestHasToken(HttpMessage msg) {
		return this.requestHasToken(msg.getRequestBody().toString());
	}
	
	public boolean requestHasToken(String reqBody) {
		Set<String> tokens = valueToToken.keySet();
		for (String token : tokens) {
			if (reqBody.indexOf(token) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	public List<AntiCsrfToken> getTokens(HttpMessage msg) {
		return this.getTokens(msg.getRequestBody().toString(), msg.getRequestHeader().getURI().toString());
	}
	
	private List<AntiCsrfToken> getTokens(String reqBody, String targetUrl) {
		List<AntiCsrfToken> tokens = new ArrayList<AntiCsrfToken>();
		Set<String> values = valueToToken.keySet();
		for (String value : values) {
			if (reqBody.indexOf(value) >= 0) {
				AntiCsrfToken token = valueToToken.get(value).clone();
				token.setTargetURL(targetUrl);
				tokens.add(token);
			}
		}
		
		return tokens;
	}
	
	public String getTokenValue(HttpMessage tokenMsg, String tokenName) {
		String response = tokenMsg.getResponseHeader().toString() + tokenMsg.getResponseBody().toString();
		Source source = new Source(response);
		List<Element> formElements = source.getAllElements(HTMLElementName.FORM);
		
		if (formElements != null && formElements.size() > 0) {
			// Loop through all of the FORM tags
			
			for (Element formElement : formElements) {
				List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);
				
				if (inputElements != null && inputElements.size() > 0) {
					// Loop through all of the INPUT elements
					for (Element inputElement : inputElements) {
						String id = inputElement.getAttributeValue("ID");
						if (id != null && id.equalsIgnoreCase(tokenName)) {
							return inputElement.getAttributeValue("VALUE");
						}
						String name = inputElement.getAttributeValue("NAME");
						if (name != null && name.equalsIgnoreCase(tokenName)) {
							return inputElement.getAttributeValue("VALUE");
						}
					}
				}
			}
		}
		return null;
	}


	@Override
	public void sessionChanged(Session session) {
		if (session == null) {
			// Closedown
			return;
		}

		valueToToken = new HashMap<String, AntiCsrfToken>();
		urlToToken = new HashMap<String, AntiCsrfToken>();
		// search for tokens...
        try {
			List<Integer> list = getModel().getDb().getTableHistory().getHistoryList(
					session.getSessionId(), HistoryReference.TYPE_MANUAL);
			HistoryFilter filter = new HistoryFilter();
			filter.setTags(new String[] {TAG});	
			AntiCsrfDetectScanner scanner = new AntiCsrfDetectScanner();
			
			for (Integer i : list) {
				HistoryReference hRef = new HistoryReference(i);
				if (filter.matches(hRef)) {
					String response = hRef.getHttpMessage().getResponseHeader().toString() + 
						hRef.getHttpMessage().getResponseBody().toString();
					Source src = new Source(response);

					scanner.scanHttpResponseReceive(hRef.getHttpMessage(), hRef.getHistoryId(), src);
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} catch (HttpMalformedHeaderException e) {
			log.error(e.getMessage(), e);
		}

	}

	public boolean isAntiCsrfToken(String name) {
		if (name == null) {
			return false;
		}
		return this.getParam().getTokens().contains(name.toLowerCase());
	}
	
	@Override
	public void sessionAboutToChange(Session session) {
	}

	@Override
	public void sessionScopeChanged(Session session) {
	}
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("anticsrf.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}
}
