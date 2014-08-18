/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.multiFuzz;
/**
 * Interface for different types of data to be inserted into selected {@link FuzzLocation} in a message 
 */
public interface Payload {
	/**
	 * Enumeration of different payload types.
	 */
	enum Type{
		STRING, REGEX, FILE, SCRIPT
	}
	/**
	 * Gets length parameter for String and Regex payloads
	 * Strings: Character count to which the String is cut/expanded to
	 * Regex: Maximum length of matching Strings generated
	 * @return	length parameter
	 */
	public int getLength();
	/**
	 * Sets length parameter for String and Regex payloads
	 * Strings: Character count to which the String is cut/expanded to
	 * Regex: Maximum length of matching Strings generated
	 * @param len	the new length parameter
	 */
	public void setLength(int len);
	/**
	 * Gets if recursive fuzzing is enabled for a file payload.
	 * @return	recursive parameter
	 */
	public boolean getRecursive();
	/**
	 * Sets dis-/enabling recursive fuzzing for a file payload.
	 * @param rec	recursive parameter
	 */
	public void setRecursive(boolean rec);
	/**
	 * Gets limit parameter of Regex payloads.
	 * @return maximum number of matching Strings generated
	 */
	public int getLimit();
	/**
	 * Sets maximum number of generated Strings
	 * @param l	the new limit
	 */
	public void setLimit(int l);
}
