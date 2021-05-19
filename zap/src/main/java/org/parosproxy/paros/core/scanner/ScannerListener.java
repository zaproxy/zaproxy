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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Removed redundant public modifiers from interface method declarations
// ZAP: 2014/10/24 Issue 1378: Revamp active scan panel
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/12/10 Issue 5278: Adding filtered messages to active scan panel.
// ZAP: 2021/05/14 Remove empty statement.
package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.network.HttpMessage;

public interface ScannerListener {

    void scannerComplete(int id);

    void hostNewScan(int id, String hostAndPort, HostProcess hostThread);

    void hostProgress(int id, String hostAndPort, String msg, int percentage);

    void hostComplete(int id, String hostAndPort);

    void alertFound(Alert alert);

    // ZAP: Added notifyNewMessage
    void notifyNewMessage(HttpMessage msg);

    /**
     * Added to notify reason for filtering message from scanning.
     *
     * @param msg
     * @param reason
     */
    default void filteredMessage(HttpMessage msg, String reason) {}
}
