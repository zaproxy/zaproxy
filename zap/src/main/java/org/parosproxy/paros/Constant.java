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
// ZAP: 2013/04/14 Issue 610: Replace the use of the String class for available/default "Forced
// Browse" files
// ZAP: 2013/04/15 Issue 632: Manual Request Editor dialogue (HTTP) configurations not saved
// correctly
// ZAP: 2013/12/03 Issue 933: Automatically determine install dir
// ZAP: 2013/12/13 Issue 919: Support for multiple language vulnerability files.
// ZAP: 2014/04/11 Issue 1148: ZAP 2.3.0 does not launch after upgrading in some situations
// ZAP: 2014/07/15 Issue 1265: Context import and export
// ZAP: 2014/08/14 Issue 1300: Add-ons show incorrect language when English is selected on non
// English locale
// ZAP: 2014/11/11 Issue 1406: Move online menu items to an add-on
// ZAP: 2015/01/04 Issue 1388: Not all translated files are updated when "zaplang" package is
// imported
// ZAP: 2014/01/04 Issue 1394: Import vulnerabilities.xml files when updating the translated
// resources
// ZAP: 2014/01/04 Issue 1458: Change home/installation dir paths to be always absolute
// ZAP: 2015/03/10 Issue 653: Handle updates on Kali better
// ZAP: 2015/03/30 Issue 1582: Enablers for low memory option
// ZAP: 2015/04/12 Remove "installation" fuzzers dir, no longer in use
// ZAP: 2015/08/01 Remove code duplication in catch of exceptions, use installation directory in
// default config file
// ZAP: 2015/11/11 Issue 2045: Dont copy old configs if -dir option used
// ZAP: 2015/11/26 Issue 2084: Warn users if they are probably using out of date versions
// ZAP: 2016/02/17 Convert extensions' options to not use extensions' names as XML element names
// ZAP: 2016/05/12 Use dev/weekly dir for plugin downloads when copying the existing 'release'
// config file
// ZAP: 2016/06/07 Remove commented constants and statement that had no (actual) effect, add doc to
// a constant and init other
// ZAP: 2016/06/07 Use filter directory in ZAP's home directory
// ZAP: 2016/06/13 Migrate config option "proxy.modifyAcceptEncoding"
// ZAP: 2016/07/07 Convert passive scanners options to new structure
// ZAP: 2016/09/22 JavaDoc tweaks
// ZAP: 2016/11/17 Issue 2701 Support Factory Reset
// ZAP: 2017/05/04 Issue 3440: Log Exception when overwriting a config file
// ZAP: 2017/12/26 Remove class methods no longer used.
// ZAP: 2018/01/03 No longer create filter dir and deprecate FOLDER_FILTER constant.
//                 Exit immediately if not able to create the home dir.
// ZAP: 2018/01/04 Clear SNI Terminator options when updating from older ZAP versions.
// ZAP: 2018/01/05 Prevent use of install dir as home dir.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/03/16 Use equalsIgnoreCase (Issue 4327).
// ZAP: 2018/04/16 Keep backup of malformed config file.
// ZAP: 2018/06/13 Correct install dir detection from JAR.
// ZAP: 2018/06/29 Allow to check if in dev mode.
// ZAP: 2018/07/19 Fallback to bundled config.xml and log4j.properties.
// ZAP: 2019/03/14 Move and correct update of old options.
// ZAP: 2019/04/01 Refactored to reduce code-duplication.
// ZAP: 2019/05/10 Apply installer config options on update.
//                 Fix exceptions during config update.
// ZAP: 2019/05/14 Added silent option
// ZAP: 2019/05/17 Update cert option to boolean.
// ZAP: 2019/05/29 Update Jericho log configuration.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/06/07 Update current version.
// ZAP: 2019/09/16 Deprecate ZAP_HOMEPAGE and ZAP_EXTENSIONS_PAGE.
// ZAP: 2019/11/07 Removed constants related to accepting the license.
// ZAP: 2020/01/02 Updated config version and default user agent
// ZAP: 2020/01/06 Set latest version to default config.
// ZAP: 2020/01/10 Correct the MailTo autoTagScanner regex pattern when upgrading from 2.8 or
// earlier.
// ZAP: 2020/04/22 Check ControlOverrides when determining the locale.
// ZAP: 2020/09/17 Correct the Syntax Highlighting markoccurrences config key name when upgrading
// from 2.9 or earlier.
// ZAP: 2020/10/07 Changes for Log4j 2 migration.
// ZAP: 2020/11/02 Do not backup old Log4j config if already present.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/09/15 Added support for detecting containers
// ZAP: 2021/09/21 Added support for detecting snapcraft
// ZAP: 2021/10/01 Added support for detecting WebSwing
// ZAP: 2021/10/06 Update user agent when upgrading from 2.10
// ZAP: 2022/02/03 Removed deprecated FILE_CONFIG_DEFAULT and VULNS_BASE
// ZAP: 2022/02/25 Remove options that are no longer needed.
// ZAP: 2022/05/20 Remove usage of ConnectionParam.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.FileCopier;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.control.AddOnLoader;
import org.zaproxy.zap.control.ControlOverrides;
import org.zaproxy.zap.extension.autoupdate.OptionsParamCheckForUpdates;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public final class Constant {
    // ZAP: rebrand
    public static final String PROGRAM_NAME = "OWASP ZAP";
    public static final String PROGRAM_NAME_SHORT = "ZAP";
    /** @deprecated (2.9.0) Do not use, it will be removed. */
    @Deprecated public static final String ZAP_HOMEPAGE = "http://www.owasp.org/index.php/ZAP";
    /** @deprecated (2.9.0) Do not use, it will be removed. */
    @Deprecated
    public static final String ZAP_EXTENSIONS_PAGE = "https://github.com/zaproxy/zap-extensions";

    public static final String ZAP_TEAM = "ZAP Dev Team";
    public static final String PAROS_TEAM = "Chinotec Technologies";

    //  ************************************************************
    //  the config.xml MUST be set to be the same as the version_tag
    //  otherwise the config.xml will be overwritten everytime.
    //  ************************************************************
    private static final String DEV_VERSION = "Dev Build";
    public static final String ALPHA_VERSION = "alpha";
    public static final String BETA_VERSION = "beta";

    private static final String VERSION_ELEMENT = "version";

    // Accessible for tests
    static final long VERSION_TAG = 20012000;

    // Old version numbers - for upgrade
    private static final long V_2_11_1_TAG = 20011001;
    private static final long V_2_9_0_TAG = 2009000;
    private static final long V_2_8_0_TAG = 2008000;
    private static final long V_2_7_0_TAG = 2007000;
    private static final long V_2_5_0_TAG = 2005000;
    private static final long V_2_4_3_TAG = 2004003;
    private static final long V_2_3_1_TAG = 2003001;
    private static final long V_2_2_2_TAG = 2002002;
    private static final long V_2_2_0_TAG = 2002000;
    private static final long V_2_1_0_TAG = 2001000;
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

    // These are no longer final - version is now loaded from the manifest file
    public static String PROGRAM_VERSION = DEV_VERSION;
    public static String PROGRAM_TITLE = PROGRAM_NAME + " " + PROGRAM_VERSION;

    public static final String SYSTEM_PAROS_USER_LOG = "zap.user.log";

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String FILE_CONFIG_NAME = "config.xml";
    public static final String FOLDER_PLUGIN = "plugin";
    /**
     * The name of the directory for filter related files (the path should be built using {@link
     * #getZapHome()} as the parent directory).
     *
     * @deprecated (2.8.0) Should not be used, the filter functionality is deprecated (replaced by
     *     scripts and Replacer add-on).
     * @since 1.0.0
     */
    @Deprecated public static final String FOLDER_FILTER = "filter";

    /**
     * The name of the directory where the (file) sessions are saved by default.
     *
     * @since 1.0.0
     */
    public static final String FOLDER_SESSION_DEFAULT = "session";

    public static final String DBNAME_TEMPLATE =
            "db" + System.getProperty("file.separator") + "zapdb";

    /**
     * Prefix (file name) of Messages.properties files.
     *
     * @see #MESSAGES_EXTENSION
     */
    public static final String MESSAGES_PREFIX = "Messages";

    /**
     * Extension (with dot) of Messages.properties files.
     *
     * @see #MESSAGES_PREFIX
     * @since 2.4.0
     */
    public static final String MESSAGES_EXTENSION = ".properties";

    public static final String DBNAME_UNTITLED_DEFAULT =
            FOLDER_SESSION_DEFAULT + System.getProperty("file.separator") + "untitled";

    public String FILE_CONFIG = FILE_CONFIG_NAME;
    public String FOLDER_SESSION = FOLDER_SESSION_DEFAULT;
    public String DBNAME_UNTITLED =
            FOLDER_SESSION + System.getProperty("file.separator") + "untitled";

    public static final String FILE_PROGRAM_SPLASH = "resource/zap128x128.png";

    // Accelerator keys - Default: Windows
    public static String ACCELERATOR_UNDO = "control Z";
    public static String ACCELERATOR_REDO = "control Y";
    public static String ACCELERATOR_TRIGGER_KEY = "Control";

    private static Constant instance = null;

    public static final int MAX_HOST_CONNECTION = 15;
    public static final int MAX_THREADS_PER_SCAN = 50;
    // ZAP: Dont announce ourselves
    // public static final String USER_AGENT = PROGRAM_NAME + "/" + PROGRAM_VERSION;
    public static final String USER_AGENT = "";

    private static String staticEyeCatcher = "0W45pz4p";

    private static final String USER_CONTEXTS_DIR = "contexts";
    private static final String USER_POLICIES_DIR = "policies";

    private static final String ZAP_CONTAINER_FILE = "/zap/container";
    private static final String FLATPAK_FILE = "/.flatpak-info";
    public static final String FLATPAK_NAME = "flatpak";
    private static final String SNAP_FILE = "meta/snap.yaml";
    public static final String SNAP_NAME = "snapcraft";
    private static final String HOME_ENVVAR = "HOME";
    public static final String WEBSWING_NAME = "webswing";

    //
    // Home dir for ZAP, i.e. where the config file is. Can be set on cmdline, otherwise will be set
    // to default loc
    private static String zapHome = null;
    // Default home dir for 'full' releases - used for copying full conf file when dev/daily release
    // run for the first time
    // and also for the JVM options config file
    private static String zapStd = null;
    // Install dir for ZAP, but default will be cwd
    private static String zapInstall = null;

    private static Boolean onKali = null;
    private static Boolean onBackBox = null;
    private static Boolean inContainer = null;
    private static String containerName;
    private static Boolean lowMemoryOption = null;

    // ZAP: Added i18n
    public static I18N messages = null;

    /**
     * The system's locale (as determined by the JVM at startup, {@code Locale#getDefault()}).
     *
     * <p>The locale is kept here because the default locale is later overridden with the user's
     * chosen locale/language.
     *
     * @see Locale#getDefault()
     */
    private static final Locale SYSTEMS_LOCALE = Locale.getDefault();

    /** The path to bundled (in zap.jar) config.xml file. */
    private static final String PATH_BUNDLED_CONFIG_XML =
            "/org/zaproxy/zap/resources/" + FILE_CONFIG_NAME;

    /**
     * Name of directory that contains the (source and translated) resource files.
     *
     * @see #MESSAGES_PREFIX
     * @see #VULNERABILITIES_PREFIX
     */
    public static final String LANG_DIR = "lang";

    /**
     * Prefix (file name) of vulnerabilities.xml files.
     *
     * @see #VULNERABILITIES_EXTENSION
     * @since 2.4.0
     */
    public static final String VULNERABILITIES_PREFIX = "vulnerabilities";

    /**
     * Extension (with dot) of vulnerabilities.xml files.
     *
     * @see #VULNERABILITIES_PREFIX
     * @since 2.4.0
     */
    public static final String VULNERABILITIES_EXTENSION = ".xml";

    /**
     * Flag that indicates whether or not the "dev mode" is enabled.
     *
     * @see #isDevMode()
     */
    private static boolean devMode;

    private static boolean silent;

    // ZAP: Added dirbuster dir
    public String DIRBUSTER_DIR = "dirbuster";
    public String DIRBUSTER_CUSTOM_DIR = DIRBUSTER_DIR;

    public String FUZZER_DIR = "fuzzers";

    public static String FOLDER_LOCAL_PLUGIN = FOLDER_PLUGIN;

    public static final URL OK_FLAG_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/072.png"); // Green
    public static final URL INFO_FLAG_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/073.png"); // Blue
    public static final URL LOW_FLAG_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/074.png"); // Yellow
    public static final URL MED_FLAG_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/076.png"); // Orange
    public static final URL HIGH_FLAG_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/071.png"); // Red
    public static final URL BLANK_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/blank.png");
    public static final URL SPIDER_IMAGE_URL =
            Constant.class.getResource("/resource/icon/10/spider.png");

    private static Logger LOG = LogManager.getLogger(Constant.class);

    public static String getEyeCatcher() {
        return staticEyeCatcher;
    }

    public static void setEyeCatcher(String eyeCatcher) {
        staticEyeCatcher = eyeCatcher;
    }

    public Constant() {
        this(null);
    }

    private Constant(ControlOverrides overrides) {
        initializeFilesAndDirectories(overrides);
        setAcceleratorKeys();
    }

    public static String getDefaultHomeDirectory(boolean incDevOption) {
        if (zapStd == null) {
            zapStd = System.getProperty("user.home");
            if (zapStd == null) {
                zapStd = ".";
            }

            if (isLinux()) {
                // Linux: Hidden Zap directory in the user's home directory
                zapStd += FILE_SEPARATOR + "." + PROGRAM_NAME_SHORT;
            } else if (isMacOsX()) {
                // Mac Os X: Support for writing the configuration into the users Library
                zapStd +=
                        FILE_SEPARATOR
                                + "Library"
                                + FILE_SEPARATOR
                                + "Application Support"
                                + FILE_SEPARATOR
                                + PROGRAM_NAME_SHORT;
            } else {
                // Windows: Zap directory in the user's home directory
                zapStd += FILE_SEPARATOR + PROGRAM_NAME;
            }
        }

        if (incDevOption) {
            if (isDevMode() || isDailyBuild()) {
                // Default to a different home dir to prevent messing up full releases
                return zapStd + "_D";
            }
        }
        return zapStd;
    }

    public void copyDefaultConfigs(File f, boolean forceReset)
            throws IOException, ConfigurationException {
        FileCopier copier = new FileCopier();
        File oldf;
        if (isDevMode() || isDailyBuild()) {
            // try standard location
            oldf = new File(getDefaultHomeDirectory(false) + FILE_SEPARATOR + FILE_CONFIG_NAME);
        } else {
            // try old location
            oldf = new File(zapHome + FILE_SEPARATOR + "zap" + FILE_SEPARATOR + FILE_CONFIG_NAME);
        }

        if (!forceReset
                && oldf.exists()
                && Paths.get(zapHome).equals(Paths.get(getDefaultHomeDirectory(true)))) {
            // Dont copy old configs if forcedReset or they've specified a non std directory
            LOG.info("Copying defaults from {} to {}", oldf.getAbsolutePath(), FILE_CONFIG);
            copier.copy(oldf, f);

            if (isDevMode() || isDailyBuild()) {
                ZapXmlConfiguration newConfig = new ZapXmlConfiguration(f);
                newConfig.setProperty(
                        OptionsParamCheckForUpdates.DOWNLOAD_DIR, Constant.FOLDER_LOCAL_PLUGIN);
                newConfig.save();
            }
        } else {
            LOG.info("Copying default configuration to {}", FILE_CONFIG);
            copyDefaultConfigFile();
        }
    }

    private void copyDefaultConfigFile() throws IOException {
        Path configFile = Paths.get(FILE_CONFIG);
        copyFileToHome(configFile, "xml/" + FILE_CONFIG_NAME, PATH_BUNDLED_CONFIG_XML);
        try {
            setLatestVersion(new ZapXmlConfiguration(configFile.toFile()));
        } catch (ConfigurationException e) {
            throw new IOException("Failed to set the latest version:", e);
        }
    }

    /**
     * Sets the latest version ({@link #VERSION_TAG}) to the given configuration and then saves it.
     *
     * @param config the configuration to change
     * @throws ConfigurationException if an error occurred while saving the configuration.
     */
    private static void setLatestVersion(XMLConfiguration config) throws ConfigurationException {
        config.setProperty(VERSION_ELEMENT, VERSION_TAG);
        config.save();
    }

    private static void copyFileToHome(
            Path targetFile, String sourceFilePath, String fallbackResource) throws IOException {
        Path defaultConfig = Paths.get(getZapInstall(), sourceFilePath);
        if (Files.exists(defaultConfig)) {
            Files.copy(defaultConfig, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } else {
            try (InputStream is = Constant.class.getResourceAsStream(fallbackResource)) {
                if (is == null) {
                    throw new IOException("Bundled resource not found: " + fallbackResource);
                }
                Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static URL getUrlDefaultConfigFile() {
        Path path = getPathDefaultConfigFile();
        if (Files.exists(path)) {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                LOG.debug("Failed to convert file system path:", e);
            }
        }
        return Constant.class.getResource(PATH_BUNDLED_CONFIG_XML);
    }

    public void initializeFilesAndDirectories() {
        initializeFilesAndDirectories(null);
    }

    private void initializeFilesAndDirectories(ControlOverrides overrides) {

        FileCopier copier = new FileCopier();
        File f = null;

        // Set up the version from the manifest
        PROGRAM_VERSION = getVersionFromManifest();
        PROGRAM_TITLE = PROGRAM_NAME + " " + PROGRAM_VERSION;

        if (zapHome == null) {
            zapHome = getDefaultHomeDirectory(true);
        }

        zapHome = getAbsolutePath(zapHome);
        f = new File(zapHome);

        FILE_CONFIG = zapHome + FILE_CONFIG;
        FOLDER_SESSION = zapHome + FOLDER_SESSION;
        DBNAME_UNTITLED = zapHome + DBNAME_UNTITLED;
        DIRBUSTER_CUSTOM_DIR = zapHome + DIRBUSTER_DIR;
        FUZZER_DIR = zapHome + FUZZER_DIR;
        FOLDER_LOCAL_PLUGIN = zapHome + FOLDER_PLUGIN;

        try {
            System.setProperty(SYSTEM_PAROS_USER_LOG, zapHome);

            if (!f.isDirectory()) {
                if (f.exists()) {
                    System.err.println("The home path is not a directory: " + zapHome);
                    System.exit(1);
                }
                if (!f.mkdir()) {
                    System.err.println("Unable to create home directory: " + zapHome);
                    System.err.println("Is the path correct and there's write permission?");
                    System.exit(1);
                }
            } else if (!f.canWrite()) {
                System.err.println("The home path is not writable: " + zapHome);
                System.exit(1);
            } else {
                Path installDir = Paths.get(getZapInstall()).toRealPath();
                if (installDir.equals(Paths.get(zapHome).toRealPath())) {
                    System.err.println(
                            "The install dir should not be used as home dir: " + installDir);
                    System.exit(1);
                }
            }

            setUpLogging();

            f = new File(FILE_CONFIG);
            if (!f.isFile()) {
                this.copyDefaultConfigs(f, false);
            }

            f = new File(FOLDER_SESSION);
            if (!f.isDirectory()) {
                LOG.info("Creating directory {}", FOLDER_SESSION);
                if (!f.mkdir()) {
                    // ZAP: report failure to create directory
                    System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            f = new File(DIRBUSTER_CUSTOM_DIR);
            if (!f.isDirectory()) {
                LOG.info("Creating directory {}", DIRBUSTER_CUSTOM_DIR);
                if (!f.mkdir()) {
                    // ZAP: report failure to create directory
                    System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            f = new File(FUZZER_DIR);
            if (!f.isDirectory()) {
                LOG.info("Creating directory {}", FUZZER_DIR);
                if (!f.mkdir()) {
                    // ZAP: report failure to create directory
                    System.out.println("Failed to create directory " + f.getAbsolutePath());
                }
            }
            f = new File(FOLDER_LOCAL_PLUGIN);
            if (!f.isDirectory()) {
                LOG.info("Creating directory {}", FOLDER_LOCAL_PLUGIN);
                if (!f.mkdir()) {
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

                // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding
                // when reading/writing configurations.
                XMLConfiguration config = new ZapXmlConfiguration(FILE_CONFIG);
                config.setAutoSave(false);

                long ver = config.getLong(VERSION_ELEMENT);

                if (ver == VERSION_TAG) {
                    // Nothing to do
                } else if (isDevMode() || isDailyBuild()) {
                    // Nothing to do
                } else {
                    // Backup the old one
                    LOG.info("Backing up config file to {}.bak", FILE_CONFIG);
                    f = new File(FILE_CONFIG);
                    try {
                        copier.copy(f, new File(FILE_CONFIG + ".bak"));
                    } catch (IOException e) {
                        String msg =
                                "Failed to backup config file "
                                        + FILE_CONFIG
                                        + " to "
                                        + FILE_CONFIG
                                        + ".bak "
                                        + e.getMessage();
                        System.err.println(msg);
                        LOG.error(msg, e);
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
                    if (ver <= V_2_1_0_TAG) {
                        // Nothing to do
                    }
                    if (ver <= V_2_2_0_TAG) {
                        upgradeFrom2_2_0(config);
                    }
                    if (ver <= V_2_2_2_TAG) {
                        upgradeFrom2_2_2(config);
                    }
                    if (ver <= V_2_3_1_TAG) {
                        upgradeFrom2_3_1(config);
                    }
                    if (ver <= V_2_4_3_TAG) {
                        upgradeFrom2_4_3(config);
                    }
                    if (ver <= V_2_5_0_TAG) {
                        upgradeFrom2_5_0(config);
                    }
                    if (ver <= V_2_7_0_TAG) {
                        upgradeFrom2_7_0(config);
                    }
                    if (ver <= V_2_8_0_TAG) {
                        upgradeFrom2_8_0(config);
                    }
                    if (ver <= V_2_9_0_TAG) {
                        upgradeFrom2_9_0(config);
                    }
                    if (ver <= V_2_11_1_TAG) {
                        upgradeFrom2_11_1(config);
                    }

                    // Execute always to pick installer choices.
                    updateCfuFromDefaultConfig(config);

                    LOG.info("Upgraded from {}", ver);

                    setLatestVersion(config);
                }

            } catch (ConfigurationException | ConversionException | NoSuchElementException e) {
                handleMalformedConfigFile(e);
            }
        } catch (Exception e) {
            System.err.println(
                    "Unable to upgrade config file " + FILE_CONFIG + " " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // ZAP: Init i18n
        Locale locale = loadLocale(overrides);

        Locale.setDefault(locale);

        messages = new I18N(locale);
    }

    private Locale loadLocale(ControlOverrides overrides) {
        try {
            String lang = null;
            if (overrides != null) {
                lang = overrides.getOrderedConfigs().get(OptionsParamView.LOCALE);
            }
            if (lang == null || lang.isEmpty()) {
                XMLConfiguration config = new ZapXmlConfiguration(FILE_CONFIG);
                config.setAutoSave(false);

                lang = config.getString(OptionsParamView.LOCALE, OptionsParamView.DEFAULT_LOCALE);
                if (lang.length() == 0) {
                    lang = OptionsParamView.DEFAULT_LOCALE;
                }
            }

            String[] langArray = lang.split("_");
            return new Locale(langArray[0], langArray[1]);

        } catch (Exception e) {
            System.out.println("Failed to load locale " + e);
        }
        return Locale.ENGLISH;
    }

    private void setUpLogging() throws IOException {
        backupLegacyLog4jConfig();

        String fileName = "log4j2.properties";
        File logFile = new File(zapHome, fileName);
        if (!logFile.exists()) {
            String defaultConfig = "/org/zaproxy/zap/resources/log4j2-home.properties";
            copyFileToHome(logFile.toPath(), "xml/" + fileName, defaultConfig);
        }

        Configurator.reconfigure(logFile.toURI());
    }

    private static void backupLegacyLog4jConfig() {
        String fileName = "log4j.properties";
        Path backupLegacyConfig = Paths.get(zapHome, fileName + ".bak");
        if (Files.exists(backupLegacyConfig)) {
            logAndPrintInfo("Ignoring legacy log4j.properties file, backup already exists.");
            return;
        }

        Path legacyConfig = Paths.get(zapHome, fileName);
        if (Files.exists(legacyConfig)) {
            logAndPrintInfo("Creating backup of legacy log4j.properties file...");
            try {
                Files.move(legacyConfig, backupLegacyConfig);
            } catch (IOException e) {
                logAndPrintError("Failed to backup legacy Log4j configuration file:", e);
            }
        }
    }

    private void handleMalformedConfigFile(Exception e) throws IOException {
        logAndPrintError("Failed to load/upgrade config file:", e);
        try {
            Path backupPath = Paths.get(zapHome, "config-" + Math.random() + ".xml.bak");
            logAndPrintInfo("Creating back up for user inspection: " + backupPath);
            Files.copy(Paths.get(FILE_CONFIG), backupPath);
            logAndPrintInfo("Back up successfully created.");
        } catch (IOException ioe) {
            logAndPrintError("Failed to backup file:", ioe);
        }
        logAndPrintInfo("Using default config file...");
        copyDefaultConfigFile();
    }

    private static void logAndPrintError(String message, Exception e) {
        LOG.error(message, e);
        System.err.println(message);
        e.printStackTrace();
    }

    private static void logAndPrintInfo(String message) {
        LOG.info(message);
        System.out.println(message);
    }

    private void copyProperty(XMLConfiguration fromConfig, XMLConfiguration toConfig, String key) {
        toConfig.setProperty(key, fromConfig.getProperty(key));
    }

    private void copyAllProperties(
            XMLConfiguration fromConfig, XMLConfiguration toConfig, String prefix) {
        Iterator<String> iter = fromConfig.getKeys(prefix);
        while (iter.hasNext()) {
            String key = iter.next();
            copyProperty(fromConfig, toConfig, key);
        }
    }

    private void upgradeFrom1_1_0(XMLConfiguration config) throws ConfigurationException {
        // Upgrade the regexs
        // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding when
        // reading/writing configurations.
        XMLConfiguration newConfig = new ZapXmlConfiguration(getUrlDefaultConfigFile());
        newConfig.setAutoSave(false);

        copyAllProperties(newConfig, config, "pscans");
    }

    private void upgradeFrom1_2_0(XMLConfiguration config) throws ConfigurationException {
        // Upgrade the regexs
        // ZAP: Changed to use ZapXmlConfiguration, to enforce the same character encoding when
        // reading/writing configurations.
        XMLConfiguration newConfig = new ZapXmlConfiguration(getUrlDefaultConfigFile());
        newConfig.setAutoSave(false);

        copyProperty(newConfig, config, "view.brkPanelView");
        copyProperty(newConfig, config, "view.showMainToolbar");
    }

    private void upgradeFrom1_4_1(XMLConfiguration config) {
        // As the POST_FORM option for the spider has been updated from int to boolean, keep
        // compatibility for old versions
        Object postForm = config.getProperty("spider.postform");
        if (postForm != null) {
            boolean enabled = !"0".equals(postForm.toString());
            config.setProperty("spider.postform", enabled);
            config.setProperty("spider.processform", enabled);
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
            data[4] = config.getBoolean(baseKey + "output", true);
            data[5] = config.getBoolean(baseKey + "note", false);
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
            data[7] = config.getBoolean(baseKey + "enabled");
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

    private void upgradeFrom2_2_0(XMLConfiguration config) {
        try {
            if (config.getInt(OptionsParamCheckForUpdates.CHECK_ON_START, 0) == 0) {
                /*
                 * Check-for-updates on start disabled - force another prompt to ask the user,
                 * as this option can have been unset incorrectly before.
                 * And we want to encourage users to use this ;)
                 */
                config.setProperty(OptionsParamCheckForUpdates.DAY_LAST_CHECKED, "");
            }
        } catch (ConversionException e) {
            LOG.debug(
                    "The option {} is not an int.", OptionsParamCheckForUpdates.CHECK_ON_START, e);
        }
        // Clear the block list - addons were incorrectly added to this if an update failed
        config.setProperty(AddOnLoader.ADDONS_BLOCK_LIST, "");
    }

    private void upgradeFrom2_2_2(XMLConfiguration config) {
        try {
            // Change the type of the option from int to boolean.
            int oldValue = config.getInt(OptionsParamCheckForUpdates.CHECK_ON_START, 1);
            config.setProperty(OptionsParamCheckForUpdates.CHECK_ON_START, oldValue != 0);
        } catch (ConversionException e) {
            LOG.debug(
                    "The option {} is no longer an int.",
                    OptionsParamCheckForUpdates.CHECK_ON_START,
                    e);
        }
    }

    private void upgradeFrom2_3_1(XMLConfiguration config) {
        // Remove old authentication options no longer used
        config.clearProperty("connection.confirmRemoveAuth");
        config.clearTree("options.auth");
    }

    private void upgradeFrom2_4_3(XMLConfiguration config) {
        List<Object[]> oldData = new ArrayList<>();
        // Convert extensions' options to not use extensions' names as XML element names
        for (Iterator<String> it = config.getKeys("ext"); it.hasNext(); ) {
            String key = it.next();

            Object[] data = new Object[2];
            data[0] = key.substring(4);
            data[1] = config.getBoolean(key);
            oldData.add(data);
        }
        config.clearTree("ext");

        for (int i = 0, size = oldData.size(); i < size; ++i) {
            String elementBaseKey = "extensions.extension(" + i + ").";
            Object[] data = oldData.get(i);

            config.setProperty(elementBaseKey + "name", data[0]);
            config.setProperty(elementBaseKey + "enabled", data[1]);
        }
    }

    private static void upgradeFrom2_5_0(XMLConfiguration config) {
        String oldConfigKey = "proxy.modifyAcceptEncoding";
        config.setProperty(
                "proxy.removeUnsupportedEncodings", config.getBoolean(oldConfigKey, true));
        config.clearProperty(oldConfigKey);

        // Convert passive scanners options to new structure
        Set<String> classnames = new HashSet<>();
        for (Iterator<String> it = config.getKeys(); it.hasNext(); ) {
            String key = it.next();
            if (!key.startsWith("pscans.org")) {
                continue;
            }
            classnames.add(key.substring(7, key.lastIndexOf('.')));
        }

        List<Object[]> oldData = new ArrayList<>();
        for (String classname : classnames) {
            Object[] data = new Object[3];
            data[0] = classname;
            data[1] = config.getBoolean("pscans." + classname + ".enabled", true);
            data[2] = config.getString("pscans." + classname + ".level", "");
            oldData.add(data);
        }

        config.clearTree("pscans.org");

        for (int i = 0, size = oldData.size(); i < size; ++i) {
            String elementBaseKey = "pscans.pscanner(" + i + ").";
            Object[] data = oldData.get(i);

            config.setProperty(elementBaseKey + "classname", data[0]);
            config.setProperty(elementBaseKey + "enabled", data[1]);
            config.setProperty(elementBaseKey + "level", data[2]);
        }
    }

    private static void upgradeFrom2_7_0(XMLConfiguration config) {
        // Remove options from SNI Terminator.
        config.clearTree("sniterm");

        String certUseKey = "certificate.use";
        try {
            // Change the type of the option from int to boolean.
            int oldValue = config.getInt(certUseKey, 0);
            config.setProperty(certUseKey, oldValue != 0);
        } catch (ConversionException e) {
            LOG.debug("The option {} is no longer an int.", certUseKey, e);
        }
    }

    private static void upgradeFrom2_8_0(XMLConfiguration config) {
        updatePscanTagMailtoPattern(config);
    }

    static void upgradeFrom2_9_0(XMLConfiguration config) {
        String oldKeyName = ".markocurrences"; // This is the typo we want to fix
        String newKeyName = ".markoccurrences";
        config.getKeys()
                .forEachRemaining(
                        key -> {
                            if (key.endsWith(oldKeyName)) {
                                config.setProperty(
                                        key.replace(oldKeyName, newKeyName),
                                        config.getProperty(key));
                                config.clearProperty(key);
                            }
                        });
        // Use new Look and Feel
        config.setProperty(
                OptionsParamView.LOOK_AND_FEEL, OptionsParamView.DEFAULT_LOOK_AND_FEEL_NAME);
        config.setProperty(
                OptionsParamView.LOOK_AND_FEEL_CLASS, OptionsParamView.DEFAULT_LOOK_AND_FEEL_CLASS);
    }

    private static void upgradeFrom2_11_1(XMLConfiguration config) {
        config.setProperty("view.largeRequest", null);
        config.setProperty("view.largeResponse", null);
        config.setProperty("hud.enableTelemetry", null);
    }

    private static void updatePscanTagMailtoPattern(XMLConfiguration config) {
        String autoTagScannersKey = "pscans.autoTagScanners.scanner";
        List<HierarchicalConfiguration> tagScanners = config.configurationsAt(autoTagScannersKey);
        String badPattern = "<.*href\\s*['\"]?mailto:";
        String goodPattern = "<.*href\\s*=\\s*['\"]?mailto:";

        for (int i = 0, size = tagScanners.size(); i < size; ++i) {
            String currentKeyResBodyRegex = autoTagScannersKey + "(" + i + ").resBodyRegex";
            if (config.getProperty(currentKeyResBodyRegex).equals(badPattern)) {
                config.setProperty(currentKeyResBodyRegex, goodPattern);
                break;
            }
        }
    }

    private static void updateCfuFromDefaultConfig(XMLConfiguration config) {
        Path path = getPathDefaultConfigFile();
        if (!Files.exists(path)) {
            return;
        }

        ZapXmlConfiguration defaultConfig;
        try {
            defaultConfig = new ZapXmlConfiguration(path.toFile());
        } catch (ConfigurationException e) {
            logAndPrintError("Failed to read default configuration file " + path, e);
            return;
        }

        copyPropertyIfSet(defaultConfig, config, "start.checkForUpdates");
        copyPropertyIfSet(defaultConfig, config, "start.downloadNewRelease");
        copyPropertyIfSet(defaultConfig, config, "start.checkAddonUpdates");
        copyPropertyIfSet(defaultConfig, config, "start.installAddonUpdates");
        copyPropertyIfSet(defaultConfig, config, "start.installScannerRules");
        copyPropertyIfSet(defaultConfig, config, "start.reportReleaseAddons");
        copyPropertyIfSet(defaultConfig, config, "start.reportBetaAddons");
        copyPropertyIfSet(defaultConfig, config, "start.reportAlphaAddons");
    }

    private static void copyPropertyIfSet(XMLConfiguration from, XMLConfiguration to, String key) {
        Object value = from.getProperty(key);
        if (value != null) {
            to.setProperty(key, value);
        }
    }

    public static void setLocale(String loc) {
        String[] langArray = loc.split("_");
        Locale locale = new Locale(langArray[0], langArray[1]);

        Locale.setDefault(locale);
        if (messages == null) {
            messages = new I18N(locale);
        } else {
            messages.setLocale(locale);
        }
    }

    public static Locale getLocale() {
        return messages.getLocal();
    }

    /**
     * Returns the system's {@code Locale} (as determined by the JVM at startup, {@link
     * Locale#getDefault()}). Should be used to show locale dependent information in the system's
     * locale.
     *
     * <p><strong>Note:</strong> The default locale is overridden with the ZAP's user defined
     * locale/language.
     *
     * @return the system's {@code Locale}
     * @see Locale#setDefault(Locale)
     */
    public static Locale getSystemsLocale() {
        return SYSTEMS_LOCALE;
    }

    public static Constant getInstance() {
        if (instance == null) {
            // ZAP: Changed to use the method createInstance().
            createInstance(null);
        }
        return instance;
    }

    public static synchronized void createInstance(ControlOverrides overrides) {
        if (instance == null) {
            instance = new Constant(overrides);
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
    private static final Pattern patternWindows =
            Pattern.compile("window", Pattern.CASE_INSENSITIVE);

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

    public static void setZapHome(String dir) {
        zapHome = getAbsolutePath(dir);
    }

    /**
     * Returns the absolute path for the given {@code directory}.
     *
     * <p>The path is terminated with a separator.
     *
     * @param directory the directory whose path will be made absolute
     * @return the absolute path for the given {@code directory}, terminated with a separator
     * @since 2.4.0
     */
    private static String getAbsolutePath(String directory) {
        String realPath = Paths.get(directory).toAbsolutePath().toString();
        String separator = FileSystems.getDefault().getSeparator();
        if (!realPath.endsWith(separator)) {
            realPath += separator;
        }
        return realPath;
    }

    public static String getZapHome() {
        return zapHome;
    }

    /**
     * Returns the path to default configuration file, located in installation directory.
     *
     * <p><strong>Note:</strong> The default configuration file is not guaranteed to exist, for
     * example, when running just with ZAP JAR.
     *
     * @return the {@code Path} to default configuration file.
     * @since 2.4.2
     */
    public static Path getPathDefaultConfigFile() {
        return Paths.get(getZapInstall(), "xml", FILE_CONFIG_NAME);
    }

    public static File getContextsDir() {
        return getFromHomeDir(USER_CONTEXTS_DIR);
    }

    public static File getPoliciesDir() {
        return getFromHomeDir(USER_POLICIES_DIR);
    }

    private static File getFromHomeDir(String subDir) {
        Path path = Paths.get(Constant.getZapHome(), subDir);

        try {
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
        }

        if (Files.isDirectory(path) && Files.isWritable(path)) {
            return path.toFile();
        }
        return Model.getSingleton().getOptionsParam().getUserDirectory();
    }

    public static void setZapInstall(String dir) {
        zapInstall = getAbsolutePath(dir);
    }

    public static String getZapInstall() {
        if (zapInstall == null) {
            String path = ".";
            Path localDir = Paths.get(path);
            if (!Files.isDirectory(localDir.resolve("db"))
                    || !Files.isDirectory(localDir.resolve("lang"))) {
                try {
                    Path sourceLocation =
                            Paths.get(
                                    ZAP.class
                                            .getProtectionDomain()
                                            .getCodeSource()
                                            .getLocation()
                                            .toURI());
                    if (!Files.isDirectory(sourceLocation)) {
                        sourceLocation = sourceLocation.getParent();
                    }
                    path = sourceLocation.toString();
                } catch (URISyntaxException e) {
                    System.err.println(
                            "Failed to determine the ZAP installation dir: " + e.getMessage());
                    path = localDir.toAbsolutePath().toString();
                }
                // Loggers wont have been set up yet
                System.out.println("Defaulting ZAP install dir to " + path);
            }

            zapInstall = getAbsolutePath(path);
        }
        return zapInstall;
    }

    private static Manifest getManifest() {
        String className = Constant.class.getSimpleName() + ".class";
        String classPath = Constant.class.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return null;
        }
        String manifestPath =
                classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        try {
            return new Manifest(new URL(manifestPath).openStream());
        } catch (Exception e) {
            // Ignore
            return null;
        }
    }

    private static String getVersionFromManifest() {
        Manifest manifest = getManifest();
        if (manifest != null) {
            Attributes attr = manifest.getMainAttributes();
            return attr.getValue("Implementation-Version");
        } else {
            return DEV_VERSION;
        }
    }

    public static Date getReleaseCreateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Manifest manifest = getManifest();
        if (manifest != null) {
            Attributes attr = manifest.getMainAttributes();
            try {
                return sdf.parse(attr.getValue("Create-Date"));
            } catch (ParseException e) {
                // Ignore - treat as undated
            }
        }
        return null;
    }

    public static Date getInstallDate() {
        String className = Constant.class.getSimpleName() + ".class";
        String classPath = Constant.class.getResource(className).toString();
        if (!classPath.startsWith("jar:file:")) {
            // Class not from JAR
            return null;
        }
        classPath = classPath.substring(9);
        int ind = classPath.indexOf("!");
        if (ind > 0) {
            classPath = classPath.substring(0, ind);
        }
        File f = new File(classPath);
        if (f.exists()) {
            // Choose the parent directories date, in case the creation date was maintained
            return new Date(f.getParentFile().lastModified());
        }
        return null;
    }

    /**
     * Tells whether or not the "dev mode" should be enabled.
     *
     * <p>Should be used to enable development related utilities/functionalities.
     *
     * @return {@code true} if the "dev mode" should be enabled, {@code false} otherwise.
     * @since 2.8.0
     */
    public static boolean isDevMode() {
        return devMode || isDevBuild();
    }

    /**
     * Sets whether or not the "dev mode" should be enabled.
     *
     * <p><strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param devMode {@code true} if the "dev mode" should be enabled, {@code false} otherwise.
     */
    public static void setDevMode(boolean devMode) {
        Constant.devMode = devMode;
    }

    /**
     * Sets whether or not ZAP should be 'silent' i.e. not make any unsolicited requests.
     *
     * <p><strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param silent {@code true} if ZAP should be silent, {@code false} otherwise.
     * @since 2.8.0
     */
    public static void setSilent(boolean silent) {
        Constant.silent = silent;
    }

    /**
     * Tells whether or not ZAP is running from a dev build.
     *
     * @return {@code true} if it's a dev build, {@code false} otherwise.
     * @see #isDevMode()
     */
    public static boolean isDevBuild() {
        return isDevBuild(PROGRAM_VERSION);
    }

    public static boolean isDevBuild(String version) {
        // Dev releases with be called "Dev Build" date stamped builds will be of the format
        // D-{yyyy}-{mm}-{dd}
        return DEV_VERSION.equals(version);
    }

    public static boolean isDailyBuild(String version) {
        // Date stamped builds will be of the format D-{yyyy}-{mm}-{dd}
        return version.startsWith("D-");
    }

    public static boolean isDailyBuild() {
        return isDailyBuild(PROGRAM_VERSION);
    }

    /**
     * If true then ZAP should not make any unsolicited requests, e.g. check-for-updates
     *
     * @return true if ZAP should not make any unsolicited requests, e.g. check-for-updates
     * @since 2.8.0
     */
    public static boolean isSilent() {
        return silent;
    }

    public static void setLowMemoryOption(boolean lowMem) {
        if (lowMemoryOption != null) {
            throw new InvalidParameterException(
                    "Low memory option already set to " + lowMemoryOption);
        }
        lowMemoryOption = lowMem;
    }

    public static boolean isLowMemoryOptionSet() {
        return lowMemoryOption != null && lowMemoryOption;
    }

    /**
     * Tells whether or not ZAP is running in Kali (and it's not a daily build).
     *
     * @return {@code true} if running in Kali (and it's not daily build), {@code false} otherwise
     */
    public static boolean isKali() {
        if (onKali == null) {
            onKali = Boolean.FALSE;
            File osReleaseFile = new File("/etc/os-release");
            if (isLinux() && !isDailyBuild() && osReleaseFile.exists()) {
                // Ignore the fact we're on Kali if this is a daily build - they will only have been
                // installed manually
                try (InputStream in = Files.newInputStream(osReleaseFile.toPath())) {
                    Properties osProps = new Properties();
                    osProps.load(in);
                    String osLikeValue = osProps.getProperty("ID");
                    if (osLikeValue != null) {
                        String[] oSLikes = osLikeValue.split(" ");
                        for (String osLike : oSLikes) {
                            if (osLike.equalsIgnoreCase("kali")) {
                                onKali = Boolean.TRUE;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return onKali;
    }

    public static boolean isBackBox() {
        if (onBackBox == null) {
            onBackBox = Boolean.FALSE;
            File issueFile = new File("/etc/issue");
            if (isLinux() && !isDailyBuild() && issueFile.exists()) {
                // Ignore the fact we're on BackBox if this is a daily build - they will only have
                // been installed manually
                try {
                    String content = new String(Files.readAllBytes(issueFile.toPath()));
                    if (content.startsWith("BackBox")) {
                        onBackBox = Boolean.TRUE;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return onBackBox;
    }

    /**
     * Returns true if ZAP is running in a container like Docker or Flatpak
     *
     * @see #getContainerName
     * @since 2.11.0
     */
    public static boolean isInContainer() {
        if (inContainer == null) {
            // This is created by the Docker files from 2.11
            File containerFile = new File(ZAP_CONTAINER_FILE);
            File flatpakFile = new File(FLATPAK_FILE);
            File snapFile = new File(SNAP_FILE);
            if (isLinux() && containerFile.exists()) {
                inContainer = true;
                String home = System.getenv(HOME_ENVVAR);
                boolean inWebSwing = home != null && home.contains(WEBSWING_NAME);
                try {
                    containerName =
                            new String(
                                            Files.readAllBytes(containerFile.toPath()),
                                            StandardCharsets.UTF_8)
                                    .trim();
                    if (inWebSwing) {
                        // Append the webswing name so we don't loose the docker image name
                        containerName += "." + WEBSWING_NAME;
                    }
                } catch (IOException e) {
                    // Ignore
                }
            } else if (flatpakFile.exists()) {
                inContainer = true;
                containerName = FLATPAK_NAME;
            } else if (snapFile.exists()) {
                inContainer = true;
                containerName = SNAP_NAME;
            } else {
                inContainer = false;
            }
        }
        return inContainer;
    }

    /**
     * Returns the name of the container ZAP is running in (if any) e.g. zap2docker-stable, flatpak
     * or null if not running in a recognised container
     *
     * @since 2.11.0
     */
    public static String getContainerName() {
        isInContainer();
        return containerName;
    }
}
