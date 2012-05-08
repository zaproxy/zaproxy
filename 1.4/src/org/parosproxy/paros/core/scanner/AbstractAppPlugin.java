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
package org.parosproxy.paros.core.scanner;

/**
 * AbstractAppPlugin is an abstract base class for Plugins basing on the hierarchy
 * of the site to perform a test.
 * 
 * Each URL under the selected node will be traversed and launch a plugin to test.
 * Multiple threads will be executed.  But AbstractAppPlugin must complete before another
 * can start.
 */
abstract public class AbstractAppPlugin extends AbstractPlugin {
    
    public void notifyPluginCompleted(HostProcess parent) {
        /*	no need to notify parent this plugin is completed.  HostProcess will wait each
         	AbstractAppPlugin to finish.
         */
        
    }
}
