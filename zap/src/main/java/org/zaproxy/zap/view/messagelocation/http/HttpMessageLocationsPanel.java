/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.view.messagelocation.http;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.messagelocation.SelectMessageLocationsPanel;

/**
 * A {@code JPanel} that allows to select locations of an {@code HttpMessage}.
 *
 * @since 2.4.0
 */
public class HttpMessageLocationsPanel extends JPanel {

    private static final long serialVersionUID = -5501414760564977715L;

    private DefaultHttpMessageLocationsPanel locationsPanel;
    private SelectMessageLocationsPanel selectMessageLocationsPanel;

    private boolean request;

    public HttpMessageLocationsPanel(boolean request, HttpMessage message, String locationsLabel) {
        super(new BorderLayout());

        this.request = request;
        this.selectMessageLocationsPanel = new SelectMessageLocationsPanel();
        if (request) {
            selectMessageLocationsPanel.addComponent(
                    new RequestSplitComponent<>(), new ZapXmlConfiguration());
            HttpPanelManager.getInstance().addRequestPanel(selectMessageLocationsPanel);
        } else {
            selectMessageLocationsPanel.addComponent(
                    new ResponseSplitComponent<>(), new ZapXmlConfiguration());
            HttpPanelManager.getInstance().addResponsePanel(selectMessageLocationsPanel);
        }
        selectMessageLocationsPanel.setMessage(message, true);

        locationsPanel = new DefaultHttpMessageLocationsPanel(this, selectMessageLocationsPanel);
        selectMessageLocationsPanel.addFocusListener(
                locationsPanel.getFocusListenerAddButtonEnabler());

        setLayout(new BorderLayout());

        JPanel rightPanel = new JPanel();
        GroupLayout leftPanelLayout = new GroupLayout(rightPanel);
        rightPanel.setLayout(leftPanelLayout);

        leftPanelLayout.setAutoCreateGaps(true);
        leftPanelLayout.setAutoCreateContainerGaps(true);

        JLabel locLabel = new JLabel(locationsLabel);

        leftPanelLayout.setHorizontalGroup(
                leftPanelLayout
                        .createParallelGroup()
                        .addComponent(locLabel)
                        .addComponent(locationsPanel));

        leftPanelLayout.setVerticalGroup(
                leftPanelLayout
                        .createSequentialGroup()
                        .addComponent(locLabel)
                        .addComponent(locationsPanel));

        JSplitPane splitPane =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT, selectMessageLocationsPanel, rightPanel);
        splitPane.setResizeWeight(0.6d);

        add(splitPane);
    }

    public boolean setSelectedContainer(String containerName) {
        if (containerName == null || containerName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter containerName must not be null nor empty.");
        }
        return selectMessageLocationsPanel.setSelectedView(containerName);
    }

    public boolean addMessageLocation(MessageLocation messageLocation) {
        if (messageLocation == null) {
            throw new IllegalArgumentException("Parameter messageLocation must not be null.");
        }
        return locationsPanel.addMessageLocation(messageLocation);
    }

    public void clear() {
        if (request) {
            HttpPanelManager.getInstance().removeRequestPanel(selectMessageLocationsPanel);
        } else {
            HttpPanelManager.getInstance().removeResponsePanel(selectMessageLocationsPanel);
        }
    }

    public List<MessageLocation> getLocations() {
        return locationsPanel.getLocations();
    }
}
