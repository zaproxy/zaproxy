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
 
// ZAP: 2011/04/08 Changed to support clearview() in HttpPanels
// ZAP: 2011/04/08 Changed to use PopupMenuResendMessage
// ZAP: 2011/07/23 Use new add alert popup
// ZAP: 2011/09/06 Fix alert save plus concurrent mod exceptions
// ZAP: 2011/10/23 Fix add note and manage tags dialogs
// ZAP: 2011/11/20 Set order
// ZAP: 2011/12/21 Added 'show in history' popup
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/03/03 Moved popups to stdmenus extension
// ZAP: 2012/03/15 Changed the method getResendDialog to pass the configuration key
//      to the ManualRequestEditorDialog.

package org.parosproxy.paros.extension.history;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.history.AlertAddDialog;
import org.zaproxy.zap.extension.history.HistoryFilterPlusDialog;
import org.zaproxy.zap.extension.history.ManageTagsDialog;
import org.zaproxy.zap.extension.history.NotesAddDialog;
import org.zaproxy.zap.extension.history.PopupMenuAlert;
import org.zaproxy.zap.extension.history.PopupMenuExportURLs;
import org.zaproxy.zap.extension.history.PopupMenuNote;
import org.zaproxy.zap.extension.history.PopupMenuTag;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionHistory extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionHistory";

	private LogPanel logPanel = null;  //  @jve:decl-index=0:visual-constraint="161,134"
	private ProxyListenerLog proxyListener = null;
	private HistoryList historyList = null;
    private String filter = "";
    
	private HistoryFilterDialog filterDialog = null;
	// ZAP: added filter plus dialog
	private HistoryFilterPlusDialog filterPlusDialog = null;
	
	//private PopupMenuDeleteHistory popupMenuDeleteHistory = null;
	private PopupMenuPurgeHistory popupMenuPurgeHistory = null;
	private ManualRequestEditorDialog resendDialog = null;
	
	private PopupMenuExportMessage popupMenuExportMessage = null;
	private PopupMenuExportMessage popupMenuExportMessage2 = null;
    private PopupMenuExportResponse popupMenuExportResponse = null;
    private PopupMenuExportResponse popupMenuExportResponse2 = null;
    private PopupMenuTag popupMenuTag = null;
    // ZAP: Added Export URLs
	private PopupMenuExportURLs popupMenuExportURLs = null;
    // ZAP: Added history notes
    private PopupMenuNote popupMenuNote = null;
	private NotesAddDialog dialogNotesAdd = null;
	private AlertAddDialog dialogAlertAdd = null;
	private ManageTagsDialog manageTags = null;
    private PopupMenuAlert popupMenuAlert = null;
    
	private Logger logger = Logger.getLogger(ExtensionHistory.class);


    /**
     * 
     */
    public ExtensionHistory() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionHistory(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName(NAME);
        this.setOrder(16);

        ExtensionHelp.enableHelpKey(this.getLogPanel(), "ui.tabs.history");

	}
	
	/**
	 * This method initializes logPanel	
	 * 	
	 * @return com.proofsecure.paros.extension.history.LogPanel	
	 */    
	public LogPanel getLogPanel() {
		if (logPanel == null) {
			logPanel = new LogPanel();
			logPanel.setName(Constant.messages.getString("history.panel.title"));	// ZAP: i18n
			// ZAP: Added History (calendar) icon
			logPanel.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/025.png")));	// 'calendar' icon

            logPanel.setExtension(this);
		}
		return logPanel;
	}
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
        extensionHook.addSessionListener(this);
        extensionHook.addProxyListener(getProxyListenerLog());

	    if (getView() != null) {
		    ExtensionHookView pv = extensionHook.getHookView();
		    pv.addStatusPanel(getLogPanel());
		    getLogPanel().setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
		    
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuTag());
            // ZAP: Added history notes
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuNote());

//	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuExportMessage());
//          extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuExportResponse());

	        //extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuDeleteHistory());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPurgeHistory());

	        // same as PopupMenuExport but for File menu
            // ZAP: Move 'export' menu items to Report menu
	        extensionHook.getHookMenu().addReportMenuItem(getPopupMenuExportMessage2());
            extensionHook.getHookMenu().addReportMenuItem(getPopupMenuExportResponse2());
            extensionHook.getHookMenu().addReportMenuItem(getPopupMenuExportURLs());
            
            /*
            if (isEnableForNativePlatform()) {
                // preload for faster loading
                getBrowserDialog();
            }
            */
	    }

	}
	
	public void sessionChanged(final Session session)  {
	    if (EventQueue.isDispatchThread()) {
		    sessionChangedEventHandler(session);

	    } else {
	        
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	        		    sessionChangedEventHandler(session);
	                }
	            });
	        } catch (Exception e) {
	            
	        }
	    }

	    
	}
	
	private void sessionChangedEventHandler(Session session) {
	    getHistoryList().clear();
	    getLogPanel().getListLog().setModel(getHistoryList());
		getView().getRequestPanel().clearView(true);
		getView().getResponsePanel().clearView(false);
		if (session == null) {
			// Closedown
			return;
		}

		try {
		    List list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL);

		    buildHistory(getHistoryList(), list);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	    
	}
	
	
	private ProxyListenerLog getProxyListenerLog() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerLog(getModel(), getView(), getHistoryList());
        }
        return proxyListener;
	}
	
	public HistoryList getHistoryList() {
	    if (historyList == null) {
	        historyList = new HistoryList();
	    }
	    return historyList;
	}
	
	private void searchHistory(String filter, boolean isRequest) {
	    Session session = getModel().getSession();
        
	    synchronized (historyList) {
	        try {
	            List list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL, filter, isRequest);
	            
	            buildHistory(getHistoryList(), list);
	        } catch (SQLException e) {
				logger.error(e.getMessage(), e);
	        }
	    }
	}
	
	private void searchHistory(HistoryFilter historyFilter) {
	    Session session = getModel().getSession();
        
	    synchronized (historyList) {
	        try {
	            List list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL);
	            
	            buildHistory(getHistoryList(), list, historyFilter);
	        } catch (SQLException e) {
				logger.error(e.getMessage(), e);
	        }
	    }
	}
	
	private void buildHistory(HistoryList historyList, List dbList) {

	    HistoryReference historyRef = null;
	    synchronized (historyList) {
	        historyList.clear();
	        
	        for (int i=0; i<dbList.size(); i++) {
	            int historyId = ((Integer) dbList.get(i)).intValue();

	            try {
	            	SiteNode sn = getModel().getSession().getSiteTree().getSiteNode(historyId);
	            	if (sn != null && sn.getHistoryReference() != null && 
	            			sn.getHistoryReference().getHistoryId() == historyId) {
	            		historyRef = sn.getHistoryReference();
	            	} else {
	                    historyRef = new HistoryReference(historyId);
	                    historyRef.setSiteNode(sn);
	            	}
                    historyRef.loadAlerts();
                    historyList.addElement(historyRef);
	                    
	            } catch (Exception e) {
	    			logger.error(e.getMessage(), e);
	            }
	        }
	    }
   }
	
	private void buildHistory(HistoryList historyList, List<Integer> dbList,
			HistoryFilter historyFilter) {
	    HistoryReference historyRef = null;
	    synchronized (historyList) {
	        historyList.clear();
	        
	        for (int i=0; i<dbList.size(); i++) {
	            int historyId = (dbList.get(i)).intValue();

	            try {
                    historyRef = new HistoryReference(historyId);
                    historyRef.loadAlerts();
                    if (historyFilter.matches(historyRef)) {
                    	historyList.addElement(historyRef);
                    }
	            } catch (Exception e) {
	    			logger.error(e.getMessage(), e);
	            }
	        }
	    }
	}

	/**
	 * This method initializes filterDialog	
	 * 	
	 * @return com.proofsecure.paros.extension.history.SearchDialog	
	 */    
	private HistoryFilterDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new HistoryFilterDialog(getView().getMainFrame(), true);
		}
		return filterDialog;
	}
	private HistoryFilterPlusDialog getFilterPlusDialog() {
		if (filterPlusDialog == null) {
			filterPlusDialog = 
				new HistoryFilterPlusDialog(getView().getMainFrame(), true);
		}
		return filterPlusDialog;
	}

	private int showFilterDialog(boolean isRequest) {
		HistoryFilterDialog dialog = getFilterDialog();
		dialog.setModal(true);
		int exit = dialog.showDialog();
		int result = 0;		// cancel, state unchanged
		if (exit == JOptionPane.OK_OPTION) {
		    filter = dialog.getPattern();
		    getProxyListenerLog().setFilter(filter);
		    searchHistory(filter, isRequest);
		    result = 1;		// applied
		    
		} else if (exit == JOptionPane.NO_OPTION) {
		    filter = "";
		    getProxyListenerLog().setFilter(filter);
		    searchHistory(filter, isRequest);
		    result = -1;	// reset
		}
		
		return result;
	}
	
	protected int showFilterPlusDialog() {
		HistoryFilterPlusDialog dialog = getFilterPlusDialog();
		dialog.setModal(true);
    	try {
			dialog.setAllTags(getModel().getDb().getTableTag().getAllTags());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		int exit = dialog.showDialog();
		int result = 0;		// cancel, state unchanged
		HistoryFilter historyFilter = dialog.getFilter();
		if (exit == JOptionPane.OK_OPTION) {
		    getProxyListenerLog().setHistoryFilter(historyFilter);
		    searchHistory(historyFilter);
		    logPanel.setFilterStatus(historyFilter);
		    result = 1;		// applied
		    
		} else if (exit == JOptionPane.NO_OPTION) {
		    getProxyListenerLog().setHistoryFilter(historyFilter);
		    searchHistory(historyFilter);
		    logPanel.setFilterStatus(historyFilter);
		    result = -1;	// reset
		}
		
		return result;
	}
	
	/**
	 * This method initializes popupMenuDeleteHistory	
	 * 	
	 * @return org.parosproxy.paros.extension.history.PopupMenuDeleteHistory	
	 */
	/*
	private PopupMenuDeleteHistory getPopupMenuDeleteHistory() {
		if (popupMenuDeleteHistory == null) {
			popupMenuDeleteHistory = new PopupMenuDeleteHistory();
			popupMenuDeleteHistory.setExtension(this);
		}
		return popupMenuDeleteHistory;
	}
	*/
	/**
	 * This method initializes popupMenuPurgeHistory	
	 * 	
	 * @return org.parosproxy.paros.extension.history.PopupMenuPurgeHistory	
	 */    
	private PopupMenuPurgeHistory getPopupMenuPurgeHistory() {
		if (popupMenuPurgeHistory == null) {
			popupMenuPurgeHistory = new PopupMenuPurgeHistory();
			popupMenuPurgeHistory.setExtension(this);

		}
		return popupMenuPurgeHistory;
	}
	/**
	 * This method initializes resendDialog	
	 * 	
	 * @return org.parosproxy.paros.extension.history.ResendDialog	
	 */    
	public ManualRequestEditorDialog getResendDialog() {
		if (resendDialog == null) {
			resendDialog = new ManualRequestEditorDialog(getView().getMainFrame(), false, true, this, "resend");
			resendDialog.setTitle(Constant.messages.getString("manReq.resend.popup"));	// ZAP: i18n
		}
		return resendDialog;
	}
	
	/**
	 * This method initializes popupMenuExport	
	 * 	
	 * @return org.parosproxy.paros.extension.history.PopupMenuExport	
	 */    
	private PopupMenuExportMessage getPopupMenuExportMessage() {
		if (popupMenuExportMessage == null) {
			popupMenuExportMessage = new PopupMenuExportMessage();
			popupMenuExportMessage.setExtension(this);

		}
		return popupMenuExportMessage;
	}
	/**
	 * This method initializes popupMenuExport1	
	 * 	
	 * @return org.parosproxy.paros.extension.history.PopupMenuExport	
	 */    
	private PopupMenuExportMessage getPopupMenuExportMessage2() {
		if (popupMenuExportMessage2 == null) {
			popupMenuExportMessage2 = new PopupMenuExportMessage();
			popupMenuExportMessage2.setExtension(this);
		}
		return popupMenuExportMessage2;
	}

    /**
     * This method initializes popupMenuExportResponse	
     * 	
     * @return org.parosproxy.paros.extension.history.PopupMenuExportResponse	
     */
    private PopupMenuExportResponse getPopupMenuExportResponse() {
        if (popupMenuExportResponse == null) {
            popupMenuExportResponse = new PopupMenuExportResponse();
            popupMenuExportResponse.setExtension(this);
        }
        return popupMenuExportResponse;
    }

    /**
     * This method initializes popupMenuExportResponse2	
     * 	
     * @return org.parosproxy.paros.extension.history.PopupMenuExportResponse	
     */
    private PopupMenuExportResponse getPopupMenuExportResponse2() {
        if (popupMenuExportResponse2 == null) {
            popupMenuExportResponse2 = new PopupMenuExportResponse();
            popupMenuExportResponse2.setExtension(this);

        }
        return popupMenuExportResponse2;
    }

    private PopupMenuTag getPopupMenuTag() {
        if (popupMenuTag == null) {
            popupMenuTag = new PopupMenuTag();
            popupMenuTag.setExtension(this);

        }
        return popupMenuTag;
    }
    
    private PopupMenuNote getPopupMenuNote() {
        if (popupMenuNote == null) {
            popupMenuNote = new PopupMenuNote();
            popupMenuNote.setExtension(this);

        }
        return popupMenuNote;
    }
    
    private void populateNotesAddDialogAndSetVisible(HistoryReference ref, String note) {
    	dialogNotesAdd.getTxtDisplay().setText(note);
    	dialogNotesAdd.setHistoryRef(ref);
    	dialogNotesAdd.setVisible(true);
    }
    
    public void showNotesAddDialog(HistoryReference ref, String note) {
    	if (dialogNotesAdd == null) {
	    	dialogNotesAdd = new NotesAddDialog(getView().getMainFrame(), false);
	    	dialogNotesAdd.setPlugin(this);
	    	populateNotesAddDialogAndSetVisible(ref, note);
    	} else if (!dialogNotesAdd.isVisible()) {
    		populateNotesAddDialogAndSetVisible(ref, note);
    	}
    }

	public void hideNotesAddDialog() {
		dialogNotesAdd.dispose();
	}
	
    public void showAlertAddDialog(HistoryReference ref) {
		if (dialogAlertAdd == null || ! dialogAlertAdd.isVisible()) {
			dialogAlertAdd = new AlertAddDialog(getView().getMainFrame(), false);
	    	dialogAlertAdd.setPlugin(this);
	    	dialogAlertAdd.setVisible(true);
	    	dialogAlertAdd.setHistoryRef(ref);
		}
    }

    public void showAlertAddDialog(Alert alert) {
		if (dialogAlertAdd == null || ! dialogAlertAdd.isVisible()) {
			dialogAlertAdd = new AlertAddDialog(getView().getMainFrame(), false);
	    	dialogAlertAdd.setPlugin(this);
	    	dialogAlertAdd.setVisible(true);
	    	dialogAlertAdd.setAlert(alert);
		}
    }

	public void hideAlertAddDialog() {
		dialogAlertAdd.dispose();
	}
	
	private void populateManageTagsDialogAndSetVisible(HistoryReference ref, Vector<String> tags) {
		try {
			manageTags.setAllTags(getModel().getDb().getTableTag().getAllTags());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
    	manageTags.setTags(tags);
    	manageTags.setHistoryRef(ref);
    	manageTags.setVisible(true);
	}
	
    public void showManageTagsDialog(HistoryReference ref, Vector<String> tags) {
    	if (manageTags == null) {
	    	manageTags = new ManageTagsDialog(getView().getMainFrame(), false);
	    	manageTags.setPlugin(this);
	    	populateManageTagsDialogAndSetVisible(ref, tags);
    	} else if (!manageTags.isVisible()) {
    		populateManageTagsDialogAndSetVisible(ref, tags);
    	}
    }

	public void hideManageTagsDialog() {
		manageTags.dispose();
	}
	
	private PopupMenuExportURLs getPopupMenuExportURLs() {
		if (popupMenuExportURLs == null) {
			popupMenuExportURLs = new PopupMenuExportURLs();
			popupMenuExportURLs.setExtension(this);
		}
		return popupMenuExportURLs;
	}


	public void showInHistory(HistoryReference href) {
		this.getLogPanel().display(href);
		this.getLogPanel().setTabFocus();
	}
	
	@Override
	public void sessionAboutToChange(Session session) {
	}
}