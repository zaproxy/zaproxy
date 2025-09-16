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
// ZAP: 2011/05/31 Added option to dynamically change the display
// ZAP: 2011/07/25 Added automatically save/restore of divider locations
// ZAP: 2013/02/17 Issue 496: Allow to see the request and response at the same
// time in the main window
// ZAP: 2013/02/26 Issue 540: Maximised work tabs hidden when response tab
// position changed
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Removed redundant final modifiers from private methods
// ZAP: 2013/12/13 Added support for 'Full Layout'.
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts
// ZAP: 2014/10/07 Issue 1357: Hide unused tabs
// ZAP: 2015/02/11 Ensure that a tab is always selected when the layout is switched
// ZAP: 2015/12/14 Disable request/response tab buttons location when in full layout
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2017/08/30 Add tool tip to response tab.
// ZAP: 2018/02/14 Add new layout ResponsePanelPosition.TAB_SIDE_BY_SIDE (Issue 4331).
// ZAP: 2018/04/03 Update for behavioural changes in TabbedPanel2.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/02 Add OneTouchExapandable control to Sites Tree/Request&Response panels
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/02/26 Remove deprecated methods in 2.5.0
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
// ZAP: 2023/01/10 Tidy up logger.
// ZAP: 2025/03/19 Make getTabbedFull public.
package org.parosproxy.paros.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.ComponentMaximiser;
import org.zaproxy.zap.view.ComponentMaximiserMouseListener;
import org.zaproxy.zap.view.TabbedPanel2;

/**
 * A workbench panel, responsible to show and manage the main panels.
 *
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class WorkbenchPanel extends JPanel {

    /**
     * The layouts available to a {@code WorkbenchPanel}.
     *
     * @since 2.5.0
     * @see ResponsePanelPosition
     */
    public enum Layout {
        /**
         * Divides the panel in 3 areas, {@code select}, {@code status} and {@code work}, containing
         * the {@link WorkbenchPanel.PanelType#SELECT SELECT}, {@link
         * WorkbenchPanel.PanelType#STATUS STATUS} and {@link WorkbenchPanel.PanelType#WORK WORK}
         * panels, respectively. The {@code select} area occupies the left portion of the workbench
         * panel while the areas {@code work} and {@code status} occupy the top-right and
         * bottom-right portions of the workbench panel, respectively.
         *
         * <p>All the areas can be resized (using {@code JSplitPane}s) and maximised (using {@link
         * ComponentMaximiser}).
         */
        EXPAND_SELECT(0),

        /**
         * The default layout, divides the panel in 3 areas, {@code select}, {@code status} and
         * {@code work}, containing the {@link WorkbenchPanel.PanelType#SELECT SELECT}, {@link
         * WorkbenchPanel.PanelType#STATUS STATUS} and {@link WorkbenchPanel.PanelType#WORK WORK}
         * panels, respectively. The {@code status} area is located at the bottom of the workbench
         * panel while the areas {@code select} and {@code work} are located in the top-left and
         * top-right portions of the workbench panel, respectively.
         *
         * <p>All the areas can be resized (using {@code JSplitPane}s) and maximised (using {@link
         * ComponentMaximiser}).
         */
        EXPAND_STATUS(1),

        /**
         * A layout that has only one area that occupies the whole workbench panel and where all
         * panels ( {@link WorkbenchPanel.PanelType#SELECT SELECT}, {@link
         * WorkbenchPanel.PanelType#STATUS STATUS} and {@link WorkbenchPanel.PanelType#WORK WORK})
         * are shown.
         *
         * <p>Since it already occupies the whole workbench panel the area can not be resized nor
         * maximised.
         */
        FULL(2);

        /** The ID of the layout. */
        private final int id;

        private Layout(int id) {
            this.id = id;
        }

        /**
         * Gets the ID of the layout.
         *
         * <p>Unique among all layouts, thus suitable for persistence of options.
         *
         * @return the ID of the layout
         * @see #getLayout(int)
         */
        public int getId() {
            return id;
        }

        /**
         * Gets the {@code Layout} corresponding to the given ID.
         *
         * @param id the ID of the layout
         * @return the {@code Layout} corresponding to the given ID, or {@link #EXPAND_STATUS} if
         *     the ID is unknown.
         */
        public static Layout getLayout(int id) {
            if (id == Layout.EXPAND_SELECT.getId()) {
                return Layout.EXPAND_SELECT;
            }
            if (id == Layout.EXPAND_STATUS.getId()) {
                return Layout.EXPAND_STATUS;
            }
            if (id == Layout.FULL.getId()) {
                return Layout.FULL;
            }
            return Layout.EXPAND_STATUS;
        }
    }

    /**
     * The position of the response panel, with respect to the request panel.
     *
     * <p>The position of the response panel can be changed dynamically but might not be used by all
     * layouts, for example, the Full Layout.
     *
     * @since 2.5.0
     * @see Layout
     */
    public enum ResponsePanelPosition {
        /** Request and response panels are shown, side-by-side, in the same tabbed panel. */
        TABS_SIDE_BY_SIDE,

        /**
         * Request panel is shown above the response panel, in different tabbed panels.
         *
         * <p>Not supported in {@link Layout#FULL FULL layout}.
         */
        PANEL_ABOVE,

        /**
         * Request and response panels are shown, side-by-side, in different tabbed panels.
         *
         * <p>Not supported in {@link Layout#FULL FULL layout}.
         */
        PANELS_SIDE_BY_SIDE,

        /**
         * Request and response panels are shown, side-by-side, in the same tab.
         *
         * @since 2.8.0
         */
        TAB_SIDE_BY_SIDE
    }

    /**
     * The type of panels added to the {@code WorkbenchPanel}, used as hint when doing the layout of
     * the panels.
     *
     * <p>Panels of the same type might be shown in the same area of the workbench panel. Some
     * layouts, like {@link Layout#FULL}, might choose to ignore the type of the panels.
     *
     * @since 2.5.0
     */
    public enum PanelType {
        /**
         * The panels that provide status/data about extensions/components, for example, History
         * panel.
         */
        STATUS,

        /**
         * The panels that allow to select/display general data, for example, Sites or Scripts
         * panels.
         */
        SELECT,

        /**
         * The panels that allow to display/manipulate common data, for example, Request and
         * Response panels.
         */
        WORK
    }

    private static final long serialVersionUID = -4610792807151921550L;

    private static final Logger LOGGER = LogManager.getLogger(WorkbenchPanel.class);

    private static final String PREF_DIVIDER_LOCATION = "divider.location";
    private static final String DIVIDER_VERTICAL = "vertical";
    private static final String DIVIDER_HORIZONTAL = "horizontal";

    private final Preferences preferences;
    private final String prefnzPrefix = this.getClass().getSimpleName() + ".";

    /**
     * The request panel, used for positioning the response panel.
     *
     * @see #setResponsePanelPosition(ResponsePanelPosition)
     */
    private final AbstractPanel requestPanel;

    /**
     * The response panel, moved around depending on the option {@code response panel position}.
     *
     * @see #setResponsePanelPosition(ResponsePanelPosition)
     */
    private final AbstractPanel responsePanel;

    /**
     * A tabbed panel to show the response panel, when it is shown separately from other panels.
     *
     * <p>Main purpose is to show the request and response panels at the same time (in different
     * tabs).
     *
     * @see #setResponsePanelPosition(ResponsePanelPosition)
     */
    private final TabbedPanel2 responseTabbedPanel;

    /**
     * The index of the {@link #requestPanel} in the tabbed pane of the current {@link #layout}.
     *
     * @see #getCurrentRequestTabIndex()
     */
    private int requestTabIndex;

    /**
     * A {@code JSplitPane} to show the request and response in the same tab.
     *
     * <p>Lazily initialised.
     *
     * @see ResponsePanelPosition#TAB_SIDE_BY_SIDE
     * @see #splitResponsePanelWithRequestPanel(int, TabbedPanel2)
     */
    private JSplitPane splitRequestAndResponse;

    /**
     * The panel that shows the request and response in the same tab.
     *
     * <p>Lazily initialised.
     *
     * @see ResponsePanelPosition#TAB_SIDE_BY_SIDE
     * @see #splitResponsePanelWithRequestPanel(int, TabbedPanel2)
     */
    private AbstractPanel splitRequestAndResponsePanel;

    /**
     * The object to maximise the components when in some layouts.
     *
     * <p>The components are not maximised in Full Layout, they already occupy the whole workbench
     * panel.
     */
    private final ComponentMaximiser componentMaximiser;

    /**
     * The layout of the workbench panel.
     *
     * @see #setWorkbenchLayout(Layout)
     */
    private Layout layout;

    /**
     * The position of the response panel.
     *
     * @see #setResponsePanelPosition(ResponsePanelPosition)
     */
    private ResponsePanelPosition responsePanelPosition;

    /**
     * The tabbed panel for {@link PanelType#STATUS STATUS} panels.
     *
     * <p>Lazily initialised.
     *
     * @see #getTabbedStatus()
     */
    private TabbedPanel2 tabbedStatus;

    /**
     * The {@code JPanel} that has the tabbed panel {@link #tabbedStatus}, represents the {@code
     * status} area.
     *
     * <p>Lazily initialised.
     *
     * @see #getPaneStatus()
     */
    private JPanel paneStatus;

    /**
     * The tabbed panel for {@link PanelType#SELECT SELECT} panels.
     *
     * <p>Lazily initialised.
     *
     * @see #getTabbedSelect()
     */
    private TabbedPanel2 tabbedSelect;

    /**
     * The {@code JPanel} that has the tabbed panel {@link #tabbedSelect}, represents the {@code
     * select} area.
     *
     * <p>Lazily initialised.
     *
     * @see #getPaneSelect()
     */
    private JPanel paneSelect;

    /**
     * The tabbed panel for {@link PanelType#WORK WORK} panels.
     *
     * <p>Lazily initialised.
     *
     * @see #getTabbedWork()
     */
    private TabbedPanel2 tabbedWork;

    /**
     * The {@code JPanel} that has the tabbed panel {@link #tabbedWork}, represents the {@code work}
     * area.
     *
     * <p>Lazily initialised.
     *
     * @see #getPaneWork()
     */
    private JPanel paneWork;

    /**
     * The tabbed panel for all {@link PanelType types} of panels, when in {@link Layout#FULL FULL}
     * layout.
     *
     * <p>Lazily initialised.
     *
     * @see #getTabbedFull()
     */
    private TabbedPanel2 tabbedFull;

    /**
     * The {@code JPanel} that has the tabbed panel {@link #tabbedFull}, that is, the {@link
     * Layout#FULL FULL} layout.
     *
     * <p>Lazily initialised.
     *
     * @see #getFullLayoutPanel()
     */
    private JPanel fullLayoutPanel;

    /**
     * Flag that indicates whether or not the tabs should show the panels' names.
     *
     * <p>When false only the icons are shown, otherwise it is shown the icons and the names.
     */
    private boolean showTabNames;

    private final Map<PanelType, Set<AbstractPanel>> layoutPanels =
            Map.of(
                    PanelType.SELECT, new HashSet<>(),
                    PanelType.STATUS, new HashSet<>(),
                    PanelType.WORK, new HashSet<>());

    /**
     * Constructs a {@code WorkbenchPanel} with the given options and request and response panels.
     *
     * @param viewOptions the options
     * @param requestPanel the request panel
     * @param responsePanel the response panel
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     * @since 2.5.0
     */
    public WorkbenchPanel(
            OptionsParamView viewOptions, AbstractPanel requestPanel, AbstractPanel responsePanel) {
        super(new BorderLayout());

        validateNotNull(viewOptions, "viewOptions");
        validateNotNull(requestPanel, "requestPanel");
        validateNotNull(responsePanel, "responsePanel");

        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
        layoutPanels.get(PanelType.WORK).add(requestPanel);
        layoutPanels.get(PanelType.WORK).add(responsePanel);

        this.componentMaximiser = new ComponentMaximiser(this);
        this.showTabNames = true;

        ComponentMaximiserMouseListener maximiseMouseListener =
                new ComponentMaximiserMouseListener(viewOptions, componentMaximiser);

        responseTabbedPanel = new TabbedPanel2();
        responseTabbedPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseTabbedPanel.addMouseListener(maximiseMouseListener);
        responseTabbedPanel.addDetachedTabListener(
                new TabbedPanel2.DetachedTabListener() {
                    @Override
                    public void tabDetached(Component c) {
                        getPaneWork().removeAll();
                        getPaneWork().add(getTabbedWork());
                        getPaneWork().validate();
                    }

                    @Override
                    public void tabReattached(Component c) {
                        setResponsePanelPosition(responsePanelPosition);
                    }
                });

        getTabbedWork().addMouseListener(maximiseMouseListener);
        getTabbedStatus().addMouseListener(maximiseMouseListener);
        getTabbedSelect().addMouseListener(maximiseMouseListener);

        addPanel(requestPanel, PanelType.WORK);
        addPanel(responsePanel, PanelType.WORK);

        this.preferences = Preferences.userNodeForPackage(getClass());

        requestTabIndex = -1;
        setResponsePanelPosition(ResponsePanelPosition.TABS_SIDE_BY_SIDE);
        setWorkbenchLayout(Layout.EXPAND_STATUS);
    }

    /**
     * Validates that the given {@code parameter} is not {@code null}, throwing an {@code
     * IllegalArgumentException} if it is.
     *
     * <p>The given parameter name is used in the exception message to indicate which parameter must
     * not be {@code null}.
     *
     * @param parameter the parameter to be validated
     * @param parameterName the name of the parameter
     * @throws IllegalArgumentException if the parameter is {@code null}.
     */
    private static void validateNotNull(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter " + parameterName + " must not be null");
        }
    }

    /**
     * Gets the layout of the workbench panel.
     *
     * @return the layout, never {@code null}
     * @since 2.5.0
     */
    public Layout getWorkbenchLayout() {
        return layout;
    }

    /**
     * Sets the layout of the workbench panel.
     *
     * @param layout the layout to set
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     */
    void setWorkbenchLayout(Layout layout) {
        validateNotNull(layout, "layout");

        if (this.layout == layout) {
            return;
        }

        requestTabIndex = getCurrentRequestTabIndex();

        Layout previousLayout = this.layout;
        this.layout = layout;

        componentMaximiser.unmaximiseComponent();
        removeAll();

        List<AbstractPanel> visiblePanels;
        Set<TabbedPanel2.DetachedTabFrame> detachedTabs = new HashSet<>();
        switch (layout) {
            case FULL:
                visiblePanels = getTabbedStatus().getVisiblePanels();
                detachedTabs.addAll(getTabbedStatus().getDetachedTabs().values());
                getTabbedStatus().hideAllTabs();

                visiblePanels.addAll(getTabbedWork().getVisiblePanels());
                detachedTabs.addAll(getTabbedWork().getDetachedTabs().values());
                getTabbedWork().hideAllTabs();

                visiblePanels.addAll(getTabbedSelect().getVisiblePanels());
                detachedTabs.addAll(getTabbedSelect().getDetachedTabs().values());
                getTabbedSelect().hideAllTabs();

                detachedTabs.addAll(responseTabbedPanel.getDetachedTabs().values());
                responseTabbedPanel.hideAllTabs();

                getTabbedFull().setDetachedTabs(detachedTabs);
                getTabbedFull().setVisiblePanels(visiblePanels);

                updateFullLayout();
                this.add(getFullLayoutPanel());
                break;
            case EXPAND_SELECT:
            case EXPAND_STATUS:
            default:
                this.add(
                        layout == Layout.EXPAND_STATUS
                                ? createStatusPanelsSplit()
                                : createSelectPanelsSplit());

                if (previousLayout == Layout.FULL) {
                    visiblePanels = getTabbedFull().getVisiblePanels();
                    detachedTabs = Set.copyOf(getTabbedFull().getDetachedTabs().values());
                    getTabbedFull().hideAllTabs();

                    getTabbedStatus()
                            .setDetachedTabs(
                                    detachedTabs.stream()
                                            .filter(
                                                    t ->
                                                            layoutPanels
                                                                    .get(PanelType.STATUS)
                                                                    .contains(t.getPanel()))
                                            .collect(Collectors.toUnmodifiableSet()));
                    getTabbedStatus().setVisiblePanels(visiblePanels);

                    getTabbedWork()
                            .setDetachedTabs(
                                    detachedTabs.stream()
                                            .filter(
                                                    t ->
                                                            layoutPanels
                                                                    .get(PanelType.WORK)
                                                                    .contains(t.getPanel()))
                                            .collect(Collectors.toUnmodifiableSet()));
                    getTabbedWork().setVisiblePanels(visiblePanels);

                    getTabbedSelect()
                            .setDetachedTabs(
                                    detachedTabs.stream()
                                            .filter(
                                                    t ->
                                                            layoutPanels
                                                                    .get(PanelType.SELECT)
                                                                    .contains(t.getPanel()))
                                            .collect(Collectors.toUnmodifiableSet()));
                    getTabbedSelect().setVisiblePanels(visiblePanels);

                    if (responsePanelPosition == ResponsePanelPosition.PANELS_SIDE_BY_SIDE
                            || responsePanelPosition == ResponsePanelPosition.PANEL_ABOVE) {
                        var detachedtab = getTabbedWork().getDetachedTabs().remove(responsePanel);
                        if (detachedtab != null) {
                            responseTabbedPanel.setDetachedTabs(Set.of(detachedtab));
                        }
                    }

                    setResponsePanelPosition(responsePanelPosition);
                }
                break;
        }

        this.validate();
        this.repaint();
    }

    /**
     * Gets index of the {@link #requestPanel} in the tabbed pane of the current {@link #layout}.
     *
     * <p><strong>Note:</strong> This should be called prior changing the layout or position of the
     * response panel, as it affects the actual position of request panel.
     *
     * @return the index of the request panel.
     */
    private int getCurrentRequestTabIndex() {
        if (layout == Layout.FULL) {
            if (responsePanelPosition == ResponsePanelPosition.TAB_SIDE_BY_SIDE) {
                return getTabbedFull().indexOfComponent(splitRequestAndResponsePanel);
            }
            return getTabbedFull().indexOfComponent(requestPanel);
        }

        if (responsePanelPosition == ResponsePanelPosition.TAB_SIDE_BY_SIDE) {
            int idx = getTabbedWork().indexOfComponent(splitRequestAndResponsePanel);
            if (idx != -1) {
                return idx;
            }
        }
        int idx = getTabbedWork().indexOfComponent(requestPanel);
        if (idx != -1) {
            return idx;
        }
        // Panel not shown yet, return "old" index.
        return requestTabIndex;
    }

    /**
     * Creates a split pane between the {@code status} area (bottom component) and {@code work} area
     * or a split between {@code work} and {@code select} areas (top component), if the layout is
     * {@link Layout#EXPAND_SELECT EXPAND_SELECT} or {@link Layout#EXPAND_STATUS EXPAND_STATUS},
     * respectively.
     *
     * @return a {@code JSplitPane} between the {@code status} area and other areas
     */
    private JSplitPane createStatusPanelsSplit() {
        JSplitPane splitVert = new JSplitPane();

        splitVert.setDividerLocation(restoreDividerLocation(DIVIDER_VERTICAL, 300));
        splitVert.addPropertyChangeListener(
                JSplitPane.DIVIDER_LOCATION_PROPERTY, new DividerResizedListener(DIVIDER_VERTICAL));

        splitVert.setDividerSize(DisplayUtils.getScaledSize(8));
        splitVert.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitVert.setResizeWeight(0.5D);

        switch (layout) {
            case EXPAND_SELECT:
                splitVert.setTopComponent(getPaneWork());
                break;
            case EXPAND_STATUS:
            default:
                splitVert.setTopComponent(createSelectPanelsSplit());
                break;
        }
        splitVert.setBottomComponent(getPaneStatus());
        splitVert.setContinuousLayout(false);
        splitVert.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return splitVert;
    }

    /**
     * Creates a split pane between the {@code select} area (left component) and {@code work} area
     * or a split between {@code work} and {@code status} areas (right component), if the layout is
     * {@link Layout#EXPAND_STATUS EXPAND_STATUS} or {@link Layout#EXPAND_SELECT EXPAND_SELECT},
     * respectively.
     *
     * @return a {@code JSplitPane} between the {@code select} area and other areas
     */
    private JSplitPane createSelectPanelsSplit() {
        JSplitPane splitHoriz = new JSplitPane();
        splitHoriz.setOneTouchExpandable(true);
        splitHoriz.setLeftComponent(getPaneSelect());
        switch (layout) {
            case EXPAND_SELECT:
                splitHoriz.setRightComponent(createStatusPanelsSplit());
                break;
            case EXPAND_STATUS:
            default:
                splitHoriz.setRightComponent(getPaneWork());
                break;
        }

        splitHoriz.setDividerLocation(restoreDividerLocation(DIVIDER_HORIZONTAL, 300));
        splitHoriz.addPropertyChangeListener(
                JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new DividerResizedListener(DIVIDER_HORIZONTAL));

        splitHoriz.setResizeWeight(0.3D);
        splitHoriz.setContinuousLayout(false);
        splitHoriz.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return splitHoriz;
    }

    /**
     * This method initializes paneStatus
     *
     * @return JPanel
     */
    private JPanel getPaneStatus() {
        if (paneStatus == null) {
            paneStatus = new JPanel();
            paneStatus.setLayout(new BorderLayout(0, 0));
            paneStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            paneStatus.add(getTabbedStatus());
        }
        return paneStatus;
    }

    /**
     * This method initializes paneSelect
     *
     * @return JPanel
     */
    private JPanel getPaneSelect() {
        if (paneSelect == null) {
            paneSelect = new JPanel();
            paneSelect.setLayout(new BorderLayout(0, 0));
            paneSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            paneSelect.add(getTabbedSelect());
        }
        return paneSelect;
    }

    /**
     * This method initializes paneWork, which is used for request/response/break/script console.
     *
     * @return JPanel
     */
    private JPanel getPaneWork() {
        if (paneWork == null) {
            paneWork = new JPanel();
            paneWork.setLayout(new BorderLayout(0, 0));
            paneWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            paneWork.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            paneWork.add(getTabbedWork());
        }
        return paneWork;
    }

    private JPanel getFullLayoutPanel() {
        if (fullLayoutPanel == null) {
            fullLayoutPanel = new JPanel();
            fullLayoutPanel.setLayout(new BorderLayout(0, 0));
            fullLayoutPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            fullLayoutPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            fullLayoutPanel.add(getTabbedFull());
        }
        return fullLayoutPanel;
    }

    /**
     * Gets the tabbed panel that has all the panels, when in {@link Layout#FULL FULL} layout.
     *
     * <p>Direct access/manipulation of the tabbed panel is discouraged, the changes done to it
     * might be lost while changing layouts.
     *
     * @return the tabbed panel used for the {@link Layout#FULL FULL} layout, never {@code null}
     * @since 2.16.1
     */
    public TabbedPanel2 getTabbedFull() {
        if (tabbedFull == null) {
            tabbedFull = new TabbedPanel2();
            tabbedFull.setName("tabbedFull");
            tabbedFull.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        return tabbedFull;
    }

    /**
     * Gets the tabbed panel that has the {@link PanelType#STATUS STATUS} panels.
     *
     * <p>Direct access/manipulation of the tabbed panel is discouraged, the changes done to it
     * might be lost while changing layouts.
     *
     * @return the tabbed panel of the {@code status} panels, never {@code null}
     * @see #addPanel(AbstractPanel, PanelType)
     */
    public TabbedPanel2 getTabbedStatus() {
        if (tabbedStatus == null) {
            tabbedStatus = new TabbedPanel2();
            tabbedStatus.setPreferredSize(new Dimension(800, 200));
            // ZAP: Move tabs to the top of the panel
            tabbedStatus.setTabPlacement(JTabbedPane.TOP);
            tabbedStatus.setName("tabbedStatus");
            tabbedStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        return tabbedStatus;
    }

    /**
     * Gets the tabbed panel that has the {@link PanelType#WORK WORK} panels.
     *
     * <p>Direct access/manipulation of the tabbed panel is discouraged, the changes done to it
     * might be lost while changing layouts.
     *
     * @return the tabbed panel of the {@code work} panels, never {@code null}
     * @see #addPanel(AbstractPanel, PanelType)
     */
    public TabbedPanel2 getTabbedWork() {
        if (tabbedWork == null) {
            tabbedWork = new TabbedPanel2();
            tabbedWork.setPreferredSize(new Dimension(600, 400));
            tabbedWork.setName("tabbedWork");
            tabbedWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        return tabbedWork;
    }

    /**
     * Sets whether or not the tabs should display the name of the panels.
     *
     * <p>The call to this method has not effect if the state is already set.
     *
     * @param showTabNames {@code true} if the names should be shown, {@code false} otherwise.
     * @since 2.4.0
     */
    public void toggleTabNames(boolean showTabNames) {
        if (this.showTabNames == showTabNames) {
            return;
        }

        this.showTabNames = showTabNames;

        responseTabbedPanel.setShowTabNames(showTabNames);

        if (layout != Layout.FULL) {
            getTabbedStatus().setShowTabNames(showTabNames);
            getTabbedSelect().setShowTabNames(showTabNames);
            getTabbedWork().setShowTabNames(showTabNames);
        } else {
            getTabbedFull().setShowTabNames(showTabNames);
        }
    }

    /**
     * Gets the tabbed panel that has the {@link PanelType#SELECT SELECT} panels.
     *
     * <p>Direct access/manipulation of the tabbed panel is discouraged, the changes done to it
     * might be lost while changing layouts.
     *
     * @return the tabbed panel of the {@code work} panels, never {@code null}
     * @see #addPanel(AbstractPanel, PanelType)
     */
    public TabbedPanel2 getTabbedSelect() {
        if (tabbedSelect == null) {
            tabbedSelect = new TabbedPanel2();
            tabbedSelect.setPreferredSize(new Dimension(200, 400));
            tabbedSelect.setName("tabbedSelect");
            tabbedSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        return tabbedSelect;
    }

    /**
     * Adds the given panels to the workbench, hinting with the given panel type.
     *
     * @param panels the panels to add to the workbench
     * @param panelType the type of the panels
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     * @since 2.5.0
     * @see #removePanels(List, PanelType)
     * @see #addPanel(AbstractPanel, PanelType)
     */
    public void addPanels(List<AbstractPanel> panels, PanelType panelType) {
        validateNotNull(panels, "panels");
        validateNotNull(panelType, "panelType");

        boolean fullLayout = layout == Layout.FULL;

        addPanels(getTabbedFull(), panels, fullLayout);
        layoutPanels.get(panelType).addAll(panels);

        switch (panelType) {
            case SELECT:
                addPanels(getTabbedSelect(), panels, !fullLayout);
                break;
            case STATUS:
                addPanels(getTabbedStatus(), panels, !fullLayout);
                break;
            case WORK:
                addPanels(getTabbedWork(), panels, !fullLayout);
                break;
            default:
                break;
        }
    }

    /**
     * Adds the given {@code panels} to the given {@code tabbedPanel} and whether they should be
     * visible.
     *
     * <p>After adding all the panels the tabbed panel is revalidated.
     *
     * @param tabbedPanel the tabbed panel to add the panels
     * @param panels the panels to add
     * @param visible {@code true} if the panel should be visible, {@code false} otherwise.
     * @see #addPanel(TabbedPanel2, AbstractPanel, boolean)
     * @see javax.swing.JComponent#revalidate()
     */
    private static void addPanels(
            TabbedPanel2 tabbedPanel, List<AbstractPanel> panels, boolean visible) {
        for (AbstractPanel panel : panels) {
            addPanel(tabbedPanel, panel, visible);
        }
        tabbedPanel.revalidate();
    }

    /**
     * Adds the given {@code panel} to the given {@code tabbedPanel}.
     *
     * @param tabbedPanel the tabbed panel to add the panel
     * @param panel the panel to add
     * @param visible {@code true} if the panel should be visible, {@code false} otherwise.
     * @see #addPanels(TabbedPanel2, List, boolean)
     */
    private static void addPanel(TabbedPanel2 tabbedPanel, AbstractPanel panel, boolean visible) {
        if (visible) {
            tabbedPanel.addTab(panel);
        } else {
            tabbedPanel.addTabHidden(panel);
        }
    }

    /**
     * Adds the given panel to the workbench, hinting with the given panel type.
     *
     * @param panel the panel to add to the workbench
     * @param panelType the type of the panel
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     * @since 2.5.0
     * @see #removePanel(AbstractPanel, PanelType)
     * @see #addPanels(List, PanelType)
     */
    public void addPanel(AbstractPanel panel, PanelType panelType) {
        validateNotNull(panel, "panel");
        validateNotNull(panelType, "panelType");

        boolean fullLayout = layout == Layout.FULL;

        addPanel(getTabbedFull(), panel, fullLayout);
        layoutPanels.get(panelType).add(panel);

        switch (panelType) {
            case SELECT:
                addPanel(getTabbedSelect(), panel, !fullLayout);
                getTabbedSelect().revalidate();
                break;
            case STATUS:
                addPanel(getTabbedStatus(), panel, !fullLayout);
                getTabbedStatus().revalidate();
                break;
            case WORK:
                addPanel(getTabbedWork(), panel, !fullLayout);
                getTabbedWork().revalidate();
                break;
            default:
                break;
        }
    }

    /**
     * Removes the given panels of given panel type from the workbench panel.
     *
     * @param panels the panels to remove from the workbench panel
     * @param panelType the type of the panels
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     * @since 2.5.0
     * @see #addPanels(List, PanelType)
     * @see #removePanel(AbstractPanel, PanelType)
     */
    public void removePanels(List<AbstractPanel> panels, PanelType panelType) {
        validateNotNull(panels, "panels");
        validateNotNull(panelType, "panelType");

        removePanels(getTabbedFull(), panels);
        panels.forEach(layoutPanels.get(panelType)::remove);

        switch (panelType) {
            case SELECT:
                removePanels(getTabbedSelect(), panels);
                break;
            case STATUS:
                removePanels(getTabbedStatus(), panels);
                break;
            case WORK:
                removePanels(getTabbedWork(), panels);
                break;
            default:
                break;
        }
    }

    /**
     * Removes the given {@code panels} from the given {@code tabbedPanel}.
     *
     * <p>After removing all the panels the tabbed panel is revalidated.
     *
     * @param tabbedPanel the tabbed panel to remove the panels
     * @param panels the panels to remove
     * @see #addPanel(TabbedPanel2, AbstractPanel, boolean)
     * @see javax.swing.JComponent#revalidate()
     */
    private static void removePanels(TabbedPanel2 tabbedPanel, List<AbstractPanel> panels) {
        for (AbstractPanel panel : panels) {
            removeTabPanel(tabbedPanel, panel);
        }
        tabbedPanel.revalidate();
    }

    /**
     * Removes the given {@code panel} from the given {@code tabbedPanel}.
     *
     * @param tabbedPanel the tabbed panel to remove the panel
     * @param panel the panel to remove
     * @see #removePanels(TabbedPanel2, List)
     */
    private static void removeTabPanel(TabbedPanel2 tabbedPanel, AbstractPanel panel) {
        tabbedPanel.removeTab(panel);
    }

    /**
     * Removes the given panel of given panel type from the workbench panel.
     *
     * @param panel the panel to remove from the workbench panel
     * @param panelType the type of the panel
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     * @since 2.5.0
     * @see #addPanel(AbstractPanel, PanelType)
     * @see #removePanels(List, PanelType)
     */
    public void removePanel(AbstractPanel panel, PanelType panelType) {
        validateNotNull(panel, "panel");
        validateNotNull(panelType, "panelType");

        removeTabPanel(getTabbedFull(), panel);
        layoutPanels.get(panelType).remove(panel);
        getTabbedFull().revalidate();

        switch (panelType) {
            case SELECT:
                removeTabPanel(getTabbedSelect(), panel);
                getTabbedSelect().revalidate();
                break;
            case STATUS:
                removeTabPanel(getTabbedStatus(), panel);
                getTabbedStatus().revalidate();
                break;
            case WORK:
                removeTabPanel(getTabbedWork(), panel);
                getTabbedWork().revalidate();
                break;
            default:
                break;
        }
    }

    /**
     * Gets the panels that were added to the workbench with the given panel type.
     *
     * @param panelType the type of the panel
     * @return a {@code List} with the panels of the given type
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     */
    public List<AbstractPanel> getPanels(PanelType panelType) {
        validateNotNull(panelType, "panelType");

        List<AbstractPanel> panels = new ArrayList<>();
        switch (panelType) {
            case SELECT:
                panels.addAll(getTabbedSelect().getPanels());
                break;
            case STATUS:
                panels.addAll(getTabbedStatus().getPanels());
                break;
            case WORK:
                panels.addAll(getTabbedWork().getPanels());
                break;
            default:
                break;
        }
        return panels;
    }

    /**
     * Gets the panels, sorted by name, that were added to the workbench with the given panel type.
     *
     * @param panelType the type of the panel
     * @return a {@code List} with the sorted panels of the given type
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     */
    public SortedSet<AbstractPanel> getSortedPanels(PanelType panelType) {
        validateNotNull(panelType, "panelType");

        List<AbstractPanel> panels = getPanels(panelType);
        SortedSet<AbstractPanel> sortedPanels =
                new TreeSet<>(
                        new Comparator<AbstractPanel>() {

                            @Override
                            public int compare(
                                    AbstractPanel abstractPanel, AbstractPanel otherAbstractPanel) {
                                String name = abstractPanel.getName();
                                String otherName = otherAbstractPanel.getName();
                                if (name == null) {
                                    if (otherName == null) {
                                        return 0;
                                    }
                                    return -1;
                                } else if (otherName == null) {
                                    return 1;
                                }
                                return name.compareTo(otherName);
                            }
                        });
        sortedPanels.addAll(panels);
        return sortedPanels;
    }

    /**
     * Sets whether or not the panels should be visible.
     *
     * <p>{@link AbstractPanel#isHideable() Non-hideable} and {@link AbstractPanel#isPinned()
     * pinned} panels are not affected by this call, when set to not be visible.
     *
     * @param visible {@code true} if all panels should be visible, {@code false} otherwise.
     * @since 2.5.0
     */
    public void setPanelsVisible(boolean visible) {
        if (layout == Layout.FULL) {
            getTabbedFull().setPanelsVisible(visible);
        } else {
            getTabbedSelect().setPanelsVisible(visible);
            getTabbedWork().setPanelsVisible(visible);
            getTabbedStatus().setPanelsVisible(visible);
        }
    }

    /**
     * Pins all visible panels.
     *
     * @since 2.5.0
     * @see #unpinVisiblePanels()
     * @see AbstractPanel#setPinned(boolean)
     */
    public void pinVisiblePanels() {
        if (layout == Layout.FULL) {
            getTabbedFull().pinVisibleTabs();
        } else {
            getTabbedSelect().pinVisibleTabs();
            getTabbedWork().pinVisibleTabs();
            getTabbedStatus().pinVisibleTabs();
        }
    }

    /**
     * Unpins all visible panels.
     *
     * @since 2.5.0
     * @see #pinVisiblePanels()
     * @see AbstractPanel#setPinned(boolean)
     */
    public void unpinVisiblePanels() {
        if (layout == Layout.FULL) {
            getTabbedFull().unpinTabs();
        } else {
            getTabbedSelect().unpinTabs();
            getTabbedWork().unpinTabs();
            getTabbedStatus().unpinTabs();
        }
    }

    /**
     * Shows the given panel, if it was previously added.
     *
     * <p>It does nothing, if the tab is already shown.
     *
     * @param panel the panel to be shown
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     * @see #addPanel(AbstractPanel, PanelType)
     */
    public void showPanel(AbstractPanel panel) {
        validateNotNull(panel, "panel");

        if (layout == Layout.FULL) {
            getTabbedFull().setVisible(panel, true);
        } else {
            getTabbedSelect().setVisible(panel, true);
            getTabbedStatus().setVisible(panel, true);
            getTabbedWork().setVisible(panel, true);
        }
        panel.setTabFocus();
    }

    /**
     * Sets the position of the response panel.
     *
     * @param position the position of the response panel
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     */
    void setResponsePanelPosition(ResponsePanelPosition position) {
        validateNotNull(position, "position");

        requestTabIndex = getCurrentRequestTabIndex();

        responsePanelPosition = position;
        if (layout == Layout.FULL) {
            updateFullLayout();
            return;
        }

        Component currentTabbedPanel = componentMaximiser.getMaximisedComponent();
        if (componentMaximiser.isComponentMaximised()) {
            componentMaximiser.unmaximiseComponent();
        }

        boolean selectRequest = removeSplitRequestAndResponsePanel(tabbedWork);

        switch (position) {
            case PANEL_ABOVE:
                splitResponsePanelWithWorkTabbedPanel(JSplitPane.VERTICAL_SPLIT);
                break;
            case PANELS_SIDE_BY_SIDE:
                splitResponsePanelWithWorkTabbedPanel(JSplitPane.HORIZONTAL_SPLIT);
                break;
            case TAB_SIDE_BY_SIDE:
                splitResponsePanelWithRequestPanel(JSplitPane.HORIZONTAL_SPLIT, tabbedWork);

                getPaneWork().removeAll();
                getPaneWork().add(getTabbedWork());
                getPaneWork().validate();
                break;
            case TABS_SIDE_BY_SIDE:
            default:
                if (currentTabbedPanel == responseTabbedPanel) {
                    currentTabbedPanel = tabbedWork;
                }
                insertResponseTab(tabbedWork);

                getPaneWork().removeAll();
                getPaneWork().add(getTabbedWork());
                getPaneWork().validate();
        }

        if (selectRequest
                || (getTabbedWork().getTabCount() > 0
                        && getTabbedWork().getSelectedComponent() == null)) {
            getTabbedWork()
                    .setSelectedComponent(
                            responsePanelPosition == ResponsePanelPosition.TAB_SIDE_BY_SIDE
                                    ? splitRequestAndResponsePanel
                                    : requestPanel);
        }

        if (currentTabbedPanel != null) {
            componentMaximiser.maximiseComponent(currentTabbedPanel);
        }
    }

    boolean hasAnyDetachedRequestResponseTabs() {
        var tabbedPanel = layout == Layout.FULL ? getTabbedFull() : getTabbedWork();
        return tabbedPanel.getDetachedTabs().containsKey(requestPanel)
                || tabbedPanel.getDetachedTabs().containsKey(responsePanel)
                || tabbedPanel.getDetachedTabs().containsKey(splitRequestAndResponsePanel)
                || responseTabbedPanel.getDetachedTabs().containsKey(responsePanel);
    }

    void closeAllDetachedRequestResponseTabs() {
        var tabbedPanel = layout == Layout.FULL ? getTabbedFull() : getTabbedWork();
        var workDetachedTabs = tabbedPanel.getDetachedTabs();
        if (workDetachedTabs.containsKey(requestPanel)) {
            workDetachedTabs.get(requestPanel).reattach();
        }
        if (workDetachedTabs.containsKey(responsePanel)) {
            workDetachedTabs.get(responsePanel).reattach();
        }
        if (workDetachedTabs.containsKey(splitRequestAndResponsePanel)) {
            workDetachedTabs.get(splitRequestAndResponsePanel).reattach();
        }
        if (responseTabbedPanel.getDetachedTabs().containsKey(responsePanel)) {
            responseTabbedPanel.getDetachedTabs().get(responsePanel).reattach();
        }
    }

    private void updateFullLayout() {
        boolean selectRequest = removeSplitRequestAndResponsePanel(tabbedFull);

        if (responsePanelPosition == ResponsePanelPosition.TAB_SIDE_BY_SIDE) {
            splitResponsePanelWithRequestPanel(JSplitPane.HORIZONTAL_SPLIT, tabbedFull);
        } else {
            addRequestResponseTabs(tabbedFull);
        }
        getFullLayoutPanel().validate();

        if (selectRequest
                || (getTabbedFull().getTabCount() > 0
                        && getTabbedFull().getSelectedComponent() == null)) {
            tabbedFull.setSelectedComponent(
                    responsePanelPosition == ResponsePanelPosition.TAB_SIDE_BY_SIDE
                            ? splitRequestAndResponsePanel
                            : requestPanel);
        }
    }

    private void addRequestResponseTabs(TabbedPanel2 tabbedPanel) {
        if (!tabbedPanel.getDetachedTabs().containsKey(requestPanel)) {
            tabbedPanel.addTab(requestPanel, requestTabIndex);
        }
        insertResponseTab(tabbedPanel);
    }

    private void insertResponseTab(TabbedPanel2 tabbedPanel) {
        if (!tabbedPanel.getDetachedTabs().containsKey(responsePanel)) {
            tabbedPanel.addTab(responsePanel, tabbedPanel.indexOfComponent(requestPanel) + 1);
        }
    }

    private String getNormalisedTabName(AbstractPanel panel) {
        return showTabNames ? panel.getName() : "";
    }

    private void splitResponsePanelWithWorkTabbedPanel(int orientation) {
        if (responseTabbedPanel.getDetachedTabs().containsKey(responsePanel)) {
            getPaneWork().removeAll();
            getPaneWork().add(getTabbedWork());
            return;
        }

        responseTabbedPanel.hideAllTabs();

        responseTabbedPanel.addTab(
                getNormalisedTabName(responsePanel), responsePanel.getIcon(), responsePanel);

        getPaneWork().removeAll();

        JSplitPane split = createSplitPane(orientation);

        split.setRightComponent(responseTabbedPanel);
        split.setLeftComponent(getTabbedWork());

        getPaneWork().add(split);
        getPaneWork().validate();
    }

    private static JSplitPane createSplitPane(int orientation) {
        JSplitPane splitPane = new JSplitPane(orientation);
        splitPane.setDividerSize(DisplayUtils.getScaledSize(6));
        splitPane.setResizeWeight(0.5D);
        splitPane.setContinuousLayout(false);
        splitPane.setDoubleBuffered(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return splitPane;
    }

    private void splitResponsePanelWithRequestPanel(int orientation, TabbedPanel2 tabbedPanel) {
        if (splitRequestAndResponsePanel == null) {
            splitRequestAndResponsePanel = new AbstractPanel();
            splitRequestAndResponsePanel.setLayout(new BorderLayout());
            splitRequestAndResponsePanel.setName(
                    Constant.messages.getString("http.panel.requestAndResponse.title"));
            splitRequestAndResponsePanel.setIcon(
                    new ImageIcon(
                            WorkbenchPanel.class.getResource("/resource/icon/16/handshake.png")));
            splitRequestAndResponsePanel.setHideable(false);

            splitRequestAndResponse = createSplitPane(orientation);
            splitRequestAndResponsePanel.add(splitRequestAndResponse);

            layoutPanels.get(PanelType.WORK).add(splitRequestAndResponsePanel);
        }

        Component selectedComponent = tabbedPanel.getSelectedComponent();

        splitRequestAndResponse.setLeftComponent(requestPanel);
        tabbedPanel.removeTab(requestPanel);
        splitRequestAndResponse.setRightComponent(responsePanel);
        tabbedPanel.removeTab(responsePanel);

        if (!tabbedPanel.getDetachedTabs().containsKey(splitRequestAndResponsePanel)) {
            tabbedPanel.addTab(splitRequestAndResponsePanel, requestTabIndex);
            if (selectedComponent == requestPanel || selectedComponent == responsePanel) {
                tabbedPanel.setSelectedComponent(splitRequestAndResponsePanel);
            }
        }
    }

    private boolean removeSplitRequestAndResponsePanel(TabbedPanel2 tabbedPanel) {
        boolean selectRequest = false;
        int pos = tabbedPanel.indexOfComponent(splitRequestAndResponsePanel);
        if (pos != -1) {
            selectRequest = tabbedPanel.getSelectedComponent() == splitRequestAndResponsePanel;
            tabbedPanel.removeTab(splitRequestAndResponsePanel);
            addRequestResponseTabs(tabbedPanel);
        }
        return selectRequest;
    }

    /**
     * @param prefix
     * @param location
     */
    private void saveDividerLocation(String prefix, int location) {
        if (location > 0) {
            LOGGER.debug(
                    "Saving preference {}{}.{}={}",
                    prefnzPrefix,
                    prefix,
                    PREF_DIVIDER_LOCATION,
                    location);
            this.preferences.put(
                    prefnzPrefix + prefix + "." + PREF_DIVIDER_LOCATION,
                    Integer.toString(location));
            // immediate flushing
            try {
                this.preferences.flush();
            } catch (final BackingStoreException e) {
                LOGGER.error("Error while saving the preferences", e);
            }
        }
    }

    /**
     * @param prefix
     * @param fallback
     * @return the size of the frame OR fallback value, if there wasn't any preference.
     */
    private int restoreDividerLocation(String prefix, int fallback) {
        int result = fallback;
        final String sizestr =
                preferences.get(prefnzPrefix + prefix + "." + PREF_DIVIDER_LOCATION, null);
        if (sizestr != null) {
            int location = 0;
            try {
                location = Integer.parseInt(sizestr.trim());
            } catch (final Exception e) {
                // ignoring, cause is prevented by default values;
            }
            if (location > 0) {
                result = location;
                LOGGER.debug(
                        "Restoring preference {}{}.{}={}",
                        prefnzPrefix,
                        prefix,
                        PREF_DIVIDER_LOCATION,
                        location);
            }
        }
        return result;
    }

    /*
     * ========================================================================
     */

    private final class DividerResizedListener implements PropertyChangeListener {

        private final String prefix;

        public DividerResizedListener(String prefix) {
            super();
            assert prefix != null;
            this.prefix = prefix;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JSplitPane component = (JSplitPane) evt.getSource();
            if (component != null) {
                LOGGER.debug(
                        "{}{}.location={}", prefnzPrefix, prefix, component.getDividerLocation());
                saveDividerLocation(prefix, component.getDividerLocation());
            }
        }
    }
}
