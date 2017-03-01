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
package org.zaproxy.zap.view.messagelocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.FileConfiguration;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * An {@code HttpPanel} that allows to select and highlight locations in the displayed message.
 *
 * @since 2.4.0
 * @see HttpPanel
 * @see MessageLocationProducer
 * @see MessageLocationHighlighter
 */
public class SelectMessageLocationsPanel extends HttpPanel implements MessageLocationProducer, MessageLocationHighlighter {

    private static final long serialVersionUID = -1511437565770653938L;

    private final FileConfiguration fileConfiguration;
    private final Map<MessageLocationProducer, MessageLocationHighlightsManager> highlightsManagers;

    private FocusListenerCollection focusListeners;
    private MessageLocationProducer selectedProducer;

    private List<HttpPanelView> views;

    public SelectMessageLocationsPanel() {
        super(false, "");

        fileConfiguration = new ZapXmlConfiguration();
        this.highlightsManagers = new HashMap<>();
        this.views = new ArrayList<>();
        this.focusListeners = new FocusListenerCollection();
    }

    @Override
    public void addFocusListener(MessageLocationProducerFocusListener fl) {
        focusListeners.addFocusListener(fl);
    }

    @Override
    public void removeFocusListener(MessageLocationProducerFocusListener fl) {
        focusListeners.removeFocusListener(fl);
    }

    @Override
    public void addComponent(HttpPanelComponentInterface component, FileConfiguration fileConfiguration) {
        // User another file configuration, no need to persist the options
        super.addComponent(component, this.fileConfiguration);
    }

    @Override
    public void addView(String componentName, HttpPanelView view, Object options, FileConfiguration fileConfiguration) {
        if (!(view instanceof MessageLocationProducer)) {
            return;
        }

        ((MessageLocationProducer) view).addFocusListener(focusListeners);
        views.add(view);

        // User another file configuration, no need to persist the options
        super.addView(componentName, view, options, this.fileConfiguration);
    }

    @Override
    public void removeView(String componentName, String viewName, Object options) {
        for (Iterator<HttpPanelView> it = views.iterator(); it.hasNext();) {
            HttpPanelView httpPanelView = it.next();
            if (viewName.equals(httpPanelView.getName())) {
                ((MessageLocationProducer) httpPanelView).removeFocusListener(focusListeners);
                it.remove();
                break;
            }

        }
        super.removeView(componentName, viewName, options);
    }

    @Override
    public void addDefaultViewSelector(String componentName, HttpPanelDefaultViewSelector defaultViewSelector, Object options) {
    }

    @Override
    public void removeDefaultViewSelector(String componentName, String defaultViewSelectorName, Object options) {
    }

    @Override
    protected void initComponents() {
    }

    @Override
    protected void initSpecial() {
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location) {
        if (getCurrentComponent() instanceof MessageLocationHighlighter) {

            MessageLocationHighlighter highlighter = (MessageLocationHighlighter) getCurrentComponent();
            return highlighter.highlight(location);
        }
        return null;
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location, MessageLocationHighlight highlight) {
        if (getCurrentComponent() instanceof MessageLocationHighlighter) {

            MessageLocationHighlighter highlighter = (MessageLocationHighlighter) getCurrentComponent();
            return highlighter.highlight(location, highlight);
        }
        return null;
    }

    @Override
    public void removeHighlight(MessageLocation location, MessageLocationHighlight highlightReference) {
        if (getCurrentComponent() instanceof MessageLocationHighlighter) {

            MessageLocationHighlighter highlighter = (MessageLocationHighlighter) getCurrentComponent();
            highlighter.removeHighlight(location, highlightReference);
        }
    }

    @Override
    public boolean supports(MessageLocation location) {
        for (HttpPanelComponentInterface component : getEnabledComponents()) {
            if (component instanceof MessageLocationHighlighter) {

                MessageLocationHighlighter highlighter = (MessageLocationHighlighter) component;
                if (highlighter.supports(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean supports(Class<? extends MessageLocation> classLocation) {
        for (HttpPanelComponentInterface component : getEnabledComponents()) {
            if (component instanceof MessageLocationHighlighter) {

                MessageLocationHighlighter highlighter = (MessageLocationHighlighter) component;
                if (highlighter.supports(classLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Class<? extends MessageLocation> getMessageLocationClass() {
        if (selectedProducer != null) {
            return selectedProducer.getMessageLocationClass();
        }
        return null;
    }

    @Override
    public MessageLocation getSelection() {
        if (selectedProducer != null) {
            return selectedProducer.getSelection();
        }
        return null;
    }

    @Override
    public MessageLocationHighlightsManager create() {
        if (selectedProducer != null) {
            MessageLocationHighlightsManager highlightsManager = highlightsManagers.get(selectedProducer);
            if (highlightsManager == null) {
                highlightsManager = selectedProducer.create();
                highlightsManagers.put(selectedProducer, highlightsManager);
            }
            return highlightsManager;
        }
        return null;
    }

    public boolean setSelectedView(String viewName) {
        if (viewName == null || viewName.isEmpty()) {
            throw new IllegalArgumentException("Parameter containerName must not be null nor empty.");
        }
        for (HttpPanelComponentInterface component : getEnabledComponents()) {
            HttpPanelView selectedView = component.setSelectedView(viewName);
            if (selectedView != null) {
                selectedProducer = (MessageLocationProducer) selectedView;
                return true;
            }
        }
        return false;
    }

    private class FocusListenerCollection implements MessageLocationProducerFocusListener {

        private final List<MessageLocationProducerFocusListener> focusListeners;

        private FocusListenerCollection() {
            focusListeners = new ArrayList<>(5);
        }

        private void addFocusListener(MessageLocationProducerFocusListener focusListener) {
            focusListeners.add(focusListener);
        }

        private void removeFocusListener(MessageLocationProducerFocusListener focusListener) {
            focusListeners.remove(focusListener);
        }

        @Override
        public void focusGained(MessageLocationProducerFocusEvent e) {
            selectedProducer = e.getSource();

            for (MessageLocationProducerFocusListener focusListener : focusListeners) {
                focusListener.focusGained(e);
            }
        }

        @Override
        public void focusLost(MessageLocationProducerFocusEvent e) {
            for (MessageLocationProducerFocusListener focusListener : focusListeners) {
                focusListener.focusLost(e);
            }
        }
    }

    public void reset() {
    }

}
