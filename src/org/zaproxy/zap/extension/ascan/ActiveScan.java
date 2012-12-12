package org.zaproxy.zap.extension.ascan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.view.ScanPanel;

public class ActiveScan extends org.parosproxy.paros.core.scanner.Scanner implements GenericScanner, ScannerListener {

	private String site = null;
	private ActiveScanPanel activeScanPanel;
	private int progress = 0;
	private boolean isAlive = false;
	private DefaultListModel<HistoryReference> list = new DefaultListModel<>();
	private SiteNode startNode = null;
	private Context startContext = null;
    private boolean scanChildren = true;
	
    /**
     * A list containing all the {@code HistoryReference} IDs that are added to
     * the instance variable {@code list}. Used to delete the
     * {@code HistoryReference}s from the database when no longer needed.
     */
    private List<Integer> historyReferencesToDelete = new ArrayList<>();

	private static final Logger log = Logger.getLogger(ActiveScan.class);

	public ActiveScan(String site, ScannerParam scannerParam, ConnectionParam param, ActiveScanPanel activeScanPanel) {
		super(scannerParam, param);
        this.setScanChildren(scanChildren());
		this.site = site;
		if (activeScanPanel != null) {
			this.activeScanPanel = activeScanPanel;
			this.addScannerListener(activeScanPanel);
		}
		// TODO doesnt this make it circular??
		this.addScannerListener(this);
	
	}

	@Override
	public int getMaximum() {
		return 100;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getSite() {
		return site;
	}

	@Override
	public boolean isRunning() {
		return isAlive;
	}

	@Override
	public boolean isStopped() {
		return super.isStop();
	}

	@Override
	public void pauseScan() {
		super.pause();
	}

	@Override
	public void start() {
		isAlive = true;
		if (startNode == null) {
			SiteMap siteTree = Model.getSingleton().getSession().getSiteTree();
			if (this.getJustScanInScope()) {
				startNode = (SiteNode) siteTree.getRoot();
			} else {
				SiteNode rootNode = (SiteNode) siteTree.getRoot();
				@SuppressWarnings("unchecked")
				Enumeration<SiteNode> en = rootNode.children();
				while (en.hasMoreElements()) {
					SiteNode sn = en.nextElement();
					String nodeName = ScanPanel.cleanSiteName(sn.getNodeName(), true);
					if (this.site.equals(nodeName)) {
						startNode = sn;
						break;
					}
				}
			}
		}
		list.clear();
		this.progress = 0;
		if (startNode != null) {
			this.start(startNode);
		} else {
			log.error("Failed to find site " + site);
		}
	}

	@Override
	public void stopScan() {
		super.stop();

	}

	@Override
	public void resumeScan() {
		super.resume();
	}

/**/
	@Override
	public void alertFound(Alert alert) {
	}

	@Override
	public void hostComplete(String hostAndPort) {
		if (activeScanPanel != null) {
			// Probably being run from the API
			this.activeScanPanel.scanFinshed(hostAndPort);
			this.removeScannerListener(activeScanPanel);
		}
		isAlive = false;
	}

	@Override
	public void hostNewScan(String hostAndPort, HostProcess hostThread) {
	}

	@Override
	public void hostProgress(String hostAndPort, String msg, int percentage) {
		this.progress = percentage;
	}

	@Override
	public void scannerComplete() {
	}

	@Override
	public DefaultListModel<HistoryReference> getList() {
		return list;
	}
	
	@Override
	public void notifyNewMessage(final HttpMessage msg) {
	    synchronized (list) {
	        HistoryReference hRef = msg.getHistoryRef();
            if (hRef == null) {
                try {
                    hRef = new HistoryReference(Model.getSingleton().getSession(), HistoryReference.TYPE_TEMPORARY, msg);
                    // If an alert is raised because of the HttpMessage msg a new HistoryReference must be created 
                    // (because hRef is temporary), and the condition to create it is when the HistoryReference of the 
                    // Alert "retrieved" through the HttpMessage is null. So it must be set to null.
                    msg.setHistoryRef(null);
                    this.historyReferencesToDelete.add(Integer.valueOf(hRef.getHistoryId()));
                    this.list.addElement(hRef);
                } catch (HttpMalformedHeaderException e) {
                    log.error(e.getMessage(), e);
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                this.list.addElement(hRef);
            }
        }
	}

	@Override
	public SiteNode getStartNode() {
		return this.startNode;
	}

	@Override
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
		super.setStartNode(startNode);
	}

	@Override
	public void reset() {
        if (historyReferencesToDelete.size() != 0) {
            try {
                Database.getSingleton().getTableHistory().delete(historyReferencesToDelete);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        this.list = new DefaultListModel<>();
        this.historyReferencesToDelete = new ArrayList<>();
	}

	@Override
	public void setJustScanInScope(boolean scanInScope) {
		super.setJustScanInScope(scanInScope);
	}

	@Override
	public boolean getJustScanInScope() {
		return super.getJustScanInScope();
	}

	@Override
	public void setScanContext(Context context) {
		this.startContext=context;		
		//TODO: Use this context to start the active scan only on Nodes in scope
	}

}
