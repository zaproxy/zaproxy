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

import java.util.Collections;
import java.util.List;

import org.parosproxy.paros.Constant;

public class Vulnerabilities {
	
	private static VulnerabilitiesI18NMap vulnerabilitiesI18NMap = null;
	
	private Vulnerabilities() {
	}

	private static synchronized void init() {
		if (vulnerabilitiesI18NMap == null) {
			VulnerabilitiesLoader loader = new VulnerabilitiesLoader(Constant.getZapInstall() + Constant.LANG_DIR, Constant.VULNS_BASE);
			vulnerabilitiesI18NMap = loader.load();
		}
	}

	/**
	 * Gets an unmodifiable {@code List} containing all the
	 * {@code Vulnerability} for the current active Locale.
	 * They are loaded from the xml files as specified by the {@code Constant}.
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
		initializeIfEmpty();
		return Collections.unmodifiableList(vulnerabilitiesI18NMap.getVulnerabilityList(Constant.getLocale().toString()));
	}
	
	public static Vulnerability getVulnerability (String name) {
		initializeIfEmpty();
		return vulnerabilitiesI18NMap.getVulnerabilityByName(name, Constant.getLocale().toString());
	}

	private static void initializeIfEmpty() {
		if (vulnerabilitiesI18NMap == null) {
			init();
		}
	}
}
