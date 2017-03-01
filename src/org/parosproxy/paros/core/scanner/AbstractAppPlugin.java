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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/06/26 Added the possibility to evaluate the current plugin/process progress
// ZAP: 2016/04/21 Remove manual progress change, HostProcess takes care of that

package org.parosproxy.paros.core.scanner;

/**
 * AbstractAppPlugin is an abstract base class for Plugins basing on the hierarchy
 * of the site to perform a test.
 * 
 * Each URL under the selected node will be traversed and launch a plugin to test.
 * Multiple threads will be executed.  But AbstractAppPlugin must complete before another
 * can start.
 */
public abstract class AbstractAppPlugin extends AbstractPlugin {
    
    @Override
    public void notifyPluginCompleted(HostProcess parent) {
        /*	no need to notify parent this plugin is completed.  HostProcess will wait each
         	AbstractAppPlugin to finish.
         */
    }
}
