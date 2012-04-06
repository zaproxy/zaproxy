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

package org.zaproxy.zap.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class LocaleUtils {

	private static Logger logger = Logger.getLogger(LocaleUtils.class);

	/**
	 * @return The list of available translations, ZAP provides
	 */
	public static List<String> getAvailableLocales() {
		List<String> locales = new ArrayList<String>();
		File dir = new File("lang");
		FilenameFilter filter = new FilenameExtensionFilter("properties", true);
		String[] files = dir.list(filter );

		// Always put English at the top
		locales.add("en_GB");

		if (files == null || files.length == 0) {
			logger.error("Failed to find any locale files in directory " + dir.getAbsolutePath());
		} else {
			Arrays.sort(files);
			
			for (String file : files) {
				if (file.startsWith(Constant.MESSAGES_PREFIX + "_")) {
					locales.add(file.substring(Constant.MESSAGES_PREFIX.length() + 1, file.indexOf(".")));
				}
			}
		}
		return locales;
	}
	
	/**
	 * @param locale
	 * @return the name of the language
	 */
	public static String getLocalDisplayName(String locale) {
		String desc = "" + locale;
		if (locale != null) {
			String[] langArray = locale.split("_");
	        Locale loc = null;
	        if (langArray.length == 1) loc = new Locale(langArray[0]);
	        if (langArray.length == 2) loc = new Locale(langArray[0], langArray[1]);
	        if (langArray.length == 3) loc = new Locale(langArray[0], langArray[1], langArray[2]);
	        if (loc != null) {
	        	desc = loc.getDisplayLanguage(loc);
	        }
		}
		return desc;
	}
}
