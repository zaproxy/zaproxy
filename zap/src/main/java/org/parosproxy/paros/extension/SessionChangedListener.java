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
// ZAP: 2016/09/22 JavaDoc tweaks
// ZAP: 2017/06/07 Allow to notify of changes in the session's properties (e.g. name, description).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.extension;

import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.model.Session;

public interface SessionChangedListener {
    /**
     * Called just after the session has changed. sessionChanged may be called by non-event thread.
     * Should handle with care in all the listener. Use EventThread for each GUI event.
     *
     * @param session the new session
     */
    void sessionChanged(Session session);

    /**
     * Called just prior to the session changing. Listeners should close down any resources
     * associated with this session. sessionAboutToChange may be called by non-event thread. Should
     * handle with care in all the listener. Use EventThread for each GUI event.
     *
     * @param session the session about to be closed
     */
    void sessionAboutToChange(Session session);

    /**
     * Called when the user has changes the session scope. sessionScopeChanged may be called by
     * non-event thread. Should handle with care in all the listener. Use EventThread for each GUI
     * event.
     *
     * @param session the current session
     */
    void sessionScopeChanged(Session session);

    /**
     * Called when the user changes the mode. sessionModeChanged may be called by non-event thread.
     * Should handle with care in all the listener. Use EventThread for each GUI event.
     *
     * @param mode the new mode
     */
    void sessionModeChanged(Mode mode);

    /**
     * Called when the session properties (e.g. name, description) have been changed.
     *
     * <p>This method may be called by other threads than {@link java.awt.EventQueue EventQueue}.
     *
     * @param session the session changed.
     * @since 2.7.0
     */
    default void sessionPropertiesChanged(Session session) {}
}
