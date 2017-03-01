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
    private Map<String, ResourceBundle> addonMessages = new HashMap<>();
    
	private static final Logger logger = Logger.getLogger(I18N.class);

    public I18N (Locale locale) {
    	setLocale(locale);
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

    /**
     * Gets the String with the given key surrounded by {@code <html><p>} tags.
     * @param key the key of the string
     * @return the string read wrapped in HTML and paragraph tags
     */
	public String getHtmlWrappedString(String key) {
		String values = getString(key);
		if (values == null)
			return null;
		return "<html><p>" + values + "</p></html>";
    }
    
    /**
     * Returns the specified char from the language file. 
     * As these are typically used for mnemnoics the 'null' char is returned if the key is not defined 
     * @param key the key of the char
     * @return the char read, or null char if not found
     */
    public char getChar(String key) {
    	try {
			String str = this.getString(key);
			if (str.length() > 0) {
				return str.charAt(0);
			}
		} catch (Exception e) {
			// Not defined
		}
		return '\u0000';
    }
    
	/**
	 * Sets the current locale to the given one and gets a resource bundle for new locale.
	 * <p>
	 * The call to this method has no effect if the given locale is the same as the current one.
	 * </p>
	 *
	 * @param locale the new locale
	 * @see ResourceBundle#getBundle(String, Locale)
	 */
	public void setLocale (Locale locale) {
		if (locale.equals(this.locale)) {
			return;
		}
    	this.locale = locale;
    	this.stdMessages = ResourceBundle.getBundle(Constant.MESSAGES_PREFIX, locale,
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
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
