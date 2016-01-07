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
// ZAP: 2011/05/27 Catch any exception when loading the config file 
// ZAP: 2011/11/15 Changed to use ZapXmlConfiguration, to enforce the same character encoding when reading/writing configurations
//      removed duplicated method calls and removed an unnecessary method (load())
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/01/17 Issue 987: Allow arbitrary config file values to be set via the command line
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog

package org.parosproxy.paros.common;

import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.log4j.Logger;
import org.zaproxy.zap.control.ControlOverrides;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public abstract class AbstractParam implements Cloneable {

    private static final Logger logger = Logger.getLogger(AbstractParam.class);
    
    private FileConfiguration config = null;
    /**
     * Load this param from config
     * @param config
     */
    public void load(FileConfiguration config) {
        this.config = config;
        
        try {
            parse();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public void load(String fileName) {
    	this.load(fileName, null);
    }
    
	public void load(String fileName, ControlOverrides overrides) {
        try {
            config = new ZapXmlConfiguration(fileName);
            if (overrides != null) {
                for (Entry<String,String> entry : overrides.getConfigs().entrySet()) {
                	logger.info("Setting config " + entry.getKey() + " = " + entry.getValue() + 
                			" was " + config.getString(entry.getKey()));
                	config.setProperty(entry.getKey(), entry.getValue());
                }
            }
            parse();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
	}

    public FileConfiguration getConfig() {
        return config;
    } 
    
    @Override
    public AbstractParam clone()  {
    	try {
			AbstractParam clone = (AbstractParam) super.clone();
			clone.load((FileConfiguration) ConfigurationUtils.cloneConfiguration(config));
			return clone;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
		}
    	return null;
    }

    /**
     * Implement by subclass to parse the config file.
     *
     */
    protected abstract void parse();
}
