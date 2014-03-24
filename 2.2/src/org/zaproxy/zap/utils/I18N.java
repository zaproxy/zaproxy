package org.zaproxy.zap.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class I18N {
	
	/*
	 * Utility class for handling multiple message bundles.
	 * This allows extensions to manage their own i18n files.
	 */

    private ResourceBundle stdMessages = null;
    private Locale locale = null;
    private Map<String, ResourceBundle> addonMessages = new HashMap<String, ResourceBundle>();
    
	private static final Logger logger = Logger.getLogger(I18N.class);

    public I18N (Locale locale) {
    	this.locale = locale;
    	this.stdMessages = ResourceBundle.getBundle(Constant.MESSAGES_PREFIX, locale);
    }
    
    public void addMessageBundle(String prefix, ResourceBundle bundle) {
		logger.debug("Adding message bundle with prefix: " + prefix);
    	if (addonMessages.containsKey(prefix)) {
    		logger.error("Adding message bundle with duplicate prefix: " + prefix);
    	}
    	addonMessages.put(prefix, bundle);
    }
    
    public void removeMessageBundle(String prefix) {
        logger.debug("Removing message bundle with prefix: " + prefix);
        if (addonMessages.containsKey(prefix)) {
            addonMessages.remove(prefix);
        } else {
            logger.debug("Message bundle not found, prefix: " + prefix);
        }
    }
    
    public ResourceBundle getMessageBundle(String prefix) {
    	return this.addonMessages.get(prefix);
    }
    
    public String getString(String key) {
    	if (key.indexOf(".") > 0) {
    		String prefix = key.substring(0, key.indexOf("."));
    		ResourceBundle bundle = this.addonMessages.get(prefix);
    		if (bundle != null && bundle.containsKey(key)) {
    			return bundle.getString(key);
    		}
    	}
    	return this.stdMessages.getString(key);
    }
    
	public void setLocale (Locale locale) {
    	this.locale = locale;
    	this.stdMessages = ResourceBundle.getBundle(Constant.MESSAGES_PREFIX, locale);
    }

    
    public Locale getLocal() {
    	return this.locale;
    }

	public boolean containsKey(String key) {
		if (key.indexOf(".") > 0) {
    		String prefix = key.substring(0, key.indexOf("."));
    		ResourceBundle bundle = this.addonMessages.get(prefix);
    		if (bundle != null && bundle.containsKey(key)) {
    			return true;
    		}
    	}
    	return this.stdMessages.containsKey(key);
	}
	
    public String getString(String key, Object... params  ) {
        try {
            return MessageFormat.format(this.getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
