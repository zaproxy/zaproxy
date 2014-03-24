/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
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
package org.zaproxy.zap.extension.autoupdate;

import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.Enableable;

public class AddOnWrapper extends Enableable {
	
	public enum Status {uninstalled, newAddon, newVersion, downloading, installed, latest}; 

	private AddOn addOn = null;
	private Status status = null;
	private int progress = 0;
	private boolean failed = false;

	public AddOnWrapper (AddOn addOn, Status status) {
		this.addOn = addOn;
		this.status = status;
	}

	public AddOn getAddOn() {
		return addOn;
	}

	public Status getStatus() {
		return status;
	}
	
	public int getProgress() {
		return progress;
	}

	public void setAddOn(AddOn addOn) {
		this.addOn = addOn;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	
}
