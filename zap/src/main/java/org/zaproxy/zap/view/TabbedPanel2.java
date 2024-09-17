/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractFrame;
import org.parosproxy.paros.view.TabbedPanel;
import org.zaproxy.zap.utils.DisplayUtils;

/**
 * A tabbed panel that adds the option to hide individual tabs via a cross button on the tab.
 *
 * @since 2.2.0
 */
@SuppressWarnings("serial")
public class TabbedPanel2 extends TabbedPanel {

    private static final long serialVersionUID = 1L;
    private static final Comparator<Component> NAME_COMPARATOR =
            (c1, c2) -> c1.getName().compareTo(c2.getName());

    private List<Component> fullTabList = new ArrayList<>();
    private List<Component> hiddenTabs = new ArrayList<>();
    private Map<Component, DetachedTabFrame> detachedTabs = new HashMap<>();

    private final List<DetachedTabListener> detachedTabListeners = new ArrayList<>();

    private static final Icon PLUS_ICON = getImageIcon("/resource/icon/fugue/plus.png");
    private static final Icon UI_TAB_EXCLAMATION_ICON =
            getImageIcon("/resource/icon/fugue/ui-tab-exclamation.png");

    // A fake component that never actually get displayed - used for the 'hidden tab list tab'
    private Component hiddenComponent = new JLabel();

    private static final Logger LOGGER = LogManager.getLogger(TabbedPanel2.class);

    private int prevTabIndex = -1;

    public TabbedPanel2() {
        super();

        var noVisibleTabsMsgPanel = new AbstractPanel();
        noVisibleTabsMsgPanel.setName(Constant.messages.getString("tab.noVisibleTabs.title"));
        noVisibleTabsMsgPanel.setIcon(UI_TAB_EXCLAMATION_ICON);
        noVisibleTabsMsgPanel.setHideable(false);
        noVisibleTabsMsgPanel.setLayout(new GridBagLayout());
        var msgPanelTextArea = new JTextArea(2, 20);
        msgPanelTextArea.setWrapStyleWord(true);
        msgPanelTextArea.setLineWrap(true);
        msgPanelTextArea.setEditable(false);
        noVisibleTabsMsgPanel.add(msgPanelTextArea);

        addChangeListener(e -> toggleNoVisibleTabsMessage(noVisibleTabsMsgPanel, msgPanelTextArea));

        this.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        setCloseButtonStates();
                        if (getSelectedComponent() != null
                                && getSelectedComponent().equals(hiddenComponent)) {
                            // The 'hidden tab list tab' has been selected - this is a special case
                            if (prevTabIndex == indexOfComponent(hiddenComponent)) {
                                // Happens when we delete the tab to the left of the hidden one
                                setSelectedIndex(prevTabIndex - 1);
                            }
                        } else {
                            prevTabIndex = getSelectedIndex();
                        }
                    }
                });

        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (hiddenComponent.equals(getSelectedComponent())) {
                            setSelectedIndex(prevTabIndex);
                            showHiddenTabPopup();
                        }
                        int tabIndex = indexAtLocation(e.getX(), e.getY());
                        if (tabIndex != -1 && SwingUtilities.isRightMouseButton(e)) {
                            var tab = getTabComponentAt(tabIndex);
                            if (tab instanceof TabbedPanelTab) {
                                var contextMenu = ((TabbedPanelTab) tab).getContextMenu();
                                contextMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    }
                });
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (hiddenTabs != null) {
            hiddenTabs.forEach(tab -> SwingUtilities.updateComponentTreeUI(tab));
        }
    }

    private void toggleNoVisibleTabsMessage(AbstractPanel panel, JTextArea textArea) {
        SwingUtilities.invokeLater(
                () -> {
                    if (getTabCount() == 0) {
                        // All tabs were detached
                        textArea.setText(
                                Constant.messages.getString("tab.noVisibleTabs.allDetachedMsg"));
                        insertTab(panel.getName(), panel.getIcon(), panel, null, 0);
                        setSelectedIndex(0);
                    } else if (indexOfComponent(hiddenComponent) == 0) {
                        // All tabs were detached or hidden
                        textArea.setText(
                                Constant.messages.getString(
                                        "tab.noVisibleTabs.detachedOrHiddenMsg"));
                        insertTab(panel.getName(), panel.getIcon(), panel, null, 0);
                        setSelectedIndex(0);
                    } else if (!getVisiblePanels().isEmpty() && indexOfComponent(panel) != -1) {
                        // Some tabs are visible
                        removeTab(panel);
                    }
                });
    }

    /**
     * Show a popup containing a list of all of the hidden tabs - selecting one will reveal that tab
     */
    private void showHiddenTabPopup() {
        JPopupMenu menu = new JPopupMenu();
        if (getMousePosition() == null) {
            // Startup
            return;
        }
        Collections.sort(this.hiddenTabs, NAME_COMPARATOR);

        for (Component c : this.hiddenTabs) {
            if (c instanceof AbstractPanel) {
                final AbstractPanel ap = (AbstractPanel) c;
                JMenuItem mi = new JMenuItem(ap.getName());
                mi.setIcon(ap.getIcon());
                mi.addActionListener(
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                setVisible(ap, true);
                                ap.setTabFocus();
                            }
                        });
                menu.add(mi);
            }
        }
        menu.show(this, this.getMousePosition().x, this.getMousePosition().y);
    }

    private void setCloseButtonStates() {
        // Hide all 'close' buttons except for the selected tab
        for (int i = 0; i < this.getTabCount(); i++) {
            Component tabCom = this.getTabComponentAt(i);
            if (tabCom != null && tabCom instanceof TabbedPanelTab) {
                TabbedPanelTab jp = (TabbedPanelTab) tabCom;
                jp.setEnabled(i == getSelectedIndex());
            }
        }
    }

    public void pinVisibleTabs() {
        for (int i = 0; i < this.getTabCount(); i++) {
            Component tabCom = this.getTabComponentAt(i);
            if (tabCom != null && tabCom instanceof TabbedPanelTab && tabCom.isVisible()) {
                TabbedPanelTab jp = (TabbedPanelTab) tabCom;
                jp.setPinned(true);
                this.saveTabState(jp.getAbstractPanel());
            }
        }
    }

    public void unpinTabs() {
        for (int i = 0; i < this.getTabCount(); i++) {
            Component tabCom = this.getTabComponentAt(i);
            if (tabCom != null && tabCom instanceof TabbedPanelTab && tabCom.isVisible()) {
                TabbedPanelTab jp = (TabbedPanelTab) tabCom;
                jp.setPinned(false);
                this.saveTabState(jp.getAbstractPanel());
            }
        }
    }

    /**
     * Returns a name safe to be used in the XML config file.
     *
     * @param str the name to be made safe
     * @return a name safe to be used in XML
     */
    private String safeName(String str) {
        return str.replaceAll("[^A-Za-z0-9]", "");
    }

    private boolean isTabPinned(Component c) {
        boolean showByDefault = false;
        if (c instanceof AbstractPanel) {
            showByDefault = ((AbstractPanel) c).isShowByDefault();
        }
        return Model.getSingleton()
                .getOptionsParam()
                .getConfig()
                .getBoolean(
                        OptionsParamView.TAB_PIN_OPTION + "." + safeName(c.getName()),
                        showByDefault);
    }

    protected void saveTabState(AbstractPanel ap) {
        if (ap == null) {
            return;
        }
        Model.getSingleton()
                .getOptionsParam()
                .getConfig()
                .setProperty(
                        OptionsParamView.TAB_PIN_OPTION + "." + safeName(ap.getName()),
                        ap.isPinned());
        try {
            Model.getSingleton().getOptionsParam().getConfig().save();
        } catch (ConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /*
     * Returns true if the specified component is a visible tab panel (typically an AbstractPanel)
     */
    public boolean isTabVisible(Component c) {
        if (!this.fullTabList.contains(c)) {
            // Not a known tab
            return false;
        }
        return !this.hiddenTabs.contains(c);
    }

    /**
     * Sets whether or not the given component (and corresponding tab) is visible.
     *
     * <p><strong>Note:</strong> Non-{@link AbstractPanel#isHideable() hideable} and {@link
     * AbstractPanel#isPinned() pinned} panels are hidden as well.
     *
     * @param c the component to show or hide.
     * @param visible {@code true} if the tab of the component should be visible, {@code false}
     *     otherwise.
     */
    public void setVisible(Component c, boolean visible) {
        if (visible) {
            if (this.hiddenTabs.contains(c)) {

                if (c instanceof AbstractPanel) {
                    // Don't use the addTab(AbstractPanel) methods as we need to force visibility
                    AbstractPanel panel = (AbstractPanel) c;
                    this.addTab(
                            c.getName(),
                            panel.getIcon(),
                            panel,
                            panel.isHideable(),
                            true,
                            panel.getTabIndex());
                } else {
                    // Work out the index to add it back in
                    int index = this.fullTabList.indexOf(c);
                    while (index >= 0) {
                        if (index > 0
                                && !this.hiddenTabs.contains(this.fullTabList.get(index - 1))) {
                            // Found the first preceding tab that isn't hidden
                            break;
                        }
                        index--;
                    }

                    this.addTab(c.getName(), null, c, true, true, index);
                }
                this.hiddenTabs.remove(c);
                handleHiddenTabListTab();
            }
            return;
        }

        hideTab(c);
    }

    /**
     * Hides the tab of the given component.
     *
     * <p><strong>Note:</strong> Non-{@link AbstractPanel#isHideable() hideable} and {@link
     * AbstractPanel#isPinned() pinned} panels are hidden as well.
     *
     * @param component the component to hide.
     */
    private void hideTab(Component component) {
        if (hiddenTabs.contains(component)) {
            return;
        }
        hiddenTabs.add(component);

        int index = indexOfComponent(component);
        if (index != -1) {
            super.removeTabAt(index);
        }

        handleHiddenTabListTab();
    }

    void detachTab(Component component) {
        if (detachedTabs.containsKey(component) || !(component instanceof AbstractPanel)) {
            return;
        }

        var frame = new DetachedTabFrame((AbstractPanel) component, this);
        detachedTabListeners.forEach(listener -> listener.tabDetached(component));
        detachedTabs.put(component, frame);
        frame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        var parent = frame.getDetachedTabParent();
                        if (parent == null) {
                            return;
                        }
                        var panel = frame.getPanel();
                        // Add it back to the current parent
                        parent.addTab(
                                panel.getName(),
                                panel.getIcon(),
                                panel,
                                panel.isHideable(),
                                true,
                                frame.getLastIndex());
                        panel.setTabFocus();
                        parent.detachedTabs.remove(panel);
                        parent.detachedTabListeners.forEach(
                                listener -> listener.tabReattached(panel));
                        // Add it back to this panel too
                        if (TabbedPanel2.this != parent) {
                            fullTabList.add(panel);
                            detachedTabs.remove(panel);
                            detachedTabListeners.forEach(listener -> listener.tabReattached(panel));
                        }
                    }
                });
    }

    @Override
    public void addTab(String title, Icon icon, final Component c) {
        if (c instanceof AbstractPanel) {
            this.addTab((AbstractPanel) c);
        } else {
            this.addTab(title, icon, c, false, true, this.getTabCount());
        }
    }

    /**
     * Adds a tab with the given panel.
     *
     * <p>If the panel is {@link AbstractPanel#isHideable() hideable} and not-{@link
     * AbstractPanel#isPinned() pinned} the corresponding tab will be hidden, until the user (or
     * programmatically) makes it visible.
     *
     * @param panel the panel to add.
     * @see #addTabHidden(AbstractPanel)
     */
    public void addTab(AbstractPanel panel) {
        addTab(panel, panel.getTabIndex());
    }

    /**
     * Adds the given panel, whose tab will be hidden.
     *
     * <p>This method effectively overrides the {@link AbstractPanel#isHideable() hideable} and
     * {@link AbstractPanel#isPinned() pinned} state of the panel.
     *
     * @param panel the panel to add.
     * @since 2.8.0
     * @see #addTab(AbstractPanel)
     * @see #setVisible(Component, boolean)
     */
    public void addTabHidden(AbstractPanel panel) {
        this.addTab(
                panel.getName(),
                panel.getIcon(),
                panel,
                panel.isHideable(),
                false,
                panel.getTabIndex(),
                false);
    }

    /**
     * Adds a tab with the given panel at the given index.
     *
     * <p>This method effectively overrides the {@link AbstractPanel#getTabIndex() index of the
     * panel}.
     *
     * <p>If the panel is {@link AbstractPanel#isHideable() hideable} and not-{@link
     * AbstractPanel#isPinned() pinned} the corresponding tab will be hidden, until the user (or
     * programmatically) makes it visible.
     *
     * @param panel the panel for the added tab.
     * @param index the index at the tabbed pane.
     * @since 2.8.0
     */
    public void addTab(AbstractPanel panel, int index) {
        boolean visible = !panel.isHideable() || this.isTabPinned(panel);
        this.addTab(panel.getName(), panel.getIcon(), panel, panel.isHideable(), visible, index);
    }

    /**
     * Adds a tab with the given component.
     *
     * @param title the title of the tab.
     * @param icon the icon of the tab.
     * @param c the component of the tab.
     * @param hideable {@code true} if the tab can be hidden, {@code false} otherwise.
     * @param visible {@code true} if the tab should be visible, {@code false} otherwise.
     * @param index the index of the tab.
     */
    public void addTab(
            String title,
            Icon icon,
            final Component c,
            boolean hideable,
            boolean visible,
            int index) {
        addTab(title, icon, c, hideable, visible, index, true);
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        super.insertTab(title, icon, component, tip, index);
        if (!isPlusTab(icon)
                && !isNoVisibleTabsMessageTab(icon)
                && !this.fullTabList.contains(component)) {
            this.fullTabList.add(component);
        }
    }

    private boolean isPlusTab(Icon icon) {
        return icon == PLUS_ICON;
    }

    private boolean isNoVisibleTabsMessageTab(Icon icon) {
        return icon == UI_TAB_EXCLAMATION_ICON;
    }

    /**
     * Adds a tab with the given component.
     *
     * @param title the title of the tab.
     * @param icon the icon of the tab.
     * @param c the component of the tab.
     * @param hideable {@code true} if the tab can be hidden, {@code false} otherwise.
     * @param visible {@code true} if the tab should be visible, {@code false} otherwise.
     * @param index the index of the tab.
     * @param reparent {@code true} if the component should have as parent this {@code
     *     TabbedPanel2}, {@code false} otherwise.
     */
    private void addTab(
            String title,
            Icon icon,
            final Component c,
            boolean hideable,
            boolean visible,
            int index,
            boolean reparent) {
        if (!this.fullTabList.contains(c)) {
            this.fullTabList.add(c);
        }

        if (c instanceof AbstractPanel) {
            if (reparent) {
                ((AbstractPanel) c).setParent(this);
            }
            ((AbstractPanel) c).setTabIndex(index);
            ((AbstractPanel) c).setHideable(hideable);
        }

        if (!visible) {
            hideTab(c);
            return;
        }

        this.hiddenTabs.remove(c);

        if (index == -1 || index > this.getTabCount()) {
            index = this.getTabCount();
        }
        if (icon instanceof ImageIcon) {
            icon = DisplayUtils.getScaledIcon((ImageIcon) icon);
        }

        super.insertTab(title, icon, c, c.getName(), index);

        int pos = this.indexOfComponent(c);
        // Now assign the component for the tab

        this.setTabComponentAt(
                pos, new TabbedPanelTab(this, title, icon, c, hideable, this.isTabPinned(c)));

        handleHiddenTabListTab();

        if ((index == 0 || getTabCount() == 1) && indexOfComponent(c) != -1) {
            // Its now the first one, give it focus
            setSelectedComponent(c);
        }
    }

    private void handleHiddenTabListTab() {
        if (indexOfComponent(hiddenComponent) >= 0) {
            // Tab is showing, remove it - it might not be needed or may no longer be at the end
            super.remove(hiddenComponent);
        }
        if (this.hiddenTabs.size() > 0) {
            // Only re-add tab if there are hidden ones
            super.addTab("", PLUS_ICON, hiddenComponent);
        }
    }

    public boolean hasDetachedTabs() {
        return !detachedTabs.isEmpty();
    }

    public void closeAllDetachedTabs() {
        for (var frame : detachedTabs.values()) {
            frame.reattach();
        }
    }

    public Map<Component, DetachedTabFrame> getDetachedTabs() {
        return detachedTabs;
    }

    public void setDetachedTabs(Set<DetachedTabFrame> detachedTabs) {
        fullTabList.addAll(this.detachedTabs.keySet());
        this.detachedTabs.clear();
        for (var frame : detachedTabs) {
            this.detachedTabs.put(frame.getPanel(), frame);
            removeFromInternalState(frame.getPanel());
            frame.setDetachedTabParent(this);
        }
    }

    /**
     * Temporarily locks/unlocks the specified tab, e.g. if its active and mustn't be closed.
     *
     * <p>Locked (AbstractPanel) tabs will not have the pin/close tab buttons displayed.
     *
     * @param panel the panel being changed
     * @param lock {@code true} if the panel should be locked, {@code false} otherwise.
     */
    public void setTabLocked(AbstractPanel panel, boolean lock) {
        for (int i = 0; i < this.getTabCount(); i++) {
            Component tabCom = this.getTabComponentAt(i);
            if (tabCom != null && tabCom instanceof TabbedPanelTab && tabCom.isVisible()) {
                TabbedPanelTab jp = (TabbedPanelTab) tabCom;
                if (panel.equals(jp.getAbstractPanel())) {
                    jp.setLocked(!lock);
                }
            }
        }
    }

    @Override
    public void setIconAt(int index, Icon icon) {
        Component tabCom = this.getTabComponentAt(index);
        if (tabCom != null && tabCom instanceof JPanel) {
            Component c = ((JPanel) tabCom).getComponent(0);
            if (c != null && c instanceof JLabel) {
                ((JLabel) c).setIcon(icon);
            }
        }
    }

    /** Set the title of the tab when hiding/showing tab names. */
    @Override
    public void setTitleAt(int index, String title) {
        Component tabCom = this.getTabComponentAt(index);
        if (tabCom != null && tabCom instanceof JPanel) {
            Component c = ((JPanel) tabCom).getComponent(0);
            if (c != null && c instanceof JLabel) {
                ((JLabel) c).setText(title);
            }
        } else {
            super.setTitleAt(index, title);
        }
    }

    public List<Component> getTabList() {
        return Collections.unmodifiableList(this.fullTabList);
    }

    public List<Component> getSortedTabList() {
        List<Component> copy = new ArrayList<>(this.fullTabList);
        Collections.sort(copy, NAME_COMPARATOR);
        return copy;
    }

    /**
     * Removes the given panel and corresponding tab.
     *
     * @param panel the panel to remove.
     * @see #setVisible(Component, boolean)
     */
    public void removeTab(AbstractPanel panel) {
        if (this.detachedTabs.containsKey(panel)) {
            detachedTabs.get(panel).reattach();
        }
        this.remove(panel);
        // Ensure its removed from internal state (for when the tab is not visible).
        removeFromInternalState(panel);
    }

    /** Removes the tab at the given index and the corresponding panel. */
    @Override
    public void removeTabAt(int index) {
        if (index < 0 || index >= getTabCount()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Tab count: " + getTabCount());
        }

        Component component = getComponentAt(index);
        super.removeTabAt(index);

        if (!(component instanceof AbstractPanel)) {
            return;
        }

        removeFromInternalState(component);
    }

    /**
     * Removes the given panel/component from the internal state, that is, from {@link #fullTabList}
     * and {@link #hiddenTabs}.
     *
     * @param panel the panel/component to remove.
     */
    private void removeFromInternalState(Component panel) {
        this.fullTabList.remove(panel);
        if (this.hiddenTabs.remove(panel)) {
            handleHiddenTabListTab();
        }
    }

    /**
     * Removes all tabs and corresponding panels.
     *
     * @see #hideAllTabs()
     */
    @Override
    public void removeAll() {
        fullTabList.clear();
        hiddenTabs.clear();
        detachedTabs.clear();

        removeAllTabs();
        closeAllDetachedTabs();
    }

    /** Internal method that removes all tabs from the UI component. */
    private void removeAllTabs() {
        setSelectedIndex(-1);
        int tabCount = getTabCount();
        while (tabCount-- > 0) {
            super.removeTabAt(tabCount);
        }

        handleHiddenTabListTab();
    }

    /**
     * Hides all tabs.
     *
     * <p><strong>Note:</strong> Non-{@link AbstractPanel#isHideable() hideable} and {@link
     * AbstractPanel#isPinned() pinned} panels are hidden as well.
     *
     * @see #setPanelsVisible(boolean)
     * @see #setVisiblePanels(List)
     * @since 2.8.0
     */
    public void hideAllTabs() {
        hiddenTabs.clear();
        hiddenTabs.addAll(fullTabList);

        removeAllTabs();
    }

    /**
     * Sets whether or not the tab names should be shown.
     *
     * @param showTabNames {@code true} if the tab names should be shown, {@code false} otherwise.
     * @since 2.4.0
     */
    public void setShowTabNames(boolean showTabNames) {
        for (int i = 0; i < getTabCount(); i++) {
            String title = showTabNames ? getComponentAt(i).getName() : "";
            setTitleAt(i, title);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to call the method {@code AbstractPanel#tabSelected()} on the currently
     * selected {@code AbstractPanel}, if any.
     *
     * @see AbstractPanel#tabSelected()
     */
    @Override
    protected void fireStateChanged() {
        super.fireStateChanged();

        Component comp = getSelectedComponent();
        if (comp instanceof AbstractPanel) {
            ((AbstractPanel) comp).tabSelected();
        }
    }

    /**
     * Returns true if the tab is 'active' - i.e. is being used for anything. This method always
     * returns false so must be overridden to be changed
     *
     * @return {@code true} if the tab is active, {@code false} otherwise
     */
    public boolean isActive() {
        return false;
    }

    /**
     * Gets all the {@code AbstractPanel}s.
     *
     * @return a {@code List} containing all the panels
     * @since 2.5.0
     * @see #getVisiblePanels()
     */
    public List<AbstractPanel> getPanels() {
        List<AbstractPanel> panels = new ArrayList<>();
        for (Component component : fullTabList) {
            if (component instanceof AbstractPanel) {
                panels.add((AbstractPanel) component);
            }
        }
        return panels;
    }

    /**
     * Gets all the {@code AbstractPanel}s that are currently visible.
     *
     * @return a {@code List} containing all the visible panels
     * @since 2.5.0
     * @see #getPanels()
     */
    public List<AbstractPanel> getVisiblePanels() {
        return fullTabList.stream()
                .filter(AbstractPanel.class::isInstance)
                .map(AbstractPanel.class::cast)
                .filter(Predicate.not(hiddenTabs::contains))
                .collect(Collectors.toList());
    }

    /**
     * Sets the given {@code panels} as visible, while hiding the remaining panels.
     *
     * <p>Any panel that cannot be hidden (per {@link AbstractPanel#isHideable()} and {@link
     * AbstractPanel#isPinned()}) will still be shown, even if the panel was not in the given {@code
     * panels}, moreover {@code panels} that are not currently added to this tabbed panel are
     * ignored.
     *
     * @param panels the panels that should be visible
     * @since 2.5.0
     * @see #getVisiblePanels()
     * @see #hideAllTabs()
     */
    public void setVisiblePanels(List<AbstractPanel> panels) {
        hideAllTabs();

        for (Component component : fullTabList) {
            if (panels.contains(component)) {
                setVisible(component, true);
            } else if (component instanceof AbstractPanel) {
                AbstractPanel ap = (AbstractPanel) component;
                if (!canHidePanel(ap)) {
                    setVisible(component, true);
                } else {
                    ap.setParent(this);
                }
            }
        }

        if (getSelectedComponent() == null && getTabCount() > 0) {
            setSelectedIndex(0);
        }
    }

    /**
     * Sets whether or not the panels should be visible.
     *
     * <p>{@link AbstractPanel#isHideable() Non-hideable} and {@link AbstractPanel#isPinned()
     * pinned} panels are not affected by this call, when set to not be visible.
     *
     * @param visible {@code true} if all panels should be visible, {@code false} otherwise.
     * @since 2.5.0
     * @see #getVisiblePanels()
     * @see #hideAllTabs()
     */
    public void setPanelsVisible(boolean visible) {
        for (Component component : fullTabList) {
            if (component instanceof AbstractPanel) {
                AbstractPanel ap = (AbstractPanel) component;
                boolean canChangeVisibility = true;
                if (!visible) {
                    canChangeVisibility = canHidePanel(ap);
                }

                if (canChangeVisibility) {
                    setVisible(component, visible);
                }
            }
        }
    }

    public void addDetachedTabListener(DetachedTabListener listener) {
        detachedTabListeners.add(listener);
    }

    public void removeDetachedTabListener(DetachedTabListener listener) {
        detachedTabListeners.remove(listener);
    }

    /**
     * Tells whether or not the given panel can be hidden.
     *
     * <p>A panel can be hidden if it is {@link AbstractPanel#isHideable() hideable} and it's not
     * {@link AbstractPanel#isPinned() pinned}.
     *
     * @param panel the panel to be checked
     * @return {@code true} if the panel can be hidden, {@code false} otherwise
     */
    private static boolean canHidePanel(AbstractPanel panel) {
        return panel.isHideable() && !panel.isPinned();
    }

    private static ImageIcon getImageIcon(String resourceName) {
        return DisplayUtils.getScaledIcon(
                new ImageIcon(TabbedPanel2.class.getResource(resourceName)));
    }

    public static class DetachedTabFrame extends AbstractFrame {
        TabbedPanel2 detachedTabParent;
        AbstractPanel panel;
        int lastIndex;

        public DetachedTabFrame(AbstractPanel panel, TabbedPanel2 detachedTabParent) {
            super(getPrefnzPrefix(panel.getName()));
            this.panel = panel;
            this.detachedTabParent = detachedTabParent;
            lastIndex = detachedTabParent.indexOfComponent(panel);

            setTitle(panel.getName());
            getContentPane().add(panel);
            setVisible(true);
        }

        public void reattach() {
            for (var listener : getWindowListeners()) {
                listener.windowClosing(null);
            }
            dispose();
        }

        public int getLastIndex() {
            return lastIndex;
        }

        public AbstractPanel getPanel() {
            return panel;
        }

        public TabbedPanel2 getDetachedTabParent() {
            return detachedTabParent;
        }

        public void setDetachedTabParent(TabbedPanel2 detachedTabParent) {
            this.detachedTabParent = detachedTabParent;
        }

        private static String getPrefnzPrefix(String title) {
            return DetachedTabFrame.class.getSimpleName()
                    + "."
                    + title.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9]+", "_")
                    + ".";
        }
    }

    public interface DetachedTabListener {
        void tabDetached(Component c);

        void tabReattached(Component c);
    }
}
