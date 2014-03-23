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
 * 
 * Note that this extension ane the other classes in this package are heavily 
 * based on the orriginal Paros ExtensionSpider! 
 */
package org.zaproxy.zap.extension.reauth;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * @deprecated (2.3.0) Replaced by {@link org.zaproxy.zap.extension.authentication.ContextAuthenticationPanel}. It will be
 *             removed in a future release.
 */
@Deprecated
public class SessionAuthenticationPanel extends AbstractContextPropertiesPanel {

	private static final String PANEL_NAME = Constant.messages.getString("auth.session.title");
	private static final long serialVersionUID = -1;

	private JPanel panelSession = null;
	private ZapTextField txtLoginUrl = null;
	private ZapTextField txtLoginPostData = null;
	private ZapTextField txtLogoutUrl = null;
	private ZapTextField txtLogoutPostData = null;
	private ZapTextField txtLoggedInIndicaterRegex = null;
	private ZapTextField txtLoggedOutIndicaterRegex = null;

	public static String getPanelName(int contextId) {
		// Panel names hav to be unique, so prefix with the context id
		return contextId + ": " + PANEL_NAME;
	}

	public SessionAuthenticationPanel(int contextId) {
		super(contextId);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getPanelName(this.getContextIndex()));
		this.add(getPanelSession(), getPanelSession().getName());
	}

	/**
	 * This method initializes panelSession
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelSession() {
		if (panelSession == null) {

			panelSession = new JPanel();
			panelSession.setLayout(new GridBagLayout());
			panelSession.setName("SessionAuthentication");
			panelSession.setLayout(new GridBagLayout());

			panelSession.add(new JLabel(Constant.messages.getString("auth.session.lable.loginurl")),
					LayoutHelper.getGBC(0, 0, 1, 1.0D));
			panelSession.add(getTxtLoginUrl(), LayoutHelper.getGBC(0, 1, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("auth.session.lable.loginpost")),
					LayoutHelper.getGBC(0, 2, 1, 1.0D));
			panelSession.add(getTxtLoginPostData(),
					LayoutHelper.getGBC(0, 3, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("auth.session.lable.loginregex")),
					LayoutHelper.getGBC(0, 4, 1, 1.0D));
			panelSession.add(getTxtLoggedInIndicaterRegex(),
					LayoutHelper.getGBC(0, 5, 1, 1.0D, new Insets(2, 0, 2, 0)));

			panelSession.add(new JLabel(Constant.messages.getString("auth.session.lable.logouturl")),
					LayoutHelper.getGBC(0, 6, 1, 1.0D));
			panelSession.add(getTxtLogoutUrl(), LayoutHelper.getGBC(0, 7, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("auth.session.lable.logoutpost")),
					LayoutHelper.getGBC(0, 8, 1, 1.0D));
			panelSession.add(getTxtLogoutPostData(),
					LayoutHelper.getGBC(0, 9, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("auth.session.lable.logoutregex")),
					LayoutHelper.getGBC(0, 10, 1, 1.0D));
			panelSession.add(getTxtLoggedOutIndicaterRegex(),
					LayoutHelper.getGBC(0, 11, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(), LayoutHelper.getGBC(0, 20, 1, 1.0D, 1.0D)); // Padding
		}
		return panelSession;
	}

	private ZapTextField getTxtLoginUrl() {
		if (txtLoginUrl == null) {
			txtLoginUrl = new ZapTextField();
		}
		return txtLoginUrl;
	}

	private ZapTextField getTxtLoginPostData() {
		if (txtLoginPostData == null) {
			txtLoginPostData = new ZapTextField();
		}
		return txtLoginPostData;
	}

	private ZapTextField getTxtLogoutUrl() {
		if (txtLogoutUrl == null) {
			txtLogoutUrl = new ZapTextField();
		}
		return txtLogoutUrl;
	}

	private ZapTextField getTxtLogoutPostData() {
		if (txtLogoutPostData == null) {
			txtLogoutPostData = new ZapTextField();
		}
		return txtLogoutPostData;
	}

	private ZapTextField getTxtLoggedInIndicaterRegex() {
		if (txtLoggedInIndicaterRegex == null) {
			txtLoggedInIndicaterRegex = new ZapTextField();
		}
		return txtLoggedInIndicaterRegex;
	}

	private ZapTextField getTxtLoggedOutIndicaterRegex() {
		if (txtLoggedOutIndicaterRegex == null) {
			txtLoggedOutIndicaterRegex = new ZapTextField();
		}
		return txtLoggedOutIndicaterRegex;
	}

	@Override
	public void initContextData(Session session, Context uiCommonContext) {
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Check for valid regexs
		Pattern.compile(this.txtLoggedInIndicaterRegex.getText());
		Pattern.compile(this.txtLoggedOutIndicaterRegex.getText());
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		ExtensionReauth ext = (ExtensionReauth) Control.getSingleton().getExtensionLoader()
				.getExtension(ExtensionReauth.NAME);
		if (ext != null) {
			ext.setLoginRequest(getContextIndex(), this.getTxtLoginUrl().getText(), this.getTxtLoginPostData()
					.getText());
			ext.setLogoutRequest(getContextIndex(), this.getTxtLogoutUrl().getText(), this.getTxtLogoutPostData()
					.getText());
			ext.setLoggedInIndicationRegex(getContextIndex(), this.txtLoggedInIndicaterRegex.getText());
			ext.setLoggedOutIndicationRegex(getContextIndex(), this.txtLoggedOutIndicaterRegex.getText());
		}
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	public void setLoginURL(String string) {
		this.getTxtLoginUrl().setText(string);

	}

	public void setLoginPostData(String string) {
		this.getTxtLoginPostData().setText(string);
	}

	public void setLogoutURL(String string) {
		this.getTxtLogoutUrl().setText(string);

	}

	public void setLogoutPostData(String string) {
		this.getTxtLogoutPostData().setText(string);
	}

	public void setLoggedOutIndicationRegex(String unauthIndicationRegex) {
		this.getTxtLoggedOutIndicaterRegex().setText(unauthIndicationRegex);

	}

	public void setLoggedInIndicationRegex(String authIndicationRegex) {
		this.getTxtLoggedInIndicaterRegex().setText(authIndicationRegex);
	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		// Do nothing as the info here is not saved into Context
	}
}
