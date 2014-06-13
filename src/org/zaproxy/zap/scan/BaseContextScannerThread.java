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
package org.zaproxy.zap.scan;

import java.util.LinkedHashSet;
import java.util.Set;

import org.zaproxy.zap.model.Context;

/**
 * An implementation of a {@link BaseScannerThread} for scans done on {@link Context Contexts},
 * adding support for notifying a list of {@link ScanListener scan listeners}.
 *
 * @see BaseScannerThread
 * @see ScanListener
 * @see Context
 */
public abstract class BaseContextScannerThread<StartOptions extends ScanStartOptions, Listener extends ScanListener>
		extends BaseScannerThread<StartOptions> {

	protected Set<Listener> listeners;
	protected int contextId;

	/**
	 * Instantiates a new base context scanner thread for a Context with a given id.
	 *
	 * @param contextId the context id
	 */
	public BaseContextScannerThread(int contextId) {
		super();
		this.contextId = contextId;
		this.listeners = new LinkedHashSet<Listener>();
	}

	/**
	 * Adds a new scan listener.
	 */
	public void addScanListener(Listener l) {
		listeners.add(l);
	}

	/**
	 * Removes a scan listener.
	 */
	public void removeScanListener(Listener l) {
		listeners.remove(l);
	}

	/**
	 * Notifies listeners that the scan has started.
	 */
	protected void notifyScanStarted() {
		for (ScanListener l : listeners)
			l.scanStarted(contextId);
	}

	/**
	 * Notifies listeners that the scan has finished.
	 */
	protected void notifyScanFinished() {
		for (ScanListener l : listeners)
			l.scanFinished(contextId);
	}

	/**
	 * Notifies listeners that the scan has changes it's progress. The progress sent to the
	 * listeners is the one set for the scan (via {@link #setScanProgress(int)}).
	 */
	protected void notifyScanProgress() {
		for (ScanListener l : listeners)
			l.scanProgress(contextId, getScanProgress(), getScanMaximumProgress());
	}

	@Override
	public void setScanProgress(int progress) {
		super.setScanProgress(progress);
		notifyScanProgress();
	}

}
