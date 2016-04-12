package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
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
	private volatile boolean shutDown = false;
	
	private final ExtensionHistory extHist;
	private final ExtensionAlert extAlert;

	private TableHistory historyTable = null;
	private HistoryReference href = null;

	public PassiveScanThread (PassiveScannerList passiveScannerList, ExtensionHistory extHist, ExtensionAlert extensionAlert) {
		super("ZAP-PassiveScanner");
		this.setDaemon(true);
		
		if (extensionAlert == null) {
			throw new IllegalArgumentException("Parameter extensionAlert must not be null.");
		}
		
		this.scannerList = passiveScannerList;
		
		MicrosoftTagTypes.register();
		PHPTagTypes.register();
		PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags otherwise they override processing instructions
		MasonTagTypes.register();

		extAlert = extensionAlert;
		this.extHist = extHist;
	}
	
	@Override
	public void run() {
		historyTable = Model.getSingleton().getDb().getTableHistory();
		// Get the last id - in case we've just opened an existing session
		currentId = this.getLastHistoryId();
		lastId = currentId;
		
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
						lastId = this.getLastHistoryId();
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
					href = getHistoryReference(currentId);
					//historyRecord = historyTable.read(currentId);
				} catch (Exception e) {
					if (shutDown) {
						return;
					}
					logger.error("Failed to read record " + currentId + " from History table", e);
				}

				if (href != null) {
					try {
						// Parse the record
						HttpMessage msg = href.getHttpMessage();
						String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
						Source src = new Source(response);
						
						for (PassiveScanner scanner : scannerList.list()) {
							try {
								if (shutDown) {
									return;
								}
								if (scanner.isEnabled() && scanner.appliesToHistoryType(href.getHistoryType())) {
									scanner.setParent(this);
									scanner.scanHttpRequestSend(msg, href.getHistoryId());
									if (msg.isResponseFromTargetHost()) {
										scanner.scanHttpResponseReceive(msg, href.getHistoryId(), src);
									}
								}
							} catch (Throwable e) {
								if (shutDown) {
									return;
								}
								logger.error("Scanner " + scanner.getName() + 
										" failed on record " + currentId + " from History table: "
										+ href.getMethod() + " " + href.getURI(), e);
							}
						}
					} catch (Exception e) {
						logger.error("Parser failed on record " + currentId + " from History table", e);
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

    private HistoryReference getHistoryReference(final int historyReferenceId) {
        if (extHist != null) {
            return extHist.getHistoryReference(historyReferenceId);
        }

        try {
            return new HistoryReference(historyReferenceId);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            return null;
        }
    }

	private int getLastHistoryId() {
		return historyTable.lastIndex();
	}
	
	protected int getRecordsToScan() {
		return this.getLastHistoryId() - getLastScannedId();
	}

	private int getLastScannedId() {
		if (currentId > lastId) {
			return currentId - 1;
		}
		return currentId;
	}

	public void raiseAlert(int id, Alert alert) {
		if (shutDown) {
			return;
		}

		if (currentId != id) {
			logger.error("Alert id != currentId! " + id + " " + currentId);
		}
	    // Raise the alert
		extAlert.alertFound(alert, href);

	}

    private void notifyHistoryItemChanged(HistoryReference historyReference) {
        if (extHist != null) {
            extHist.notifyHistoryItemChanged(historyReference);
        }
    }
	
	public void addTag(int id, String tag) {
		if (shutDown) {
			return;
		}

		try {
			if (! href.getTags().contains(tag)) {
				href.addTag(tag);
				notifyHistoryItemChanged(href);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public int getArrangeableListenerOrder() {
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
		historyTable = Model.getSingleton().getDb().getTableHistory();
		href = null;
		// Get the last id - in case we've just opened an existing session
		currentId = historyTable.lastIndex();
		lastId = currentId;
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
