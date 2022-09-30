/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.extension.ext.ExtensionParam;
import org.zaproxy.zap.utils.ZapResourceBundleControl;

public class ExtensionFactory {

    private static Logger log = LogManager.getLogger(ExtensionFactory.class);

    private static Vector<Extension> listAllExtension = new Vector<>();
    private static TreeMap<String, Extension> mapAllExtension = new TreeMap<>();
    private static Map<Class<? extends Extension>, Extension> mapClassExtension = new HashMap<>();
    private static TreeMap<Integer, Extension> mapOrderToExtension = new TreeMap<>();
    private static List<Extension> unorderedExtensions = new ArrayList<>();
    private static Map<Extension, Boolean> extensionsWithMessages = new HashMap<>();

    private static AddOnLoader addOnLoader = null;

    public ExtensionFactory() {
        super();
    }

    public static AddOnLoader getAddOnLoader(List<File> extraDirs) {
        if (addOnLoader == null) {
            File[] dirs = new File[extraDirs.size() + 2];
            dirs[0] = new File(Constant.getZapInstall(), Constant.FOLDER_PLUGIN);
            dirs[1] = new File(Constant.getZapHome(), Constant.FOLDER_PLUGIN);
            for (int i = 0; i < extraDirs.size(); i++) {
                dirs[2 + i] = extraDirs.get(i);
            }
            addOnLoader = new AddOnLoader(dirs);
            List<AddOn> sortedAddOns =
                    new ArrayList<>(addOnLoader.getAddOnCollection().getInstalledAddOns());
            Collections.sort(
                    sortedAddOns,
                    new Comparator<AddOn>() {

                        @Override
                        public int compare(AddOn addOn, AddOn otherAddOn) {
                            return addOn.getId().compareTo(otherAddOn.getId());
                        }
                    });
            log.info("Installed add-ons: {}", sortedAddOns);
        } else {
            log.error("AddOnLoader initialised without additional directories");
        }
        return addOnLoader;
    }

    public static AddOnLoader getAddOnLoader() {
        if (addOnLoader == null) {
            addOnLoader =
                    new AddOnLoader(
                            new File[] {
                                new File(Constant.getZapInstall(), Constant.FOLDER_PLUGIN),
                                new File(Constant.getZapHome(), Constant.FOLDER_PLUGIN)
                            });
            log.info(
                    "Installed add-ons: {}", addOnLoader.getAddOnCollection().getInstalledAddOns());
        }
        return addOnLoader;
    }

    public static synchronized void loadAllExtension(
            ExtensionLoader extensionLoader, OptionsParam optionsParam) {
        log.info("Loading extensions");
        List<Extension> listExts = new ArrayList<>(CoreFunctionality.getBuiltInExtensions());

        listExts.addAll(getAddOnLoader().getExtensions());

        ExtensionParam extParam = optionsParam.getExtensionParam();
        synchronized (mapAllExtension) {
            mapAllExtension.clear();
            mapClassExtension.clear();
            for (int i = 0; i < listExts.size(); i++) {
                addExtensionImpl(listExts.get(i), extParam);
            }

            // Add the ordered extensions
            Iterator<Integer> iter = mapOrderToExtension.keySet().iterator();
            while (iter.hasNext()) {
                Integer order = iter.next();
                Extension ext = mapOrderToExtension.get(order);
                if (ext.isEnabled()) {
                    log.debug("Ordered extension {} {}", order, ext.getName());
                }
                loadMessagesAndAddExtension(extensionLoader, ext);
            }

            // And then the unordered ones
            for (Extension ext : unorderedExtensions) {
                if (ext.isEnabled()) {
                    log.debug("Unordered extension {}", ext.getName());
                }
                loadMessagesAndAddExtension(extensionLoader, ext);
            }
        }

        log.info("Extensions loaded");
    }

    /**
     * Loads the messages of the {@code extension} and, if enabled, adds it to the {@code
     * extensionLoader} and loads the extension's help set.
     *
     * @param extensionLoader the extension loader
     * @param extension the extension
     * @see #loadMessages(Extension)
     * @see ExtensionLoader#addExtension(Extension)
     */
    private static void loadMessagesAndAddExtension(
            ExtensionLoader extensionLoader, Extension extension) {
        loadMessages(extension);
        if (!extension.isEnabled()) {
            return;
        }

        if (!canBeLoaded(mapClassExtension, extension)) {
            return;
        }

        if (extension.supportsDb(Model.getSingleton().getDb().getType())
                && (extension.supportsLowMemory() || !Constant.isLowMemoryOptionSet())) {
            extensionLoader.addExtension(extension);
        } else if (!extension.supportsDb(Model.getSingleton().getDb().getType())) {
            log.debug(
                    "Not loading extension {}: doesn't support {}",
                    extension.getName(),
                    Model.getSingleton().getDb().getType());
            extension.setEnabled(false);
        } else if (extension.supportsLowMemory() || !Constant.isLowMemoryOptionSet()) {
            log.debug(
                    "Not loading extension {}: doesn't support low memory option",
                    extension.getName());
            extension.setEnabled(false);
        }
    }

    // Relax visibility to ease the tests.
    static boolean canBeLoaded(
            Map<Class<? extends Extension>, Extension> extensions, Extension extension) {
        return canBeLoaded(extensions, extension, new ArrayList<>());
    }

    private static boolean canBeLoaded(
            Map<Class<? extends Extension>, Extension> extensions,
            Extension extension,
            List<Extension> extsBeingProcessed) {
        if (extsBeingProcessed.contains(extension)) {
            log.error("Dependency loop with \"{}\" and {}", extension, extsBeingProcessed);
            return false;
        }

        List<Class<? extends Extension>> dependencies = extension.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }

        extsBeingProcessed.add(extension);
        for (Class<? extends Extension> dependency : dependencies) {
            Extension extDep = extensions.get(dependency);
            if (extDep == null) {
                logUnableToLoadExt(extension, "missing dependency", dependency);
                extension.setEnabled(false);
                return false;
            }
            if (!extDep.isEnabled()) {
                logUnableToLoadExt(extension, "dependency not enabled", dependency);
                extension.setEnabled(false);
                return false;
            }
            if (!canBeLoaded(extensions, extDep, extsBeingProcessed)) {
                logUnableToLoadExt(extension, "can not load dependency", dependency);
                extension.setEnabled(false);
                return false;
            }
        }

        extsBeingProcessed.remove(extension);
        return true;
    }

    private static void logUnableToLoadExt(
            Extension extension, String reason, Class<? extends Extension> dependency) {
        log.warn("Unable to load \"{}\", {}: {}", extension, reason, dependency.getCanonicalName());
    }

    public static synchronized void addAddOnExtension(
            ExtensionLoader extensionLoader, Configuration config, Extension extension) {
        synchronized (mapAllExtension) {
            addExtensionImpl(extension, Model.getSingleton().getOptionsParam().getExtensionParam());

            if (extension.isEnabled()) {
                log.debug("Adding new extension {}", extension.getName());
            }
            loadMessagesAndAddExtension(extensionLoader, extension);
        }
    }

    private static void addExtensionImpl(Extension extension, ExtensionParam extensionParam) {
        if (mapAllExtension.containsKey(extension.getName())) {
            if (mapAllExtension.get(extension.getName()).getClass().equals(extension.getClass())) {
                // Same name, same class so ignore
                log.error(
                        "Duplicate extension: {} {}",
                        extension.getName(),
                        extension.getClass().getCanonicalName());
                extension.setEnabled(false);
                return;
            }
            // Same name but different class, log but still load it
            log.error(
                    "Duplicate extension name: {} {} {}",
                    extension.getName(),
                    extension.getClass().getCanonicalName(),
                    mapAllExtension.get(extension.getName()).getClass().getCanonicalName());
        }
        if (extension.isDepreciated()) {
            log.debug("Depreciated extension {}", extension.getName());
            extension.setEnabled(false);
            return;
        }

        AddOn addOn = extension.getAddOn();
        boolean mandatory = addOn != null && addOn.isMandatory();
        extension.setEnabled(mandatory || extensionParam.isExtensionEnabled(extension.getName()));

        listAllExtension.add(extension);
        mapAllExtension.put(extension.getName(), extension);
        mapClassExtension.put(extension.getClass(), extension);

        int order = extension.getOrder();
        if (order == 0) {
            unorderedExtensions.add(extension);
        } else if (mapOrderToExtension.containsKey(order)) {
            log.error(
                    "Duplicate order {} {}/{} already registered, {}/{} will be added as an unordered extension",
                    order,
                    mapOrderToExtension.get(order).getName(),
                    mapOrderToExtension.get(order).getClass().getCanonicalName(),
                    extension.getName(),
                    extension.getClass().getCanonicalName());
            unorderedExtensions.add(extension);
        } else {
            mapOrderToExtension.put(order, extension);
        }
    }

    public static synchronized List<Extension> loadAddOnExtensions(
            ExtensionLoader extensionLoader, Configuration config, AddOn addOn) {
        List<Extension> listExts = getAddOnLoader().getExtensions(addOn);

        synchronized (mapAllExtension) {
            ExtensionParam extParam = Model.getSingleton().getOptionsParam().getExtensionParam();
            for (Extension extension : listExts) {
                addExtensionImpl(extension, extParam);
            }
            for (Extension ext : listExts) {
                if (ext.isEnabled()) {
                    log.debug("Adding new extension {}", ext.getName());
                }
                loadMessagesAndAddExtension(extensionLoader, ext);
            }
        }
        return listExts;
    }

    private static void loadMessages(Extension ext) {
        AddOn addOn = ext.getAddOn();
        if (addOn == null) {
            // Core extensions use core resource bundle.
            ext.setMessages(Constant.messages.getCoreResourceBundle());
            return;
        }

        ResourceBundle msg = getExtensionResourceBundle(ext);
        if (msg != null) {
            ext.setMessages(msg);
            extensionsWithMessages.put(ext, Boolean.TRUE);
            Constant.messages.addMessageBundle(ext.getI18nPrefix(), ext.getMessages());
        } else if (addOn.getResourceBundle() != null) {
            ext.setMessages(addOn.getResourceBundle());
        } else {
            ext.setMessages(Constant.messages.getCoreResourceBundle());
        }
    }

    private static ResourceBundle getExtensionResourceBundle(Extension ext) {
        Package extPackage = ext.getClass().getPackage();
        String extensionPackage = extPackage != null ? extPackage.getName() + "." : "";
        ClassLoader classLoader = ext.getClass().getClassLoader();
        try {
            // Try to load a message bundle in the new/default location
            String name = extensionPackage + "resources." + Constant.MESSAGES_PREFIX;
            return getPropertiesResourceBundle(name, classLoader);
        } catch (MissingResourceException ignore) {
            // Try to load in the old location
            String oldLocation = extensionPackage + Constant.MESSAGES_PREFIX;
            try {
                return getPropertiesResourceBundle(oldLocation, classLoader);
            } catch (MissingResourceException ignoreAgain) {
                // It will be using a fallback message bundle
            }
        }
        return null;
    }

    private static ResourceBundle getPropertiesResourceBundle(String name, ClassLoader classLoader)
            throws MissingResourceException {
        return ResourceBundle.getBundle(
                name, Constant.getLocale(), classLoader, new ZapResourceBundleControl());
    }

    public static List<Extension> getAllExtensions() {
        return listAllExtension;
    }

    public static Extension getExtension(String name) {
        return mapAllExtension.get(name);
    }

    public static void unloadAddOnExtension(Extension extension) {
        synchronized (mapAllExtension) {
            unloadMessages(extension);

            mapAllExtension.remove(extension.getName());
            mapClassExtension.remove(extension.getClass());
            listAllExtension.remove(extension);
            boolean isUnordered = true;
            for (Iterator<Extension> it = mapOrderToExtension.values().iterator(); it.hasNext(); ) {
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
        if (extensionsWithMessages.remove(extension) == null) {
            return;
        }

        ResourceBundle msg = extension.getMessages();
        if (msg != null) {
            Constant.messages.removeMessageBundle(extension.getI18nPrefix());
        }
    }
}
