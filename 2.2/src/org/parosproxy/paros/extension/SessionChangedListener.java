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
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension;

import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.model.Session;

public interface SessionChangedListener {
    /**
     * Called just after the session has changed.
     * sessionChanged may be called by non-event thread.  Should handle with care in
     * all the listener.  Use EventThread for each GUI event.
     * @param session
     */
    void sessionChanged(Session session);
    
    /**
     * Called just prior to the session changing.
     * Listeners should close down any resources associaited with this session. 
     * sessionAboutToChange may be called by non-event thread.  Should handle with care in
     * all the listener.  Use EventThread for each GUI event.
     * @param session
     */
    void sessionAboutToChange(Session session);
    
    /**
     * Called when the user has changes the session scope.
     * sessionScopeChanged may be called by non-event thread.  Should handle with care in
     * all the listener.  Use EventThread for each GUI event.
     * @param session
     */
    void sessionScopeChanged(Session session);
    
    /**
     * Called when the user changes the mode.
     * sessionModeChanged may be called by non-event thread.  Should handle with care in
     * all the listener.  Use EventThread for each GUI event.
     * @param mode
     */
    void sessionModeChanged(Mode mode);
    
}
