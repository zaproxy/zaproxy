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
package org.zaproxy.zap.extension.saverawmessage;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.extension.alert.AlertNode;
import org.zaproxy.zap.extension.ascan.ActiveScanPanel;
import org.zaproxy.zap.extension.bruteforce.BruteForceItem;
import org.zaproxy.zap.extension.bruteforce.BruteForcePanel;
import org.zaproxy.zap.extension.fuzz.FuzzerPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.search.SearchResult;

class PopupMenuSaveRawMessage extends ExtensionPopupMenu {

	private static final long serialVersionUID = -7217818541206464572L;
	
	private static final Logger log = Logger.getLogger(PopupMenuSaveRawMessage.class);

	private static final String POPUP_MENU_LABEL = Constant.messages.getString("saveraw.popup.option");
	private static final String POPUP_MENU_ALL = Constant.messages.getString("saveraw.popup.option.all");
	private static final String POPUP_MENU_BODY = Constant.messages.getString("saveraw.popup.option.body");
	private static final String POPUP_MENU_HEADER = Constant.messages.getString("saveraw.popup.option.header");
	private static final String POPUP_MENU_REQUEST = Constant.messages.getString("saveraw.popup.option.request");
	private static final String POPUP_MENU_RESPONSE = Constant.messages.getString("saveraw.popup.option.response");
	
	private static final String FILE_DESCRIPTION = Constant.messages.getString("saveraw.file.description");
	private static final String ERROR_SAVE = Constant.messages.getString("saveraw.file.save.error");
	private static final String CONFIRM_OVERWRITE = Constant.messages.getString("saveraw.file.overwrite.warning");

	private static enum Invoker {sites, history, alerts, ascan, search, fuzz, bruteforce, httppanel};

	private JTree treeInvoker = null;
    private JList listInvoker = null;
    private HttpPanel httpPanelInvoker = null;
    private Invoker lastInvoker = null;

    private JMenu request;
    private JMenuItem requestHeader;
    private JMenuItem requestBody;
    private JMenuItem requestAll;
    
    private JMenu response;
    private JMenuItem responseHeader;
    private JMenuItem responseBody;
    private JMenuItem responseAll;
    
    private static enum Message {REQUEST_HEADER, REQUEST_BODY, REQUEST_ALL, RESPONSE_HEADER, RESPONSE_BODY, RESPONSE_ALL};
    
    private Message selectedMessage;
    
	public PopupMenuSaveRawMessage() {
		super(POPUP_MENU_LABEL);

		request = new JMenu(POPUP_MENU_REQUEST);
		requestHeader = new JMenuItem(POPUP_MENU_HEADER); 
		requestHeader.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMessage = Message.REQUEST_HEADER;
				saveFile();
			}
		});
		request.add(requestHeader);
		requestBody = new JMenuItem(POPUP_MENU_BODY);
		requestBody.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMessage = Message.REQUEST_BODY;
				saveFile();
			}
		});
		request.add(requestBody);
		request.addSeparator();
		requestAll = new JMenuItem(POPUP_MENU_ALL);
		requestAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMessage = Message.REQUEST_ALL;
				saveFile();
			}
		});
		request.add(requestAll);
		add(request);
		
		response = new JMenu(POPUP_MENU_RESPONSE);
		responseHeader = new JMenuItem(POPUP_MENU_HEADER);
		responseHeader.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMessage = Message.RESPONSE_HEADER;
				saveFile();
			}
		});
		response.add(responseHeader);
		responseBody = new JMenuItem(POPUP_MENU_BODY);
		responseBody.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMessage = Message.RESPONSE_BODY;
				saveFile();
			}
		});
		response.add(responseBody);
		response.addSeparator();
		responseAll = new JMenuItem(POPUP_MENU_ALL);
		responseAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMessage = Message.RESPONSE_ALL;
				saveFile();
			}
		});
		response.add(responseAll);
		add(response);
	}

	private byte[] getBytes() {
		byte[] bytes = new byte[0];
		
		byte[] bytesHeader;
		byte[] bytesBody;
		
		HttpMessage httpMessage = getSelectedHttpMessage();
		
		switch(selectedMessage) {
		case REQUEST_HEADER:
			bytes = httpMessage.getRequestHeader().toString().getBytes();
			break;
		case REQUEST_BODY:
			bytes = httpMessage.getRequestBody().getBytes();
			break;
		case REQUEST_ALL:
			bytesHeader = httpMessage.getRequestHeader().toString().getBytes();
			bytesBody = httpMessage.getRequestBody().getBytes();
			bytes = new byte[bytesHeader.length + bytesBody.length];
			System.arraycopy(bytesHeader, 0, bytes, 0, bytesHeader.length);
			System.arraycopy(bytesBody, 0, bytes, bytesHeader.length, bytesBody.length);
			break;
		case RESPONSE_HEADER:
			bytes = httpMessage.getResponseHeader().toString().getBytes();
			break;
		case RESPONSE_BODY:
			bytes = httpMessage.getResponseBody().getBytes();
			break;
		case RESPONSE_ALL:
			bytesHeader = httpMessage.getResponseHeader().toString().getBytes();
			bytesBody = httpMessage.getResponseBody().getBytes();
			bytes = new byte[bytesHeader.length + bytesBody.length];
			System.arraycopy(bytesHeader, 0, bytes, 0, bytesHeader.length);
			System.arraycopy(bytesBody, 0, bytes, bytesHeader.length, bytesBody.length);
			break;
		}
		
		return bytes;
	}
	
	private HttpMessage getSelectedHttpMessage() {
		HttpMessage httpMessage = null;
    	try {
    		switch (lastInvoker) {
    		case sites:
    		    SiteNode sNode = (SiteNode) treeInvoker.getLastSelectedPathComponent();
    		    if (sNode != null && sNode.getHistoryReference() != null) {
    		    	httpMessage = sNode.getHistoryReference().getHttpMessage();
    		    }
                break;

    		case history:
    			HistoryReference ref = (HistoryReference) listInvoker.getSelectedValue();
    			if (ref != null) {
    				httpMessage = ref.getHttpMessage();
    			}
				break;

    		case alerts:
    			AlertNode aNode = (AlertNode) treeInvoker.getLastSelectedPathComponent();
        	    if (aNode.getUserObject() != null) {
        	        if (aNode.getUserObject() instanceof Alert) {
        	            Alert alert = (Alert) aNode.getUserObject();
        	            httpMessage = alert.getHistoryRef().getHttpMessage();
        	        }
        	    }
				break;
				
    		case ascan:
    		case fuzz:
    			httpMessage = (HttpMessage) listInvoker.getSelectedValue();
				break;
				
    		case search:
        	    SearchResult sr = (SearchResult) listInvoker.getSelectedValue();
        	    if (sr != null) {
        	    	httpMessage = sr.getMessage();
        	    }
				break;
				
    		case bruteforce:
    	    	BruteForceItem bfi = (BruteForceItem) listInvoker.getSelectedValue();
    	    	if (bfi != null) {
    	    		httpMessage = new HistoryReference(bfi.getHistoryId()).getHttpMessage();
    	    	}
				break;
				
    		case httppanel:
    		    org.zaproxy.zap.extension.httppanel.Message message = httpPanelInvoker.getMessage();
    		    if (message instanceof HttpMessage) {
    		        httpMessage = (HttpMessage) message;
    		    }
    			break;
    			
    		}
    		
		} catch (Exception e2) {
			log.error(e2.getMessage(), e2);
		}
    	
    	return httpMessage;
	}
	
	@Override
    public boolean isEnableForComponent(Component invoker) {
    	boolean display = false;
    	if (invoker instanceof JTextComponent) {
            Container c = invoker.getParent();
            while (!(c instanceof JFrame)) {
                c = c.getParent();
                if (c instanceof HttpPanel) {
                	if (!((HttpPanel)c).isEditable()) {
	                	lastInvoker = Invoker.httppanel;
	                	httpPanelInvoker = (HttpPanel)c;
	                    this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
	                	display = true;
                	}
                	break;
                }
            }
    	} else if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
        	this.lastInvoker = Invoker.history;
            this.listInvoker = (JList) invoker;
            this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
            display = true;
        } else if (invoker instanceof JTree && invoker.getName().equals("treeSite")) {
        	this.lastInvoker = Invoker.sites;
        	this.treeInvoker = (JTree) invoker;
            this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
            display = true;
        } else if (invoker.getName() != null && invoker.getName().equals("treeAlert")) {
        	this.lastInvoker = Invoker.alerts;
        	this.treeInvoker = (JTree) invoker;
        	JTree tree = (JTree) invoker;
            if (tree.getLastSelectedPathComponent() != null) {
            	if (tree.getSelectionCount() > 1) {
                	// Note - the Alerts tree only supports single selections
                    this.setEnabled(false);
            	} else {
	                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	                if (!node.isRoot() && node.getUserObject() != null) {
	                    this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
	                } else {
	                    this.setEnabled(false);
	                }
            	}
            }
            display = true;
        } else if (invoker.getName() != null && invoker.getName().equals("listSearch")) {
        	this.lastInvoker = Invoker.search;
            this.listInvoker = (JList) invoker;
            this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
            display = true;
        } else if (invoker.getName() != null && invoker.getName().equals(ActiveScanPanel.PANEL_NAME)) {
        	this.lastInvoker = Invoker.ascan;
            this.listInvoker = (JList) invoker;
            this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
            display = true;
        } else if (invoker.getName() != null && invoker.getName().equals(FuzzerPanel.PANEL_NAME)) {
        	this.lastInvoker = Invoker.fuzz;
            this.listInvoker = (JList) invoker;
            this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
            display = true;
        } else if (invoker.getName() != null && invoker.getName().equals(BruteForcePanel.PANEL_NAME)) {
        	this.lastInvoker = Invoker.bruteforce;
            this.listInvoker = (JList) invoker;
            this.setEnabled(isEnabledForHttpMessage(getSelectedHttpMessage()));
            display = true;
        } else {
        	log.debug("Popup " + this.getName() + 
        			" not enabled for panel " + invoker.getName() + 
        			" class " + invoker.getClass().getName());
        }

        if (display) {
        	return this.isEnableForInvoker(lastInvoker);
        }
       
        return false;
    }

    private boolean isEnabledForHttpMessage (HttpMessage httpMessage) {
    	
    	if (httpMessage != null) {
    		
    		if (httpMessage.getRequestHeader() != null) {
    			requestHeader.setEnabled(!httpMessage.getRequestHeader().isEmpty());
    			requestBody.setEnabled(httpMessage.getRequestBody().length() != 0);
    			requestAll.setEnabled(httpMessage.getRequestBody().length() != 0);
    		} else {
    			request.setEnabled(false);
    		}
    		
    		if (httpMessage.getResponseHeader() != null) {
    			responseHeader.setEnabled(!httpMessage.getResponseHeader().isEmpty());
    			responseBody.setEnabled(httpMessage.getResponseBody().length() != 0);
    			responseAll.setEnabled(httpMessage.getResponseBody().length() != 0);
    		} else {
    			response.setEnabled(false);
    		}
    	}
    	
    	return httpMessage != null;
    }

	private boolean isEnableForInvoker(Invoker invoker) {
		switch (invoker) {
		case alerts:
		case sites:
		case history:
		case ascan:
		case search:
		case fuzz:
		case bruteforce:
		case httppanel:
			return true;
		default:
			return false;
		}
	}

	private void saveFile() {
		File file = getOutputFile();
	    if (file == null) {
	        return;
	    }
	    
	    if (file.exists()) {
            int rc = View.getSingleton().showConfirmDialog(CONFIRM_OVERWRITE);
            if (rc == JOptionPane.CANCEL_OPTION) {
                return;
            }
	    }
            
	    BufferedOutputStream fw = null;
        try {
            fw = new BufferedOutputStream(new FileOutputStream(file));
	        fw.write(getBytes());

        } catch (Exception e1) {
        	View.getSingleton().showWarningDialog(MessageFormat.format(ERROR_SAVE, file.getAbsolutePath()));
        	log.warn(e1.getMessage(), e1);
        } finally {
    	    try {
    	    	if (fw != null) {
    	    		fw.close();
    	    	}
    	    } catch (Exception e2) {
            	log.warn(e2.getMessage(), e2);
    	    }
        }
        
        
	}
	
	private File getOutputFile() {
	    JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
	    chooser.setFileFilter(new RawFileFilter());
		File file = null;
	    int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
    		file = chooser.getSelectedFile();
    		if (file == null) {
    			return file;
    		}
    		Model.getSingleton().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
    		String fileName = file.getAbsolutePath();
    		if (!fileName.endsWith(".raw")) {
    		    fileName += ".raw";
    		    file = new File(fileName);
    		}
    		return file;
    		
	    }
	    return file;
    }
	
	@Override
	public boolean precedeWithSeparator() {
		return true;
	}

	private static final class RawFileFilter extends FileFilter {
		
		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			} else if (file.isFile() && file.getName().endsWith(".raw")) {
				return true;
			}
			return false;
		}
		
		@Override
		public String getDescription() {
			return FILE_DESCRIPTION;
		}
	}
}
