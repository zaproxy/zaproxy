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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public final class LocaleUtils {

	private static final Logger logger = Logger.getLogger(LocaleUtils.class);
	
	private static final String MESSAGES_BASE_FILENAME = Constant.MESSAGES_PREFIX.toLowerCase(Locale.ENGLISH) + "_";

	private LocaleUtils() {
	}

	/**
	 * Regular expression to match a language of a {@code Locale}.
	 * 
	 * @see #COUNTRY_LOCALE_REGEX
	 * @since 2.4.0
	 */
	public static final String LANGUAGE_LOCALE_REGEX = "[a-zA-Z]{2,8}";

	/**
	 * Regular expression to match a country/region of a {@code Locale}.
	 * 
	 * @see #LANGUAGE_LOCALE_REGEX
	 * @since 2.4.0
	 */
	public static final String COUNTRY_LOCALE_REGEX = "[a-zA-Z]{2}|[0-9]{3}";

	/**
	 * Convenience method that calls the method {@code #createResourceFilePattern(String, String)}, with parameters
	 * {@code Constant.MESSAGES_PREFIX} and {@code Constant.MESSAGES_EXTENSION}, respectively.
	 *
	 * @return a {@code Pattern} that matches the Messages.properties files of different {@code Locale}s
	 * @see #createResourceFilesPattern(String, String)
	 * @see Constant#MESSAGES_PREFIX
	 * @see Constant#MESSAGES_EXTENSION
	 * @since 2.4.0
	 */
	public static Pattern createMessagesPropertiesFilePattern() {
		return createResourceFilesPattern(Constant.MESSAGES_PREFIX, Constant.MESSAGES_EXTENSION);
	}

	/**
	 * Returns a regular expression to match source and translated resource filenames with the given {@code fileName} and
	 * {@code fileExtension}.
	 * <p>
	 * For example, with {@code fileName} as "Messages" and {@code fileExtension} as ".properties" the returned pattern would
	 * match:
	 * <ul>
	 * <li>Messages.properties</li>
	 * <li>Messages_en.properties</li>
	 * <li>Messages_en_GB.properties</li>
	 * </ul>
	 *
	 * @param fileName the name of the resource files
	 * @param fileExtension the extension of the resource files
	 * @return the regular expression to match resource filenames
	 * @throws IllegalArgumentException if the given {@code fileName} or {@code fileExtension} is {@code null}.
	 * @see #createResourceFilesPattern(String, String)
	 * @see #LANGUAGE_LOCALE_REGEX
	 * @see #COUNTRY_LOCALE_REGEX
	 * @since 2.4.0
	 */
	public static String createResourceFilesRegex(String fileName, String fileExtension) {
		if (fileName == null) {
			throw new IllegalArgumentException("Parameter fileName must not be null.");
		}
		if (fileExtension == null) {
			throw new IllegalArgumentException("Parameter fileExtension must not be null.");
		}

		StringBuilder strBuilder = new StringBuilder(fileName.length() + LANGUAGE_LOCALE_REGEX.length()
				+ COUNTRY_LOCALE_REGEX.length() + fileExtension.length() + 13);
		strBuilder.append(Pattern.quote(fileName));
		strBuilder.append("(?:_").append(LANGUAGE_LOCALE_REGEX);
		strBuilder.append("(?:_").append(COUNTRY_LOCALE_REGEX).append(")?").append(")?");
		strBuilder.append(Pattern.quote(fileExtension));
		strBuilder.append('$');
		return strBuilder.toString();
	}

	/**
	 * Returns a {@code Pattern} to match source and translated resource filenames with the given {@code fileName} and
	 * {@code fileExtension}.
	 * <p>
	 * For example, with {@code fileName} as "Messages" and {@code fileExtension} as ".properties" the returned pattern would
	 * match:
	 * <ul>
	 * <li>Messages.properties</li>
	 * <li>Messages_en.properties</li>
	 * <li>Messages_en_GB.properties</li>
	 * </ul>
	 * <p>
	 * The pattern is case-sensitive.
	 *
	 * @param fileName the name of the resource files
	 * @param fileExtension the extension of the resource files
	 * @return the {@code Pattern} to match resource filenames
	 * @throws IllegalArgumentException if the given {@code fileName} or {@code fileExtension} is {@code null}.
	 * @see #createResourceFilesRegex(String, String)
	 * @see #LANGUAGE_LOCALE_REGEX
	 * @see #COUNTRY_LOCALE_REGEX
	 * @since 2.4.0
	 */
	public static Pattern createResourceFilesPattern(String fileName, String fileExtension) {
		return Pattern.compile(createResourceFilesRegex(fileName, fileExtension));
	}
	
	/**
	 * @return The list of available translations, ZAP provides
	 */
	public static List<String> getAvailableLocales() {
		File dir = new File(Constant.getZapInstall(), Constant.LANG_DIR);
		FilenameFilter filter = new MessagesPropertiesFilenameFilter();
		String[] files = dir.list(filter);

		List<String> locales;
		if (files == null || files.length == 0) {
			logger.error("Failed to find any locale files in directory " + dir.getAbsolutePath());
			locales = new ArrayList<>(1);
		} else {
			List<String> filesList = new ArrayList<>(Arrays.asList(files));
			// Remove source/default file (Messages.properties), it's added later
			filesList.remove(Constant.MESSAGES_PREFIX + Constant.MESSAGES_EXTENSION);
			locales = new ArrayList<>(filesList.size());

			// XXX: Doing the sort here doesn't add much to the end user.
			// This sort the locales (es_ES, in_ID). The sort should be made on
			// the names ("español", "Bahasa Indonesia") returned by the method
			// getLocalDisplayName() (note that the order would be different).
			// In the end what is shown to the user, in the combo box, is not
			// sorted, knowing that the name "English" would have to be the 
			// first.
			Collections.sort(filesList);
			
			final int baseFilenameLength = MESSAGES_BASE_FILENAME.length();
			for (String file : filesList) {
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
		private final Pattern messagesPropertiesPattern = createMessagesPropertiesFilePattern();
		
		@Override
		public boolean accept(File dir, String name) {
			return messagesPropertiesPattern.matcher(name).matches();
		}
		
	}
}
