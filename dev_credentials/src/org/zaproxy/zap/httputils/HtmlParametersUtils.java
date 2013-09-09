/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.httputils;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.parosproxy.paros.network.HtmlParameter;

public class HtmlParametersUtils {

	// private static String PATTERN = "([^=&]*)=([^=&]*)";

	/**
	 * Extracts a set of parameters from the provided string. The type of the HtmlParameter is set
	 * as the one provided.
	 * <p>
	 * NOTE: Parameters not separated by '&' and '=' are not properly parsed.
	 * </p>
	 * 
	 * @param type the type
	 * @param params the params
	 * @return the params set
	 */
	public static Set<HtmlParameter> getParamsSet(HtmlParameter.Type type, String params) {
		if (params == null || params.isEmpty()) {
			return Collections.emptySet();
		}

		TreeSet<HtmlParameter> set = new TreeSet<>();
		String[] keyValue = Pattern.compile("&", Pattern.CASE_INSENSITIVE).split(params);
		String key = null;
		String value = null;
		int pos = 0;
		for (int i = 0; i < keyValue.length; i++) {
			key = null;
			value = null;
			pos = keyValue[i].indexOf('=');
			if (pos > 0) {
				key = keyValue[i].substring(0, pos);
				value = keyValue[i].substring(pos + 1);
				set.add(new HtmlParameter(type, key, value));
			} else if (keyValue[i].length() > 0) {
				set.add(new HtmlParameter(type, keyValue[i], ""));
			}
		}

		return set;
	}
}
