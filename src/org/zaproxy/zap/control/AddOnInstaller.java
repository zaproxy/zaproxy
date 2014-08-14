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
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.extension.pscan.PassiveScanner;

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
     * Installs all the (dynamically installable) components of the given {@code addOn}. {@code Extension}s, {@code Plugin}s,
     * {@code PassiveScanner}s and files.
     * <p>
     * It's also responsible to notify the installed extensions when the installation has finished by calling the method
     * {@code Extension#postInstall()}.
     * 
     * @param addOnClassLoader the class loader of the given {@code addOn}
     * @param addOn the add-on that will be installed
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     * @see Extension#postInstall()
     */
    public static void install(AddOnClassLoader addOnClassLoader, AddOn addOn) {
        List<Extension> listExts = installAddOnExtensions(addOn);
        installAddOnActiveScanRules(addOn);
        installAddOnPassiveScanRules(addOn);
        installAddOnFiles(addOnClassLoader, addOn, true);

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
     * Uninstalls all the (dynamically installable) components of the given {@code addOn}. {@code Extension}s, {@code Plugin}s,
     * {@code PassiveScanner}s and files.
     * 
     * @param addOn the add-on that will be uninstalled
     * @return {@code true} if the add-on was uninstalled without errors, {@code false} otherwise.
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     */
    public static boolean uninstall(AddOn addOn) {
        try {
            boolean uninstalledWithoutErrors = true;
            uninstalledWithoutErrors &= uninstallAddOnActiveScanRules(addOn);
            uninstalledWithoutErrors &= uninstallAddOnPassiveScanRules(addOn);
            uninstalledWithoutErrors &= uninstallAddOnFiles(addOn);
            // This will remove the message bundle, so do it last in case the rules use it
            uninstalledWithoutErrors &= uninstallAddOnExtensions(addOn);
    
            return uninstalledWithoutErrors;
        } catch (Exception e) {
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
            if (ext.isEnabled()) {
                logger.debug("Starting extension " + ext.getName());
                extensionLoader.startLifeCycle(ext);
            }
        }

        return listExts;
    }

    private static boolean uninstallAddOnExtensions(AddOn addOn) {
        boolean uninstalledWithoutErrors = true;

        List<String> extNames = addOn.getExtensions();
        if (extNames != null) {
            ExtensionLoader extensionLoader = Control.getSingleton().getExtensionLoader();
            for (String name : extNames) {
                Extension ext = extensionLoader.getExtensionByClassName(name);
                if (ext != null && ext.isEnabled()) {
                    if (ext.canUnload()) {
                        logger.debug("Unloading ext: " + ext.getName());
                        try {
                            ext.unload();
                            ExtensionFactory.unloadAddOnExtension(ext);
                        } catch (Exception e) {
                            logger.error("An error occurred while uninstalling the extension \"" + name
                                    + "\" bundled in the add-on \"" + addOn.getId() + "\":", e);
                            uninstalledWithoutErrors = false;
                        }
                    } else {
                        logger.debug("Cant dynamically unload ext: " + name);
                        uninstalledWithoutErrors = false;
                    }
                }
            }
        }

        return uninstalledWithoutErrors;
    }

    private static void installAddOnActiveScanRules(AddOn addOn) {
        List<String> ascanNames = addOn.getAscanrules();
        if (ascanNames != null) {
            PluginFactory pluginFactory = Control.getSingleton().getPluginFactory();
            for (String name : ascanNames) {
                logger.debug("Install ascanrule: " + name);
                if (!pluginFactory.addPlugin(name)) {
                    logger.error("Failed to install ascanrule: " + name);
                }
            }
        }
    }

    private static boolean uninstallAddOnActiveScanRules(AddOn addOn) {
        boolean uninstalledWithoutErrors = true;

        List<String> ascanNames = addOn.getAscanrules();
        logger.debug("Uninstall ascanrules: " + ascanNames);
        if (ascanNames != null) {
            PluginFactory pluginFactory = Control.getSingleton().getPluginFactory();
            for (String name : ascanNames) {
                logger.debug("Uninstall ascanrule: " + name);
                if (!pluginFactory.removePlugin(name)) {
                    logger.error("Failed to uninstall ascanrule: " + name);
                    uninstalledWithoutErrors = false;
                }
            }
        }

        return uninstalledWithoutErrors;
    }

    private static void installAddOnPassiveScanRules(AddOn addOn) {
        List<String> pscanNames = addOn.getPscanrules();
        ExtensionPassiveScan extPscan = (ExtensionPassiveScan) Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionPassiveScan.NAME);

        if (pscanNames != null && extPscan != null) {
            for (String name : pscanNames) {
                logger.debug("Install pscanrule: " + name);
                if (!extPscan.addPassiveScanner(name)) {
                    logger.error("Failed to install pscanrule: " + name);
                }
            }
        }
    }

    private static boolean uninstallAddOnPassiveScanRules(AddOn addOn) {
        boolean uninstalledWithoutErrors = true;

        List<String> pscanNames = addOn.getPscanrules();
        ExtensionPassiveScan extPscan = (ExtensionPassiveScan) Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionPassiveScan.NAME);
        logger.debug("Uninstall pscanrules: " + pscanNames);
        if (pscanNames != null && extPscan != null) {
            for (String name : pscanNames) {
                logger.debug("Uninstall pscanrule: " + name);
                if (!extPscan.removePassiveScanner(name)) {
                    logger.error("Failed to uninstall pscanrule: " + name);
                    uninstalledWithoutErrors = false;
                }
            }
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
            logger.debug("Install file: " + name);

            if (!overwrite && outfile.exists()) {
                logger.debug("Ignoring, file already exists.");
                continue;
            }
            if (!outfile.getParentFile().exists() && !outfile.getParentFile().mkdirs()) {
                logger.error("Failed to create directories for: " + outfile.getAbsolutePath());
                continue;
            }

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

    private static boolean uninstallAddOnFiles(AddOn addOn) {
        List<String> fileNames = addOn.getFiles();
        if (fileNames == null || fileNames.isEmpty()) {
            return true;
        }

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
