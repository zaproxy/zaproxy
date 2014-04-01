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
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.FileCopier;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.control.AddOnCollection.Platform;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.control.ZapRelease;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.log4j.ExtensionLog4j;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionAutoUpdate extends ExtensionAdaptor implements CheckForUpdateCallback {
	
	// The short URL means that the number of checkForUpdates can be tracked - see http://goo.gl/info/V4aWX
    private static final String ZAP_VERSIONS_XML_SHORT = "http://goo.gl/V4aWX";
    private static final String ZAP_VERSIONS_XML_FULL = "http://zaproxy.googlecode.com/svn/wiki/ZapVersions.xml";

	// URLs for use when testing locally ;)
	//private static final String ZAP_VERSIONS_XML_SHORT = "http://localhost:8080/zapcfu/ZapVersions.xml";
    //private static final String ZAP_VERSIONS_XML_FULL = "http://localhost:8080/zapcfu/ZapVersions.xml";

	private static final String VERSION_FILE_NAME = "ZapVersions.xml";

	private ZapMenuItem menuItemCheckUpdate = null;
	private ZapMenuItem menuItemLoadAddOn = null;
    
    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
	private HttpSender httpSender = null;

    private DownloadManager downloadManager = null;
	private ManageAddOnsDialog addonsDialog = null;
	//private UpdateDialog updateDialog = null;
	private Thread downloadProgressThread = null;
	private Thread remoteCallThread = null; 
	private ScanStatus scanStatus = null;
	private JButton addonsButton = null;
	
	private AddOnCollection latestVersionInfo = null;
	private AddOnCollection localVersionInfo = null;
	private AddOnCollection previousVersionInfo = null;

    private AutoUpdateAPI api = null;

    // Files currently being downloaded
	private List<Downloader> downloadFiles = new ArrayList<>();

    /**
     * 
     */
    public ExtensionAutoUpdate() {
        super();
 		initialize();
   }   

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName("ExtensionAutoUpdate");
        this.setOrder(40);
        this.downloadManager = new DownloadManager(Model.getSingleton().getOptionsParam().getConnectionParam());
        this.downloadManager.start();
        // Do this before it can get overwritten by the latest one
        this.getPreviousVersionInfo();
	}

	/**
	 * This method initializes menuItemEncoder	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private ZapMenuItem getMenuItemCheckUpdate() {
		if (menuItemCheckUpdate == null) {
			menuItemCheckUpdate = new ZapMenuItem("cfu.help.menu.check", 
					KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK, false));
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
					KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK, false));
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

		AddOn ao = new AddOn(file);
		if (!ao.canLoad()) {
			showWarningMessageCantLoadAddOn(ao);
			return;
		}

		File addOnFile = file;
		try {
			addOnFile = copyAddOnFileToLocalPluginFolder(file);
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

		if (install(ao)) {
			// Refresh lists
			reloadAddOnData();
		}
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

	private File copyAddOnFileToLocalPluginFolder(File file) throws IOException {
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

	private boolean isFileInLocalPluginFolder(File file) {
		File fileLocalPluginFolder = new File(Constant.FOLDER_LOCAL_PLUGIN, file.getName());
		if (fileLocalPluginFolder.getAbsolutePath().equals(file.getAbsolutePath())) {
			return true;
		}
		return false;
	}

	private void showWarningMessageAddOnFileAlreadyExists(String file, String targetFile) {
		String message = MessageFormat.format(Constant.messages.getString("cfu.warn.addOnAlreadExists"), file, targetFile);
		View.getSingleton().showWarningDialog(message);
	}

	private void showWarningMessageUnableToCopyAddOnFile() {
		String pathPluginFolder = new File(Constant.FOLDER_LOCAL_PLUGIN).getAbsolutePath();
		String message = MessageFormat.format(Constant.messages.getString("cfu.warn.unableToCopyAddOn"), pathPluginFolder);
		View.getSingleton().showWarningDialog(message);
	}

	private List <AddOnWrapper> getInstalledAddOns() {
		List <AddOnWrapper> list = new ArrayList <>();
		
		for (AddOn ao : this.getLocalVersionInfo().getAddOns()) {
			list.add(new AddOnWrapper(ao, AddOnWrapper.Status.installed));
		}
		
		return list;
	}
	

	private synchronized ManageAddOnsDialog getAddOnsDialog() {
		if (addonsDialog == null) {
			addonsDialog = new ManageAddOnsDialog(this, this.getCurrentVersion(), this.getInstalledAddOns());
			if (this.previousVersionInfo != null) {
				addonsDialog.setPreviousVersionInfo(this.previousVersionInfo);
			}
			if (this.latestVersionInfo != null) {
				addonsDialog.setLatestVersionInfo(this.latestVersionInfo);
			}
		}
		return addonsDialog;
	}
	
	public void downloadFile (URL url, File targetFile, long size) {
		if (View.isInitialised()) {
			// Report info to the Output tab
			View.getSingleton().getOutputPanel().append(
					MessageFormat.format(
							Constant.messages.getString("cfu.output.downloading") + "\n", 
							url.toString(),
							targetFile.getAbsolutePath()));
		}
		this.downloadFiles.add(this.downloadManager.downloadFile(url, targetFile, size));
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
		List<Downloader> handledFiles = new ArrayList<>();
		
		for (Downloader dl : downloadFiles) {
			if (dl.getFinished() == null) {
				continue;
			}
			handledFiles.add(dl);
			try {
				if (AddOn.isAddOn(dl.getTargetFile())) {
					AddOn ao = new AddOn(dl.getTargetFile());
					if (ao.canLoad()) {
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
			// Refresh lists
			reloadAddOnData();
		}
		
		for (Downloader dl : handledFiles) {
			// Cant remove in loop above as we're iterating through the list
			this.downloadFiles.remove(dl);
		}
		
	}
	
	protected void reloadAddOnData() {
		this.getLocalVersionInfo(true);
		if (addonsDialog != null) {
			addonsDialog.setInstalledAddOns(this.getInstalledAddOns());
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
        this.api = new AutoUpdateAPI(this);
        this.api.addApiOptions(getModel().getOptionsParam().getCheckForUpdatesParam());
        API.getInstance().registerApiImplementor(this.api);
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
            httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true, HttpSender.CHECK_FOR_UPDATES_INITIATOR);
        }
        return httpSender;
    }
    
    /*
     * 
     */
    public void alertIfNewVersions() {
    	// Kicks off a thread and pops up a window if there are new verisons
    	// (depending on the options the user has chosen
    	// Only expect this to be called on startup
    	
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
    
    
    private AddOnCollection getLocalVersionInfo () {
    	return this.getLocalVersionInfo(false);
    }
    
    private AddOnCollection getLocalVersionInfo (boolean forceRefresh) {
    	if (localVersionInfo == null || forceRefresh) {
    		localVersionInfo = ExtensionFactory.getAddOnLoader().getAddOnCollection(); 
    	}
    	return localVersionInfo;
    }

    private ZapXmlConfiguration getRemoteConfigurationUrl(String url) throws 
    		IOException, ConfigurationException {
        HttpMessage msg = new HttpMessage(new URI(url, true));
        getHttpSender().sendAndReceive(msg,true);
        if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
        	logger.error("Failed to access " + url +
        			" response " + msg.getResponseHeader().getStatusCode());
            throw new IOException();
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
    	if (this.getLatestVersionInfo() == null ||
    			this.getLatestVersionInfo().getZapRelease() == null) {
    		return false;
    	}
    	ZapRelease latestRelease = this.getLatestVersionInfo().getZapRelease();
		if (latestRelease.isNewerThan(this.getCurrentVersion())) {
			File f = new File(Constant.FOLDER_LOCAL_PLUGIN, latestRelease.getFileName());
			downloadFile(latestRelease.getUrl(), f, latestRelease.getSize());
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
    
    protected boolean installUpdatedAddOns() {
    	if (this.getLatestVersionInfo() == null ||
    			this.getLatestVersionInfo().getZapRelease() == null) {
    		return false;
    	}
    	boolean response = false;
		for (AddOn ao : getUpdatedAddOns()) {
			this.downloadFile(ao.getUrl(), ao.getFile(), ao.getSize());
			response = true;
		}
		return response;
    }
    
    private boolean installUpdatedScannerRules() {
    	if (this.getLatestVersionInfo() == null ||
    			this.getLatestVersionInfo().getZapRelease() == null) {
    		return false;
    	}
    	boolean response = false;
		for (AddOn ao : getUpdatedAddOns()) {
			if (ao.getId().contains("scanrules")) {
				this.downloadFile(ao.getUrl(), ao.getFile(), ao.getSize());
				response = true;
			}
		}
		return response;
    }
    
    protected boolean installAddOn(String id) {
    	for (AddOn ao : this.getLatestVersionInfo().getAddOns()) {
    		if (ao.getId().equals(id)) {
    			this.downloadFile(ao.getUrl(), ao.getFile(), ao.getSize());
    			return true;
    		}
    	}
    	return false;
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
						logger.debug("Getting latest version info from " + ZAP_VERSIONS_XML_SHORT);
			    		try {
							latestVersionInfo = new AddOnCollection(getRemoteConfigurationUrl(ZAP_VERSIONS_XML_SHORT), getPlatform());
						} catch (Exception e1) {
							logger.debug("Getting latest version info from " + ZAP_VERSIONS_XML_FULL);
							logger.debug("Failed to access " + ZAP_VERSIONS_XML_SHORT, e1);
				    		try {
				    			latestVersionInfo = new AddOnCollection(getRemoteConfigurationUrl(ZAP_VERSIONS_XML_FULL), getPlatform());
							} catch (Exception e2) {
								logger.debug("Failed to access " + ZAP_VERSIONS_XML_FULL, e2);
							}
						}
			    		if (callback != null) {
							logger.debug("Calling callback with  " + latestVersionInfo);
			    			callback.gotLatestData(latestVersionInfo);
			    		}
						logger.debug("Done");
	    			}
    			};
    			this.remoteCallThread.start();
    		}
    		if (callback == null) {
    			// Synchronous
				while (latestVersionInfo == null && this.remoteCallThread.isAlive()) {
					try {
						Thread.sleep(1000);
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
	
	public boolean install(AddOn ao) {
		if (! ao.canLoad()) {
    		throw new IllegalArgumentException("Cant load add-on " + ao.getName() + 
    				" Not before=" + ao.getNotBeforeVersion() + " Not from=" + ao.getNotFromVersion() + 
    				" Version=" + Constant.PROGRAM_VERSION);
		}
		
		AddOn installedAddOn = this.getLocalVersionInfo().getAddOn(ao.getId());
		if (installedAddOn != null) {
   			logger.debug("Trying to uninstall addon " + installedAddOn.getId() + " v" + installedAddOn.getVersion());
   			if (View.isInitialised()) {
   				// Report info to the Output tab
   				View.getSingleton().getOutputPanel().append(
   						MessageFormat.format(
   								Constant.messages.getString("cfu.output.replacing") + "\n", 
   								ao.getName(),
   								ao.getVersion()));
   			}
			if ( ! uninstall(installedAddOn, true)) {
				// Cant uninstall the old version, so dont try to install the new one
	   			logger.debug("Failed to uninstall addon " + installedAddOn.getId() + " v" + installedAddOn.getVersion());
	   			if (View.isInitialised()) {
	   				// Report info to the Output tab
	   				View.getSingleton().getOutputPanel().append(
	   						MessageFormat.format(
	   								Constant.messages.getString("cfu.output.replace.failed") + "\n", 
	   								ao.getName(),
	   								ao.getVersion()));
	   			}
				return false;
			}
		}
		logger.debug("Installing new addon " + ao.getId() + " v" + ao.getVersion());
		if (View.isInitialised()) {
			// Report info to the Output tab
			View.getSingleton().getOutputPanel().append(
					MessageFormat.format(
							Constant.messages.getString("cfu.output.installing") + "\n", 
							ao.getName(),
							ao.getVersion()));
		}

		ExtensionFactory.getAddOnLoader().addAddon(ao);
		return true;
	}

	public boolean uninstall(AddOn addOn, boolean upgrading) {
		boolean removedDynamically = ExtensionFactory.getAddOnLoader().removeAddOn(addOn, upgrading);
		if (View.isInitialised()) {
			if (removedDynamically) {
				View.getSingleton().getOutputPanel().append(
						MessageFormat.format(
								Constant.messages.getString("cfu.output.uninstalled") + "\n", 
								addOn.getName(),
								addOn.getVersion()));
			} else {
				View.getSingleton().getOutputPanel().append(
						MessageFormat.format(
								Constant.messages.getString("cfu.output.uninstall.failed") + "\n", 
								addOn.getName(),
								addOn.getVersion()));
			}
		}
		return removedDynamically;
	}

	@Override
	public void gotLatestData(AddOnCollection aoc) {
		if (aoc == null) {
			return;
		}
		try {
			ZapRelease rel = aoc.getZapRelease();

	    	final OptionsParamCheckForUpdates options = getModel().getOptionsParam().getCheckForUpdatesParam();

			if (rel.isNewerThan(getCurrentVersion())) {
				logger.debug("There is a newer release: " + rel.getVersion());
				// New ZAP release
				File f = new File(Constant.FOLDER_LOCAL_PLUGIN, rel.getFileName());
				if (f.exists() && f.length() >= rel.getSize()) {
					// Already downloaded, prompt to install and exit
					promptToLaunchReleaseAndClose(rel.getVersion(), f);
				} else if (options.isDownloadNewRelease()) {
					logger.debug("Auto-downloading release");
					if (downloadLatestRelease() && View.isInitialised()) {
						getAddOnsDialog().setDownloadingZap();
					}
				} else {
					// Just show the dialog
					getAddOnsDialog().setVisible(true);
				}
			} else if (getUpdatedAddOns().size() > 0) {
				logger.debug("There is are " + getUpdatedAddOns().size() + " newer addons");
				// Updated addons
				if (options.isInstallAddonUpdates()) {
					logger.debug("Auto-downloading addons");
					// download in the background
					installUpdatedAddOns();
					if (View.isInitialised()) {
						getAddOnsDialog().setDownloadingAllUpdates();
					}
				} else if (options.isInstallScannerRules() && installUpdatedScannerRules()) {
					logger.debug("Auto-downloading scanner rules");
					if (View.isInitialised()) {
						// Not strictly true, but has the right effect
						getAddOnsDialog().setDownloadingAllUpdates();
					}
				} else if (options.isCheckAddonUpdates()) {
					// Just show the dialog
					getAddOnsDialog().setVisible(true);
				}
			} else if (getNewAddOns().size() > 0) {
				boolean report = false;
				List<AddOn> addons = getNewAddOns();
				for (AddOn addon : addons) {
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
		} catch (Exception e) {
			// Ignore (well, debug;), will be already logged
			logger.debug(e.getMessage(), e);
		}
	}
}
