package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.alert.ExtensionAlert;

public class PassiveScanThread extends Thread implements ProxyListener, SessionChangedListener {

	private static final Logger logger = Logger.getLogger(PassiveScanThread.class);

    //Could be after the last one that saves the HttpMessage, as this ProxyListener doesn't change the HttpMessage.
	public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;
	
	@SuppressWarnings("unused")
	private OptionsPassiveScan options = null;
	private PassiveScannerList scannerList = null;
	private int currentId = 1;
	private int lastId = -1;
	private int mainSleep = 5000;
	private int postSleep = 200;
	private boolean shutDown = false;
	
	private ExtensionHistory extHist = null; 

	private TableHistory historyTable = null;
	private HistoryReference href = null;

	public PassiveScanThread (PassiveScannerList passiveScannerList) {
		super("ZAP-PassiveScanner");
		this.setDaemon(true);
		
		this.scannerList = passiveScannerList;
		
		MicrosoftTagTypes.register();
		PHPTagTypes.register();
		PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags otherwise they override processing instructions
		MasonTagTypes.register();

	}
	
	@Override
	public void run() {
		historyTable = Database.getSingleton().getTableHistory();
		// Get the last id - in case we've just opened an existing session
		currentId = historyTable.lastIndex();
		
		while (!shutDown) {
			try {
				if (href != null || lastId > currentId ) {
					currentId ++;
				} else {
					// Either just started or there are no new records 
					try {
						Thread.sleep(mainSleep);
						if (shutDown) {
							return;
						}
						lastId = historyTable.lastIndex();
					} catch (InterruptedException e) {
						// New URL, but give it a chance to be processed first
						try {
							Thread.sleep(postSleep);
						} catch (InterruptedException e2) {
							// Ignore
						}
					}
				}
				try {
					href = this.getExtensionHistory().getHistoryReference(currentId);
					//historyRecord = historyTable.read(currentId);
				} catch (Exception e) {
					if (shutDown) {
						return;
					}
					logger.error("Failed to read record " + currentId + " from History table", e);
				}

				if (href != null && 
						(href.getHistoryType() == HistoryReference.TYPE_MANUAL ||
						href.getHistoryType() == HistoryReference.TYPE_SPIDER)) {
					// Note that scanning TYPE_SCANNER records will result in a loop ;)
					// Parse the record
					HttpMessage msg = href.getHttpMessage();
					String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
					Source src = new Source(response);
					
					for (PassiveScanner scanner : scannerList.list()) {
						try {
							if (scanner.isEnabled()) {
								scanner.setParent(this);
								scanner.scanHttpRequestSend(msg, href.getHistoryId());
								scanner.scanHttpResponseReceive(href.getHttpMessage(), href.getHistoryId(), src);
							}
							if (shutDown) {
								return;
							}
						} catch (Exception e) {
							if (shutDown) {
								return;
							}
							logger.error("Scanner " + scanner.getName() + 
									" failed on record " + currentId + " from History table", e);
						}
					}
					
				}
			} catch (Exception e) {
				if (shutDown) {
					return;
				}
				logger.error("Failed on record " + currentId + " from History table", e);
			}
		}
		
	}
	
	private ExtensionHistory getExtensionHistory() {
		if (extHist == null) {
			extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
		}
		return extHist;
	}
	
	public void raiseAlert(int id, Alert alert) {
		if (currentId != id) {
			logger.error("Alert id != crurentId! " + id + " " + currentId);
		}
		alert.setSourceHistoryId(href.getHistoryId());
		
		try {
			href.addAlert(alert);
			this.getExtensionHistory().notifyHistoryItemChanged(href);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	    // Raise the alert
		ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.NAME);
		if (extAlert != null) {
			extAlert.alertFound(alert, href);
		}

	}
	
	public void addTag(int id, String tag) {
		try {
			if (! href.getTags().contains(tag)) {
				href.addTag(tag);
				this.getExtensionHistory().notifyHistoryItemChanged(href);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public int getProxyListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}
	
	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		// Ignore
		return true;
	}

	@Override
	public boolean onHttpResponseReceive(HttpMessage msg) {
		// Wakey wakey
		this.interrupt();
		return true;
	}

	@Override
	public void sessionChanged(Session session) {
		// Reset the currentId
		historyTable = Database.getSingleton().getTableHistory();
		href = null;
		lastId = -1;
		// Get the last id - in case we've just opened an existing session
		currentId = historyTable.lastIndex();
	}
	
	@Override
	public void sessionScopeChanged(Session session) {
	}

	public void shutdown() {
		this.shutDown = true;
	}
	
	@Override
	public void sessionAboutToChange(Session session) {
	}
	
	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}
}
