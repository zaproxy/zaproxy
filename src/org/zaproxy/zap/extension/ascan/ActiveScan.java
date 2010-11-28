package org.zaproxy.zap.extension.ascan;

import java.util.Enumeration;

import javax.swing.DefaultListModel;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.GenericScanner;

public class ActiveScan extends org.parosproxy.paros.core.scanner.Scanner implements GenericScanner, ScannerListener {

	private String site = null;
	private ActiveScanPanel activeScanPanel;
	private int progress = 0;
	private boolean isAlive = false;
	private DefaultListModel list;
	
	public ActiveScan(String site, ScannerParam scannerParam, ConnectionParam param, ActiveScanPanel activeScanPanel) {
		super(scannerParam, param);
		this.site = site;
		this.activeScanPanel = activeScanPanel;
		this.addScannerListener(activeScanPanel);
		// TODO doesnt this make it circular??
		this.addScannerListener(this);
		this.list = new DefaultListModel();
	
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

	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		isAlive = true;
		SiteMap siteTree = this.activeScanPanel.getExtension().getModel().getSession().getSiteTree();
		SiteNode rootNode = (SiteNode) siteTree.getRoot();
		SiteNode startNode = null;
		
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
			String nodeName = sn.getNodeName();
			if (nodeName.indexOf("//") >= 0) {
				nodeName = nodeName.substring(nodeName.indexOf("//") + 2);
			}
			if (this.site.equals(nodeName)) {
				startNode = sn;
				break;
			}
		}
		if (startNode != null) {
			this.start(startNode);
		} else {
			// TODO what? Popup?
			System.out.println("Failed to find site " + site);
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
		this.activeScanPanel.scanFinshed(hostAndPort);
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

	public DefaultListModel getList() {
		return list;
	}
	
	
	@Override
	public void notifyNewMessage(HttpMessage msg) {
		this.list.addElement(msg);
	}
}
