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

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;

public class ActiveScanController implements ScanController {
	
	private ExtensionActiveScan extension;
    private static final Logger logger = Logger.getLogger(ActiveScanController.class);

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
	private Map<Integer, GenericScanner2> activeScanMap;
	
	/**
	 * An ordered list of all of the {@code ActiveScan}s created (and not yet removed). Used to get provide the 'last'
	 * scan for client using the 'old' API that didnt support concurrent scans. 
	 */
	private List<GenericScanner2> activeScanList;

	public ActiveScanController (ExtensionActiveScan extension) {
		this.activeScansLock = new ReentrantLock();
		this.extension = extension;
		this.activeScanMap = new HashMap<>();
		this.activeScanList = new ArrayList<GenericScanner2>();
	}

	public void setExtAlert(ExtensionAlert extAlert) {
		this.extAlert = extAlert;
	}

	public int startScan(String name, Target target, User user, Object[] contextSpecificObjects) {
		activeScansLock.lock();
		try {
			int id = this.scanIdCounter++;
			ActiveScan ascan = new ActiveScan(name, extension.getScannerParam(), 
					extension.getModel().getOptionsParam().getConnectionParam(), 
					Control.getSingleton().getPluginFactory().clone()) {
				@Override
				public void alertFound(Alert alert) {
					if (extAlert!= null) {
						extAlert.alertFound(alert, null);
					}
					super.alertFound(alert);
				}
			};
			
			// Set session level configs
			Session session = Model.getSingleton().getSession();
			ascan.setExcludeList(session.getExcludeFromScanRegexs());
			
			ascan.setId(id);
			ascan.setUser(user);
			
			if (contextSpecificObjects != null) {
				for (Object obj : contextSpecificObjects) {
					if (obj instanceof ScannerParam) {
						logger.debug("Setting custom scanner params");
						ascan.setScannerParam((ScannerParam)obj);
					} else if (obj instanceof PluginFactory) {
						ascan.setPluginFactory((PluginFactory)obj);
					} else if (obj instanceof TechSet) {
						ascan.setTechSet((TechSet) obj);
					} else {
						logger.error("Unexpected contextSpecificObject: " + obj.getClass().getCanonicalName());
					}
				}
			}
			
			this.activeScanMap.put(id, ascan);
			this.activeScanList.add(ascan);
			ascan.start(target);
			
			return id;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public int registerScan(GenericScanner2 ascan) {
		activeScansLock.lock();
		try {
			int id = this.scanIdCounter++;
			ascan.setScanId(id);
			this.activeScanMap.put(id, ascan);
			this.activeScanList.add(ascan);
			return id;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public GenericScanner2 getScan(int id) {
		return this.activeScanMap.get(id);
	}
	
	public GenericScanner2 getLastScan() {
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
	
	public List<GenericScanner2> getAllScans() {
		List<GenericScanner2> list = new ArrayList<GenericScanner2>();
		activeScansLock.lock();
		try {
			for (GenericScanner2 scan : activeScanList) {
				list.add(scan);
			}
			return list;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public List<GenericScanner2> getActiveScans() {
		List<GenericScanner2> list = new ArrayList<GenericScanner2>();
		activeScansLock.lock();
		try {
			for (GenericScanner2 scan : activeScanList) {
				if (!scan.isStopped()) {
					list.add(scan);
				}
			}
			return list;
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public GenericScanner2 removeScan(int id) {
		activeScansLock.lock();

		try {
			GenericScanner2 ascan = this.activeScanMap.get(id);
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
			for (GenericScanner2 scan : activeScanMap.values()) {
				scan.stopScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public void pauseAllScans() {
		activeScansLock.lock();
		try {
			for (GenericScanner2 scan : activeScanMap.values()) {
				scan.pauseScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public void resumeAllScans() {
		activeScansLock.lock();
		try {
			for (GenericScanner2 scan : activeScanMap.values()) {
				scan.resumeScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public int removeAllScans() {
		activeScansLock.lock();
		try {
			int count = 0;
			for (Iterator<GenericScanner2> it = activeScanMap.values().iterator(); it.hasNext();) {
				GenericScanner2 ascan = it.next();
				ascan.stopScan();
				it.remove();
				activeScanList.remove(ascan);
				count++;
			}
			return count;
		} finally {
			activeScansLock.unlock();
		}
	}

	@Override
	public int removeFinishedScans() {
		activeScansLock.lock();
		try {
			int count = 0;
			for (Iterator<GenericScanner2> it = activeScanMap.values().iterator(); it.hasNext();) {
				GenericScanner2 ascan = it.next();
				if (ascan.isStopped()) {
					ascan.stopScan();
					it.remove();
					activeScanList.remove(ascan);
					count ++;
				}
			}
			return count;
		} finally {
			activeScansLock.unlock();
		}
	}

	@Override
	public void stopScan(int id) {
		activeScansLock.lock();
		try {
			if (this.activeScanMap.containsKey(id)) {
				this.activeScanMap.get(id).stopScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}

	@Override
	public void pauseScan(int id) {
		activeScansLock.lock();
		try {
			if (this.activeScanMap.containsKey(id)) {
				this.activeScanMap.get(id).pauseScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}

	@Override
	public void resumeScan(int id) {
		activeScansLock.lock();
		try {
			if (this.activeScanMap.containsKey(id)) {
				this.activeScanMap.get(id).resumeScan();
			}
		} finally {
			activeScansLock.unlock();
		}
	}
	
	public void reset() {
		this.stopAllScans();
		activeScansLock.lock();
		try {
			this.scanIdCounter = 0;
		} finally {
			activeScansLock.unlock();
		}
	}
	
}
