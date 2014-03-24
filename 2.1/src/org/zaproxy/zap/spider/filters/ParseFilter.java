/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.spider.filters;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A ParseFilter is used to filter which resources should be parsed by the Spider after they have
 * already been fetched and which shouldn't.
 */
public abstract class ParseFilter {

	/** The Constant log. */
	protected static final Logger log = Logger.getLogger(ParseFilter.class);

	/**
	 * Checks if the resource must be ignored and not processed.
	 * 
	 * @param responseMessage the response message after the resource was fetched
	 * @return true, if is filtered
	 */
	public abstract boolean isFiltered(HttpMessage responseMessage);
}
