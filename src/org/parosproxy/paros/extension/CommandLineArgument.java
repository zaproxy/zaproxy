/*
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
package org.parosproxy.paros.extension;

import java.util.Vector;
import java.util.regex.Pattern;

public class CommandLineArgument {

    private String name = "";
    private int numOfArguments = 0;
    private Vector<String> arg = new Vector<>();
    private boolean enabled = false;
    private Pattern pattern = null;
    private String errorMessage = "";
    private String helpMessage = "";
    
    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * @return Returns the pattern.
     */
    public Pattern getPattern() {
        return pattern;
    }
    public CommandLineArgument(String name, int numOfArguments) {
        this.name = name;
        this.numOfArguments = numOfArguments;
    }

    public CommandLineArgument(String name, int numOfArguments, String pattern, String errorMessage, String helpMessage) {
        this(name, numOfArguments);
        if (pattern != null && pattern.length() > 0) {
            this.pattern = Pattern.compile(pattern);            
        }
        if (errorMessage != null) {
            this.errorMessage = errorMessage;
        }
        
        if (helpMessage != null) {
            this.helpMessage = helpMessage;
        }
    }

    public String getName() {
        return name;
    }
    
    public int getNumOfArguments() {
        return numOfArguments;
    }
    
    public Vector<String> getArguments() {
        return arg;
    }
    
    /**
     * Check if this command line is enabled by the caller
     * @return if this
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable this command line parameter.
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * @return Returns the helpMessage.
     */
    public String getHelpMessage() {
        return helpMessage;
    }
}
