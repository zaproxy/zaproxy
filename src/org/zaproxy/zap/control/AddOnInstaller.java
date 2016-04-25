/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.extension.pscan.PassiveScanner;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

/**
 * Helper class responsible to install and uninstall add-ons and all its (dynamically installable) components 
 * ({@code Extension}s, {@code Plugin}s, {@code PassiveScanner}s and files).
 * 
 * @see Extension
 * @see org.parosproxy.paros.core.scanner.Plugin
 * @see PassiveScanner
 * @since 2.3.0
 */
public final class AddOnInstaller {

    private static final Logger logger = Logger.getLogger(AddOnInstaller.class);

    private AddOnInstaller() {
    }

    /**
     * Installs all the (dynamically installable) components ({@code Extension}s, {@code Plugin}s, {@code PassiveScanner}s and
     * files) of the given {@code addOn}.
     * <p>
     * It's also responsible to notify the installed extensions when the installation has finished by calling the method
     * {@code Extension#postInstall()}.
     * <p>
     * The components are installed in the following order:
     * <ol>
     * <li>Files;</li>
     * <li>Extensions;</li>
     * <li>Active scanners;</li>
     * <li>Passive scanners.</li>
     * </ol>
     * The files are installed first as they might be required by extensions and scanners.
     * 
     * @param addOnClassLoader the class loader of the given {@code addOn}
     * @param addOn the add-on that will be installed
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     * @see Extension#postInstall()
     */
    public static void install(AddOnClassLoader addOnClassLoader, AddOn addOn) {
        installAddOnFiles(addOnClassLoader, addOn, true);
        List<Extension> listExts = installAddOnExtensions(addOn);
        installAddOnActiveScanRules(addOn, addOnClassLoader);
        installAddOnPassiveScanRules(addOn, addOnClassLoader);
 
        // postInstall actions
        for (Extension ext : listExts) {
            try {
                ext.postInstall();
            } catch (Exception e) {
                logger.error("Post install method failed for add-on " + addOn.getId() + " extension " + ext.getName());
            }
        }
    }

    /**
     * Uninstalls all the (dynamically installable) components ({@code Extension}s, {@code Plugin}s, {@code PassiveScanner}s and
     * files) of the given {@code addOn}.
     * <p>
     * The components are uninstalled in the following order (inverse to installation):
     * <ol>
     * <li>Passive scanners;</li>
     * <li>Active scanners;</li>
     * <li>Extensions;</li>
     * <li>Files;</li>
     * </ol>
     * 
     * @param addOn the add-on that will be uninstalled
     * @param callback the callback that will be notified of the progress of the uninstallation
     * @return {@code true} if the add-on was uninstalled without errors, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     * @see #softUninstall(AddOn, AddOnUninstallationProgressCallback)
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     */
    public static boolean uninstall(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        Validate.notNull(addOn, "Parameter addOn must not be null.");
        validateCallbackNotNull(callback);

        try {
            boolean uninstalledWithoutErrors = true;
            uninstalledWithoutErrors &= uninstallAddOnPassiveScanRules(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnActiveScanRules(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnExtensions(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnFiles(addOn, callback);
    
            return uninstalledWithoutErrors;
        } catch (Throwable e) {
            logger.error("An error occurred while uninstalling the add-on: " + addOn.getId(), e);
            return false;
        }
    }

    /**
     * Uninstalls Java classes ({@code Extension}s, {@code Plugin}s, {@code PassiveScanner}s) of the given {@code addOn}. Should
     * be called when the add-on must be temporarily uninstalled for an update of a dependency.
     * <p>
     * The Java classes are uninstalled in the following order (inverse to installation):
     * <ol>
     * <li>Passive scanners;</li>
     * <li>Active scanners;</li>
     * <li>Extensions.</li>
     * </ol>
     * 
     * @param addOn the add-on that will be softly uninstalled
     * @param callback the callback that will be notified of the progress of the uninstallation
     * @return {@code true} if the add-on was uninstalled without errors, {@code false} otherwise.
     * @since 2.4.0
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     */
    public static boolean softUninstall(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        Validate.notNull(addOn, "Parameter addOn must not be null.");
        validateCallbackNotNull(callback);

        try {
            boolean uninstalledWithoutErrors = true;
            uninstalledWithoutErrors &= uninstallAddOnPassiveScanRules(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnActiveScanRules(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnExtensions(addOn, callback);

            return uninstalledWithoutErrors;
        } catch (Throwable e) {
            logger.error("An error occurred while uninstalling the add-on: " + addOn.getId(), e);
            return false;
        }
    }

    private static List<Extension> installAddOnExtensions(AddOn addOn) {
        ExtensionLoader extensionLoader = Control.getSingleton().getExtensionLoader();
        List<Extension> listExts = ExtensionFactory.loadAddOnExtensions(extensionLoader, Model.getSingleton()
                .getOptionsParam()
                .getConfig(), addOn);

        for (Extension ext : listExts) {
            installAddOnExtensionImpl(addOn, ext, extensionLoader);
        }

        return listExts;
    }
    
    public static void installAddOnExtension(AddOn addOn, Extension ext) {
        ExtensionLoader extensionLoader = Control.getSingleton().getExtensionLoader();
        ExtensionFactory.addAddOnExtension(extensionLoader, Model.getSingleton()
                .getOptionsParam()
                .getConfig(), ext);

        installAddOnExtensionImpl(addOn, ext, extensionLoader);
    }
    
    private static void installAddOnExtensionImpl(AddOn addOn, Extension ext, ExtensionLoader extensionLoader) {
        if (ext.isEnabled()) {
            logger.debug("Starting extension " + ext.getName());
            try {
                extensionLoader.startLifeCycle(ext);
            } catch (Exception e) {
                logger.error("An error occurred while installing the add-on: " + addOn.getId(), e);
            }
        }
    }

    private static boolean uninstallAddOnExtensions(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;

        callback.extensionsWillBeRemoved(addOn.getLoadedExtensions().size());
        List<Extension> extensions = new ArrayList<>(addOn.getLoadedExtensions());
        Collections.reverse(extensions);
        for (Extension ext : extensions) {
            uninstalledWithoutErrors &= uninstallAddOnExtension(addOn, ext, callback);
        }
        return uninstalledWithoutErrors;
    }

    /**
     * Uninstalls the given extension.
     *
     * @param addOn the add-on that has the extension
     * @param extension the extension that should be uninstalled
     * @param callback the callback that will be notified of the progress of the uninstallation
     * @return {@code true} if the extension was uninstalled without errors, {@code false} otherwise.
     * @since 2.4.0
     * @see Extension
     */
    protected static boolean uninstallAddOnExtension(
            AddOn addOn,
            Extension extension,
            AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;
        if (extension.isEnabled()) {
            String extUiName = extension.getUIName();
            if (extension.canUnload()) {
                logger.debug("Unloading ext: " + extension.getName());
                try {
                    extension.unload();
                    ExtensionFactory.unloadAddOnExtension(extension);
                } catch (Exception e) {
                    logger.error("An error occurred while uninstalling the extension \"" + extension.getName()
                            + "\" bundled in the add-on \"" + addOn.getId() + "\":", e);
                    uninstalledWithoutErrors = false;
                }
            } else {
                logger.debug("Cant dynamically unload ext: " + extension.getName());
                uninstalledWithoutErrors = false;
            }
            callback.extensionRemoved(extUiName);
        }
        addOn.removeLoadedExtension(extension);

        return uninstalledWithoutErrors;
    }

    private static void installAddOnActiveScanRules(AddOn addOn, AddOnClassLoader addOnClassLoader) {
        List<AbstractPlugin> ascanrules = AddOnLoaderUtils.getActiveScanRules(addOn, addOnClassLoader);
        if (!ascanrules.isEmpty()) {
            for (AbstractPlugin ascanrule : ascanrules) {
                String name = ascanrule.getClass().getCanonicalName();
                logger.debug("Install ascanrule: " + name);
                PluginFactory.loadedPlugin(ascanrule);
                if (!PluginFactory.isPluginLoaded(ascanrule)) {
                    logger.error("Failed to install ascanrule: " + name);
                }
            }
        }
    }

    private static boolean uninstallAddOnActiveScanRules(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;

        List<AbstractPlugin> loadedAscanrules = addOn.getLoadedAscanrules();
        if (!loadedAscanrules.isEmpty()) {
            logger.debug("Uninstall ascanrules: " + addOn.getAscanrules());
            callback.activeScanRulesWillBeRemoved(loadedAscanrules.size());
            for (AbstractPlugin ascanrule : loadedAscanrules) {
                String name = ascanrule.getClass().getCanonicalName();
                logger.debug("Uninstall ascanrule: " + name);
                PluginFactory.unloadedPlugin(ascanrule);
                if (PluginFactory.isPluginLoaded(ascanrule)) {
                    logger.error("Failed to uninstall ascanrule: " + name);
                    uninstalledWithoutErrors = false;
                }
                callback.activeScanRuleRemoved(name);
            }
            addOn.setLoadedAscanrules(Collections.<AbstractPlugin>emptyList());
            addOn.setLoadedAscanrulesSet(false);
        }

        return uninstalledWithoutErrors;
    }

    private static void installAddOnPassiveScanRules(AddOn addOn, AddOnClassLoader addOnClassLoader) {
        List<PluginPassiveScanner> pscanrules = AddOnLoaderUtils.getPassiveScanRules(addOn, addOnClassLoader);
        ExtensionPassiveScan extPscan = (ExtensionPassiveScan) Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionPassiveScan.NAME);

        if (!pscanrules.isEmpty() && extPscan != null) {
            for (PluginPassiveScanner pscanrule : pscanrules) {
                String name = pscanrule.getClass().getCanonicalName();
                logger.debug("Install pscanrule: " + name);
                if (!extPscan.addPassiveScanner(pscanrule)) {
                    logger.error("Failed to install pscanrule: " + name);
                }
            }
        }
    }

    private static boolean uninstallAddOnPassiveScanRules(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;

        List<PluginPassiveScanner> loadedPscanrules = addOn.getLoadedPscanrules();
        ExtensionPassiveScan extPscan = (ExtensionPassiveScan) Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionPassiveScan.NAME);
        if (!loadedPscanrules.isEmpty()) {
            logger.debug("Uninstall pscanrules: " + addOn.getPscanrules());
            callback.passiveScanRulesWillBeRemoved(loadedPscanrules.size());
            for (PluginPassiveScanner pscanrule : loadedPscanrules) {
                String name = pscanrule.getClass().getCanonicalName();
                logger.debug("Uninstall pscanrule: " + name);
                if (!extPscan.removePassiveScanner(pscanrule)) {
                    logger.error("Failed to uninstall pscanrule: " + name);
                    uninstalledWithoutErrors = false;
                }
                callback.passiveScanRuleRemoved(name);
            }
            addOn.setLoadedPscanrules(Collections.<PluginPassiveScanner>emptyList());
            addOn.setLoadedPscanrulesSet(false);
        }

        return uninstalledWithoutErrors;
    }

    /**
     * Installs all the missing files declared by the given {@code addOn}.
     * 
     * @param addOnClassLoader the class loader of the given {@code addOn}
     * @param addOn the add-on that will have the missing declared files installed
     */
    public static void installMissingAddOnFiles(AddOnClassLoader addOnClassLoader, AddOn addOn) {
        installAddOnFiles(addOnClassLoader, addOn, false);
    }

    private static void installAddOnFiles(AddOnClassLoader addOnClassLoader, AddOn addOn, boolean overwrite) {
        List<String> fileNames = addOn.getFiles();

        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }
        for (String name : fileNames) {
            File outfile = new File(Constant.getZapHome(), name);

            if (!overwrite && outfile.exists()) {
                // logger.debug("Ignoring, file already exists.");
                continue;
            }
            if (!outfile.getParentFile().exists() && !outfile.getParentFile().mkdirs()) {
                logger.error("Failed to create directories for: " + outfile.getAbsolutePath());
                continue;
            }

            logger.debug("Installing file: " + name);
            URL fileURL = addOnClassLoader.findResource(name);
            if (fileURL == null) {
                logger.error("File not found on add-on package: " + name);
                continue;
            }
            try (InputStream in = fileURL.openStream(); OutputStream out = new FileOutputStream(outfile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                logger.error("Failed to install file " + outfile.getAbsolutePath(), e);
            }
        }
        Control.getSingleton().getExtensionLoader().addonFilesAdded();
    }

    private static void validateCallbackNotNull(AddOnUninstallationProgressCallback callback) {
        Validate.notNull(callback, "Parameter callback must not be null.");
    }

    /**
     * Uninstalls the files of the given add-on.
     *
     * @param addOn the add-on
     * @param callback the callback for notification of progress
     * @return {@code true} if not error occurred while remove the files, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     */
    public static boolean uninstallAddOnFiles(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        Validate.notNull(addOn, "Parameter addOn must not be null.");
        validateCallbackNotNull(callback);

        List<String> fileNames = addOn.getFiles();
        if (fileNames == null || fileNames.isEmpty()) {
            return true;
        }

        callback.filesWillBeRemoved(fileNames.size());
        boolean uninstalledWithoutErrors = true;
        for (String name : fileNames) {
            if (name == null) {
                continue;
            }
            logger.debug("Uninstall file: " + name);
            File file = new File(Constant.getZapHome(), name);
            try {
                File parent = file.getParentFile();
                if (!file.delete()) {
                    logger.error("Failed to delete: " + file.getAbsolutePath());
                    uninstalledWithoutErrors = false;
                }
                callback.fileRemoved();
                if (parent.isDirectory() && parent.list().length == 0) {
                    logger.debug("Deleting: " + parent.getAbsolutePath());
                    if (!parent.delete()) {
                        // Ignore - check for <= 2 as on *nix '.' and '..' are returned
                        logger.debug("Failed to delete: " + parent.getAbsolutePath());
                    }
                }
                deleteEmptyDirsCreatedForAddOnFiles(file);
            } catch (Exception e) {
                logger.error("Failed to uninstall file " + file.getAbsolutePath(), e);
            }
        }

        Control.getSingleton().getExtensionLoader().addonFilesRemoved();

        return uninstalledWithoutErrors;
    }

    private static void deleteEmptyDirsCreatedForAddOnFiles(File file) {
        if (file == null) {
            return;
        }
        File currentFile = file;
        // Delete any empty dirs up to the ZAP root dir
        while (currentFile != null && !currentFile.exists()) {
            currentFile = currentFile.getParentFile();
        }
        String root = new File(Constant.getZapHome()).getAbsolutePath();
        while (currentFile != null && currentFile.exists()) {
            if (currentFile.getAbsolutePath().startsWith(root) && currentFile.getAbsolutePath().length() > root.length()) {
                deleteEmptyDirs(currentFile);
                currentFile = currentFile.getParentFile();
            } else {
                // Gone above the ZAP home dir
                break;
            }
        }
    }

    private static void deleteEmptyDirs(File dir) {
        logger.debug("Deleting dir " + dir.getAbsolutePath());
        for (File d : dir.listFiles()) {
            if (d.isDirectory()) {
                deleteEmptyDirs(d);
            }
        }
        if (!dir.delete()) {
            logger.debug("Failed to delete: " + dir.getAbsolutePath());
        }
    }

}
