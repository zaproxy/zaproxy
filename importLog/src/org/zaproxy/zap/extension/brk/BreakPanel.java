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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.tab.Tab;

public class BreakPanel extends AbstractPanel implements Tab {

	private static final long serialVersionUID = 1L;

	private static final String REQUEST_PANEL = "request";
	private static final String RESPONSE_PANEL = "response";
	
	private HttpPanelRequest requestPanel;
	private HttpPanelResponse responsePanel;

	private JPanel panelContent;
	private BreakPanelToolbarFactory breakToolbarFactory;

	public BreakPanel() {
		super();
		initialize();
	}
	
	private void initialize() {
		this.setIcon(new ImageIcon(BreakPanel.class.getResource("/resource/icon/16/101grey.png")));	// 'grey X' icon
		this.setLayout(new BorderLayout());

		breakToolbarFactory = new BreakPanelToolbarFactory(this);

		panelContent = new JPanel(new CardLayout());
		this.add(panelContent, BorderLayout.CENTER);

		requestPanel = new HttpPanelRequest(false, OptionsParamView.BASE_VIEW_KEY + ".break.");
		requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		responsePanel = new HttpPanelResponse(false, OptionsParamView.BASE_VIEW_KEY + ".break.");
		responsePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());

		panelContent.add(requestPanel, REQUEST_PANEL);
		panelContent.add(responsePanel, RESPONSE_PANEL);

		switch(Model.getSingleton().getOptionsParam().getViewParam().getBrkPanelViewOption()) {
		case 0:
			// If the user decided to disable the main toolbar, the break
			// buttons have to be force to be displayed in the break panel
			if(Model.getSingleton().getOptionsParam().getViewParam().getShowMainToolbar() == 0) {
				this.add(getPanelCommand(), BorderLayout.NORTH);
			} else {
				getPanelMainToolbarCommand();
			}
			break;
		case 1:
			requestPanel.addOptions(getPanelCommand(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
			responsePanel.addOptions(getPanelCommand(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);

			break;
		case 2:
			requestPanel.addOptions(getPanelCommand(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
			responsePanel.addOptions(getPanelCommand(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
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
	
	public void breakpointHit () {
		breakToolbarFactory.breakpointHit();
	}
	
	public boolean isHoldMessage() {
		return breakToolbarFactory.isHoldMessage();
	}
	
	public boolean isStepping() {
		return breakToolbarFactory.isStepping();
	}
    
    public boolean isToBeDropped() {
        return breakToolbarFactory.isToBeDropped();
    }
	
	protected void breakpointDisplayed () {
		// Grab the focus
		this.setTabFocus();
	}
	
	private JToolBar getPanelCommand() {
		JToolBar panelCommand = new JToolBar();
		panelCommand.setFloatable(false);
		panelCommand.setBorder(BorderFactory.createEmptyBorder());
		panelCommand.setRollover(true);
		
		panelCommand.setName("Command");

		panelCommand.add(breakToolbarFactory.getBtnBreakRequest());
		panelCommand.add(breakToolbarFactory.getBtnBreakResponse());
		panelCommand.add(breakToolbarFactory.getBtnStep());
		panelCommand.add(breakToolbarFactory.getBtnContinue());
		panelCommand.add(breakToolbarFactory.getBtnDrop());

		return panelCommand;
	}

	private void getPanelMainToolbarCommand() {
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnBreakRequest());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnBreakResponse());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnStep());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnContinue());
		View.getSingleton().addMainToolbarButton(breakToolbarFactory.getBtnDrop());
	}
	
	public void setMessage(Message aMessage, boolean isRequest) {
		CardLayout cl = (CardLayout)(panelContent.getLayout());

		if (isRequest) {
            requestPanel.setMessage(aMessage, true);
            requestPanel.setEditable(true);
			cl.show(panelContent, REQUEST_PANEL);
		} else {
            responsePanel.setMessage(aMessage, true);
            responsePanel.setEditable(true);
			cl.show(panelContent, RESPONSE_PANEL);
		}
	}

	
	public void saveMessage(boolean isRequest) {
		CardLayout cl = (CardLayout)(panelContent.getLayout());

		if (isRequest) {
			requestPanel.saveData();
			cl.show(panelContent, REQUEST_PANEL);
		} else {
			responsePanel.saveData();
			cl.show(panelContent, RESPONSE_PANEL);
		}
	}

	public void savePanels() {
		requestPanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
		responsePanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
	}
	
	public void clearAndDisableRequest() {
		requestPanel.clearView(false);
		requestPanel.setEditable(false);
	}
	
	public void clearAndDisableResponse() {
		responsePanel.clearView(false);
		responsePanel.setEditable(false);
	}
	
	public void init() {
		breakToolbarFactory.init();
	}
	
	public void reset() {
		breakToolbarFactory.reset();
	}

	public void sessionModeChanged(Mode mode) {
		if (mode.equals(Mode.safe)) {
			this.breakToolbarFactory.setBreakEnabled(false);
		} else {
			this.breakToolbarFactory.setBreakEnabled(true);
		}

	}

}