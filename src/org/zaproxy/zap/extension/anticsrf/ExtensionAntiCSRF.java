/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP Development team
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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;

public class ExtensionAntiCSRF extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionAntiCSRF"; 
	public static final String TAG = "AntiCSRF"; 
	
	private Map<String, AntiCsrfToken> valueToToken = new HashMap<>();
	
	private OptionsAntiCsrfPanel optionsAntiCsrfPanel = null;
	private PopupMenuGenerateForm popupMenuGenerateForm = null;
	
	private Encoder encoder = new Encoder();

	private static Logger log = Logger.getLogger(ExtensionAntiCSRF.class);

	private AntiCsrfDetectScanner antiCsrfDetectScanner;

	private HistoryReferenceFactory historyReferenceFactory;

	public ExtensionAntiCSRF() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
        this.setOrder(50);
	}

    @Override
    public void init() {
        antiCsrfDetectScanner = new AntiCsrfDetectScanner(this);
    }

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

		final ExtensionHistory extensionHistory = (ExtensionHistory) Control.getSingleton()
				.getExtensionLoader()
				.getExtension(ExtensionHistory.NAME);
		if (extensionHistory != null) {
			historyReferenceFactory = new HistoryReferenceFactory() {

				@Override
				public HistoryReference createHistoryReference(int id) {
					return extensionHistory.getHistoryReference(id);
				}
			};
		} else {
			historyReferenceFactory = new HistoryReferenceFactory() {

				@Override
				public HistoryReference createHistoryReference(int id) throws HttpMalformedHeaderException, DatabaseException {
					return new HistoryReference(id);
				}
			};
		}
		AntiCsrfToken.setHistoryReferenceFactory(historyReferenceFactory);

	    extensionHook.addSessionListener(this);

	    if (getView() != null) {
	        extensionHook.getHookView().addOptionPanel(getOptionsAntiCsrfPanel());
	        extensionHook.getHookMenu().addPopupMenuItem(this.getPopupMenuGenerateForm());
	    }

        ExtensionPassiveScan extensionPassiveScan = (ExtensionPassiveScan) Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionPassiveScan.NAME);
        if (extensionPassiveScan != null) {
            extensionPassiveScan.addPassiveScanner(antiCsrfDetectScanner);
        }

	    AntiCsrfAPI api = new AntiCsrfAPI(this);
        api.addApiOptions(getParam());
        extensionHook.addApiImplementor(api);

	}
	
	@Override
	public void unload() {
		ExtensionPassiveScan extensionPassiveScan = (ExtensionPassiveScan) Control.getSingleton()
				.getExtensionLoader()
				.getExtension(ExtensionPassiveScan.NAME);
		if (extensionPassiveScan != null) {
			extensionPassiveScan.removePassiveScanner(antiCsrfDetectScanner);
		}

		super.unload();
	}

	private PopupMenuGenerateForm getPopupMenuGenerateForm() {
		if (popupMenuGenerateForm == null) {
			this.popupMenuGenerateForm = new PopupMenuGenerateForm(Constant.messages.getString("anticsrf.genForm.popup"));
		}
		return popupMenuGenerateForm;
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
		return this.getParam().getTokensNames();
	}
	
	public void addAntiCsrfTokenName(String token) {
		this.getParam().addToken(token);
	}

	public void removeAntiCsrfTokenName(String token) {
		this.getParam().removeToken(token);
	}


	public void registerAntiCsrfToken(AntiCsrfToken token) {
		log.debug("registerAntiCsrfToken " + token.getMsg().getRequestHeader().getURI().toString() + " " + token.getValue());
		synchronized (valueToToken) {
			try {
				HistoryReference hRef = token.getMsg().getHistoryRef();
				if (hRef == null) {
					hRef = new HistoryReference(getModel().getSession(), HistoryReference.TYPE_TEMPORARY, token.getMsg());
					token.getMsg().setHistoryRef(null);
				}

				token.setHistoryReferenceId(hRef.getHistoryId());
				valueToToken.put(encoder.getURLEncode(token.getValue()), token);
			} catch (HttpMalformedHeaderException | DatabaseException e) {
				log.error("Failed to persist the message: ", e);
			}
		}
	}

	public boolean requestHasToken(HttpMessage msg) {
		return this.requestHasToken(msg.getRequestBody().toString());
	}
	
	public boolean requestHasToken(String reqBody) {
		Set<String> values;
		synchronized (valueToToken) {
			values = Collections.unmodifiableSet(new HashSet<String>(valueToToken.keySet()));
		}
		for (String token : values) {
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
		List<AntiCsrfToken> tokens = new ArrayList<>();
		Set<String> values;
		synchronized (valueToToken) {
			values = Collections.unmodifiableSet(new HashSet<String>(valueToToken.keySet()));
		}
		
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

	public List<AntiCsrfToken> getTokensFromResponse(HttpMessage msg, Source source) {
		List<AntiCsrfToken> list = new ArrayList<>();
		List<Element> formElements = source.getAllElements(HTMLElementName.FORM);

		if (formElements != null && formElements.size() > 0) {
			// Loop through all of the FORM tags
			log.debug("Found " + formElements.size() + " forms");
			int formIndex = 0;
			
			for (Element formElement : formElements) {
				List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);
				
				if (inputElements != null && inputElements.size() > 0) {
					// Loop through all of the INPUT elements
					log.debug("Found " + inputElements.size() + " inputs");
					for (Element inputElement : inputElements) {
						String value = inputElement.getAttributeValue("VALUE");
						if (value == null) {
							continue;
						}

						String attId = inputElement.getAttributeValue("ID");
						boolean found = false;
						if (attId != null) {
							for (String tokenName : this.getAntiCsrfTokenNames()) {
								if (tokenName.equalsIgnoreCase(attId)) {
									list.add(new AntiCsrfToken(msg, attId, value, formIndex));
									found = true;
									break;
								}
							}
						}
						if (!found) {
							String name = inputElement.getAttributeValue("NAME");
							if (name != null) {
								for (String tokenName : this.getAntiCsrfTokenNames()) {
									if (tokenName.equalsIgnoreCase(name)) {
										list.add(new AntiCsrfToken(msg, name, value, formIndex));
										break;
									}
								}
							}
						}
					}
				}
				formIndex++;
			}
		}
		return list;
	}

	@Override
	public void sessionChanged(Session session) {
		if (session == null) {
			// Closedown
			return;
		}

		synchronized (valueToToken) {
			valueToToken.clear();
		}
		// search for tokens...
        try {
			List<Integer> list = getModel().getDb().getTableHistory().getHistoryIdsOfHistType(
					session.getSessionId(), HistoryReference.TYPE_PROXIED, HistoryReference.TYPE_ZAP_USER);
			HistoryFilter filter = new HistoryFilter();
			filter.setTags(Arrays.asList(new String[] {TAG}));
			
			AntiCsrfDetectScanner antiCsrfDetectScanner = new AntiCsrfDetectScanner(this);
			for (Integer i : list) {
				HistoryReference hRef = historyReferenceFactory.createHistoryReference(i.intValue());
				if (filter.matches(hRef)) {
					HttpMessage msg = hRef.getHttpMessage();
					String response = msg.getResponseHeader().toString() + 
						msg.getResponseBody().toString();
					Source src = new Source(response);

					if (msg.isResponseFromTargetHost()) {
					    antiCsrfDetectScanner.scanHttpResponseReceive(msg, hRef.getHistoryId(), src);
					}
				}
			}
		} catch (DatabaseException | HttpMalformedHeaderException e) {
			log.error(e.getMessage(), e);
		}

	}

	public boolean isAntiCsrfToken(String name) {
		if (name == null) {
			return false;
		}
		return this.getParam().getTokensNames().contains(name.toLowerCase());
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

	public String generateForm(int hrefId) throws Exception {
		ExtensionHistory extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
		if (extHist != null) {
			HistoryReference hr = extHist.getHistoryReference(hrefId);
			if (hr == null) {
				return null;
			}
			HttpMessage msg = hr.getHttpMessage();
			StringBuilder sb = new StringBuilder(300);
			sb.append("<html>\n");
			sb.append("<body>\n");
			sb.append("<h3>");
			sb.append(msg.getRequestHeader().getURI());
			sb.append("</h3>");
			sb.append("<form id=\"f1\" method=\"POST\" action=\"" + hr.getURI() + "\">\n");
			sb.append("<table>\n");
			
			TreeSet<HtmlParameter> params = msg.getFormParams();
			// Let the message be GC'ed as it's no longer needed.
			msg = null;
			Iterator<HtmlParameter> iter = params.iterator();
			while (iter.hasNext()) {
				HtmlParameter htmlParam = iter.next();
				String name = URLDecoder.decode(htmlParam.getName(), "UTF-8");
				String value = URLDecoder.decode(htmlParam.getValue(), "UTF-8");
				sb.append("<tr><td>\n");
				sb.append(name);
				sb.append("<td>");
				sb.append("<input name=\"");
				sb.append(name);
				sb.append("\" value=\"");
				sb.append(value);
				sb.append("\" size=\"100\">");
				sb.append("</tr>\n");
			}

			sb.append("</table>\n");
			sb.append("<input id=\"submit\" type=\"submit\" value=\"Submit\"/>\n");
			sb.append("</form>\n");
			sb.append("</body>\n");
			sb.append("</html>\n");

			return sb.toString();
		}

		return null;
	}

	static interface HistoryReferenceFactory {

		HistoryReference createHistoryReference(int id) throws DatabaseException, HttpMalformedHeaderException;

	}
}
