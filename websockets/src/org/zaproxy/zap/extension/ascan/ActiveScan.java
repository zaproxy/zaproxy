package org.zaproxy.zap.extension.ascan;

import java.util.Enumeration;
import javax.swing.DefaultListModel;
import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.view.ScanPanel;

public class ActiveScan extends org.parosproxy.paros.core.scanner.Scanner implements GenericScanner, ScannerListener {

	private String site = null;
	private ActiveScanPanel activeScanPanel;
	private int progress = 0;
	private boolean isAlive = false;
	private DefaultListModel list = new DefaultListModel();
	private SiteNode startNode = null;
	private static Logger log = Logger.getLogger(ActiveScan.class);

	public ActiveScan(String site, ScannerParam scannerParam, ConnectionParam param, ActiveScanPanel activeScanPanel) {
		super(scannerParam, param);
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
	public boolean isAlive() {
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
		SiteMap siteTree = this.activeScanPanel.getExtension().getModel().getSession().getSiteTree();
		SiteNode rootNode = (SiteNode) siteTree.getRoot();
		//SiteNode startNode = null;
		if (startNode == null) {
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
	public DefaultListModel getList() {
		return list;
	}
	
	
	@Override
	public void notifyNewMessage(final HttpMessage msg) {
		synchronized (list) {
			this.list.addElement(msg);
		}
	}

	@Override
	public SiteNode getStartNode() {
		return this.startNode;
	}

	@Override
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
	}

	@Override
	public void reset() {
		this.list = new DefaultListModel();
	}
}
