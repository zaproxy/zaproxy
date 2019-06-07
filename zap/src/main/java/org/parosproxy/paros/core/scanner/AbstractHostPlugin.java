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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2017/09/21 Add JavaDoc.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.core.scanner;

/**
 * A {@link Plugin} that is called just once per scan, it should be used when testing whole site or
 * server related issues.
 *
 * <p>The plugin is {@link #init(org.parosproxy.paros.network.HttpMessage, HostProcess) initialised}
 * with the first message being scanned, which the plugin can later {@link #getBaseMsg() obtain} to
 * extract host related information (for example, domain, port) for the {@link #scan() scan}.
 */
public abstract class AbstractHostPlugin extends AbstractPlugin {

    @Override
    public void notifyPluginCompleted(HostProcess parent) {
        parent.pluginCompleted(this);
    }
}
