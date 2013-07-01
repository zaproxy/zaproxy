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

package org.parosproxy.paros;

import java.util.Hashtable;
import java.util.Vector;

import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.network.HttpSender;


public class CommandLine {

	// ZAP: Made public
    public static final String SESSION = "-session";
    public static final String NEW_SESSION = "-newsession";
    public static final String DAEMON = "-daemon";
    public static final String HELP = "-help";
    public static final String HELP2 = "-h";
    public static final String DIR = "-dir";
    public static final String VERSION = "-version";
    public static final String PORT = "-port";
    public static final String CMD = "-cmd";
    
    static final String NO_USER_AGENT = "-nouseragent";
    static final String SP = "-sp";
    
    private boolean GUI = true;
    private boolean daemon = false;
    private boolean reportVersion = false;
    private int port = -1;
    private String[] args = null;
    private Hashtable<String, String> keywords = new Hashtable<>();
    private Vector<CommandLineArgument[]> commandList = null;
    
    public CommandLine(String[] args) throws Exception {
        this.args = args;
        parseFirst(this.args);
    }
    
    private boolean checkPair(String[] args, String paramName, int i) throws Exception {
        String key = args[i];
        String value = null;
        if (key == null) return false;
        if (key.equalsIgnoreCase(paramName)) {
            value = args[i+1];
            if (value == null) throw new Exception();
            keywords.put(paramName, value);
            args[i] = null;
            args[i+1] = null;
            return true;
        }
        return false;
    }

    private boolean checkSwitch(String[] args, String paramName, int i) throws Exception {
        String key = args[i];
        if (key == null) return false;
        if (key.equalsIgnoreCase(paramName)) {
            keywords.put(paramName, "");
            args[i] = null;
            return true;
        }
        return false;
    }

    
	private void parseFirst(String[] args) throws Exception {

	    for (int i=0; i<args.length; i++) {
	        
	        if (parseSwitchs(args, i)) continue;
	        if (parseKeywords(args, i)) continue;
	        
        }
	        
    }

	public void parse(Vector<CommandLineArgument[]> commandList) throws Exception {
	    this.commandList = commandList;
	    CommandLineArgument lastArg = null;
	    boolean found = false;
	    int remainingValueCount = 0;
	    
	    for (int i=0; i<args.length; i++) {
	        if (args[i] == null) continue;
	        found = false;
	        
		    for (int j=0; j<commandList.size() && !found; j++) {
		        CommandLineArgument[] extArg = commandList.get(j);
		        for (int k=0; k<extArg.length && !found; k++)
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
	    
	    // check if there is some unknown keywords
	    for (int i=0; i<args.length; i++) {
	        if (args[i] != null) {
                throw new Exception("Unknown options: " + args[i]);	            
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
            setGUI(false);
            result = true;
	    } else if (checkPair(args, SESSION, i)) {
	        setGUI(false);
	        result = true;
	    } else if (checkPair(args, DIR, i)) {
	    	Constant.setZapHome(keywords.get(DIR));
	        result = true;
	    } else if (checkPair(args, PORT, i)) {
	    	this.port = Integer.parseInt(keywords.get(PORT));
	        result = true;
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
	
	public boolean isReportVersion() {
		return this.reportVersion;
	}
	
	public int getPort () {
		return this.port;
	}

	public String getArgument(String keyword) {
        return keywords.get(keyword);
    }

    // ZAP: Made public and rebranded
	public static String getHelpGeneral() {
    	StringBuilder sb = new StringBuilder();        
		sb.append("GUI usage:\n");
    	if (Constant.isWindows()) {
            sb.append("\tzap.bat ");
        } else {
            sb.append("\tzap.sh ");
        }
		sb.append("[-dir directory]\n\n");
		return sb.toString();
	}
	
	// ZAP: Rebranded
    public String getHelp() {
    	StringBuilder sb = new StringBuilder(getHelpGeneral());        
        sb.append("Command line usage:\n");
        if (Constant.isWindows()) {
            sb.append("\tzap.bat ");
        } else {
            sb.append("\tzap.sh ");
        }
        sb.append("[-h |-help] [-newsession session_file_path] [options] [-dir directory]\n" +
        		"\t\t[-port port] [-daemon] [-cmd] [-version]\n");
        sb.append("options:\n");

        for (int i=0; i<commandList.size(); i++) {
	        CommandLineArgument[] extArg = commandList.get(i);
	        for (int j=0; j<extArg.length; j++) {
	            sb.append("\t");
	            sb.append(extArg[j].getHelpMessage()).append("\n");
	        }
        }
        
        return sb.toString();
    }
    
    public boolean isEnabled(String keyword) {
        
        Object obj = keywords.get(keyword);
        if (obj != null && obj instanceof String) {
            return true;
        }
        return false;
    }
    
}
