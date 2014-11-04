/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.help;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.help.JHelpContentViewer;
import javax.help.plaf.basic.BasicContentViewerUI;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.apache.log4j.Logger;

public class BasicOnlineContentViewerUI extends BasicContentViewerUI {

	private static final Logger logger = Logger.getLogger(BasicOnlineContentViewerUI.class);

    private static final long serialVersionUID = -1640590425627589113L;

    public BasicOnlineContentViewerUI(JHelpContentViewer contentViewer) {
        super(contentViewer);
    }

    @Override
    protected void linkActivated(URL u) {
        String protocol = u.getProtocol();
        if (Desktop.isDesktopSupported() && ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))) {
            try {
                Desktop.getDesktop().browse(u.toURI());
            } catch (IOException | URISyntaxException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            super.linkActivated(u);
        }
    }

    public static ComponentUI createUI(JComponent x) {
        return new BasicOnlineContentViewerUI((JHelpContentViewer) x);
    }

}