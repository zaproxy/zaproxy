package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	
	private static Set<Integer> optedInHistoryTypes = new HashSet<Integer>();
	
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
	private final PassiveScanParam pscanOptions;

	private TableHistory historyTable = null;
	private HistoryReference href = null;
	private Session session;

	/**
	 * Constructs a {@code PassiveScanThread} with the given data.
	 *
	 * @param passiveScannerList the passive scanners, must not be {@code null}.
	 * @param extHist the extension to obtain the (cached) history references, might be {@code null}.
	 * @param extensionAlert the extension used to raise the alerts, must not be {@code null}.
	 * @deprecated (2.6.0) Use
	 *             {@link #PassiveScanThread(PassiveScannerList, ExtensionHistory, ExtensionAlert, PassiveScanParam)} instead.
	 *             It will be removed in a future release.
	 */
	@Deprecated
	public PassiveScanThread(PassiveScannerList passiveScannerList, ExtensionHistory extHist, ExtensionAlert extensionAlert) {
		this(passiveScannerList, extHist, extensionAlert, new PassiveScanParam());
	}

	/**
	 * Constructs a {@code PassiveScanThread} with the given data.
	 *
	 * @param passiveScannerList the passive scanners, must not be {@code null}.
	 * @param extHist the extension to obtain the (cached) history references, might be {@code null}.
	 * @param extensionAlert the extension used to raise the alerts, must not be {@code null}.
	 * @param pscanOptions the passive scanner options, must not be {@code null}.
	 * @since 2.6.0
	 */
	public PassiveScanThread (PassiveScannerList passiveScannerList, ExtensionHistory extHist, ExtensionAlert extensionAlert,
			PassiveScanParam pscanOptions) {
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
		this.pscanOptions = pscanOptions;
	}
	
	@Override
	public void run() {
		historyTable = Model.getSingleton().getDb().getTableHistory();
		session = Model.getSingleton().getSession();
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

				if (href != null && (!pscanOptions.isScanOnlyInScope() || session.isInScope(href))) {
					try {
						// Parse the record
						HttpMessage msg = href.getHttpMessage();
						Source src = new Source(msg.getResponseBody().toString());
						
						for (PassiveScanner scanner : scannerList.list()) {
							try {
								if (shutDown) {
									return;
								}
								int hrefHistoryType = href.getHistoryType();
								if (scanner.isEnabled() && (scanner.appliesToHistoryType(hrefHistoryType)
										|| optedInHistoryTypes.contains(hrefHistoryType))) {
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
						if (HistoryReference.getTemporaryTypes().contains(href.getHistoryType())) {
							if (logger.isDebugEnabled()) {
								logger.debug("Temporary record " + currentId + " no longer available:", e);
							}
						} else {
							logger.error("Parser failed on record " + currentId + " from History table", e);
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

		alert.setSource(Alert.Source.PASSIVE);
	    // Raise the alert
		extAlert.alertFound(alert, href);

	}

	public void addTag(int id, String tag) {
		if (shutDown) {
			return;
		}

		try {
			if (! href.getTags().contains(tag)) {
				href.addTag(tag);
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
	
	/**
	 * Add the History Type ({@code int}) to the set of applicable history
	 * types.
	 * 
	 * @param type
	 *            the type to be added to the set of applicable history types
	 * @since TODO add version
	 */
	public static void addApplicableHistoryType(int type) {
		optedInHistoryTypes.add(type);
	}
	
	/**
	 * Remove the History Type ({@code int}) from the set of applicable history
	 * types.
	 * 
	 * @param type
	 *            the type to be removed from the set of applicable history
	 *            types
	 * @since TODO add version
	 */
	public static void removeApplicableHistoryType(int type) {
		optedInHistoryTypes.remove(type);
	}

	/**
	 * Returns the set of History Types which have "opted-in" to be applicable
	 * for passive scanning.
	 * 
	 * @return a set of {@code Integer} representing all of the History Types
	 *         which have "opted-in" for passive scanning.
	 * @since TODO add version
	 */
	public static Set<Integer> getOptedInHistoryTypes() {
		return Collections.unmodifiableSet(optedInHistoryTypes);
	}
	
	/**
	 * Returns the full set (both default and "opted-in") which are to be
	 * applicable for passive scanning.
	 * 
	 * @return a set of {@code Integer} representing all of the History Types
	 *         which are applicable for passive scanning.
	 * @since TODO add version
	 */
	public static Set<Integer> getApplicableHistoryTypes() {
		Set<Integer> allApplicableTypes = new HashSet<Integer>();
		allApplicableTypes.addAll(PluginPassiveScanner.getDefaultHistoryTypes());
		if (!optedInHistoryTypes.isEmpty()) {
			allApplicableTypes.addAll(optedInHistoryTypes);
		}
		return allApplicableTypes;
	}
}
