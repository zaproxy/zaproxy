/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP development team
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Scanner;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMapEventPublisher;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.log4j.ExtensionLog4j;
import org.zaproxy.zap.extension.ruleconfig.ExtensionRuleConfig;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.view.ScanStatus;

public class AttackModeScanner implements EventConsumer {

	private static final String ATTACK_ICON_RESOURCE = "/resource/icon/16/093.png";

	private ExtensionActiveScan extension;
	private Date lastUpdated = new Date(); 
	private ScanStatus scanStatus;
	private ExtensionAlert extAlert = null;
	private AttackModeThread attackModeThread = null;
	private boolean rescanOnChange = false;
	
    private Logger log = Logger.getLogger(AttackModeScanner.class);
    
    private List<SiteNode> nodeStack = new ArrayList<SiteNode>();

	public AttackModeScanner(ExtensionActiveScan extension) {
		this.extension = extension;
		ZAP.getEventBus().registerConsumer(this, SiteMapEventPublisher.class.getCanonicalName());
		
        scanStatus = new ScanStatus(
				new ImageIcon(
						ExtensionLog4j.class.getResource("/resource/icon/fugue/target.png")),
					Constant.messages.getString("ascan.attack.icon.title"));

	}
	
	public void start() {
		log.debug("Starting");
		nodeStack.clear();
		
		this.addAllInScope();
		
		if (attackModeThread != null) {
			attackModeThread.shutdown();
		}
		attackModeThread = new AttackModeThread();
		Thread t = new Thread(attackModeThread);
		t.setName("ZAP-AttackMode");
		t.start();
		
	}
	
	private void addAllInScope() {
		if (this.rescanOnChange) {
			this.nodeStack.addAll(Model.getSingleton().getSession().getNodesInScopeFromSiteTree());
			log.debug("Added existing in scope nodes to attack mode stack " + this.nodeStack.size());
			updateCount();
		}
	}
	
	public void stop() {
		log.debug("Stopping");
		if (this.attackModeThread != null) {
			this.attackModeThread.shutdown();
		}
		nodeStack.clear();
		updateCount();
	}
	
	@Override
	public void eventReceived(Event event) {
		if (this.attackModeThread != null && this.attackModeThread.isRunning()) {
			if (event.getEventType().equals(SiteMapEventPublisher.SITE_NODE_ADDED_EVENT) &&
					event.getTarget().getStartNode().isIncludedInScope()) {
				if (event.getTarget().getStartNode().getHistoryReference().getHistoryType()
						!= HistoryReference.TYPE_TEMPORARY) {
					// Add to the stack awaiting attack
					log.debug("Adding node to attack mode stack " + event.getTarget().getStartNode());
					nodeStack.add(event.getTarget().getStartNode());
					updateCount();
				}
			} else if (event.getEventType().equals(SiteMapEventPublisher.SITE_NODE_REMOVED_EVENT)) {
				if (nodeStack.contains(event.getTarget().getStartNode())) {
					nodeStack.remove(event.getTarget().getStartNode());
				}
			}
		}
	}
	
	public ScanStatus getScanStatus() {
		return scanStatus;
	}
	
    public void sessionScopeChanged(Session session) {
		this.addAllInScope();
    }
    
    public void sessionModeChanged(Mode mode) {
        if (mode.equals(Mode.attack)) {
        	if (View.isInitialised() && extension.getScannerParam().isPromptInAttackMode()) {
        		SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int res = View.getSingleton().showYesNoRememberDialog(
								View.getSingleton().getMainFrame(), 
								Constant.messages.getString("ascan.attack.prompt"));

		        		if (View.getSingleton().isRememberLastDialogChosen()) {
		        			extension.getScannerParam().setPromptInAttackMode(false);
		        			extension.getScannerParam().setRescanInAttackMode(res == JOptionPane.YES_OPTION);
		        		}
		        		rescanOnChange = (res == JOptionPane.YES_OPTION); 
		            	start();
					}});
        	} else {
        		this.rescanOnChange = extension.getScannerParam().isRescanInAttackMode(); 
            	this.start();
        	}
        	
        } else {
        	this.stop();
        }

    }

	private void updateCount() {
		Date now = new Date();
		if (now.getTime() - this.lastUpdated.getTime() > 200) {
			// Dont update too frequently, eg using the spider could hammer the UI unnecessarily
			this.lastUpdated = now;
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					scanStatus.setScanCount(nodeStack.size());
				}});
		}
	}
	
	public int getStackSize() {
		int count = nodeStack.size();
		if (count > 0) {
			// There are nodes to scan
			return count;
		}
		// Work out if any scanning is in progress
		if (this.attackModeThread != null && this.attackModeThread.isActive()) {
			return 0;
		}
		return -1;
	}
	
	public boolean isRescanOnChange() {
		return rescanOnChange;
	}

	public void setRescanOnChange(boolean rescanOnChange) {
		this.rescanOnChange = rescanOnChange;
	}

	private ExtensionAlert getExtensionAlert() {
		if (extAlert == null) {
			extAlert = (ExtensionAlert) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.NAME);
		}
		return extAlert;
	}

	private class AttackModeThread implements Runnable, ScannerListener {

		private int scannerCount = 4; 
		private List<Scanner> scanners = new ArrayList<Scanner>();
		private AttackScan ascanWrapper;
		private boolean running = false;
		
		@Override
		public void run() {
			log.debug("Starting attack thread");
			this.running = true;

			RuleConfigParam ruleConfigParam = null;
			ExtensionRuleConfig extRC = 
				Control.getSingleton().getExtensionLoader().getExtension(ExtensionRuleConfig.class);
			if (extRC != null) {
				ruleConfigParam = extRC.getRuleConfigParam();
			}

			ascanWrapper = new AttackScan(Constant.messages.getString("ascan.attack.scan"), extension.getScannerParam(), 
					extension.getModel().getOptionsParam().getConnectionParam(), 
					extension.getPolicyManager().getAttackScanPolicy(), ruleConfigParam);
			extension.registerScan(ascanWrapper);
			while (running) {
				if (scanStatus.getScanCount() != nodeStack.size()) {
					updateCount();
				}
				if (nodeStack.size() == 0 || scanners.size() == scannerCount) {
					if (scanners.size() > 0) {
						// Check to see if any have finished
						scannerComplete(-1);
					}
					// Still scanning a node or nothing to scan now
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// Ignore
					}
					continue;
				}
				while (nodeStack.size() > 0 && scanners.size() < scannerCount) {
					SiteNode node = nodeStack.remove(0);
					log.debug("Attacking node " + node.getNodeName());
					
					Scanner scanner = new Scanner(extension.getScannerParam(), 
							extension.getModel().getOptionsParam().getConnectionParam(), 
							extension.getPolicyManager().getAttackScanPolicy(),
							ruleConfigParam);
					scanner.setStartNode(node);
					scanner.setScanChildren(false);
					scanner.addScannerListener(this);
					synchronized (this.scanners) {
						this.scanners.add(scanner);
					}
					
					if (View.isInitialised()) {
						// set icon to show its being scanned
						node.addCustomIcon(ATTACK_ICON_RESOURCE, false);
					}
					scanner.start(node);
				}
			}
			synchronized (this.scanners) {
				for (Scanner scanner : this.scanners) {
					scanner.stop();
				}
			}
			log.debug("Attack thread finished");
		}

		@Override
		public void scannerComplete(int id) {
			// Clear so we can attack the next node
			List<Scanner> stoppedScanners = new ArrayList<Scanner>();
			synchronized (this.scanners) {
				for (Scanner scanner : this.scanners) {
					if (scanner.isStop()) {
						SiteNode node = scanner.getStartNode();
						if (node != null) {
							log.debug("Finished attacking node " + node.getNodeName());
							if (View.isInitialised()) {
								// Remove the icon
								node.removeCustomIcon(ATTACK_ICON_RESOURCE);
							}
						}
						stoppedScanners.add(scanner);
					}
				}
				for (Scanner scanner : stoppedScanners) {
					// Cant remove them in the above loop
					scanners.remove(scanner);
				}
			}
			updateCount();
		}

		@Override
		public void hostNewScan(int id, String hostAndPort, HostProcess hostThread) {
			// Ignore
		}

		@Override
		public void hostProgress(int id, String hostAndPort, String msg, int percentage) {
			// Ignore
		}

		@Override
		public void hostComplete(int id, String hostAndPort) {
			// Ignore
		}

		@Override
		public void alertFound(Alert alert) {
			getExtensionAlert().alertFound(alert, alert.getHistoryRef());
		}

		@Override
		public void notifyNewMessage(HttpMessage msg) {
			ascanWrapper.notifyNewMessage(msg);
		}
		
		public void shutdown() {
			this.running = false;
		}
		
		public boolean isRunning() {
			return this.running;
		}
		
		/**
		 * Returns true if any of the scan threads are currently active
		 * @return
		 */
		public boolean isActive() {
			synchronized (this.scanners) {
				for (Scanner scanner : this.scanners) {
					if (! scanner.isStop()) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
