/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
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
// ZAP: 2011/04/16 Support for running ZAP as a daemon
// ZAP: 2012/03/15 Removed unnecessary castings from methods parse, getArgument and getHelp.
//      Changed to use the class StringBuilder instead of the class StringBuffer in the method
// getHelp.
// ZAP: 2012/10/15 Issue 397: Support weekly builds
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/20 Issue 568: Allow extensions to run from the command line
// ZAP: 2013/08/30 Issue 775: Allow host to be set via the command line
// ZAP: 2013/12/03 Issue 933: Automatically determine install dir
// ZAP: 2013/12/03 Issue 934: Handle files on the command line via extension
// ZAP: 2014/01/17 Issue 987: Allow arbitrary config file values to be set via the command line
// ZAP: 2014/05/20 Issue 1191: Cmdline session params have no effect
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2015/10/06 Issue 1962: Install and update add-ons from the command line
// ZAP: 2016/08/19 Issue 2782: Support -configfile
// ZAP: 2016/09/22 JavaDoc tweaks
// ZAP: 2016/11/07 Allow to disable default standard output logging
// ZAP: 2017/03/26 Allow to obtain configs in the order specified
// ZAP: 2017/05/12 Issue 3460: Support -suppinfo
// ZAP: 2017/05/31 Handle null args and include a message in all exceptions.
// ZAP: 2017/08/31 Use helper method I18N.getString(String, Object...).
// ZAP: 2017/11/21 Validate that -cmd and -daemon are not set at the same time (they are mutually
// exclusive).
// ZAP: 2017/12/26 Remove unused command line arg SP.
// ZAP: 2018/06/29 Add command line to run ZAP in dev mode.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/10/09 Issue 5619: Ensure -configfile maintains key order
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/14 Remove redundant type arguments.
// ZAP: 2022/02/09 No longer parse host/port and deprecate related code.
// ZAP: 2022/02/28 Remove code deprecated in 2.6.0
// ZAP: 2022/04/11 Remove -nouseragent option.
// ZAP: 2022/08/18 Support parameters supplied to newly installed or updated add-ons.
package org.parosproxy.paros;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.extension.autoupdate.ExtensionAutoUpdate;

public class CommandLine {

    private static final Logger logger = LogManager.getLogger(CommandLine.class);

    // ZAP: Made public
    public static final String SESSION = "-session";
    public static final String NEW_SESSION = "-newsession";
    public static final String DAEMON = "-daemon";
    public static final String HELP = "-help";
    public static final String HELP2 = "-h";
    public static final String DIR = "-dir";
    public static final String VERSION = "-version";
    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated public static final String PORT = "-port";
    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated public static final String HOST = "-host";

    public static final String CMD = "-cmd";
    public static final String INSTALL_DIR = "-installdir";
    public static final String CONFIG = "-config";
    public static final String CONFIG_FILE = "-configfile";
    public static final String LOWMEM = "-lowmem";
    public static final String EXPERIMENTALDB = "-experimentaldb";
    public static final String SUPPORT_INFO = "-suppinfo";
    public static final String SILENT = "-silent";

    /**
     * Command line option to disable the default logging through standard output.
     *
     * @see #isNoStdOutLog()
     * @since 2.6.0
     */
    public static final String NOSTDOUT = "-nostdout";

    /**
     * Command line option to enable "dev mode".
     *
     * <p>With this option development related utilities/functionalities are enabled. For example,
     * it's shown an error counter in the footer tool bar and license is implicitly accepted (thus
     * not requiring to show/accept the license each time a new home is used).
     *
     * <p><strong>Note:</strong> this mode is always enabled when running ZAP directly from source
     * (i.e. not packaged in a JAR) or using a dev build.
     *
     * @see #isDevMode()
     * @since 2.8.0
     */
    public static final String DEV_MODE = "-dev";

    private boolean GUI = true;
    private boolean daemon = false;
    private boolean reportVersion = false;
    private boolean displaySupportInfo = false;
    private boolean lowMem = false;
    private boolean experimentalDb = false;
    private boolean silent = false;
    private String[] args;
    private String[] argsBackup;
    private final Map<String, String> configs = new LinkedHashMap<>();
    private final Hashtable<String, String> keywords = new Hashtable<>();
    private List<CommandLineArgument[]> commandList = null;

    /**
     * Flag that indicates whether or not the default logging through standard output should be
     * disabled.
     */
    private boolean noStdOutLog;

    /** Flag that indicates whether or not the "dev mode" is enabled. */
    private boolean devMode;

    public CommandLine(String[] args) throws Exception {
        this.args = args == null ? new String[0] : args;
        this.argsBackup = new String[this.args.length];
        System.arraycopy(this.args, 0, argsBackup, 0, this.args.length);

        parseFirst(this.args);

        if (isEnabled(CommandLine.CMD) && isEnabled(CommandLine.DAEMON)) {
            throw new IllegalArgumentException(
                    "Command line arguments "
                            + CommandLine.CMD
                            + " and "
                            + CommandLine.DAEMON
                            + " cannot be used at the same time.");
        }
    }

    private boolean checkPair(String[] args, String paramName, int i) throws Exception {
        String key = args[i];
        String value = null;
        if (key == null) {
            return false;
        }

        if (key.equalsIgnoreCase(paramName)) {
            value = args[i + 1];
            if (value == null) {
                throw new Exception("Missing parameter for keyword '" + paramName + "'.");
            }

            keywords.put(paramName, value);
            args[i] = null;
            args[i + 1] = null;
            return true;
        }

        return false;
    }

    private boolean checkSwitch(String[] args, String paramName, int i) throws Exception {
        String key = args[i];
        if (key == null) {
            return false;
        }
        if (key.equalsIgnoreCase(paramName)) {
            keywords.put(paramName, "");
            args[i] = null;
            return true;
        }
        return false;
    }

    private void parseFirst(String[] args) throws Exception {

        for (int i = 0; i < args.length; i++) {

            if (parseSwitchs(args, i)) {
                continue;
            }
            if (parseKeywords(args, i)) {
                continue;
            }
        }
    }

    public void parse(
            List<CommandLineArgument[]> commandList, Map<String, CommandLineListener> extMap)
            throws Exception {
        this.parse(commandList, extMap, true);
    }

    /**
     * Parse the command line arguments
     *
     * @param commandList the list of commands
     * @param extMap a map of the extensions which support command line args
     * @param reportUnsupported if true will report unsupported args
     * @throws Exception
     * @since 2.12.0
     */
    public void parse(
            List<CommandLineArgument[]> commandList,
            Map<String, CommandLineListener> extMap,
            boolean reportUnsupported)
            throws Exception {
        this.commandList = commandList;
        CommandLineArgument lastArg = null;
        boolean found = false;
        int remainingValueCount = 0;
        boolean installingAddons = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }

            found = false;

            for (int j = 0; j < commandList.size() && !found; j++) {
                CommandLineArgument[] extArg = commandList.get(j);
                for (int k = 0; k < extArg.length && !found; k++) {
                    if (args[i].compareToIgnoreCase(extArg[k].getName()) == 0) {

                        // check if previous keyword satisfied its required no. of parameters
                        if (remainingValueCount > 0) {
                            throw new Exception(
                                    "Missing parameters for keyword '" + lastArg.getName() + "'.");
                        }

                        // process this keyword
                        lastArg = extArg[k];
                        lastArg.setEnabled(true);
                        found = true;

                        if (ExtensionAutoUpdate.ADDON_INSTALL.equals(args[i])
                                || ExtensionAutoUpdate.ADDON_INSTALL_ALL.equals(args[i])) {
                            installingAddons = true;
                        }

                        args[i] = null;
                        remainingValueCount = lastArg.getNumOfArguments();
                    }
                }
            }

            // check if current string is a keyword preceded by '-'
            if (args[i] != null && args[i].startsWith("-")) {
                continue;
            }

            // check if there is no more expected param value
            if (lastArg != null && remainingValueCount == 0) {
                continue;
            }

            // check if consume remaining for last matched keywords
            if (!found && lastArg != null) {
                if (lastArg.getPattern() == null || lastArg.getPattern().matcher(args[i]).find()) {
                    lastArg.getArguments().add(args[i]);
                    if (remainingValueCount > 0) {
                        remainingValueCount--;
                    }
                    args[i] = null;
                } else {
                    throw new Exception(lastArg.getErrorMessage());
                }
            }
        }

        // check if the last keyword satisfied its no. of parameters.
        if (lastArg != null && remainingValueCount > 0) {
            throw new Exception("Missing parameters for keyword '" + lastArg.getName() + "'.");
        }

        // check for supported extensions
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            int dotIndex = args[i].lastIndexOf(".");
            if (dotIndex < 0) {
                // Only support files with extensions
                continue;
            }
            File file = new File(args[i]);
            if (!file.exists() || !file.canRead()) {
                // Not there or cant read .. move on
                continue;
            }

            String ext = args[i].substring(dotIndex + 1);
            CommandLineListener cll = extMap.get(ext);
            if (cll != null) {
                if (cll.handleFile(file)) {
                    found = true;
                    args[i] = null;
                }
            }
        }

        if (reportUnsupported && !installingAddons) {
            // check if there is some unknown keywords or parameters
            for (String arg : args) {
                if (arg != null) {
                    if (arg.startsWith("-")) {
                        throw new Exception(
                                Constant.messages.getString("start.cmdline.badparam", arg));

                    } else {
                        // Assume they were trying to specify a file
                        File f = new File(arg);
                        if (!f.exists()) {
                            throw new Exception(
                                    Constant.messages.getString("start.cmdline.nofile", arg));

                        } else if (!f.canRead()) {
                            throw new Exception(
                                    Constant.messages.getString("start.cmdline.noread", arg));

                        } else {
                            // We probably dont handle this sort of file
                            throw new Exception(
                                    Constant.messages.getString("start.cmdline.badfile", arg));
                        }
                    }
                }
            }
        }
    }

    private boolean parseSwitchs(String[] args, int i) throws Exception {

        boolean result = false;

        if (checkSwitch(args, CMD, i)) {
            setDaemon(false);
            setGUI(false);

        } else if (checkSwitch(args, DAEMON, i)) {
            setDaemon(true);
            setGUI(false);

        } else if (checkSwitch(args, LOWMEM, i)) {
            setLowMem(true);

        } else if (checkSwitch(args, EXPERIMENTALDB, i)) {
            setExperimentalDb(true);

        } else if (checkSwitch(args, HELP, i)) {
            result = true;
            setGUI(false);

        } else if (checkSwitch(args, HELP2, i)) {
            result = true;
            setGUI(false);

        } else if (checkSwitch(args, VERSION, i)) {
            reportVersion = true;
            setDaemon(false);
            setGUI(false);
        } else if (checkSwitch(args, NOSTDOUT, i)) {
            noStdOutLog = true;
        } else if (checkSwitch(args, SUPPORT_INFO, i)) {
            displaySupportInfo = true;
            setDaemon(false);
            setGUI(false);
        } else if (checkSwitch(args, DEV_MODE, i)) {
            devMode = true;
            Constant.setDevMode(true);
        } else if (checkSwitch(args, SILENT, i)) {
            silent = true;
            Constant.setSilent(true);
        }

        return result;
    }

    private boolean parseKeywords(String[] args, int i) throws Exception {
        boolean result = false;
        if (checkPair(args, NEW_SESSION, i)) {
            result = true;

        } else if (checkPair(args, SESSION, i)) {
            result = true;

        } else if (checkPair(args, DIR, i)) {
            Constant.setZapHome(keywords.get(DIR));
            result = true;

        } else if (checkPair(args, INSTALL_DIR, i)) {
            Constant.setZapInstall(keywords.get(INSTALL_DIR));
            result = true;

        } else if (checkPair(args, CONFIG, i)) {
            String pair = keywords.get(CONFIG);
            if (pair != null && pair.indexOf("=") > 0) {
                int eqIndex = pair.indexOf("=");
                this.configs.put(pair.substring(0, eqIndex), pair.substring(eqIndex + 1));
                result = true;
            }
        } else if (checkPair(args, CONFIG_FILE, i)) {
            String conf = keywords.get(CONFIG_FILE);
            File confFile = new File(conf);
            if (!confFile.isFile()) {
                // We cant use i18n here as the messages wont have been loaded
                throw new Exception("No such file: " + confFile.getAbsolutePath());
            } else if (!confFile.canRead()) {
                // We cant use i18n here as the messages wont have been loaded
                throw new Exception("File not readable: " + confFile.getAbsolutePath());
            }

            Properties prop =
                    new Properties() {
                        // Override methods to ensure keys returned in order
                        List<Object> orderedKeys = new ArrayList<>();
                        private static final long serialVersionUID = 1L;

                        @Override
                        public synchronized Object put(Object key, Object value) {
                            orderedKeys.add(key);
                            return super.put(key, value);
                        }

                        @Override
                        public synchronized Enumeration<Object> keys() {
                            return Collections.enumeration(orderedKeys);
                        }
                    };
            try (FileInputStream inStream = new FileInputStream(confFile)) {
                prop.load(inStream);
            }

            Enumeration<Object> keyEnum = prop.keys();

            while (keyEnum.hasMoreElements()) {
                String key = (String) keyEnum.nextElement();
                this.configs.put(key, prop.getProperty(key));
            }
        }

        return result;
    }

    /**
     * Tells whether or not ZAP was started with GUI.
     *
     * @return {@code true} if ZAP was started with GUI, {@code false} otherwise
     */
    public boolean isGUI() {
        return GUI;
    }

    /**
     * Sets whether or not ZAP was started with GUI.
     *
     * @param GUI {@code true} if ZAP was started with GUI, {@code false} otherwise
     */
    public void setGUI(boolean GUI) {
        this.GUI = GUI;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public boolean isLowMem() {
        return lowMem;
    }

    public void setLowMem(boolean lowMem) {
        this.lowMem = lowMem;
    }

    public boolean isExperimentalDb() {
        return experimentalDb;
    }

    public void setExperimentalDb(boolean experimentalDb) {
        this.experimentalDb = experimentalDb;
    }

    public boolean isReportVersion() {
        return this.reportVersion;
    }

    public boolean isDisplaySupportInfo() {
        return this.displaySupportInfo;
    }

    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated
    public int getPort() {
        return -1;
    }

    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated
    public String getHost() {
        return null;
    }

    /**
     * Gets the {@code config} command line arguments, in the order they were specified.
     *
     * @return the {@code config} command line arguments.
     * @since 2.6.0
     */
    public Map<String, String> getOrderedConfigs() {
        return configs;
    }

    public String getArgument(String keyword) {
        return keywords.get(keyword);
    }

    public String getHelp() {
        return CommandLine.getHelp(commandList);
    }

    /**
     * Tells whether or not the default logging through standard output should be disabled.
     *
     * @return {@code true} if the default logging through standard output should be disabled,
     *     {@code false} otherwise.
     * @since 2.6.0
     */
    public boolean isNoStdOutLog() {
        return noStdOutLog;
    }

    /**
     * Returns true if ZAP should not make any unsolicited requests, e.g. check-for-updates, etc.
     *
     * @since 2.8.0
     */
    public boolean isSilent() {
        return silent;
    }

    /**
     * Tells whether or not the "dev mode" should be enabled.
     *
     * @return {@code true} if the "dev mode" should be enabled, {@code false} otherwise.
     * @since 2.8.0
     * @see #DEV_MODE
     */
    public boolean isDevMode() {
        return devMode;
    }

    public static String getHelp(List<CommandLineArgument[]> cmdList) {
        String zap;
        if (Constant.isWindows()) {
            zap = "zap.bat";

        } else {
            zap = "zap.sh";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(Constant.messages.getString("cmdline.help", zap));

        if (cmdList != null) {
            for (CommandLineArgument[] extArgs : cmdList) {
                for (CommandLineArgument extArg : extArgs) {
                    sb.append("\t");
                    sb.append(extArg.getHelpMessage()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public boolean isEnabled(String keyword) {
        Object obj = keywords.get(keyword);
        return (obj != null) && (obj instanceof String);
    }

    /**
     * Reset the arguments so that they can be parsed again (e.g. after an add-on is installed)
     *
     * @since 2.12.0
     */
    public void resetArgs() {
        System.arraycopy(argsBackup, 0, args, 0, argsBackup.length);
    }

    /**
     * A method for reporting informational messages in {@link
     * CommandLineListener#execute(CommandLineArgument[])} implementations. It ensures that messages
     * are written to the log file and/or written to stdout as appropriate.
     *
     * @param str the informational message
     */
    public static void info(String str) {
        switch (ZAP.getProcessType()) {
            case cmdline:
                System.out.println(str);
                break;
            default: // Ignore
        }
        // Always write to the log
        logger.info(str);
    }

    /**
     * A method for reporting error messages in {@link
     * CommandLineListener#execute(CommandLineArgument[])} implementations. It ensures that messages
     * are written to the log file and/or written to stderr as appropriate.
     *
     * @param str the error message
     */
    public static void error(String str) {
        switch (ZAP.getProcessType()) {
            case cmdline:
                System.err.println(str);
                break;
            default: // Ignore
        }
        // Always write to the log
        logger.error(str);
    }

    /**
     * A method for reporting error messages in {@link
     * CommandLineListener#execute(CommandLineArgument[])} implementations. It ensures that messages
     * are written to the log file and/or written to stderr as appropriate.
     *
     * @param str the error message
     * @param e the cause of the error
     */
    public static void error(String str, Throwable e) {
        switch (ZAP.getProcessType()) {
            case cmdline:
                System.err.println(str);
                break;
            default: // Ignore
        }
        // Always write to the log
        logger.error(str, e);
    }
}
