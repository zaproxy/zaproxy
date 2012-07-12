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

/**
 * A {@link XMLConfiguration} with character encoding always set to UTF-8
 * and delimiter parsing always disabled.
 * <p>
 * <b>Note:</b> This class should be used, <i>always</i>, to read/write from/to
 * the configurations' file used by ZAP, this way there shouldn't be any
 * problems with character encodings.
 * </p>
 * 
 * @see #setDelimiterParsingDisabled(boolean)
 */
public class ZapXmlConfiguration extends XMLConfiguration {

	private static final long serialVersionUID = -8598525138011232529L;

	public ZapXmlConfiguration() {
		super();
		
		super.setEncoding("UTF-8");
		
		// XXX Remove when commons-configuration is updated to version 1.9 or
		// higher.
		super.setListDelimiter('\0');
		// Changed because setDelimiterParsingDisabled cannot be true and
		// the list delimiter character must be defined to a character that
		// will not clash with characters normally used (like a comma).
		
		// Always uses \0 to not clash with any character that the user writes.
		// The same character used internally by AbstractConfiguration
		// when delimiter parsing is disabled.
		// (org.apache.commons.configuration.AbstractConfiguration#DISABLED_DELIMITER)
		
		
		
		// XXX Change to _true_ when commons-configuration is updated to version
		// 1.9 or higher.
		super.setDelimiterParsingDisabled(false);
		// Needs to be false to allow the use of setProperty(propertyName, List)
		// and have all the elements of the list added in each XML element (1).
		// If it's true, then all list elements are added on only one XML
		// element (2).
		// For more informations see:
		// https://issues.apache.org/jira/browse/CONFIGURATION-495

		// (1)
		// <anticsrf>
		//  <tokens>anticsrf</tokens>
		//  <tokens>CSRFToken</tokens>
		//  <tokens>__RequestVerificationToken</tokens>
		// </anticsrf>
		
		// (2)
		// <anticsrf>
		//  <tokens>[CSRFToken, __RequestVerificationToken, anticsrf]</tokens>
		// </anticsrf>
	}

	public ZapXmlConfiguration(String fileName) throws ConfigurationException {
		this();
		setFileName(fileName);
		load();
	}
	
	public ZapXmlConfiguration(File file) throws ConfigurationException {
		this();
		setFile(file);
		load();
	}
	
	public ZapXmlConfiguration(URL url) throws ConfigurationException {
		this();
		setURL(url);
		load();
	}
	
	/**
	 * Calling this method has <b>no</b> effect. The character encoding used is
	 * always the same, UTF-8.
	 * 
	 * @param encoding
	 */
	@Override
	public void setEncoding(String encoding) {
		// Always uses UTF-8
	}
	
	/**
	 * Calling this method has <b>no</b> effect. Delimiter parsing is always
	 * disabled.
	 * 
	 * @see #setDelimiterParsingDisabled(boolean)
	 */
	@Override
	public void setListDelimiter(char listDelimiter) {
		// Always use the default character, as calling
		// setDelimiterParsingDisabled as no effect, there is no need to change
		// the character.
	}
	
	/**
	 * Calling this method has <b>no</b> effect. The delimiter parsing is always
	 * disabled.
	 * 
	 * <p>
	 * <b>Note:</b> Currently the delimiter parsing is enabled because of an
	 * issue with commons-configuration, although there should be no problem
	 * because the list delimiter character is set to character '\0' (the
	 * character used by commons-configuration when delimiter parsing is
	 * disabled). The delimiter parsing will be effectively disabled when
	 * commons-configuration is updated to version 1.9 or higher.
	 * </p>
	 */
	@Override
	// XXX: Remove the "Note" in the above method documentation when
	// commons-configuration is updated to version 1.9 or higher.
	public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled) {
		// Always disabled.
	}
}
