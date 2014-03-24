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

	private static final Logger logger = Logger.getLogger(LocaleUtils.class);
	
	private static final String MESSAGES_BASE_FILENAME = Constant.MESSAGES_PREFIX.toLowerCase(Locale.ENGLISH) + "_";
	private static final String MESSAGES_EXTENSION = "properties";

	/**
	 * @return The list of available translations, ZAP provides
	 */
	public static List<String> getAvailableLocales() {
		File dir = new File("lang");
		FilenameFilter filter = new MessagesPropertiesFilenameFilter();
		String[] files = dir.list(filter);

		List<String> locales;
		if (files == null || files.length == 0) {
			logger.error("Failed to find any locale files in directory " + dir.getAbsolutePath());
			locales = new ArrayList<>(1);
		} else {
			locales = new ArrayList<>(files.length + 1);
			// XXX: Doing the sort here doesn't add much to the end user.
			// This sort the locales (es_ES, in_ID). The sort should be made on
			// the names ("español", "Bahasa Indonesia") returned by the method
			// getLocalDisplayName() (note that the order would be different).
			// In the end what is shown to the user, in the combo box, is not
			// sorted, knowing that the name "English" would have to be the 
			// first.
			Arrays.sort(files);
			
			final int baseFilenameLength = MESSAGES_BASE_FILENAME.length();
			for (String file : files) {
				locales.add(file.substring(baseFilenameLength, file.indexOf(".")));
			}
		}
		
		// Always put English at the top
		locales.add(0, "en_GB");

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
	        if (langArray.length == 1) {
	            loc = new Locale(langArray[0]);
	        } else if (langArray.length == 2) {
	            loc = new Locale(langArray[0], langArray[1]);
	        } else if (langArray.length == 3) {
	            loc = new Locale(langArray[0], langArray[1], langArray[2]);
	        }
	        if (loc != null) {
	        	desc = loc.getDisplayLanguage(loc);
	        }
		}
		return desc;
	}
	
	private static final class MessagesPropertiesFilenameFilter implements FilenameFilter {
		
		@Override
		public boolean accept(File dir, String name) {
			final String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
			
			return lowerCaseName.startsWith(MESSAGES_BASE_FILENAME) && lowerCaseName.endsWith(MESSAGES_EXTENSION);
		}
		
	}
}
