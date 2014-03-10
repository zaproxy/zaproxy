/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.reveal;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.ZapToggleButton;

public class ExtensionReveal extends ExtensionAdaptor implements ProxyListener {

	private static final Logger logger = Logger.getLogger(ExtensionReveal.class);
	
	public static final String NAME = "ExtensionReveal"; 
	public static final int PROXY_LISTENER_ORDER = 10;

	private static final String ATT_DISABLED 	= "DISABLED";
	private static final String ATT_READONLY 	= "READONLY";
	private static final String ATT_TYPE 		= "TYPE";
	private static final String TYPE_HIDDEN 	= "HIDDEN";
	
	private boolean reveal = false;
	private ZapToggleButton revealButton = null;

	public ExtensionReveal() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
        this.setOrder(18);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    extensionHook.addProxyListener(this);
	    if (getView() != null) {
			View.getSingleton().addMainToolbarButton(getRevealButton());
			View.getSingleton().addMainToolbarSeparator();
	    }
	}
	
	private void setReveal(boolean reveal) {
		this.reveal = reveal; 
	    revealButton.setSelected(reveal);
	}

	private JToggleButton getRevealButton() {
		if (revealButton == null) {
			revealButton = new ZapToggleButton();
			revealButton.setIcon(new ImageIcon(ExtensionReveal.class.getResource("/resource/icon/16/044.png"))); // 'light off' icon
			revealButton.setToolTipText(Constant.messages.getString("reveal.button.enable"));
			revealButton.setSelectedIcon(new ImageIcon(ExtensionReveal.class.getResource("/resource/icon/16/043.png"))); // 'light on' icon
			revealButton.setSelectedToolTipText(Constant.messages.getString("reveal.button.disable"));
		    setReveal(Model.getSingleton().getOptionsParam().getViewParam().getReveal());
			
			revealButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
				    setReveal(getRevealButton().isSelected());
				    Model.getSingleton().getOptionsParam().getViewParam().setReveal(reveal);
					try {
						Model.getSingleton().getOptionsParam().getViewParam().getConfig().save();
					} catch (ConfigurationException e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			});
		}
		return revealButton;
	}

	@Override
	public int getArrangeableListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}
	
	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		return true;
	}
	
	@Override
	public boolean onHttpResponseReceive(HttpMessage msg) {
		if (reveal) {
			boolean changed = false;
			String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
			Source src = new Source(response);
			OutputDocument outputDocument = new OutputDocument(src);
			
			List<Element> formElements = src.getAllElements(HTMLElementName.FORM);
			
			if (formElements != null && formElements.size() > 0) {
				// Loop through all of the FORM tags
				logger.debug("Found " + formElements.size() + " forms");
				
				for (Element formElement : formElements) {
					List<Element> elements = formElement.getAllElements();
					
					if (elements != null && elements.size() > 0) {
						// Loop through all of the elements
						logger.debug("Found " + elements.size() + " inputs");
						for (Element element : elements) {
							Attributes atts = element.getAttributes();
							
							if(atts != null && atts.size() > 0) {
								Iterator<Attribute> iter = atts.iterator();
								while (iter.hasNext()) {
									Attribute att = iter.next();
									if (ATT_DISABLED.equalsIgnoreCase(att.getName()) ||
										ATT_READONLY.equalsIgnoreCase(att.getName()) ||
										(ATT_TYPE.equalsIgnoreCase(att.getName()) && 
												TYPE_HIDDEN.equalsIgnoreCase(att.getValue()))) {
										logger.debug("Removing " + att.getName() + ": " + response.substring(att.getBegin(), att.getEnd()));
										outputDocument.remove(att);
										changed = true;
									}
								}
							}
						}
					}
				}
			}
			if (changed) {
				response = outputDocument.toString();
				
				int i = response.indexOf(HttpHeader.CRLF + HttpHeader.CRLF);
				msg.setResponseBody(response.substring(i + (HttpHeader.CRLF + HttpHeader.CRLF).length()));
			}
		}
		return true;
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("reveal.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
