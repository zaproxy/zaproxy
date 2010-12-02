package org.zaproxy.zap.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class LocaleUtils {

	private static Logger logger = Logger.getLogger(LocaleUtils.class);

	public static List<String> getAvailableLocales() {
		List<String> locales = new ArrayList<String>();
		File dir = new File(".");
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
	
	public static String getLocalDisplayName (String locale) {
		String desc = locale;
		try {
			desc = Constant.messages.getString("view.locale." + locale);
		} catch (MissingResourceException e) {
			// Try picking up from the java built in ones
	        String[] langArray = locale.split("_");
	        Locale loc = new Locale(langArray[0], langArray[1]);
	        if (loc != null) {
	        	desc = loc.getDisplayLanguage();
	        }
		}
		return desc;
	}
}
