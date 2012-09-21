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
package org.parosproxy.paros;

import java.util.Hashtable;
import java.util.Vector;

import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.network.HttpSender;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommandLine {

	// ZAP: Made public
    public static final String SESSION = "-session";
    public static final String NEW_SESSION = "-newsession";
    public static final String DAEMON = "-daemon";
    public static final String HELP = "-help";
    public static final String HELP2 = "-h";
    public static final String DIR = "-dir";
    
    static final String NO_USER_AGENT = "-nouseragent";
    static final String SP = "-sp";
    
    private boolean GUI = true;
    private boolean daemon = false;
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
        } else if (checkSwitch(args, DAEMON, i)) {
        	setDaemon(true);
            setGUI(false);
        } else if (checkSwitch(args, HELP, i)) {
            result = true;
            setGUI(false);
        } else if (checkSwitch(args, HELP2, i)) {
            result = true;
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

	public String getArgument(String keyword) {
        return keywords.get(keyword);
    }

    // ZAP: Made public and rebranded
	public static String getHelpGeneral() {
		String CRLF = "\r\n";
		String help =
			"GUI usage:" + CRLF +
			"\tjavaw zap.jar" + CRLF +
			"\tjava -jar zap.jar" + CRLF +
			"see java -jar zap.jar {-h|-help} for detail.\r\n\r\n";
		return help;
	}
	
	// ZAP: Rebranded
    public String getHelp() {
    	StringBuilder sb = new StringBuilder(getHelpGeneral());        
        sb.append("Command line usage:\r\n");
        sb.append("java -jar zap.jar {-h|-help} {-newsession session_file_path} {options} (-daemon)\r\n");
        sb.append("options:\r\n\r\n");

        for (int i=0; i<commandList.size(); i++) {
	        CommandLineArgument[] extArg = commandList.get(i);
	        for (int j=0; j<extArg.length; j++) {
	            sb.append(extArg[j].getHelpMessage()).append("\r\n");
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
