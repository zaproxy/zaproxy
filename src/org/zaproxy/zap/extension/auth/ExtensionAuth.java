package org.zaproxy.zap.extension.auth;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.RecordSessionUrl;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.network.HttpSenderListener;

public class ExtensionAuth extends ExtensionAdaptor implements HttpSenderListener, SessionChangedListener {
	
	public static final String NAME = "ExtensionAuth";
	
	private static final String LOGIN_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-arrow.png";
	private static final String LOGOUT_ICON_RESOURCE = "/resource/icon/fugue/door-open-red-arrow.png";
	private static final String REAUTH_OFF_ICON_RESOURCE = "/resource/icon/fugue/door-open.png";
	private static final String REAUTH_ON_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-loop-arrow.png";

	private PopupFlagLoginMenu popupFlagLoginMenu= null;
	private PopupFlagLogoutMenu popupFlagLogoutMenu= null;
	private PopupFlagLoggedInIndicatorMenu popupFlagAuthIndicatorMenu = null;
	private PopupFlagLoggedOutIndicatorMenu popupFlagUnauthIndicatorMenu = null;
	private JToggleButton reauthenticateButton = null;

	private SessionAuthenticationPanel sessionAuthenticationPanel = null;
	private HttpSender httpSender = null;
	private SiteNode loginSiteNode = null;
	private HttpMessage loginMsg = null;
	private SiteNode logoutSiteNode = null;
	private HttpMessage logoutMsg = null;
	private Pattern loggedInIndicationPattern = null;
	private Pattern loggedOutIndicationPattern = null;
	private boolean reauthenticate = false;
	private Session session = null;
	private AuthAPI api = null;
	
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

	    if (getView() != null) {	        
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoginMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLogoutMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagAuthIndicatorMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagUnauthIndicatorMenu());
			View.getSingleton().addMainToolbarButton(getReauthenticateButton());

			// setup Session Properties
            this.sessionAuthenticationPanel = new SessionAuthenticationPanel();
			getView().getSessionDialog().addParamPanel(new String[]{}, sessionAuthenticationPanel, false);

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

	private PopupFlagLoginMenu getPopupFlagLoginMenu() {
		if (popupFlagLoginMenu == null) {
			popupFlagLoginMenu = new PopupFlagLoginMenu(this);
		}
		return popupFlagLoginMenu;
	}

	private PopupFlagLogoutMenu getPopupFlagLogoutMenu() {
		if (popupFlagLogoutMenu == null) {
			popupFlagLogoutMenu = new PopupFlagLogoutMenu(this);
		}
		return popupFlagLogoutMenu;
	}

	private PopupFlagLoggedInIndicatorMenu getPopupFlagAuthIndicatorMenu() {
		if (popupFlagAuthIndicatorMenu == null) {
			popupFlagAuthIndicatorMenu = new PopupFlagLoggedInIndicatorMenu(this);
		}
		return popupFlagAuthIndicatorMenu;
	}

	private PopupFlagLoggedOutIndicatorMenu getPopupFlagUnauthIndicatorMenu() {
		if (popupFlagUnauthIndicatorMenu == null) {
			popupFlagUnauthIndicatorMenu = new PopupFlagLoggedOutIndicatorMenu(this);
		}
		return popupFlagUnauthIndicatorMenu;
	}

	protected HttpMessage getLoginRequest() {
		return loginMsg;
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
		if (this.loginMsg != null && (this.loggedInIndicationPattern != null || this.loggedOutIndicationPattern != null)) {
			if (! this.getReauthenticateButton().isEnabled()) {
				// Theres now enough info for re-authentication
				this.setReauthButtonState(true);
			}
		} else if (this.getReauthenticateButton().isEnabled()) {
			// No longer enough info for re-authentication
			this.setReauthButtonState(false);
		}
	}

	protected void setLoginRequest(SiteNode sn) throws Exception {
		if (this.loginSiteNode != null) {
			this.loginSiteNode.removeCustomIcon(LOGIN_ICON_RESOURCE);
		}
		this.loginSiteNode = sn;
		if (sn == null) {
			this.loginMsg = null;
	        if (getView() != null) {
				this.sessionAuthenticationPanel.setLoginURL("");
				this.sessionAuthenticationPanel.setLoginPostData("");
				this.setReauthButtonState();
	        }
			return;
		}
		sn.addCustomIcon(LOGIN_ICON_RESOURCE, false);
		this.setLoginMsg(sn.getHistoryReference().getHttpMessage());
	}

	private void setLoginMsg(HttpMessage msg) throws Exception {
		this.loginMsg = msg;
		
        if (getView() != null) {
			this.sessionAuthenticationPanel.setLoginURL(this.loginMsg.getRequestHeader().getURI().toString());
			
			if (this.loginMsg.getRequestHeader().getMethod().equals(HttpRequestHeader.POST)) {
				this.sessionAuthenticationPanel.setLoginPostData(this.loginMsg.getRequestBody().toString());
			} else {
				this.sessionAuthenticationPanel.setLoginPostData("");
			}
			
			this.setReauthButtonState();
        }
        
	}

	protected void setLoginRequest(String url, String postData) throws Exception {
		if (url == null || url.length() == 0) {
			this.setLoginRequest(null);
		} else {
			String method = HttpRequestHeader.GET;
			if (postData != null && postData.length() > 0) {
				method = HttpRequestHeader.POST;
			}
			URI uri = new URI(url, true);
			SiteNode sn = Model.getSingleton().getSession().getSiteTree().findNode(uri, method, postData);
			if (sn != null) {
				this.setLoginRequest(sn);
			} else {
				// Havnt visited this node before, not a problem
				HttpMessage msg = new HttpMessage();
				msg.setRequestHeader(new HttpRequestHeader(method, uri, HttpHeader.HTTP10));
				msg.setRequestBody(postData);
				this.setLoginMsg(msg);
			}
		}
	}

	protected HttpMessage getLogoutRequest() {
		return logoutMsg;
	}

	protected void setLogoutRequest(SiteNode sn) throws Exception {
		if (this.logoutSiteNode != null) {
			this.logoutSiteNode.removeCustomIcon(LOGOUT_ICON_RESOURCE);
		}
		this.logoutSiteNode = sn;
		if (sn == null) {
			this.logoutMsg = null;
	        if (getView() != null) {
				this.sessionAuthenticationPanel.setLogoutURL("");
				this.sessionAuthenticationPanel.setLogoutPostData("");
	        }
			return;
		}
		sn.addCustomIcon(LOGOUT_ICON_RESOURCE, false);
		this.setLogoutMsg(sn.getHistoryReference().getHttpMessage());
	}
	
	private void setLogoutMsg(HttpMessage msg) throws Exception {
		this.logoutMsg = msg;

        if (getView() != null) {
			this.sessionAuthenticationPanel.setLogoutURL(this.logoutMsg.getRequestHeader().getURI().toString());
			
			if (this.logoutMsg.getRequestHeader().getMethod().equals(HttpRequestHeader.POST)) {
				this.sessionAuthenticationPanel.setLogoutPostData(this.logoutMsg.getRequestBody().toString());
			} else {
				this.sessionAuthenticationPanel.setLogoutPostData("");
			}
        }
	}
	
	protected void setLogoutRequest(String url, String postData) throws Exception {
		if (url == null || url.length() == 0) {
			this.setLogoutRequest(null);
		} else {
			String method = HttpRequestHeader.GET;
			if (postData != null && postData.length() > 0) {
				method = HttpRequestHeader.POST;
			}
			URI uri = new URI(url, true);
			SiteNode sn = Model.getSingleton().getSession().getSiteTree().findNode(uri, method, postData);
			if (sn != null) {
				this.setLogoutRequest(sn);
			} else {
				// Havnt visited this node before, not a problem
				HttpMessage msg = new HttpMessage();
				msg.setRequestHeader(new HttpRequestHeader(method, uri, HttpHeader.HTTP10));
				msg.setRequestBody(postData);
				this.setLogoutMsg(msg);
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
	
	private boolean isLoggedIn(HttpMessage msg) {
		if (msg == null || msg.getResponseBody() == null) {
			return false;
		}
		String body = msg.getResponseBody().toString();
		
		if (this.loggedInIndicationPattern != null && this.loggedInIndicationPattern.matcher(body).find()) {
			// Looks like we're authenticated
			logger.debug("isLoggedIn " + msg.getRequestHeader().getURI() + " found auth pattern " + this.getLoggedInIndicationRegex());
			return true;
		}
		
		if (this.loggedOutIndicationPattern != null && ! this.loggedOutIndicationPattern.matcher(body).find()) {
			// Cant find the unauthenticated indicator, assume we're authenticated
			logger.debug("isLoggedIn " + msg.getRequestHeader().getURI() + " not found unauth pattern " + this.getLoggedOutIndicationRegex());
			return true;
		}
		return false;
	}

	@Override
	public void onHttpResponseReceive(HttpMessage msg, int initiator) {
		if (! reauthenticate || msg.getResponseBody() == null || msg.getRequestHeader().isImage() || initiator != HttpSender.ACTIVE_SCANNER_INITIATOR) {
			// Not relevant
			return;
		}
		if (this.loginMsg == null || (this.loggedInIndicationPattern == null && this.loggedOutIndicationPattern == null)) {
			// Dont have enough info yet
			return;
		}
		
		if (this.isLoggedIn(msg)) {
			// Looks like we're authenticated - nothing to do
			// logger.debug("onHttpResponseReceive " + msg.getRequestHeader().getURI() + " (initial req) is logged in, so no action");
			return;
		}
		
		// We're not authenticated, so try to login

		if (this.login()) {
			// Let the user know it worked
			View.getSingleton().getOutputPanel().append(Constant.messages.getString("auth.output.success") + "\n");
			
			// Logged in, try previous request
			try {
				this.getHttpSender().sendAndReceive(msg, true);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			// Let the user know it failed :(
			View.getSingleton().getOutputPanel().append(Constant.messages.getString("auth.output.failure") + "\n");
		}
	}
	
	protected boolean login() {
		if (this.loginMsg == null) {
			return false;
		}
		HttpMessage msg = this.loginMsg.cloneRequest();
		try {
			// TODO log in history or relevant tab
			this.getHttpSender().sendAndReceive(msg, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return this.isLoggedIn(msg);
	}
	
	protected boolean logout() {
		if (this.logoutMsg == null) {
			return false;
		}
		HttpMessage msg = this.logoutMsg.cloneRequest();
		try {
			// TODO log in history or relevant tab
			this.getHttpSender().sendAndReceive(msg, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return ! this.isLoggedIn(msg);
	}
	
	protected String getLoggedOutIndicationRegex() {
		if (loggedOutIndicationPattern == null) {
			return null;
		}
		return loggedOutIndicationPattern.pattern();
	}

	protected void setLoggedOutIndicationRegex(String unauthIndicationRegex) {
		if (unauthIndicationRegex == null || unauthIndicationRegex.trim().length() == 0) {
			this.loggedOutIndicationPattern = null;
		} else {
			this.loggedOutIndicationPattern = Pattern.compile(unauthIndicationRegex);
		}
        if (getView() != null) {
        	this.sessionAuthenticationPanel.setLoggedOutIndicationRegex(unauthIndicationRegex);
        	
			this.setReauthButtonState();
        }
	}
	
	protected String getLoggedInIndicationRegex() {
		if (loggedInIndicationPattern == null) {
			return null;
		}
		return loggedInIndicationPattern.pattern();
	}

	protected void setLoggedInIndicationRegex(String authIndicationRegex) {
		if (authIndicationRegex == null || authIndicationRegex.trim().length() == 0) {
			this.loggedInIndicationPattern = null;
		} else {
			this.loggedInIndicationPattern = Pattern.compile(authIndicationRegex);
		}
        if (getView() != null) {
        	this.sessionAuthenticationPanel.setLoggedInIndicationRegex(authIndicationRegex);

			this.setReauthButtonState();
        }
	}
	
	private String getSingleSessionUrl(Session session, int type) {
		try {
		    List<String> urls = session.getSessionUrls(type);
		    if (urls.size() > 0) {
		    	return urls.get(0);
		    }
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	@Override
	public void sessionChanged(Session session) {
		this.session = session;
		if (session == null) {
			// Shutting down
			return;
		}
	    try {
			this.setLoginRequest(getSingleSessionUrl(session, RecordSessionUrl.TYPE_AUTH_LOGIN_URL),
					getSingleSessionUrl(session, RecordSessionUrl.TYPE_AUTH_LOGIN_POST_DATA));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	    this.setLoggedInIndicationRegex(getSingleSessionUrl(session, RecordSessionUrl.TYPE_AUTH_LOGIN_INDICATOR));
	    try {
			this.setLogoutRequest(getSingleSessionUrl(session, RecordSessionUrl.TYPE_AUTH_LOGOUT_URL),
					getSingleSessionUrl(session, RecordSessionUrl.TYPE_AUTH_LOGOUT_POST_DATA));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	    this.setLoggedOutIndicationRegex(getSingleSessionUrl(session, RecordSessionUrl.TYPE_AUTH_LOGOUT_INDICATOR));
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

	protected void saveAuthParams() {
        if (session != null) {
    		try {
    			if (this.getLoginRequest() != null) {
					session.setSessionUrl(RecordSessionUrl.TYPE_AUTH_LOGIN_URL, this.loginMsg.getRequestHeader().getURI().toString());
					session.setSessionUrl(RecordSessionUrl.TYPE_AUTH_LOGIN_POST_DATA, this.loginMsg.getRequestBody().toString());
        		}
    			if (this.getLogoutRequest() != null) {
					session.setSessionUrl(RecordSessionUrl.TYPE_AUTH_LOGOUT_URL, this.logoutMsg.getRequestHeader().getURI().toString());
					session.setSessionUrl(RecordSessionUrl.TYPE_AUTH_LOGOUT_POST_DATA, this.logoutMsg.getRequestBody().toString());
    			}
				session.setSessionUrl(RecordSessionUrl.TYPE_AUTH_LOGIN_INDICATOR, this.getLoggedInIndicationRegex());
				session.setSessionUrl(RecordSessionUrl.TYPE_AUTH_LOGOUT_INDICATOR, this.getLoggedOutIndicationRegex());
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
        }
	}

	public AuthAPI getApi() {
		return api;
	}
}
