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
package org.zaproxy.zap.extension.spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;

public class SpiderScanController implements ScanController {
	
	private ExtensionSpider extension;

	/**
	 * The {@code Lock} for exclusive access of instance variables related to multiple active scans.
	 * 
	 * @see #spiderScanMap
	 * @see #scanIdCounter
	 * @see #lastSpiderScanAvailable
	 */
	private final Lock spiderScansLock;

	/**
	 * The counter used to give an unique ID to active scans.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code spiderScansLock}.
	 * </p>
	 * 
	 * @see #spiderScansLock
	 * @see #scanURL(String, boolean, boolean)
	 */
	private int scanIdCounter;

	/**
	 * A map that contains all {@code SpiderScan}s created (and not yet removed). Used to control (i.e. pause/resume and
	 * stop) the multiple active scans and get its results. The instance variable is never {@code null}. The map key is the ID
	 * of the scan.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code spiderScansLock}.
	 * </p>
	 * 
	 * @see #spiderScansLock
	 * @see #scanURL(String, boolean, boolean)
	 * @see #scanIdCounter
	 */
	private Map<Integer, SpiderScan> spiderScanMap;
	
	/**
	 * An ordered list of all of the {@code SpiderScan}s created (and not yet removed). Used to get provide the 'last'
	 * scan for client using the 'old' API that didnt support concurrent scans. 
	 */
	private List<SpiderScan> spiderScanList;

	public SpiderScanController (ExtensionSpider extension) {
		this.spiderScansLock = new ReentrantLock();
		this.extension = extension;
		this.spiderScanMap = new HashMap<>();
		this.spiderScanList = new ArrayList<SpiderScan>();
	}

	public int startScan(String name, Target target, User user, Object[] contextSpecificObjects) {
		spiderScansLock.lock();
		try {
			int id = this.scanIdCounter++;
			SpiderScan scan = new SpiderScan(extension, target, user, id);
			scan.setDisplayName(name);
			
			this.spiderScanMap.put(id, scan);
			this.spiderScanList.add(scan);
			scan.start();
			
			return id;
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public GenericScanner2 getScan(int id) {
		return this.spiderScanMap.get(id);
	}
	
	public SpiderScan getLastScan() {
		spiderScansLock.lock();
		try {
			if (spiderScanList.size() == 0) {
				return null;
			}
			return spiderScanList.get(spiderScanList.size()-1);
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public List<GenericScanner2> getAllScans() {
		List<GenericScanner2> list = new ArrayList<GenericScanner2>();
		spiderScansLock.lock();
		try {
			for (SpiderScan scan : spiderScanList) {
				list.add(scan);
			}
			return list;
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public List<GenericScanner2> getActiveScans() {
		List<GenericScanner2> list = new ArrayList<GenericScanner2>();
		spiderScansLock.lock();
		try {
			for (SpiderScan scan : spiderScanList) {
				if (!scan.isStopped()) {
					list.add(scan);
				}
			}
			return list;
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public GenericScanner2 removeScan(int id) {
		spiderScansLock.lock();

		try {
			SpiderScan ascan = this.spiderScanMap.get(id);
			if (! spiderScanMap.containsKey(id)) {
				//throw new IllegalArgumentException("Unknown id " + id);
				return null;
			}
			ascan.stopScan();
			spiderScanMap.remove(id);
			spiderScanList.remove(ascan);
			return ascan;
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public int getTotalNumberScans() {
		return spiderScanMap.size();
	}
	
	public void stopAllScans() {
		spiderScansLock.lock();
		try {
			for (SpiderScan scan : spiderScanMap.values()) {
				scan.stopScan();
			}
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public void pauseAllScans() {
		spiderScansLock.lock();
		try {
			for (SpiderScan scan : spiderScanMap.values()) {
				scan.pauseScan();
			}
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public void resumeAllScans() {
		spiderScansLock.lock();
		try {
			for (SpiderScan scan : spiderScanMap.values()) {
				scan.resumeScan();
			}
		} finally {
			spiderScansLock.unlock();
		}
	}
	
	public int removeAllScans() {
		spiderScansLock.lock();
		try {
			int count = 0;
			for (Iterator<SpiderScan> it = spiderScanMap.values().iterator(); it.hasNext();) {
				SpiderScan ascan = it.next();
				ascan.stopScan();
				it.remove();
				spiderScanList.remove(ascan);
				count++;
			}
			return count;
		} finally {
			spiderScansLock.unlock();
		}
	}

	@Override
	public int removeFinishedScans() {
		spiderScansLock.lock();
		try {
			int count = 0;
			for (Iterator<SpiderScan> it = spiderScanMap.values().iterator(); it.hasNext();) {
				SpiderScan scan = it.next();
				if (scan.isStopped()) {
					scan.stopScan();
					it.remove();
					spiderScanList.remove(scan);
					count ++;
				}
			}
			return count;
		} finally {
			spiderScansLock.unlock();
		}
	}

	@Override
	public void stopScan(int id) {
		spiderScansLock.lock();
		try {
			if (this.spiderScanMap.containsKey(id)) {
				this.spiderScanMap.get(id).stopScan();
			}
		} finally {
			spiderScansLock.unlock();
		}
	}

	@Override
	public void pauseScan(int id) {
		spiderScansLock.lock();
		try {
			if (this.spiderScanMap.containsKey(id)) {
				this.spiderScanMap.get(id).pauseScan();
			}
		} finally {
			spiderScansLock.unlock();
		}
	}

	@Override
	public void resumeScan(int id) {
		spiderScansLock.lock();
		try {
			if (this.spiderScanMap.containsKey(id)) {
				this.spiderScanMap.get(id).resumeScan();
			}
		} finally {
			spiderScansLock.unlock();
		}
	}
	
}
