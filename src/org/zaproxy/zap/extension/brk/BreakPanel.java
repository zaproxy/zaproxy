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

package org.zaproxy.zap.extension.brk;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.tab.Tab;

/**
 *
 * Break Panel UI Element
 * 
 */
public class BreakPanel extends AbstractPanel implements Tab {
	private static final long serialVersionUID = 1L;

	private HttpPanelRequest requestPanel;
	private HttpPanelResponse responsePanel;

	private JPanel panelContent;
	private BreakPanelToolbarFactory breakToolbarFactory;

	public BreakPanel() {
		super();
		initialize();
	}

	/**
	 * @param isEditable
	 */
	public BreakPanel(boolean isEditable) {
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/101grey.png")));	// 'grey X' icon
		this.setLayout(new BorderLayout());

		breakToolbarFactory = new BreakPanelToolbarFactory(this);

		panelContent = new JPanel(new CardLayout());
		this.add(panelContent, BorderLayout.CENTER);

		requestPanel = new HttpPanelRequest(true, null);
		responsePanel = new HttpPanelResponse(true, null);

		panelContent.add(requestPanel, "request");
		panelContent.add(responsePanel, "response");

		switch(Model.getSingleton().getOptionsParam().getViewParam().getBrkPanelViewOption()) {
		case 0:
			// If the user decided to disable the main toolbar, the break
			// buttons have to be force to be displayed in the break panel
			if(Model.getSingleton().getOptionsParam().getViewParam().getShowMainToolbar() == 0) {
				JPanel panelCommand = getPanelCommand();
				this.add(panelCommand, BorderLayout.NORTH);
			} else {
				getPanelMainToolbarCommand();
			}
			break;
		case 1:
			requestPanel.addHeaderPanel(getPanelCommand());
			responsePanel.addHeaderPanel(getPanelCommand());

			break;
		case 2:
			requestPanel.addHeaderPanel(getPanelCommand());
			responsePanel.addHeaderPanel(getPanelCommand());
			getPanelMainToolbarCommand();
			break;
		default:
			getPanelMainToolbarCommand();
		}
	}

	public boolean isBreakRequest() {
		return breakToolbarFactory.isBreakRequest();
	}

	public boolean isBreakResponse() {
		return breakToolbarFactory.isBreakResponse();
	}

	public void breakPointHit () {
		breakToolbarFactory.breakPointHit();
	}

	public boolean isHoldMessage() {
		return breakToolbarFactory.isHoldMessage();
	}

	public boolean isStepping() {
		return breakToolbarFactory.isStepping();
	}

	protected void breakPointDisplayed () {
		// Grab the focus
		this.setTabFocus();
	}

	/**
	 * This method initializes panelCommand
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelCommand() {
		JPanel panelCommand = new JPanel();
		Box box = Box.createHorizontalBox();
		
		panelCommand.setLayout(new BoxLayout(panelCommand,BoxLayout.X_AXIS));
		panelCommand.setName("Command");

		box.add(Box.createGlue());
		box.add(breakToolbarFactory.getBtnBreakRequest());
		box.add(breakToolbarFactory.getBtnBreakResponse());
		box.add(breakToolbarFactory.getBtnStep());
		box.add(breakToolbarFactory.getBtnContinue());
		box.add(breakToolbarFactory.getBtnDrop());

		panelCommand.add(box);

		return panelCommand;
	}

	private void getPanelMainToolbarCommand() {
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnBreakRequest());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnBreakResponse());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnStep());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnContinue());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnDrop());
	}

	public void setMessage(HttpMessage msg, boolean isRequest) {
		CardLayout cl = (CardLayout)(panelContent.getLayout());

		if (isRequest) {
			cl.show(panelContent, "request");
			requestPanel.setMessage(msg);
		} else {
			cl.show(panelContent, "response");
			responsePanel.setMessage(msg);
		}
		
		breakToolbarFactory.gotMessage(isRequest);
		
	}

	public void getMessage(HttpMessage msg, boolean isRequest) {
		CardLayout cl = (CardLayout)(panelContent.getLayout());

		if (isRequest) {
			requestPanel.saveData();
			cl.show(panelContent, "request");
			requestPanel.setMessage(msg);
		} else {
			responsePanel.saveData();
			responsePanel.getHttpMessage().getResponseHeader().setContentLength(responsePanel.getHttpMessage().getResponseBody().length());
			cl.show(panelContent, "response");
			responsePanel.setMessage(msg);
		}
	}

	public void clearView() {
		responsePanel.clearView(false);
		requestPanel.clearView(false);
	}

}