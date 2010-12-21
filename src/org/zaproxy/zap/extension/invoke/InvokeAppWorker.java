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
package org.zaproxy.zap.extension.invoke;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.view.View;

public class InvokeAppWorker extends SwingWorker<Void, Void> {

    private String command = null;
    private String parameters = null;
    private boolean captureOutput = true;
    private URI uri = null;

    private Logger logger = Logger.getLogger(InvokeAppWorker.class);

	public InvokeAppWorker (String command, String parameters, boolean captureOutput, URI uri) {
		this.command = command;
		this.parameters = parameters;
		this.captureOutput = captureOutput;
		this.uri = uri;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		
		String url = "";	// Full URL
		String host = "";	// Just the server name, e.g. localhost
		String port = "";	// the port
		String site = "";	// e.g. http://localhost:8080/

		url = uri.toString();
		host = uri.getHost();
		site = uri.getScheme() + "://" + uri.getHost();
		if (uri.getPort() > 0) {
			site = site + ":" + uri.getPort() + "/";
			port = "" + uri.getPort();
		} else {
			if (uri.getScheme().equalsIgnoreCase("http")) {
				port = "80";
			} else if (uri.getScheme().equalsIgnoreCase("https")) {
				port = "443";
			}
			site = site + "/";
		}

		List<String> cmd = new ArrayList<String>();
		cmd.add(command);
		if (parameters != null) {
			// Replace all of the tags 
			String params = parameters.replace("%url%", url)
									.replace("%host%", host)
									.replace("%port%", port)
									.replace("%site%", site);
			for (String p : params.split(" ")) {
				cmd.add(p);
			}
		}

		logger.debug("Invoking: " + cmd.toString());
		View.getSingleton().getOutputPanel().append("\n" + cmd.toString() + "\n");
		Process proc = new ProcessBuilder(cmd).start();

		if (captureOutput) {
			BufferedReader brErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			BufferedReader brOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			boolean isOutput = false;

			// Show any error messages
			while ((line = brErr.readLine()) != null) {
				View.getSingleton().getOutputPanel().append(line + "\n");
				isOutput = true;
			}
			// Show any stdout messages
			while ((line = brOut.readLine()) != null) {
				View.getSingleton().getOutputPanel().append(line + "\n");
				isOutput = true;
			}
			if (isOutput) {
				// Somethings been written, switch to the Output tab
				View.getSingleton().getOutputPanel().setTabFocus();
			}
		}
			
		return null;
	}

}
