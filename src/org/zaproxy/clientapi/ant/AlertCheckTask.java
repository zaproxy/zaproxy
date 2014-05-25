/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.zaproxy.clientapi.core.Alert;

public class AlertCheckTask extends ZapTask {
	
	private List<AlertTask> ignoreAlertTasks = new ArrayList<>();
	private List<AlertTask> requireAlertTasks = new ArrayList<>();
	
	@Override
	public void execute() throws BuildException {
		try {
			List<Alert> ignoreAlerts = new ArrayList<>(ignoreAlertTasks.size());
			List<Alert> requireAlerts = new ArrayList<>(requireAlertTasks.size());
			for (AlertTask alert: ignoreAlertTasks) {
				ignoreAlerts.add(new Alert(alert.getAlert(), alert.getUrl(), alert.getRisk(), alert.getConfidence(), alert.getParam(), alert.getOther()));
			}
			for (AlertTask alert: requireAlertTasks) {
				requireAlerts.add(new Alert(alert.getAlert(), alert.getUrl(), alert.getRisk(), alert.getConfidence(), alert.getParam(), alert.getOther()));
			}
			
			this.getClientApi().checkAlerts(ignoreAlerts, requireAlerts);
			
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	public void addIgnoreAlert(AlertTask alert) {
		this.ignoreAlertTasks.add(alert);
	}
	
	public void addRequireAlert(AlertTask alert) {
		this.requireAlertTasks.add(alert);
	}
	
	public List<AlertTask> getIgnoreAlerts() {
		return ignoreAlertTasks;
	}

	public List<AlertTask> getRequireAlerts() {
		return requireAlertTasks;
	}
}
