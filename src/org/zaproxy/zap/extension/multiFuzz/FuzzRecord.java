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

import java.util.List;

import javax.swing.ImageIcon;

import org.zaproxy.zap.utils.Pair;
/**
 * Interface to be implemented by entries in the record table displaying fuzzing results.
 */
public interface FuzzRecord {
	/**
	 * Sets entry name.
	 * @param s	the new name
	 */
	public void setName(String s);
	/**
	 * Gets entry name
	 * @return	the entry name
	 */
	public String getName();
	/**
	 * Gets result status (Error, Successful...) of the entry.
	 * @return the entry result
	 */
	public Pair<String, ImageIcon> getResult();
	/**
	 * Gets {@link Payload} list that was inserted.
	 * @return	the {@link Payload} list
	 */
	public List<String> getPayloads();
	/**
	 * Gets inclusion of this data entry into the generation of result diagrams
	 * @return inclusion parameter
	 */
	public Boolean isIncluded();
	/**
	 * Sets dis-/enabling inclusion of this data entry into the generation of result diagrams.
	 * @param b
	 */
	public void setIncluded(Boolean b);
	/**
	 * Gets alternative result messages.
	 * @return alternative message
	 */
	public String getCustom();
}
