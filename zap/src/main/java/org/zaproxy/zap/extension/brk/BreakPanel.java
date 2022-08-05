/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.brk;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.InvalidMessageDataException;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.HttpPanelViewModelUtils;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.ZapToggleButton;

@SuppressWarnings("serial")
public class BreakPanel extends AbstractPanel implements BreakpointManagementInterface {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(BreakPanel.class);

    private static final String REQUEST_PANEL = "request";
    private static final String RESPONSE_PANEL = "response";

    private HttpPanelRequest requestPanel;
    private HttpPanelResponse responsePanel;

    private ExtensionBreak extension;
    private JPanel panelContent;
    private BreakPanelToolbarFactory breakToolbarFactory;
    private BreakpointsParam breakpointsParams;

    private final JToggleButton toolBarReqButton;
    private final JToggleButton toolBarResButton;
    private final JToggleButton toolBarAllButton;
    private final JButton toolBarBtnStep;
    private final JButton toolBarBtnContinue;
    private final JButton toolBarBtnDrop;
    private final JButton toolBarBtnBreakPoint;

    private ZapToggleButton fixRequestContentLength = null;
    private ZapToggleButton fixResponseContentLength = null;

    private Message msg;
    private boolean isAlwaysOnTop = false;
    private boolean request;

    /** The break buttons shown in the main panel of the Break tab. */
    private final BreakButtonsUI mainBreakButtons;

    /** The break buttons shown in the request panel of the Break tab. */
    private final BreakButtonsUI requestBreakButtons;

    /** The break buttons shown in the response panel of the Break tab. */
    private final BreakButtonsUI responseBreakButtons;

    /**
     * The current location of the break buttons.
     *
     * @see #setButtonsLocation(int)
     */
    private int currentButtonsLocation;

    /**
     * The current button mode.
     *
     * @see #setButtonMode(int)
     */
    private int currentButtonMode;

    public BreakPanel(ExtensionBreak extension, BreakpointsParam breakpointsParams) {
        super();
        this.extension = extension;
        this.breakpointsParams = breakpointsParams;

        this.setIcon(
                new ImageIcon(
                        BreakPanel.class.getResource(
                                "/resource/icon/16/101grey.png"))); // 'grey X' icon

        this.setDefaultAccelerator(
                extension
                        .getView()
                        .getMenuShortcutKeyStroke(KeyEvent.VK_B, KeyEvent.SHIFT_DOWN_MASK, false));
        this.setMnemonic(Constant.messages.getChar("brk.panel.mnemonic"));

        this.setLayout(new BorderLayout());

        breakToolbarFactory = new BreakPanelToolbarFactory(extension, breakpointsParams, this);

        panelContent = new JPanel(new CardLayout());
        this.add(panelContent, BorderLayout.CENTER);

        requestPanel = new HttpPanelRequest(false, OptionsParamView.BASE_VIEW_KEY + ".break.");
        requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
        responsePanel = new HttpPanelResponse(false, OptionsParamView.BASE_VIEW_KEY + ".break.");
        responsePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());

        panelContent.add(requestPanel, REQUEST_PANEL);
        panelContent.add(responsePanel, RESPONSE_PANEL);

        toolBarReqButton = breakToolbarFactory.getBtnBreakRequest();
        View.getSingleton().addMainToolbarButton(toolBarReqButton);

        toolBarResButton = breakToolbarFactory.getBtnBreakResponse();
        View.getSingleton().addMainToolbarButton(toolBarResButton);

        toolBarAllButton = breakToolbarFactory.getBtnBreakAll();
        View.getSingleton().addMainToolbarButton(toolBarAllButton);

        toolBarBtnStep = breakToolbarFactory.getBtnStep();
        View.getSingleton().addMainToolbarButton(toolBarBtnStep);

        toolBarBtnContinue = breakToolbarFactory.getBtnContinue();
        View.getSingleton().addMainToolbarButton(toolBarBtnContinue);

        toolBarBtnDrop = breakToolbarFactory.getBtnDrop();
        View.getSingleton().addMainToolbarButton(toolBarBtnDrop);

        toolBarBtnBreakPoint = breakToolbarFactory.getBtnBreakPoint();
        View.getSingleton().addMainToolbarButton(toolBarBtnBreakPoint);

        mainBreakButtons = new BreakButtonsUI("mainBreakButtons", breakToolbarFactory);
        this.add(mainBreakButtons.getComponent(), BorderLayout.NORTH);

        requestBreakButtons = new BreakButtonsUI("requestBreakButtons", breakToolbarFactory);
        requestPanel.addOptions(
                requestBreakButtons.getComponent(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);

        responseBreakButtons = new BreakButtonsUI("responseBreakButtons", breakToolbarFactory);
        responsePanel.addOptions(
                responseBreakButtons.getComponent(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);

        // The options toolbars are always added just to the Break request and response panels
        JToolBar requestOptionsToolBar = new JToolBar();
        requestOptionsToolBar.setFloatable(false);
        requestOptionsToolBar.setBorder(BorderFactory.createEmptyBorder());
        requestOptionsToolBar.setRollover(true);

        requestOptionsToolBar.add(this.getRequestButtonFixContentLength());
        requestPanel.addOptions(requestOptionsToolBar, HttpPanel.OptionsLocation.AFTER_COMPONENTS);

        JToolBar responseOptionsToolBar = new JToolBar();
        responseOptionsToolBar.setFloatable(false);
        responseOptionsToolBar.setBorder(BorderFactory.createEmptyBorder());
        responseOptionsToolBar.setRollover(true);

        responseOptionsToolBar.add(this.getResponseButtonFixContentLength());
        responsePanel.addOptions(
                responseOptionsToolBar, HttpPanel.OptionsLocation.AFTER_COMPONENTS);

        currentButtonsLocation = -1;
    }

    /**
     * Sets the location of the break buttons.
     *
     * <p>If the location is already set and the main tool bar visibility is the same, no change is
     * done.
     *
     * @param location the location to set
     */
    void setButtonsLocation(int location) {
        if (currentButtonsLocation == location) {
            mainBreakButtons.setVisible(location == 0 && isMainToolBarHidden());
            return;
        }
        currentButtonsLocation = location;

        switch (location) {
            case 0:
                requestBreakButtons.setVisible(false);
                responseBreakButtons.setVisible(false);
                setToolbarButtonsVisible(true);

                // If the user decided to disable the main toolbar, the break
                // buttons have to be force to be displayed in the break panel
                mainBreakButtons.setVisible(isMainToolBarHidden());
                break;
            case 1:
            case 2:
                requestBreakButtons.setVisible(true);
                responseBreakButtons.setVisible(true);
                setToolbarButtonsVisible(location == 2);

                mainBreakButtons.setVisible(false);
                break;
            default:
                setToolbarButtonsVisible(true);
        }
    }

    /**
     * Tells whether or not the main tool bar is hidden.
     *
     * @return {@code true} if the main tool bar is hidden, {@code false} otherwise
     */
    private boolean isMainToolBarHidden() {
        return !extension.getModel().getOptionsParam().getViewParam().isShowMainToolbar();
    }

    @Override
    public boolean isBreakRequest() {
        return breakToolbarFactory.isBreakRequest();
    }

    @Override
    public boolean isBreakResponse() {
        return breakToolbarFactory.isBreakResponse();
    }

    @Override
    public boolean isBreakAll() {
        return breakToolbarFactory.isBreakAll();
    }

    @Override
    public void breakpointHit() {
        breakToolbarFactory.breakpointHit();
    }

    @Override
    public boolean isHoldMessage(Message aMessage) {
        return breakToolbarFactory.isHoldMessage();
    }

    public boolean isHoldMessage() {
        return breakToolbarFactory.isHoldMessage();
    }

    @Override
    public boolean isStepping() {
        return breakToolbarFactory.isStepping();
    }

    @Override
    public boolean isToBeDropped() {
        return breakToolbarFactory.isToBeDropped();
    }

    @Override
    public void breakpointDisplayed() {
        if (!View.getSingleton().isCanGetFocus()) {
            return;
        }
        final Boolean alwaysOnTopOption = breakpointsParams.getAlwaysOnTop();
        if (alwaysOnTopOption == null || alwaysOnTopOption) {

            java.awt.EventQueue.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {

                            View.getSingleton().getMainFrame().setAlwaysOnTop(true);
                            View.getSingleton().getMainFrame().toFront();
                            setTabFocus();
                            isAlwaysOnTop = true;

                            if (alwaysOnTopOption == null) {
                                // Prompt the user the first time
                                boolean keepOn =
                                        View.getSingleton()
                                                        .showConfirmDialog(
                                                                Constant.messages.getString(
                                                                        "brk.alwaysOnTop.message"))
                                                == JOptionPane.OK_OPTION;
                                breakpointsParams.setAlwaysOnTop(keepOn);
                                if (!keepOn) {
                                    // Turn it off
                                    View.getSingleton().getMainFrame().setAlwaysOnTop(false);
                                    isAlwaysOnTop = false;
                                }
                            }
                        }
                    });
        }
        try {
            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            View.getSingleton().getMainFrame().toFront();
                        }
                    });
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private void setToolbarButtonsVisible(boolean visible) {
        boolean simple = currentButtonMode == BreakpointsParam.BUTTON_MODE_SIMPLE;
        toolBarReqButton.setVisible(visible && !simple);
        toolBarResButton.setVisible(visible && !simple);
        toolBarAllButton.setVisible(visible && simple);
        toolBarBtnStep.setVisible(visible);
        toolBarBtnContinue.setVisible(visible);
        toolBarBtnDrop.setVisible(visible);
        toolBarBtnBreakPoint.setVisible(visible);
    }

    @Override
    public void setMessage(final Message aMessage, final boolean isRequest) {
        try {
            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            msg = aMessage;
                            CardLayout cl = (CardLayout) (panelContent.getLayout());
                            request = isRequest;

                            if (isRequest) {
                                requestPanel.setMessage(aMessage, true);
                                requestPanel.setEditable(true);
                                getRequestButtonFixContentLength()
                                        .setVisible(msg instanceof HttpMessage);
                                cl.show(panelContent, REQUEST_PANEL);
                            } else {
                                responsePanel.setMessage(aMessage, true);
                                responsePanel.setEditable(true);
                                getResponseButtonFixContentLength()
                                        .setVisible(msg instanceof HttpMessage);
                                cl.show(panelContent, RESPONSE_PANEL);
                            }
                        }
                    });
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean isRequest() {
        return this.request;
    }

    @Override
    public Message getMessage() {
        return msg;
    }

    @Override
    public void saveMessage(final boolean isRequest) {
        try {
            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            CardLayout cl = (CardLayout) (panelContent.getLayout());

                            if (isRequest) {
                                Message msg = getMessage();
                                if (msg instanceof HttpMessage
                                        && getRequestButtonFixContentLength().isSelected()) {
                                    HttpPanelViewModelUtils.updateRequestContentLength(
                                            (HttpMessage) msg);
                                }
                                cl.show(panelContent, REQUEST_PANEL);
                            } else {
                                Message msg = getMessage();
                                if (msg instanceof HttpMessage
                                        && getResponseButtonFixContentLength().isSelected()) {
                                    HttpPanelViewModelUtils.updateResponseContentLength(
                                            (HttpMessage) msg);
                                }
                                cl.show(panelContent, RESPONSE_PANEL);
                            }
                        }
                    });
        } catch (Exception ie) {
            LOGGER.warn(ie.getMessage(), ie);
        }
    }

    boolean isValidState() {
        HttpPanel panel = isRequest() ? requestPanel : responsePanel;
        try {
            panel.saveData();
            return true;
        } catch (InvalidMessageDataException e) {
            StringBuilder warnMessage = new StringBuilder(150);
            warnMessage.append(Constant.messages.getString("brk.panel.warn.datainvalid"));

            String exceptionMessage = e.getLocalizedMessage();
            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                warnMessage.append('\n').append(exceptionMessage);
            }
            View.getSingleton().showWarningDialog(warnMessage.toString());
        }
        return false;
    }

    public void savePanels() {
        requestPanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
        responsePanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
    }

    @Override
    public void clearAndDisableRequest() {
        if (EventQueue.isDispatchThread()) {
            clearAndDisableRequestEventHandler();
        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                clearAndDisableRequestEventHandler();
                            }
                        });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    private void clearAndDisableRequestEventHandler() {
        this.msg = null;
        requestPanel.clearView(false);
        requestPanel.setEditable(false);
        getRequestButtonFixContentLength().setVisible(false);
        breakpointLeft();
    }

    @Override
    public void clearAndDisableResponse() {
        if (EventQueue.isDispatchThread()) {
            clearAndDisableResponseEventHandler();
        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                clearAndDisableResponseEventHandler();
                            }
                        });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    private void clearAndDisableResponseEventHandler() {
        this.msg = null;
        responsePanel.clearView(false);
        responsePanel.setEditable(false);
        getResponseButtonFixContentLength().setVisible(false);
        breakpointLeft();
    }

    private void breakpointLeft() {
        if (this.isAlwaysOnTop) {
            View.getSingleton().getMainFrame().setAlwaysOnTop(false);
            this.isAlwaysOnTop = false;
        }
    }

    @Override
    public void init() {
        breakToolbarFactory.init();
    }

    @Override
    public void reset() {
        this.msg = null;
        breakToolbarFactory.reset();
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        if (mode.equals(Mode.safe)) {
            this.breakToolbarFactory.setBreakEnabled(false);
        } else {
            this.breakToolbarFactory.setBreakEnabled(true);
        }
    }

    @Override
    public void setBreakAllRequests(boolean brk) {
        breakToolbarFactory.setBreakRequest(brk);
    }

    @Override
    public void setBreakAllResponses(boolean brk) {
        breakToolbarFactory.setBreakResponse(brk);
    }

    @Override
    public void setBreakAll(boolean brk) {
        breakToolbarFactory.setBreakAll(brk);
    }

    @Override
    public void step() {
        if (!isValidState()) {
            return;
        }

        breakToolbarFactory.step();
    }

    @Override
    public void cont() {
        if (!isValidState()) {
            return;
        }

        breakToolbarFactory.setContinue(true);
        breakToolbarFactory.setBreakAll(false);
        breakToolbarFactory.setBreakRequest(false);
        breakToolbarFactory.setBreakResponse(false);
    }

    @Override
    public void drop() {
        breakToolbarFactory.drop();
    }

    public void showNewBreakPointDialog() {
        extension.addUiBreakpoint(new HttpMessage());
    }

    public void setButtonMode(int mode) {
        if (currentButtonMode == mode) {
            return;
        }
        currentButtonMode = mode;

        this.breakToolbarFactory.setButtonMode(mode);

        if (currentButtonsLocation == 0 || currentButtonsLocation == 2) {
            boolean simple = mode == BreakpointsParam.BUTTON_MODE_SIMPLE;
            toolBarReqButton.setVisible(!simple);
            toolBarResButton.setVisible(!simple);
            toolBarAllButton.setVisible(simple);
        }

        mainBreakButtons.setButtonMode(mode);
        requestBreakButtons.setButtonMode(mode);
        responseBreakButtons.setButtonMode(mode);
    }

    protected void setShowIgnoreFilesButtons(boolean showButtons) {
        this.breakToolbarFactory.setShowIgnoreFilesButtons(showButtons);

        mainBreakButtons.setShowIgnoreFilesButtons(showButtons);
        requestBreakButtons.setShowIgnoreFilesButtons(showButtons);
        responseBreakButtons.setShowIgnoreFilesButtons(showButtons);
    }

    public List<BreakpointMessageInterface> getIgnoreRulesEnableList() {
        return breakToolbarFactory.getIgnoreRulesEnableList();
    }

    public void updateIgnoreFileTypesRegexs() {
        breakToolbarFactory.updateIgnoreFileTypesRegexs();
    }

    private ZapToggleButton getRequestButtonFixContentLength() {
        if (fixRequestContentLength == null) {
            fixRequestContentLength =
                    new ZapToggleButton(
                            DisplayUtils.getScaledIcon(
                                    new ImageIcon(
                                            BreakPanel.class.getResource(
                                                    "/resource/icon/fugue/application-resize.png"))),
                            true);
            fixRequestContentLength.setToolTipText(
                    Constant.messages.getString("brk.checkBox.fixLength"));
            fixRequestContentLength.setVisible(false);
        }
        return fixRequestContentLength;
    }

    private ZapToggleButton getResponseButtonFixContentLength() {
        if (fixResponseContentLength == null) {
            fixResponseContentLength =
                    new ZapToggleButton(
                            DisplayUtils.getScaledIcon(
                                    new ImageIcon(
                                            BreakPanel.class.getResource(
                                                    "/resource/icon/fugue/application-resize.png"))),
                            true);
            fixResponseContentLength.setToolTipText(
                    Constant.messages.getString("brk.checkBox.fixLength"));
            fixResponseContentLength.setVisible(false);
        }
        return fixResponseContentLength;
    }

    /**
     * A wrapper of a view component with break related buttons/functionality.
     *
     * @see #getComponent()
     */
    private static class BreakButtonsUI {

        private final JToolBar toolBar;

        private final JToggleButton requestButton;
        private final JToggleButton responseButton;
        private final JToggleButton allButton;
        private final Separator separatorForBreakOnButtons;
        private final JToggleButton brkOnJavaScriptButton;
        private final JToggleButton brkOnCssAndFontsButton;
        private final JToggleButton brkOnMultimediaButton;
        private final Separator separatorForBreakOnlyScopeButton;
        private final JToggleButton brkOnlyOnScopeButton;

        public BreakButtonsUI(String name, BreakPanelToolbarFactory breakToolbarFactory) {
            requestButton = breakToolbarFactory.getBtnBreakRequest();
            responseButton = breakToolbarFactory.getBtnBreakResponse();
            allButton = breakToolbarFactory.getBtnBreakAll();
            separatorForBreakOnButtons = new JToolBar.Separator();
            brkOnJavaScriptButton = breakToolbarFactory.getBtnBreakOnJavaScript();
            brkOnCssAndFontsButton = breakToolbarFactory.getBtnBreakOnCssAndFonts();
            brkOnMultimediaButton = breakToolbarFactory.getBtnBreakOnMultimedia();
            separatorForBreakOnlyScopeButton = new JToolBar.Separator();
            brkOnlyOnScopeButton = breakToolbarFactory.getBtnOnlyBreakOnScope();

            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setBorder(BorderFactory.createEmptyBorder());
            toolBar.setRollover(true);

            toolBar.setName(name);

            toolBar.add(requestButton);
            toolBar.add(responseButton);
            toolBar.add(allButton);
            toolBar.add(breakToolbarFactory.getBtnStep());
            toolBar.add(breakToolbarFactory.getBtnContinue());
            toolBar.add(breakToolbarFactory.getBtnDrop());
            toolBar.add(breakToolbarFactory.getBtnBreakPoint());
            toolBar.add(separatorForBreakOnButtons);
            toolBar.add(brkOnJavaScriptButton);
            toolBar.add(brkOnCssAndFontsButton);
            toolBar.add(brkOnMultimediaButton);
            toolBar.add(separatorForBreakOnlyScopeButton);
            toolBar.add(brkOnlyOnScopeButton);
        }

        /**
         * Sets whether or not the underlying view component is visible.
         *
         * @param visible {@code true} if the view component should be visible, {@code false}
         *     otherwise
         */
        public void setVisible(boolean visible) {
            toolBar.setVisible(visible);
        }

        /**
         * Sets the current button mode.
         *
         * @param mode the mode to be set
         * @see BreakpointsParam#BUTTON_MODE_SIMPLE
         * @see BreakpointsParam#BUTTON_MODE_DUAL
         */
        public void setButtonMode(int mode) {
            boolean simple = mode == BreakpointsParam.BUTTON_MODE_SIMPLE;
            requestButton.setVisible(!simple);
            responseButton.setVisible(!simple);
            allButton.setVisible(simple);
        }

        public void setShowIgnoreFilesButtons(boolean showButtons) {
            separatorForBreakOnButtons.setVisible(showButtons);
            brkOnJavaScriptButton.setVisible(showButtons);
            brkOnCssAndFontsButton.setVisible(showButtons);
            brkOnMultimediaButton.setVisible(showButtons);
            separatorForBreakOnlyScopeButton.setVisible(showButtons);
            brkOnlyOnScopeButton.setVisible(showButtons);
        }

        /**
         * Gets the underlying view component, with the break buttons.
         *
         * @return the view component
         */
        public JComponent getComponent() {
            return toolBar;
        }
    }
}
