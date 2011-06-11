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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.parosproxy.paros.Constant;

public class Vulnerabilities {

	private static List<Vulnerability> vulns = null;
	
	@SuppressWarnings("unchecked")
	private static synchronized void init() {
		if (vulns == null) {
			// Read them in from the file
	        try {
	        	File f = new File(Constant.getInstance().VULNS_CONFIG);
	        	XMLConfiguration config = new XMLConfiguration(f);
	        	List<String> test = config.getList("vuln_items");
	        	vulns = new ArrayList<Vulnerability>();
	        	for (String item : test) {
	        		String name = "vuln_item_" + item;
	        		vulns.add(
	        			new Vulnerability(
	        					config.getString(name + ".alert"),
	        					config.getString(name + ".desc"),
	        					config.getString(name + ".solution"),
	        					(List<String>)config.getList(name + ".reference")));
	        	}

          } catch (ConfigurationException e) {
              e.printStackTrace();
          }
		}
	}
	
	public static List<Vulnerability> getAllVulnerabilities() {
		if (vulns == null) {
			init();
		}
		return vulns;
	}
}
