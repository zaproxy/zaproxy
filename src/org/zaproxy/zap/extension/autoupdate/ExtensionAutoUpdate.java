/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy Project
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
package org.zaproxy.zap.extension.autoupdate;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.text.MessageFormat;
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

import javax.net.ssl.SSLHandshakeException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.commons.httpclient.URI;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
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
import org.zaproxy.zap.extension.log4j.ExtensionLog4j;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionAutoUpdate extends ExtensionAdaptor implements CheckForUpdateCallback, CommandLineListener {
	
	// The short URL means that the number of checkForUpdates can be tracked - see https://bitly.com/u/psiinon
	// Note that URLs must now use https (unless you change the code;)
    
    private static final String ZAP_VERSIONS_REL_XML_SHORT = "https://bit.ly/owaspzap-2-5-0";
    private static final String ZAP_VERSIONS_REL_XML_FULL = "https://raw.githubusercontent.com/zaproxy/zap-admin/master/ZapVersions-2.5.xml";
    
    private static final String ZAP_VERSIONS_DEV_XML_SHORT = "https://bit.ly/owaspzap-dev";
    private static final String ZAP_VERSIONS_DEV_XML_FULL = "https://raw.githubusercontent.com/zaproxy/zap-admin/master/ZapVersions-dev.xml";
    private static final String ZAP_VERSIONS_WEEKLY_XML_SHORT = "https://bit.ly/owaspzap-devw";

	// URLs for use when testing locally ;)
	//private static final String ZAP_VERSIONS_XML_SHORT = "https://localhost:8080/zapcfu/ZapVersions.xml";
	//private static final String ZAP_VERSIONS_XML_FULL = "https://localhost:8080/zapcfu/ZapVersions.xml";

	private static final String VERSION_FILE_NAME = "ZapVersions.xml";

	private ZapMenuItem menuItemCheckUpdate = null;
	private ZapMenuItem menuItemLoadAddOn = null;
    
    private static final Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
	private HttpSender httpSender = null;

    private DownloadManager downloadManager = null;
	private ManageAddOnsDialog addonsDialog = null;
	//private UpdateDialog updateDialog = null;
	private Thread downloadProgressThread = null;
	private Thread remoteCallThread = null; 
	private ScanStatus scanStatus = null;
	private JButton addonsButton = null;
    private JButton outOfDateButton = null;

	private AddOnCollection latestVersionInfo = null;
	private AddOnCollection localVersionInfo = null;
	private AddOnCollection previousVersionInfo = null;

    private AutoUpdateAPI api = null;

    private boolean oldZapAlertAdded = false;
    private boolean noCfuAlertAdded = false;

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
    							ARG_CFU_LIST_IDX};
	private CommandLineArgument[] arguments = new CommandLineArgument[ARG_IDXS.length];

    public ExtensionAutoUpdate() {
        super();
 		initialize();
   }   

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName("ExtensionAutoUpdate");
        this.setOrder(1);	// High order so that cmdline updates are installed asap
        this.downloadManager = new DownloadManager(Model.getSingleton().getOptionsParam().getConnectionParam());
        this.downloadManager.start();
        // Do this before it can get overwritten by the latest one
        this.getPreviousVersionInfo();
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
			menuItemCheckUpdate = new ZapMenuItem("cfu.help.menu.check", 
					KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			menuItemCheckUpdate.setText(Constant.messages.getString("cfu.help.menu.check"));
			menuItemCheckUpdate.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					getAddOnsDialog().setVisible(true);
					getAddOnsDialog().checkForUpdates();
				}

			});
		}
		return menuItemCheckUpdate;
	}

	private ZapMenuItem getMenuItemLoadAddOn() {
		if (menuItemLoadAddOn == null) {
			menuItemLoadAddOn = new ZapMenuItem("cfu.file.menu.loadaddon", 
					KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			menuItemLoadAddOn.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
						File file = null;
						chooser.setFileFilter(new FileFilter() {
						       @Override
						       public boolean accept(File file) {
						            if (file.isDirectory()) {
						                return true;
						            } else if (file.isFile() && file.getName().endsWith(".zap")) {
						                return true;
						            }
						            return false;
						        }
						       @Override
						       public String getDescription() {
						           return Constant.messages.getString("file.format.zap.addon");
						       }
						});
						int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
						if(rc == JFileChooser.APPROVE_OPTION) {
							file = chooser.getSelectedFile();
							if (file == null) {
								return;
							}
							installLocalAddOn(file);
						}
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			});
		}
		return menuItemLoadAddOn;
	}
	
	private void installLocalAddOn(File file) throws Exception {
		if (!AddOn.isAddOn(file)) {
			showWarningMessageInvalidAddOnFile();
			return;
		}

		AddOn ao;
		try {
			ao = new AddOn(file);
		} catch (Exception e) {
			showWarningMessageInvalidAddOnFile();
			return;
		}

		if (!ao.canLoadInCurrentVersion()) {
			showWarningMessageCantLoadAddOn(ao);
			return;
		}

		AddOnDependencyChecker dependencyChecker = new AddOnDependencyChecker(getLocalVersionInfo(), latestVersionInfo == null
				? getLocalVersionInfo()
				: latestVersionInfo);

		boolean update = false;
		AddOnChangesResult result;
		AddOn installedAddOn = getLocalVersionInfo().getAddOn(ao.getId());
		if (installedAddOn != null) {
			if (!ao.isUpdateTo(installedAddOn)) {
				View.getSingleton().showWarningDialog(
						MessageFormat.format(
								Constant.messages.getString("cfu.warn.addOnOlderVersion"),
								installedAddOn.getFileVersion(),
								View.getSingleton().getStatusUI(installedAddOn.getStatus()).toString(),
								ao.getFileVersion(),
								View.getSingleton().getStatusUI(ao.getStatus()).toString()));
				return;
			}

			result = dependencyChecker.calculateUpdateChanges(ao);
			update = true;
		} else {
			result = dependencyChecker.calculateInstallChanges(ao);
		}

		if (result.getOldVersions().isEmpty() && result.getUninstalls().isEmpty()) {
			AddOnRunRequirements reqs = ao.calculateRunRequirements(getLocalVersionInfo().getAddOns());
			if (!reqs.isRunnable()) {
				if (!AddOnRunIssuesUtils.askConfirmationAddOnNotRunnable(
						Constant.messages.getString("cfu.warn.addOnNotRunnable.message"),
						Constant.messages.getString("cfu.warn.addOnNotRunnable.question"),
						getLocalVersionInfo(),
						ao)) {
					return;
				}
			}
			
			installLocalAddOn(ao);
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
		installLocalAddOn(ao);
	}

	private void installLocalAddOn(AddOn ao) {
		File addOnFile;
		try {
			addOnFile = copyAddOnFileToLocalPluginFolder(ao.getFile());
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

	private void showWarningMessageInvalidAddOnFile() {
		View.getSingleton().showWarningDialog(Constant.messages.getString("cfu.warn.invalidAddOn"));
	}

	private void showWarningMessageCantLoadAddOn(AddOn ao) {
		String message = MessageFormat.format(
				Constant.messages.getString("cfu.warn.cantload"),
				ao.getNotBeforeVersion(),
				ao.getNotFromVersion());
		View.getSingleton().showWarningDialog(message);
	}

	private static File copyAddOnFileToLocalPluginFolder(File file) throws IOException {
		if (isFileInLocalPluginFolder(file)) {
			return file;
		}

		File targetFile = new File(Constant.FOLDER_LOCAL_PLUGIN, file.getName());
		if (targetFile.exists()) {
			throw new FileAlreadyExistsException(file.getAbsolutePath(), targetFile.getAbsolutePath(), "");
		}

		FileCopier fileCopier = new FileCopier();
		fileCopier.copy(file, targetFile);

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
		String message = MessageFormat.format(Constant.messages.getString("cfu.warn.addOnAlreadExists"), file, targetFile);
		View.getSingleton().showWarningDialog(message);
	}

	private static void showWarningMessageUnableToCopyAddOnFile() {
		String pathPluginFolder = new File(Constant.FOLDER_LOCAL_PLUGIN).getAbsolutePath();
		String message = MessageFormat.format(Constant.messages.getString("cfu.warn.unableToCopyAddOn"), pathPluginFolder);
		View.getSingleton().showWarningDialog(message);
	}
	

	private synchronized ManageAddOnsDialog getAddOnsDialog() {
		if (addonsDialog == null) {
			addonsDialog = new ManageAddOnsDialog(this, this.getCurrentVersion(), getLocalVersionInfo());
			if (this.previousVersionInfo != null) {
				addonsDialog.setPreviousVersionInfo(this.previousVersionInfo);
			}
			if (this.latestVersionInfo != null) {
				addonsDialog.setLatestVersionInfo(this.latestVersionInfo);
			}
		}
		return addonsDialog;
	}
	
	private void downloadFile (URL url, File targetFile, long size, String hash) {
		if (View.isInitialised()) {
			// Report info to the Output tab
			View.getSingleton().getOutputPanel().append(
					MessageFormat.format(
							Constant.messages.getString("cfu.output.downloading") + "\n", 
							url.toString(),
							targetFile.getAbsolutePath()));
		}
		this.downloadFiles.add(this.downloadManager.downloadFile(url, targetFile, size, hash));
		if (View.isInitialised()) {
			// Means we do have a UI
			if (this.downloadProgressThread != null && ! this.downloadProgressThread.isAlive()) {
				this.downloadProgressThread = null;
			}
			if (this.downloadProgressThread == null) {
				this.downloadProgressThread = new Thread() {
					@Override
					public void run() {
						while (downloadManager.getCurrentDownloadCount() > 0) {
							getScanStatus().setScanCount(downloadManager.getCurrentDownloadCount());
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
				this.downloadProgressThread.start();
			}
		}
	}
	
	public void installNewExtensions() {
    	final OptionsParamCheckForUpdates options = getModel().getOptionsParam().getCheckForUpdatesParam();
		List<Downloader> handledFiles = new ArrayList<>();
		
		for (Downloader dl : downloadFiles) {
			if (dl.getFinished() == null) {
				continue;
			}
			handledFiles.add(dl);
			try {
				if (!dl.isValidated()) {
					logger.debug("Ignoring unvalidated download: " + dl.getUrl());
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
				} else if (AddOn.isAddOn(dl.getTargetFile())) {
					File f = dl.getTargetFile();
					if (! options.getDownloadDirectory().equals(dl.getTargetFile().getParentFile())) {
						// Move the file to the specified directory - we do this after its been downloaded
						// as these directories can be shared, and other ZAP instances could get incomplete
						// add-ons
						try {
							f = new File(options.getDownloadDirectory(), dl.getTargetFile().getName());
							logger.info("Moving downloaded add-on from " + dl.getTargetFile().getAbsolutePath() +
									" to " + f.getAbsolutePath());
							FileUtils.moveFile(dl.getTargetFile(), f);
						} catch (Exception e) {
							if (!f.exists() && dl.getTargetFile().exists()) {
								logger.error("Failed to move downloaded add-on from " + dl.getTargetFile().getAbsolutePath() +
										" to " + f.getAbsolutePath() + " - left at original location", e);
								f = dl.getTargetFile();
							} else {
								logger.error("Failed to move downloaded add-on from " + dl.getTargetFile().getAbsolutePath() +
										" to " + f.getAbsolutePath() + " - skipping", e);
								continue;
							}
						}
					}
					
					AddOn ao = new AddOn(f);
					if (ao.canLoadInCurrentVersion()) {
						install(ao);
					} else {
			    		logger.info("Cant load add-on " + ao.getName() + 
			    				" Not before=" + ao.getNotBeforeVersion() + " Not from=" + ao.getNotFromVersion() + 
			    				" Version=" + Constant.PROGRAM_VERSION);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		for (Downloader dl : handledFiles) {
			// Cant remove in loop above as we're iterating through the list
			this.downloadFiles.remove(dl);
		}
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
	        
			View.getSingleton().addMainToolbarButton(getAddonsButton());

			View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarRightLabel(getScanStatus().getCountLabel());
	    }
	    extensionHook.addCommandLine(getCommandLineArguments());
        this.api = new AutoUpdateAPI(this);
        this.api.addApiOptions(getModel().getOptionsParam().getCheckForUpdatesParam());
        extensionHook.addApiImplementor(this.api);
	}
	
	private ScanStatus getScanStatus() {
		if (scanStatus == null) {
	        scanStatus = new ScanStatus(
					new ImageIcon(
							ExtensionLog4j.class.getResource("/resource/icon/fugue/download.png")),
						Constant.messages.getString("cfu.downloads.icon.title"));
		}
		return scanStatus;
	}
    

	private JButton getAddonsButton() {
		if (addonsButton == null) {
			addonsButton = new JButton();
			addonsButton.setIcon(new ImageIcon(ExtensionAutoUpdate.class.getResource("/resource/icon/fugue/block.png")));
			addonsButton.setToolTipText(Constant.messages.getString("cfu.button.addons.browse"));
			addonsButton.setEnabled(true);
			addonsButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getAddOnsDialog().setVisible(true);
				}
			});

		}
		return this.addonsButton;
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
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	@Override
	public void destroy() {
		this.downloadManager.shutdown(true);
	}
	
    private HttpSender getHttpSender() {
        if (httpSender == null) {
            httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true, 
            		HttpSender.CHECK_FOR_UPDATES_INITIATOR);
        }
        return httpSender;
    }
    
    private int dayDiff(Date d1, Date d2) {
    	long diff = d1.getTime() - d2.getTime();
    	return (int) (diff / (1000 * 60 * 60 * 24));
    }
    
    public void alertIfNewVersions() {
    	// Kicks off a thread and pops up a window if there are new versions.
    	// Depending on the options the user has chosen.
    	// Only expect this to be called on startup and in desktop mode
    	
    	final OptionsParamCheckForUpdates options = getModel().getOptionsParam().getCheckForUpdatesParam();
    	
		if (View.isInitialised()) {
			if (options.isCheckOnStartUnset()) {
				// First time in
                int result = getView().showConfirmDialog(
                		Constant.messages.getString("cfu.confirm.startCheck"));
                if (result == JOptionPane.OK_OPTION) {
                	options.setCheckOnStart(true);
                	options.setCheckAddonUpdates(true);
                	options.setDownloadNewRelease(true);
                } else {
                	options.setCheckOnStart(false);
                }
                // Save
			    try {
			    	this.getModel().getOptionsParam().getConfig().save();
	            } catch (ConfigurationException ce) {
	            	logger.error(ce.getMessage(), ce);
	                getView().showWarningDialog(
	                		Constant.messages.getString("cfu.confirm.error"));
	                return;
	            }
			}
			if (! options.isCheckOnStart()) {
				alertIfOutOfDate(false);
				return;
			}
		}
    	
		if (! options.checkOnStart()) {
			// Top level option not set, dont do anything, unless already downloaded last release
			if (View.isInitialised() && this.getPreviousVersionInfo() != null) {
				ZapRelease rel = this.getPreviousVersionInfo().getZapRelease();
				if (rel != null && rel.isNewerThan(this.getCurrentVersion())) {
					File f = new File(Constant.FOLDER_LOCAL_PLUGIN, rel.getFileName());
					if (f.exists() && f.length() >= rel.getSize()) {
						// Already downloaded, prompt to install and exit
						this.promptToLaunchReleaseAndClose(rel.getVersion(), f);
					}
				}
			}
			return;
		}
		// Handle the response in a callback
		this.getLatestVersionInfo(this);
    }
    
    private void warnIfOutOfDate() {
    	final OptionsParamCheckForUpdates options = getModel().getOptionsParam().getCheckForUpdatesParam();
		Date today = new Date();
		Date releaseCreated = Constant.getReleaseCreateDate();
		if (releaseCreated != null) {
			// Should only be null for dev builds
			if (dayDiff(today, releaseCreated) > 365) {
				// Oh no, its more than a year old!
				if (ZAP.getProcessType().equals(ZAP.ProcessType.cmdline)) {
					CommandLine.error("This ZAP installation is over a year old - its probably very out of date");
				} else {
					logger.warn("This ZAP installation is over a year old - its probably very out of date");
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
				CommandLine.error("No check for updates for over 3 month - add-ons may well be out of date");
			} else {
				logger.warn("No check for updates for over 3 month - add-ons may well be out of date");
			}
		}
    }
    

    
    private void alertIfOutOfDate(boolean alwaysPrompt) {
    	final OptionsParamCheckForUpdates options = getModel().getOptionsParam().getCheckForUpdatesParam();
		Date today = new Date();
		Date releaseCreated = Constant.getReleaseCreateDate();
		Date lastInstallWarning = options.getDayLastInstallWarned();
        int result = -1;
        logger.debug("Install created " + releaseCreated);
		if (releaseCreated != null) {
			// Should only be null for dev builds
			int daysOld = dayDiff(today, releaseCreated);
	        logger.debug("Install is " + daysOld + " days old");
			if (daysOld > 365) {
				// Oh no, its more than a year old!
				boolean setCfuOnStart = false;
				
				if (alwaysPrompt || lastInstallWarning == null || dayDiff(today, lastInstallWarning) > 30) {
					JCheckBox cfuOnStart = new JCheckBox(Constant.messages.getString("cfu.label.cfuonstart"));
					cfuOnStart.setSelected(true);
					String msg = Constant.messages.getString("cfu.label.oldzap"); 
					
					result = View.getSingleton().showYesNoDialog(
							View.getSingleton().getMainFrame(), new Object[]{msg, cfuOnStart}						);
					setCfuOnStart = cfuOnStart.isSelected();
					
				}
				options.setDayLastInstallWarned();
				
                if (result == JOptionPane.OK_OPTION) {
    				if (setCfuOnStart) {
    					options.setCheckOnStart(true);
    				}
					getAddOnsDialog().setVisible(true);
					getAddOnsDialog().checkForUpdates();
					
                } else if (!oldZapAlertAdded){
                	JButton button = new JButton(Constant.messages.getString("cfu.label.outofdatezap"));
                	button.setIcon(new ImageIcon(
                			ExtensionAutoUpdate.class.getResource("/resource/icon/16/050.png"))); // Alert triangle
                	button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							alertIfOutOfDate(true);
						}
                	});
                	
        			View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarLeftComponent(button );
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
			
			if (alwaysPrompt || lastUpdateWarning == null || dayDiff(today, lastUpdateWarning) > 30) {
				JCheckBox cfuOnStart = new JCheckBox(Constant.messages.getString("cfu.label.cfuonstart"));
				cfuOnStart.setSelected(true);
				String msg = Constant.messages.getString("cfu.label.norecentcfu");
				
				result = View.getSingleton().showYesNoDialog(
						View.getSingleton().getMainFrame(), new Object[]{msg, cfuOnStart});
				setCfuOnStart = cfuOnStart.isSelected();
				
			}
			options.setDayLastUpdateWarned();
			
            if (result == JOptionPane.OK_OPTION) {
				if (setCfuOnStart) {
					options.setCheckOnStart(true);
				}
				getAddOnsDialog().setVisible(true);
				getAddOnsDialog().checkForUpdates();
				if (noCfuAlertAdded) {
	    			View.getSingleton().getMainFrame().getMainFooterPanel().
    					removeFooterToolbarLeftComponent(getOutOfDateButton());
				}
				
            } else if (!noCfuAlertAdded){
    			View.getSingleton().getMainFrame().getMainFooterPanel().
    				addFooterToolbarLeftComponent(getOutOfDateButton());
    			noCfuAlertAdded = true;
            }
		}
    }
    
    private JButton getOutOfDateButton() {
    	if (outOfDateButton == null) {
    		outOfDateButton = new JButton(Constant.messages.getString("cfu.label.outofdateaddons"));
    		outOfDateButton.setIcon(new ImageIcon(
	    			ExtensionAutoUpdate.class.getResource("/resource/icon/16/050.png"))); // Alert triangle
    		outOfDateButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					alertIfOutOfDate(true);
				}
	    	});
		}
    	return outOfDateButton;	
    }
    
    private AddOnCollection getLocalVersionInfo () {
    	if (localVersionInfo == null) {
    		localVersionInfo = ExtensionFactory.getAddOnLoader().getAddOnCollection(); 
    	}
    	return localVersionInfo;
    }

    private ZapXmlConfiguration getRemoteConfigurationUrl(String url) throws 
    		IOException, ConfigurationException, InvalidCfuUrlException {
        HttpMessage msg = new HttpMessage(new URI(url, true), 
        		Model.getSingleton().getOptionsParam().getConnectionParam());
        getHttpSender().sendAndReceive(msg,true);
        if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
            throw new IOException();
        }
        if (! msg.getRequestHeader().isSecure()) {
        	// Only access the cfu page over https
            throw new InvalidCfuUrlException(msg.getRequestHeader().getURI().toString());
        }
        
    	ZapXmlConfiguration config = new ZapXmlConfiguration();
    	config.setDelimiterParsingDisabled(true);
    	config.load(new StringReader(msg.getResponseBody().toString()));

        // Save version file so we can report new addons next time
		File f = new File(Constant.FOLDER_LOCAL_PLUGIN, VERSION_FILE_NAME);
    	FileWriter out = null;
	    try {
	    	out = new FileWriter(f);
	    	out.write(msg.getResponseBody().toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
	    } finally {
	    	try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// Ignore
			}
		}

    	
    	return config;
    }

    protected String getLatestVersionNumber() {
    	if (this.getLatestVersionInfo() == null ||
    			this.getLatestVersionInfo().getZapRelease() == null) {
    		return null;
    	}
    	return this.getLatestVersionInfo().getZapRelease().getVersion();
    }
    
    protected boolean isLatestVersion() {
    	if (this.getLatestVersionInfo() == null ||
    			this.getLatestVersionInfo().getZapRelease() == null) {
    		return true;
    	}
		return  ! this.getLatestVersionInfo().getZapRelease().isNewerThan(this.getCurrentVersion());
    }
    
    protected boolean downloadLatestRelease() {
    	if (Constant.isKali()) {
    		if (View.isInitialised()) {
	    		// Just tell the user to use one of the Kali options
	    		View.getSingleton().showMessageDialog(this.getAddOnsDialog(), Constant.messages.getString("cfu.kali.options"));
    		}
    		return false;
    	}
    	if (this.getLatestVersionInfo() == null ||
    			this.getLatestVersionInfo().getZapRelease() == null) {
    		return false;
    	}
    	ZapRelease latestRelease = this.getLatestVersionInfo().getZapRelease();
		if (latestRelease.isNewerThan(this.getCurrentVersion())) {
			File f = new File(Constant.FOLDER_LOCAL_PLUGIN, latestRelease.getFileName());
			downloadFile(latestRelease.getUrl(), f, latestRelease.getSize(), latestRelease.getHash());
			return true;
		}
		return false;
    }
    
	private AddOnCollection getPreviousVersionInfo() {
		if (this.previousVersionInfo == null) {
			File f = new File(Constant.FOLDER_LOCAL_PLUGIN, VERSION_FILE_NAME);
			if (f.exists()) {
				try {
					this.previousVersionInfo = new AddOnCollection(new ZapXmlConfiguration(f), this.getPlatform());
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

    
    private List<AddOn> getUpdatedAddOns() {
    	return getLocalVersionInfo().getUpdatedAddOns(this.getLatestVersionInfo());
    }
    
    private List<AddOn> getNewAddOns() {
    	if (this.getPreviousVersionInfo() != null) {
    		return this.getPreviousVersionInfo().getNewAddOns(this.getLatestVersionInfo());
    	}
    	return getLocalVersionInfo().getNewAddOns(this.getLatestVersionInfo());
    }

    protected AddOnCollection getLatestVersionInfo () {
    	return getLatestVersionInfo(null);
    }
    
    protected AddOnCollection getLatestVersionInfo (final CheckForUpdateCallback callback) {
    	if (latestVersionInfo == null) {
    		
    		if (this.remoteCallThread == null || !this.remoteCallThread.isAlive()) {
    			this.remoteCallThread = new Thread() {
    			
	    			@Override
	    			public void run() {
	    				// Using a thread as the first call could timeout
	    				// and we dont want the ui to hang in the meantime
	    				this.setName("ZAP-cfu");
						String shortUrl;
						String longUrl;
						if (Constant.isDevBuild()) {
							shortUrl = ZAP_VERSIONS_DEV_XML_SHORT;
							longUrl = ZAP_VERSIONS_DEV_XML_FULL;
						} else if (Constant.isDailyBuild()) {
							shortUrl = ZAP_VERSIONS_WEEKLY_XML_SHORT;
							longUrl = ZAP_VERSIONS_DEV_XML_FULL;
						} else {
							shortUrl = ZAP_VERSIONS_REL_XML_SHORT;
							longUrl = ZAP_VERSIONS_REL_XML_FULL;
						}
						logger.debug("Getting latest version info from " + shortUrl);
			    		try {
							latestVersionInfo = new AddOnCollection(getRemoteConfigurationUrl(shortUrl), getPlatform(), false);
						} catch (Exception e1) {
							logger.debug("Failed to access " + shortUrl, e1);
							logger.debug("Getting latest version info from " + longUrl);
				    		try {
				    			latestVersionInfo = new AddOnCollection(getRemoteConfigurationUrl(longUrl), getPlatform(), false);
				    		} catch (SSLHandshakeException e2) {
					    		if (callback != null) {
					    			callback.insecureUrl(longUrl, e2);
					    		}
							} catch (InvalidCfuUrlException e2) {
					    		if (callback != null) {
					    			callback.insecureUrl(longUrl, e2);
					    		}
							} catch (Exception e2) {
								logger.debug("Failed to access " + longUrl, e2);
							}
						}
			    		if (callback != null && latestVersionInfo != null) {
							logger.debug("Calling callback with  " + latestVersionInfo);
			    			callback.gotLatestData(latestVersionInfo);
			    		}
						logger.debug("Done");
	    			}
    			};
    			this.remoteCallThread.start();
    		}
    		if (callback == null) {
    			// Synchronous, but include a 30 sec max anyway
    			int i=0;
				while (latestVersionInfo == null && this.remoteCallThread.isAlive() && i < 30) {
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
		} else  {
			return Platform.mac;
		}
	}

	protected void promptToLaunchReleaseAndClose(String version, File f) {
		int ans = View.getSingleton().showConfirmDialog(
				MessageFormat.format(
						Constant.messages.getString("cfu.confirm.launch"), 
						version,
						f.getAbsolutePath()));
		if (ans == JOptionPane.OK_OPTION) {
			Control.getSingleton().exit(false, f);		
		}
	}
	
	private void install(AddOn ao) {
		if (! ao.canLoadInCurrentVersion()) {
    		throw new IllegalArgumentException("Cant load add-on " + ao.getName() + 
    				" Not before=" + ao.getNotBeforeVersion() + " Not from=" + ao.getNotFromVersion() + 
    				" Version=" + Constant.PROGRAM_VERSION);
		}
		
		AddOn installedAddOn = this.getLocalVersionInfo().getAddOn(ao.getId());
		if (installedAddOn != null) {
			if ( ! uninstallAddOn(null, installedAddOn, true)) {
                // Cant uninstall the old version, so dont try to install the new one
	            return;
			}
		}
		logger.info("Installing new addon " + ao.getId() + " v" + ao.getFileVersion());
		if (View.isInitialised()) {
			// Report info to the Output tab
			View.getSingleton().getOutputPanel().append(
					MessageFormat.format(
							Constant.messages.getString("cfu.output.installing") + "\n", 
							ao.getName(),
							Integer.valueOf(ao.getFileVersion())));
		}

		ExtensionFactory.getAddOnLoader().addAddon(ao);

        if (latestVersionInfo != null) {
            AddOn addOn = latestVersionInfo.getAddOn(ao.getId());
            if (addOn != null && AddOn.InstallationStatus.DOWNLOADING == addOn.getInstallationStatus()) {
                addOn.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);
            }
        }

        if (addonsDialog != null) {
            addonsDialog.notifyAddOnInstalled(ao);
        }
	}
	
    private boolean uninstall(AddOn addOn, boolean upgrading, AddOnUninstallationProgressCallback callback) {
        logger.debug("Trying to uninstall addon " + addOn.getId() + " v" + addOn.getFileVersion());

        boolean removedDynamically = ExtensionFactory.getAddOnLoader().removeAddOn(addOn, upgrading, callback);
        if (removedDynamically) {
            logger.debug("Uninstalled add-on " + addOn.getName());

            if (latestVersionInfo != null) {
                AddOn availableAddOn = latestVersionInfo.getAddOn(addOn.getId());
                if (availableAddOn != null && availableAddOn.getInstallationStatus() != AddOn.InstallationStatus.AVAILABLE) {
                    availableAddOn.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
                }
            }
        } else {
            logger.debug("Failed to uninstall add-on " + addOn.getId() + " v" + addOn.getFileVersion());
        }
        return removedDynamically;
    }

	@Override
	public void insecureUrl(String url, Exception cause) {
		logger.error("Failed to get check for updates on " + url, cause);
    	if (View.isInitialised()) {
    		View.getSingleton().showWarningDialog(Constant.messages.getString("cfu.warn.badurl"));
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
			getAddOnsDialog();
		}
		try {
			ZapRelease rel = aoc.getZapRelease();

	    	OptionsParamCheckForUpdates options = getModel().getOptionsParam().getCheckForUpdatesParam();

	    	if (rel.isNewerThan(getCurrentVersion())) {
				logger.debug("There is a newer release: " + rel.getVersion());
				// New ZAP release
				if (Constant.isKali()) {
		    		// Kali has its own package management system
					if (View.isInitialised()) {
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
				if (newAddOns.size() > 0) {
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

        logger.debug("There is/are " + updates.size() + " newer addons");
        AddOnDependencyChecker addOnDependencyChecker = new AddOnDependencyChecker(localVersionInfo, aoc);
        Set<AddOn> addOns = new HashSet<>(updates);
        AddOnDependencyChecker.AddOnChangesResult result = addOnDependencyChecker.calculateUpdateChanges(addOns);

        if (!result.getUninstalls().isEmpty() || result.isNewerJavaVersionRequired()) {
            if (options.isCheckAddonUpdates()) {
                if (addonsDialog != null) {
                    // Just show the dialog
                    getAddOnsDialog().setVisible(true);
                    return false;
                }
                logger.info("Updates not installed some add-ons would be uninstalled or require newer java version: "
                        + result.getUninstalls());
            }
            return true;
        }

        if (options.isInstallAddonUpdates()) {
            logger.debug("Auto-downloading addons");
            processAddOnChanges(null, result);
            
            return false;
        }

        if (options.isInstallScannerRules()) {
            for (Iterator<AddOn> it = addOns.iterator(); it.hasNext();) {
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
            Window caller,
            Collection<AddOn> addOns,
            Set<Extension> extensions,
            boolean updating) {
        Set<AddOn> allAddOns = new HashSet<>(addOns);
        addDependents(allAddOns);

        String baseMessagePrefix = updating ? "cfu.update." : "cfu.uninstall.";

        String unsavedResources = getExtensionsUnsavedResources(addOns, extensions);
        String activeActions = getExtensionsActiveActions(addOns, extensions);

        String message = null;
        if (!unsavedResources.isEmpty()) {
            if (activeActions.isEmpty()) {
                message = MessageFormat.format(
                        Constant.messages.getString(baseMessagePrefix + "message.resourcesNotSaved"),
                        unsavedResources);
            } else {
                message = MessageFormat.format(
                        Constant.messages.getString(baseMessagePrefix + "message.resourcesNotSavedAndActiveActions"),
                        unsavedResources,
                        activeActions);
            }
        } else if (!activeActions.isEmpty()) {
            message = MessageFormat.format(
                    Constant.messages.getString(baseMessagePrefix + "message.activeActions"),
                    activeActions);
        }

        if (message != null
                && JOptionPane.showConfirmDialog(
                        getWindowParent(caller),
                        message,
                        Constant.PROGRAM_NAME,
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
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
     * Returns all unsaved resources of the given {@code addOns} and {@code extensions} wrapped in {@code <li>} elements or an
     * empty {@code String} if there are no unsaved resources.
     *
     * @param addOns the add-ons that will be queried for unsaved resources
     * @param extensions the extensions that will be queried for unsaved resources
     * @return a {@code String} containing all unsaved resources or empty {@code String} if none
     * @since 2.4.0
     * @see Extension#getUnsavedResources()
     */
    private static String getExtensionsUnsavedResources(Collection<AddOn> addOns, Set<Extension> extensions) {
        List<String> unsavedResources = new ArrayList<>();
        for (AddOn addOn : addOns) {
            for (Extension extension : addOn.getLoadedExtensions()) {
                List<String> resources = extension.getUnsavedResources();
                if (resources != null) {
                    unsavedResources.addAll(resources);
                }
            }
        }
        for (Extension extension : extensions) {
            List<String> resources = extension.getUnsavedResources();
            if (resources != null) {
                unsavedResources.addAll(resources);
            }
        }
        return wrapEntriesInLiTags(unsavedResources);
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
     * Returns all active actions of the given {@code addOns} and {@code extensions} wrapped in {@code <li>} elements or an
     * empty {@code String} if there are no active actions.
     *
     * @param addOns the add-ons that will be queried for active actions
     * @param extensions the extensions that will be queried for active actions
     * @return a {@code String} containing all active actions or empty {@code String} if none
     * @since 2.4.0
     * @see Extension#getActiveActions()
     */
    private static String getExtensionsActiveActions(Collection<AddOn> addOns, Set<Extension> extensions) {
        List<String> activeActions = new ArrayList<>();
        for (AddOn addOn : addOns) {
            for (Extension extension : addOn.getLoadedExtensions()) {
                List<String> actions = extension.getActiveActions();
                if (actions != null) {
                    activeActions.addAll(actions);
                }
            }
        }
        for (Extension extension : extensions) {
            List<String> resources = extension.getActiveActions();
            if (resources != null) {
                activeActions.addAll(resources);
            }
        }
        return wrapEntriesInLiTags(activeActions);
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
            return uninstallAddOnsWithView(caller, addOns, updates, new HashSet<AddOn>());
        }

        final Set<AddOn> failedUninstallations = new HashSet<>();
        for (AddOn addOn : addOns) {
            if (!uninstall(addOn, false, null)) {
                failedUninstallations.add(addOn);
            }
        }

        if (!failedUninstallations.isEmpty()) {
            logger.warn("It's recommended to restart ZAP. Not all add-ons were successfully uninstalled: "
                    + failedUninstallations);
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
                EventQueue.invokeAndWait(new Runnable() {
                    
                    @Override
                    public void run() {
                        uninstallAddOnsWithView(caller, addOns, updates, failedUninstallations);
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                logger.error("Failed to uninstall add-ons:", e);
                return false;
            }
            return failedUninstallations.isEmpty();
        }

        final Window parent = getWindowParent(caller);

        final UninstallationProgressDialogue waitDialogue = new UninstallationProgressDialogue(parent, addOns);
        waitDialogue.addAddOnUninstallListener(new AddOnUninstallListener() {

            @Override
            public void uninstallingAddOn(AddOn addOn, boolean updating) {
                if (updating) {
                    String message = MessageFormat.format(
                            Constant.messages.getString("cfu.output.replacing") + "\n",
                            addOn.getName(),
                            Integer.valueOf(addOn.getFileVersion()));
                    getView().getOutputPanel().append(message);
                }
            }

            @Override
            public void addOnUninstalled(AddOn addOn, boolean update, boolean uninstalled) {
                if (uninstalled) {
                    if (!update && addonsDialog != null) {
                        addonsDialog.notifyAddOnUninstalled(addOn);
                    }

                    String message = MessageFormat.format(
                            Constant.messages.getString("cfu.output.uninstalled") + "\n",
                            addOn.getName(),
                            Integer.valueOf(addOn.getFileVersion()));
                    getView().getOutputPanel().append(message);
                } else {
                    if (addonsDialog != null) {
                        addonsDialog.notifyAddOnFailedUninstallation(addOn);
                    }

                    String message;
                    if (update) {
                        message = MessageFormat.format(
                                Constant.messages.getString("cfu.output.replace.failed") + "\n",
                                addOn.getName(),
                                Integer.valueOf(addOn.getFileVersion()));
                    } else {
                        message = MessageFormat.format(
                                Constant.messages.getString("cfu.output.uninstall.failed") + "\n",
                                addOn.getName(),
                                Integer.valueOf(addOn.getFileVersion()));
                    }
                    getView().getOutputPanel().append(message);
                }
            }

        });

        SwingWorker<Void, UninstallationProgressEvent> a = new SwingWorker<Void, UninstallationProgressEvent>() {

            @Override
            protected void process(List<UninstallationProgressEvent> events) {
                waitDialogue.update(events);
            }

            @Override
            protected Void doInBackground() {
                UninstallationProgressHandler progressHandler = new UninstallationProgressHandler() {

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
                    logger.warn("Not all add-ons were successfully uninstalled: " + failedUninstallations);
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

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_CFU_INSTALL_IDX] = new CommandLineArgument("-addoninstall", 1, null, "", 
        		"-addoninstall <addon>    " + Constant.messages.getString("cfu.cmdline.install.help"));
        arguments[ARG_CFU_INSTALL_ALL_IDX] = new CommandLineArgument("-addoninstallall", 0, null, "", 
        		"-addoninstallall         " + Constant.messages.getString("cfu.cmdline.installall.help"));
        arguments[ARG_CFU_UNINSTALL_IDX] = new CommandLineArgument("-addonuninstall", 1, null, "", 
        		"-addonuninstall <addon>  " + Constant.messages.getString("cfu.cmdline.uninstall.help"));
        arguments[ARG_CFU_UPDATE_IDX] = new CommandLineArgument("-addonupdate", 0, null, "", 
        		"-addonupdate             " + Constant.messages.getString("cfu.cmdline.update.help"));
        arguments[ARG_CFU_LIST_IDX] = new CommandLineArgument("-addonlist", 0, null, "", 
        		"-addonlist               " + Constant.messages.getString("cfu.cmdline.list.help"));
        return arguments;
    }


	@Override
	public void execute(CommandLineArgument[] args) {
        if (arguments[ARG_CFU_UPDATE_IDX].isEnabled()) {
        	AddOnCollection aoc = getLatestVersionInfo();
        	// Create some temporary options with the settings we need
        	OptionsParamCheckForUpdates options = new OptionsParamCheckForUpdates();
        	options.load(new XMLPropertiesConfiguration());
        	options.setCheckOnStart(true);
        	options.setCheckAddonUpdates(true);
        	options.setInstallAddonUpdates(true);
			checkForAddOnUpdates(aoc, options);

			waitAndInstallDownloads();
    		CommandLine.info(Constant.messages.getString("cfu.cmdline.updated"));
        }
        if (arguments[ARG_CFU_INSTALL_ALL_IDX].isEnabled()) {
        	AddOnCollection aoc = getLatestVersionInfo();
        	if (aoc == null) {
        		CommandLine.error(Constant.messages.getString("cfu.cmdline.nocfu"));
        	} else {
        		AddOnDependencyChecker addOnDependencyChecker = new AddOnDependencyChecker(getLocalVersionInfo(), aoc);
        		AddOnDependencyChecker.AddOnChangesResult result;
        		AddOnDependencyChecker.AddOnChangesResult allResults = null;
                Set<AddOn> allAddOns = new HashSet<>();

        		for (AddOn ao : aoc.getAddOns()) {
        			if (ao.getId().equals("coreLang") && (Constant.isDevBuild() || Constant.isDailyBuild())) {
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
                    CommandLine.info(MessageFormat.format(
                            Constant.messages.getString("cfu.cmdline.addonurl"),
                            addOn.getUrl()));
                }
                
                processAddOnChanges(null, allResults);

                waitAndInstallDownloads();
        	}
        }
        if (arguments[ARG_CFU_INSTALL_IDX].isEnabled()) {
        	Vector<String> params = arguments[ARG_CFU_INSTALL_IDX].getArguments();
			AddOnCollection aoc = getLatestVersionInfo();
			if (aoc == null) {
				CommandLine.error(Constant.messages.getString("cfu.cmdline.nocfu"));
			} else {
				for (String aoName : params) {
            		AddOn ao = aoc.getAddOn(aoName);
            		if (ao == null) {
                		CommandLine.error(MessageFormat.format(
                                Constant.messages.getString("cfu.cmdline.noaddon"), aoName));
            			continue;
            		}
            		AddOnDependencyChecker addOnDependencyChecker = new AddOnDependencyChecker(getLocalVersionInfo(), aoc);
            		AddOnDependencyChecker.AddOnChangesResult result;
            		// Check to see if its already installed
            		AddOn iao = getLocalVersionInfo().getAddOn(aoName);
            		if (iao != null) {
            			if (!ao.isUpdateTo(iao)) {
                    		CommandLine.info(MessageFormat.format(
                                    Constant.messages.getString("cfu.cmdline.addoninst"),
                                    iao.getFile().getAbsolutePath()));
                			continue;
            			}

                        result = addOnDependencyChecker.calculateUpdateChanges(ao);
                    } else {
                        result = addOnDependencyChecker.calculateInstallChanges(ao);
            		}
            		
                    if (!result.getUninstalls().isEmpty()) {
                        CommandLine.info(
                                MessageFormat.format(
                                        Constant.messages.getString("cfu.cmdline.addoninst.uninstalls.required"),
                                        result.getUninstalls()));
                        continue;
                    }

                    Set<AddOn> allAddOns = new HashSet<>();
                    allAddOns.addAll(result.getInstalls());
                    allAddOns.addAll(result.getNewVersions());
                    for (AddOn addOn : allAddOns) {
                        CommandLine.info(MessageFormat.format(
                                Constant.messages.getString("cfu.cmdline.addonurl"),
                                addOn.getUrl()));
                    }

                    processAddOnChanges(null, result);
            	}
                waitAndInstallDownloads();
            }
        }
        if (arguments[ARG_CFU_UNINSTALL_IDX].isEnabled()) {
        	Vector<String> params = arguments[ARG_CFU_UNINSTALL_IDX].getArguments();
			AddOnCollection aoc = this.getLocalVersionInfo();
			if (aoc == null) {
				CommandLine.error(Constant.messages.getString("cfu.cmdline.nocfu"));
			} else {
				for (String aoName : params) {
            		AddOn ao = aoc.getAddOn(aoName);
            		if (ao == null) {
                		CommandLine.error(MessageFormat.format(
                                Constant.messages.getString("cfu.cmdline.noaddon"), aoName));
            			continue;
            		}
            		AddOnDependencyChecker addOnDependencyChecker = new AddOnDependencyChecker(getLocalVersionInfo(), aoc);
            		
            		Set<AddOn> addonSet = new HashSet<AddOn>();
            		addonSet.add(ao);
            		UninstallationResult result = addOnDependencyChecker.calculateUninstallChanges(addonSet);
            		
            		// Check to see if other add-ons depend on it
            		if (result.getUninstallations().size() > 1) {
            			// Will always report this add-on as needing to be uninstalled
                    	// Remove the specified add-on for the error message
                    	result.getUninstallations().remove(ao);
                        CommandLine.info(
                                MessageFormat.format(
                                        Constant.messages.getString("cfu.cmdline.addonuninst.uninstalls.required"),
                                        result.getUninstallations()));
            			continue;
            		}

            		if (this.uninstallAddOn(null, ao, false)) {
                		CommandLine.info(MessageFormat.format(
                                Constant.messages.getString("cfu.cmdline.uninstallok"), aoName));

            		} else {
                		CommandLine.error(MessageFormat.format(
                                Constant.messages.getString("cfu.cmdline.uninstallfail"), aoName));

            		}
            	}
            }
        }
        if (arguments[ARG_CFU_LIST_IDX].isEnabled()) {
			AddOnCollection aoc = this.getLocalVersionInfo();
			List<AddOn> aolist = new ArrayList<AddOn>(aoc.getAddOns());
	        Collections.sort(aolist, new Comparator<AddOn>(){
				@Override
				public int compare(AddOn ao1, AddOn ao2) {
					return ao1.getName().compareTo(ao2.getName());
				}});

			for (AddOn addon : aolist) {
				CommandLine.info(addon.getName() + "\t" + addon.getId() + 
						"\tv" + addon.getFileVersion() + "\t" + addon.getStatus().name() +
						"\t" + addon.getDescription());
			}
        }
	}
	
	private void waitAndInstallDownloads() {
		while (downloadManager.getCurrentDownloadCount() > 0) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		for (Downloader download : downloadManager.getProgress()) {
			if (download.isValidated()) {
				CommandLine.info(MessageFormat.format(
						Constant.messages.getString("cfu.cmdline.addondown"),
						download.getTargetFile().getAbsolutePath()));
			} else {
				CommandLine.error(MessageFormat.format(
						Constant.messages.getString("cfu.cmdline.addondown.failed"),
						download.getTargetFile().getName()));
			}
		}
		if (getView() == null) {
			installNewExtensions();
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
