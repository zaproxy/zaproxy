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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.zaproxy.zap.utils.ZapResourceBundleControl;

/**
 * Helper class responsible to install and uninstall add-ons and all its (dynamically installable)
 * components ({@code Extension}s, {@code Plugin}s, {@code PassiveScanner}s and files).
 *
 * @see Extension
 * @see org.parosproxy.paros.core.scanner.Plugin
 * @see PassiveScanner
 * @since 2.3.0
 */
public final class AddOnInstaller {

    private static final Logger logger = LogManager.getLogger(AddOnInstaller.class);

    /** The base directory to where add-on data (e.g. libraries) is copied. */
    private static final String ADD_ON_DATA_DIR = "addOnData";

    /**
     * The name of the directory that contains the libraries of an add-on.
     *
     * @see #getAddOnLibsDir(AddOn)
     */
    private static final String ADD_ON_DATA_LIBS_DIR = "libs";

    private AddOnInstaller() {}

    /**
     * Installs all the (dynamically installable) components ({@code Extension}s, {@code Plugin}s,
     * {@code PassiveScanner}s and files) of the given {@code addOn}.
     *
     * <p>It's also responsible to notify the installed extensions when the installation has
     * finished by calling the method {@code Extension#postInstall()}.
     *
     * <p>The components are installed in the following order:
     *
     * <ol>
     *   <li>{@link java.util.ResourceBundle ResourceBundle};
     *   <li>Files;
     *   <li>Extensions;
     *   <li>Active scanners;
     *   <li>Passive scanners.
     * </ol>
     *
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
        installResourceBundle(addOnClassLoader, addOn);
        installAddOnFiles(addOnClassLoader, addOn, true);
        List<Extension> listExts = installAddOnExtensions(addOn);
        installAddOnActiveScanRules(addOn, addOnClassLoader);
        installAddOnPassiveScanRules(addOn, addOnClassLoader);

        // postInstall actions
        for (Extension ext : listExts) {
            if (!ext.isEnabled()) {
                continue;
            }

            try {
                ext.postInstall();
            } catch (Exception e) {
                logger.error(
                        "Post install method failed for add-on {} extension {}",
                        addOn.getId(),
                        ext.getName());
            }
        }
    }

    /**
     * Uninstalls all the (dynamically installable) components ({@code Extension}s, {@code Plugin}s,
     * {@code PassiveScanner}s and files) of the given {@code addOn}.
     *
     * <p>The components are uninstalled in the following order (inverse to installation):
     *
     * <ol>
     *   <li>Passive scanners;
     *   <li>Active scanners;
     *   <li>Extensions;
     *   <li>Files;
     *   <li>{@link java.util.ResourceBundle ResourceBundle};
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
     * @deprecated (2.8.0) Use {@link #uninstall(AddOn, AddOnUninstallationProgressCallback, Set)}
     *     instead.
     */
    @Deprecated
    public static boolean uninstall(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        return uninstall(addOn, callback, null);
    }

    /**
     * Uninstalls all the (dynamically installable) components ({@code Extension}s, {@code Plugin}s,
     * {@code PassiveScanner}s and files) of the given {@code addOn}.
     *
     * <p>The components are uninstalled in the following order (inverse to installation):
     *
     * <ol>
     *   <li>Passive scanners;
     *   <li>Active scanners;
     *   <li>Extensions;
     *   <li>Files (if not in use by other add-ons);
     *   <li>{@link java.util.ResourceBundle ResourceBundle};
     * </ol>
     *
     * @param addOn the add-on that will be uninstalled.
     * @param callback the callback that will be notified of the progress of the uninstallation.
     * @param installedAddOns the add-ons currently installed.
     * @return {@code true} if the add-on was uninstalled without errors, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     * @since 2.8.0
     * @see #softUninstall(AddOn, AddOnUninstallationProgressCallback)
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     * @deprecated (2.13.0) Use {@link #uninstall(AddOn, AddOnUninstallationProgressCallback, Set,
     *     PostponedTasksRunner)} instead.
     */
    @Deprecated
    public static boolean uninstall(
            AddOn addOn, AddOnUninstallationProgressCallback callback, Set<AddOn> installedAddOns) {
        return uninstall(addOn, callback, installedAddOns, null);
    }

    /**
     * Uninstalls all the (dynamically installable) components ({@code Extension}s, {@code Plugin}s,
     * {@code PassiveScanner}s and files) of the given {@code addOn}.
     *
     * <p>The components are uninstalled in the following order (inverse to installation):
     *
     * <ol>
     *   <li>Passive scanners;
     *   <li>Active scanners;
     *   <li>Extensions;
     *   <li>Files (if not in use by other add-ons);
     *   <li>{@link java.util.ResourceBundle ResourceBundle};
     * </ol>
     *
     * @param addOn the add-on that will be uninstalled.
     * @param callback the callback that will be notified of the progress of the uninstallation.
     * @param installedAddOns the add-ons currently installed.
     * @param postponedTasks the container for postponed tasks.
     * @return {@code true} if the add-on was uninstalled without errors, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     * @since 2.13.0
     * @see #softUninstall(AddOn, AddOnUninstallationProgressCallback)
     * @see Extension
     * @see PassiveScanner
     * @see org.parosproxy.paros.core.scanner.Plugin
     */
    public static boolean uninstall(
            AddOn addOn,
            AddOnUninstallationProgressCallback callback,
            Set<AddOn> installedAddOns,
            PostponedTasksRunner postponedTasks) {
        Validate.notNull(addOn, "Parameter addOn must not be null.");
        validateCallbackNotNull(callback);

        try {
            boolean uninstalledWithoutErrors = true;
            uninstalledWithoutErrors &= uninstallAddOnPassiveScanRules(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnActiveScanRules(addOn, callback);
            uninstalledWithoutErrors &= uninstallAddOnExtensions(addOn, callback);
            uninstalledWithoutErrors &=
                    uninstallAddOnFiles(addOn, callback, installedAddOns, postponedTasks);
            uninstallResourceBundle(addOn);

            return uninstalledWithoutErrors;
        } catch (Throwable e) {
            logger.error("An error occurred while uninstalling the add-on: {}", addOn.getId(), e);
            return false;
        }
    }

    /**
     * Uninstalls Java classes ({@code Extension}s, {@code Plugin}s, {@code PassiveScanner}s) of the
     * given {@code addOn}. Should be called when the add-on must be temporarily uninstalled for an
     * update of a dependency.
     *
     * <p>The Java classes are uninstalled in the following order (inverse to installation):
     *
     * <ol>
     *   <li>Passive scanners;
     *   <li>Active scanners;
     *   <li>Extensions.
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
            logger.error("An error occurred while uninstalling the add-on: {}", addOn.getId(), e);
            return false;
        }
    }

    /**
     * Installs (and sets into the add-on) the resource bundle declared by the given add-on, if any.
     *
     * @param addOnClassLoader the ClassLoader of the add-on.
     * @param addOn the add-on.
     * @since 2.8.0
     * @see AddOn#getBundleData()
     */
    static void installResourceBundle(AddOnClassLoader addOnClassLoader, AddOn addOn) {
        AddOn.BundleData bundleData = addOn.getBundleData();
        if (bundleData.isEmpty()) {
            return;
        }

        try {
            ResourceBundle resourceBundle =
                    ResourceBundle.getBundle(
                            bundleData.getBaseName(),
                            Constant.getLocale(),
                            addOnClassLoader,
                            new ZapResourceBundleControl());
            addOn.setResourceBundle(resourceBundle);
            String bundlePrefix = bundleData.getPrefix();
            if (!bundlePrefix.isEmpty()) {
                Constant.messages.addMessageBundle(bundlePrefix, resourceBundle);
            }
        } catch (MissingResourceException e) {
            logger.error("Declared bundle not found in {} add-on:", addOn.getId(), e);
        }
    }

    private static void uninstallResourceBundle(AddOn addOn) {
        String bundlePrefix = addOn.getBundleData().getPrefix();
        if (!bundlePrefix.isEmpty()) {
            Constant.messages.removeMessageBundle(bundlePrefix);
        }
    }

    private static List<Extension> installAddOnExtensions(AddOn addOn) {
        ExtensionLoader extensionLoader = Control.getSingleton().getExtensionLoader();
        List<Extension> listExts =
                ExtensionFactory.loadAddOnExtensions(
                        extensionLoader, Model.getSingleton().getOptionsParam().getConfig(), addOn);

        for (Extension ext : listExts) {
            installAddOnExtensionImpl(addOn, ext, extensionLoader);
        }

        return listExts;
    }

    public static void installAddOnExtension(AddOn addOn, Extension ext) {
        ExtensionLoader extensionLoader = Control.getSingleton().getExtensionLoader();
        ExtensionFactory.addAddOnExtension(
                extensionLoader, Model.getSingleton().getOptionsParam().getConfig(), ext);

        installAddOnExtensionImpl(addOn, ext, extensionLoader);
    }

    private static void installAddOnExtensionImpl(
            AddOn addOn, Extension ext, ExtensionLoader extensionLoader) {
        if (ext.isEnabled()) {
            logger.debug("Starting extension {}", ext.getName());
            try {
                extensionLoader.startLifeCycle(ext);
            } catch (Throwable e) {
                // Catch Throwable to (try) prevent extensions' issues from breaking the
                // installation process.
                logger.error("An error occurred while installing the add-on: {}", addOn.getId(), e);
            }
        }
    }

    private static boolean uninstallAddOnExtensions(
            AddOn addOn, AddOnUninstallationProgressCallback callback) {
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
     * @return {@code true} if the extension was uninstalled without errors, {@code false}
     *     otherwise.
     * @since 2.4.0
     * @see Extension
     */
    protected static boolean uninstallAddOnExtension(
            AddOn addOn, Extension extension, AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;
        if (extension.isEnabled()) {
            String extUiName = extension.getUIName();
            if (extension.canUnload()) {
                logger.debug("Unloading extension: {}", extension.getName());
                try {
                    extension.unload();
                    Control.getSingleton().getExtensionLoader().removeExtension(extension);
                    ExtensionFactory.unloadAddOnExtension(extension);
                } catch (Exception e) {
                    logger.error(
                            "An error occurred while uninstalling the extension \"{}\" bundled in the add-on \"{}\":",
                            extension.getName(),
                            addOn.getId(),
                            e);
                    uninstalledWithoutErrors = false;
                }
            } else {
                logger.debug("Can't dynamically unload extension: {}", extension.getName());
                uninstalledWithoutErrors = false;
            }
            callback.extensionRemoved(extUiName);
        } else {
            ExtensionFactory.unloadAddOnExtension(extension);
        }
        addOn.removeLoadedExtension(extension);

        return uninstalledWithoutErrors;
    }

    private static void installAddOnActiveScanRules(
            AddOn addOn, AddOnClassLoader addOnClassLoader) {
        List<AbstractPlugin> ascanrules =
                AddOnLoaderUtils.getActiveScanRules(addOn, addOnClassLoader);
        if (!ascanrules.isEmpty()) {
            for (AbstractPlugin ascanrule : ascanrules) {
                String name = ascanrule.getClass().getCanonicalName();
                logger.debug("Install ascanrule: {}", name);
                PluginFactory.loadedPlugin(ascanrule);
                if (!PluginFactory.isPluginLoaded(ascanrule)) {
                    logger.error("Failed to install ascanrule: {}", name);
                }
            }
        }
    }

    private static boolean uninstallAddOnActiveScanRules(
            AddOn addOn, AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;

        List<AbstractPlugin> loadedAscanrules = addOn.getLoadedAscanrules();
        if (!loadedAscanrules.isEmpty()) {
            logger.debug("Uninstall ascanrules: {}", addOn.getAscanrules());
            callback.activeScanRulesWillBeRemoved(loadedAscanrules.size());
            for (AbstractPlugin ascanrule : loadedAscanrules) {
                String name = ascanrule.getClass().getCanonicalName();
                logger.debug("Uninstall ascanrule: {}", name);
                PluginFactory.unloadedPlugin(ascanrule);
                if (PluginFactory.isPluginLoaded(ascanrule)) {
                    logger.error("Failed to uninstall ascanrule: {}", name);
                    uninstalledWithoutErrors = false;
                }
                callback.activeScanRuleRemoved(name);
            }
            addOn.setLoadedAscanrules(Collections.<AbstractPlugin>emptyList());
            addOn.setLoadedAscanrulesSet(false);
        }

        return uninstalledWithoutErrors;
    }

    private static void installAddOnPassiveScanRules(
            AddOn addOn, AddOnClassLoader addOnClassLoader) {
        List<PluginPassiveScanner> pscanrules =
                AddOnLoaderUtils.getPassiveScanRules(addOn, addOnClassLoader);
        ExtensionPassiveScan extPscan =
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionPassiveScan.class);

        if (!pscanrules.isEmpty() && extPscan != null) {
            for (PluginPassiveScanner pscanrule : pscanrules) {
                String name = pscanrule.getClass().getCanonicalName();
                logger.debug("Install pscanrule: {}", name);
                if (!extPscan.addPassiveScanner(pscanrule)) {
                    logger.error("Failed to install pscanrule: {}", name);
                }
            }
        }
    }

    private static boolean uninstallAddOnPassiveScanRules(
            AddOn addOn, AddOnUninstallationProgressCallback callback) {
        boolean uninstalledWithoutErrors = true;

        List<PluginPassiveScanner> loadedPscanrules = addOn.getLoadedPscanrules();
        ExtensionPassiveScan extPscan =
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionPassiveScan.class);
        if (!loadedPscanrules.isEmpty()) {
            logger.debug("Uninstall pscanrules: {}", addOn.getPscanrules());
            callback.passiveScanRulesWillBeRemoved(loadedPscanrules.size());
            for (PluginPassiveScanner pscanrule : loadedPscanrules) {
                String name = pscanrule.getClass().getCanonicalName();
                logger.debug("Uninstall pscanrule: {}", name);
                if (!extPscan.removePassiveScanner(pscanrule)) {
                    logger.error("Failed to uninstall pscanrule: {}", name);
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
     * Installs the libraries declared by the given add-on.
     *
     * <p>The libraries are copied to the directory with the following path: {@code
     * <zapHome>/addOnData/<addOnId>/<version>/libs/}
     *
     * @param addOn the add-on that will have the declared libraries installed.
     * @return {@code true} if no error occurred while installing the libraries, {@code false}
     *     otherwise.
     * @see #uninstallAddOnLibs(AddOn)
     * @see #installMissingAddOnLibs(AddOn)
     * @see #getAddOnDataDir(AddOn)
     */
    static boolean installAddOnLibs(AddOn addOn) {
        return installAddOnLibs(addOn, true);
    }

    private static boolean installAddOnLibs(AddOn addOn, boolean overwrite) {
        List<AddOn.Lib> libs = addOn.getLibs();
        if (libs.isEmpty()) {
            return true;
        }

        Path targetDir = getAddOnLibsDir(addOn);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            logger.warn("Failed to create libs directory for {}", addOn.getId(), e);
            return false;
        }

        try (ZipFile zip = new ZipFile(addOn.getFile())) {
            for (AddOn.Lib lib : libs) {
                String name = lib.getName();
                logger.debug("Installing library for {}: {}", addOn, name);

                Path targetFile = targetDir.resolve(name);
                try {
                    lib.setFileSystemUrl(targetFile.toUri().toURL());
                } catch (MalformedURLException e) {
                    logger.warn(
                            "Failed to convert lib's filesystem path to URL for {}",
                            addOn.getId(),
                            e);
                    return false;
                }

                if (!overwrite && Files.exists(targetFile)) {
                    continue;
                }

                ZipEntry libZipEntry = zip.getEntry(lib.getJarPath());
                if (libZipEntry == null) {
                    logger.warn("Library not found in {} add-on: {}", addOn, lib);
                    return false;
                }

                try (InputStream in = zip.getInputStream(libZipEntry)) {
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    logger.warn("Failed to copy the library for {}: {}", addOn, targetFile, e);
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("An error occurred while installing libraries for {}", addOn, e);
            return false;
        }

        return true;
    }

    /**
     * Gets the path to the data directory of the given add-on.
     *
     * <p>The path is built as: {@code <zapHome>/addOnData/<addOnId>/<version>/}
     *
     * @param addOn the add-on.
     * @return the path to the directory.
     * @see #ADD_ON_DATA_DIR
     */
    static Path getAddOnDataDir(AddOn addOn) {
        return Paths.get(
                Constant.getZapHome(),
                ADD_ON_DATA_DIR,
                addOn.getId(),
                addOn.getVersion().toString());
    }

    static void deleteLegacyAddOnLibsDir(List<AddOn> addOns) {
        for (AddOn addOn : addOns) {
            Path libsDir =
                    Paths.get(
                            Constant.getZapHome(),
                            ADD_ON_DATA_DIR,
                            addOn.getId(),
                            ADD_ON_DATA_LIBS_DIR);
            try {
                deleteDir(libsDir);
            } catch (IOException e) {
                logger.warn(
                        "An error occurred while removing legacy add-on libs directory {}",
                        libsDir,
                        e);
            }
        }
    }

    /**
     * Gets the path to the libraries directory of the given add-on.
     *
     * <p>The path is built as: {@code <zapHome>/addOnData/<addOnId>/<version>/libs/}
     *
     * @param addOn the add-on.
     * @return the path to the directory.
     * @see #ADD_ON_DATA_LIBS_DIR
     */
    static Path getAddOnLibsDir(AddOn addOn) {
        return getAddOnDataDir(addOn).resolve(ADD_ON_DATA_LIBS_DIR);
    }

    /**
     * Installs all the missing libraries declared by the given add-on.
     *
     * @param addOn the add-on that will have the missing declared libraries installed.
     * @return {@code true} if no error occurred while installing the libraries, {@code false}
     *     otherwise.
     * @see #uninstallAddOnLibs(AddOn)
     * @see #installAddOnLibs(AddOn, boolean)
     */
    static boolean installMissingAddOnLibs(AddOn addOn) {
        return installAddOnLibs(addOn, false);
    }

    /**
     * Uninstalls the libraries declared by the given add-on.
     *
     * @param addOn the add-on that will have the declared libraries uninstalled.
     * @see #installAddOnLibs(AddOn)
     * @see #installMissingAddOnLibs(AddOn)
     * @return {@code true} if the libraries were uninstalled without errors, {@code false}
     *     otherwise.
     */
    static boolean uninstallAddOnLibs(AddOn addOn) {
        List<AddOn.Lib> libs = addOn.getLibs();
        if (libs.isEmpty()) {
            return true;
        }

        Path addOnLibsDir = getAddOnLibsDir(addOn);
        try {
            deleteDir(addOnLibsDir);
        } catch (IOException e) {
            logger.error("An error occurred while uninstalling libraries for {}", addOn, e);
            return false;
        }

        Path addOnDataDir = addOnLibsDir.getParent();
        try (Stream<Path> entries = Files.list(addOnDataDir)) {
            if (!entries.findAny().isPresent()) {
                Files.delete(addOnDataDir);
            }
        } catch (IOException e) {
            logger.warn("An error occurred while removing the directory {}", addOnDataDir, e);
            return false;
        }

        return true;
    }

    /**
     * Deletes the given directory and all files/directories under it.
     *
     * @param dir the directory to delete.
     * @throws IOException if an error occurred while deleting the directory or any file/directory
     *     under it.
     */
    private static void deleteDir(Path dir) throws IOException {
        if (Files.notExists(dir)) {
            return;
        }

        Files.walkFileTree(
                dir,
                new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e)
                            throws IOException {
                        if (e != null) {
                            throw e;
                        }
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
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

    /**
     * Updates the files declared by the given {@code addOn}.
     *
     * @param addOnClassLoader the class loader of the given {@code addOn}.
     * @param addOn the add-on that will have the declared files updated.
     * @since 2.7.0
     */
    public static void updateAddOnFiles(AddOnClassLoader addOnClassLoader, AddOn addOn) {
        installAddOnFiles(addOnClassLoader, addOn, true);
    }

    private static void installAddOnFiles(
            AddOnClassLoader addOnClassLoader, AddOn addOn, boolean overwrite) {
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
                logger.error(
                        "Failed to create directories for add-on {}: {}",
                        addOn,
                        outfile.getAbsolutePath());
                continue;
            }

            logger.debug("Installing file: {}", name);
            URL fileURL = addOnClassLoader.findResource(name);
            if (fileURL == null) {
                logger.error("File not found in add-on {}: {}", addOn, name);
                continue;
            }
            try (InputStream in = fileURL.openStream();
                    OutputStream out = new FileOutputStream(outfile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                logger.error(
                        "Failed to install a file from add-on {}: {}",
                        addOn,
                        outfile.getAbsolutePath(),
                        e);
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
     * @return {@code true} if no error occurred while removing the files, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     * @deprecated (2.8.0) Use {@link #uninstallAddOnFiles(AddOn,
     *     AddOnUninstallationProgressCallback, Set)} instead.
     */
    @Deprecated
    public static boolean uninstallAddOnFiles(
            AddOn addOn, AddOnUninstallationProgressCallback callback) {
        return uninstallAddOnFiles(addOn, callback, null);
    }

    /**
     * Uninstalls the files of the given add-on.
     *
     * <p><strong>Note:</strong> Files that are in use by other installed add-ons are not
     * uninstalled.
     *
     * @param addOn the add-on whose files should be uninstalled.
     * @param callback the callback for notification of progress.
     * @param installedAddOns the add-ons currently installed (to check if the files can be safely
     *     uninstalled).
     * @return {@code true} if no error occurred while removing the files, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     * @since 2.8.0
     * @deprecated (2.13.0) Use {@link #uninstallAddOnFiles(AddOn,
     *     AddOnUninstallationProgressCallback, Set, PostponedTasksRunner)} instead.
     */
    @Deprecated
    public static boolean uninstallAddOnFiles(
            AddOn addOn, AddOnUninstallationProgressCallback callback, Set<AddOn> installedAddOns) {
        return uninstallAddOnFiles(addOn, callback, installedAddOns, null);
    }

    /**
     * Uninstalls the files of the given add-on.
     *
     * <p><strong>Note:</strong> Files that are in use by other installed add-ons are not
     * uninstalled.
     *
     * @param addOn the add-on whose files should be uninstalled.
     * @param callback the callback for notification of progress.
     * @param installedAddOns the add-ons currently installed (to check if the files can be safely
     *     uninstalled).
     * @param postponedTasks the container for postponed tasks.
     * @return {@code true} if no error occurred while removing the files, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code addOn} or {@code callback} are null.
     * @since 2.13.0
     */
    public static boolean uninstallAddOnFiles(
            AddOn addOn,
            AddOnUninstallationProgressCallback callback,
            Set<AddOn> installedAddOns,
            PostponedTasksRunner postponedTasks) {
        Validate.notNull(addOn, "Parameter addOn must not be null.");
        validateCallbackNotNull(callback);

        List<String> fileNames =
                getFilesSafeForUninstall(
                        addOn, installedAddOns == null ? Collections.emptySet() : installedAddOns);
        if (fileNames.isEmpty()) {
            return true;
        }

        callback.filesWillBeRemoved(fileNames.size());
        boolean uninstalledWithoutErrors = true;
        for (String name : fileNames) {
            if (name == null) {
                continue;
            }
            logger.debug("Uninstall file: {}", name);
            File file = new File(Constant.getZapHome(), name);
            try {
                File parent = file.getParentFile();
                if (file.exists() && !file.delete()) {
                    logger.error("Failed to delete: {}", file.getAbsolutePath());
                    uninstalledWithoutErrors = false;
                    if (postponedTasks != null) {
                        postponedTasks.addDeleteFileTask(file.toPath());
                    }
                }
                callback.fileRemoved();
                if (parent.isDirectory() && parent.list().length == 0) {
                    logger.debug("Deleting: {}", parent.getAbsolutePath());
                    if (!parent.delete()) {
                        // Ignore - check for <= 2 as on *nix '.' and '..' are returned
                        logger.debug("Failed to delete: {}", parent.getAbsolutePath());
                    }
                }
                deleteEmptyDirsCreatedForAddOnFiles(file);
            } catch (Exception e) {
                logger.error("Failed to uninstall file {}", file.getAbsolutePath(), e);
            }
        }

        Control.getSingleton().getExtensionLoader().addonFilesRemoved();

        return uninstalledWithoutErrors;
    }

    /**
     * Gets the files of the given add-on that can be safely uninstalled, that is, are not in
     * use/declared by other add-ons.
     *
     * @param addOn the add-on whose files should be uninstalled.
     * @param installedAddOns the add-ons currently installed.
     * @return the files that can be safely uninstalled.
     */
    private static List<String> getFilesSafeForUninstall(AddOn addOn, Set<AddOn> installedAddOns) {
        if (addOn.getFiles() == null || addOn.getFiles().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> files = new ArrayList<>(addOn.getFiles());
        installedAddOns.forEach(
                installedAddOn -> {
                    if (installedAddOn == addOn) {
                        return;
                    }

                    List<String> addOnFiles = installedAddOn.getFiles();
                    if (addOnFiles == null || addOnFiles.isEmpty()) {
                        return;
                    }

                    files.removeAll(addOnFiles);
                });
        return files;
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
            if (currentFile.getAbsolutePath().startsWith(root)
                    && currentFile.getAbsolutePath().length() > root.length()) {
                deleteEmptyDirs(currentFile);
                currentFile = currentFile.getParentFile();
            } else {
                // Gone above the ZAP home dir
                break;
            }
        }
    }

    private static void deleteEmptyDirs(File dir) {
        logger.debug("Deleting dir {}", dir.getAbsolutePath());
        for (File d : dir.listFiles()) {
            if (d.isDirectory()) {
                deleteEmptyDirs(d);
            }
        }
        if (!dir.delete()) {
            logger.debug("Failed to delete: {}", dir.getAbsolutePath());
        }
    }
}
