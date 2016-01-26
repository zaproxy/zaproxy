/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.api.ZapApiIgnore;

public class OptionsParamCheckForUpdates extends AbstractParam {

	public static final String CHECK_ON_START = "start.checkForUpdates";
	public static final String DAY_LAST_CHECKED = "start.dayLastChecked";
	public static final String DAY_LAST_INSTALL_WARNED = "start.dayLastInstallWarned";
	public static final String DAY_LAST_UPDATE_WARNED = "start.dayLastUpdateWarned";
	public static final String DOWNLOAD_NEW_RELEASE = "start.downloadNewRelease";
	public static final String CHECK_ADDON_UPDATES = "start.checkAddonUpdates";
	public static final String INSTALL_ADDON_UPDATES = "start.installAddonUpdates";
	public static final String INSTALL_SCANNER_RULES = "start.installScannerRules";
	public static final String REPORT_RELEASE_ADDON = "start.reportReleaseAddons";
	public static final String REPORT_BETA_ADDON = "start.reportBetaAddons";
	public static final String REPORT_ALPHA_ADDON = "start.reportAlphaAddons";
	public static final String ADDON_DIRS = "start.addonDirs";
	public static final String DOWNLOAD_DIR = "start.downloadDir";

	private static String SDF_FORMAT = "yyyy-MM-dd";

	private boolean checkOnStart;
	private boolean downloadNewRelease = false;
	private boolean checkAddonUpdates = false;
	private boolean installAddonUpdates = false;
	private boolean installScannerRules = false;
	private boolean reportReleaseAddons = false;
	private boolean reportBetaAddons = false;
	private boolean reportAlphaAddons = false;
	private List<File> addonDirectories = new ArrayList<File>();
	private File downloadDirectory = new File(Constant.FOLDER_LOCAL_PLUGIN);
	
	// Day last checked is used to ensure if the user has agreed then we only check the first time ZAP is run every day
	private String dayLastChecked = null; 
	private String dayLastInstallWarned = null; 
	private String dayLastUpdateWarned = null; 
	private boolean unset = true;
    private static Logger log = Logger.getLogger(OptionsParamCheckForUpdates.class);
    
    public OptionsParamCheckForUpdates() {
    }

    @Override
    protected void parse() {
        updateOldOptions();
        
	    checkOnStart = getConfig().getBoolean(CHECK_ON_START, false);
	    dayLastChecked = getConfig().getString(DAY_LAST_CHECKED, "");
	    // There was a bug in 1.2.0 where it defaulted silently to dont check
	    // We now use the lack of a dayLastChecked value to indicate we should reprompt the user.
		unset = dayLastChecked.length() == 0;
	    dayLastInstallWarned = getConfig().getString(DAY_LAST_INSTALL_WARNED, "");
	    dayLastUpdateWarned = getConfig().getString(DAY_LAST_UPDATE_WARNED, "");
		
		downloadNewRelease = getConfig().getBoolean(DOWNLOAD_NEW_RELEASE, false);
		checkAddonUpdates = getConfig().getBoolean(CHECK_ADDON_UPDATES, false);
		installAddonUpdates = getConfig().getBoolean(INSTALL_ADDON_UPDATES, false);
		installScannerRules = getConfig().getBoolean(INSTALL_SCANNER_RULES, false);
		reportReleaseAddons = getConfig().getBoolean(REPORT_RELEASE_ADDON, false);
		reportBetaAddons = getConfig().getBoolean(REPORT_BETA_ADDON, false);
		reportAlphaAddons = getConfig().getBoolean(REPORT_ALPHA_ADDON, false);
		for (Object dir : getConfig().getList(ADDON_DIRS)) {
			File f = new File(dir.toString());
			if (!f.exists()) {
				log.error("Add-on directory does not exist: " + f.getAbsolutePath());
			} else if (! f.isDirectory()) {
				log.error("Add-on directory is not a directory: " + f.getAbsolutePath());
			} else if (! f.canRead()) {
				log.error("Add-on directory not readable: " + f.getAbsolutePath());
			} else {
				this.addonDirectories.add(f);
			}
		}
		setDownloadDirectory(new File(getConfig().getString(DOWNLOAD_DIR, Constant.FOLDER_LOCAL_PLUGIN)), false);
    }

	private void updateOldOptions() {
		try {
			int oldValue = getConfig().getInt(CHECK_ON_START, 0);
			getConfig().setProperty(CHECK_ON_START, Boolean.valueOf(oldValue != 0));
		} catch(ConversionException ignore) {
			// Option already using boolean type.
		}
	}

	@ZapApiIgnore
	public boolean isCheckOnStartUnset() {
		return unset;
	}
	
	/**
	 * @param checkOnStart 0 to disable check for updates on startup, any other number to enable.
	 * @deprecated (2.3.0) Replaced by {@link #setCheckOnStart(boolean)}. It will be removed in a future release.
	 */
	@Deprecated
	@ZapApiIgnore
	public void setCheckOnStart(int checkOnStart) {
	    setCheckOnStart(checkOnStart != 0);
	}

	/**
	 * Sets whether or not the "check for updates on start up" is enabled.
	 * 
	 * @param checkOnStart {@code true} if the "check for updates on start up" should be enabled, {@code false} otherwise.
	 */
	public void setCheckOnStart(boolean checkOnStart) {
		this.checkOnStart = checkOnStart;
		getConfig().setProperty(CHECK_ON_START, Boolean.valueOf(checkOnStart));
		if (dayLastChecked.length() == 0) {
			dayLastChecked = "Never";
			getConfig().setProperty(DAY_LAST_CHECKED, dayLastChecked);
		}
	}
	
	/**
	 * Tells whether or not the option "check for updates on start up" is enabled.
	 * 
	 * @return {@code true} if check for updates on start up is enabled, {@code false} otherwise.
	 * @see #checkOnStart()
	 */
	public boolean isCheckOnStart() {
		return checkOnStart;
	}

	/**
	 * Get a new SimpleDateFormat each time for thread safeness
	 * @return
	 */
	private SimpleDateFormat getSdf() {
		return new SimpleDateFormat(SDF_FORMAT);
	}

	/**
	 * Tells whether or not a "check for updates on start up" needs to be performed.
	 * <p>
	 * A check for updates needs to be performed if the method {@code isCheckOnStart()} returns {@code true} and if no check was
	 * already done during the same day.
	 * </p>
	 * 
	 * @return {@code true} if a check for updates on start up needs to be performed, {@code false} otherwise.
	 * @see #isCheckOnStart()
	 */
	@ZapApiIgnore
	public boolean checkOnStart() {
		if (!checkOnStart) {
			log.debug("isCheckForStart - false");
			return false;
		}
		String today = getSdf().format(new Date());
		if (today.equals(dayLastChecked)) {
			log.debug("isCheckForStart - already checked today");
			return false;
		}
		getConfig().setProperty(DAY_LAST_CHECKED, today);
		try {
			getConfig().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
		}
		
		return true;
	}
	
	/**
	 * Returns the date the last check for updates check was made, or null if no check has been made
	 * @return
	 */
	public Date getDayLastChecked() {
		try {
			return getSdf().parse(dayLastChecked);
		} catch (ParseException e) {
			// Assume its not been checked
			return null;
		}
	}

	/**
	 * Returns the date the last check for warning about out of date ZAP / add-ons was made, 
	 * or null if no check has been made
	 * @return
	 */
	public Date getDayLastInstallWarned() {
		try {
			return getSdf().parse(dayLastInstallWarned);
		} catch (ParseException e) {
			// Assume we've never warned
			return null;
		}
	}
	
	/**
	 * Returns the date the last check for warning about out of date add-ons was made, 
	 * or null if no check has been made
	 * @return
	 */
	public Date getDayLastUpdateWarned() {
		try {
			return getSdf().parse(dayLastUpdateWarned);
		} catch (ParseException e) {
			// Assume we've never warned
			return null;
		}
	}
	
	public void setDayLastInstallWarned() {
		getConfig().setProperty(DAY_LAST_INSTALL_WARNED, getSdf().format(new Date()));
		try {
			getConfig().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setDayLastUpdateWarned() {
		getConfig().setProperty(DAY_LAST_UPDATE_WARNED, getSdf().format(new Date()));
		try {
			getConfig().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
		}
	}

	public boolean isDownloadNewRelease() {
		return downloadNewRelease;
	}

	public void setDownloadNewRelease(boolean downloadNewRelease) {
		this.downloadNewRelease = downloadNewRelease;
		getConfig().setProperty(DOWNLOAD_NEW_RELEASE, this.downloadNewRelease);
	}

	public boolean isCheckAddonUpdates() {
		return checkAddonUpdates;
	}

	public void setCheckAddonUpdates(boolean checkAddonUpdates) {
		this.checkAddonUpdates = checkAddonUpdates;
		getConfig().setProperty(CHECK_ADDON_UPDATES, checkAddonUpdates);
	}

	public boolean isInstallAddonUpdates() {
		return installAddonUpdates;
	}

	public void setInstallAddonUpdates(boolean installAddonUpdates) {
		this.installAddonUpdates = installAddonUpdates;
		getConfig().setProperty(INSTALL_ADDON_UPDATES, installAddonUpdates);
	}

	public boolean isInstallScannerRules() {
		return installScannerRules;
	}

	public void setInstallScannerRules(boolean installScannerRules) {
		this.installScannerRules = installScannerRules;
		getConfig().setProperty(INSTALL_SCANNER_RULES, installScannerRules);
	}

	public boolean isReportReleaseAddons() {
		return reportReleaseAddons;
	}

	public void setReportReleaseAddons(boolean reportReleaseAddons) {
		this.reportReleaseAddons = reportReleaseAddons;
		getConfig().setProperty(REPORT_RELEASE_ADDON, reportReleaseAddons);
	}

	public boolean isReportBetaAddons() {
		return reportBetaAddons;
	}

	public void setReportBetaAddons(boolean reportBetaAddons) {
		this.reportBetaAddons = reportBetaAddons;
		getConfig().setProperty(REPORT_BETA_ADDON, reportBetaAddons);
	}

	public boolean isReportAlphaAddons() {
		return reportAlphaAddons;
	}

	public void setReportAlphaAddons(boolean reportAlphaAddons) {
		this.reportAlphaAddons = reportAlphaAddons;
		getConfig().setProperty(REPORT_ALPHA_ADDON, reportAlphaAddons);
	}

	public List<File> getAddonDirectories() {
		return addonDirectories;
	}

	public void setAddonDirectories(List<File> addonDirectories) {
		this.addonDirectories = addonDirectories;
		getConfig().setProperty(ADDON_DIRS, addonDirectories);
	}

	public File getDownloadDirectory() {
		return downloadDirectory;
	}

	public void setDownloadDirectory(File downloadDirectory) throws InvalidParameterException {
		setDownloadDirectory(downloadDirectory, true);
	}

	private void setDownloadDirectory(File downloadDirectory, boolean save) throws InvalidParameterException {
		if (!Constant.FOLDER_LOCAL_PLUGIN.equals(downloadDirectory.getAbsolutePath())) {
			// Check its one of the extra addon dirs
			boolean found = false;
			for (File f : this.addonDirectories) {
				if (f.equals(downloadDirectory)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new InvalidParameterException(
						"Directory must be the default one or one of the addonDirectories " + 
						downloadDirectory.getAbsolutePath());
			}
		}
		if (!downloadDirectory.canWrite()) {
			throw new InvalidParameterException(
					"No write access to directory " + 
					downloadDirectory.getAbsolutePath());
		}
		
		this.downloadDirectory = downloadDirectory;
		if (save) {
			getConfig().setProperty(DOWNLOAD_DIR, downloadDirectory.getAbsolutePath());
		}
	}

}
