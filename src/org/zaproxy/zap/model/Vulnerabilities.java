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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class Vulnerabilities {

	private static final Logger logger = Logger.getLogger(Vulnerabilities.class);
	
	private static List<Vulnerability> vulns = null;
	private static Map<String, Vulnerability> idToVuln = null;
	
	private Vulnerabilities() {
	}

	private static void initEmpty() {
    	idToVuln = Collections.emptyMap();
    	vulns = Collections.unmodifiableList(Collections.<Vulnerability>emptyList());
	}
	
	private static synchronized void init() {
		if (vulns == null) {
			// Read them in from the file
			XMLConfiguration config;
	        try {
	        	File f = new File(Constant.getZapInstall(), Constant.getInstance().VULNS_CONFIG);
	        	config = new XMLConfiguration();
	        	config.setDelimiterParsingDisabled(true);
	        	config.load(f);
	        } catch (ConfigurationException e) {
	        	logger.error(e.getMessage(), e);
	        	initEmpty();
	        	return ;
	        }
	        
	        String[] test;
	        try {
	        	test = config.getStringArray("vuln_items");
	        } catch (ConversionException e) {
            	logger.error(e.getMessage(), e);
            	initEmpty();
            	return;
	        }
        	final int numberOfVulns = test.length;
        	
        	List<Vulnerability> tempVulns = new ArrayList<>(numberOfVulns);
        	idToVuln = new HashMap<>(Math.max((int) (numberOfVulns / 0.75) + 1, 16));
        	
        	String name;
        	List<String> references;
        	
        	for (String item : test) {
        		name = "vuln_item_" + item;
        		try {
        			references = new ArrayList<>(Arrays.asList(config.getStringArray(name + ".reference")));
        		} catch (ConversionException e) {
        			logger.error(e.getMessage(), e);
        			references = new ArrayList<>(0);
        		}
        			
        		Vulnerability v = 
        			new Vulnerability(
        					item,
        					config.getString(name + ".alert"),
        					config.getString(name + ".desc"),
        					config.getString(name + ".solution"),
        					references);
        		tempVulns.add(v);
        		idToVuln.put(item, v);
        	}
        	
        	vulns = Collections.unmodifiableList(tempVulns);
		}
	}
	
	/**
	 * Gets an unmodifiable {@code List} containing all the
	 * {@code Vulnerability} loaded from the path {@code Constant.VULNS_CONFIG}.
	 * <p>
	 * An empty {@code List} is returned if any error occurred while opening the
	 * file. The returned {@code List} is guaranteed to be <i>non</i>
	 * {@code null}.
	 * </p>
	 * <p>
	 * <b>Note:</b> Trying to modify the list will result in an
	 * {@code UnsupportedOperationException}.
	 * </p>
	 * 
	 * @return An unmodifiable {@code List} containing all the
	 *         {@code Vulnerability} loaded, never {@code null}.
	 * 
	 * @see Constant#VULNS_CONFIG
	 */
	public static List<Vulnerability> getAllVulnerabilities() {
		if (vulns == null) {
			init();
		}
		return vulns;
	}
	
	public static Vulnerability getVulnerability (String name) {
		if (vulns == null) {
			init();
		}
		return idToVuln.get(name);
	}
}
