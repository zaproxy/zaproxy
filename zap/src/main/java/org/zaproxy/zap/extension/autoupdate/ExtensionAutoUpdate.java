/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.autoupdate;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.FileCopier;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.ZAP.ProcessType;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOn.AddOnRunRequirements;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.control.AddOnCollection.Platform;
import org.zaproxy.zap.control.AddOnRunIssuesUtils;
import org.zaproxy.zap.control.AddOnUninstallationProgressCallback;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.control.ZapRelease;
import org.zaproxy.zap.extension.autoupdate.AddOnDependencyChecker.AddOnChangesResult;
import org.zaproxy.zap.extension.autoupdate.AddOnDependencyChecker.UninstallationResult;
import org.zaproxy.zap.extension.autoupdate.UninstallationProgressDialogue.AddOnUninstallListener;
import org.zaproxy.zap.extension.autoupdate.UninstallationProgressDialogue.UninstallationProgressEvent;
import org.zaproxy.zap.extension.autoupdate.UninstallationProgressDialogue.UninstallationProgressHandler;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionAutoUpdate extends ExtensionAdaptor
        implements CheckForUpdateCallback, CommandLineListener {

    private static final String NAME = "ExtensionAutoUpdate";

    public static final String ADDON_INSTALL = "-addoninstall";
    public static final String ADDON_INSTALL_ALL = "-addoninstallall";

    private static final String VERSION_FILE_NAME = "ZapVersions.xml";

    private ZapMenuItem menuItemCheckUpdate = null;
    private ZapMenuItem menuItemLoadAddOn = null;

    private static final Logger logger = LogManager.getLogger(ExtensionAutoUpdate.class);

    private DownloadManager downloadManager = null;
    private ManageAddOnsDialog addonsDialog = null;
    private Thread downloadProgressThread = null;
    private Thread remoteCallThread = null;
    private ScanStatus scanStatus = null;
    private JButton addonsButton = null;
    private JButton checkForUpdatesButton = null;
    private JButton outOfDateButton = null;

    private AddOnCollection latestVersionInfo = null;
    private AddOnCollection localVersionInfo = null;
    private AddOnCollection previousVersionInfo = null;

    private AutoUpdateAPI api = null;

    private boolean oldZapAlertAdded = false;
    private boolean noCfuAlertAdded = false;
    private boolean installsOk = true;
    private boolean installsCompleted = true;

    // Files currently being downloaded
    private List<Downloader> downloadFiles = new ArrayList<>();

    private static final int ARG_CFU_INSTALL_IDX = 0;
    private static final int ARG_CFU_INSTALL_ALL_IDX = 1;
    private static final int ARG_CFU_UNINSTALL_IDX = 2;
    private static final int ARG_CFU_UPDATE_IDX = 3;
    private static final int ARG_CFU_LIST_IDX = 4;
    private static final int[] ARG_IDXS = {
        ARG_CFU_INSTALL_IDX,
        ARG_CFU_INSTALL_ALL_IDX,
        ARG_CFU_UNINSTALL_IDX,
        ARG_CFU_UPDATE_IDX,
        ARG_CFU_LIST_IDX
    };
    private CommandLineArgument[] arguments = new CommandLineArgument[ARG_IDXS.length];
    private Supplier<ZapXmlConfiguration> checkForUpdatesSupplier;

    public ExtensionAutoUpdate() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setName(NAME);
        this.setOrder(1); // High order so that cmdline updates are installed asap
        this.downloadManager = new DownloadManager(HttpSender.CHECK_FOR_UPDATES_INITIATOR);
        this.downloadManager.start();
        // Do this before it can get overwritten by the latest one
        this.getPreviousVersionInfo();
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("autoupdate.name");
    }

    @Override
    public void postInit() {
        switch (ZAP.getProcessType()) {
            case cmdline:
            case daemon:
            case zaas:
                this.warnIfOutOfDate();
                break;
            case desktop:
            default:
                break;
        }
    }

    /**
     * This method initializes menuItemEncoder
     *
     * @return javax.swing.JMenuItem
     */
    private ZapMenuItem getMenuItemCheckUpdate() {
        if (menuItemCheckUpdate == null) {
            menuItemCheckUpdate =
                    new ZapMenuItem(
                            "cfu.help.menu.check",
                            getView().getMenuShortcutKeyStroke(KeyEvent.VK_U, 0, false));
            menuItemCheckUpdate.setText(Constant.messages.getString("cfu.help.menu.check"));
            menuItemCheckUpdate.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            checkForUpdates(false);
                        }
                    });
        }
        return menuItemCheckUpdate;
    }

    private ZapMenuItem getMenuItemLoadAddOn() {
        if (menuItemLoadAddOn == null) {
            menuItemLoadAddOn =
                    new ZapMenuItem(
                            "cfu.file.menu.loadaddon",
                            getView().getMenuShortcutKeyStroke(KeyEvent.VK_L, 0, false));
            menuItemLoadAddOn.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            try {
                                JFileChooser chooser =
                                        new JFileChooser(
                                                Model.getSingleton()
                                                        .getOptionsParam()
                                                        .getUserDirectory());
                                File file = null;
                                chooser.setFileFilter(
                                        new FileFilter() {
                                            @Override
                                            public boolean accept(File file) {
                                                return file.isDirectory()
                                                        || (file.isFile()
                                                                && AddOn.isAddOnFileName(
                                                                        file.getName()));
                                            }

                                            @Override
                                            public String getDescription() {
                                                return Constant.messages.getString(
                                                        "file.format.zap.addon");
                                            }
                                        });
                                int rc = chooser.showOpenDialog(getView().getMainFrame());
                                if (rc == JFileChooser.APPROVE_OPTION) {
                                    file = chooser.getSelectedFile();
                                    if (file == null) {
                                        return;
                                    }
                                    installLocalAddOn(file.toPath());
                                }
                            } catch (Exception e1) {
                                logger.error(e1.getMessage(), e1);
                            }
                        }
                    });
        }
        return menuItemLoadAddOn;
    }

    boolean installLocalAddOnQuietly(Path file) {
        AddOn ao;
        try {
            ao = new AddOn(file);
        } catch (IOException e) {
            logger.warn("Failed to create the add-on: {}", e.getMessage(), e);
            return false;
        }

        if (!ao.canLoadInCurrentVersion()) {
            logger.warn("Can not install the add-on, incompatible ZAP version.");
            return false;
        }

        AddOn installedAddOn = this.getLocalVersionInfo().getAddOn(ao.getId());
        if ("network".equals(ao.getId())) {
            new Thread(() -> installLocalAddOnQuietly(installedAddOn, ao), "ZAP-AddOnAsyncInstall")
                    .start();
            return true;
        }
        return installLocalAddOnQuietly(installedAddOn, ao);
    }

    private boolean installLocalAddOnQuietly(AddOn installedAddOn, AddOn ao) {
        if (installedAddOn != null) {
            try {
                if (Files.isSameFile(installedAddOn.getFile().toPath(), ao.getFile().toPath())) {
                    logger.warn("Can not install the add-on, same file already installed.");
                    return false;
                }
            } catch (IOException e) {
                logger.warn(
                        "An error occurred while checking the add-ons' files: {}",
                        e.getMessage(),
                        e);
                return false;
            }

            if (!uninstallAddOn(null, installedAddOn, true)) {
                return false;
            }
        }

        File addOnFile;
        try {
            addOnFile = copyAddOnFileToLocalPluginFolder(ao);
        } catch (FileAlreadyExistsException e) {
            logger.warn("Unable to copy add-on, a file with the same name already exists.", e);
            return false;
        } catch (IOException e) {
            logger.warn("Unable to copy add-on to local plugin folder.", e);
            return false;
        }

        ao.setFile(addOnFile);

        return install(ao);
    }

    private void installLocalAddOn(Path file) throws Exception {
        AddOn ao;
        try {
            ao = new AddOn(file);
        } catch (AddOn.InvalidAddOnException e) {
            AddOn.ValidationResult result = e.getValidationResult();
            switch (result.getValidity()) {
                case INVALID_PATH:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString("cfu.warn.invalidAddOn.invalidPath"));
                    break;
                case INVALID_FILE_NAME:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString("cfu.warn.invalidAddOn.noZapExtension"));
                    break;
                case FILE_NOT_READABLE:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString("cfu.warn.invalidAddOn.notReadable"));
                    break;
                case UNREADABLE_ZIP_FILE:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString(
                                    "cfu.warn.invalidAddOn.errorZip", e.getMessage()));
                    break;
                case IO_ERROR_FILE:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString(
                                    "cfu.warn.invalidAddOn.ioError", e.getMessage()));
                    break;
                case MISSING_MANIFEST:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString("cfu.warn.invalidAddOn.missingManifest"));
                    break;
                case INVALID_MANIFEST:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString(
                                    "cfu.warn.invalidAddOn.invalidManifest", e.getMessage()));
                    break;
                case INVALID_LIB:
                    showWarningMessageInvalidAddOnFile(
                            Constant.messages.getString("cfu.warn.invalidAddOn.invalidLib"));
                    break;
                default:
                    showWarningMessageInvalidAddOnFile(e.getMessage());
                    logger.warn(e);
                    break;
            }
            return;
        }

        if (!ao.canLoadInCurrentVersion()) {
            showWarningMessageCantLoadAddOn(ao);
            return;
        }

        AddOnDependencyChecker dependencyChecker =
                new AddOnDependencyChecker(
                        getLocalVersionInfo(),
                        latestVersionInfo == null ? getLocalVersionInfo() : latestVersionInfo);

        boolean update = false;
        boolean uninstallBeforeAddOnCopy = false;
        AddOnChangesResult result;
        AddOn installedAddOn = getLocalVersionInfo().getAddOn(ao.getId());
        if (installedAddOn != null) {
            if (ao.getVersion().equals(installedAddOn.getVersion())) {
                int reinstall =
                        View.getSingleton()
                                .showYesNoDialog(
                                        getView().getMainFrame(),
                                        new Object[] {
                                            Constant.messages.getString(
                                                    "cfu.warn.addOnSameVersion",
                                                    installedAddOn.getVersion(),
                                                    View.getSingleton()
                                                            .getStatusUI(installedAddOn.getStatus())
                                                            .toString(),
                                                    ao.getVersion(),
                                                    View.getSingleton()
                                                            .getStatusUI(ao.getStatus())
                                                            .toString())
                                        });
                if (reinstall != JOptionPane.YES_OPTION) {
                    return;
                }
                uninstallBeforeAddOnCopy = true;
            } else if (!ao.isUpdateTo(installedAddOn)) {
                getView()
                        .showWarningDialog(
                                Constant.messages.getString(
                                        "cfu.warn.addOnOlderVersion",
                                        installedAddOn.getVersion(),
                                        View.getSingleton()
                                                .getStatusUI(installedAddOn.getStatus())
                                                .toString(),
                                        ao.getVersion(),
                                        View.getSingleton()
                                                .getStatusUI(ao.getStatus())
                                                .toString()));
                return;
            }

            result = dependencyChecker.calculateUpdateChanges(ao);
            update = true;
        } else {
            result = dependencyChecker.calculateInstallChanges(ao);
        }

        if (result.getOldVersions().isEmpty() && result.getUninstalls().isEmpty()) {
            AddOnRunRequirements reqs =
                    ao.calculateInstallRequirements(getLocalVersionInfo().getAddOns());
            if (!reqs.isRunnable()) {
                if (!AddOnRunIssuesUtils.askConfirmationAddOnNotRunnable(
                        Constant.messages.getString("cfu.warn.addOnNotRunnable.message"),
                        Constant.messages.getString("cfu.warn.addOnNotRunnable.question"),
                        getLocalVersionInfo(),
                        ao)) {
                    return;
                }
            }

            installLocalAddOn(ao, uninstallBeforeAddOnCopy);
            return;
        }

        if (update) {
            if (!dependencyChecker.confirmUpdateChanges(getView().getMainFrame(), result)) {
                return;
            }
            // The new version of the add-on is installed manually
            result.getNewVersions().remove(ao);
        } else {
            if (!dependencyChecker.confirmInstallChanges(getView().getMainFrame(), result)) {
                return;
            }
            // The add-on is installed manually
            result.getInstalls().remove(ao);
        }

        processAddOnChanges(getView().getMainFrame(), result);
        installLocalAddOn(ao, uninstallBeforeAddOnCopy);
    }

    private void installLocalAddOn(AddOn ao, boolean uninstallBeforeAddOnCopy) {
        if (uninstallBeforeAddOnCopy
                && !uninstallAddOn(null, getLocalVersionInfo().getAddOn(ao.getId()), true)) {
            return;
        }

        File addOnFile;
        try {
            addOnFile = copyAddOnFileToLocalPluginFolder(ao);
        } catch (FileAlreadyExistsException e) {
            showWarningMessageAddOnFileAlreadyExists(e.getFile(), e.getOtherFile());
            logger.warn("Unable to copy add-on, a file with the same name already exists.", e);
            return;
        } catch (IOException e) {
            showWarningMessageUnableToCopyAddOnFile();
            logger.warn("Unable to copy add-on to local plugin folder.", e);
            return;
        }

        ao.setFile(addOnFile);

        install(ao);
    }

    private void showWarningMessageInvalidAddOnFile(String reason) {
        getView().showWarningDialog(Constant.messages.getString("cfu.warn.invalidAddOn", reason));
    }

    private void showWarningMessageCantLoadAddOn(AddOn ao) {
        String message =
                Constant.messages.getString(
                        "cfu.warn.cantload", ao.getNotBeforeVersion(), ao.getNotFromVersion());
        getView().showWarningDialog(message);
    }

    private static File copyAddOnFileToLocalPluginFolder(AddOn addOn) throws IOException {
        if (isFileInLocalPluginFolder(addOn.getFile())) {
            return addOn.getFile();
        }

        File targetFile = new File(Constant.FOLDER_LOCAL_PLUGIN, addOn.getNormalisedFileName());
        if (targetFile.exists()) {
            throw new FileAlreadyExistsException(
                    addOn.getFile().getAbsolutePath(), targetFile.getAbsolutePath(), "");
        }

        FileCopier fileCopier = new FileCopier();
        fileCopier.copy(addOn.getFile(), targetFile);

        return targetFile;
    }

    private static boolean isFileInLocalPluginFolder(File file) {
        File fileLocalPluginFolder = new File(Constant.FOLDER_LOCAL_PLUGIN, file.getName());
        if (fileLocalPluginFolder.getAbsolutePath().equals(file.getAbsolutePath())) {
            return true;
        }
        return false;
    }

    private static void showWarningMessageAddOnFileAlreadyExists(String file, String targetFile) {
        String message =
                Constant.messages.getString("cfu.warn.addOnAlreadyExists", file, targetFile);
        View.getSingleton().showWarningDialog(message);
    }

    private static void showWarningMessageUnableToCopyAddOnFile() {
        String pathPluginFolder = new File(Constant.FOLDER_LOCAL_PLUGIN).getAbsolutePath();
        String message =
                Constant.messages.getString("cfu.warn.unableToCopyAddOn", pathPluginFolder);
        View.getSingleton().showWarningDialog(message);
    }

    private synchronized ManageAddOnsDialog getAddOnsDialog() {
        if (addonsDialog == null) {
            addonsDialog =
                    new ManageAddOnsDialog(this, this.getCurrentVersion(), getLocalVersionInfo());
            if (this.previousVersionInfo != null) {
                addonsDialog.setPreviousVersionInfo(this.previousVersionInfo);
            }
            if (this.latestVersionInfo != null) {
                addonsDialog.setLatestVersionInfo(this.latestVersionInfo);
            }
        }
        return addonsDialog;
    }

    private void downloadFile(URL url, File targetFile, long size, String hash) {
        if (hasView()) {
            // Report info to the Output tab
            getView()
                    .getOutputPanel()
                    .append(
                            Constant.messages.getString(
                                            "cfu.output.downloading",
                                            url.toString(),
                                            targetFile.getAbsolutePath())
                                    + "\n");
        }
        this.downloadFiles.add(this.downloadManager.downloadFile(url, targetFile, size, hash));

        if (this.downloadProgressThread != null && !this.downloadProgressThread.isAlive()) {
            this.downloadProgressThread = null;
        }
        if (this.downloadProgressThread == null) {
            this.downloadProgressThread =
                    new Thread("ZAP-DownloadInstaller") {
                        @Override
                        public void run() {
                            while (downloadManager.getCurrentDownloadCount() > 0) {
                                getScanStatus()
                                        .setScanCount(downloadManager.getCurrentDownloadCount());
                                if (addonsDialog != null && addonsDialog.isVisible()) {
                                    addonsDialog.showProgress();
                                }
                                try {
                                    sleep(100);
                                } catch (InterruptedException e) {
                                    // Ignore
                                }
                            }
                            // Complete download progress
                            if (addonsDialog != null) {
                                addonsDialog.showProgress();
                            }
                            getScanStatus().setScanCount(0);
                            installNewExtensions();
                        }
                    };
            this.installsOk = true;
            this.installsCompleted = false;
            this.downloadProgressThread.start();
        }
    }

    public void installNewExtensions() {
        final OptionsParamCheckForUpdates options =
                getModel().getOptionsParam().getCheckForUpdatesParam();
        List<Downloader> handledFiles = new ArrayList<>();

        MutableBoolean allInstalled = new MutableBoolean(true);
        for (Downloader dl : downloadFiles) {
            if (dl.getFinished() == null) {
                continue;
            }
            handledFiles.add(dl);
            try {
                if (!dl.isValidated()) {
                    logger.debug("Ignoring unvalidated download: {}", dl.getUrl());
                    allInstalled.setFalse();
                    if (addonsDialog != null) {
                        addonsDialog.notifyAddOnDownloadFailed(dl.getUrl().toString());
                    } else {
                        String url = dl.getUrl().toString();
                        for (AddOn addOn : latestVersionInfo.getAddOns()) {
                            if (url.equals(addOn.getUrl().toString())) {
                                addOn.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
                                break;
                            }
                        }
                    }
                } else if (AddOn.isAddOnFileName(dl.getTargetFile().getName())) {
                    File f = dl.getTargetFile();
                    if (!options.getDownloadDirectory()
                            .equals(dl.getTargetFile().getParentFile())) {
                        // Move the file to the specified directory - we do this after its been
                        // downloaded
                        // as these directories can be shared, and other ZAP instances could get
                        // incomplete
                        // add-ons
                        try {
                            f =
                                    new File(
                                            options.getDownloadDirectory(),
                                            dl.getTargetFile().getName());
                            logger.info(
                                    "Moving downloaded add-on from {} to {}",
                                    dl.getTargetFile().getAbsolutePath(),
                                    f.getAbsolutePath());
                            FileUtils.moveFile(dl.getTargetFile(), f);
                        } catch (Exception e) {
                            if (!f.exists() && dl.getTargetFile().exists()) {
                                logger.error(
                                        "Failed to move downloaded add-on from {} to {} - left at original location",
                                        dl.getTargetFile().getAbsolutePath(),
                                        f.getAbsolutePath(),
                                        e);
                                f = dl.getTargetFile();
                            } else {
                                logger.error(
                                        "Failed to move downloaded add-on from {} to {} - skipping",
                                        dl.getTargetFile().getAbsolutePath(),
                                        f.getAbsolutePath(),
                                        e);
                                allInstalled.setFalse();
                                continue;
                            }
                        }
                    }

                    AddOn.createAddOn(f.toPath())
                            .ifPresent(
                                    ao -> {
                                        if (ao.canLoadInCurrentVersion()) {
                                            allInstalled.setValue(
                                                    allInstalled.booleanValue() & install(ao));
                                        } else {
                                            logger.info(
                                                    "Can't load add-on: {} Not before={} Not from={} Version={}",
                                                    ao.getName(),
                                                    ao.getNotBeforeVersion(),
                                                    ao.getNotFromVersion(),
                                                    Constant.PROGRAM_VERSION);
                                        }
                                    });
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        for (Downloader dl : handledFiles) {
            // Can't remove in loop above as we're iterating through the list
            this.downloadFiles.remove(dl);
        }
        this.installsCompleted = true;
        this.installsOk = allInstalled.booleanValue();
    }

    public int getDownloadProgressPercent(URL url) throws Exception {
        return this.downloadManager.getProgressPercent(url);
    }

    public int getCurrentDownloadCount() {
        return this.downloadManager.getCurrentDownloadCount();
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookMenu().addHelpMenuItem(getMenuItemCheckUpdate());
            extensionHook.getHookMenu().addFileMenuItem(getMenuItemLoadAddOn());

            extensionHook.getHookView().addMainToolBarComponent(getAddonsButton());
            extensionHook.getHookView().addMainToolBarComponent(getCheckForUpdatesButton());

            getView()
                    .getMainFrame()
                    .getMainFooterPanel()
                    .addFooterToolbarRightLabel(getScanStatus().getCountLabel());
        }
        extensionHook.addCommandLine(getCommandLineArguments());
        this.api = new AutoUpdateAPI(this);
        this.api.addApiOptions(getModel().getOptionsParam().getCheckForUpdatesParam());
        extensionHook.addApiImplementor(this.api);
    }

    private ScanStatus getScanStatus() {
        if (scanStatus == null) {
            scanStatus =
                    new ScanStatus(
                            new ImageIcon(
                                    ExtensionAutoUpdate.class.getResource(
                                            "/resource/icon/fugue/download.png")),
                            Constant.messages.getString("cfu.downloads.icon.title"));
        }
        return scanStatus;
    }

    private JButton getAddonsButton() {
        if (addonsButton == null) {
            addonsButton = new JButton();
            addonsButton.setIcon(
                    new ImageIcon(
                            ExtensionAutoUpdate.class.getResource(
                                    "/resource/icon/fugue/block.png")));
            addonsButton.setToolTipText(Constant.messages.getString("cfu.button.addons.browse"));
            addonsButton.setEnabled(true);
            addonsButton.addActionListener(e -> getAddOnsDialog().setVisible(true));
        }
        return this.addonsButton;
    }

    private JButton getCheckForUpdatesButton() {
        if (checkForUpdatesButton == null) {
            checkForUpdatesButton = new JButton();
            checkForUpdatesButton.setIcon(
                    new ImageIcon(
                            ExtensionAutoUpdate.class.getResource(
                                    "/resource/icon/fugue/update-zap.png")));
            checkForUpdatesButton.setToolTipText(
                    Constant.messages.getString("cfu.button.checkForUpdates"));
            checkForUpdatesButton.setEnabled(true);
            checkForUpdatesButton.addActionListener(e -> checkForUpdates(true));
        }
        return this.checkForUpdatesButton;
    }

    private void checkForUpdates(boolean force) {
        getAddOnsDialog().setVisible(true);
        getAddOnsDialog().checkForUpdates(force);
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("autoupdate.desc");
    }

    @Override
    public void destroy() {
        this.downloadManager.shutdown(true);
    }

    private int dayDiff(Date d1, Date d2) {
        long diff = d1.getTime() - d2.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    public void alertIfNewVersions() {
        // Kicks off a thread and tells user if there are new versions, depending on the options the
        // user has chosen.
        // Only expect this to be called on startup

        final OptionsParamCheckForUpdates options =
                getModel().getOptionsParam().getCheckForUpdatesParam();

        if (Constant.isSilent()) {
            // Never make unsolicited requests in silent mode
            logger.info("Shh! No check-for-update - silent mode enabled");
            return;
        }

        AddOnCollection prev = this.getPreviousVersionInfo();
        if (hasView() && prev != null) {
            ZapRelease rel = prev.getZapRelease();
            if (rel != null && rel.isNewerThan(this.getCurrentVersion())) {
                File f = new File(Constant.FOLDER_LOCAL_PLUGIN, rel.getFileName());
                if (f.exists() && f.length() >= rel.getSize()) {
                    // Already downloaded, prompt to install and exit
                    this.promptToLaunchReleaseAndClose(rel.getVersion(), f);
                }
            }
        }

        if (!options.checkOnStart()) {
            if (hasView()) {
                alertIfOutOfDate(false);
            }
            return;
        }

        if (ProcessType.desktop.equals(ZAP.getProcessType())) {
            // Handle the response in a callback for the GUI
            this.getLatestVersionInfo(this, false);
        } else {
            // In automation always do this inline so that add-ons are updated before cmdline args
            // are applied
            AddOnCollection aoc = this.getLatestVersionInfo(null, false);
            if (aoc != null) {
                this.updateAddOnsInline(aoc);
            }
        }
    }

    private void updateAddOnsInline(AddOnCollection aoc) {
        // Create some temporary options with the settings we need
        OptionsParamCheckForUpdates options = new OptionsParamCheckForUpdates();
        options.load(new XMLPropertiesConfiguration());
        options.setCheckOnStart(true);
        options.setCheckAddonUpdates(true);
        options.setInstallAddonUpdates(true);
        checkForAddOnUpdates(aoc, options);
        waitForDownloadInstalls();
    }

    private void warnIfOutOfDate() {
        final OptionsParamCheckForUpdates options =
                getModel().getOptionsParam().getCheckForUpdatesParam();
        Date today = new Date();
        Date releaseCreated = Constant.getReleaseCreateDate();
        if (releaseCreated != null) {
            // Should only be null for dev builds
            if (dayDiff(today, releaseCreated) > 365) {
                // Oh no, its more than a year old!
                if (ZAP.getProcessType().equals(ZAP.ProcessType.cmdline)) {
                    CommandLine.error(
                            "This ZAP installation is over a year old - its probably very out of date");
                } else {
                    logger.warn(
                            "This ZAP installation is over a year old - its probably very out of date");
                }
                return;
            }
        }

        Date lastChecked = options.getDayLastChecked();
        Date installDate = Constant.getInstallDate();
        if (installDate == null || dayDiff(today, installDate) < 90) {
            // Dont warn if installed in the last 3 months
        } else if (lastChecked == null || dayDiff(today, lastChecked) > 90) {
            // Not checked for update in 3 months :(
            if (ZAP.getProcessType().equals(ZAP.ProcessType.cmdline)) {
                CommandLine.error(
                        "No check for updates for over 3 month - add-ons may well be out of date");
            } else {
                logger.warn(
                        "No check for updates for over 3 month - add-ons may well be out of date");
            }
        }
    }

    private void alertIfOutOfDate(boolean alwaysPrompt) {
        final OptionsParamCheckForUpdates options =
                getModel().getOptionsParam().getCheckForUpdatesParam();
        Date today = new Date();
        Date releaseCreated = Constant.getReleaseCreateDate();
        Date lastInstallWarning = options.getDayLastInstallWarned();
        int result = -1;
        logger.debug("Install created {}", releaseCreated);
        if (releaseCreated != null) {
            // Should only be null for dev builds
            int daysOld = dayDiff(today, releaseCreated);
            logger.debug("Install is {} days old", daysOld);
            if (daysOld > 365) {
                // Oh no, its more than a year old!
                boolean setCfuOnStart = false;

                if (alwaysPrompt
                        || lastInstallWarning == null
                        || dayDiff(today, lastInstallWarning) > 30) {
                    JCheckBox cfuOnStart =
                            new JCheckBox(Constant.messages.getString("cfu.label.cfuonstart"));
                    cfuOnStart.setSelected(true);
                    String msg = Constant.messages.getString("cfu.label.oldzap");

                    result =
                            View.getSingleton()
                                    .showYesNoDialog(
                                            getView().getMainFrame(),
                                            new Object[] {msg, cfuOnStart});
                    setCfuOnStart = cfuOnStart.isSelected();
                }
                options.setDayLastInstallWarned();

                if (result == JOptionPane.OK_OPTION) {
                    if (setCfuOnStart) {
                        options.setCheckOnStart(true);
                    }
                    getAddOnsDialog().setVisible(true);
                    getAddOnsDialog().checkForUpdates(false);

                } else if (!oldZapAlertAdded) {
                    JButton button =
                            new JButton(Constant.messages.getString("cfu.label.outofdatezap"));
                    button.setIcon(
                            new ImageIcon(
                                    ExtensionAutoUpdate.class.getResource(
                                            "/resource/icon/16/050.png"))); // Alert triangle
                    button.addActionListener(e -> alertIfOutOfDate(true));

                    getView()
                            .getMainFrame()
                            .getMainFooterPanel()
                            .addFooterToolbarLeftComponent(button);
                    oldZapAlertAdded = true;
                }
                return;
            }
        }

        Date lastChecked = options.getDayLastChecked();
        Date lastUpdateWarning = options.getDayLastUpdateWarned();
        Date installDate = Constant.getInstallDate();
        if (installDate == null || dayDiff(today, installDate) < 90) {
            // Dont warn if installed in the last 3 months
        } else if (lastChecked == null || dayDiff(today, lastChecked) > 90) {
            // Not checked for updates in 3 months :(
            boolean setCfuOnStart = false;

            if (alwaysPrompt
                    || lastUpdateWarning == null
                    || dayDiff(today, lastUpdateWarning) > 30) {
                JCheckBox cfuOnStart =
                        new JCheckBox(Constant.messages.getString("cfu.label.cfuonstart"));
                cfuOnStart.setSelected(true);
                String msg = Constant.messages.getString("cfu.label.norecentcfu");

                result =
                        View.getSingleton()
                                .showYesNoDialog(
                                        getView().getMainFrame(), new Object[] {msg, cfuOnStart});
                setCfuOnStart = cfuOnStart.isSelected();
            }
            options.setDayLastUpdateWarned();

            if (result == JOptionPane.OK_OPTION) {
                if (setCfuOnStart) {
                    options.setCheckOnStart(true);
                }
                getAddOnsDialog().setVisible(true);
                getAddOnsDialog().checkForUpdates(false);
                if (noCfuAlertAdded) {
                    getView()
                            .getMainFrame()
                            .getMainFooterPanel()
                            .removeFooterToolbarLeftComponent(getOutOfDateButton());
                }

            } else if (!noCfuAlertAdded) {
                getView()
                        .getMainFrame()
                        .getMainFooterPanel()
                        .addFooterToolbarLeftComponent(getOutOfDateButton());
                noCfuAlertAdded = true;
            }
        }
    }

    private JButton getOutOfDateButton() {
        if (outOfDateButton == null) {
            outOfDateButton = new JButton(Constant.messages.getString("cfu.label.outofdateaddons"));
            outOfDateButton.setIcon(
                    new ImageIcon(
                            ExtensionAutoUpdate.class.getResource(
                                    "/resource/icon/16/050.png"))); // Alert triangle
            outOfDateButton.addActionListener(e -> alertIfOutOfDate(true));
        }
        return outOfDateButton;
    }

    protected AddOnCollection getLocalVersionInfo() {
        if (localVersionInfo == null) {
            localVersionInfo = ExtensionFactory.getAddOnLoader().getAddOnCollection();
        }
        return localVersionInfo;
    }

    public void setCheckForUpdatesSupplier(Supplier<ZapXmlConfiguration> supplier) {
        this.checkForUpdatesSupplier = supplier;
    }

    private ZapXmlConfiguration getRemoteConfiguration() throws ConfigurationException {
        if (checkForUpdatesSupplier != null) {
            ZapXmlConfiguration config = checkForUpdatesSupplier.get();
            if (config != null) {
                // Save version file so we can report new addons next time
                File f = new File(Constant.FOLDER_LOCAL_PLUGIN, VERSION_FILE_NAME);
                try {
                    config.save(f);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return config;
            }
        }
        return null;
    }

    protected String getLatestVersionNumber() {
        if (this.getLatestVersionInfo() == null
                || this.getLatestVersionInfo().getZapRelease() == null) {
            return null;
        }
        return this.getLatestVersionInfo().getZapRelease().getVersion();
    }

    protected boolean isLatestVersion() {
        if (this.getLatestVersionInfo() == null
                || this.getLatestVersionInfo().getZapRelease() == null) {
            return true;
        }
        return !this.getLatestVersionInfo().getZapRelease().isNewerThan(this.getCurrentVersion());
    }

    protected boolean downloadLatestRelease() {
        if (Constant.isKali()) {
            if (hasView()) {
                // Just tell the user to use one of the Kali options
                View.getSingleton()
                        .showMessageDialog(
                                this.getAddOnsDialog(),
                                new ZapHtmlLabel(Constant.messages.getString("cfu.kali.options")));
            }
            return false;
        }
        if (this.getLatestVersionInfo() == null
                || this.getLatestVersionInfo().getZapRelease() == null) {
            return false;
        }
        ZapRelease latestRelease = this.getLatestVersionInfo().getZapRelease();
        if (latestRelease.isNewerThan(this.getCurrentVersion())) {
            File f = new File(Constant.FOLDER_LOCAL_PLUGIN, latestRelease.getFileName());
            downloadFile(
                    latestRelease.getUrl(), f, latestRelease.getSize(), latestRelease.getHash());
            return true;
        }
        return false;
    }

    private AddOnCollection getPreviousVersionInfo() {
        if (this.previousVersionInfo == null) {
            File f = new File(Constant.FOLDER_LOCAL_PLUGIN, VERSION_FILE_NAME);
            if (f.exists()) {
                try {
                    this.previousVersionInfo =
                            new AddOnCollection(new ZapXmlConfiguration(f), this.getPlatform());
                } catch (ConfigurationException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return this.previousVersionInfo;
    }

    protected List<Downloader> getAllDownloadsProgress() {
        return this.downloadManager.getProgress();
    }

    protected List<AddOn> getUpdatedAddOns() {
        return getLocalVersionInfo().getUpdatedAddOns(this.getLatestVersionInfo());
    }

    protected List<AddOn> getNewAddOns() {
        AddOnCollection prev = this.getPreviousVersionInfo();
        if (prev != null) {
            return prev.getNewAddOns(this.getLatestVersionInfo());
        }
        return getLocalVersionInfo().getNewAddOns(this.getLatestVersionInfo());
    }

    protected AddOn getAddOn(String id) {
        AddOnCollection aoc = getLatestVersionInfo();
        if (aoc != null) {
            return aoc.getAddOn(id);
        }
        return null;
    }

    protected List<AddOn> getInstalledAddOns() {
        return getLocalVersionInfo().getInstalledAddOns();
    }

    protected List<AddOn> getLocalAddOns() {
        return getLocalVersionInfo().getAddOns();
    }

    protected List<AddOn> getMarketplaceAddOns() {
        AddOnCollection aoc = this.getLatestVersionInfo();
        if (aoc != null) {
            return aoc.getAddOns();
        }
        return Collections.<AddOn>emptyList();
    }

    protected AddOnCollection getLatestVersionInfo() {
        return getLatestVersionInfo(null, false);
    }

    protected AddOnCollection getLatestVersionInfo(
            final CheckForUpdateCallback callback, boolean force) {
        if (latestVersionInfo == null || force) {

            if (this.remoteCallThread == null || !this.remoteCallThread.isAlive()) {
                this.remoteCallThread =
                        new Thread("ZAP-cfu") {

                            @Override
                            public void run() {
                                // Using a thread as the first call could timeout
                                // and we dont want the ui to hang in the meantime
                                try {
                                    ZapXmlConfiguration conf = getRemoteConfiguration();
                                    if (conf != null) {
                                        latestVersionInfo =
                                                new AddOnCollection(conf, getPlatform(), false);
                                    }
                                } catch (Exception e1) {
                                    logger.warn(
                                            "Failed to check for updates - see log for details",
                                            e1);
                                }
                                if (latestVersionInfo != null) {
                                    for (AddOn addOn : latestVersionInfo.getAddOns()) {
                                        AddOn localAddOn =
                                                getLocalVersionInfo().getAddOn(addOn.getId());
                                        if (localAddOn != null && !addOn.isUpdateTo(localAddOn)) {
                                            addOn.setInstallationStatus(
                                                    localAddOn.getInstallationStatus());
                                        }
                                    }
                                }
                                if (callback != null) {
                                    logger.debug("Calling callback with {}", latestVersionInfo);
                                    callback.gotLatestData(latestVersionInfo);
                                }
                                logger.debug("Done");
                            }
                        };
                this.remoteCallThread.start();
            }
            if (callback == null) {
                // Synchronous, but include a 60 sec max anyway, give enough(?) time for 1st request
                // to timeout (default 20s)
                // and the 2nd to be fully processed (e.g. in case the connection is throttled,
                // requires proxy authentication).
                int i = 0;
                while (latestVersionInfo == null && this.remoteCallThread.isAlive() && i < 60) {
                    try {
                        Thread.sleep(1000);
                        i++;
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
        }
        return latestVersionInfo;
    }

    private String getCurrentVersion() {
        // Put into local function to make it easy to manually test different scenarios;)
        return Constant.PROGRAM_VERSION;
    }

    private Platform getPlatform() {
        if (Constant.isDailyBuild()) {
            return Platform.daily;
        } else if (Constant.isWindows()) {
            return Platform.windows;
        } else if (Constant.isLinux()) {
            return Platform.linux;
        } else {
            return Platform.mac;
        }
    }

    protected void promptToLaunchReleaseAndClose(String version, File f) {
        int ans =
                getView()
                        .showConfirmDialog(
                                Constant.messages.getString(
                                        "cfu.confirm.launch", version, f.getAbsolutePath()));
        if (ans == JOptionPane.OK_OPTION) {
            Control.getSingleton().exit(false, f);
        }
    }

    private boolean install(AddOn ao) {
        if (!ao.canLoadInCurrentVersion()) {
            throw new IllegalArgumentException(
                    "Can't load add-on "
                            + ao.getName()
                            + " Not before="
                            + ao.getNotBeforeVersion()
                            + " Not from="
                            + ao.getNotFromVersion()
                            + " Version="
                            + Constant.PROGRAM_VERSION);
        }

        AddOn installedAddOn = this.getLocalVersionInfo().getAddOn(ao.getId());
        if (installedAddOn != null && !uninstallAddOn(null, installedAddOn, true)) {
            // Can't uninstall the old version, so dont try to install the new one
            return false;
        }
        logger.info("Installing new addon {} v{}", ao.getId(), ao.getVersion());
        if (hasView()) {
            // Report info to the Output tab
            getView()
                    .getOutputPanel()
                    .append(
                            Constant.messages.getString(
                                            "cfu.output.installing", ao.getName(), ao.getVersion())
                                    + "\n");
        }

        ExtensionFactory.getAddOnLoader().addAddon(ao);

        logger.info("Finished installing new addon {} v{}", ao.getId(), ao.getVersion());
        if (hasView()) {
            // Report info to the Output tab
            getView()
                    .getOutputPanel()
                    .append(
                            Constant.messages.getString(
                                            "cfu.output.installing.finished",
                                            ao.getName(),
                                            ao.getVersion())
                                    + "\n");
        }

        if (latestVersionInfo != null) {
            AddOn addOn = latestVersionInfo.getAddOn(ao.getId());
            if (addOn != null
                    && AddOn.InstallationStatus.DOWNLOADING == addOn.getInstallationStatus()) {
                addOn.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);
            }
        }

        if (addonsDialog != null) {
            addonsDialog.notifyAddOnInstalled(ao);
        }
        return true;
    }

    private boolean uninstall(
            AddOn addOn, boolean upgrading, AddOnUninstallationProgressCallback callback) {
        logger.debug("Trying to uninstall addon {} v{}", addOn.getId(), addOn.getVersion());

        boolean removedDynamically =
                ExtensionFactory.getAddOnLoader().removeAddOn(addOn, upgrading, callback);
        if (removedDynamically) {
            logger.debug("Uninstalled add-on {}", addOn.getName());

            if (latestVersionInfo != null) {
                AddOn availableAddOn = latestVersionInfo.getAddOn(addOn.getId());
                if (availableAddOn != null
                        && availableAddOn.getInstallationStatus()
                                != AddOn.InstallationStatus.AVAILABLE) {
                    availableAddOn.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
                }
            }
        } else {
            logger.debug("Failed to uninstall add-on {} v{}", addOn.getId(), addOn.getVersion());
        }
        return removedDynamically;
    }

    @Override
    public void insecureUrl(String url, Exception cause) {
        logger.error("Failed to get check for updates on {}", url, cause);
        if (hasView()) {
            getView().showWarningDialog(Constant.messages.getString("cfu.warn.badurl"));
        }
    }

    @Override
    public void gotLatestData(AddOnCollection aoc) {
        if (aoc == null) {
            return;
        }
        if (getView() != null) {
            // Initialise the dialogue so that it gets notifications of
            // possible add-on changes and is also shown when needed
            try {
                EventQueue.invokeAndWait(() -> getAddOnsDialog());
            } catch (InvocationTargetException | InterruptedException e) {
                logger.error("Failed to initialise the Manage Add-ons dialogue:", e);
            }
        }
        try {
            ZapRelease rel = aoc.getZapRelease();

            OptionsParamCheckForUpdates options =
                    getModel().getOptionsParam().getCheckForUpdatesParam();

            if (rel.isNewerThan(getCurrentVersion())) {
                logger.debug("There is a newer release: {}", rel.getVersion());
                // New ZAP release
                if (Constant.isKali()) {
                    // Kali has its own package management system
                    if (hasView()) {
                        getAddOnsDialog().setVisible(true);
                    }
                    return;
                }

                File f = new File(Constant.FOLDER_LOCAL_PLUGIN, rel.getFileName());
                if (f.exists() && f.length() >= rel.getSize()) {
                    // Already downloaded, prompt to install and exit
                    promptToLaunchReleaseAndClose(rel.getVersion(), f);
                } else if (options.isDownloadNewRelease()) {
                    logger.debug("Auto-downloading release");
                    if (downloadLatestRelease() && addonsDialog != null) {
                        addonsDialog.setDownloadingZap();
                    }
                } else if (addonsDialog != null) {
                    // Just show the dialog
                    addonsDialog.setVisible(true);
                }
                return;
            }

            boolean keepChecking = checkForAddOnUpdates(aoc, options);

            if (keepChecking && addonsDialog != null) {
                List<AddOn> newAddOns = getNewAddOns();
                if (!newAddOns.isEmpty()) {
                    boolean report = false;
                    for (AddOn addon : newAddOns) {
                        switch (addon.getStatus()) {
                            case alpha:
                                if (options.isReportAlphaAddons()) {
                                    report = true;
                                }
                                break;
                            case beta:
                                if (options.isReportBetaAddons()) {
                                    report = true;
                                }
                                break;
                            case release:
                                if (options.isReportReleaseAddons()) {
                                    report = true;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (report) {
                        getAddOnsDialog().setVisible(true);
                        getAddOnsDialog().selectMarketplaceTab();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore (well, debug;), will be already logged
            logger.debug(e.getMessage(), e);
        }
    }

    private boolean checkForAddOnUpdates(AddOnCollection aoc, OptionsParamCheckForUpdates options) {
        List<AddOn> updates = getUpdatedAddOns();
        if (updates.isEmpty()) {
            return true;
        }

        // Log at info for daemon mode as its the only indication if not auto-installing
        logger.info("There is/are {} newer addons", updates.size());
        AddOnDependencyChecker addOnDependencyChecker =
                new AddOnDependencyChecker(localVersionInfo, aoc);
        Set<AddOn> addOns = new HashSet<>(updates);
        AddOnDependencyChecker.AddOnChangesResult result =
                addOnDependencyChecker.calculateUpdateChanges(addOns);

        if (!result.getUninstalls().isEmpty() || result.isNewerJavaVersionRequired()) {
            if (options.isCheckAddonUpdates()) {
                if (addonsDialog != null) {
                    // Just show the dialog
                    getAddOnsDialog().setVisible(true);
                    return false;
                }
                logger.info(
                        "Updates not installed some add-ons would be uninstalled or require newer java version: {}",
                        result.getUninstalls());
            }
            return true;
        }

        if (options.isInstallAddonUpdates()) {
            logger.debug("Auto-downloading addons");
            processAddOnChanges(null, result);

            return false;
        }

        if (options.isInstallScannerRules()) {
            for (Iterator<AddOn> it = addOns.iterator(); it.hasNext(); ) {
                if (!it.next().getId().contains("scanrules")) {
                    it.remove();
                }
            }

            logger.debug("Auto-downloading scanner rules");
            processAddOnChanges(null, addOnDependencyChecker.calculateUpdateChanges(addOns));
            return false;
        }

        if (options.isCheckAddonUpdates() && addonsDialog != null) {
            // Just show the dialog
            addonsDialog.setVisible(true);
            return false;
        }

        return true;
    }

    /**
     * Processes the given add-on changes.
     *
     * @param caller the caller to set as parent of shown dialogues
     * @param changes the changes that will be processed
     */
    void processAddOnChanges(Window caller, AddOnDependencyChecker.AddOnChangesResult changes) {
        if (addonsDialog != null) {
            addonsDialog.setDownloadingUpdates();
        }

        if (getView() != null) {
            Set<AddOn> addOns = new HashSet<>(changes.getUninstalls());
            addOns.addAll(changes.getOldVersions());

            Set<Extension> extensions = new HashSet<>();
            extensions.addAll(changes.getUnloadExtensions());
            extensions.addAll(changes.getSoftUnloadExtensions());

            if (!warnUnsavedResourcesOrActiveActions(caller, addOns, extensions, true)) {
                return;
            }
        }

        uninstallAddOns(caller, changes.getUninstalls(), false);

        Set<AddOn> allAddons = new HashSet<>(changes.getNewVersions());
        allAddons.addAll(changes.getInstalls());

        for (AddOn addOn : allAddons) {
            if (addonsDialog != null) {
                addonsDialog.notifyAddOnDownloading(addOn);
            }
            downloadAddOn(addOn);
        }
    }

    boolean warnUnsavedResourcesOrActiveActions(
            Window caller, Collection<AddOn> addOns, Set<Extension> extensions, boolean updating) {
        Set<AddOn> allAddOns = new HashSet<>(addOns);
        addDependents(allAddOns);

        String baseMessagePrefix = updating ? "cfu.update." : "cfu.uninstall.";

        String unsavedResources = getExtensionsUnsavedResources(addOns, extensions);
        String activeActions = getExtensionsActiveActions(addOns, extensions);

        String message = null;
        if (!unsavedResources.isEmpty()) {
            if (activeActions.isEmpty()) {
                message =
                        Constant.messages.getString(
                                baseMessagePrefix + "message.resourcesNotSaved", unsavedResources);
            } else {
                message =
                        Constant.messages.getString(
                                baseMessagePrefix + "message.resourcesNotSavedAndActiveActions",
                                unsavedResources,
                                activeActions);
            }
        } else if (!activeActions.isEmpty()) {
            message =
                    Constant.messages.getString(
                            baseMessagePrefix + "message.activeActions", activeActions);
        }

        if (message != null
                && JOptionPane.showConfirmDialog(
                                getWindowParent(caller),
                                new ZapHtmlLabel(message),
                                Constant.PROGRAM_NAME,
                                JOptionPane.YES_NO_OPTION)
                        != JOptionPane.YES_OPTION) {
            return false;
        }

        return true;
    }

    private void addDependents(Set<AddOn> addOns) {
        for (AddOn availableAddOn : localVersionInfo.getInstalledAddOns()) {
            if (availableAddOn.dependsOn(addOns) && !addOns.contains(availableAddOn)) {
                addOns.add(availableAddOn);
                addDependents(addOns);
            }
        }
    }

    private Window getWindowParent(Window caller) {
        if (caller != null) {
            return caller;
        }

        if (addonsDialog != null && addonsDialog.isFocused()) {
            return addonsDialog;
        }

        return getView().getMainFrame();
    }

    /**
     * Returns all unsaved resources of the given {@code addOns} and {@code extensions} wrapped in
     * {@code <li>} elements or an empty {@code String} if there are no unsaved resources.
     *
     * @param addOns the add-ons that will be queried for unsaved resources
     * @param extensions the extensions that will be queried for unsaved resources
     * @return a {@code String} containing all unsaved resources or empty {@code String} if none
     * @since 2.4.0
     * @see Extension#getUnsavedResources()
     */
    private static String getExtensionsUnsavedResources(
            Collection<AddOn> addOns, Set<Extension> extensions) {
        return getExtensionsMessages(addOns, extensions, Extension::getUnsavedResources);
    }

    private static String getExtensionsMessages(
            Collection<AddOn> addOns,
            Set<Extension> extensions,
            Function<Extension, List<String>> function) {
        List<String> messages = new ArrayList<>();
        for (AddOn addOn : addOns) {
            addMessages(addOn.getLoadedExtensions(), function, messages);
        }
        addMessages(extensions, function, messages);
        return wrapEntriesInLiTags(messages);
    }

    private static void addMessages(
            Collection<Extension> extensions,
            Function<Extension, List<String>> function,
            List<String> sink) {
        extensions.stream()
                .filter(Extension::isEnabled)
                .map(
                        e -> {
                            try {
                                List<String> messages = function.apply(e);
                                if (messages != null) {
                                    return messages;
                                }
                            } catch (Throwable ex) {
                                logger.error(
                                        "Error while getting messages from {}",
                                        e.getClass().getCanonicalName(),
                                        ex);
                            }
                            return Collections.<String>emptyList();
                        })
                .forEach(sink::addAll);
    }

    private static String wrapEntriesInLiTags(List<String> entries) {
        if (entries.isEmpty()) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder(entries.size() * 15);
        for (String entry : entries) {
            strBuilder.append("<li>");
            strBuilder.append(entry);
            strBuilder.append("</li>");
        }
        return strBuilder.toString();
    }

    /**
     * Returns all active actions of the given {@code addOns} and {@code extensions} wrapped in
     * {@code <li>} elements or an empty {@code String} if there are no active actions.
     *
     * @param addOns the add-ons that will be queried for active actions
     * @param extensions the extensions that will be queried for active actions
     * @return a {@code String} containing all active actions or empty {@code String} if none
     * @since 2.4.0
     * @see Extension#getActiveActions()
     */
    private static String getExtensionsActiveActions(
            Collection<AddOn> addOns, Set<Extension> extensions) {
        return getExtensionsMessages(addOns, extensions, Extension::getActiveActions);
    }

    private void downloadAddOn(AddOn addOn) {
        if (AddOn.InstallationStatus.DOWNLOADING == addOn.getInstallationStatus()) {
            return;
        }

        addOn.setInstallationStatus(AddOn.InstallationStatus.DOWNLOADING);
        downloadFile(addOn.getUrl(), addOn.getFile(), addOn.getSize(), addOn.getHash());
    }

    private boolean uninstallAddOn(Window caller, AddOn addOn, boolean update) {
        Set<AddOn> addOns = new HashSet<>();
        addOns.add(addOn);
        return uninstallAddOns(caller, addOns, update);
    }

    boolean uninstallAddOns(Window caller, Set<AddOn> addOns, boolean updates) {
        if (addOns == null || addOns.isEmpty()) {
            return true;
        }

        if (getView() != null) {
            return uninstallAddOnsWithView(caller, addOns, updates, new HashSet<>());
        }

        final Set<AddOn> failedUninstallations = new HashSet<>();
        for (AddOn addOn : addOns) {
            if (!uninstall(addOn, updates, null)) {
                failedUninstallations.add(addOn);
            }
        }

        if (!failedUninstallations.isEmpty()) {
            logger.warn(
                    "It's recommended to restart ZAP. Not all add-ons were successfully uninstalled: {}",
                    failedUninstallations);
            return false;
        }

        return true;
    }

    boolean uninstallAddOnsWithView(
            final Window caller,
            final Set<AddOn> addOns,
            final boolean updates,
            final Set<AddOn> failedUninstallations) {
        if (addOns == null || addOns.isEmpty()) {
            return true;
        }

        if (!EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(
                        () ->
                                uninstallAddOnsWithView(
                                        caller, addOns, updates, failedUninstallations));
            } catch (InvocationTargetException | InterruptedException e) {
                logger.error("Failed to uninstall add-ons:", e);
                return false;
            }
            return failedUninstallations.isEmpty();
        }

        final Window parent = getWindowParent(caller);

        final UninstallationProgressDialogue waitDialogue =
                new UninstallationProgressDialogue(parent, addOns);
        waitDialogue.addAddOnUninstallListener(
                new AddOnUninstallListener() {

                    @Override
                    public void uninstallingAddOn(AddOn addOn, boolean updating) {
                        if (updating) {
                            getView()
                                    .getOutputPanel()
                                    .append(
                                            Constant.messages.getString(
                                                            "cfu.output.replacing",
                                                            addOn.getName(),
                                                            addOn.getVersion())
                                                    + "\n");
                        }
                    }

                    @Override
                    public void addOnUninstalled(AddOn addOn, boolean update, boolean uninstalled) {
                        if (uninstalled) {
                            if (!update && addonsDialog != null) {
                                addonsDialog.notifyAddOnUninstalled(addOn);
                            }

                            getView()
                                    .getOutputPanel()
                                    .append(
                                            Constant.messages.getString(
                                                            "cfu.output.uninstalled",
                                                            addOn.getName(),
                                                            addOn.getVersion())
                                                    + "\n");
                        } else {
                            if (addonsDialog != null) {
                                addonsDialog.notifyAddOnFailedUninstallation(addOn);
                            }

                            String message;
                            if (update) {
                                message =
                                        Constant.messages.getString(
                                                "cfu.output.replace.failed",
                                                addOn.getName(),
                                                addOn.getVersion());
                            } else {
                                message =
                                        Constant.messages.getString(
                                                "cfu.output.uninstall.failed",
                                                addOn.getName(),
                                                addOn.getVersion());
                            }
                            getView().getOutputPanel().append(message + "\n");
                        }
                    }
                });

        SwingWorker<Void, UninstallationProgressEvent> a =
                new SwingWorker<Void, UninstallationProgressEvent>() {

                    @Override
                    protected void process(List<UninstallationProgressEvent> events) {
                        waitDialogue.update(events);
                    }

                    @Override
                    protected Void doInBackground() {
                        UninstallationProgressHandler progressHandler =
                                new UninstallationProgressHandler() {

                                    @Override
                                    protected void publishEvent(UninstallationProgressEvent event) {
                                        publish(event);
                                    }
                                };

                        for (AddOn addOn : addOns) {
                            if (!uninstall(addOn, updates, progressHandler)) {
                                failedUninstallations.add(addOn);
                            }
                        }

                        if (!failedUninstallations.isEmpty()) {
                            logger.warn(
                                    "Not all add-ons were successfully uninstalled: {}",
                                    failedUninstallations);
                        }

                        return null;
                    }
                };

        waitDialogue.bind(a);
        a.execute();
        waitDialogue.setSynchronous(updates);
        waitDialogue.setVisible(true);

        return failedUninstallations.isEmpty();
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_CFU_INSTALL_IDX] =
                new CommandLineArgument(
                        ADDON_INSTALL,
                        1,
                        null,
                        "",
                        "-addoninstall <addOnId>   "
                                + Constant.messages.getString("cfu.cmdline.install.help"));
        arguments[ARG_CFU_INSTALL_ALL_IDX] =
                new CommandLineArgument(
                        ADDON_INSTALL_ALL,
                        0,
                        null,
                        "",
                        "-addoninstallall          "
                                + Constant.messages.getString("cfu.cmdline.installall.help"));
        arguments[ARG_CFU_UNINSTALL_IDX] =
                new CommandLineArgument(
                        "-addonuninstall",
                        1,
                        null,
                        "",
                        "-addonuninstall <addOnId> "
                                + Constant.messages.getString("cfu.cmdline.uninstall.help"));
        arguments[ARG_CFU_UPDATE_IDX] =
                new CommandLineArgument(
                        "-addonupdate",
                        0,
                        null,
                        "",
                        "-addonupdate              "
                                + Constant.messages.getString("cfu.cmdline.update.help"));
        arguments[ARG_CFU_LIST_IDX] =
                new CommandLineArgument(
                        "-addonlist",
                        0,
                        null,
                        "",
                        "-addonlist                "
                                + Constant.messages.getString("cfu.cmdline.list.help"));
        return arguments;
    }

    /**
     * Installs the specified add-ons
     *
     * @param addons The identifiers of the add-ons to be installed
     * @return A string containing any error messages, will be empty if there were no problems
     */
    public synchronized String installAddOns(List<String> addons) {
        StringBuilder errorMessages = new StringBuilder();
        AddOnCollection aoc = getLatestVersionInfo();
        if (aoc == null) {
            String error = Constant.messages.getString("cfu.cmdline.nocfu");
            errorMessages.append(error);
            CommandLine.error(error);
        } else {
            for (String aoName : addons) {
                AddOn ao = aoc.getAddOn(aoName);
                if (ao == null) {
                    String error = Constant.messages.getString("cfu.cmdline.noaddon", aoName);
                    errorMessages.append(error);
                    errorMessages.append("\n");
                    CommandLine.error(error);
                    continue;
                }
                AddOnDependencyChecker addOnDependencyChecker =
                        new AddOnDependencyChecker(getLocalVersionInfo(), aoc);
                AddOnDependencyChecker.AddOnChangesResult result;
                // Check to see if its already installed
                AddOn iao = getLocalVersionInfo().getAddOn(aoName);
                if (iao != null) {
                    if (!ao.isUpdateTo(iao)) {
                        CommandLine.info(
                                Constant.messages.getString(
                                        "cfu.cmdline.addoninst", iao.getFile().getAbsolutePath()));
                        continue;
                    }

                    result = addOnDependencyChecker.calculateUpdateChanges(ao);
                } else {
                    result = addOnDependencyChecker.calculateInstallChanges(ao);
                }

                if (!result.getUninstalls().isEmpty()) {
                    String error =
                            Constant.messages.getString(
                                    "cfu.cmdline.addoninst.uninstalls.required",
                                    result.getUninstalls());
                    errorMessages.append(error);
                    errorMessages.append("\n");
                    CommandLine.error(error);
                    continue;
                }

                Set<AddOn> allAddOns = new HashSet<>();
                allAddOns.addAll(result.getInstalls());
                allAddOns.addAll(result.getNewVersions());
                for (AddOn addOn : allAddOns) {
                    CommandLine.info(
                            Constant.messages.getString("cfu.cmdline.addonurl", addOn.getUrl()));
                }

                processAddOnChanges(null, result);
            }
            waitForDownloadInstalls();
            if (!this.installsOk) {
                errorMessages
                        .append(Constant.messages.getString("cfu.cmdline.addoninst.error"))
                        .append("\n");
            }
        }
        return errorMessages.toString();
    }

    /**
     * Uninstalls the specified add-ons
     *
     * @param addons The identifiers of the add-ons to be installed
     * @return A string containing any error messages, will be empty if there were no problems
     */
    public synchronized String uninstallAddOns(List<String> addons) {
        StringBuilder errorMessages = new StringBuilder();
        AddOnCollection aoc = this.getLocalVersionInfo();
        if (aoc == null) {
            String error = Constant.messages.getString("cfu.cmdline.nocfu");
            errorMessages.append(error);
            CommandLine.error(error);
        } else {
            for (String aoName : addons) {
                AddOn ao = aoc.getAddOn(aoName);
                if (ao == null) {
                    String error = Constant.messages.getString("cfu.cmdline.noaddon", aoName);
                    errorMessages.append(error);
                    errorMessages.append("\n");
                    CommandLine.error(error);
                    continue;
                }
                if (ao.isMandatory()) {
                    String error =
                            Constant.messages.getString("cfu.cmdline.mandatoryaddon", aoName);
                    errorMessages.append(error);
                    errorMessages.append("\n");
                    CommandLine.error(error);
                    continue;
                }
                AddOnDependencyChecker addOnDependencyChecker =
                        new AddOnDependencyChecker(getLocalVersionInfo(), aoc);

                Set<AddOn> addonSet = new HashSet<>();
                addonSet.add(ao);
                UninstallationResult result =
                        addOnDependencyChecker.calculateUninstallChanges(addonSet);

                // Check to see if other add-ons depend on it
                if (result.getUninstallations().size() > 1) {
                    // Will always report this add-on as needing to be uninstalled
                    // Remove the specified add-on for the error message
                    result.getUninstallations().remove(ao);
                    String error =
                            Constant.messages.getString(
                                    "cfu.cmdline.addonuninst.uninstalls.required",
                                    result.getUninstallations());
                    errorMessages.append(error);
                    errorMessages.append("\n");
                    CommandLine.error(error);
                    continue;
                }

                if (this.uninstallAddOn(null, ao, false)) {
                    CommandLine.info(
                            Constant.messages.getString("cfu.cmdline.uninstallok", aoName));

                } else {
                    String error = Constant.messages.getString("cfu.cmdline.uninstallfail", aoName);
                    errorMessages.append(error);
                    errorMessages.append("\n");
                    CommandLine.error(error);
                }
            }
        }
        return errorMessages.toString();
    }

    @Override
    public void execute(CommandLineArgument[] args) {
        // Do nothing - everything is done in preExecute
    }

    @Override
    public void preExecute(CommandLineArgument[] args) {
        if (arguments[ARG_CFU_UPDATE_IDX].isEnabled()) {
            AddOnCollection aoc = getLatestVersionInfo();
            if (aoc == null) {
                CommandLine.error(Constant.messages.getString("cfu.cmdline.nocfu"));
            } else {
                this.updateAddOnsInline(aoc);
                CommandLine.info(Constant.messages.getString("cfu.cmdline.updated"));
            }
        }
        if (arguments[ARG_CFU_INSTALL_ALL_IDX].isEnabled()) {
            AddOnCollection aoc = getLatestVersionInfo();
            if (aoc == null) {
                CommandLine.error(Constant.messages.getString("cfu.cmdline.nocfu"));
            } else {
                AddOnDependencyChecker addOnDependencyChecker =
                        new AddOnDependencyChecker(getLocalVersionInfo(), aoc);
                AddOnDependencyChecker.AddOnChangesResult result;
                AddOnDependencyChecker.AddOnChangesResult allResults = null;
                Set<AddOn> allAddOns = new HashSet<>();

                for (AddOn ao : aoc.getAddOns()) {
                    if (ao.getId().equals("coreLang")
                            && (Constant.isDevBuild() || Constant.isDailyBuild())) {
                        // Ignore coreLang add-on if its not a full release
                        // this may well be missing strings that are now necessary
                        continue;
                    }

                    // Check to see if its already installed
                    AddOn iao = getLocalVersionInfo().getAddOn(ao.getId());
                    if (iao != null) {
                        if (!ao.isUpdateTo(iao)) {
                            continue;
                        }

                        result = addOnDependencyChecker.calculateUpdateChanges(ao);
                    } else {
                        result = addOnDependencyChecker.calculateInstallChanges(ao);
                    }

                    if (result.getUninstalls().isEmpty()) {
                        allAddOns.addAll(result.getInstalls());
                        allAddOns.addAll(result.getNewVersions());
                        if (allResults == null) {
                            allResults = result;
                        } else {
                            allResults.addResults(result);
                        }
                    }
                }

                if (allAddOns.isEmpty()) {
                    // Nothing to do
                    return;
                }

                for (AddOn addOn : allAddOns) {
                    CommandLine.info(
                            Constant.messages.getString("cfu.cmdline.addonurl", addOn.getUrl()));
                }

                processAddOnChanges(null, allResults);

                waitForDownloadInstalls();
            }
        }
        if (arguments[ARG_CFU_INSTALL_IDX].isEnabled()) {
            Vector<String> params = arguments[ARG_CFU_INSTALL_IDX].getArguments();
            installAddOns(params);
        }
        if (arguments[ARG_CFU_UNINSTALL_IDX].isEnabled()) {
            Vector<String> params = arguments[ARG_CFU_UNINSTALL_IDX].getArguments();
            uninstallAddOns(params);
        }
        if (arguments[ARG_CFU_LIST_IDX].isEnabled()) {
            AddOnCollection aoc = this.getLocalVersionInfo();
            List<AddOn> aolist = new ArrayList<>(aoc.getAddOns());
            Collections.sort(
                    aolist,
                    new Comparator<AddOn>() {
                        @Override
                        public int compare(AddOn ao1, AddOn ao2) {
                            return ao1.getName().compareTo(ao2.getName());
                        }
                    });

            for (AddOn addon : aolist) {
                CommandLine.info(
                        addon.getName()
                                + "\t"
                                + addon.getId()
                                + "\tv"
                                + addon.getVersion()
                                + "\t"
                                + addon.getStatus().name()
                                + "\t"
                                + addon.getDescription());
            }
        }
    }

    private void waitForDownloadInstalls() {
        while (downloadManager.getCurrentDownloadCount() > 0 || !this.installsCompleted) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        for (Downloader download : downloadManager.getProgress()) {
            if (download.isValidated()) {
                CommandLine.info(
                        Constant.messages.getString(
                                "cfu.cmdline.addondown",
                                download.getTargetFile().getAbsolutePath()));
            } else {
                CommandLine.error(
                        Constant.messages.getString(
                                "cfu.cmdline.addondown.failed",
                                download.getTargetFile().getName()));
            }
        }
    }

    @Override
    public boolean handleFile(File file) {
        // Not supported
        return false;
    }

    @Override
    public List<String> getHandledExtensions() {
        // None
        return null;
    }
}
