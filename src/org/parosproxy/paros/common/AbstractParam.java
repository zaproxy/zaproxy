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
package org.parosproxy.paros.common;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class AbstractParam {

    private FileConfiguration config = null;
    /**
     * Load this param from config
     * @param config
     */
    public void load(FileConfiguration config) {
        this.config = config;
        parse();
    }
    
    public void load(String fileName) {
        try {
            config = new XMLConfiguration(fileName);
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void load() throws Exception {
        config.load();
        parse();
    }
    
    public FileConfiguration getConfig() {
        return config;
    } 

    /**
     * Implement by subclass to parse the config file.
     *
     */
    abstract protected void parse();
}
