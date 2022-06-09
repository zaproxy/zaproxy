/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.TabbedPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.brk.impl.http.HttpBreakpointMessage;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.Stats;
import org.zaproxy.zap.view.TabbedPanel2;
import org.zaproxy.zap.view.ZapToggleButton;

// Button notes
// BreakRequest button, if set all requests trapped
// BreakResponse button, ditto for responses
// If breakpoint hit, Break tab gets focus and icon goes red
// Step button, only if breakpoint hit, submits just this req/resp, breaks on next
// Continue button, only if breakpoint hit, submits this req/resp and continues until next break
// point hit
// If BreakReq & Resp both selected Step and Continue buttons have same effect
//

public class BreakPanelToolbarFactory {

    private static final String ICON_RESOURCE_PATH = "/resource/icon/";

    private ContinueButtonAction continueButtonAction;
    private StepButtonAction stepButtonAction;
    private DropButtonAction dropButtonAction;
    private AddBreakpointButtonAction addBreakpointButtonAction;

    private BreakRequestsButtonAction breakRequestsButtonAction;
    private BreakResponsesButtonAction breakResponsesButtonAction;
    private BreakAllButtonAction breakAllButtonAction;

    private SetBreakOnJavaScriptAction setBreakOnJavaScriptAction;
    private SetBreakOnCssAndFontsAction setBreakOnCssAndFontsAction;
    private SetBreakOnMultimediaAction setBreakOnMultimediaAction;
    private SetBreakOnlyOnScopeAction setOnlyBreakOnScopeAction;

    private boolean cont = false;
    private boolean step = false;
    private boolean stepping = false;
    private boolean drop = false;
    private boolean isBreakRequest = false;
    private boolean isBreakResponse = false;
    private boolean isBreakAll = false;
    private boolean isBreakOnJavaScript = true;
    private boolean isBreakOnCssAndFonts = true;
    private boolean isBreakOnMultimedia = true;

    private BreakPanel breakPanel = null;

    private BreakpointsParam breakpointsParams;
    private int mode = 0;

    private List<BreakpointMessageInterface> ignoreRulesEnable;
    private HttpBreakpointMessage ignoreJavascriptBreakpointMessage;
    private HttpBreakpointMessage ignoreCssAndFontsBreakpointMessage;
    private HttpBreakpointMessage ignoreMultimediaBreakpointMessage;

    /**
     * A counter to keep track of how many messages are currently caught, to disable the break
     * buttons when no message is left.
     *
     * <p>The counter is increased when a {@link #breakpointHit() breakpoint is hit} and decreased
     * when a message is no longer {@link #isHoldMessage() being held}.
     *
     * @see #countLock
     */
    private int countCaughtMessages;

    /** The object to synchronise changes to {@link #countCaughtMessages}. */
    private final Object countLock = new Object();

    private final ExtensionBreak extensionBreak;

    public BreakPanelToolbarFactory(BreakpointsParam breakpointsParams, BreakPanel breakPanel) {
        this(null, breakpointsParams, breakPanel);
    }

    public BreakPanelToolbarFactory(
            ExtensionBreak extensionBreak,
            BreakpointsParam breakpointsParams,
            BreakPanel breakPanel) {
        super();

        this.extensionBreak = extensionBreak;

        continueButtonAction = new ContinueButtonAction();
        stepButtonAction = new StepButtonAction();
        dropButtonAction = new DropButtonAction();
        addBreakpointButtonAction = new AddBreakpointButtonAction();

        breakRequestsButtonAction = new BreakRequestsButtonAction();
        breakResponsesButtonAction = new BreakResponsesButtonAction();
        breakAllButtonAction = new BreakAllButtonAction();

        setBreakOnJavaScriptAction = new SetBreakOnJavaScriptAction();
        setBreakOnCssAndFontsAction = new SetBreakOnCssAndFontsAction();
        setBreakOnMultimediaAction = new SetBreakOnMultimediaAction();
        setOnlyBreakOnScopeAction = new SetBreakOnlyOnScopeAction();

        this.breakpointsParams = breakpointsParams;
        this.breakPanel = breakPanel;

        this.ignoreRulesEnable = new ArrayList<>(3);

        ignoreJavascriptBreakpointMessage =
                new HttpBreakpointMessage(
                        breakpointsParams.getJavascriptUrlRegex(),
                        HttpBreakpointMessage.Location.url,
                        HttpBreakpointMessage.Match.regex,
                        false,
                        true);
        ignoreRulesEnable.add(ignoreJavascriptBreakpointMessage);

        ignoreCssAndFontsBreakpointMessage =
                new HttpBreakpointMessage(
                        breakpointsParams.getCssAndFontsUrlRegex(),
                        HttpBreakpointMessage.Location.url,
                        HttpBreakpointMessage.Match.regex,
                        false,
                        true);
        ignoreRulesEnable.add(ignoreCssAndFontsBreakpointMessage);

        ignoreMultimediaBreakpointMessage =
                new HttpBreakpointMessage(
                        breakpointsParams.getMultimediaUrlRegex(),
                        HttpBreakpointMessage.Location.url,
                        HttpBreakpointMessage.Match.regex,
                        false,
                        true);
        ignoreRulesEnable.add(ignoreMultimediaBreakpointMessage);
    }

    public List<BreakpointMessageInterface> getIgnoreRulesEnableList() {
        return ignoreRulesEnable;
    }

    private static ImageIcon getScaledIcon(String path) {
        return DisplayUtils.getScaledIcon(
                new ImageIcon(
                        BreakPanelToolbarFactory.class.getResource(ICON_RESOURCE_PATH + path)));
    }

    private void setActiveIcon(boolean active) {
        if (active) {
            // Have to do this before the getParent() call
            breakPanel.setTabFocus();
        }
        if (breakPanel.getParent() instanceof TabbedPanel) {
            TabbedPanel parent = (TabbedPanel) breakPanel.getParent();
            if (active) {
                parent.setIconAt(
                        parent.indexOfComponent(breakPanel), getScaledIcon("16/101.png")); // Red X
            } else {
                parent.setIconAt(
                        parent.indexOfComponent(breakPanel),
                        getScaledIcon("16/101grey.png")); // Grey X
            }
            if (parent instanceof TabbedPanel2) {
                // If possible lock the tab while it is active so it cant be closed
                ((TabbedPanel2) parent).setTabLocked(breakPanel, !active);
            }
        }
    }

    public void breakpointHit() {
        synchronized (countLock) {
            countCaughtMessages++;
        }

        // This could have been via a breakpoint, so force the serialisation
        resetRequestSerialization(true);

        // Set the active icon and reset the continue button
        this.setActiveIcon(true);
        setContinue(false);
    }

    public boolean isBreakRequest() {
        return isBreakRequest || isBreakAll;
    }

    public boolean isBreakResponse() {
        return isBreakResponse || isBreakAll;
    }

    public boolean isBreakAll() {
        return isBreakAll;
    }

    public JButton getBtnStep() {
        return new JButton(stepButtonAction);
    }

    public JButton getBtnContinue() {
        return new JButton(continueButtonAction);
    }

    public JButton getBtnDrop() {
        return new JButton(dropButtonAction);
    }

    private int askForDropConfirmation() {
        String title = Constant.messages.getString("brk.dialogue.confirmDropMessage.title");
        String message = Constant.messages.getString("brk.dialogue.confirmDropMessage.message");
        JCheckBox checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "brk.dialogue.confirmDropMessage.option.dontAskAgain"));
        String confirmButtonLabel =
                Constant.messages.getString("brk.dialogue.confirmDropMessage.button.confirm.label");
        String cancelButtonLabel =
                Constant.messages.getString("brk.dialogue.confirmDropMessage.button.cancel.label");

        int option =
                JOptionPane.showOptionDialog(
                        View.getSingleton().getMainFrame(),
                        new Object[] {message, " ", checkBox},
                        title,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] {confirmButtonLabel, cancelButtonLabel},
                        null);

        if (checkBox.isSelected()) {
            breakpointsParams.setConfirmDropMessage(false);
        }

        return option;
    }

    public JToggleButton getBtnBreakRequest() {
        ZapToggleButton btnBreakRequest;

        btnBreakRequest = new ZapToggleButton(breakRequestsButtonAction);
        btnBreakRequest.setSelectedIcon(getScaledIcon("16/105r.png"));

        btnBreakRequest.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.request.unset"));

        return btnBreakRequest;
    }

    public JToggleButton getBtnBreakResponse() {
        ZapToggleButton btnBreakResponse;

        btnBreakResponse = new ZapToggleButton(breakResponsesButtonAction);
        btnBreakResponse.setSelectedIcon(getScaledIcon("16/106r.png"));

        btnBreakResponse.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.response.unset"));

        return btnBreakResponse;
    }

    public JToggleButton getBtnBreakAll() {
        ZapToggleButton btnBreakAll;

        btnBreakAll = new ZapToggleButton(breakAllButtonAction);
        btnBreakAll.setSelectedIcon(getScaledIcon("16/151.png"));

        btnBreakAll.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.all.unset"));

        return btnBreakAll;
    }

    public JToggleButton getBtnBreakOnJavaScript() {
        ZapToggleButton btnBreakOnJavaScript;

        btnBreakOnJavaScript = new ZapToggleButton(setBreakOnJavaScriptAction);
        btnBreakOnJavaScript.setSelectedIcon(getScaledIcon("breakTypes/javascriptNotBreaking.png"));
        btnBreakOnJavaScript.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.brkjavascript.set"));

        return btnBreakOnJavaScript;
    }

    public JToggleButton getBtnBreakOnCssAndFonts() {
        ZapToggleButton btnBreakOnCssAndFonts;

        btnBreakOnCssAndFonts = new ZapToggleButton(setBreakOnCssAndFontsAction);
        btnBreakOnCssAndFonts.setSelectedIcon(
                getScaledIcon("breakTypes/cssAndFontsNotBreaking.png"));
        btnBreakOnCssAndFonts.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.brkcssfonts.set"));

        return btnBreakOnCssAndFonts;
    }

    public JToggleButton getBtnBreakOnMultimedia() {
        ZapToggleButton btnBreakOnMultimedia;

        btnBreakOnMultimedia = new ZapToggleButton(setBreakOnMultimediaAction);
        btnBreakOnMultimedia.setSelectedIcon(getScaledIcon("breakTypes/multimediaNotBreaking.png"));
        btnBreakOnMultimedia.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.brkmultimedia.set"));

        return btnBreakOnMultimedia;
    }

    public JToggleButton getBtnOnlyBreakOnScope() {
        ZapToggleButton btnOnlyBreakOnScope;

        btnOnlyBreakOnScope = new ZapToggleButton(setOnlyBreakOnScopeAction);
        btnOnlyBreakOnScope.setSelectedIcon(getScaledIcon("fugue/target.png"));

        btnOnlyBreakOnScope.setSelectedToolTipText(
                Constant.messages.getString("brk.toolbar.button.brkOnlyOnScope.unset"));

        return btnOnlyBreakOnScope;
    }

    public JButton getBtnBreakPoint() {
        return new JButton(addBreakpointButtonAction);
    }

    public boolean isStepping() {
        return stepping;
    }

    private void resetRequestSerialization(boolean forceSerialize) {
        if (Control.getSingleton() == null) {
            // Still in setup
            return;
        }
        // If forces or either break buttons are pressed force the proxy to submit requests and
        // responses serially
        boolean serialise = forceSerialize || isBreakRequest() || isBreakResponse() || isBreakAll;
        if (extensionBreak != null) {
            extensionBreak.getSerialisationRequiredListeners().forEach(e -> e.accept(serialise));
        }
    }

    public void setBreakRequest(boolean brk) {
        isBreakRequest = brk;
        resetRequestSerialization(false);

        breakRequestsButtonAction.setSelected(isBreakRequest);
    }

    public void setBreakResponse(boolean brk) {
        isBreakResponse = brk;
        resetRequestSerialization(false);

        breakResponsesButtonAction.setSelected(isBreakResponse);
    }

    public void setBreakAll(boolean brk) {
        isBreakAll = brk;
        if (!brk) {
            stepping = false;
        }

        resetRequestSerialization(false);

        breakAllButtonAction.setSelected(isBreakAll);
    }

    public void setBreakOnJavaScript(boolean brk) {
        isBreakOnJavaScript = brk;
        setBreakOnJavaScriptAction.setSelected(!isBreakOnJavaScript);
        ignoreJavascriptBreakpointMessage.setEnabled(!isBreakOnJavaScript);
    }

    public void setBreakOnCssAndFonts(boolean brk) {
        isBreakOnCssAndFonts = brk;
        setBreakOnCssAndFontsAction.setSelected(!isBreakOnCssAndFonts);
        ignoreCssAndFontsBreakpointMessage.setEnabled(!isBreakOnCssAndFonts);
    }

    public void setBreakOnMultimedia(boolean brk) {
        isBreakOnMultimedia = brk;
        setBreakOnMultimediaAction.setSelected(!isBreakOnMultimedia);
        ignoreMultimediaBreakpointMessage.setEnabled(!isBreakOnMultimedia);
    }

    public void setOnlyBreakOnScope(boolean brk) {
        breakpointsParams.setInScopeOnly(brk);
        setOnlyBreakOnScopeAction.setSelected(brk);
    }

    private void toggleBreakRequest() {
        setBreakRequest(!isBreakRequest);
    }

    private void toggleBreakResponse() {
        setBreakResponse(!isBreakResponse);
    }

    private void toggleBreakAll() {
        setBreakAll(!isBreakAll);
    }

    private void toggleBreakOnJavascript() {
        setBreakOnJavaScript(!isBreakOnJavaScript);
    }

    private void toggleBreakOnCssAndFonts() {
        setBreakOnCssAndFonts(!isBreakOnCssAndFonts);
    }

    private void toggleBreakOnMultimedia() {
        setBreakOnMultimedia(!isBreakOnMultimedia);
    }

    private void toggleBreakOnScopeOnly() {
        setOnlyBreakOnScope(!breakpointsParams.isInScopeOnly());
    }

    public boolean isHoldMessage() {
        if (isHoldMessageImpl()) {
            return true;
        }

        synchronized (countLock) {
            countCaughtMessages--;
            if (countCaughtMessages == 0) {
                setButtonsAndIconState(false);
            }
        }
        return false;
    }

    private boolean isHoldMessageImpl() {
        if (step) {
            // Only works one time, until its pressed again
            stepping = true;
            step = false;
            return false;
        }
        if (cont) {
            // They've pressed the continue button, stop stepping
            stepping = false;
            resetRequestSerialization(false);
            return false;
        }
        if (drop) {
            return false;
        }
        return true;
    }

    public boolean isContinue() {
        return cont;
    }

    public void setBreakEnabled(boolean enabled) {
        if (!enabled) {
            this.isBreakRequest = false;
            this.isBreakResponse = false;
            this.isBreakAll = false;
            this.setContinue(true);
        }
        breakRequestsButtonAction.setSelected(false);
        breakRequestsButtonAction.setEnabled(enabled);

        breakResponsesButtonAction.setSelected(false);
        breakResponsesButtonAction.setEnabled(enabled);

        breakAllButtonAction.setSelected(false);
        breakAllButtonAction.setEnabled(enabled);
    }

    private void setButtonsAndIconState(boolean enabled) {
        stepButtonAction.setEnabled(enabled);
        continueButtonAction.setEnabled(enabled);
        dropButtonAction.setEnabled(enabled);
        if (!enabled) {
            this.setActiveIcon(false);
        }
    }

    protected void setContinue(boolean isContinue) {
        this.cont = isContinue;
        setButtonsAndIconState(!isContinue);
    }

    protected void step() {
        step = true;
        Stats.incCounter(ExtensionBreak.BREAK_POINT_STEP_STATS);
    }

    protected void drop() {
        if (breakpointsParams.isConfirmDropMessage()
                && askForDropConfirmation() != JOptionPane.OK_OPTION) {
            return;
        }
        drop = true;
        Stats.incCounter(ExtensionBreak.BREAK_POINT_DROP_STATS);
    }

    public boolean isToBeDropped() {
        if (drop) {
            drop = false;
            return true;
        }
        return false;
    }

    public void init() {
        cont = false;
        step = false;
        stepping = false;
        drop = false;
        isBreakRequest = false;
        isBreakResponse = false;
        isBreakAll = false;
        setShowIgnoreFilesButtons(false);
        countCaughtMessages = 0;
    }

    public void reset() {
        if (isBreakRequest()) {
            toggleBreakRequest();
        }

        if (isBreakResponse()) {
            toggleBreakResponse();
        }

        if (isBreakAll()) {
            toggleBreakAll();
        }

        setContinue(true);
    }

    /**
     * Sets the current button mode.
     *
     * <p>If the mode is already set no change is done, otherwise it does the following:
     *
     * <ul>
     *   <li>When changing from {@link BreakpointsParam#BUTTON_MODE_SIMPLE BUTTON_MODE_SIMPLE} to
     *       {@link BreakpointsParam#BUTTON_MODE_DUAL BUTTON_MODE_DUAL} set "break on request" and
     *       "on response" enabled and "break on all" disabled, if "break on all" is enabled;
     *   <li>When changing from {@code BUTTON_MODE_DUAL} to {@code BUTTON_MODE_SIMPLE} set "break on
     *       all" enabled and "break on request" and "on response" disabled, if at least one of
     *       "break on request" and "on response" is enabled;
     *   <li>If none of the "break on ..." states is enabled there's no changes in its states.
     * </ul>
     *
     * The enabled state of previous mode is disabled to prevent interferences between the modes.
     *
     * @param mode the mode to be set
     * @see #isBreakAll()
     * @see #isBreakRequest()
     * @see #isBreakResponse()
     */
    public void setButtonMode(int mode) {
        if (this.mode == mode) {
            return;
        }
        if (this.mode == BreakpointsParam.BUTTON_MODE_SIMPLE) {
            if (isBreakAll) {
                setBreakAll(false);
                setBreakRequest(true);
                setBreakResponse(true);
            }
        } else if (isBreakRequest || isBreakResponse) {
            setBreakRequest(false);
            setBreakResponse(false);
            setBreakAll(true);
        }
        this.mode = mode;
    }

    public void setShowIgnoreFilesButtons(boolean showButtons) {
        if (!showButtons) {
            setBreakOnJavaScript(true);
            setBreakOnCssAndFonts(true);
            setBreakOnMultimedia(true);
            setOnlyBreakOnScope(breakpointsParams.isInScopeOnly());
        }
    }

    public void updateIgnoreFileTypesRegexs() {
        ignoreJavascriptBreakpointMessage.setString(this.breakpointsParams.getJavascriptUrlRegex());
        ignoreCssAndFontsBreakpointMessage.setString(
                this.breakpointsParams.getCssAndFontsUrlRegex());
        ignoreMultimediaBreakpointMessage.setString(this.breakpointsParams.getMultimediaUrlRegex());
    }

    private class ContinueButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public ContinueButtonAction() {
            super(null, getScaledIcon("16/131.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.cont"));

            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!breakPanel.isValidState()) {
                return;
            }

            setContinue(true);
            setBreakAll(false);
            setBreakRequest(false);
            setBreakResponse(false);
        }
    }

    private class StepButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public StepButtonAction() {
            super(null, getScaledIcon("16/143.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.step"));

            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!breakPanel.isValidState()) {
                return;
            }

            if (mode == BreakpointsParam.BUTTON_MODE_SIMPLE && !isBreakAll) {
                // In simple mode 'step' if the breakAll button is disabled then it acts like
                // 'continue'
                // so that its hopefully obvious to users when break is on or not
                setContinue(true);
            } else {
                step();
            }
        }
    }

    private class DropButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public DropButtonAction() {
            super(null, getScaledIcon("16/150.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.bin"));

            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            drop();
        }
    }

    private class AddBreakpointButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public AddBreakpointButtonAction() {
            super(null, getScaledIcon("16/break_add.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.brkpoint"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            breakPanel.showNewBreakPointDialog();
        }
    }

    private class BreakRequestsButtonAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public BreakRequestsButtonAction() {
            super(null, getScaledIcon("16/105.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.request.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakRequest();
        }
    }

    private class BreakResponsesButtonAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public BreakResponsesButtonAction() {
            super(null, getScaledIcon("16/106.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.response.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakResponse();
        }
    }

    private class BreakAllButtonAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public BreakAllButtonAction() {
            super(null, getScaledIcon("16/152.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.all.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakAll();
        }
    }

    private class SetBreakOnJavaScriptAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public SetBreakOnJavaScriptAction() {
            super(null, getScaledIcon("breakTypes/javascript.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.brkjavascript.unset"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakOnJavascript();
        }
    }

    private class SetBreakOnCssAndFontsAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public SetBreakOnCssAndFontsAction() {
            super(null, getScaledIcon("breakTypes/cssAndFonts.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.brkcssfonts.unset"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakOnCssAndFonts();
        }
    }

    private class SetBreakOnMultimediaAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public SetBreakOnMultimediaAction() {
            super(null, getScaledIcon("breakTypes/multimedia.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.brkmultimedia.unset"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakOnMultimedia();
        }
    }

    private class SetBreakOnlyOnScopeAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public SetBreakOnlyOnScopeAction() {
            super(null, getScaledIcon("fugue/target-grey.png"));

            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("brk.toolbar.button.brkOnlyOnScope.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakOnScopeOnly();
        }
    }

    /**
     * An {@code AbstractAction} which allows to be selected.
     *
     * @see AbstractAction
     * @see #setSelected(boolean)
     */
    private abstract static class SelectableAbstractAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a {@code SelectableAbstractAction} with the specified {@code name} and {@code
         * icon}.
         *
         * @param name the name for the action or {@code null} for no name
         * @param icon the icon for the action or {@code null} for no icon
         */
        public SelectableAbstractAction(String name, Icon icon) {
            super(name, icon);
        }

        /**
         * Sets whether the action is selected or not.
         *
         * @param selected {@code true} if the action should be selected, {@code false} otherwise
         */
        public void setSelected(boolean selected) {
            putValue(Action.SELECTED_KEY, selected);
        }
    }
}
