/*
 * Created on May 18, 2004
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/08/03 Revamped upgrade for 1.3.2
// ZAP: 2011/10/05 Write backup file to user dir
// ZAP: 2011/11/15 Changed to use ZapXmlConfiguration, to enforce the same 
// character encoding when reading/writing configurations. Changed to use the
// correct file when an error occurs during the load of the configuration file.
// Removed the calls XMLConfiguration.load() as they are not needed, the 
// XMLConfiguration constructor used already does that.
// ZAP: 2011/11/20 Support for extension factory
// ZAP: 2012/03/03 Added ZAP homepage
// ZAP: 2012/03/15 Removed a @SuppressWarnings annotation from the method 
// copyAllProperties.
// ZAP: 2012/03/17 Issue 282 ZAP and PAROS team constants
// ZAP: 2012/05/02 Added method createInstance and changed the method
// getInstance to use it.
// ZAP: 2012/05/03 Changed the Patterns used to detect the O.S. to be final.
// ZAP: 2012/06/15 Issue 312 Increase the maximum number of scanning threads allowed
// ZAP: 2012/07/13 Added variable for maximum number of threads used in scan (MAX_THREADS_PER_SCAN)
// ZAP: 2012/10/15 Issue 397: Support weekly builds
// ZAP: 2012/10/17 Issue 393: Added more online links from menu
// ZAP: 2012/11/15 Issue 416: Normalise how multiple related options are managed
// throughout ZAP and enhance the usability of some options.
// ZAP: 2012/11/20 Issue 419: Restructure jar loading code
// ZAP: 2012/12/08 Issue 428: Changed to use I18N for messages, to support the marketplace
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/04/14 Issue 610: Replace the use of the String class for available/default "Forced Browse" files
// ZAP: 2013/04/15 Issue 632: Manual Request Editor dialogue (HTTP) configurations not saved correctly

package org.parosproxy.paros;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.FileCopier;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public final class Constant {
	// ZAP: rebrand
    public static final String PROGRAM_NAME     		= "OWASP ZAP";
    public static final String PROGRAM_NAME_SHORT 		= "ZAP";
    public static final String ZAP_HOMEPAGE				= "http://www.owasp.org/index.php/ZAP";
    public static final String ZAP_EXTENSIONS_PAGE		= "https://code.google.com/p/zap-extensions/";
    public static final String ZAP_DOWNLOADS_PAGE		= "https://code.google.com/p/zaproxy/downloads/list";
    public static final String ZAP_WIKI_PAGE			= "https://code.google.com/p/zaproxy/wiki/Introduction";
    public static final String ZAP_USER_GROUP_PAGE		= "http://groups.google.com/group/zaproxy-users";
    public static final String ZAP_DEV_GROUP_PAGE		= "http://groups.google.com/group/zaproxy-develop";
    public static final String ZAP_ISSUES_PAGE			= "https://code.google.com/p/zaproxy/issues/list";
    public static final String ZAP_TEAM					= "ZAP Dev Team";
    public static final String PAROS_TEAM				= "Chinotec Technologies";
    
//  ************************************************************
//  the config.xml MUST be set to be the same as the version_tag
//  otherwise the config.xml will be overwritten everytime.
//  ************************************************************
    private static final String DEV_VERSION = "Dev Build";
    public static final String ALPHA_VERSION = "alpha";
    public static final String BETA_VERSION = "beta";
    // Note: Change this before building a release!
    //public static final String PROGRAM_VERSION = "1.4.0.1";
    
    private static final long VERSION_TAG = 1004001;
    
    // Old version numbers - for upgrade
    private static final long V_2_0_0_TAG = 2000000;
	private static final long V_1_4_1_TAG = 1004001;
    private static final long V_1_3_1_TAG = 1003001;
	private static final long V_1_3_0_TAG = 1003000;
    private static final long V_1_2_1_TAG = 1002001;
    private static final long V_1_2_0_TAG = 1002000;
    private static final long V_1_1_0_TAG = 1001000;
    private static final long V_1_0_0_TAG = 1000000;
    private static final long V_PAROS_TAG = 30020013;
    
//  ************************************************************
//  note the above
//  ************************************************************
    
    // These are no longer final
    public static String PROGRAM_VERSION = DEV_VERSION;
    public static String PROGRAM_TITLE = PROGRAM_NAME + " " + PROGRAM_VERSION;
    
    public static final String SYSTEM_PAROS_USER_LOG = "zap.user.log";
    
//  public static final String FILE_CONFIG = "xml/config.xml";
//  public static final String FOLDER_PLUGIN = "plugin";
//  public static final String FOLDER_FILTER = "filter";
//  public static final String FOLDER_SESSION = "session";
//  public static final String DBNAME_TEMPLATE = "db/parosdb";
//  public static final String DBNAME_UNTITLED = FOLDER_SESSION + "/untitled";

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    
    public static final String FILE_CONFIG_DEFAULT = "xml/config.xml";
    public static final String FILE_CONFIG_NAME = "config.xml";
    public static final String FOLDER_PLUGIN = "plugin";
    public static final String FOLDER_FILTER = "filter";
    public static final String FOLDER_SESSION_DEFAULT = "session";
    public static final String DBNAME_TEMPLATE = "db" + System.getProperty("file.separator") + "zapdb";
    public static final String MESSAGES_PREFIX = "Messages";

    public static final String DBNAME_UNTITLED_DEFAULT = FOLDER_SESSION_DEFAULT + System.getProperty("file.separator") + "untitled";

    public String FILE_CONFIG = FILE_CONFIG_NAME;
    public String FOLDER_SESSION = "session";
    public String DBNAME_UNTITLED = FOLDER_SESSION + System.getProperty("file.separator") + "untitled";
    public String ACCEPTED_LICENSE_DEFAULT = "AcceptedLicense";
    public String ACCEPTED_LICENSE = ACCEPTED_LICENSE_DEFAULT;
    
    public static final String FILE_PROGRAM_SPLASH = "resource/zap128x128.png";
    
	// Accelerator keys - Default: Windows
	public static String ACCELERATOR_UNDO = "control Z";
	public static String ACCELERATOR_REDO = "control Y";
	public static String ACCELERATOR_TRIGGER_KEY = "Control";
    
    private static Constant instance = null;
    
    public static final int MAX_HOST_CONNECTION = 15;
    public static final int MAX_THREADS_PER_SCAN = 50;
    // ZAP: Dont announce ourselves
    //public static final String USER_AGENT = PROGRAM_NAME + "/" + PROGRAM_VERSION;
    public static final String USER_AGENT = "";

    private static String staticEyeCatcher = "0W45pz4p";
    private static boolean staticSP = false;
    
    // 
    // Home dir for ZAP, ie where the config file is. Can be set on cmdline, otherwise will be set to default loc
    private static String zapHome = null;
    // Default home dir for 'full' releases - only used for copying full conf file when dev/daily release run for the first time
    private static String zapStd = null;
    
    // ZAP: Added i18n
    public static I18N messages = null;

    /**
     * Path to the file that contains the vulnerabilities' data.
     */
    // ZAP: Added vulnerabilities file
    public String VULNS_CONFIG = "xml/vulnerabilities.xml";
    
    // ZAP: Added dirbuster dir
    public String DIRBUSTER_DIR = "dirbuster";
    public String DIRBUSTER_CUSTOM_DIR = DIRBUSTER_DIR;

    public String FUZZER_DIR = "fuzzers";
    public String FUZZER_CUSTOM_DIR = FUZZER_DIR;
    
    public static String FOLDER_LOCAL_PLUGIN = FOLDER_PLUGIN;


	public static final URL OK_FLAG_IMAGE_URL = Constant.class.getResource("/resource/icon/10/072.png"); 		// Green
	public static final URL INFO_FLAG_IMAGE_URL = Constant.class.getResource("/resource/icon/10/073.png"); 	// Blue
	public static final URL LOW_FLAG_IMAGE_URL = Constant.class.getResource("/resource/icon/10/074.png");		// Yellow
	public static final URL MED_FLAG_IMAGE_URL = Constant.class.getResource("/resource/icon/10/076.png");		// Orange
	public static final URL HIGH_FLAG_IMAGE_URL = Constant.class.getResource("/resource/icon/10/071.png");	// Red
	public static final URL BLANK_IMAGE_URL = Constant.class.getResource("/resource/icon/10/blank.png");
	public static final URL SPIDER_IMAGE_URL = Constant.class.getResource("/resource/icon/10/spider.png");

    public static String getEyeCatcher() {
        return staticEyeCatcher;
    }
    
    public static void setEyeCatcher(String eyeCatcher) {
        staticEyeCatcher = eyeCatcher;
    }
    
    public static void setSP(boolean isSP) {
        staticSP = isSP;
    }

    public static boolean isSP() {
        return staticSP;
    }


    public Constant() {
    	initializeFilesAndDirectories();
    	setAcceleratorKeys();
    }
    	
    private void initializeFilesAndDirectories() {
        
    	FileCopier copier = new FileCopier();
        File f = null;
        Logger log = null;

        // default to use application directory 'log'
        System.setProperty(SYSTEM_PAROS_USER_LOG, "log");

        // Set up the version from the manifest
        PROGRAM_VERSION = getVersionFromManifest();
        PROGRAM_TITLE = PROGRAM_NAME + " " + PROGRAM_VERSION;

        if (zapHome == null) {
            zapHome = System.getProperty("user.home");
            if (zapHome == null) {
            	zapHome = ".";
            }

            if (isLinux()) {
            	// Linux: Hidden Zap directory in the user's home directory
            	zapHome += FILE_SEPARATOR + "." + PROGRAM_NAME_SHORT;
			} else if (isMacOsX()) {
				// Mac Os X: Support for writing the configuration into the users Library 
				zapHome += FILE_SEPARATOR + "Library" + FILE_SEPARATOR
					+ "Application Support" + FILE_SEPARATOR + PROGRAM_NAME_SHORT;
			} else {
				// Windows: Zap directory in the user's home directory
				zapHome += FILE_SEPARATOR + PROGRAM_NAME;
			}
            // Will use default when first dev/daily release run
            zapStd = zapHome;
            if (isDevBuild() || isDailyBuild()) {
            	// Default to a different home dir to prevent messing up full releases
            	zapHome += "_D";
            }
        }
		
		f = new File(zapHome);
		
		zapHome += FILE_SEPARATOR;
		FILE_CONFIG = zapHome + FILE_CONFIG;
		FOLDER_SESSION = zapHome + FOLDER_SESSION;
		DBNAME_UNTITLED = zapHome + DBNAME_UNTITLED;
		ACCEPTED_LICENSE = zapHome + ACCEPTED_LICENSE;
		DIRBUSTER_CUSTOM_DIR = zapHome + DIRBUSTER_DIR;
		FUZZER_CUSTOM_DIR = zapHome + FUZZER_CUSTOM_DIR;
		FOLDER_LOCAL_PLUGIN = zapHome + FOLDER_LOCAL_PLUGIN;

        try {
            System.setProperty(SYSTEM_PAROS_USER_LOG, zapHome);
            
            if (!f.isDirectory()) {
                if (! f.mkdir() ) {
                	// ZAP: report failure to create directory
                	System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            
            // Setup the logging
            File logFile = new File(zapHome + "/log4j.properties");
            if (!logFile.exists()) {
            	copier.copy(new File("xml/log4j.properties"),logFile);
            }
            System.setProperty("log4j.configuration", logFile.getAbsolutePath());
            PropertyConfigurator.configure(logFile.getAbsolutePath());
            log = Logger.getLogger(Constant.class);
            
            f = new File(FILE_CONFIG);
            if (!f.isFile()) {
            	File oldf;
                if (isDevBuild() || isDailyBuild()) {
                	// try standard location
                	oldf = new File (zapStd + FILE_SEPARATOR + FILE_CONFIG_NAME);
                } else {
                	// try old location
                	oldf = new File (zapHome + FILE_SEPARATOR + "zap" + FILE_SEPARATOR + FILE_CONFIG_NAME);
                }
            	
            	if (oldf.exists()) {
            		log.info("Copying defaults from " + oldf.getAbsolutePath() + " to " + FILE_CONFIG);
            		copier.copy(oldf,f);
            		
            	} else {
            		log.info("Copying defaults from " + FILE_CONFIG_DEFAULT + " to " + FILE_CONFIG);
            		copier.copy(new File(FILE_CONFIG_DEFAULT),f);
            	}
            }
            
            f = new File(FOLDER_SESSION);
            if (!f.isDirectory()) {
                log.info("Creating directory " + FOLDER_SESSION);
                if (! f.mkdir() ) {
                	// ZAP: report failure to create directory
                	System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            f = new File(DIRBUSTER_CUSTOM_DIR);
            if (!f.isDirectory()) {
                log.info("Creating directory " + DIRBUSTER_CUSTOM_DIR);
                if (! f.mkdir() ) {
                	// ZAP: report failure to create directory
                	System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            f = new File(FUZZER_CUSTOM_DIR);
            if (!f.isDirectory()) {
                log.info("Creating directory " + FUZZER_CUSTOM_DIR);
                if (! f.mkdir() ) {
                	// ZAP: report failure to create directory
                	System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            f = new File(FOLDER_LOCAL_PLUGIN);
            if (!f.isDirectory()) {
                log.info("Creating directory " + FOLDER_LOCAL_PLUGIN);
                if (! f.mkdir() ) {
                	// ZAP: report failure to create directory
                	System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            System.err.println("Unable to initialize home directory! " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        
        
        // Upgrade actions
        try {
	        try {
	            
	            // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding when reading/writing configurations.
	        	XMLConfiguration config = new ZapXmlConfiguration(FILE_CONFIG);
	            config.setAutoSave(false);
	
	            long ver = config.getLong("version");
	            
	            if (ver == VERSION_TAG) {
	            	// Nothing to do
	            } else if (isDevBuild() || isDailyBuild()) {
	            	// Nothing to do
	            } else {
	            	// Backup the old one
	            	log.info("Backing up config file to " + FILE_CONFIG + ".bak");
            		f = new File(FILE_CONFIG);
	                try {
						copier.copy(f, new File(FILE_CONFIG + ".bak"));
					} catch (IOException e) {
						String msg = "Failed to backup config file " + 
	            			FILE_CONFIG + " to " + FILE_CONFIG + ".bak " + e.getMessage();
			            System.err.println(msg);
			            log.error(msg, e);
					}
	                
		            if (ver == V_PAROS_TAG) {
	            		upgradeFrom1_1_0(config);
	            		upgradeFrom1_2_0(config);
		            }
	            	if (ver <= V_1_0_0_TAG) {
	            		// Nothing to do
	            	}
	            	if (ver <= V_1_1_0_TAG) {
	            		upgradeFrom1_1_0(config);
	            	}
	            	if (ver <= V_1_2_0_TAG) {
	            		upgradeFrom1_2_0(config);
	            	}
	            	if (ver <= V_1_2_1_TAG) {
	            		// Nothing to do
	            	}
	            	if (ver <= V_1_3_0_TAG) {
	            		// Nothing to do
	            	}
	            	if (ver <= V_1_3_1_TAG) {
	            		// Nothing to do
	            	}
	            	if (ver <= V_1_4_1_TAG) {
	            		upgradeFrom1_4_1(config);
	            	}
	            	if (ver <= V_2_0_0_TAG) {
	            		upgradeFrom2_0_0(config);
	            	}
	            	log.info("Upgraded from " + ver);
            		
            		// Update the version
            		config.setProperty("version", VERSION_TAG);
            		config.save();
            	}

	        } catch (ConfigurationException e) {
	            //  if there is any error in config file (eg config file not exist),
	            //  overwrite previous configuration file 
	            // ZAP: changed to use the correct file
	            copier.copy(new File(FILE_CONFIG_DEFAULT), new File(FILE_CONFIG));
	
	        } catch (NoSuchElementException e) {
	            //  overwrite previous configuration file if config file corrupted
	            // ZAP: changed to use the correct file
	            copier.copy(new File(FILE_CONFIG_DEFAULT), new File(FILE_CONFIG));
	            
	        }
        } catch (Exception e) {
            System.err.println("Unable to upgrade config file " + FILE_CONFIG + " " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        
        // ZAP: Init i18n
        String lang = null;
        Locale locale = Locale.ENGLISH;
        try {
            // Select the correct locale
            // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding when reading/writing configurations.
            XMLConfiguration config = new ZapXmlConfiguration(FILE_CONFIG);
            config.setAutoSave(false);

            lang = config.getString(OptionsParamView.LOCALE, OptionsParamView.DEFAULT_LOCALE);
            if (lang.length() == 0) {
            	lang = OptionsParamView.DEFAULT_LOCALE;
            }
            String[] langArray = lang.split("_");
            locale = new Locale(langArray[0], langArray[1]);
        } catch (Exception e) {
        	System.out.println("Failed to initialise locale " + e);
        }

        messages = new I18N(locale);
    }
    
    private void copyProperty(XMLConfiguration fromConfig, XMLConfiguration toConfig, String key) {
    	toConfig.setProperty(key, fromConfig.getProperty(key));
    }
    
	private void copyAllProperties(XMLConfiguration fromConfig, XMLConfiguration toConfig, String prefix) {
    	Iterator<String> iter = fromConfig.getKeys(prefix);
    	while (iter.hasNext()) {
    		String key = iter.next();
    		copyProperty(fromConfig, toConfig, key);
    	}
    }
    
    private void upgradeFrom1_1_0(XMLConfiguration config) throws ConfigurationException {
		// Upgrade the regexs
        // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding when reading/writing configurations.
        XMLConfiguration newConfig = new ZapXmlConfiguration(FILE_CONFIG_DEFAULT);
        newConfig.setAutoSave(false);

        copyAllProperties(newConfig, config, "pscans");                
	}
    
    private void upgradeFrom1_2_0(XMLConfiguration config) throws ConfigurationException {
		// Upgrade the regexs
        // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding when reading/writing configurations.
        XMLConfiguration newConfig = new ZapXmlConfiguration(FILE_CONFIG_DEFAULT);
        newConfig.setAutoSave(false);

        copyProperty(newConfig, config, "view.editorView");
        copyProperty(newConfig, config, "view.brkPanelView");
        copyProperty(newConfig, config, "view.showMainToolbar");
	}
    
    private void upgradeFrom1_4_1(XMLConfiguration config) {
		// As the POST_FORM option for the spider has been updated from int to boolean, keep
		// compatibility for old versions
		if (!config.getProperty("spider.postform").toString().equals("0")) {
			config.setProperty("spider.postform", "true");
			config.setProperty("spider.processform", "true");
		} else {
			config.setProperty("spider.postform", "false");
			config.setProperty("spider.processform", "false");
		}

		
		// Move the old session tokens to the new "httpsessions" hierarchy and 
		// delete the old "session" hierarchy as it's no longer used/needed.
		String[] tokens = config.getStringArray("session.tokens");
		for (int i = 0; i < tokens.length; ++i) {
		    String elementBaseKey = "httpsessions.tokens.token(" + i + ").";
		    
		    config.setProperty(elementBaseKey + "name", tokens[i]);
		    config.setProperty(elementBaseKey + "enabled", Boolean.TRUE);
		}
		config.clearTree("session");
		
		
		// Update the anti CSRF tokens elements/hierarchy.
		tokens = config.getStringArray("anticsrf.tokens");
		config.clearTree("anticsrf.tokens");
		for (int i = 0; i < tokens.length; ++i) {
		    String elementBaseKey = "anticsrf.tokens.token(" + i + ").";
		    
		    config.setProperty(elementBaseKey + "name", tokens[i]);
		    config.setProperty(elementBaseKey + "enabled", Boolean.TRUE);
		}
		
		
		// Update the invoke applications elements/hierarchy.
		List<Object[]> oldData = new ArrayList<>();
		for (int i = 0; ; i++) {
		    String baseKey = "invoke.A" + i + ".";
            String host = config.getString(baseKey + "name");
            if (host == null || "".equals(host)) {
               break;
            }
            
            Object[] data = new Object[6];
            data[0] = host;
            data[1] = config.getString(baseKey + "directory", "");
            data[2] = config.getString(baseKey + "command");
            data[3] = config.getString(baseKey + "parameters");
            data[4] = Boolean.valueOf(config.getBoolean(baseKey + "output", true));
            data[5] = Boolean.valueOf(config.getBoolean(baseKey + "note", false));
            oldData.add(data);
        }
        config.clearTree("invoke.A");
        for (int i = 0, size = oldData.size(); i < size; ++i) {
            String elementBaseKey = "invoke.apps.app(" + i + ").";
            Object[] data = oldData.get(i);
            
            config.setProperty(elementBaseKey + "name", data[0]);
            config.setProperty(elementBaseKey + "directory", data[1]);
            config.setProperty(elementBaseKey + "command", data[2]);
            config.setProperty(elementBaseKey + "parameters", data[3]);
            config.setProperty(elementBaseKey + "output", data[4]);
            config.setProperty(elementBaseKey + "note", data[5]);
            config.setProperty(elementBaseKey + "enabled", Boolean.TRUE);
        }

        
		// Update the authentication elements/hierarchy.
        oldData = new ArrayList<>();
        for (int i = 0; ; i++) {
            String baseKey = "connection.auth.A" + i + ".";
            String host = config.getString(baseKey + "hostName");
            if (host == null || "".equals(host)) {
               break;
            }
            
            Object[] data = new Object[5];
            data[0] = host;
            data[1] = Integer.valueOf(config.getString(baseKey + "port", "80"));
            data[2] = config.getString(baseKey + "userName");
            data[3] = config.getString(baseKey + "password");
            data[4] = config.getString(baseKey + "realm");
            oldData.add(data);
        }
        config.clearTree("connection.auth.A");
        for (int i = 0, size = oldData.size(); i < size; ++i) {
            String elementBaseKey = "connection.auths.auth(" + i + ").";
            Object[] data = oldData.get(i);
            
            config.setProperty(elementBaseKey + "name", "Auth " + i);
            config.setProperty(elementBaseKey + "hostName", data[0]);
            config.setProperty(elementBaseKey + "port", data[1]);
            config.setProperty(elementBaseKey + "userName", data[2]);
            config.setProperty(elementBaseKey + "password", data[3]);
            config.setProperty(elementBaseKey + "realm", data[4]);
            config.setProperty(elementBaseKey + "enabled", Boolean.TRUE);
        }
        
        
        // Update the passive scan elements/hierarchy.
        String[] names = config.getStringArray("pscans.names");
        oldData = new ArrayList<>();
        for (String pscanName : names) {
            String baseKey = "pscans." + pscanName + ".";

            Object[] data = new Object[8];
            data[0] = pscanName;
            data[1] = config.getString(baseKey + "type");
            data[2] = config.getString(baseKey + "config");
            data[3] = config.getString(baseKey + "reqUrlRegex");
            data[4] = config.getString(baseKey + "reqHeadRegex");
            data[5] = config.getString(baseKey + "resHeadRegex");
            data[6] = config.getString(baseKey + "resBodyRegex");
            data[7] = Boolean.valueOf(config.getBoolean(baseKey + "enabled"));
            oldData.add(data);
        }
        config.clearTree("pscans.names");
        for (String pscanName : names) {
            config.clearTree("pscans." + pscanName);
        }
        for (int i = 0, size = oldData.size(); i < size; ++i) {
            String elementBaseKey = "pscans.autoTagScanners.scanner(" + i + ").";
            Object[] data = oldData.get(i);
            
            config.setProperty(elementBaseKey + "name", data[0]);
            config.setProperty(elementBaseKey + "type", data[1]);
            config.setProperty(elementBaseKey + "config", data[2]);
            config.setProperty(elementBaseKey + "reqUrlRegex", data[3]);
            config.setProperty(elementBaseKey + "reqHeadRegex", data[4]);
            config.setProperty(elementBaseKey + "resHeadRegex", data[5]);
            config.setProperty(elementBaseKey + "resBodyRegex", data[6]);
            config.setProperty(elementBaseKey + "enabled", data[7]);
        }
	}

    private void upgradeFrom2_0_0(XMLConfiguration config) {
        String forcedBrowseFile = config.getString("bruteforce.defaultFile", "");
        if (!"".equals(forcedBrowseFile)) {
            String absolutePath = "";
            // Try the 'local' dir first
            File f = new File(DIRBUSTER_CUSTOM_DIR + File.separator + forcedBrowseFile);
            if (!f.exists()) {
                f = new File(DIRBUSTER_DIR + File.separator + forcedBrowseFile);
            }
            if (f.exists()) {
                absolutePath = f.getAbsolutePath();
            }
            config.setProperty("bruteforce.defaultFile", absolutePath);
        }
        
        // Remove the manual request editor configurations that were incorrectly created.
        config.clearTree("nullrequest");
        config.clearTree("nullresponse");
    }

	public static void setLocale (String loc) {
        String[] langArray = loc.split("_");
        Locale locale = new Locale(langArray[0], langArray[1]);
        if (messages == null) {
        	messages = new I18N(locale);
        } else {
        	messages.setLocale(locale);
        }
    }
	
	public static Locale getLocale () {
		return messages.getLocal();
	}
    
    public static Constant getInstance() {
        if (instance==null) {
            // ZAP: Changed to use the method createInstance().
            createInstance();
        }
        return instance;
    }
    
    // ZAP: Added method.
    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new Constant();
        }
    }
    
    private void setAcceleratorKeys() {

		// Undo/Redo
		if (Constant.isMacOsX()) {
			ACCELERATOR_UNDO = "meta Z";
			ACCELERATOR_REDO = "meta shift Z";
			ACCELERATOR_TRIGGER_KEY = "Meta";
		} else {
			ACCELERATOR_UNDO = "control Z";
			ACCELERATOR_REDO = "control Y";
			ACCELERATOR_TRIGGER_KEY = "Control";
		}
	}
    
    
    // Determine Windows Operating System
    // ZAP: Changed to final.
    private static final Pattern patternWindows = Pattern.compile("window", Pattern.CASE_INSENSITIVE);
    
    public static boolean isWindows() {
        String os_name = System.getProperty("os.name");
        
        Matcher matcher = patternWindows.matcher(os_name);
        return matcher.find();
    }
    
    // Determine Linux Operating System
    // ZAP: Changed to final.
    private static final Pattern patternLinux = Pattern.compile("linux", Pattern.CASE_INSENSITIVE);
    
    public static boolean isLinux() {
        String os_name = System.getProperty("os.name");
        Matcher matcher = patternLinux.matcher(os_name);
        return matcher.find();
    }
    
    // Determine Windows Operating System
    // ZAP: Changed to final.
    private static final Pattern patternMacOsX = Pattern.compile("mac", Pattern.CASE_INSENSITIVE);
    
    public static boolean isMacOsX() {
        String os_name = System.getProperty("os.name");
        Matcher matcher = patternMacOsX.matcher(os_name);
        return matcher.find();
    }
    
    public static void setZapHome (String dir) {
    	zapHome = dir;
    }
    
    public static String getZapHome () {
    	return zapHome;
    }

    private static String getVersionFromManifest() {
    	String className = Constant.class.getSimpleName() + ".class";
    	String classPath = Constant.class.getResource(className).toString();
    	if (!classPath.startsWith("jar")) {
    	  // Class not from JAR
    	  return DEV_VERSION;
    	}
    	String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
    	Manifest manifest;
		try {
			manifest = new Manifest(new URL(manifestPath).openStream());
	    	Attributes attr = manifest.getMainAttributes();
	    	return attr.getValue("Implementation-Version");
		} catch (Exception e) {
			// Ignore
			return DEV_VERSION;
		}
    }
    
    public static boolean isDevBuild() {
    	return isDevBuild(PROGRAM_VERSION);
    }
    
    public static boolean isDevBuild(String version) {
    	// Dev releases with be called "Dev Build" date stamped builds will be of the format D-{yyyy}-{mm}-{dd}
    	return DEV_VERSION.equals(version);
    }
    
    public static boolean isDailyBuild(String version) {
    	// Date stamped builds will be of the format D-{yyyy}-{mm}-{dd}
    	return version.startsWith("D-");
    }
    
    public static boolean isDailyBuild() {
    	return isDailyBuild(PROGRAM_VERSION);
    }
}
