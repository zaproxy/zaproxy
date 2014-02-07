/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP Development team
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.zaproxy.zap.extension.api.API;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
public class ExtensionGlobalExcludeURL extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionGlobalExcludeURL"; 
	public static final String TAG = "GlobalExcludeURL"; 
	
	private Map<String, GlobalExcludeURLToken> valueToToken = new HashMap<>();
	private Map<String, GlobalExcludeURLToken> urlToToken = new HashMap<>();
	
	private OptionsGlobalExcludeURLPanel optionsGlobalExcludeURLPanel = null;
	private PopupMenuGenerateForm popupMenuGenerateForm = null;
	
	private Encoder encoder = new Encoder();

	private static Logger log = Logger.getLogger(ExtensionGlobalExcludeURL.class);

	public ExtensionGlobalExcludeURL() {
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
	        extensionHook.getHookMenu().addPopupMenuItem(this.getPopupMenuGenerateForm());
	    }

	    GlobalExcludeURLAPI api = new GlobalExcludeURLAPI(this);
        api.addApiOptions(getParam());
        API.getInstance().registerApiImplementor(api);

	}
	
	private PopupMenuGenerateForm getPopupMenuGenerateForm() {
		if (popupMenuGenerateForm == null) {
			this.popupMenuGenerateForm = new PopupMenuGenerateForm(Constant.messages.getString("anticsrf.genForm.popup")); // FIXME lang todo
		}
		return popupMenuGenerateForm;
	}

	private OptionsGlobalExcludeURLPanel getOptionsAntiCsrfPanel() {
		if (optionsGlobalExcludeURLPanel == null) {
			optionsGlobalExcludeURLPanel = new OptionsGlobalExcludeURLPanel();
		}
		return optionsGlobalExcludeURLPanel;
	}
	
	protected GlobalExcludeURLParam getParam() {
        return Model.getSingleton().getOptionsParam().getGlobalExcludeURLParam();
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


	public void registerAntiCsrfToken(GlobalExcludeURLToken token) {
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
	
	public List<GlobalExcludeURLToken> getTokens(HttpMessage msg) {
		return this.getTokens(msg.getRequestBody().toString(), msg.getRequestHeader().getURI().toString());
	}
	
	private List<GlobalExcludeURLToken> getTokens(String reqBody, String targetUrl) {
		List<GlobalExcludeURLToken> tokens = new ArrayList<>();
		Set<String> values = valueToToken.keySet();
		for (String value : values) {
			if (reqBody.indexOf(value) >= 0) {
				GlobalExcludeURLToken token = valueToToken.get(value).clone();
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

	public List<GlobalExcludeURLToken> getTokensFromResponse(HttpMessage msg, Source source) {
		List<GlobalExcludeURLToken> list = new ArrayList<>();
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
						String attId = inputElement.getAttributeValue("ID");
						boolean found = false;
						if (attId != null) {
							for (String tokenName : this.getAntiCsrfTokenNames()) {
								if (tokenName.equalsIgnoreCase(attId)) {
									list.add(new GlobalExcludeURLToken(msg, attId, inputElement.getAttributeValue("VALUE"), formIndex));
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
										list.add(new GlobalExcludeURLToken(msg, name, inputElement.getAttributeValue("VALUE"), formIndex));
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

		valueToToken = new HashMap<>();
		urlToToken = new HashMap<>();
		// search for tokens...
        try {
			List<Integer> list = getModel().getDb().getTableHistory().getHistoryList(
					session.getSessionId(), HistoryReference.TYPE_PROXIED);
			list.addAll(getModel().getDb().getTableHistory().getHistoryList(
			        session.getSessionId(), HistoryReference.TYPE_ZAP_USER));
			HistoryFilter filter = new HistoryFilter();
			filter.setTags(Arrays.asList(new String[] {TAG}));
			GlobalExcludeURLDetectScanner scanner = new GlobalExcludeURLDetectScanner();
			
			for (Integer i : list) {
				HistoryReference hRef = new HistoryReference(i);
				if (filter.matches(hRef)) {
					HttpMessage msg = hRef.getHttpMessage();
					String response = msg.getResponseHeader().toString() + 
						msg.getResponseBody().toString();
					Source src = new Source(response);

					if (msg.isResponseFromTargetHost()) {
						scanner.scanHttpResponseReceive(msg, hRef.getHistoryId(), src);
					}
				}
			}
		} catch (SQLException | HttpMalformedHeaderException e) {
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
			sb.append("</form>\n");
			sb.append("<button onclick=\"document.getElementById('f1').submit()\">Submit</button>\n");
			sb.append("</body>\n");
			sb.append("</html>\n");

			return sb.toString();
		}

		return null;
	}
}
