/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.view;

import org.parosproxy.paros.view.MainFrame;
import org.parosproxy.paros.view.TabbedPanel;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;

/**
 * @deprecated (2.5.0) No longer used/needed. {@link WorkbenchPanel} now manages the position of the message panels.
 */
@Deprecated
@SuppressWarnings("javadoc")
public class MessagePanelsPositionController {

    private enum MessagePanelsPosition {
        TABS_SIDE_BY_SIDE,
        PANEL_ABOVE,
        PANELS_SIDE_BY_SIDE
    }

    public MessagePanelsPositionController(
            HttpPanelRequest requestPanel,
            HttpPanelResponse responsePanel,
            MainFrame mainFrame,
            WorkbenchPanel workbenchPanel) {
    }

    /**
     * Restores the original parent of the panels and the panel.
     * @return always {@code null}
     */
    public TabbedPanel restoreOriginalParentTabbedPanel() {
        return null;
    }

    public void restoreState() {
        // No longer in use.
    }

    public void saveState(MessagePanelsPosition currentPosition) {
        // No longer in use.
    }

}
