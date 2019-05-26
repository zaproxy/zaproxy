/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.model;

import javax.swing.ListModel;

import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.users.User;

public interface GenericScanner extends Runnable {

	void stopScan();

	boolean isStopped();

	String getSite();

	int getProgress();

	int getMaximum();

	void pauseScan();

	void resumeScan();

	boolean isPaused();

	boolean isRunning();

	void start();

	SiteNode getStartNode();

	void setStartNode(SiteNode startNode);

	/**
	 * Sets whether the scanner is started with the nodes in scope or in context.
	 * 
	 * @param scanInScope the new just scan in scope
	 * @see GenericScanner#setScanContext(Context)
	 */
	void setJustScanInScope(boolean scanInScope);

	boolean getJustScanInScope();

	ListModel<?> getList();

	void reset();

	void setScanChildren(boolean scanChildren);

	/**
	 * Sets the {@link Context} that should be scanned. The value should be used only if
	 * {@code JustScanInScope} is enabled. If the {@code justScanInScope} property is set, when the scan is
	 * started, it should be scan all the nodes in scope, if this StartContext is <code>null</code>, or all
	 * the nodes in context, if this StartContext is not null.
	 * 
	 * @param context the new scan context. If null, the scan will be run for all the nodes in scope.
	 * @see GenericScanner#setJustScanInScope(boolean)
	 * @see GenericScanner#getJustScanInScope()
	 */
	void setScanContext(Context context);
	
	void setScanAsUser(User user);

	void setTechSet(TechSet techSet);
}
