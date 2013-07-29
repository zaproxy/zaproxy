/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP development team
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
package org.zaproxy.zap.extension.ascan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.ScanPanel;

public class ActiveScan extends org.parosproxy.paros.core.scanner.Scanner implements GenericScanner, ScannerListener {

	private String site = null;
	private ActiveScanPanel activeScanPanel;
	private int progress = 0;
	private boolean isAlive = false;
	private DefaultListModel<HistoryReference> list = new DefaultListModel<>();
	private SiteNode startNode = null;
	private Context startContext = null;
	private int totalRequests = 0;
	private Date timeStarted = null;
	private Date timeFinished = null;
	private boolean deleteRecordsOnExit = true;
	
    /**
     * A list containing all the {@code HistoryReference} IDs that are added to
     * the instance variable {@code list}. Used to delete the
     * {@code HistoryReference}s from the database when no longer needed.
     */
    private List<Integer> historyReferencesToDelete = new ArrayList<>();

	private static final Logger log = Logger.getLogger(ActiveScan.class);

	public ActiveScan(String site, ScannerParam scannerParam, ConnectionParam param, ActiveScanPanel activeScanPanel) {
		super(scannerParam, param);
		this.site = site;
		this.deleteRecordsOnExit = scannerParam.isDeleteRequestsOnShutdown();
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
		this.timeStarted = new Date();
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
		this.timeFinished = new Date();
	}

	@Override
	public DefaultListModel<HistoryReference> getList() {
		return list;
	}
	
	@Override
	public void notifyNewMessage(final HttpMessage msg) {
	    synchronized (list) {
	        HistoryReference hRef = msg.getHistoryRef();
        	this.totalRequests++;
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
        if (deleteRecordsOnExit && historyReferencesToDelete.size() != 0) {
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

	public int getTotalRequests() {
		return totalRequests;
	}

	public Date getTimeStarted() {
		return timeStarted;
	}

	public Date getTimeFinished() {
		return timeFinished;
	}

	@Override
	public void setScanAsUser(User user) {
		// TODO Should be implemented to make the Active Scan work from the point of view of a User
	}
	
}
