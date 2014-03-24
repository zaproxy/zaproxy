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
package org.zaproxy.zap.utils;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class ZapXmlConfiguration extends XMLConfiguration {

	private static final long serialVersionUID = -8598525138011232529L;

	public ZapXmlConfiguration() {
		init();
	}

	public ZapXmlConfiguration(String fileName) throws ConfigurationException {
		init();
		setFileName(fileName);
		load();
	}
	
	public ZapXmlConfiguration(File file) throws ConfigurationException {
		init();
		setFile(file);
		load();
	}
	
	public ZapXmlConfiguration(URL url) throws ConfigurationException {
		init();
		setURL(url);
		load();
	}
	
	@Override
	public void setEncoding(String encoding) {
		//Always uses UTF-8
	}
	
	@Override
	public void setListDelimiter(char listDelimiter) {
		//Always uses \0 to not clash with any character that the user writes.
	}
	
	@Override
	public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled) {
		//Needs to be false to allow the use of setPorperty(propertyName, List)
		//and have all the elements of the list added in each XML element.
		//If it's true, then all list elements are added on only one XML element.
	}
	
	private void init() {
		super.setEncoding("UTF-8");
		super.setListDelimiter('\0');
		super.setDelimiterParsingDisabled(false);
	}
}
