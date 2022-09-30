/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.component;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.InvalidMessageDataException;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchableHttpPanelView;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlighter;

public class HttpPanelComponentViewsManager implements ItemListener, MessageLocationHighlighter {

    private static final Logger logger = LogManager.getLogger(HttpPanelComponentViewsManager.class);

    private static final String VIEWS_KEY = "views";
    private static final String DEFAULT_VIEW_KEY = "defaultview";

    private static DefaultViewSelectorComparator defaultViewSelectorComparator;

    private Message message;

    private JPanel panelViews;

    private JComboBox<ViewItem> comboBoxSelectView;

    private MutableComboBoxModel<ViewItem> comboBoxModel;

    private HttpPanelView currentView;

    private List<ViewItem> enabledViews;

    private Map<String, ViewItem> viewItems;

    private Map<String, HttpPanelView> views;

    private List<HttpPanelDefaultViewSelector> defaultViewsSelectors;

    private String savedSelectedViewName;

    private String configurationKey;

    private String viewsConfigurationKey;

    private boolean isEditable;

    private Object changingComboBoxLocker;

    private boolean changingComboBox;

    private HttpPanel owner;

    public HttpPanelComponentViewsManager(String configurationKey) {
        enabledViews = new ArrayList<>();
        viewItems = new HashMap<>();
        views = new HashMap<>();
        defaultViewsSelectors = new ArrayList<>();

        isEditable = false;
        this.configurationKey = configurationKey;
        this.viewsConfigurationKey = "";

        changingComboBoxLocker = new Object();
        changingComboBox = false;

        savedSelectedViewName = null;

        comboBoxModel = new SortedComboBoxModel<>();
        comboBoxSelectView = new JComboBox<>(comboBoxModel);
        comboBoxSelectView.addItemListener(this);

        panelViews = new JPanel(new CardLayout());
    }

    public HttpPanelComponentViewsManager(String configurationKey, String label) {
        this(configurationKey);

        comboBoxSelectView.setRenderer(
                new CustomDelegateListCellRenderer(comboBoxSelectView, label));
    }

    public HttpPanelComponentViewsManager(HttpPanel owner, String configurationKey) {
        this(configurationKey);
        this.owner = owner;
    }

    public HttpPanelComponentViewsManager(HttpPanel owner, String configurationKey, String label) {
        this(configurationKey, label);
        this.owner = owner;
    }

    public JComponent getSelectableViewsComponent() {
        return comboBoxSelectView;
    }

    public JPanel getViewsPanel() {
        return panelViews;
    }

    public void setSelected(boolean selected) {
        if (currentView != null) {
            currentView.setSelected(selected);
        }
    }

    private void switchView(final String name) {
        if (this.currentView != null && this.currentView.getCaptionName().equals(name)) {
            currentView.setSelected(true);
            if (owner != null) {
                owner.fireMessageViewChangedEvent(currentView, currentView);
            }
            return;
        }

        HttpPanelView view = views.get(name);

        if (view == null) {
            logger.info("No view found with name: {}", name);
            return;
        }

        HttpPanelView previousView = currentView;
        if (this.currentView != null) {
            this.currentView.setSelected(false);
            this.currentView.getModel().clear();
        }

        this.currentView = view;

        comboBoxModel.setSelectedItem(viewItems.get(name));

        this.currentView.getModel().setMessage(message);

        ((CardLayout) panelViews.getLayout()).show(panelViews, name);

        this.currentView.setSelected(true);

        if (owner != null) {
            owner.fireMessageViewChangedEvent(previousView, currentView);
        }
    }

    public void setMessage(Message aMessage) {
        this.message = aMessage;

        enableViews();

        String defaultViewName = getDefaultEnabledViewName();

        if (defaultViewName != null) {
            if (defaultViewName.equals(currentView.getName())) {
                currentView.getModel().setMessage(message);
            } else {
                switchView(defaultViewName);
            }
        } else if (!enabledViews.contains(viewItems.get(currentView.getName()))) {
            switchView(enabledViews.get(0).getConfigName());
        } else {
            currentView.getModel().setMessage(message);
        }
    }

    private void enableViews() {
        Iterator<Entry<String, HttpPanelView>> it = views.entrySet().iterator();
        while (it.hasNext()) {
            HttpPanelView view = it.next().getValue();

            ViewItem viewItem = viewItems.get(view.getName());

            if (!view.isEnabled(message)) {
                if (enabledViews.contains(viewItem)) {
                    disableView(viewItem);
                }
            } else if (!enabledViews.contains(viewItem)) {
                enableView(viewItem);
            }
        }
    }

    private String getDefaultEnabledViewName() {
        String defaultViewName = null;

        Iterator<HttpPanelDefaultViewSelector> itD = defaultViewsSelectors.iterator();
        while (itD.hasNext()) {
            HttpPanelDefaultViewSelector defaultView = itD.next();

            if (defaultView.matchToDefaultView(message)) {
                if (enabledViews.contains(viewItems.get(defaultView.getViewName()))) {
                    defaultViewName = defaultView.getViewName();
                    break;
                }
            }
        }

        return defaultViewName;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        synchronized (changingComboBoxLocker) {
            if (changingComboBox) {
                return;
            }
        }

        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (currentView == null) {
                return;
            }

            ViewItem item = (ViewItem) comboBoxModel.getSelectedItem();

            if (item == null || item.getConfigName().equals(currentView.getName())) {
                return;
            }

            try {
                save();
            } catch (InvalidMessageDataException e1) {
                comboBoxModel.setSelectedItem(viewItems.get(currentView.getName()));

                StringBuilder warnMessage = new StringBuilder(150);
                warnMessage.append(Constant.messages.getString("http.panel.view.warn.datainvalid"));

                String exceptionMessage = e1.getLocalizedMessage();
                if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                    warnMessage.append('\n').append(exceptionMessage);
                }
                View.getSingleton().showWarningDialog(warnMessage.toString());
                return;
            }
            switchView(item.getConfigName());
        }
    }

    /**
     * Saves the data shown in the views into the current message.
     *
     * <p>Has not effect if there's no view or message.
     *
     * @throws InvalidMessageDataException if unable to save the data (e.g. malformed).
     */
    public void save() {
        if (message == null || currentView == null) {
            return;
        }

        if (isEditable) {
            if (currentView.hasChanged()) {
                currentView.save();
            }
        }
    }

    public void addView(HttpPanelView view) {
        validateView(view);

        final String targetViewName = view.getTargetViewName();
        if (!"".equals(targetViewName) && views.containsKey(targetViewName)) {
            removeView(targetViewName);
        }

        final String viewConfigName = view.getName();

        views.put(viewConfigName, view);

        ViewItem viewItem = new ViewItem(viewConfigName, view.getCaptionName(), view.getPosition());
        viewItems.put(viewConfigName, viewItem);

        panelViews.add(view.getPane(), viewConfigName);
        view.setEditable(isEditable);
        view.setParentConfigurationKey(viewsConfigurationKey);

        if (view.isEnabled(message)) {
            enableView(viewItem);

            boolean switchView = false;
            if (currentView == null) {
                switchView = true;
            } else if (savedSelectedViewName != null) {
                if (savedSelectedViewName.equals(viewConfigName)) {
                    switchView = true;
                } else if (!savedSelectedViewName.equals(currentView.getName())
                        && currentView.getPosition() > view.getPosition()) {
                    switchView = true;
                }
            } else if (currentView.getPosition() > view.getPosition()) {
                switchView = true;
            }

            if (switchView) {
                switchView(viewConfigName);
            }
        }
    }

    private static void validateView(HttpPanelView view) {
        if (view == null) {
            throw new IllegalArgumentException("Attempting to add null view.");
        }

        validateNonEmpty(
                view.getName(), "The view should have a non-null and non-empty name.", view);
        validateNonEmpty(
                view.getCaptionName(),
                "The view should have a non-null and non-empty caption name.",
                view);
        validateNonNull(view.getPane(), "The view should have a pane.", view);
        validateNonNull(view.getModel(), "The view should have a model.", view);
    }

    private static void validateNonEmpty(String value, String message, HttpPanelView view) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                    message + " Classname: " + view.getClass().getCanonicalName());
        }
    }

    private static void validateNonNull(Object value, String message, HttpPanelView view) {
        if (value == null) {
            throw new IllegalArgumentException(
                    message + " Classname: " + view.getClass().getCanonicalName());
        }
    }

    private void enableView(ViewItem viewItem) {
        enabledViews.add(viewItem);
        Collections.sort(enabledViews);

        synchronized (changingComboBoxLocker) {
            changingComboBox = true;
            comboBoxModel.addElement(viewItem);
            changingComboBox = false;
        }
    }

    private void disableView(ViewItem viewItem) {
        enabledViews.remove(viewItem);

        synchronized (changingComboBoxLocker) {
            changingComboBox = true;
            comboBoxModel.removeElement(viewItem);
            changingComboBox = false;
        }
    }

    public void addView(HttpPanelView view, FileConfiguration fileConfiguration) {
        addView(view);

        view.loadConfiguration(fileConfiguration);
    }

    public void removeView(String viewName) {
        HttpPanelView view = views.get(viewName);
        if (view == null) {
            return;
        }

        views.remove(viewName);
        panelViews.remove(view.getPane());

        ViewItem viewItem = viewItems.get(viewName);
        if (enabledViews.contains(viewItem)) {
            disableView(viewItem);
        }

        viewItems.remove(view.getName());

        if (viewName.equals(currentView.getName())) {
            if (enabledViews.size() > 0) {
                switchView(enabledViews.get(0).getConfigName());
            } else {
                currentView = null;
            }
        }
    }

    public void clearView() {
        if (currentView != null) {
            currentView.getModel().clear();
            setMessage(null);
        }
    }

    public void clearView(boolean enableViewSelect) {
        clearView();
        setEnableViewSelect(enableViewSelect);
    }

    public void setEnableViewSelect(boolean enableViewSelect) {
        comboBoxSelectView.setEnabled(enableViewSelect);
    }

    public void addDefaultViewSelector(HttpPanelDefaultViewSelector defaultViewSelector) {
        defaultViewsSelectors.add(defaultViewSelector);
        Collections.sort(defaultViewsSelectors, getDefaultViewSelectorComparator());
    }

    public void removeDefaultViewSelector(String defaultViewSelectorName) {
        Iterator<HttpPanelDefaultViewSelector> itD = defaultViewsSelectors.iterator();
        while (itD.hasNext()) {
            HttpPanelDefaultViewSelector defaultView = itD.next();

            if (defaultView.getName().equals(defaultViewSelectorName)) {
                defaultViewsSelectors.remove(defaultView);
                break;
            }
        }
    }

    private static Comparator<HttpPanelDefaultViewSelector> getDefaultViewSelectorComparator() {
        if (defaultViewSelectorComparator == null) {
            createDefaultViewSelectorComparator();
        }
        return defaultViewSelectorComparator;
    }

    private static synchronized void createDefaultViewSelectorComparator() {
        if (defaultViewSelectorComparator == null) {
            defaultViewSelectorComparator = new DefaultViewSelectorComparator();
        }
    }

    public void setConfigurationKey(String parentKey) {
        configurationKey = parentKey + configurationKey + ".";
        viewsConfigurationKey = configurationKey + VIEWS_KEY + ".";

        Iterator<HttpPanelView> it = views.values().iterator();
        while (it.hasNext()) {
            it.next().setParentConfigurationKey(viewsConfigurationKey);
        }
    }

    public void loadConfig(FileConfiguration fileConfiguration) {
        savedSelectedViewName = fileConfiguration.getString(configurationKey + DEFAULT_VIEW_KEY);

        Iterator<HttpPanelView> it = views.values().iterator();
        while (it.hasNext()) {
            it.next().loadConfiguration(fileConfiguration);
        }

        if (savedSelectedViewName != null && views.containsKey(savedSelectedViewName)) {
            switchView(savedSelectedViewName);
        }
    }

    public void saveConfig(FileConfiguration fileConfiguration) {
        if (currentView != null) {
            fileConfiguration.setProperty(
                    configurationKey + DEFAULT_VIEW_KEY, currentView.getName());
        }

        Iterator<HttpPanelView> it = views.values().iterator();
        while (it.hasNext()) {
            it.next().saveConfiguration(fileConfiguration);
        }
    }

    public void setEditable(boolean editable) {
        if (isEditable != editable) {
            isEditable = editable;

            Iterator<HttpPanelView> it = views.values().iterator();
            while (it.hasNext()) {
                it.next().setEditable(editable);
            }
        }
    }

    public void highlight(SearchMatch sm) {
        if (currentView instanceof SearchableHttpPanelView) {
            ((SearchableHttpPanelView) currentView).highlight(sm);
        } else {
            SearchableHttpPanelView searchableView = findSearchableView();
            if (currentView != null) {
                switchView(((HttpPanelView) searchableView).getName());
                searchableView.highlight(sm);
            }
        }
    }

    public void search(Pattern p, List<SearchMatch> matches) {
        if (currentView instanceof SearchableHttpPanelView) {
            ((SearchableHttpPanelView) currentView).search(p, matches);
        } else {
            SearchableHttpPanelView searchableView = findSearchableView();
            if (searchableView != null) {
                searchableView.search(p, matches);
            }
        }
    }

    private SearchableHttpPanelView findSearchableView() {
        SearchableHttpPanelView searchableView = null;

        Iterator<HttpPanelView> it = views.values().iterator();
        while (it.hasNext()) {
            HttpPanelView view = it.next();
            if (view.isEnabled(message)) {
                if (view instanceof SearchableHttpPanelView) {
                    searchableView = (SearchableHttpPanelView) view;
                    break;
                }
            }
        }

        return searchableView;
    }

    private static final class ViewItem implements Comparable<ViewItem> {

        private final String configName;
        private String name;
        private final int position;

        public ViewItem(String configName, String name, int position) {
            this.configName = configName;
            this.name = name;
            this.position = position;
        }

        public String getConfigName() {
            return configName;
        }

        @Override
        public int compareTo(ViewItem o) {
            if (position < o.position) {
                return -1;
            } else if (position > o.position) {
                return 1;
            }

            return 0;
        }

        @Override
        public int hashCode() {
            return 31 * configName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ViewItem other = (ViewItem) obj;
            if (!configName.equals(other.configName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class CustomDelegateListCellRenderer
            implements ListCellRenderer<ViewItem> {

        private ListCellRenderer<? super ViewItem> delegateRenderer;
        private JComboBox<ViewItem> comboBox;
        private String label;

        private ViewItem viewItem;

        public CustomDelegateListCellRenderer(JComboBox<ViewItem> aComboBox, String label) {
            this.delegateRenderer = aComboBox.getRenderer();
            this.comboBox = aComboBox;
            this.label = label;

            this.viewItem = new ViewItem("", "", -1);

            this.comboBox.addPropertyChangeListener(
                    "UI",
                    new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            delegateRenderer = new JComboBox<>().getRenderer();
                        }
                    });
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ViewItem> list,
                ViewItem value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            if (index != -1) {
                return delegateRenderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
            }

            viewItem.name = label + value.name;
            return delegateRenderer.getListCellRendererComponent(
                    list, viewItem, index, isSelected, cellHasFocus);
        }
    }

    private static final class DefaultViewSelectorComparator
            implements Comparator<HttpPanelDefaultViewSelector>, Serializable {

        private static final long serialVersionUID = -1380844848294384189L;

        @Override
        public int compare(HttpPanelDefaultViewSelector o1, HttpPanelDefaultViewSelector o2) {
            final int order1 = o1.getOrder();
            final int order2 = o2.getOrder();
            if (order1 < order2) {
                return -1;
            } else if (order1 > order2) {
                return 1;
            }
            return 0;
        }
    }

    @Override
    public boolean supports(MessageLocation location) {
        for (ViewItem item : enabledViews) {
            HttpPanelView view = views.get(item.getConfigName());
            if (view instanceof MessageLocationHighlighter) {

                MessageLocationHighlighter highlighter = (MessageLocationHighlighter) view;
                if (highlighter.supports(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean supports(Class<? extends MessageLocation> classLocation) {
        for (ViewItem item : enabledViews) {
            HttpPanelView view = views.get(item.getConfigName());
            if (view instanceof MessageLocationHighlighter) {

                MessageLocationHighlighter highlighter = (MessageLocationHighlighter) view;
                if (highlighter.supports(classLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location) {
        if (currentView instanceof MessageLocationHighlighter) {

            MessageLocationHighlighter highlighter = (MessageLocationHighlighter) currentView;
            return highlighter.highlight(location);
        }
        return null;
    }

    @Override
    public MessageLocationHighlight highlight(
            MessageLocation location, MessageLocationHighlight highlight) {
        if (currentView instanceof MessageLocationHighlighter) {

            MessageLocationHighlighter highlighter = (MessageLocationHighlighter) currentView;
            return highlighter.highlight(location, highlight);
        }
        return null;
    }

    @Override
    public void removeHighlight(
            MessageLocation location, MessageLocationHighlight highlightReference) {
        if (currentView instanceof MessageLocationHighlighter) {

            MessageLocationHighlighter highlighter = (MessageLocationHighlighter) currentView;
            highlighter.removeHighlight(location, highlightReference);
        }
    }

    public HttpPanelView setSelectedView(String viewName) {
        for (ViewItem item : enabledViews) {
            if (viewName.equals(item.getConfigName())) {
                switchView(viewName);
                return currentView;
            }
        }
        return null;
    }
}
