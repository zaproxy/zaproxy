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
//      Changed to use the class StringBuilder instead of the class StringBuffer in the method getHelp.
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

package org.parosproxy.paros;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.ZAP;

public class CommandLine {

	private static final Logger logger = Logger.getLogger(CommandLine.class);

    // ZAP: Made public
    public static final String SESSION = "-session";
    public static final String NEW_SESSION = "-newsession";
    public static final String DAEMON = "-daemon";
    public static final String HELP = "-help";
    public static final String HELP2 = "-h";
    public static final String DIR = "-dir";
    public static final String VERSION = "-version";
    public static final String PORT = "-port";
    public static final String HOST = "-host";
    public static final String CMD = "-cmd";
    public static final String INSTALL_DIR = "-installdir";
    public static final String CONFIG = "-config";
    public static final String CONFIG_FILE = "-configfile";
    public static final String LOWMEM = "-lowmem";
    public static final String EXPERIMENTALDB = "-experimentaldb";

    static final String NO_USER_AGENT = "-nouseragent";
    static final String SP = "-sp";

    private boolean GUI = true;
    private boolean daemon = false;
    private boolean reportVersion = false;
    private boolean lowMem = false;
    private boolean experimentalDb = false;
    private int port = -1;
    private String host = null;
    private String[] args = null;
    private final Hashtable<String, String> configs = new Hashtable<>();
    private final Hashtable<String, String> keywords = new Hashtable<>();
    private List<CommandLineArgument[]> commandList = null;

    public CommandLine(String[] args) throws Exception {
        this.args = args;
        parseFirst(this.args);
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
                throw new Exception();
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

    public void parse(List<CommandLineArgument[]> commandList, Map<String, CommandLineListener> extMap) throws Exception {
        this.commandList = commandList;
        CommandLineArgument lastArg = null;
        boolean found = false;
        int remainingValueCount = 0;

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
                            throw new Exception("Missing parameters for keyword '" + lastArg.getName() + "'.");
                        }

                        // process this keyword
                        lastArg = extArg[k];
                        lastArg.setEnabled(true);
                        found = true;
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

        // check if the last keyword satified its no. of parameters.
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

        // check if there is some unknown keywords or parameters
        for (String arg : args) {
            if (arg != null) {
                if (arg.startsWith("-")) {
                    throw new Exception(
                            MessageFormat.format(Constant.messages.getString("start.cmdline.badparam"), arg));
                    
                } else {
                    // Assume they were trying to specify a file
                    File f = new File(arg);
                    if (!f.exists()) {
                        throw new Exception(
                                MessageFormat.format(Constant.messages.getString("start.cmdline.nofile"), arg));
                    
                    } else if (!f.canRead()) {
                        throw new Exception(
                                MessageFormat.format(Constant.messages.getString("start.cmdline.noread"), arg));
                    
                    } else {
                        // We probably dont handle this sort of file
                        throw new Exception(
                                MessageFormat.format(Constant.messages.getString("start.cmdline.badfile"), arg));
                    }
                }
            }
        }
    }

    private boolean parseSwitchs(String[] args, int i) throws Exception {

        boolean result = false;

        if (checkSwitch(args, NO_USER_AGENT, i)) {
            HttpSender.setUserAgent("");
            Constant.setEyeCatcher("");
            result = true;

        } else if (checkSwitch(args, SP, i)) {
            Constant.setSP(true);
            result = true;
            
        } else if (checkSwitch(args, CMD, i)) {
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
            
        } else if (checkPair(args, HOST, i)) {
            this.host = keywords.get(HOST);
            result = true;
            
        } else if (checkPair(args, PORT, i)) {
            this.port = Integer.parseInt(keywords.get(PORT));
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
            if (! confFile.isFile()) {
                // We cant use i18n here as the messages wont have been loaded
                String error = "No such file: " + confFile.getAbsolutePath();
                System.out.println(error);
                throw new Exception(error);
            } else if (! confFile.canRead()) {
                // We cant use i18n here as the messages wont have been loaded
                String error = "File not readable: " + confFile.getAbsolutePath();
                System.out.println(error);
                throw new Exception(error);
            }
            Properties prop = new Properties();
            try (FileInputStream inStream = new FileInputStream(confFile)) {
                prop.load(inStream);
            }
            
            for (Entry<Object, Object> keyValue : prop.entrySet()) {
                this.configs.put((String)keyValue.getKey(), (String)keyValue.getValue());
            }
        }
        
        return result;
    }

    /**
     * @return Returns the noGUI.
     */
    public boolean isGUI() {
        return GUI;
    }

    /**
     * @param GUI The noGUI to set.
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

    public int getPort() {
        return this.port;
    }

    public String getHost() {
        return host;
    }

    public Hashtable<String, String> getConfigs() {
        return configs;
    }

    public String getArgument(String keyword) {
        return keywords.get(keyword);
    }

    public String getHelp() {
    	return CommandLine.getHelp(commandList);
    }

    public static String getHelp(List<CommandLineArgument[]> cmdList) {
        String zap;
        if (Constant.isWindows()) {
            zap = "zap.bat";
            
        } else {
            zap = "zap.sh";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
				Constant.messages.getString("cmdline.help"), zap));

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
     * A method for reporting informational messages in CommandLineListener.execute(..) implementations.
     * It ensures that messages are written to the log file and/or written to stdout as appropriate.
     * @param str
     * @see org.parosproxy.paros.extension.CommandLineListener#execute()
     */
    public static void info(String str) {
    	switch (ZAP.getProcessType()) {
    	case cmdline:	System.out.println(str); break;
    	default:		// Ignore
    	}
    	// Always write to the log
    	logger.info(str);
    }
    
    /**
     * A method for reporting error messages in CommandLineListener.execute(..) implementations.
     * It ensures that messages are written to the log file and/or written to stderr as appropriate.
     * @param str
     * @see org.parosproxy.paros.extension.CommandLineListener#execute()
     */
    public static void error(String str) {
    	switch (ZAP.getProcessType()) {
    	case cmdline:	System.err.println(str); break;
    	default:		// Ignore
    	}
    	// Always write to the log
		logger.error(str);
    }
    
    /**
     * A method for reporting error messages in CommandLineListener.execute(..) implementations.
     * It ensures that messages are written to the log file and/or written to stderr as appropriate.
     * @param str
     * @see org.parosproxy.paros.extension.CommandLineListener#execute()
     */
    public static void error(String str, Throwable e) {
    	switch (ZAP.getProcessType()) {
    	case cmdline:	System.err.println(str); break;
    	default:		// Ignore
    	}
    	// Always write to the log
		logger.error(str, e);
    }
    
}
