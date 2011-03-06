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

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.HttpPanel;
/**
*
* To change the template for this generated type comment go to
* Window - Preferences - Java - Code Generation - Code and Comments
*/
public class ManualRequestEditorDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	// ZAP: Added logger 
    private static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);

	private HttpPanel requestPanel = null;
	private JPanel panelCommand = null;
	private JButton btnSend = null;
	private JLabel jLabel = null;  //  @jve:decl-index=0:
	private JTabbedPane panelTab = null;
	private HttpPanel responsePanel = null;
	private Extension extension = null;
	private HttpSender httpSender = null;
	private boolean isSendEnabled = true;
	// ZAP: Add request to the history pane, c/o Andiparos
	private HistoryList historyList = null;

	private JPanel jPanel = null;
	private JCheckBox chkFollowRedirect = null;
	private JCheckBox chkUseTrackingSessionState = null;
	// ZAP: Change method pulldown 
	private JComboBox comboChangeMethod = null;
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
       super(parent, modal);
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
	    getRequestPanel().getPanelOption().add(getPanelCommand(), "");

	    this.addWindowListener(new java.awt.event.WindowAdapter() { 
	    	public void windowClosing(java.awt.event.WindowEvent e) {
	    	    getHttpSender().shutdown();
	    	    getResponsePanel().setMessage("","", false);
	    	}
	    });

	    this.setContentPane(getJPanel());
	    //this.setVisible(true);
	    
	    this.historyList = ((ExtensionHistory)Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory")).getHistoryList();
	}
	
	/**
	 * This method initializes requestPanel	
	 * 	
	 * @return org.parosproxy.paros.view.HttpPanel	
	 */    
	public HttpPanel getRequestPanel() {
		if (requestPanel == null) {
			requestPanel = new HttpPanel(true);
		}
		return requestPanel;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelCommand() {
		if (panelCommand == null) {
			panelCommand = new JPanel();
			GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1b = new GridBagConstraints();
			jLabel = new JLabel();
			panelCommand.setLayout(new GridBagLayout());
			jLabel.setText("");
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints0.gridx = 1;
			gridBagConstraints0.gridy = 0;
			gridBagConstraints0.ipadx = 0;
			gridBagConstraints0.ipady = 0;
			gridBagConstraints0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints0.weightx = 1.0D;
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
			panelCommand.add(jLabel, gridBagConstraints0);
			panelCommand.add(getComboChangeMethod(), gridBagConstraints1);
			panelCommand.add(getChkUseTrackingSessionState(), gridBagConstraints1b);
			panelCommand.add(getChkFollowRedirect(), gridBagConstraints2);
			panelCommand.add(getBtnSend(), gridBagConstraints3);
		}
		return panelCommand;
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
                    btnSend.setEnabled(false);
			        HttpMessage msg = new HttpMessage();
			        getRequestPanel().getMessage(msg, true);
			        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
			        send(msg);
				    
				}
			});
		}
		return btnSend;
	}
	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */    
	private JTabbedPane getPanelTab() {
		if (panelTab == null) {
			panelTab = new JTabbedPane();
			panelTab.setDoubleBuffered(true);
			panelTab.addTab(Constant.messages.getString("manReq.tab.request"), null, getRequestPanel(), null);
			panelTab.addTab(Constant.messages.getString("manReq.tab.response"), null, getResponsePanel(), null);
		}
		return panelTab;
	}
	/**
	 * This method initializes httpPanel	
	 * 	
	 * @return org.parosproxy.paros.view.HttpPanel	
	 */    
	public HttpPanel getResponsePanel() {
		if (responsePanel == null) {
			responsePanel = new HttpPanel(false);
		}
		return responsePanel;
	}

   public void setExtension(Extension extension) {
       this.extension = extension;
   }
   
   private Extension getExtention() {
       return extension;
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
           getPanelTab().setSelectedIndex(0);
       }

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
   
   public void setMessage(HttpMessage msg) {
       getPanelTab().setSelectedIndex(0);
       getRequestPanel().setMessage(msg, true);
       getResponsePanel().setMessage("", "", false);
       getBtnSend().setEnabled(true);
       
   }
   
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHWEST;
			jPanel.add(getPanelTab(), gridBagConstraints31);
		}
		return jPanel;
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
    
    private void send(final HttpMessage msg) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    getHttpSender().sendAndReceive(msg, getChkFollowRedirect().isSelected());
                    
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
	                        if (!msg.getResponseHeader().isEmpty()) {
	                            getResponsePanel().setMessage(msg, false);
	                            
	                            final int finalType = HistoryReference.TYPE_MANUAL;
	                            Thread t = new Thread(new Runnable() {
	                            	public void run() {
	                            		addHistory(msg, finalType);
	                            	}
	                            });
	                            t.start();
	                        }
                        getPanelTab().setSelectedIndex(1);
                        }
                    });
                } catch (NullPointerException npe) {
                    getExtention().getView().showWarningDialog("Malformed header error.");                      
                } catch (HttpMalformedHeaderException mhe) {
                    getExtention().getView().showWarningDialog("Malformed header error.");                      
                } catch (IOException ioe) {
                    getExtention().getView().showWarningDialog("IO error in sending request.");
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
    
    private JComboBox getComboChangeMethod() {
    	if (comboChangeMethod == null) {
    		comboChangeMethod = new JComboBox();
    		comboChangeMethod.setEditable(false);
    		comboChangeMethod.addItem(Constant.messages.getString("manReq.pullDown.method"));
    		comboChangeMethod.addItem(HttpRequestHeader.CONNECT);
    		comboChangeMethod.addItem(HttpRequestHeader.DELETE);
    		comboChangeMethod.addItem(HttpRequestHeader.GET);
    		comboChangeMethod.addItem(HttpRequestHeader.HEAD);
    		comboChangeMethod.addItem(HttpRequestHeader.OPTIONS);
    		comboChangeMethod.addItem(HttpRequestHeader.POST);
    		comboChangeMethod.addItem(HttpRequestHeader.PUT);
    		comboChangeMethod.addItem(HttpRequestHeader.TRACE);
    		comboChangeMethod.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (comboChangeMethod.getSelectedIndex() > 0) {
						changeMethod((String) comboChangeMethod.getSelectedItem());
						comboChangeMethod.setSelectedIndex(0);
					}
				}});
    	}
    	
    	return this.comboChangeMethod;
    }
    
    private void changeMethod(String method) {
    	HttpPanel reqPanel = getRequestPanel();
    	String header = reqPanel.getTxtHeader().getText();
    	try {
			HttpRequestHeader hrh = new HttpRequestHeader(header);
			URI uri = hrh.getURI();
			String body = reqPanel.getTxtBody().getText();
			String prevMethod = hrh.getMethod();
			if (prevMethod.equalsIgnoreCase(method)) {
				return;
			}
			if (prevMethod.equals(HttpRequestHeader.POST)) {
				// Was POST, move all params onto the URL
				if (body != null && body.length() > 0) {
					StringBuffer sb = new StringBuffer();
					if (uri.getQuery() != null) {
						sb.append(uri.getQuery());
					}
					
					String [] params = body.split("&");
					for (String param : params) {
						if (sb.length() > 0) {
							sb.append("&");
						}
						String[] nv = param.split("=");
						if (nv.length == 1) {
							// This effectively strips out the equals if theres no value 
							sb.append(nv[0]);
						} else {
							sb.append(param);
						}
					}
					uri.setQuery(sb.toString());

				}
				hrh.setURI(uri);
				// Clear the body
				body = "";
				
			} else if (method.equals(HttpRequestHeader.POST)) {
				// To be a port, move all URL query params into the body
				String query = uri.getQuery();
				if (query != null) {
					StringBuffer sb = new StringBuffer();
					String [] params = query.split("&");
					for (String param : params) {
						if (sb.length() > 0) {
							sb.append("&");
						}
						sb.append(param);
						String[] nv = param.split("=");
						if (nv.length == 1) {
							// Cope with URL params with no values e.g. http://www.example.com/test?key
							sb.append("=");
						}
					}
					body = sb.toString();
					uri.setQuery(null);
					hrh.setURI(uri);
				}
			}
			hrh.setMethod(method);
			
			reqPanel.setMessage(hrh.toString(), body, true);
			
		} catch (HttpMalformedHeaderException e) {
			// Ignore?
			log.error(e.getMessage(), e);
		} catch (URIException e) {
			log.error(e.getMessage(), e);
		}
    	
    	
    }
}
