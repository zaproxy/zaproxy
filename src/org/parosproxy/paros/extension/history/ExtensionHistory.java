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
// to the ManualRequestEditorDialog.
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/24 Added type arguments to generic types, removed unnecessary
// cast and added @Override annotation to all appropriate methods.
// ZAP: 2012/04/28 Added log of exception.
// ZAP: 2012/05/31 Issue 308 NPE in sessionChangedEventHandler in daemon mode
// ZAP: 2012/07/02 Added the method showAlertAddDialog(HttpMessage, int).
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event and removed access to some UI elements
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/04/14 Issue 588: ExtensionHistory.historyIdToRef should be cleared when changing session
// ZAP: 2013/04/14 Issue 598: Replace/update "old" pop up menu items
// ZAP: 2013/07/14 Issue 725: Clear alert's panel fields

package org.parosproxy.paros.extension.history;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.extension.manualrequest.http.impl.ManualHttpRequestEditorDialog;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.history.AlertAddDialog;
import org.zaproxy.zap.extension.history.HistoryFilterPlusDialog;
import org.zaproxy.zap.extension.history.ManageTagsDialog;
import org.zaproxy.zap.extension.history.NotesAddDialog;
import org.zaproxy.zap.extension.history.PopupMenuExportURLs;
import org.zaproxy.zap.extension.history.PopupMenuNote;
import org.zaproxy.zap.extension.history.PopupMenuPurgeHistory;
import org.zaproxy.zap.extension.history.PopupMenuTag;

public class ExtensionHistory extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionHistory";

	private LogPanel logPanel = null;  //  @jve:decl-index=0:visual-constraint="161,134"
	private ProxyListenerLog proxyListener = null;
	private HistoryList historyList = null;
    
	// ZAP: added filter plus dialog
	private HistoryFilterPlusDialog filterPlusDialog = null;
	
	private PopupMenuPurgeHistory popupMenuPurgeHistory = null;
	private ManualRequestEditorDialog resendDialog = null;
	
	private PopupMenuExportMessage popupMenuExportMessage2 = null;
    private PopupMenuExportResponse popupMenuExportResponse2 = null;
    private PopupMenuTag popupMenuTag = null;
    // ZAP: Added Export URLs
	private PopupMenuExportURLs popupMenuExportURLs = null;
    // ZAP: Added history notes
    private PopupMenuNote popupMenuNote = null;
	private NotesAddDialog dialogNotesAdd = null;
	private AlertAddDialog dialogAlertAdd = null;
	private ManageTagsDialog manageTags = null;
	
	private boolean showJustInScope = false;
	
	// Used to cache hrefs not added into the historyList
	private Hashtable<Integer, HistoryReference> historyIdToRef = new Hashtable<Integer, HistoryReference>();

    
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
	 */
	private void initialize() {
        this.setName(NAME);
        this.setOrder(16);

        ExtensionHelp.enableHelpKey(this.getLogPanel(), "ui.tabs.history");

	}
	
	/**
	 * This method initializes logPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.history.LogPanel	
	 */    
	private LogPanel getLogPanel() {
		if (logPanel == null) {
			logPanel = new LogPanel();
			logPanel.setName(Constant.messages.getString("history.panel.title"));	// ZAP: i18n
			// ZAP: Added History (calendar) icon
			logPanel.setIcon(new ImageIcon(ExtensionHistory.class.getResource("/resource/icon/16/025.png")));	// 'calendar' icon

            logPanel.setExtension(this);
		}
		return logPanel;
	}
	
	public void clearLogPanelDisplayQueue() {
		this.getLogPanel().clearDisplayQueue();
	}
	
	public HistoryReference getSelectedHistoryReference () {
		return getLogPanel().getListLog().getSelectedValue();
	}
	
	public List<HistoryReference> getSelectedHistoryReferences () {
		return getLogPanel().getListLog().getSelectedValuesList();
	}
	
	@Override
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

	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPurgeHistory());

	        // same as PopupMenuExport but for File menu
            // ZAP: Move 'export' menu items to Report menu
	        extensionHook.getHookMenu().addReportMenuItem(getPopupMenuExportMessage2());
            extensionHook.getHookMenu().addReportMenuItem(getPopupMenuExportResponse2());
            extensionHook.getHookMenu().addReportMenuItem(getPopupMenuExportURLs());
	    }

	}
	
	@Override
	public void sessionChanged(final Session session)  {
	    if (EventQueue.isDispatchThread()) {
		    sessionChangedEventHandler(session);
	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                @Override
	                public void run() {
	        		    sessionChangedEventHandler(session);
	                }
	            });
	        } catch (Exception e) {
	            // ZAP: Added logging.
	            logger.error(e.getMessage(), e);
	        }
	    }
	}
	
	private void sessionChangedEventHandler(Session session) {
	    getHistoryList().clear();
	    historyIdToRef.clear();
	    getLogPanel().getListLog().setModel(getHistoryList());
	    if (getView() != null) { 
	    	getView().getRequestPanel().clearView(true);
	    	getView().getResponsePanel().clearView(false);
	    }
		if (session == null) {
			// Closedown
			return;
		}

		try {
		    // ZAP: Added type argument.
		    List<Integer> list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL);

		    buildHistory(getHistoryList(), list);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	    
	}
	
	
	private ProxyListenerLog getProxyListenerLog() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerLog(getModel(), getView(), this);
        }
        return proxyListener;
	}
	
	private HistoryList getHistoryList() {
	    if (historyList == null) {
	        historyList = new HistoryList();
	    }
	    return historyList;
	}
	
	public void removeFromHistoryList(HistoryReference href) {
		this.getHistoryList().removeElement(href);
	}
	
	public void notifyHistoryItemChanged(HistoryReference href) {
		this.getHistoryList().notifyItemChanged(href);
	}
	
	public HistoryReference getHistoryReference (int historyId) {
		HistoryReference href = getHistoryList().getHistoryReference(historyId);
		if (href != null) {
			return href;
		}
		href = historyIdToRef.get(historyId);
		if (href == null) {		
			try {
				href = new HistoryReference(historyId);
				hack(href);
			} catch (Exception e) {
				return null;
			}
		}
		return href;
		
	}
	
    public void addHistory (HttpMessage msg, int type) {
        try {
			this.addHistory(new HistoryReference(Model.getSingleton().getSession(), type, msg));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
		}
    }
    
    private void hack (HistoryReference historyRef) {
    	historyIdToRef.put(historyRef.getHistoryId(), historyRef);
    }
    
    public void addHistory (HistoryReference historyRef) {
        try {
            synchronized (getHistoryList()) {
                if (historyRef.getHistoryType() == HistoryReference.TYPE_MANUAL) {
	            	if (this.showJustInScope && ! getModel().getSession().isInScope(
	            			historyRef.getURI().toString())) {
	            		// Not in scope
	            		hack(historyRef);
	            		return;
	            	}
	        	    if (getView() != null) { 
	        	    	// Dont do this in daemon mode 
		        		HistoryFilterPlusDialog dialog = getFilterPlusDialog();
		        		HistoryFilter historyFilter = dialog.getFilter();
	                    if (historyFilter != null && !historyFilter.matches(historyRef)) {
		            		// Not in filter
		            		hack(historyRef);
		            		return;
	                    }
	
	                	addHistoryInEventQueue(historyRef);
	                    historyList.notifyItemChanged(historyRef);
	        	    }
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addHistoryInEventQueue(final HistoryReference ref) {
        if (EventQueue.isDispatchThread()) {
            historyList.addElement(ref);
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        historyList.addElement(ref);
                    }
                });
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

	
	private void searchHistory(HistoryFilter historyFilter) {
	    Session session = getModel().getSession();
        
	    synchronized (historyList) {
	        try {
	            // ZAP: Added type argument.
	            List<Integer> list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL);
	            
	            buildHistory(getHistoryList(), list, historyFilter);
	        } catch (SQLException e) {
				logger.error(e.getMessage(), e);
	        }
	    }
	}
	
	private void buildHistory(HistoryList historyList, List<Integer> dbList) {
		buildHistory(historyList, dbList, null);
	}
	
	private void buildHistory(HistoryList historyList, List<Integer> dbList,
			HistoryFilter historyFilter) {
	    HistoryReference historyRef = null;
	    synchronized (historyList) {
	        historyList.clear();
	        
	        for (int i=0; i<dbList.size(); i++) {
	            int historyId = (dbList.get(i)).intValue();

	            try {
	            	SiteNode sn = getModel().getSession().getSiteTree().getSiteNode(historyId);
	            	if (sn != null && sn.getHistoryReference() != null && 
	            			sn.getHistoryReference().getHistoryId() == historyId) {
	            		historyRef = sn.getHistoryReference();
	            	} else {
	                    historyRef = new HistoryReference(historyId);
	                    historyRef.setSiteNode(sn);
	            	}
	            	if (this.showJustInScope && ! getModel().getSession().isInScope(
	            			historyRef.getURI().toString())) {
	            		// Not in scope
	            		continue;
	            	}
                    if (historyFilter != null && !historyFilter.matches(historyRef)) {
	            		// Not in filter
	            		continue;
                    }
                    historyRef.loadAlerts();
                    historyList.addElement(historyRef);
                    
	            } catch (Exception e) {
	    			logger.error(e.getMessage(), e);
	            }
	        }
	    }
	}

	private HistoryFilterPlusDialog getFilterPlusDialog() {
		if (filterPlusDialog == null) {
			filterPlusDialog = 
				new HistoryFilterPlusDialog(getView().getMainFrame(), true);
		}
		return filterPlusDialog;
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
		    searchHistory(historyFilter);
		    logPanel.setFilterStatus(historyFilter);
		    result = 1;		// applied
		    
		} else if (exit == JOptionPane.NO_OPTION) {
		    searchHistory(historyFilter);
		    logPanel.setFilterStatus(historyFilter);
		    result = -1;	// reset
		}
		
		return result;
	}
	
	private PopupMenuPurgeHistory getPopupMenuPurgeHistory() {
		if (popupMenuPurgeHistory == null) {
			popupMenuPurgeHistory = new PopupMenuPurgeHistory(this);
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
			resendDialog = new ManualHttpRequestEditorDialog(true, "resend");
			resendDialog.setTitle(Constant.messages.getString("manReq.resend.popup"));	// ZAP: i18n
		}
		return resendDialog;
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
            popupMenuTag = new PopupMenuTag(this);

        }
        return popupMenuTag;
    }
    
    private PopupMenuNote getPopupMenuNote() {
        if (popupMenuNote == null) {
            popupMenuNote = new PopupMenuNote(this);

        }
        return popupMenuNote;
    }
    
    private void populateNotesAddDialogAndSetVisible(HistoryReference ref, String note) {
    	dialogNotesAdd.setNote(note);
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

    /**
     * Sets the {@code HttpMessage} and the history type of the
     * {@code HistoryReference} that will be created if the user creates the
     * alert. The current session will be used to create the
     * {@code HistoryReference}. The alert created will be added to the newly
     * created {@code HistoryReference}.
     * <p>
     * Should be used when the alert is added to a temporary
     * {@code HistoryReference} as the temporary {@code HistoryReference}s are
     * deleted when the session is closed.
     * </p>
     * 
     * @param httpMessage
     *            the {@code HttpMessage} that will be used to create the
     *            {@code HistoryReference}, must not be {@code null}
     * @param historyType
     *            the type of the history reference that will be used to create
     *            the {@code HistoryReference}
     * 
     * @see Model#getSession()
     * @see HistoryReference#HistoryReference(org.parosproxy.paros.model.Session,
     *      int, HttpMessage)
     */
    // ZAP: Added the method.
    public void showAlertAddDialog(HttpMessage httpMessage, int historyType) {
        if (dialogAlertAdd == null || ! dialogAlertAdd.isVisible()) {
            dialogAlertAdd = new AlertAddDialog(getView().getMainFrame(), false);
            dialogAlertAdd.setPlugin(this);
            dialogAlertAdd.setHttpMessage(httpMessage, historyType);
            dialogAlertAdd.setVisible(true);
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

	private void populateManageTagsDialogAndSetVisible(HistoryReference ref, List<String> tags) {
		try {
			manageTags.setAllTags(getModel().getDb().getTableTag().getAllTags());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
    	manageTags.setTags(tags);
    	manageTags.setHistoryRef(ref);
    	manageTags.setVisible(true);
	}
	
    public void showManageTagsDialog(HistoryReference ref, List<String> tags) {
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
	
	@Override
	public String getAuthor() {
		return Constant.PAROS_TEAM;
	}

	public boolean isShowJustInScope() {
		return showJustInScope;
	}

	public void setShowJustInScope(boolean showJustInScope) {
		this.showJustInScope = showJustInScope;
		// Refresh with the next option
	    searchHistory(getFilterPlusDialog().getFilter());
	}

	@Override
	public void sessionScopeChanged(Session session) {
		if (getView() != null) {
			searchHistory(getFilterPlusDialog().getFilter());
		} else {
			searchHistory(null);
		}
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}
}