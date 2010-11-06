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
package org.parosproxy.paros;

import java.io.File;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.FileCopier;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Constant {
	// ZAP: rebrand
    public static final String PROGRAM_NAME     = "ZAP";
    
//  ************************************************************
//  the config.xml MUST be set to be the same as the version_tag
//  otherwise the config.xml will be overwritten everytime.
//  ************************************************************
    public static final String PROGRAM_VERSION = "1.0.0";
    public static final long VERSION_TAG = 10000000;
//  ************************************************************
//  note the above
//  ************************************************************
    
    public static final String PROGRAM_TITLE = PROGRAM_NAME + " " + PROGRAM_VERSION;
    public static final String SYSTEM_PAROS_USER_LOG = "zap.user.log";
    
//  public static final String FILE_CONFIG = "xml/config.xml";
//  public static final String FOLDER_PLUGIN = "plugin";
//  public static final String FOLDER_FILTER = "filter";
//  public static final String FOLDER_SESSION = "session";
//    public static final String DBNAME_TEMPLATE = "db/parosdb";
//  public static final String DBNAME_UNTITLED = FOLDER_SESSION + "/untitled";

    public static final String FILE_CONFIG_DEFAULT = "xml/config.xml";
    public String FILE_CONFIG = "config.xml";
    public static final String FOLDER_PLUGIN = "plugin";
    public static final String FOLDER_FILTER = "filter";
    public static final String FOLDER_SESSION_DEFAULT = "session";
    public String FOLDER_SESSION = "session";
    public static final String DBNAME_TEMPLATE = "db" + System.getProperty("file.separator") + "zapdb";

    public static final String DBNAME_UNTITLED_DEFAULT = FOLDER_SESSION_DEFAULT + System.getProperty("file.separator") + "untitled";

    public String DBNAME_UNTITLED = FOLDER_SESSION + System.getProperty("file.separator") + "untitled";
    public String ACCEPTED_LICENSE_DEFAULT = "AcceptedLicense";
    public String ACCEPTED_LICENSE = ACCEPTED_LICENSE_DEFAULT;
    
    private static Constant instance = null;
    
    public static final int MAX_HOST_CONNECTION = 5;
    // ZAP: Dont announce ourselves
    //public static final String USER_AGENT = PROGRAM_NAME + "/" + PROGRAM_VERSION;
    public static final String USER_AGENT = "";

    private static String staticEyeCatcher = PROGRAM_NAME;
    private static boolean staticSP = false;
    private static Pattern patternWindows = Pattern.compile("window", Pattern.CASE_INSENSITIVE);
    private static Pattern patternLinux = Pattern.compile("linux", Pattern.CASE_INSENSITIVE);
    
    // ZAP: Added i18n
    public static ResourceBundle messages = null;
    
    // ZAP: Added vulnerabilities file
    public String VULNS_CONFIG = "xml/vulnerabilities.xml";
    
    // ZAP: Added dirbuster dir
    public String DIRBUSTER_DIR = "dirbuster";

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
        FileCopier copier = new FileCopier();
        File f = null;
        Log log = null;
        
        String userhome = System.getProperty("user.home");
        
        // default to use application directory 'log'
        System.setProperty(SYSTEM_PAROS_USER_LOG, "log");

        if (userhome != null && !userhome.equals("")) {
        	// ZAP: Rebrand
            userhome += System.getProperty("file.separator")+"zap";
            f = new File(userhome);
            userhome += System.getProperty("file.separator");
            FILE_CONFIG = userhome+FILE_CONFIG;
            FOLDER_SESSION = userhome+FOLDER_SESSION;
            DBNAME_UNTITLED = userhome+DBNAME_UNTITLED;
            ACCEPTED_LICENSE = userhome+ACCEPTED_LICENSE;

            try {
                
                System.setProperty(SYSTEM_PAROS_USER_LOG, userhome);
                System.setProperty("log4j.configuration","xml/log4j.properties");
                
                if (!f.isDirectory()) {
                    if (! f.mkdir() ) {
                    	// ZAP: report failure to create directory
                    	System.out.println("Failed to create directory " + f.getAbsolutePath());
                    }
                    log = LogFactory.getLog(Constant.class);
                    log.info("Created directory "+userhome);

                } else {
                    log = LogFactory.getLog(Constant.class);
                    
                }
                
                f=new File(FILE_CONFIG);
                if (!f.isFile()) {
                    log.info("Copying defaults from "+FILE_CONFIG_DEFAULT+" to "+FILE_CONFIG);
                    copier.copy(new File(FILE_CONFIG_DEFAULT),f);

                } else {
                    try {
                        
                        XMLConfiguration config = new XMLConfiguration(FILE_CONFIG);
                        config.setAutoSave(false);
                        config.load();

                        long ver = config.getLong("version");
                        if (VERSION_TAG > ver) {
                            // overwrite previous configuration file
                            copier.copy(new File(FILE_CONFIG_DEFAULT),f);                        
                        }
                    } catch (ConfigurationException e) {
                        //  if there is any error in config file (eg config file not exist),
                        //  overwrite previous configuration file 
                        copier.copy(new File(FILE_CONFIG_DEFAULT),f);                        

                    } catch (NoSuchElementException e) {
                        //  overwrite previous configuration file if config file corrupted
                        copier.copy(new File(FILE_CONFIG_DEFAULT),f);                        
                        
                    }

                }
                
                f=new File(FOLDER_SESSION);
                if (!f.isDirectory()) {
                    log.info("Creating directory "+FOLDER_SESSION);
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
            
        } else {

            System.setProperty("log4j.configuration","xml/log4j.properties");

            FILE_CONFIG = FILE_CONFIG_DEFAULT;
            FOLDER_SESSION = FOLDER_SESSION_DEFAULT;
            DBNAME_UNTITLED = DBNAME_UNTITLED_DEFAULT;
            ACCEPTED_LICENSE = ACCEPTED_LICENSE_DEFAULT;
            
        }
        
        // ZAP: Init i18n
        
        String lang = null;
        Locale locale = Locale.ENGLISH;
        try {
            // Select the correct locale
            XMLConfiguration config = new XMLConfiguration(FILE_CONFIG);
            config.setAutoSave(false);
            config.load();

            lang = config.getString(OptionsParamView.LOCALE, OptionsParamView.DEFAULT_LOCALE);
            String[] langArray = lang.split("_");
            locale = new Locale(langArray[0], langArray[1]);
        } catch (Exception e) {
        	System.out.println("SBSB Exception " + e);
        }
        
	    messages = ResourceBundle.getBundle("Messages", locale);
    }
    
    public static Constant getInstance() {
        if (instance==null) {
            instance=new Constant();
        }
        return instance;

    }
    
    public static boolean isWindows() {
        String os_name = System.getProperty("os.name");
        
        Matcher matcher = patternWindows.matcher(os_name);
        return matcher.find();
    }
    
    public static boolean isLinux() {
        String os_name = System.getProperty("os.name");
        Matcher matcher = patternLinux.matcher(os_name);
        return matcher.find();
    }
    
}
