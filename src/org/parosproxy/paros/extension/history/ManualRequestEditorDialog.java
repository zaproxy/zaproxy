/*
*
* Paros and its related class files.
* 
* Paros is an HTTP/HTTPS proxy for assessing web application security.
* Copyright (C) 2003-2004 Chinotec Technologies Company
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the Clarified Artistic License
* as published by the Free Software Foundation.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* Clarified Artistic License for more details.
* 
* You should have received a copy of the Clarified Artistic License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.parosproxy.paros.extension.history;

import org.parosproxy.paros.model.Model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.AbstractFrame;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.tab.Tab;


/**
*
* Creates:
* 
* +----------------------------+
* | panelHeader                |
* +----------------------------+
* | panelContent               |
* |                            |
* |                            |
* +----------------------------+
* 
*/
public class ManualRequestEditorDialog extends AbstractFrame implements Tab {
	private static final long serialVersionUID = 1L;

	// ZAP: Added logger
    private static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);

    // Window
    private JPanel panelWindow = null; // ZAP
    private JPanel panelHeader = null;
    //private JPanel panelContent = null;
	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;
	private JPanel panelCommand = null;
	// ZAP: Changed panelTab to JSplitPane
	private JComponent panelMain = null;
	private JPanel panelContent = null;
	
	private JCheckBox chkFollowRedirect = null;
	private JCheckBox chkUseTrackingSessionState = null;
	
	private JButton btnSend = null;

	
	// Other
	private HttpSender httpSender = null;
	private boolean isSendEnabled = true;
	// ZAP: Add request to the history pane, c/o Andiparos
	private HistoryList historyList = null;
	private Extension extension = null;
	private HttpMessage httpMessage = null;
	
   /**
    * @throws HeadlessException
    */
	public ManualRequestEditorDialog() throws HeadlessException {
	   super();
	   initialize();
   }

   /**
    * @param arg0
    * @param arg1
    * @throws HeadlessException
    */
   public ManualRequestEditorDialog(Frame parent, boolean modal, boolean isSendEnabled, Extension extension) throws HeadlessException {
	   super();
       this.isSendEnabled = isSendEnabled;
       this.extension = extension;
       initialize();
   }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
	    this.requestPanel = getRequestPanel();

	    this.addWindowListener(new java.awt.event.WindowAdapter() { 
	    	public void windowClosing(java.awt.event.WindowEvent e) {
	    	    getHttpSender().shutdown();
	    	    getResponsePanel().clearView(false);
	    	}
	    });

	    this.setContentPane(getWindowPanel());
	    
	    this.historyList = ((ExtensionHistory)Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory")).getHistoryList();
	}
		
	private JPanel getWindowPanel() {
		if (panelWindow == null) {
			panelWindow = new JPanel();
			panelWindow.setLayout(new BorderLayout());
			
			panelHeader = getPanelHeader();
			panelWindow.add(panelHeader, BorderLayout.NORTH);
			
			panelContent = getPanelContent();
			panelWindow.add(panelContent);
		}
		
		return panelWindow;
	}
	
	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();

			GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1b = new GridBagConstraints();
			
			panelHeader.setLayout(new GridBagLayout());
			panelHeader.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

			gridBagConstraints0.gridx = 1;
			gridBagConstraints0.gridy = 0;
			gridBagConstraints0.ipadx = 0;
			gridBagConstraints0.ipady = 0;
			gridBagConstraints0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints0.weightx = 1.0D;
			
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			
			gridBagConstraints1b.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints1b.gridx = 2;
			gridBagConstraints1b.gridy = 0;
			gridBagConstraints1b.insets = new java.awt.Insets(0,0,0,0);
			
			gridBagConstraints2.gridx = 3;
			gridBagConstraints2.gridy = 0;
			
			gridBagConstraints3.gridx = 4;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHEAST;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			
			panelHeader.add(getChkUseTrackingSessionState(), gridBagConstraints1);
			panelHeader.add(getChkFollowRedirect(), gridBagConstraints2);
			panelHeader.add(getBtnSend(), gridBagConstraints3);
		}
		
		return panelHeader;
	}
	
	/**
	 * This method initializes requestPanel	
	 * 	
	 * @return org.parosproxy.paros.view.HttpPanel	
	 */    
	private HttpPanelRequest getRequestPanel() {
		if (requestPanel == null) {

			requestPanel = new HttpPanelRequest(true, extension);
		}
		return requestPanel;
	}
	
	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */    
	private JComponent getPanelTab() {
		JSplitPane splitPane = null;
		Dimension frameSize = null;
		
		if (panelMain == null) {
			switch(Model.getSingleton().getOptionsParam().getViewParam().getEditorViewOption()) {
			case 0:
				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getRequestPanel(), getResponsePanel());
				this.panelMain = (JComponent) splitPane;
				panelMain.setDoubleBuffered(true);
				
				frameSize = this.getSize();
				splitPane.setDividerLocation(frameSize.height / 2);
				
				break;
				
			case 1:
				splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getRequestPanel(), getResponsePanel());
				this.panelMain = (JComponent) splitPane;
				panelMain.setDoubleBuffered(true);
				
				frameSize = this.getSize();
				splitPane.setDividerLocation(frameSize.width / 2);
				
				this.panelMain = (JComponent) new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getRequestPanel(), getResponsePanel());
				panelMain.setDoubleBuffered(true);
				break;
				
			case 2:
				JTabbedPane tabbedPane = new JTabbedPane(); 
				tabbedPane.addTab(Constant.messages.getString("manReq.tab.request"), null, getRequestPanel(), null);
				tabbedPane.addTab(Constant.messages.getString("manReq.tab.response"), null, getResponsePanel(), null);
				this.panelMain = (JComponent) tabbedPane;
				break;
				
			default:
				this.panelMain = (JComponent) new JSplitPane(JSplitPane.VERTICAL_SPLIT, getRequestPanel(), getResponsePanel());
				panelMain.setDoubleBuffered(true);
			}
		}
		
		return panelMain;
	}
	
	/**
	 * This method initializes httpPanel	
	 * 	
	 * @return org.parosproxy.paros.view.HttpPanel	
	 */    
	private HttpPanelResponse getResponsePanel() {
		if (responsePanel == null) {
			responsePanel = new HttpPanelResponse(false, extension);
		}
		return responsePanel;
	}

   public void setExtension(Extension extension) {
	   requestPanel.setExtension(extension);
	   responsePanel.setExtension(extension);
   }
   
   public void setVisible(boolean show) {
       if (show) {
           try {
               if (httpSender != null) {
                   httpSender.shutdown();
                   httpSender = null;
               }
           } catch (Exception e) {
        	   // ZAP: Log exceptions
        	   log.error(e.getMessage(), e);
           }
       }
       
       switchToTab(0);
       
       boolean isSessionTrackingEnabled = Model.getSingleton().getOptionsParam().getConnectionParam().isHttpStateEnabled();
       getChkUseTrackingSessionState().setEnabled(isSessionTrackingEnabled);
       super.setVisible(show);

   }
	
   private HttpSender getHttpSender() {
       if (httpSender == null) {
           httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), getChkUseTrackingSessionState().isSelected());
           
       }
       return httpSender;
   }
   
   /* Set new HttpMessage
    * this means ManualRequestEditor does show another HttpMessage.
    * Copy the message (this is not a viewer. User will modify it),
    * and update Request/Response views.
    */
   public void setMessage(HttpMessage msg) {
	   if (msg == null) {
		   System.out.println("Manual: set message NULL");
		   return;
	   }
	   
	   this.httpMessage = msg.cloneAll();
	   
       getRequestPanel().setMessage(httpMessage);
       getResponsePanel().setMessage(httpMessage);
       switchToTab(0);
   }
   
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelContent() {
		if (panelContent == null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			panelContent = new JPanel();
			panelContent.setLayout(new GridBagLayout());
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHWEST;
			panelContent.add(getPanelTab(), gridBagConstraints31);
		}
		return panelContent;
	}
	
	/**
	 * This method initializes chkFollowRedirect	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkFollowRedirect() {
		if (chkFollowRedirect == null) {
			chkFollowRedirect = new JCheckBox();
			chkFollowRedirect.setText(Constant.messages.getString("manReq.checkBox.followRedirect"));
			chkFollowRedirect.setSelected(true);
		}
		return chkFollowRedirect;
	}
	
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkUseTrackingSessionState() {
		if (chkUseTrackingSessionState == null) {
			chkUseTrackingSessionState = new JCheckBox();
			chkUseTrackingSessionState.setText(Constant.messages.getString("manReq.checkBox.useSession"));
		}
		return chkUseTrackingSessionState;
	}
    
    private void addHistory(HttpMessage msg, int type) {
        HistoryReference historyRef = null;
        try {
	        historyRef = new HistoryReference(Model.getSingleton().getSession(), type, msg);
	        synchronized (historyList) {
	        	if (type == HistoryReference.TYPE_MANUAL) {
	        		addHistoryInEventQueue(historyRef);
	        		historyList.notifyItemChanged(historyRef);
	        	}
	        }
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    }

    private void addHistoryInEventQueue(final HistoryReference ref) {
        if (EventQueue.isDispatchThread()) {
                historyList.addElement(ref);
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                	public void run() {
                		historyList.addElement(ref);
                	}
                });
            } catch (Exception e) {
            	log.error(e.getMessage(), e);
            }
        }
    }
    
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnSend() {
		if (btnSend == null) {
			btnSend = new JButton();
			btnSend.setText(Constant.messages.getString("manReq.button.send"));		// ZAP: i18n
			btnSend.setEnabled(isSendEnabled);
			btnSend.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					btnSendAction();
				}
			});
		}
		return btnSend;
	}
	
	private void btnSendAction() {
        btnSend.setEnabled(false);
        
        // Get current HttpMessage
        requestPanel.saveData();
        HttpMessage msg = requestPanel.getHttpMessage();
        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
        
        // Send Request, Receive Response
        send(msg);
        
        // redraw request, as it could have changed 
        requestPanel.updateContent();
        
        btnSend.setEnabled(true);
	}
	
    private void send(final HttpMessage msg) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    getHttpSender().sendAndReceive(msg, getChkFollowRedirect().isSelected());
                    
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
	                        if (!msg.getResponseHeader().isEmpty()) {
	                        	// Indicate UI new response arrived
	                            switchToTab(1);
	                        	responsePanel.updateContent();
	                            
	                            final int finalType = HistoryReference.TYPE_MANUAL;
	                            Thread t = new Thread(new Runnable() {
	                            	public void run() {
	                            		addHistory(msg, finalType);
	                            	}
	                            });
	                            t.start();
	                        }
                        }
                    });
                } catch (NullPointerException npe) {
                    requestPanel.getExtension().getView().showWarningDialog("Malformed header error.");                      
                } catch (HttpMalformedHeaderException mhe) {
                	requestPanel.getExtension().getView().showWarningDialog("Malformed header error.");                      
                } catch (IOException ioe) {
                	requestPanel.getExtension().getView().showWarningDialog("IO error in sending request.");
                } catch (Exception e) {
                	// ZAP: Log exceptions
                	log.error(e.getMessage(), e);
                } finally {
                    btnSend.setEnabled(true);
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

	private void switchToTab(int i) {
        if (Model.getSingleton().getOptionsParam().getViewParam().getEditorViewOption() == 2) {
     	   JTabbedPane tab = (JTabbedPane) getPanelTab();
     	   tab.setSelectedIndex(i);
        }
	}
}
