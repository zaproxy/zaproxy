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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.TabbedPanel;
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
	private JPanel panelCommand = null;
	
	// Button notes
	// BreakRequest button, if set all requests trapped
	// BreakResponse button, ditto for responses
	// If break point hit, Break tab gets focus and icon goes red
	// Step button, only if break point hit, submits just this req/resp, breaks on next
	// Continue button, only if break point hit, submits this req/resp and continues until next break point hit
	// If BreakReq & Resp both selected Step and Continue buttons have same effect
	// 
	
	private JButton btnContinue = null;
	private JButton btnStep = null;
	private JButton btnDrop = null;

	private JToggleButton btnBreakRequest = null;
	private JToggleButton btnBreakResponse = null;

	private boolean cont = false;
	private boolean step = false;
	private boolean stepping = false;
	
	private HttpPanelRequest requestPanel;
	private HttpPanelResponse responsePanel;

	/**
     * 
     */
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
		this.setLayout(new CardLayout());
		
		requestPanel = new HttpPanelRequest(false, null);
		responsePanel = new HttpPanelResponse(false, null);
		
		this.add(requestPanel, "request");
		this.add(responsePanel, "response");
		
		switch(Model.getSingleton().getOptionsParam().getViewParam().getBrkPanelViewOption()) {
			case 0:
				// If the user decided to disable the main toolbar, the break
				// buttons have to be force to be displayed in the break panel
				if(Model.getSingleton().getOptionsParam().getViewParam().getShowMainToolbar() == 0) {
					requestPanel.getPanelSpecial().add(getPanelCommand(), "");
					responsePanel.getPanelSpecial().add(getPanelCommand(), "");
					//getPanelOption().add(getPanelCommand(), "");
				} else {
					getPanelMainToolbarCommand();
				}
				break;
			case 1:
				requestPanel.getPanelSpecial().add(getPanelCommand(), "");
				responsePanel.getPanelSpecial().add(getPanelCommand(), "");
				//getPanelOption().add(getPanelCommand(), "");
				break;
			/*
			 * TODO Currently, buttons cannot be display in toolbar and break panel, as the first initalization of the buttons wins
			case 2:
				getPanelOption().add(getPanelCommand(), "");
				getPanelMainToolbarCommand();
				break;
			*/
			default:
				getPanelMainToolbarCommand();
		}
	}
	
	public boolean isBreakRequest() {
		return this.btnBreakRequest.isSelected();
	}
	
	public boolean isBreakResponse() {
		return this.btnBreakResponse.isSelected();
	}
	
	private void resetRequestSerialization (boolean forceSerialize) {
		if (Control.getSingleton() == null) {
			// Still in setup
			return;
		}
		// If forces or either break buttons are pressed force the proxy to submit requests and responses serially 
		if (forceSerialize || btnBreakRequest.isSelected() || btnBreakResponse.isSelected()) {
		    Control.getSingleton().getProxy().setSerialize(true);
		} else {
		    Control.getSingleton().getProxy().setSerialize(true);
		}
	}
	
	private void setBreakRequest() {
		resetRequestSerialization(false);
		
		if (btnBreakRequest.isSelected()) {
			btnBreakRequest.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/105r.png")));
			btnBreakRequest.setToolTipText(Constant.messages.getString("brk.toolbar.button.request.unset"));
		} else {
			btnBreakRequest.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/105.png")));
			btnBreakRequest.setToolTipText(Constant.messages.getString("brk.toolbar.button.request.set"));
		}
	}
	
	private void setBreakResponse() {
		resetRequestSerialization(false);
		
		if (btnBreakResponse.isSelected()) {
			btnBreakResponse.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/106r.png")));
			btnBreakResponse.setToolTipText(Constant.messages.getString("brk.toolbar.button.response.unset"));
		} else {
			btnBreakResponse.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/106.png")));
			btnBreakResponse.setToolTipText(Constant.messages.getString("brk.toolbar.button.response.set"));
		}
	}

 	/**
	 * @return Returns the true if the message (request or response) should be held (ie not submited)
	 */
	public boolean isHoldMessage() {
		if (step) {
			// Only works one time, until its pressed again
			stepping = true;
			step = false;
			return false;
		}
		if (cont) {
			// They've pressed the continue button, stop stepping
			stepping = false;
			resetRequestSerialization(false);
			return false;
		}
		return true;
	}
	
	public boolean isContinue() {
		return cont;
	}
	
	/**
	 * @param isContinue The isContinue to set.
	 */
	private void setContinue(boolean isContinue) {
		this.cont = isContinue;
		
		btnStep.setEnabled( ! isContinue);
		btnContinue.setEnabled( ! isContinue);
		btnDrop.setEnabled( ! isContinue);
		if (isContinue) {
			this.setActiveIcon(false);
		}
	}
	
	private void setActiveIcon(boolean active) {
		if (this.getParent() instanceof TabbedPanel) {
			TabbedPanel parent = (TabbedPanel) this.getParent();
			if (active) {
				parent.setIconAt(
						parent.indexOfComponent(this),
						new ImageIcon(getClass().getResource("/resource/icon/16/101.png")));	// Red X
			} else {
				parent.setIconAt(
						parent.indexOfComponent(this), 
						new ImageIcon(getClass().getResource("/resource/icon/16/101grey.png")));	// Grey X
			}
		}
	}
	
	public void breakPointHit () {
		// This could have been via a break point, so force the serialisation
		resetRequestSerialization(true);

		// Set the active icon and reset the continue button
		this.setActiveIcon(true);
		setContinue(false);
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
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {
			panelCommand = new JPanel();
			panelCommand.setLayout(new BoxLayout(panelCommand,BoxLayout.X_AXIS));
			panelCommand.setName("Command");
			
			Box box = Box.createHorizontalBox();
		
			box.add(Box.createGlue());
			box.add(getBtnBreakRequest());
			box.add(getBtnBreakResponse());
			box.add(getBtnStep());
			box.add(getBtnContinue());
			box.add(getBtnDrop());
			
			panelCommand.add(box);
		}
		return panelCommand;
	}
	
	private void getPanelMainToolbarCommand() {
		View.getSingleton().addMainToolbarButton(this.getBtnBreakRequest());
        View.getSingleton().addMainToolbarButton(this.getBtnBreakResponse());
        View.getSingleton().addMainToolbarButton(this.getBtnStep());
        View.getSingleton().addMainToolbarButton(this.getBtnContinue());
        View.getSingleton().addMainToolbarButton(this.getBtnDrop());
        //View.getSingleton().addMainToolbarSeparator();
	}
	
	

	private JButton getBtnStep() {
		if (btnStep == null) {
			btnStep = new JButton();
			btnStep.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/143.png")));
			btnStep.setToolTipText(Constant.messages.getString("brk.toolbar.button.step"));
			btnStep.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {    
					step = true;
				}
			});
			// Default to disabled
			btnStep.setEnabled(false);
		}
		return btnStep;
	}

	/**
	 * This method initializes btnContinue	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnContinue() {
		if (btnContinue == null) {
			btnContinue = new JButton();
			btnContinue.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/131.png")));
			btnContinue.setToolTipText(Constant.messages.getString("brk.toolbar.button.cont"));
			btnContinue.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {    
					setContinue(true);
				}
			});
			// Default to disabled
			btnContinue.setEnabled(false);
		}
		return btnContinue;
	}

	/**
	 * This method initializes btnDrop	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnDrop() {
		if (btnDrop == null) {
			btnDrop = new JButton();
			btnDrop.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/150.png")));
			btnDrop.setToolTipText(Constant.messages.getString("brk.toolbar.button.bin"));
			btnDrop.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					responsePanel.clearView(false);
					requestPanel.clearView(false);
				    setContinue(true);
				}
			});
			// Default to disabled
			btnDrop.setEnabled(false);
		}
		return btnDrop;
	}

	/**
	 * This method initializes btnContinue	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JToggleButton getBtnBreakRequest() {
		if (btnBreakRequest == null) {
			btnBreakRequest = new JToggleButton();
			setBreakRequest();
			btnBreakRequest.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {    
					// Toggle button
					setBreakRequest();
				}
			});
		}
		return btnBreakRequest;
	}
	
	/**
	 * This method initializes btnContinue	
	 * 	
	 * @return javax.swing.JButton	
	 */ 
	private JToggleButton getBtnBreakResponse() {
		if (btnBreakResponse == null) {
			btnBreakResponse = new JToggleButton();
			setBreakResponse();
			btnBreakResponse.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {    
					// Toggle button
					setBreakResponse();
				}
			});
		}
		return btnBreakResponse;
	}

	public boolean isStepping() {
		return stepping;
	}

	public void setMessage(HttpMessage msg, boolean isRequest) {
	    CardLayout cl = (CardLayout)(this.getLayout());
		
		if (isRequest) {
		    cl.show(this, "request");
			requestPanel.setMessage(msg);
		} else {
			cl.show(this, "response");
			responsePanel.setMessage(msg);
		}
	}

	public void getMessage(HttpMessage msg, boolean isRequest) {
	    CardLayout cl = (CardLayout)(this.getLayout());
		
		if (isRequest) {
			cl.show(this, "request");
			requestPanel.setMessage(msg);
		} else {
			cl.show(this, "response");
			responsePanel.setMessage(msg);
		}
	}

}