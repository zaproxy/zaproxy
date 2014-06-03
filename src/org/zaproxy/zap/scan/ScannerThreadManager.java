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

import java.util.Collection;

/**
 * An utility class used to manage the scanner threads that have been created for a particular scan
 * type and/or extension.
 * 
 * @param <ScannerThread> the type of scanner
 * @param <ScanTarget> the main target of a scan thread (e.g. Context, SiteNode, domain).
 */
public interface ScannerThreadManager<ScannerThread extends BaseScannerThread<?>, ScanTarget> {

	/**
	 * Gets the scanner thread for a target.
	 *
	 * @param scanTarget the scan target
	 * @return the scanner thread
	 */
	public ScannerThread getScannerThread(ScanTarget scanTarget);

	/**
	 * Recreate the scanner thread for a target if it has already run.
	 *
	 * @param scanTarget the scan target
	 * @return the scanner thread
	 */
	public ScannerThread recreateScannerThreadIfHasRun(ScanTarget scanTarget);

	/**
	 * Gets the all threads.
	 */
	public Collection<ScannerThread> getAllThreads();

	/**
	 * Clear threads.
	 */
	public void clearThreads();

	/**
	 * Stop all scans.
	 */
	public void stopAllScannerThreads();
}
