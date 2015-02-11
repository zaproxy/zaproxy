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

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.MainFrame;
import org.parosproxy.paros.view.TabbedPanel;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.utils.DisplayUtils;

public class MessagePanelsPositionController {

    private enum MessagePanelsPosition {
        TABS_SIDE_BY_SIDE,
        PANEL_ABOVE,
        PANELS_SIDE_BY_SIDE
    }

    private static final String TABS_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.tabs");
    private static final String ABOVE_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.above");
    private static final String SIDE_BY_SIDE_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.sideBySide");

    private static final String BASE_KEY = OptionsParamView.BASE_VIEW_KEY + ".messagePanelsPosition.";

    private static final String LAST_POSITION_CONFIG_KEY = BASE_KEY + "lastSelectedPosition";

    private HttpPanelRequest requestPanel;
    private HttpPanelResponse responsePanel;

    private WorkbenchPanel workbenchPanel;
    private TabbedPanel tabbedWork;

    private JToggleButton tabsButtonView;
    private JToggleButton aboveButtonView;
    private JToggleButton sideBySideButtonView;

    private TabbedPanel splitTabbedPanel;

    private MessagePanelsPosition currentPosition;

    public MessagePanelsPositionController(
            HttpPanelRequest requestPanel,
            HttpPanelResponse responsePanel,
            MainFrame mainFrame,
            WorkbenchPanel workbenchPanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
        this.workbenchPanel = workbenchPanel;
        this.tabbedWork = workbenchPanel.getTabbedWork();
        this.currentPosition = MessagePanelsPosition.TABS_SIDE_BY_SIDE;

        tabsButtonView = new JToggleButton(new ChangeMessagePanelsPositionAction(
                MessagePanelsPositionController.class.getResource("/resource/icon/layout_tabbed.png"),
                MessagePanelsPosition.TABS_SIDE_BY_SIDE));
        tabsButtonView.setToolTipText(TABS_VIEW_TOOL_TIP);

        aboveButtonView = new JToggleButton(new ChangeMessagePanelsPositionAction(
                MessagePanelsPositionController.class.getResource("/resource/icon/layout_vertical_split.png"),
                MessagePanelsPosition.PANEL_ABOVE));
        aboveButtonView.setToolTipText(ABOVE_VIEW_TOOL_TIP);

        sideBySideButtonView = new JToggleButton(new ChangeMessagePanelsPositionAction(
                MessagePanelsPositionController.class.getResource("/resource/icon/layout_horizontal_split.png"),
                MessagePanelsPosition.PANELS_SIDE_BY_SIDE));
        sideBySideButtonView.setToolTipText(SIDE_BY_SIDE_VIEW_TOOL_TIP);

        ButtonGroup messageTabsPositionButtonGroup = new ButtonGroup();
        messageTabsPositionButtonGroup.add(tabsButtonView);
        messageTabsPositionButtonGroup.add(aboveButtonView);
        messageTabsPositionButtonGroup.add(sideBySideButtonView);

        tabsButtonView.setSelected(true);

        MainToolbarPanel toolbar = mainFrame.getMainToolbarPanel();
        
        toolbar.addButton(tabsButtonView);
        toolbar.addButton(aboveButtonView);
        toolbar.addButton(sideBySideButtonView);

        toolbar.addSeparator();

        splitTabbedPanel = new TabbedPanel();
        splitTabbedPanel.setAlternativeParent(mainFrame.getPaneDisplay());
        splitTabbedPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    private void changeMessageTabsPosition(MessagePanelsPosition position) {
        // 29.12.2013 Dejan Lukan: commented out this code, so the function is called when
        // changing the layout where the Request/Response tab representation stays the same.
        /*if (currentPosition == position) {
            return;
        }*/

        // save the current position, so we can change Request/Response view in 'Full Layout'
        currentPosition = position;
        saveState(position);

        // Prevent 'Request' icon from being removed when changing Request/Response tabs
        // in Full Layout mode.
        if(View.getDisplayOption() == View.DISPLAY_OPTION_TOP_FULL) {
            return;
        }

        TabbedPanel tabbedPanel = restoreOriginalParentTabbedPanel();

        switch (position) {
        case PANEL_ABOVE:
            splitResponsePanelWithWorkTabbedPanel(JSplitPane.VERTICAL_SPLIT);
            break;
        case PANELS_SIDE_BY_SIDE:
            splitResponsePanelWithWorkTabbedPanel(JSplitPane.HORIZONTAL_SPLIT);
            break;
        case TABS_SIDE_BY_SIDE:
        default:
            if (tabbedPanel == splitTabbedPanel) {
                tabbedPanel = tabbedWork;
            }
            boolean showTabNames = Model.getSingleton().getOptionsParam().getViewParam().getShowTabNames();
            String tabName = responsePanel.getName();
            if(!showTabNames) {
                tabName = "";
            }
            tabbedWork.insertTab(
                    tabName,
                    DisplayUtils.getScaledIcon(responsePanel.getIcon()),
                    responsePanel,
                    null,
                    tabbedWork.indexOfComponent(requestPanel) + 1);
            workbenchPanel.removeSplitPaneWork();
        }

        restoreAlternativeParentTabbedPanel(tabbedPanel);

    }


    private void splitResponsePanelWithWorkTabbedPanel(int orientation) {
        splitTabbedPanel.removeAll();
        boolean showTabNames = Model.getSingleton().getOptionsParam().getViewParam().getShowTabNames(); 
        if(showTabNames) {
            splitTabbedPanel.addTab(responsePanel.getName(), DisplayUtils.getScaledIcon(responsePanel.getIcon()), responsePanel);
        }
        else {
            splitTabbedPanel.addTab("", DisplayUtils.getScaledIcon(responsePanel.getIcon()), responsePanel);
        }

        workbenchPanel.splitPaneWorkWithTabbedPanel(splitTabbedPanel, orientation);
    }

    /**
     * Restores the original parent of the panels and the panel.
     */
    public TabbedPanel restoreOriginalParentTabbedPanel() {
        if (tabbedWork.isInAlternativeParent()) {
            tabbedWork.alternateParent();
            return tabbedWork;
        } else if (splitTabbedPanel.isInAlternativeParent()) {
            splitTabbedPanel.alternateParent();
            return splitTabbedPanel;
        }
        return null;
    }


    private void restoreAlternativeParentTabbedPanel(TabbedPanel tabbedPanel) {
        if (tabbedPanel != null) {
            tabbedPanel.alternateParent();
        }
    }

    public void restoreState() {
        FileConfiguration configuration = Model.getSingleton().getOptionsParam().getConfig();

        MessagePanelsPosition position = MessagePanelsPosition.valueOf(configuration.getString(
                LAST_POSITION_CONFIG_KEY,
                MessagePanelsPosition.TABS_SIDE_BY_SIDE.toString()));

        changeMessageTabsPosition(position);

        switch (position) {
        case PANEL_ABOVE:
            aboveButtonView.setSelected(true);
            break;
        case PANELS_SIDE_BY_SIDE:
            sideBySideButtonView.setSelected(true);
            break;
        case TABS_SIDE_BY_SIDE:
        default:
            tabsButtonView.setSelected(true);
        }
    }

    public void saveState(MessagePanelsPosition currentPosition) {
        FileConfiguration configuration = Model.getSingleton().getOptionsParam().getConfig();

        configuration.setProperty(LAST_POSITION_CONFIG_KEY, currentPosition.toString());
    }

    private final class ChangeMessagePanelsPositionAction extends AbstractAction {

        private static final long serialVersionUID = 756133292459364854L;

        private final MessagePanelsPosition position;

        public ChangeMessagePanelsPositionAction(URL iconLocation, MessagePanelsPosition position) {
            super("", new ImageIcon(iconLocation));

            this.position = position;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            changeMessageTabsPosition(position);
        }
    }

}
