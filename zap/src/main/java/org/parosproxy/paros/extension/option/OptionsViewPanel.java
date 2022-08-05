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
// ZAP: 2012/03/15 Removed the options to change the display of the ManualRequestEditorDialog,
// now they are changed dynamically.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/12/13 Added support for a new option 'show tab names'.
// ZAP: 2014/04/25 Issue 642: Add timestamps to Output tab(s)
// ZAP: 2014/10/09 Issue 1359: Options for splash screen
// ZAP: 2014/12/16 Issue 1466: Config option for 'large display' size
// ZAP: 2016/04/04 Do not require a restart to show/hide the tool bar
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2017/01/09 Remove method no longer needed.
// ZAP: 2018/02/14 Add option for ResponsePanelPosition.TAB_SIDE_BY_SIDE (Issue 4331).
// ZAP: 2018/02/27 Add option for Look And Feel and a scrollbar.
// ZAP: 2018/03/01 Remove the name from a panel and use BorderLayout.
// ZAP: 2018/06/11 Added options for Work Panels Font.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/02/24 Use LookAndFeelInfo when setting the look and feel option.
// ZAP: 2020/03/25 Remove hardcoded colour in titled border (Issue 5542).
// ZAP: 2020/12/03 Add constants for indexes of possible break buttons locations
// ZAP: 2021/05/14 Remove redundant type arguments and empty statement.
// ZAP: 2021/09/16 Add support for enabling app integration in containers
// ZAP: 2022/02/25 Remove options no longer in use.
// ZAP: 2022/08/05 Address warns with Java 18 (Issue 7389).
package org.parosproxy.paros.extension.option;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.extension.brk.BreakpointsParam;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.TimeStampUtils;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

// ZAP: 2011: added more configuration options

@SuppressWarnings("serial")
public class OptionsViewPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    private static final String TIME_STAMP_FORMAT_COMBOBOX_TOOL_TIP =
            Constant.messages.getString("options.display.timestamp.format.combobox.tooltip");
    // Translatable formats
    private static final String TIME_STAMP_FORMAT_DATETIME =
            Constant.messages.getString("timestamp.format.datetime");
    private static final String TIME_STAMP_FORMAT_TIMEONLY =
            Constant.messages.getString("timestamp.format.timeonly");
    // ISO Standards compliant format
    private static final String TIME_STAMP_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";

    public enum BreakLocation {
        TOOL_BAR_ONLY(0),
        BREAK_ONLY(1),
        BREAK_PANEL_AND_TOOL_BAR(2);
        private final int value;

        private BreakLocation(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private JPanel panelMisc = null;
    private JScrollPane mainScrollPane;

    private JCheckBox chkShowTabNames = null;
    private JCheckBox chkProcessImages = null;
    private JCheckBox chkShowMainToolbar = null;
    private JCheckBox chkAdvancedView = null;
    private JCheckBox chkAskOnExit = null;
    private JCheckBox chkWmUiHandling = null;
    private JCheckBox chkOutputTabTimeStamping = null;
    private JCheckBox chkShowSplashScreen = null;
    private JCheckBox scaleImages = null;
    private JCheckBox showLocalConnectRequestsCheckbox;
    private JCheckBox allowAppsInContainers = null;

    private JComboBox<String> brkPanelViewSelect = null;
    private JComboBox<String> displaySelect = null;
    private JComboBox<ResponsePanelPositionUI> responsePanelPositionComboBox;
    private JComboBox<String> timeStampsFormatSelect = null;
    private JComboBox<LookAndFeelInfoUi> lookAndFeel = null;

    private JLabel brkPanelViewLabel = null;
    private JLabel advancedViewLabel = null;
    private JLabel wmUiHandlingLabel = null;
    private JLabel askOnExitLabel = null;
    private JLabel displayLabel = null;
    private JLabel showMainToolbarLabel = null;
    private JLabel processImagesLabel = null;
    private JLabel showTabNamesLabel = null;
    private JLabel outputTabTimeStampLabel = null;
    private JLabel outputTabTimeStampExampleLabel = null;
    private JLabel showSplashScreenLabel = null;
    private JLabel lookAndFeelLabel = null;
    private Map<FontUtils.FontType, JLabel> fontLabels = new EnumMap<>(FontUtils.FontType.class);
    private Map<FontUtils.FontType, ZapNumberSpinner> fontSizes =
            new EnumMap<>(FontUtils.FontType.class);
    private Map<FontUtils.FontType, JComboBox<String>> fontNames =
            new EnumMap<>(FontUtils.FontType.class);
    private Map<FontUtils.FontType, String> fontTypeLabels =
            new EnumMap<>(FontUtils.FontType.class);

    public OptionsViewPanel() {
        super();
        fontTypeLabels.put(
                FontUtils.FontType.general,
                Constant.messages.getString("view.options.label.generalFont"));
        fontTypeLabels.put(
                FontUtils.FontType.workPanels,
                Constant.messages.getString("view.options.label.workPanelsFont"));

        for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
            fontLabels.put(fontType, null);
            fontNames.put(fontType, null);
            fontSizes.put(fontType, null);
        }
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setName(Constant.messages.getString("view.options.title"));
        this.add(getMainScrollPane());
    }

    private JScrollPane getMainScrollPane() {
        if (mainScrollPane == null) {
            mainScrollPane = new JScrollPane();
            mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
            mainScrollPane.setViewportView(getPanelMisc());
        }
        return mainScrollPane;
    }

    /**
     * This method initializes panelMisc
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelMisc() {
        if (panelMisc == null) {
            panelMisc = new JPanel();

            panelMisc.setLayout(new GridBagLayout());
            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption()
                    == 0) {
                panelMisc.setSize(114, 132);
            }

            displayLabel = new JLabel(Constant.messages.getString("view.options.label.display"));
            brkPanelViewLabel =
                    new JLabel(Constant.messages.getString("view.options.label.brkPanelView"));
            advancedViewLabel =
                    new JLabel(Constant.messages.getString("view.options.label.advancedview"));
            wmUiHandlingLabel =
                    new JLabel(Constant.messages.getString("view.options.label.wmuihandler"));
            askOnExitLabel =
                    new JLabel(Constant.messages.getString("view.options.label.askonexit"));
            showMainToolbarLabel =
                    new JLabel(Constant.messages.getString("view.options.label.showMainToolbar"));
            processImagesLabel =
                    new JLabel(Constant.messages.getString("view.options.label.processImages"));
            showTabNamesLabel =
                    new JLabel(Constant.messages.getString("view.options.label.showTabNames"));
            outputTabTimeStampLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "options.display.timestamp.format.outputtabtimestamps.label"));
            lookAndFeelLabel =
                    new JLabel(Constant.messages.getString("view.options.label.lookandfeel"));
            outputTabTimeStampExampleLabel =
                    new JLabel(TimeStampUtils.currentDefaultFormattedTimeStamp());
            showSplashScreenLabel =
                    new JLabel(Constant.messages.getString("view.options.label.showSplashScreen"));

            int row = 0;
            displayLabel.setLabelFor(getDisplaySelect());
            panelMisc.add(
                    displayLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getDisplaySelect(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            JLabel responsePanelPositionLabel =
                    new JLabel(Constant.messages.getString("view.options.label.responsepanelpos"));
            panelMisc.add(
                    responsePanelPositionLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getResponsePanelPositionComboBox(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            brkPanelViewLabel.setLabelFor(getBrkPanelViewSelect());
            panelMisc.add(
                    brkPanelViewLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getBrkPanelViewSelect(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            advancedViewLabel.setLabelFor(getChkAdvancedView());
            panelMisc.add(
                    advancedViewLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getChkAdvancedView(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            wmUiHandlingLabel.setLabelFor(getChkWmUiHandling());
            panelMisc.add(
                    wmUiHandlingLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getChkWmUiHandling(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            askOnExitLabel.setLabelFor(getChkAskOnExit());
            panelMisc.add(
                    askOnExitLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getChkAskOnExit(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            showMainToolbarLabel.setLabelFor(getChkShowMainToolbar());
            panelMisc.add(
                    showMainToolbarLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getChkShowMainToolbar(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            processImagesLabel.setLabelFor(getChkProcessImages());
            panelMisc.add(
                    processImagesLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getChkProcessImages(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            Insets insets = new Insets(2, 2, 2, 2);
            String labelText =
                    Constant.messages.getString("view.options.label.showlocalconnectrequests");
            JLabel showConnectRequestLabel = new JLabel(labelText);
            showConnectRequestLabel.setLabelFor(getShowLocalConnectRequestsCheckbox());
            panelMisc.add(showConnectRequestLabel, LayoutHelper.getGBC(0, row, 1, 1.0D, insets));
            panelMisc.add(
                    getShowLocalConnectRequestsCheckbox(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, insets));

            row++;
            showTabNamesLabel.setLabelFor(getShowTabNames());
            panelMisc.add(
                    showTabNamesLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getShowTabNames(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            showSplashScreenLabel.setLabelFor(getShowSplashScreen());
            panelMisc.add(
                    showSplashScreenLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getShowSplashScreen(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            JLabel allowAppIntegrationInContainersLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "view.options.label.allowAppsInContainers"));
            allowAppIntegrationInContainersLabel.setLabelFor(getAllowAppsInContainers());
            panelMisc.add(
                    allowAppIntegrationInContainersLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getAllowAppsInContainers(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            outputTabTimeStampLabel.setLabelFor(getChkOutputTabTimeStamps());
            panelMisc.add(
                    outputTabTimeStampLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getChkOutputTabTimeStamps(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            outputTabTimeStampExampleLabel.setLabelFor(getTimeStampsFormatSelect());
            panelMisc.add(
                    getTimeStampsFormatSelect(),
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    outputTabTimeStampExampleLabel,
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
                row++;
                JPanel fontsPanel = new JPanel();
                fontsPanel.setLayout(new GridBagLayout());
                fontsPanel.setBorder(
                        BorderFactory.createTitledBorder(
                                null,
                                fontTypeLabels.get(fontType),
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                FontUtils.getFont(FontUtils.Size.standard)));

                panelMisc.add(
                        fontsPanel,
                        LayoutHelper.getGBC(0, row, 2, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

                JLabel fontNameLabel =
                        new JLabel(Constant.messages.getString("view.options.label.fontName"));
                fontNameLabel.setLabelFor(getFontName(fontType));
                fontsPanel.add(
                        fontNameLabel,
                        LayoutHelper.getGBC(0, 1, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
                fontsPanel.add(
                        getFontName(fontType),
                        LayoutHelper.getGBC(1, 1, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

                JLabel fontSizeLabel =
                        new JLabel(Constant.messages.getString("view.options.label.fontSize"));
                fontSizeLabel.setLabelFor(getFontSize(fontType));
                fontsPanel.add(
                        fontSizeLabel,
                        LayoutHelper.getGBC(0, 2, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
                fontsPanel.add(
                        getFontSize(fontType),
                        LayoutHelper.getGBC(1, 2, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

                JLabel fontExampleLabel =
                        new JLabel(Constant.messages.getString("view.options.label.fontExample"));
                fontExampleLabel.setLabelFor(getFontExampleLabel(fontType));
                fontsPanel.add(
                        fontExampleLabel,
                        LayoutHelper.getGBC(0, 3, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
                fontsPanel.add(
                        getFontExampleLabel(fontType),
                        LayoutHelper.getGBC(1, 3, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            }

            row++;
            JLabel scaleImagesLabel =
                    new JLabel(Constant.messages.getString("view.options.label.scaleImages"));
            scaleImagesLabel.setLabelFor(getScaleImages());
            panelMisc.add(
                    scaleImagesLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getScaleImages(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            lookAndFeelLabel.setLabelFor(getLookAndFeelSelect());
            panelMisc.add(
                    lookAndFeelLabel,
                    LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));
            panelMisc.add(
                    getLookAndFeelSelect(),
                    LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2, 2, 2, 2)));

            row++;
            panelMisc.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D, 1.0D));
        }
        return panelMisc;
    }

    private JCheckBox getShowTabNames() {
        if (chkShowTabNames == null) {
            chkShowTabNames = new JCheckBox();
            chkShowTabNames.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkShowTabNames.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkShowTabNames;
    }

    private JCheckBox getShowSplashScreen() {
        if (chkShowSplashScreen == null) {
            chkShowSplashScreen = new JCheckBox();
            chkShowSplashScreen.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkShowSplashScreen.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkShowSplashScreen;
    }

    private JCheckBox getChkProcessImages() {
        if (chkProcessImages == null) {
            chkProcessImages = new JCheckBox();
            chkProcessImages.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkProcessImages.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkProcessImages;
    }

    private JCheckBox getChkShowMainToolbar() {
        if (chkShowMainToolbar == null) {
            chkShowMainToolbar = new JCheckBox();
            chkShowMainToolbar.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkShowMainToolbar.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkShowMainToolbar;
    }

    private JCheckBox getChkWmUiHandling() {
        if (chkWmUiHandling == null) {
            chkWmUiHandling = new JCheckBox();
            chkWmUiHandling.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkWmUiHandling.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkWmUiHandling;
    }

    private JCheckBox getChkAskOnExit() {
        if (chkAskOnExit == null) {
            chkAskOnExit = new JCheckBox();
            chkAskOnExit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkAskOnExit.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return chkAskOnExit;
    }

    private JComboBox<String> getDisplaySelect() {
        if (displaySelect == null) {
            displaySelect = new JComboBox<>();
            displaySelect.addItem(Constant.messages.getString("view.options.label.display.left"));
            displaySelect.addItem(Constant.messages.getString("view.options.label.display.bottom"));
            displaySelect.addItem(Constant.messages.getString("view.options.label.display.full"));
        }
        return displaySelect;
    }

    private JComboBox<ResponsePanelPositionUI> getResponsePanelPositionComboBox() {
        if (responsePanelPositionComboBox == null) {
            responsePanelPositionComboBox = new JComboBox<>();
            responsePanelPositionComboBox.addItem(
                    new ResponsePanelPositionUI(
                            Constant.messages.getString("view.options.label.responsepanelpos.tabs"),
                            WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE));
            responsePanelPositionComboBox.addItem(
                    new ResponsePanelPositionUI(
                            Constant.messages.getString(
                                    "view.options.label.responsepanelpos.tabSideBySide"),
                            WorkbenchPanel.ResponsePanelPosition.TAB_SIDE_BY_SIDE));
            responsePanelPositionComboBox.addItem(
                    new ResponsePanelPositionUI(
                            Constant.messages.getString(
                                    "view.options.label.responsepanelpos.sideBySide"),
                            WorkbenchPanel.ResponsePanelPosition.PANELS_SIDE_BY_SIDE));
            responsePanelPositionComboBox.addItem(
                    new ResponsePanelPositionUI(
                            Constant.messages.getString(
                                    "view.options.label.responsepanelpos.above"),
                            WorkbenchPanel.ResponsePanelPosition.PANEL_ABOVE));
        }
        return responsePanelPositionComboBox;
    }

    private JComboBox<String> getBrkPanelViewSelect() {
        // if you change items order also change the enum BreakLocation
        if (brkPanelViewSelect == null) {
            brkPanelViewSelect = new JComboBox<>();
            brkPanelViewSelect.addItem(
                    Constant.messages.getString("view.options.label.brkPanelView.toolbaronly"));
            brkPanelViewSelect.addItem(
                    Constant.messages.getString("view.options.label.brkPanelView.breakonly"));
            brkPanelViewSelect.addItem(
                    Constant.messages.getString("view.options.label.brkPanelView.both"));
        }
        return brkPanelViewSelect;
    }

    private JCheckBox getChkAdvancedView() {
        if (chkAdvancedView == null) {
            chkAdvancedView = new JCheckBox();
            chkAdvancedView.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkAdvancedView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }

        return chkAdvancedView;
    }

    private JCheckBox getChkOutputTabTimeStamps() {
        if (chkOutputTabTimeStamping == null) {
            chkOutputTabTimeStamping = new JCheckBox();
            chkOutputTabTimeStamping.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            chkOutputTabTimeStamping.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
            chkOutputTabTimeStamping.addItemListener(
                    new java.awt.event.ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            timeStampsFormatSelect.setEnabled(
                                    e.getStateChange() == ItemEvent.SELECTED);
                        }
                    });
        }
        return chkOutputTabTimeStamping;
    }

    private JComboBox<String> getTimeStampsFormatSelect() {
        if (timeStampsFormatSelect == null) {
            String[] timeStampFormatStrings = {
                TIME_STAMP_FORMAT_DATETIME, TIME_STAMP_FORMAT_ISO8601, TIME_STAMP_FORMAT_TIMEONLY
            };
            timeStampsFormatSelect = new JComboBox<>(timeStampFormatStrings);
            timeStampsFormatSelect.setToolTipText(TIME_STAMP_FORMAT_COMBOBOX_TOOL_TIP);
            timeStampsFormatSelect.setSelectedItem(getTimeStampsFormatSelect().getSelectedItem());
            timeStampsFormatSelect.setEditable(true);
            if (chkOutputTabTimeStamping
                    .isSelected()) // The drop-down should only be enabled if time stamping is
                // turned on
                timeStampsFormatSelect.setEnabled(true);
            else timeStampsFormatSelect.setEnabled(false);
            timeStampsFormatSelect.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            String selectedDateFormat =
                                    (String) getTimeStampsFormatSelect().getSelectedItem();
                            outputTabTimeStampExampleLabel.setText(
                                    TimeStampUtils.currentFormattedTimeStamp(selectedDateFormat));
                        }
                    });
        }
        return timeStampsFormatSelect;
    }

    private JCheckBox getShowLocalConnectRequestsCheckbox() {
        if (showLocalConnectRequestsCheckbox == null) {
            showLocalConnectRequestsCheckbox = new JCheckBox();
        }
        return showLocalConnectRequestsCheckbox;
    }

    private ZapNumberSpinner initFontSize(FontUtils.FontType fontType) {
        ZapNumberSpinner fontSize;
        fontSize = new ZapNumberSpinner(-1, 8, 100);
        if (!FontUtils.canChangeSize()) {
            fontSize.setEnabled(false);
        }
        fontSize.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        // Show what the default font will look like
                        setExampleFont(fontType);
                    }
                });
        return fontSize;
    }

    private ZapNumberSpinner getFontSize(FontUtils.FontType fontType) {
        if (fontSizes.get(fontType) == null) {
            fontSizes.put(fontType, initFontSize(fontType));
        }
        return fontSizes.get(fontType);
    }

    private void setExampleFont(FontUtils.FontType fontType) {
        String name;
        if (getFontName(fontType).getSelectedItem() == null) {
            name = "";
        } else {
            name = (String) getFontName(fontType).getSelectedItem();
        }
        Font font = FontUtils.getFont(name);
        int size = getFontSize(fontType).getValue();
        if (size == -1) {
            size = FontUtils.getSystemDefaultFont().getSize();
        }

        getFontExampleLabel(fontType).setFont(font.deriveFont((float) size));
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String> initFontName(FontUtils.FontType fontType) {
        JComboBox<String> fontName;
        fontName = new JComboBox<>();
        fontName.setRenderer(new JComboBoxFontRenderer());
        String fonts[] =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontName.addItem(" "); // Default to system font
        for (String font : fonts) {
            fontName.addItem(font);
        }
        if (!FontUtils.canChangeSize()) {
            fontName.setEnabled(false);
        }
        fontName.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Show what the default font will look like
                        setExampleFont(fontType);
                    }
                });
        return fontName;
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String> getFontName(FontUtils.FontType fontType) {
        if (fontNames.get(fontType) == null) {
            fontNames.put(fontType, initFontName(fontType));
        }
        return fontNames.get(fontType);
    }

    private JLabel getFontExampleLabel(FontUtils.FontType fontType) {
        if (fontLabels.get(fontType) == null) {
            fontLabels.put(
                    fontType,
                    new JLabel(Constant.messages.getString("view.options.label.exampleText")));
            fontLabels.get(fontType).setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        return fontLabels.get(fontType);
    }

    private JCheckBox getScaleImages() {
        if (scaleImages == null) {
            scaleImages = new JCheckBox();
            if (!FontUtils.canChangeSize()) {
                scaleImages.setEnabled(false);
            }
        }
        return scaleImages;
    }

    private JCheckBox getAllowAppsInContainers() {
        if (allowAppsInContainers == null) {
            allowAppsInContainers = new JCheckBox();
        }
        return allowAppsInContainers;
    }

    private JComboBox<LookAndFeelInfoUi> getLookAndFeelSelect() {
        if (lookAndFeel == null) {
            lookAndFeel = new JComboBox<>();
            lookAndFeel.setMaximumRowCount(5);
            UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
            lookAndFeel.addItem(new LookAndFeelInfoUi(OptionsParamView.DEFAULT_LOOK_AND_FEEL));
            for (UIManager.LookAndFeelInfo look : looks) {
                lookAndFeel.addItem(new LookAndFeelInfoUi(look));
            }
        }
        return lookAndFeel;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        getShowTabNames().setSelected(options.getViewParam().getShowTabNames());
        getShowSplashScreen().setSelected(options.getViewParam().isShowSplashScreen());
        getChkProcessImages().setSelected(options.getViewParam().getProcessImages() > 0);
        displaySelect.setSelectedIndex(options.getViewParam().getDisplayOption());
        String panelPosition = options.getViewParam().getResponsePanelPosition();
        selectItem(
                getResponsePanelPositionComboBox(),
                item -> item.getPosition().name().equals(panelPosition));
        brkPanelViewSelect.setSelectedIndex(options.getViewParam().getBrkPanelViewOption());
        getChkShowMainToolbar().setSelected(options.getViewParam().isShowMainToolbar());
        chkAdvancedView.setSelected(options.getViewParam().getAdvancedViewOption() > 0);
        chkAskOnExit.setSelected(options.getViewParam().getAskOnExitOption() > 0);
        chkWmUiHandling.setSelected(options.getViewParam().getWmUiHandlingOption() > 0);
        getChkOutputTabTimeStamps()
                .setSelected(options.getViewParam().isOutputTabTimeStampingEnabled());
        timeStampsFormatSelect.setSelectedItem(
                options.getViewParam().getOutputTabTimeStampsFormat());
        getShowLocalConnectRequestsCheckbox()
                .setSelected(options.getViewParam().isShowLocalConnectRequests());
        for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
            getFontSize(fontType).setValue(options.getViewParam().getFontSize(fontType));
            getFontName(fontType).setSelectedItem(options.getViewParam().getFontName(fontType));
        }

        getScaleImages().setSelected(options.getViewParam().isScaleImages());
        String nameLaf = options.getViewParam().getLookAndFeelInfo().getName();
        selectItem(
                getLookAndFeelSelect(),
                item -> item.getLookAndFeelInfo().getName().equals(nameLaf));
        getAllowAppsInContainers()
                .setSelected(options.getViewParam().isAllowAppIntegrationInContainers());
    }

    private static <T> void selectItem(JComboBox<T> comboBox, Predicate<T> predicate) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            T item = comboBox.getItemAt(i);
            if (predicate.test(item)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }

        if (comboBox.getSelectedIndex() == -1) {
            comboBox.setSelectedIndex(0);
        }
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        options.getViewParam().setShowTabNames(getShowTabNames().isSelected());
        options.getViewParam().setShowSplashScreen(getShowSplashScreen().isSelected());
        options.getViewParam().setProcessImages((getChkProcessImages().isSelected()) ? 1 : 0);
        options.getViewParam().setDisplayOption(displaySelect.getSelectedIndex());
        ResponsePanelPositionUI selectedItem =
                (ResponsePanelPositionUI) getResponsePanelPositionComboBox().getSelectedItem();
        options.getViewParam().setResponsePanelPosition(selectedItem.getPosition().name());
        options.getViewParam().setBrkPanelViewOption(brkPanelViewSelect.getSelectedIndex());
        if (brkPanelViewSelect.getSelectedIndex() == BreakLocation.TOOL_BAR_ONLY.getValue()) {
            options.getParamSet(BreakpointsParam.class).setShowIgnoreFilesButtons(false);
        }
        options.getViewParam().setShowMainToolbar(getChkShowMainToolbar().isSelected());
        options.getViewParam().setAdvancedViewOption(getChkAdvancedView().isSelected() ? 1 : 0);
        options.getViewParam().setAskOnExitOption(getChkAskOnExit().isSelected() ? 1 : 0);
        options.getViewParam().setWmUiHandlingOption(getChkWmUiHandling().isSelected() ? 1 : 0);
        options.getViewParam()
                .setOutputTabTimeStampingEnabled(getChkOutputTabTimeStamps().isSelected());
        options.getViewParam()
                .setOutputTabTimeStampsFormat(
                        (String) getTimeStampsFormatSelect().getSelectedItem());
        options.getViewParam()
                .setShowLocalConnectRequests(getShowLocalConnectRequestsCheckbox().isSelected());
        for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
            options.getViewParam().setFontSize(fontType, getFontSize(fontType).getValue());
            options.getViewParam()
                    .setFontName(fontType, (String) getFontName(fontType).getSelectedItem());
        }
        options.getViewParam().setScaleImages(getScaleImages().isSelected());
        options.getViewParam()
                .setLookAndFeelInfo(
                        ((LookAndFeelInfoUi) getLookAndFeelSelect().getSelectedItem())
                                .getLookAndFeelInfo());
        options.getViewParam()
                .setAllowAppIntegrationInContainers(getAllowAppsInContainers().isSelected());
    }

    @Override
    public String getHelpIndex() {
        // ZAP: added help index
        return "ui.dialogs.options.view";
    }

    @SuppressWarnings("serial")
    private class JComboBoxFontRenderer extends BasicComboBoxRenderer {
        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        @SuppressWarnings("rawtypes")
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer =
                    (JLabel)
                            defaultRenderer.getListCellRendererComponent(
                                    list, value, index, isSelected, cellHasFocus);
            Font font = FontUtils.getFont((String) value);
            if (font != null) {
                renderer.setFont(FontUtils.getFont((String) value));
            } else {
                renderer.setFont(FontUtils.getFont(FontUtils.Size.standard));
            }
            return renderer;
        }
    }

    private static class ResponsePanelPositionUI {

        private final String name;
        private final WorkbenchPanel.ResponsePanelPosition position;

        public ResponsePanelPositionUI(String name, WorkbenchPanel.ResponsePanelPosition position) {
            this.name = name;
            this.position = position;
        }

        public WorkbenchPanel.ResponsePanelPosition getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class LookAndFeelInfoUi {

        private final LookAndFeelInfo lookAndFeelInfo;

        LookAndFeelInfoUi(LookAndFeelInfo lookAndFeelInfo) {
            this.lookAndFeelInfo = lookAndFeelInfo;
        }

        LookAndFeelInfo getLookAndFeelInfo() {
            return lookAndFeelInfo;
        }

        @Override
        public String toString() {
            return lookAndFeelInfo.getName();
        }
    }
}
