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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.view.messagelocation.AbstractMessageLocationsPanel;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.MessageLocationTableEntry;
import org.zaproxy.zap.view.messagelocation.MessageLocationsTableModel;
import org.zaproxy.zap.view.messagelocation.SelectMessageLocationsPanel;

/**
 * Default implementation of an {@code AbstractMessageLocationsPanel} for locations in an
 * {HttpMessage}.
 *
 * @since 2.4.0
 * @see AbstractMessageLocationsPanel
 * @see MessageLocationsTableModel
 */
public class DefaultHttpMessageLocationsPanel
        extends AbstractMessageLocationsPanel<
                MessageLocationTableEntry, MessageLocationsTableModel<MessageLocationTableEntry>> {

    private static final long serialVersionUID = -1897228805775451356L;

    protected DefaultHttpMessageLocationsPanel(
            Component parent, SelectMessageLocationsPanel selectMessageLocationsPanel) {
        super(parent, selectMessageLocationsPanel, new MessageLocationsTableModel<>(), false);
    }

    @Override
    protected MessageLocationTableEntry createMessageLocationTableEntry(
            boolean buttonAddedLocation,
            MessageLocation location,
            MessageLocationHighlight highlight,
            MessageLocationHighlight highlightReference) {
        return new MessageLocationTableEntry(location, highlight, highlightReference);
    }

    public List<MessageLocation> getLocations() {
        List<MessageLocation> locations = new ArrayList<>();
        for (MessageLocationTableEntry entry : getModel().getElements()) {
            locations.add(entry.getLocation());
        }
        return locations;
    }
}
