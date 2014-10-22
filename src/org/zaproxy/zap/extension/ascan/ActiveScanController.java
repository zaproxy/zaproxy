/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.alert.ExtensionAlert;

public class ActiveScanController {
	
	private ExtensionActiveScan extension;
	
	private ExtensionAlert extAlert = null;

	/**
	 * The {@code Lock} for exclusive access of instance variables related to multiple active scans.
	 * 
	 * @see #activeScanMap
	 * @see #scanIdCounter
	 * @see #lastActiveScanAvailable
	 */
	private final Lock activeScansLock;

	/**
	 * The counter used to give an unique ID to active scans.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code activeScansLock}.
	 * </p>
	 * 
	 * @see #activeScansLock
	 * @see #scanURL(String, boolean, boolean)
	 */
	private int scanIdCounter;

	/**
	 * A map that contains all {@code ActiveScan}s created (and not yet removed). Used to control (i.e. pause/resume and
	 * stop) the multiple active scans and get its results. The instance variable is never {@code null}. The map key is the ID
	 * of the scan.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code activeScansLock}.
	 * </p>
	 * 
	 * @see #activeScansLock
	 * @see #scanURL(String, boolean, boolean)
	 * @see #scanIdCounter
	 */
	private Map<Integer, ActiveScan> activeScanMap;
	
	/**
	 * An ordered list of all of the {@code ActiveScan}s created (and not yet removed). Used to get provide the 'last'
	 * scan for client using the 'old' API that didnt support concurrent scans. 
	 */
	private List<ActiveScan> activeScanList;

	public ActiveScanController (ExtensionActiveScan extension) {
		this.activeScansLock = new ReentrantLock();
		this.extension = extension;
		this.activeScanMap = new HashMap<>();
		this.activeScanList = new ArrayList<ActiveScan>();
	}

	public void setExtAlert(ExtensionAlert extAlert) {
		this.extAlert = extAlert;
	}

	public int scan(String name, SiteNode startNode, boolean scanChildren, boolean scanJustInScope) {
		activeScansLock.lock();
		try {
			int id = this.scanIdCounter++;
			ActiveScan ascan = new ActiveScan(name, extension.getScannerParam(), 
					extension.getModel().getOptionsParam().getConnectionParam(), 
					null, Control.getSingleton().getPluginFactory().clone()) {
				@Override
				public void alertFound(Alert alert) {
					if (extAlert!= null) {
						extAlert.alertFound(alert, null);
					}
					super.alertFound(alert);
				}
			};
			ascan.setJustScanInScope(scanJustInScope);
			ascan.setStartNode(startNode);
			ascan.setScanChildren(scanChildren);
			ascan.setId(id);
			this.activeScanMap.put(id, ascan);
			this.activeScanList.add(ascan);
			
			ascan.start();
			return id;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public ActiveScan getScan(int id) {
		return this.activeScanMap.get(id);
	}
	
	public ActiveScan getLastScan() {
		activeScansLock.lock();
		try {
			if (activeScanList.size() == 0) {
				return null;
			}
			return activeScanList.get(activeScanList.size()-1);
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public List<ActiveScan> getAllScans() {
		List<ActiveScan> list = new ArrayList<ActiveScan>();
		activeScansLock.lock();
		try {
			for (ActiveScan scan : activeScanList) {
				list.add(scan);
			}
			return list;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public ActiveScan removeScan(int id) {
		activeScansLock.lock();

		try {
			ActiveScan ascan = this.activeScanMap.get(id);
			if (! activeScanMap.containsKey(id)) {
				//throw new IllegalArgumentException("Unknown id " + id);
				return null;
			}
			ascan.stopScan();
			activeScanMap.remove(id);
			activeScanList.remove(ascan);
			return ascan;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public int getTotalNumberScans() {
		return activeScanMap.size();
	}
	
	public void stopAllScans() {
		activeScansLock.lock();
		try {
			for (ActiveScan scan : activeScanMap.values()) {
				scan.stopScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public void pauseAllScans() {
		activeScansLock.lock();
		try {
			for (ActiveScan scan : activeScanMap.values()) {
				scan.pauseScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public void resumeAllScans() {
		activeScansLock.lock();
		try {
			for (ActiveScan scan : activeScanMap.values()) {
				scan.resumeScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public void removeAllScans() {
		activeScansLock.lock();
		try {
			for (Iterator<ActiveScan> it = activeScanMap.values().iterator(); it.hasNext();) {
				ActiveScan ascan = it.next();
				ascan.stopScan();
				it.remove();
				activeScanList.remove(ascan);
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
}
