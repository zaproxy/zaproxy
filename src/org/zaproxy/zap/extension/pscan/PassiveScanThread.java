package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.scanner.ExtensionScanner;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.ExtensionActiveScan;

public class PassiveScanThread extends Thread implements ProxyListener, SessionChangedListener {
	
	@SuppressWarnings("unused")
	private OptionsPassiveScan options = null;
	private PassiveScannerList scannerList = null;
	private int currentId = 1;
	private int lastId = -1;
	private int mainSleep = 5000;
	private int postSleep = 200;
	private boolean shutDown = false;
	
	private ExtensionHistory extHist = null; 
	private ExtensionScanner extScan = null;
	private ExtensionActiveScan extAScan = null;

	private TableHistory historyTable = null;
	private RecordHistory historyRecord = null;
	
	private Logger logger = Logger.getLogger(this.getClass());

	public PassiveScanThread (PassiveScannerList passiveScannerList) {
		this.scannerList = passiveScannerList;
		
		MicrosoftTagTypes.register();
		PHPTagTypes.register();
		PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags otherwise they override processing instructions
		MasonTagTypes.register();

	}
	
	@Override
	public void run() {
		historyTable = Database.getSingleton().getTableHistory();
		try {
			// Get the last id - in case we've just opened an existing session
			currentId = historyTable.lastIndex();
		} catch (Exception e1) {
			logger.error("Failed to get last index in History table", e1);
		}
		
		while (!shutDown) {
			try {
				if (historyRecord != null || lastId > currentId ) {
					currentId ++;
				} else {
					// Either just started or there are no new records 
					try {
						Thread.sleep(mainSleep);
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
					historyRecord = historyTable.read(currentId);
				} catch (Exception e) {
					logger.error("Failed to read record " + currentId + " from History table", e);
				}

				if (historyRecord != null && 
						(historyRecord.getHistoryType() == HistoryReference.TYPE_MANUAL ||
						historyRecord.getHistoryType() == HistoryReference.TYPE_SPIDER)) {
					// Note that scanning TYPE_SCANNER records will result in a loop ;)
					// Parse the record
					String response = historyRecord.getHttpMessage().getResponseHeader().toString() + 
						historyRecord.getHttpMessage().getResponseBody().toString();
					Source src = new Source(response);
					
					for (PassiveScanner scanner : scannerList.list()) {
						try {
							if (scanner.isEnabled()) {
								scanner.setParent(this);
								scanner.scanHttpRequestSend(historyRecord.getHttpMessage(), historyRecord.getHistoryId());
								scanner.scanHttpResponseReceive(historyRecord.getHttpMessage(), historyRecord.getHistoryId(), src);
							}
						} catch (Exception e) {
							logger.error("Scanner " + scanner.getName() + 
									" failed on record " + currentId + " from History table", e);
						}
					}
					
				}
			} catch (Exception e) {
				logger.error("Failed on record " + currentId + " from History table", e);
			}
		}
		
	}
	
	private void init () {
		extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory");
		if (Control.getSingleton().getExtensionLoader().getExtension("ExtensionScanner") != null) {
			extScan = (ExtensionScanner) Control.getSingleton().getExtensionLoader().getExtension("ExtensionScanner");
		}
		if (Control.getSingleton().getExtensionLoader().getExtension("ExtensionActiveScan") != null) {
			extAScan = (ExtensionActiveScan) Control.getSingleton().getExtensionLoader().getExtension("ExtensionActiveScan");
		}
	}
		
	public void raiseAlert(int id, Alert alert) {
		HistoryReference href = null;
		if (extHist == null) {
			init();
		}

		if (currentId != id) {
			logger.error("Alert id != crurentId! " + id + " " + currentId);
		}
		alert.setSourceHistoryId(historyRecord.getHistoryId());
		
		try {
			href = extHist.getHistoryList().getHistoryReference(historyRecord.getHistoryId());
			if (href != null) {
				href.addAlert(alert);
				extHist.getHistoryList().notifyItemChanged(historyRecord.getHistoryId());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	    // Raise the alert
		if (extAScan != null) {
			extAScan.alertFound(alert, href);
		}
		if (extScan != null) {
			extScan.alertFound(alert);
		}

	}

	public void addTag(int id, String tag) {
		if (extHist == null) {
			init();
		}
		try {
			HistoryReference href = extHist.getHistoryList().getHistoryReference(historyRecord.getHistoryId());
			if (href != null) {
				if (! href.getTags().contains(tag)) {
					href.addTag(tag);
					extHist.getHistoryList().notifyItemChanged(historyRecord.getHistoryId());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
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
		historyRecord = null;
		lastId = -1;
		try {
			// Get the last id - in case we've just opened an existing session
			currentId = historyTable.lastIndex();
		} catch (Exception e1) {
			logger.error("Failed to get last index in History table", e1);
		}
	}
	
	public void shutdown() {
		this.shutDown = true;
	}
}
