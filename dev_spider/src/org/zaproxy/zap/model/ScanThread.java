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

import org.parosproxy.paros.model.SiteNode;


public abstract class ScanThread extends Thread implements GenericScanner {

	protected String site;
	protected ScanListenner listenner;
	protected int progress = 0;
	protected SiteNode startNode = null;
	
	public ScanThread (String site, ScanListenner listenner) {
		this.site = site;
		this.listenner = listenner;
	}
	
	public void scanProgress(String host, int progress, int maximum) {
		if (progress > this.progress) {
			this.progress = progress;
			this.listenner.scanProgress(site, progress, maximum);
		}
	}

	@Override
	public SiteNode getStartNode() {
		return startNode;
	}

	@Override
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
	}

	@Override
	public abstract void stopScan();

	@Override
	public abstract boolean isStopped();

	@Override
	public String getSite() {
		return site;
	}
	
	@Override
	public abstract int getProgress ();

	@Override
	public abstract int getMaximum ();
	
	@Override
	public abstract void pauseScan();

	@Override
	public abstract void resumeScan();
	
	@Override
	public abstract boolean isPaused();
}
