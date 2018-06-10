package org.zaproxy.zap.utils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

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

    /**
     * A set of missing keys, to {@link #handleMissingResourceException(MissingResourceException) log an error} just once.
     * <p>
     * The keys are removed when the corresponding bundle is removed (e.g. same prefix) or if a new bundle already defines it
     * (e.g. contained in a resource bundle of an updated add-on), still, it might be possible that some keys are kept in memory
     * until ZAP is shutdown.
     */
    private Set<String> missingKeys = Collections.synchronizedSet(new HashSet<>());
    
	private static final Logger logger = Logger.getLogger(I18N.class);

    public I18N (Locale locale) {
    	setLocale(locale);
    }

    /**
     * Gets the core resource bundle.
     *
     * @return the core resource bundle, never {@code null}.
     * @since 2.7.0
     */
    public ResourceBundle getCoreResourceBundle() {
        return this.stdMessages;
    }
    
    public void addMessageBundle(String prefix, ResourceBundle bundle) {
		logger.debug("Adding message bundle with prefix: " + prefix);
    	if (addonMessages.containsKey(prefix)) {
    		logger.error("Adding message bundle with duplicate prefix: " + prefix);
    	}
    	addonMessages.put(prefix, bundle);
    }
    
    public void removeMessageBundle(String prefix) {
        missingKeys.removeIf(k -> k.startsWith(prefix));

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
    
    /**
     * Gets the message with the given key.
     * <p>
     * The message will be obtained either from the core {@link ResourceBundle} or a {@code ResourceBundle} of an add-on
     * (depending on the prefix of the key).
     * <p>
     * <strong>Note:</strong> Since TODO add version this method no longer throws a {@code MissingResourceException} if the key
     * does not exist, instead it logs an error and returns the key itself. This avoids breaking ZAP when a resource message is
     * accidentally missing. Use {@link #containsKey(String)} instead to know if a message exists or not.
     *
     * @param key the key.
     * @return the message.
     * @see #getString(String, Object...)
     * @see #getMessageBundle(String)
     */
    public String getString(String key) {
        try {
            return this.getStringImpl(key);
        } catch (MissingResourceException e) {
            return handleMissingResourceException(e);
        }
    }

    private String handleMissingResourceException(MissingResourceException e) {
        logger.error("Failed to load a message:", e);
        String key = e.getKey();
        missingKeys.add(key);
        return missingKeyReplacement(key);
    }

    private static String missingKeyReplacement(String key) {
        return '!' + key  + '!';
    }

    private String getStringImpl(String key) {
        if (missingKeys.contains(key)) {
            if (!containsKey(key)) {
                return missingKeyReplacement(key);
            }
            missingKeys.remove(key);
        }

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
		String values = getStringImpl(key);
		if (values == null)
			return null;
		return "<html><p>" + values + "</p></html>";
    }
    
    /**
     * Returns the specified char from the language file. 
     * As these are typically used for mnemonics the 'null' char is returned if the key is not defined
     * @param key the key of the char
     * @return the char read, or null char if not found
     */
    public char getChar(String key) {
    	try {
			String str = this.getStringImpl(key);
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
    	this.stdMessages = ResourceBundle.getBundle(Constant.MESSAGES_PREFIX, locale, new ZapResourceBundleControl());
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
	
    /**
     * Gets the message with the given key, formatted with the given parameters.
     * <p>
     * The message will be obtained either from the core {@link ResourceBundle} or a {@code ResourceBundle} of an add-on
     * (depending on the prefix of the key) and then {@link MessageFormat#format(String, Object...) formatted}.
     * <p>
     * <strong>Note:</strong> This method does not throw a {@code MissingResourceException} if the key does not exist, instead
     * it logs an error and returns the key itself. This avoids breaking ZAP when a resource message is accidentally missing.
     * Use {@link #containsKey(String)} instead to know if a message exists or not.
     *
     * @param key the key.
     * @param params the parameters to format the message.
     * @return the message formatted.
     * @see #getString(String)
     */
    public String getString(String key, Object... params  ) {
        try {
            return MessageFormat.format(this.getStringImpl(key), params);
        } catch (MissingResourceException e) {
            return handleMissingResourceException(e);
        }
    }
}
