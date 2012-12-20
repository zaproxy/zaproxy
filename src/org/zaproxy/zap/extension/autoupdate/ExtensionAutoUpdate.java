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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.history.LogPanelCellRenderer;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.control.AddOnCollection.Platform;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.control.ZapRelease;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.log4j.ExtensionLog4j;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ScanStatus;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionAutoUpdate extends ExtensionAdaptor {
	
	// The short URL means that the number of checkForUpdates can be tracked - see http://goo.gl/info/V4aWX
	// TODO
    //private static final String ZAP_VERSIONS_XML_SHORT = "http://goo.gl/V4aWX";
    //private static final String ZAP_VERSIONS_XML_FULL = "http://zaproxy.googlecode.com/svn/wiki/ZapVersions.xml";
    private static final String ZAP_VERSIONS_XML_SHORT = "http://localhost:8080/zapcfu/ZapVersions.xml";
    private static final String ZAP_VERSIONS_XML_FULL = "http://localhost:8080/zapcfu/ZapVersions.xml";
    
	private static final String VERSION_FILE_NAME = "ZapVersions.xml";

	private JMenuItem menuItemCheckUpdate = null;
    
    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
    private WaitMessageDialog waitDialog = null;
	private HttpSender httpSender = null;

    private DownloadManager downloadManager = null;
	private AddOnsDialog addonsDialog = null;
	private UpdateDialog updateDialog = null;
	private Thread downloadProgressThread = null;
	private Thread remoteCallThread = null; 
	private ScanStatus scanStatus = null;
	private JButton addonsButton = null;
	
	private AddOnCollection latestVersionInfo = null;
	private AddOnCollection localVersionInfo = null;
	private AddOnCollection previousVersionInfo = null;

    private AutoUpdateAPI api = null;

    // Files currently being downloaded
	private List<Downloader> downloadFiles = new ArrayList<Downloader>();

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
        this.downloadManager = new DownloadManager();
        this.downloadManager.start();
        // Do this before it can get overwritten by the latest one
        this.getPreviousVersionInfo();
	}

	/**
	 * This method initializes menuItemEncoder	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemCheckUpdate() {
		if (menuItemCheckUpdate == null) {
			menuItemCheckUpdate = new JMenuItem();
			menuItemCheckUpdate.setText(Constant.messages.getString("cfu.help.menu.check"));
			menuItemCheckUpdate.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					showUpdatesDialog(true);
				}

			});
		}
		return menuItemCheckUpdate;
	}

	public void showUpdatesDialog(boolean manual) {
        if (manual && latestVersionInfo == null) {
        	// Could fail to connect and time out
        	waitDialog = getView().getWaitMessageDialog(Constant.messages.getString("cfu.check.checking"));
        	// Allow user to close the dialog
        	waitDialog.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        	waitDialog.setVisible(true);
        }
		
		try {
			if (updateDialog == null) {
				if (this.getLatestVersionInfo() == null) {
					// Manually cancelled
					return;
				}
		        updateDialog = this.getUpdateDialog();
			} else {
				updateDialog.setUpdatedAddOns(getUpdatedAddOns());
			}
			updateDialog.setVisible(true);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
        	if (manual) {
        		getView().showWarningDialog(
    				Constant.messages.getString("cfu.check.failed"));
        	}
		}
	}
	
	private synchronized UpdateDialog getUpdateDialog() {
		if (updateDialog == null) {
			ZapRelease rel = this.getLatestVersionInfo().getZapRelease();
			if (rel != null && rel.isNewerThan(this.getCurrentVersion())) {
				updateDialog = new UpdateDialog(this, rel, getUpdatedAddOns());
			} else {
				// Dont supply new version info - its not newer
				updateDialog = new UpdateDialog(this, null, getUpdatedAddOns());
			}
		}
		return updateDialog;
	}

	public void downloadFile (URL url, File targetFile, long size) {
		this.downloadFiles.add(this.downloadManager.downloadFile(url, targetFile, size));
		if (View.isInitialised()) {
			// Means we do have a UI
			if (this.downloadProgressThread != null && ! this.downloadProgressThread.isAlive()) {
				this.downloadProgressThread = null;
			}
			if (this.downloadProgressThread == null) {
				this.downloadProgressThread = new Thread() {
					public void run() {
						while (downloadManager.getCurrentDownloadCount() > 0) {
							getScanStatus().setScanCount(downloadManager.getCurrentDownloadCount());
							getUpdateDialog().showUpdateProgress();
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
						if (updateDialog != null) {
							updateDialog.showUpdateProgress();
						}
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
		List<Downloader> handledFiles = new ArrayList<Downloader>();
		
		ExtensionLoader loader = Control.getSingleton().getExtensionLoader();
		for (Downloader dl : downloadFiles) {
			if (dl.getFinished() == null) {
				continue;
			}
			handledFiles.add(dl);
			try {
				if (AddOn.isAddOn(dl.getTargetFile())) {
					AddOn ao = new AddOn(dl.getTargetFile());
					ExtensionFactory.getAddOnLoader().addAddon(ao);
					// Note that updated extensions will be ignored until ZAP is restarted
					List<Extension> listExts = ExtensionFactory.loadAddOnExtensions(
							Control.getSingleton().getExtensionLoader(), 
							Model.getSingleton().getOptionsParam().getConfig(), ao);
					
				   	for (Extension ext : listExts) {
				   		if (ext.isEnabled()) {
				   			logger.debug("Starting extension " + ext.getName());
				   			loader.startLifeCycle(ext);
				   		}
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
			addonsButton.setIcon(new ImageIcon(LogPanelCellRenderer.class.getResource("/resource/icon/fugue/block.png")));
			addonsButton.setToolTipText(Constant.messages.getString("cfu.button.addons.browse"));
			addonsButton.setEnabled(true);
			addonsButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					browseAddons();
				}
			});

		}
		return this.addonsButton;
	}
	
	private void browseAddons() {
		try {
			if (addonsDialog == null) {
				addonsDialog = new AddOnsDialog(this, 
						this.getUninstalledAddOns(), this.getNewAddOns());
			}
			addonsDialog.setVisible(true);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
    		getView().showWarningDialog(
				Constant.messages.getString("cfu.browse.failed"));
		}
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
    	
		if (! options.isCheckOnStart()) {
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
    	
		Thread t = new Thread() {
			@Override
			public void run() {
				// Using a thread as the first call to getLatestVersionInfo() could timeout
				// and we dont want the ui to hang in the meantime
				try {
					ZapRelease rel = getLatestVersionInfo().getZapRelease();
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
								getUpdateDialog().setDownloadingZap();
							}
						} else {
							showUpdatesDialog(false);
						}
					} else if (getUpdatedAddOns().size() > 0) {
						logger.debug("There is are " + getUpdatedAddOns().size() + " newer addons");
						// Updated addons
						if (options.isInstallAddonUpdates()) {
							logger.debug("Auto-downloading addons");
							// download in the background
							installUpdatedAddOns();
							if (View.isInitialised()) {
								getUpdateDialog().setDownloadingAllUpdates();
							}
						} else if (options.isCheckAddonUpdates()) {
							// just show the updates dialog
							showUpdatesDialog(false);
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
							browseAddons();
						}
					}
				} catch (Exception e) {
					// Ignore, will be already logged
				}
			}
		};
		t.start();
    }
    
    
    private AddOnCollection getLocalVersionInfo () {
    	if (localVersionInfo == null) {
    		localVersionInfo = ExtensionFactory.getAddOnLoader().getAddOnCollection(); 
    	}
    	return localVersionInfo;
    }

    private ZapXmlConfiguration getRemoteConfigurationUrl(String url) throws 
    		HttpException, IOException, ConfigurationException {
        HttpMessage msg = new HttpMessage(new URI(url, true));
        getHttpSender().sendAndReceive(msg,true);
        if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
        	logger.error("Failed to access " + url +
        			" response " + msg.getResponseHeader().getStatusCode());
            throw new IOException();
        }
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
        
    	ZapXmlConfiguration config = new ZapXmlConfiguration();
    	config.setDelimiterParsingDisabled(true);
    	config.load(new StringReader(msg.getResponseBody().toString()));
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
    
    private List<AddOn> getUninstalledAddOns() {
    	return getLocalVersionInfo().getNewAddOns(this.getLatestVersionInfo());
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
    
    protected boolean installAddOns(String id) {
    	for (AddOn ao : this.getLatestVersionInfo().getAddOns()) {
    		if (ao.getId().equals(id)) {
    			this.downloadFile(ao.getUrl(), ao.getFile(), ao.getSize());
    			return true;
    		}
    	}
    	return false;
    }

    private AddOnCollection getLatestVersionInfo () {
    	return getLatestVersionInfo(true);
    }
    
    private AddOnCollection getLatestVersionInfo (boolean sync) {
    	if (latestVersionInfo == null) {
    		
    		if (this.remoteCallThread == null) {
    			this.remoteCallThread = new Thread() {
    			
	    			@Override
	    			public void run() {
	    				// Using a thread as the first call could timeout
	    				// and we dont want the ui to hang in the meantime
			    		try {
							latestVersionInfo = new AddOnCollection(getRemoteConfigurationUrl(ZAP_VERSIONS_XML_SHORT), getPlatform());
						} catch (Exception e1) {
							logger.debug("Failed to access " + ZAP_VERSIONS_XML_SHORT, e1);
				    		try {
				    			latestVersionInfo = new AddOnCollection(getRemoteConfigurationUrl(ZAP_VERSIONS_XML_FULL), getPlatform());
							} catch (Exception e2) {
								logger.debug("Failed to access " + ZAP_VERSIONS_XML_FULL, e2);
							}
						}
				        if (waitDialog != null) {
				            waitDialog.setVisible(false);
				            waitDialog = null;
				        }
	    			}
    			};
    			this.remoteCallThread.start();
    			
    			if (!sync) {
    				return null;
    			}
    		}
			while (latestVersionInfo == null && this.remoteCallThread.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Ignore
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
}
