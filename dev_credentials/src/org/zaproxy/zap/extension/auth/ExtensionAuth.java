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
package org.zaproxy.zap.extension.auth;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenu;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuItemFactory;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuSiteNodeFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.view.ContextPanelFactory;

public class ExtensionAuth extends ExtensionAdaptor implements HttpSenderListener, SessionChangedListener, ContextPanelFactory, ContextDataFactory {
	
	public static final String NAME = "ExtensionAuth";
	
	private static final String LOGIN_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-arrow.png";
	private static final String LOGOUT_ICON_RESOURCE = "/resource/icon/fugue/door-half-open-red-arrow.png";
	private static final String REAUTH_OFF_ICON_RESOURCE = "/resource/icon/fugue/door-half-open.png";
	private static final String REAUTH_ON_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-loop-arrow.png";

	private PopupContextMenuSiteNodeFactory popupFlagLoginMenuFactory= null;
	private PopupContextMenuSiteNodeFactory popupFlagLogoutMenuFactory= null;
	private PopupContextMenuItemFactory popupFlagLoggedInIndicatorMenuFactory= null;
	private PopupContextMenuItemFactory popupFlagLoggedOutIndicatorMenuFactory= null;

	private JToggleButton reauthenticateButton = null;

	private HttpSender httpSender = null;
	private boolean reauthenticate = false;
	private Session session = null;
	private AuthAPI api = null;
	
	private Map<Integer,SessionAuthenticationPanel> authPanelMap = new HashMap<Integer,SessionAuthenticationPanel>();
	private Map<Integer,ContextAuth> contextAuthMap = new HashMap<Integer,ContextAuth>();
	
	private Logger logger = Logger.getLogger(this.getClass());
	
    /**
     * 
     */
    public ExtensionAuth() {
        super();
 		initialize();
    }
    
	private void initialize() {
        this.setName(NAME);
        this.setOrder(160);
        
        this.api = new AuthAPI(this);
        API.getInstance().registerApiImplementor(api);
        HttpSender.addListener(this);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
		extensionHook.addSessionListener(this);
		// TODO addthis to the extensionHook??
		Model.getSingleton().addContextDataFactory(this);

	    if (getView() != null) {	
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoginMenuFactory());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLogoutMenuFactory());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoggedInIndicatorMenuFactory());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoggedOutIndicatorMenuFactory());

			View.getSingleton().addMainToolbarButton(getReauthenticateButton());

			// Factory for generating Session Context Auth panels
			getView().addContextPanelFactory(this);

	    }
	}
	
	protected void setReauthenticate(boolean reauth) {
		this.reauthenticate = reauth; 
	    if (getView() != null) {	        
		    reauthenticateButton.setSelected(reauth);
		    if (reauth) {
				reauthenticateButton.setIcon(new ImageIcon(ExtensionAuth.class.getResource(REAUTH_ON_ICON_RESOURCE)));
				reauthenticateButton.setToolTipText(Constant.messages.getString("auth.toolbar.button.reauth.on"));
			} else {
				reauthenticateButton.setIcon(new ImageIcon(ExtensionAuth.class.getResource(REAUTH_OFF_ICON_RESOURCE)));
				reauthenticateButton.setToolTipText(Constant.messages.getString("auth.toolbar.button.reauth.off"));
		    }
	    }
	}

	private JToggleButton getReauthenticateButton() {
		if (reauthenticateButton == null) {
			reauthenticateButton = new JToggleButton();
			reauthenticateButton.setIcon(new ImageIcon(ExtensionAuth.class.getResource(REAUTH_OFF_ICON_RESOURCE)));
			reauthenticateButton.setToolTipText(Constant.messages.getString("auth.toolbar.button.reauth.disabled"));
			reauthenticateButton.setEnabled(false);	// Disable until login and one indicator flagged
			
			reauthenticateButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
				    setReauthenticate(getReauthenticateButton().isSelected());
				}
			});
		}
		return reauthenticateButton;
	}

	private PopupContextMenuSiteNodeFactory getPopupFlagLoginMenuFactory() {
		if (this.popupFlagLoginMenuFactory == null) {
			popupFlagLoginMenuFactory = new PopupContextMenuSiteNodeFactory (Constant.messages.getString("context.flag.popup")) {
				private static final long serialVersionUID = 1L;

				@Override
				public PopupContextMenu getContextMenu(Context context,
						String parentMenu) {
					return new PopupContextMenu(context, parentMenu, 
							MessageFormat.format(Constant.messages.getString("auth.popup.login.req"), context.getName())) {
						private static final long serialVersionUID = 1L;

						@Override
						public void performAction(SiteNode sn) throws Exception {
							setLoginRequest(this.getContext().getIndex(), sn);
					        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), getContextPanel(this.getContext()).getName());
						}
					};
				}
			    @Override
			    public int getParentMenuIndex() {
			    	return 3;
			    }
			};
		}
		return this.popupFlagLoginMenuFactory;
	}

	private PopupContextMenuSiteNodeFactory getPopupFlagLogoutMenuFactory() {
		if (this.popupFlagLogoutMenuFactory == null) {
			popupFlagLogoutMenuFactory = new PopupContextMenuSiteNodeFactory (Constant.messages.getString("context.flag.popup")) {
				private static final long serialVersionUID = 1L;

				@Override
				public PopupContextMenu getContextMenu(Context context,
						String parentMenu) {
					return new PopupContextMenu(context, parentMenu, 
							MessageFormat.format(Constant.messages.getString("auth.popup.logout.req"), context.getName())) {
						private static final long serialVersionUID = 1L;

						@Override
						public void performAction(SiteNode sn) throws Exception {
							setLogoutRequest(this.getContext().getIndex(), sn);
					        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), getContextPanel(this.getContext()).getName());
						}
					};
				}
			    @Override
			    public int getParentMenuIndex() {
			    	return 3;
			    }
			};
		}
		return this.popupFlagLogoutMenuFactory;
	}

	private PopupContextMenuItemFactory getPopupFlagLoggedInIndicatorMenuFactory() {
		if (this.popupFlagLoggedInIndicatorMenuFactory == null) {
			popupFlagLoggedInIndicatorMenuFactory = new PopupContextMenuItemFactory (Constant.messages.getString("context.flag.popup")) {
				private static final long serialVersionUID = 1L;

				@Override
				public ExtensionPopupMenuItem getContextMenu(Context context,
						String parentMenu) {
				
					PopupFlagLoggedInIndicatorMenu subMenu = new PopupFlagLoggedInIndicatorMenu(context) {
						private static final long serialVersionUID = 1L;
						@Override
						public void performAction() {
							try {
								setLoggedInIndicationRegex(this.getContextId(), this.getSelectedText());
				    			// Show the relevant session dialog
						        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), getContextPanel(this.getContextId()).getName());
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
					};
					
					return subMenu;
				}
			    @Override
			    public int getParentMenuIndex() {
			    	return 3;
			    }
			};
		}
		return this.popupFlagLoggedInIndicatorMenuFactory;
	}

	private PopupContextMenuItemFactory getPopupFlagLoggedOutIndicatorMenuFactory() {
		if (this.popupFlagLoggedOutIndicatorMenuFactory == null) {
			popupFlagLoggedOutIndicatorMenuFactory = new PopupContextMenuItemFactory (Constant.messages.getString("context.flag.popup")) {
				private static final long serialVersionUID = 1L;

				@Override
				public ExtensionPopupMenuItem getContextMenu(Context context,
						String parentMenu) {
				
					PopupFlagLoggedOutIndicatorMenu subMenu = new PopupFlagLoggedOutIndicatorMenu(context) {
						private static final long serialVersionUID = 1L;
						@Override
						public void performAction() {
							try {
								setLoggedOutIndicationRegex(this.getContextId(), this.getSelectedText());
				    			// Show the relevant session dialog
						        View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), getContextPanel(this.getContextId()).getName());
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
					};
					
					return subMenu;
				}
			    @Override
			    public int getParentMenuIndex() {
			    	return 3;
			    }
			};
		}
		return this.popupFlagLoggedOutIndicatorMenuFactory;
	}

	
	private ContextAuth getContextAuth (int contextId) {
		if (this.contextAuthMap.containsKey(contextId)) {
			return this.contextAuthMap.get(contextId);
		}
		ContextAuth ca = new ContextAuth(contextId);
		this.contextAuthMap.put(contextId, ca);
		return ca;
	}
	
	protected HttpMessage getLoginRequest(int contextId) throws Exception {
		return this.getContextAuth(contextId).getLoginMsg();
	}
	
	private void setReauthButtonState(boolean enabled) {
		if (enabled) {
			this.getReauthenticateButton().setIcon(new ImageIcon(ExtensionAuth.class.getResource(REAUTH_OFF_ICON_RESOURCE)));
			this.getReauthenticateButton().setToolTipText(Constant.messages.getString("auth.toolbar.button.reauth.off"));
			this.getReauthenticateButton().setEnabled(true);
		} else {
			this.reauthenticate = false;
			this.getReauthenticateButton().setIcon(new ImageIcon(ExtensionAuth.class.getResource(REAUTH_OFF_ICON_RESOURCE)));
			this.getReauthenticateButton().setToolTipText(Constant.messages.getString("auth.toolbar.button.reauth.disabled"));
			this.getReauthenticateButton().setSelected(false);
			this.getReauthenticateButton().setEnabled(false);
		}
	}
	
	private void setReauthButtonState() {
		if (Control.getSingleton().getMode().equals(Mode.safe)) {
			// safe mode - dont allow anything potentially 'bad' ;)
			this.setReauthButtonState(false);
		}
		boolean canAuth = false;
		List<Context> contexts = Model.getSingleton().getSession().getContexts();
		for (Context context : contexts) {
			if (this.getContextAuth(context.getIndex()).canAuthenticate()) {
				canAuth = true;
				break;
			}
		}

		if (canAuth) {
			// Can reauthenticate for at least one context
			if (! this.getReauthenticateButton().isEnabled()) {
				// Theres now enough info for re-authentication
				this.setReauthButtonState(true);
			}
		} else if (this.getReauthenticateButton().isEnabled()) {
			// No longer enough info for re-authentication
			this.setReauthButtonState(false);
		}
	}

	protected void setLoginRequest(int contextId, SiteNode sn) throws Exception {
		ContextAuth ca = this.getContextAuth(contextId);
		if (ca.getLoginSiteNode() != null) {
			ca.getLoginSiteNode().removeCustomIcon(LOGIN_ICON_RESOURCE);
		}
		ca.setLoginSiteNode(sn);
		if (sn == null) {
			ca.setLoginMsg(null);
	        if (getView() != null) {
	        	this.getContextPanel(contextId).setLoginURL("");
	        	this.getContextPanel(contextId).setLoginPostData("");
				this.setReauthButtonState();
	        }
			return;
		}
		sn.addCustomIcon(LOGIN_ICON_RESOURCE, false);
		this.setLoginMsg(contextId, sn.getHistoryReference().getHttpMessage());
	}

	private void setLoginMsg(int contextId, HttpMessage msg) throws Exception {
		ContextAuth ca = this.getContextAuth(contextId);
		ca.setLoginMsg(msg);
		
        if (getView() != null) {
        	this.getContextPanel(contextId).setLoginURL(msg.getRequestHeader().getURI().toString());
			
			if (ca.getLoginMsg().getRequestHeader().getMethod().equals(HttpRequestHeader.POST)) {
				this.getContextPanel(contextId).setLoginPostData(msg.getRequestBody().toString());
			} else {
				this.getContextPanel(contextId).setLoginPostData("");
			}
			
			this.setReauthButtonState();
        }
        
	}

	protected void setLoginRequest(int contextId, String url, String postData) throws Exception {
		if (url == null || url.length() == 0) {
			this.setLoginRequest(contextId, null);
		} else {
			String method = HttpRequestHeader.GET;
			if (postData != null && postData.length() > 0) {
				method = HttpRequestHeader.POST;
			}
			URI uri = new URI(url, true);
			// Note the findNode just checks the parameter names, not their values
			SiteNode sn = Model.getSingleton().getSession().getSiteTree().findNode(uri, method, postData);
			if (isSiteNodeMatch(sn, uri.getQuery(), postData)) {
				this.setLoginRequest(contextId, sn);
			} else {
				// Havnt visited this node before, not a problem
				HttpMessage msg = new HttpMessage();
				msg.setRequestHeader(new HttpRequestHeader(method, uri, HttpHeader.HTTP10));
				msg.setRequestBody(postData);
				this.setLoginMsg(contextId, msg);
			}
		}
	}

    private static boolean isSiteNodeMatch(SiteNode sn, String query, String postData) throws Exception {
        if (sn == null) {
            return false;
        }

        final HttpMessage httpMessage = sn.getHistoryReference().getHttpMessage();

        if (!httpMessage.getUrlParams().equals(getParamsSet(query))) {
            return false;
        }

        if (!httpMessage.getRequestBody().toString().equals(postData)) {
            return false;
        }

        return true;
    }

    // TODO factor out, adapted from HttpMessage.getParamsSet(HtmlParameter.Type,String)
    private static Set<HtmlParameter> getParamsSet(String params) {
        if (params == null || params.isEmpty()) {
            return Collections.emptySet();
        }

        TreeSet<HtmlParameter> set = new TreeSet<>();
        String[] keyValue = Pattern.compile("&", Pattern.CASE_INSENSITIVE).split(params);
        String key = null;
        String value = null;
        int pos = 0;
        for (int i = 0; i < keyValue.length; i++) {
            key = null;
            value = null;
            pos = keyValue[i].indexOf('=');
            if (pos > 0) {
                key = keyValue[i].substring(0, pos);
                value = keyValue[i].substring(pos + 1);
                set.add(new HtmlParameter(HtmlParameter.Type.url, key, value));
            } else if (keyValue[i].length() > 0) {
                set.add(new HtmlParameter(HtmlParameter.Type.url, keyValue[i], ""));
            }
        }

        return set;
    }

	protected HttpMessage getLogoutRequest(int contextId) throws Exception {
		return this.getContextAuth(contextId).getLogoutMsg();
	}

	protected void setLogoutRequest(int contextId, SiteNode sn) throws Exception {
		ContextAuth ca = this.getContextAuth(contextId);

		if (ca.getLogoutSiteNode() != null) {
			ca.getLogoutSiteNode().removeCustomIcon(LOGOUT_ICON_RESOURCE);
		}
		ca.setLogoutSiteNode(sn);
		if (sn == null) {
			ca.setLogoutMsg(null);
	        if (getView() != null) {
	        	this.getContextPanel(contextId).setLogoutURL("");
	        	this.getContextPanel(contextId).setLogoutPostData("");
	        }
			return;
		}
		sn.addCustomIcon(LOGOUT_ICON_RESOURCE, false);
		this.setLogoutMsg(contextId, sn.getHistoryReference().getHttpMessage());
	}
	
	private void setLogoutMsg(int contextId, HttpMessage msg) throws Exception {
		ContextAuth ca = this.getContextAuth(contextId);

		ca.setLogoutMsg(msg);

        if (getView() != null) {
        	this.getContextPanel(contextId).setLogoutURL(msg.getRequestHeader().getURI().toString());
			
			if (msg.getRequestHeader().getMethod().equals(HttpRequestHeader.POST)) {
				this.getContextPanel(contextId).setLogoutPostData(msg.getRequestBody().toString());
			} else {
				this.getContextPanel(contextId).setLogoutPostData("");
			}
        }
	}
	
	protected void setLogoutRequest(int contextId, String url, String postData) throws Exception {
		if (url == null || url.length() == 0) {
			this.setLogoutRequest(contextId, null);
		} else {
			String method = HttpRequestHeader.GET;
			if (postData != null && postData.length() > 0) {
				method = HttpRequestHeader.POST;
			}
			URI uri = new URI(url, true);
			// Note the findNode just checks the parameter names, not their values
			SiteNode sn = Model.getSingleton().getSession().getSiteTree().findNode(uri, method, postData);
			if (isSiteNodeMatch(sn, uri.getQuery(), postData)) {
				this.setLogoutRequest(contextId, sn);
			} else {
				// Havnt visited this node before, not a problem
				HttpMessage msg = new HttpMessage();
				msg.setRequestHeader(new HttpRequestHeader(method, uri, HttpHeader.HTTP10));
				msg.setRequestBody(postData);
				this.setLogoutMsg(contextId, msg);
			}
		}
	}

	private HttpSender getHttpSender() {
		if (this.httpSender == null) {
			this.httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), false, HttpSender.AUTHENTICATION_INITIATOR);
		}
		return httpSender;
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
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
	public int getListenerOrder() {
		// Typically want to be one of the last ones invoked 
		return 50;
	}

	@Override
	public void onHttpRequestSend(HttpMessage msg, int initiator) {
	}
	
	private boolean isLoggedIn(int  contextId, HttpMessage msg) {
		if (msg == null || msg.getResponseBody() == null) {
			return false;
		}
		ContextAuth ca = this.getContextAuth(contextId);
		String body = msg.getResponseBody().toString();

		if (ca.getLoggedInIndicationPattern() != null && ca.getLoggedInIndicationPattern().matcher(body).find()) {
			// Looks like we're authenticated
			logger.debug("isLoggedIn " + msg.getRequestHeader().getURI() + " found auth pattern " + ca.getLoggedInIndicationPattern());
			return true;
		}
		
		if (ca.getLoggedOutIndicationPattern() != null && ! ca.getLoggedOutIndicationPattern().matcher(body).find()) {
			// Cant find the unauthenticated indicator, assume we're authenticated
			logger.debug("isLoggedIn " + msg.getRequestHeader().getURI() + " not found unauth pattern " + ca.getLoggedOutIndicationPattern());
			return true;
		}
		return false;
	}

	@Override
	public void onHttpResponseReceive(HttpMessage msg, int initiator) {
		if (! reauthenticate || msg.getResponseBody() == null || msg.getRequestHeader().isImage() || 
				(initiator == HttpSender.AUTHENTICATION_INITIATOR)) {
			// Not relevant
			return;
		}
		
		// Is the message in any of the contexts?
		List<Context> contexts = Model.getSingleton().getSession().getContexts();
		ContextAuth ca = null;
		for (Context context : contexts) {
			if (context.isInContext(msg.getRequestHeader().getURI().toString())) {
				ca = this.getContextAuth(context.getIndex());
				if (! ca.canAuthenticate()) {
					// Dont have enough info yet
					ca = null;
				} else {
					// Select the first context we find that has enough info
					break;
				}
			}
		}
		if (ca == null) {
			// Havnt found a context this message is in with enough info to reauthenticate
			return;
		}
		
		if (this.isLoggedIn(ca.getContextId(), msg)) {
			// Looks like we're authenticated - nothing to do
			// logger.debug("onHttpResponseReceive " + msg.getRequestHeader().getURI() + " (initial req) is logged in, so no action");
			return;
		}
		
		// We're not authenticated, so try to login

		if (this.login(ca.getContextId())) {
			if (View.isInitialised()) {
			// Let the user know it worked
				View.getSingleton().getOutputPanel().append(Constant.messages.getString("auth.output.success") + "\n");
			}
			
			// Logged in, try previous request
			try {
				this.getHttpSender().sendAndReceive(msg, true);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else if (View.isInitialised()) {
			// Let the user know it failed :(
			View.getSingleton().getOutputPanel().append(Constant.messages.getString("auth.output.failure") + "\n");
		}
	}
	
	protected boolean login(int contextId) {
		ContextAuth ca = this.getContextAuth(contextId);
		
		if (ca.getLoginMsg() == null) {
			return false;
		}
		HttpMessage msg = ca.getLoginMsg().cloneRequest();
		try {
			// TODO log in history or relevant tab
			this.getHttpSender().sendAndReceive(msg, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return this.isLoggedIn(contextId, msg);
	}
	
	protected boolean logout(int contextId) {
		ContextAuth ca = this.getContextAuth(contextId);

		if (ca.getLogoutMsg() == null) {
			return false;
		}
		HttpMessage msg = ca.getLogoutMsg().cloneRequest();
		try {
			// TODO log in history or relevant tab
			this.getHttpSender().sendAndReceive(msg, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return ! this.isLoggedIn(contextId, msg);
	}
	
	protected String getLoggedOutIndicationRegex(int contextId) {
		ContextAuth ca = this.getContextAuth(contextId);

		if (ca.getLoggedOutIndicationPattern() == null) {
			return null;
		}
		return ca.getLoggedOutIndicationPattern().pattern();
	}

	protected void setLoggedOutIndicationRegex(int contextId, String unauthIndicationRegex) {
		ContextAuth ca = this.getContextAuth(contextId);

		if (unauthIndicationRegex == null || unauthIndicationRegex.trim().length() == 0) {
			ca.setLoggedOutIndicationPattern(null);
		} else {
			ca.setLoggedOutIndicationPattern(Pattern.compile(unauthIndicationRegex));
		}
        if (getView() != null) {
        	this.getContextPanel(contextId).setLoggedOutIndicationRegex(unauthIndicationRegex);
        	
			this.setReauthButtonState();
        }
	}
	
	protected String getLoggedInIndicationRegex(int contextId) throws Exception {
		ContextAuth ca = this.getContextAuth(contextId);

		if (ca.getLoggedInIndicationPattern() == null) {
			return null;
		}
		return ca.getLoggedInIndicationPattern().pattern();
	}

	protected void setLoggedInIndicationRegex(int contextId, String authIndicationRegex) {
		ContextAuth ca = this.getContextAuth(contextId);

		if (authIndicationRegex == null || authIndicationRegex.trim().length() == 0) {
			ca.setLoggedInIndicationPattern (null);
		} else {
			ca.setLoggedInIndicationPattern (Pattern.compile(authIndicationRegex));
		}
        if (getView() != null) {
        	this.getContextPanel(contextId).setLoggedInIndicationRegex(authIndicationRegex);

			this.setReauthButtonState();
        }
	}
	
	@Override
	public void sessionChanged(Session session) {
		// Ignore
		this.session = session;
	}

	@Override
	public void sessionAboutToChange(Session session) {
		// Ignore
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// Ignore
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		setReauthButtonState();
	}

	protected void saveAuthParams(int contextId) {
		if (session != null) {
    		ContextAuth ca = this.getContextAuth(contextId);
    		try {
    			if (ca.getLoginMsg() != null) {
					session.setContextData(contextId, RecordContext.TYPE_AUTH_LOGIN_URL, ca.getLoginMsg().getRequestHeader().getURI().toString());
					session.setContextData(contextId, RecordContext.TYPE_AUTH_LOGIN_POST_DATA, ca.getLoginMsg().getRequestBody().toString());
        		}
    			if (ca.getLogoutMsg() != null) {
    				session.setContextData(contextId, RecordContext.TYPE_AUTH_LOGOUT_URL, ca.getLogoutMsg().getRequestHeader().getURI().toString());
    				session.setContextData(contextId, RecordContext.TYPE_AUTH_LOGOUT_POST_DATA, ca.getLogoutMsg().getRequestBody().toString());
    			}
    			if (ca.getLoggedInIndicationPattern() != null) {
    				session.setContextData(contextId, RecordContext.TYPE_AUTH_LOGIN_INDICATOR, ca.getLoggedInIndicationPattern().toString());
    			}
    			if (ca.getLoggedOutIndicationPattern() != null) {
    				session.setContextData(contextId, RecordContext.TYPE_AUTH_LOGOUT_INDICATOR, ca.getLoggedOutIndicationPattern().toString());
    			}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
        }
	}
	
	@Override
	public void saveContextData (Context ctx) {
		this.saveAuthParams(ctx.getIndex());
	}

	public AuthAPI getApi() {
		return api;
	}

	@Override
	public AbstractParamPanel getContextPanel(Context ctx) {
		return this.getContextPanel(ctx.getIndex());
	}
	
	private SessionAuthenticationPanel getContextPanel(int contextId) {
		SessionAuthenticationPanel panel = this.authPanelMap.get(contextId);
		if (panel == null) {
			panel = new SessionAuthenticationPanel(contextId);
			this.authPanelMap.put(contextId, panel);
		} else {
		}
		return panel;
	}

	@Override
	public void loadContextData(Context ctx) {
        try {
			if (session != null) {
				this.getContextAuth(ctx.getIndex());
				if (View.isInitialised()) {
					this.getContextPanel(ctx);
				}
				
				List<String> strs = session.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_AUTH_LOGIN_URL);
				if (strs != null && strs.size() > 0) {
					this.setLoginRequest(ctx.getIndex(), strs.get(0), session.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_AUTH_LOGIN_POST_DATA).get(0));
				}
				strs = session.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_AUTH_LOGOUT_URL);
				if (strs != null && strs.size() > 0) {
					this.setLogoutRequest(ctx.getIndex(), strs.get(0), session.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_AUTH_LOGOUT_POST_DATA).get(0));
				}
				strs = session.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_AUTH_LOGIN_INDICATOR);
				if (strs != null && strs.size() > 0) {
					this.setLoggedInIndicationRegex(ctx.getIndex(), strs.get(0));
				}
				strs = session.getContextDataStrings(ctx.getIndex(), RecordContext.TYPE_AUTH_LOGOUT_INDICATOR);
				if (strs != null && strs.size() > 0) {
					this.setLoggedOutIndicationRegex(ctx.getIndex(), strs.get(0));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void discardContexts() {
		authPanelMap.clear();
		contextAuthMap.clear();
		
	}
}
