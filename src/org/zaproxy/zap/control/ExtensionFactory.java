/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 ZAP development team
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
package org.zaproxy.zap.control;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.HelpUtilities;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.extension.help.ExtensionHelp;

public class ExtensionFactory {

    private static Logger log = Logger.getLogger(ExtensionFactory.class);

    private static Vector<Extension> listAllExtension = new Vector<>();
    private static TreeMap<String, Extension> mapAllExtension = new TreeMap<>();
    private static TreeMap<Integer, Extension> mapOrderToExtension = new TreeMap<>();
    private static List<Extension> unorderedExtensions = new ArrayList<>();

    private static AddOnLoader addOnLoader = null;

    public ExtensionFactory() {
        super();
    }

    private static AddOnLoader getAddOnLoader(List<File> extraDirs) {
        if (addOnLoader == null) {
        	File [] dirs = new File[extraDirs.size()+2];
        	dirs [0] = new File(Constant.getZapInstall(), Constant.FOLDER_PLUGIN);
        	dirs [1] = new File(Constant.getZapHome(), Constant.FOLDER_PLUGIN);
        	for (int i=0; i < extraDirs.size(); i++) {
            	dirs [2+i] = extraDirs.get(i);
        	}
            addOnLoader = new AddOnLoader(dirs);
        } else {
        	log.error("AddOnLoader initialised without additional directories");
        }
        return addOnLoader;
    }


    public static AddOnLoader getAddOnLoader() {
        if (addOnLoader == null) {
            addOnLoader = new AddOnLoader(new File[]{
                new File(Constant.getZapInstall(), Constant.FOLDER_PLUGIN),
                new File(Constant.getZapHome(), Constant.FOLDER_PLUGIN)});
        }
        return addOnLoader;
    }

    public static synchronized void loadAllExtension(ExtensionLoader extensionLoader, OptionsParam optionsParam) {
        log.info("Loading extensions");
        List<Extension> listExts = new ArrayList<>(CoreFunctionality.getBuiltInExtensions());
    	
        listExts.addAll(getAddOnLoader(optionsParam.getCheckForUpdatesParam().getAddonDirectories()).getExtensions());

        synchronized (mapAllExtension) {
            mapAllExtension.clear();
            for (int i = 0; i < listExts.size(); i++) {
                Extension extension = listExts.get(i);
                if (mapAllExtension.containsKey(extension.getName())) {
                    if (mapAllExtension.get(extension.getName()).getClass().equals(extension.getClass())) {
                        // Same name, same class so ignore
                        log.error("Duplicate extension: " + extension.getName() + " "
                                + extension.getClass().getCanonicalName());
                        continue;
                    } else {
                        // Same name but different class, log but still load it
                        log.error("Duplicate extension name: " + extension.getName() + " "
                                + extension.getClass().getCanonicalName()
                                + " " + mapAllExtension.get(extension.getName()).getClass().getCanonicalName());
                    }
                }
                if (extension.isDepreciated()) {
                    log.debug("Depreciated extension " + extension.getName());
                    continue;
                }
                extension.setEnabled(optionsParam.getConfig().getBoolean("ext." + extension.getName(), true));

                listAllExtension.add(extension);
                mapAllExtension.put(extension.getName(), extension);

                int order = extension.getOrder();
                if (order == 0) {
                    unorderedExtensions.add(extension);
                } else if (mapOrderToExtension.containsKey(order)) {
                    log.error("Duplicate order " + order + " "
                            + mapOrderToExtension.get(order).getName() + "/" + mapOrderToExtension.get(order).getClass().getCanonicalName()
                            + " already registered, "
                            + extension.getName() + "/" + extension.getClass().getCanonicalName()
                            + " will be added as an unordered extension");
                    unorderedExtensions.add(extension);
                } else {
                    mapOrderToExtension.put(order, extension);
                }

            }
            
            // Add the ordered extensions
            Iterator<Integer> iter = mapOrderToExtension.keySet().iterator();
            while (iter.hasNext()) {
                Integer order = iter.next();
                Extension ext = mapOrderToExtension.get(order);
                if (ext.isEnabled()) {
                    log.debug("Ordered extension " + order + " " + ext.getName());
                }
                loadMessagesAndAddExtension(extensionLoader, ext);
            }
            
            // And then the unordered ones
            for (Extension ext : unorderedExtensions) {
                if (ext.isEnabled()) {
                    log.debug("Unordered extension " + ext.getName());
                }
                loadMessagesAndAddExtension(extensionLoader, ext);
            }
        }
        
        log.info("Extensions loaded");
    }

    /**
     * Loads the messages of the {@code extension} and, if enabled, adds it to
     * the {@code extensionLoader} and loads the extension's help set.
     *
     * @param extensionLoader the extension loader
     * @param extension the extension
     * @see #loadMessages(Extension)
     * @see ExtensionLoader#addExtension(Extension)
     * @see #intitializeHelpSet(Extension)
     */
    private static void loadMessagesAndAddExtension(ExtensionLoader extensionLoader, Extension extension) {
        loadMessages(extension);
        if (extension.isEnabled() && extension.supportsDb(Model.getSingleton().getDb().getType()) &&  
        		(extension.supportsLowMemory() || ! Constant.isLowMemoryOptionSet())) {
            extensionLoader.addExtension(extension);
            intitializeHelpSet(extension);
        } else if (!extension.supportsDb(Model.getSingleton().getDb().getType())) {
            log.debug("Not loading extension " + extension.getName() + ": doesnt support " + Model.getSingleton().getDb().getType());
        } else if (extension.supportsLowMemory() || ! Constant.isLowMemoryOptionSet()) {
            log.debug("Not loading extension " + extension.getName() + ": doesnt support low memory option");
        }
    }

    public static synchronized void addAddOnExtension(
            ExtensionLoader extensionLoader,
            Configuration config,
            Extension extension) {
        synchronized (mapAllExtension) {
            addExtensionImpl(config, extension);

            if (extension.isEnabled()) {
                log.debug("Adding new extension " + extension.getName());
            }
            loadMessagesAndAddExtension(extensionLoader, extension);
        }
    }

    private static void addExtensionImpl(Configuration config, Extension extension) {
        if (mapAllExtension.containsKey(extension.getName())) {
            if (mapAllExtension.get(extension.getName()).getClass().equals(extension.getClass())) {
                // Same name, same class cant currently replace exts already loaded
                log.debug("Duplicate extension: " + extension.getName() + " " + extension.getClass().getCanonicalName());
                extension.setEnabled(false);
                return;
            }
            // Same name but different class, log but still load it
            log.error("Duplicate extension name: " + extension.getName() + " " + extension.getClass().getCanonicalName() + " "
                    + mapAllExtension.get(extension.getName()).getClass().getCanonicalName());
        }
        if (extension.isDepreciated()) {
            log.debug("Depreciated extension " + extension.getName());
            return;
        }
        extension.setEnabled(config.getBoolean("ext." + extension.getName(), true));

        listAllExtension.add(extension);
        mapAllExtension.put(extension.getName(), extension);
        // Order actually irrelevant when adding an addon;)
        int order = extension.getOrder();
        if (order == 0) {
            unorderedExtensions.add(extension);
        } else if (mapOrderToExtension.containsKey(order)) {
            log.error("Duplicate order " + order + " " + mapOrderToExtension.get(order).getName() + "/"
                    + mapOrderToExtension.get(order).getClass().getCanonicalName() + " already registered, "
                    + extension.getName() + "/" + extension.getClass().getCanonicalName()
                    + " will be added as an unordered extension");
            unorderedExtensions.add(extension);
        } else {
            mapOrderToExtension.put(order, extension);
        }
    }

    public static synchronized List<Extension> loadAddOnExtensions(ExtensionLoader extensionLoader, Configuration config, AddOn addOn) {
        List<Extension> listExts = getAddOnLoader().getExtensions(addOn);

        synchronized (mapAllExtension) {

            for (Extension extension : listExts) {
                addExtensionImpl(config, extension);
            }
            for (Extension ext : listExts) {
                if (ext.isEnabled()) {
                    log.debug("Adding new extension " + ext.getName());
                }
                loadMessagesAndAddExtension(extensionLoader, ext);
            }
        }
        return listExts;
    }

    private static void loadMessages(Extension ext) {
        ResourceBundle msg = getExtensionResourceBundle(ext);
        if (msg != null) {
            ext.setMessages(msg);
            Constant.messages.addMessageBundle(ext.getI18nPrefix(), ext.getMessages());
        }
    }

    private static ResourceBundle getExtensionResourceBundle(Extension ext) {
        String extensionPackage = ext.getClass().getPackage().getName();
        ClassLoader classLoader = ext.getClass().getClassLoader();
        try {
            // Try to load a message bundle in the new/default location
            String name = extensionPackage + ".resources." + Constant.MESSAGES_PREFIX;
            return getPropertiesResourceBundle(name, classLoader);
        } catch (MissingResourceException ignore) {
            // Try to load in the old location
            String oldLocation = extensionPackage + "." + Constant.MESSAGES_PREFIX;
            try {
                return getPropertiesResourceBundle(oldLocation, classLoader);
            } catch (MissingResourceException ignoreAgain) {
                // It will be using the standard message bundle
            }
        }
        return null;
    }

    private static ResourceBundle getPropertiesResourceBundle(String name, ClassLoader classLoader)
            throws MissingResourceException {
        return ResourceBundle.getBundle(
                name,
                Constant.getLocale(),
                classLoader,
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    }

    /**
     * If there are help files within the extension, they are loaded and merged
     * with existing help files if the core help was correctly loaded.
     */
    private static void intitializeHelpSet(Extension ext) {
        HelpBroker hb = ExtensionHelp.getHelpBroker();
        if (hb == null) {
            return;
        }

        URL helpSetUrl = getExtensionHelpSetUrl(ext);
        if (helpSetUrl != null) {
            try {
                log.debug("Load help files for extension '" + ext.getName() + "' and merge with core help.");
                HelpSet extHs = new HelpSet(ext.getClass().getClassLoader(), helpSetUrl);
                hb.getHelpSet().add(extHs);
            } catch (HelpSetException e) {
                log.error("An error occured while adding help file of extension '" + ext.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    private static URL getExtensionHelpSetUrl(Extension extension) {
        String extensionPackage = extension.getClass().getPackage().getName().replace('.', '/') + "/";
        URL helpSetUrl = findResource(
                extension.getClass().getClassLoader(),
                extensionPackage + "resources/help",
                "helpset",
                ".hs",
                Constant.getLocale());
        if (helpSetUrl == null) {
            // Search in old location
            helpSetUrl = findResource(
                    extension.getClass().getClassLoader(),
                    extensionPackage + "resource/help",
                    "helpset",
                    ".hs",
                    Constant.getLocale());
        }
        return helpSetUrl;
    }

    public static List<Extension> getAllExtensions() {
        return listAllExtension;
    }

    public static Extension getExtension(String name) {
        Extension test = mapAllExtension.get(name);
        return test;
    }

    public static void unloadAddOnExtension(Extension extension) {
        synchronized (mapAllExtension) {
            unloadMessages(extension);
            unloadHelpSet(extension);

            mapAllExtension.remove(extension.getName());
            listAllExtension.remove(extension);
            boolean isUnordered = true;
            for (Iterator<Extension> it = mapOrderToExtension.values().iterator(); it.hasNext();) {
                if (it.next() == extension) {
                    it.remove();
                    isUnordered = false;
                    break;
                }
            }
            if (isUnordered) {
                unorderedExtensions.remove(extension);
            }
        }
    }

    private static void unloadMessages(Extension extension) {
        ResourceBundle msg = extension.getMessages();
        if (msg != null) {
            Constant.messages.removeMessageBundle(extension.getI18nPrefix());
        }
    }

    private static void unloadHelpSet(Extension ext) {
        HelpBroker hb = ExtensionHelp.getHelpBroker();
        if (hb == null) {
            return;
        }

        URL helpSetUrl = getExtensionHelpSetUrl(ext);
        if (helpSetUrl != null) {
            HelpSet baseHelpSet = hb.getHelpSet();
            Enumeration<?> helpSets = baseHelpSet.getHelpSets();
            while (helpSets.hasMoreElements()) {
                HelpSet extensionHelpSet = (HelpSet) helpSets.nextElement();
                if (helpSetUrl.equals(extensionHelpSet.getHelpSetURL())) {
                    baseHelpSet.remove(extensionHelpSet);
                    break;
                }
            }
        }
    }

    /**
     * Finds and returns the URL to a resource with the given class loader, or
     * system class loader if {@code null}, for the given or default locales.
     * <p>
     * The resource pathname will be constructed using the parameters package
     * name, file name, file extension and candidate locales. The candidate
     * locales are created from the given locale using the method
     * {@code HelpUtilities#getCandidates(Locale)}.
     * </p>
     * <p>
     * The resource pathname is constructed as:
     *
     * <pre>
     * &quot;package name&quot; + &quot;candidate locale&quot; + '/' + &quot;file name&quot; + &quot;candidate locale&quot; + &quot;file extension&quot;
     * </pre>
     *
     * For example, with the following parameters:
     * <ul>
     * <li>package name - /org/zaproxy/zap/extension/example/resources/help</li>
     * <li>file name - helpset</li>
     * <li>file extension - .hs</li>
     * <li>locale - es_ES</li>
     * </ul>
     * and default locale "en_GB", it would produce the following resource
     * pathnames:
     *
     * <pre>
     * /org/zaproxy/zap/extension/example/resources/help_es_ES/helpset_es_ES.hs
     * /org/zaproxy/zap/extension/example/resources/help_es/helpset_es.hs
     * /org/zaproxy/zap/extension/example/resources/help/helpset.hs
     * /org/zaproxy/zap/extension/example/resources/help_en_GB/helpset_en_GB.hs
     * /org/zaproxy/zap/extension/example/resources/help_en/helpset_en.hs
     * </pre>
     *
     * The URL of the first existent resource is returned.
     * </p>
     *
     * @param cl the class loader that will be used to get the resource,
     * {@code null} the system class loader is used.
     * @param packageName the name of the package where the resource is
     * @param fileName the file name of the resource
     * @param fileExtension the file extension of the resource
     * @param locale the target locale of the required resource
     * @return An {@code URL} with the path to the resource or {@code null} if
     * not found.
     * @see HelpUtilities#getCandidates(Locale)
     */
    // Implementation based (read copied) from:
    // javax.help.HelpUtilities#getLocalizedResource(ClassLoader cl, String front, String back, Locale locale, boolean tryRead)
    // Changes:
    // - Removed the "tryRead" flag since it's not needed (it's set to try to read always);
    // - Replaced the use of StringBuffer with StringBuilder;
    // - Renamed parameters "front" to "packageName" and "back" to "name";
    // - Renamed variable "tail" to "candidateLocale";
    // - Renamed variable "name" to "resource";
    // - Added type parameter to "tails" enumeration (now "candidateLocales"), @SuppressWarnings annotation and removed the
    // String cast;
    // - Changed to use try-with-resource statement to manage the input stream.
    // - Changed to also append the "candidateLocale" to the packageName followed by character '/';
    private static URL findResource(ClassLoader cl, String packageName, String fileName, String fileExtension, Locale locale) {
        URL url;

        for (@SuppressWarnings("unchecked") Enumeration<String> candidateLocales = HelpUtilities.getCandidates(locale); candidateLocales.hasMoreElements();) {
            String candidateLocale = candidateLocales.nextElement();
            String resource = (new StringBuilder(packageName)).append(candidateLocale)
                    .append('/')
                    .append(fileName)
                    .append(candidateLocale)
                    .append(fileExtension)
                    .toString();
            if (cl == null) {
                url = ClassLoader.getSystemResource(resource);
            } else {
                url = cl.getResource(resource);
            }
            if (url != null) {
                // Try doing an actual read to be sure it exists
                try (InputStream is = url.openConnection().getInputStream()) {
                    if (is != null && is.read() != -1) {
                        return url;
                    }
                } catch (Throwable t) {
                    // ignore and continue looking
                }
            }
        }
        return null;
    }
}
