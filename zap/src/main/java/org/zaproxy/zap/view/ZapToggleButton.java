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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * A {@code JToggleButton} that allows to set tool tip texts that are shown only when the toggle button is selected, disabled
 * and when both disabled and selected.
 * 
 * @see JToggleButton
 * @see #setSelectedToolTipText(String)
 * @see #setDisabledToolTipText(String)
 * @see #setDisabledSelectedToolTipText(String)
 */
public class ZapToggleButton extends JToggleButton {

    private static final long serialVersionUID = 1L;

    /**
     * If non-{@code null} the tool tip text shown when the toggle button is in normal state or in any other state if there's no
     * custom tool tip text set (either {@code selectedToolTipText}, {@code disabledToolTipText} and/or
     * {@code disabledSelectedToolTipText}).
     * 
     * @see #selectedToolTipText
     * @see #disabledToolTipText
     * @see #disabledSelectedToolTipText
     * @see #setToolTipText(String)
     */
    private String defaultToolTipText;

    /**
     * If non-{@code null} the tool tip text shown when the toggle button is selected (but not disabled).
     * 
     * @see #defaultToolTipText
     * @see #disabledToolTipText
     * @see #disabledSelectedToolTipText
     * @see #toolTipTextUpdaterOnSelectionStateChange
     * @see #setSelectedToolTipText(String)
     */
    private String selectedToolTipText;

    /**
     * If non-{@code null} the tool tip text shown when the toggle button is disabled.
     * 
     * @see #defaultToolTipText
     * @see #disabledToolTipText
     * @see #disabledSelectedToolTipText
     * @see #setDisabledToolTipText(String)
     */
    private String disabledToolTipText;

    /**
     * If non-{@code null} the tool tip text shown when the toggle button is disabled and selected.
     * 
     * @see #defaultToolTipText
     * @see #selectedToolTipText
     * @see #disabledToolTipText
     * @see #toolTipTextUpdaterOnSelectionStateChange
     * @see #setDisabledSelectedToolTipText(String)
     */
    private String disabledSelectedToolTipText;

    /**
     * The {@code ItemListener} used to update the tool tip text when the selection state of the toggle button changes.
     * <p>
     * Might be {@code null} if there's no tool tip text set for selected state, either {@code selectedToolTipText} or
     * {@code disabledSelectedToolTipText}.
     * </p>
     * 
     * @see #selectedToolTipText
     * @see #disabledSelectedToolTipText
     * @see #addRemoveToolTipTextUpdaterOnSelectionStateChangeAsNeeded()
     */
    private ToolTipTextUpdaterOnSelectionStateChange toolTipTextUpdaterOnSelectionStateChange;

    // NOTE: All ZapToggleButton constructors JavaDoc has been copied from the corresponding JToggleButton constructor.

    /**
     * Creates an initially unselected toggle button without setting the text or image.
     */
    public ZapToggleButton() {
        super(null, null, false);
    }

    /**
     * Creates an initially unselected toggle button with the specified image but no text.
     * 
     * @param icon the image that the button should display
     */
    public ZapToggleButton(Icon icon) {
        super(null, icon, false);
    }

    /**
     * Creates a toggle button with the specified image and selection state, but no text.
     * 
     * @param icon the image that the button should display
     * @param selected if true, the button is initially selected; otherwise, the button is initially unselected
     */
    public ZapToggleButton(Icon icon, boolean selected) {
        super(null, icon, selected);
    }

    /**
     * Creates an unselected toggle button with the specified text.
     * 
     * @param text the string displayed on the toggle button
     */
    public ZapToggleButton(String text) {
        super(text, null, false);
    }

    /**
     * Creates a toggle button with the specified text and selection state.
     * 
     * @param text the string displayed on the toggle button
     * @param selected if true, the button is initially selected; otherwise, the button is initially unselected
     */
    public ZapToggleButton(String text, boolean selected) {
        super(text, null, selected);
    }

    /**
     * Creates a toggle button where properties are taken from the Action supplied.
     * 
     * @param action the action with the properties
     */
    public ZapToggleButton(Action action) {
        super(action);
    }

    /**
     * Creates a toggle button that has the specified text and image, and that is initially unselected.
     * 
     * @param text the string displayed on the button
     * @param icon the image that the button should display
     */
    public ZapToggleButton(String text, Icon icon) {
        super(text, icon, false);
    }

    /**
     * Creates a toggle button with the specified text, image, and selection state.
     * 
     * @param text the text of the toggle button
     * @param icon the image that the button should display
     * @param selected if true, the button is initially selected; otherwise, the button is initially unselected
     */
    public ZapToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        updateCurrentToolTipText();
    }

    /**
     * Updates the current tool tip text based on the toggle button's state.
     * <table>
     * <caption>The tool tip that will be set based on button's state</caption>
     * <tr>
     * <td>State</td>
     * <td>Tool tip text set</td>
     * </tr>
     * <tr>
     * <td>Disabled and selected</td>
     * <td>{@code disabledSelectedToolTipText} if non-{@code null} otherwise {@code defaultToolTipText}</td>
     * </tr>
     * <tr>
     * <td>Disabled</td>
     * <td>{@code disabledToolTipText} if non-{@code null} otherwise {@code defaultToolTipText}</td>
     * </tr>
     * <tr>
     * <td>Selected (but not disabled)</td>
     * <td>{@code selectedToolTipText} if non-{@code null} otherwise {@code defaultToolTipText}</td>
     * </tr>
     * <tr>
     * <td>Other states</td>
     * <td>{@code defaultToolTipText}</td>
     * </tr>
     * </table>
     * 
     * @see #defaultToolTipText
     * @see #selectedToolTipText
     * @see #disabledToolTipText
     * @see #disabledSelectedToolTipText
     */
    private void updateCurrentToolTipText() {
        String toolTipText;
        final boolean disabled = !isEnabled();
        final boolean selected = isSelected();

        if (disabled && selected && disabledSelectedToolTipText != null) {
            toolTipText = disabledSelectedToolTipText;
        } else if (disabled && disabledToolTipText != null) {
            toolTipText = disabledToolTipText;
        } else if (selected && selectedToolTipText != null) {
            toolTipText = selectedToolTipText;
        } else {
            toolTipText = defaultToolTipText;
        }

        super.setToolTipText(toolTipText);
    }

    /**
     * Sets the default tool tip text, shown when the toggle button is in normal state or in any other state if there's no
     * custom tool tip text set. If the given {@code text} is {@code null} no tool tip text is shown in normal state.
     * 
     * @param text the tool tip text that will be shown when the toggle button is in normal state or {@code null} if no tool tip
     *            text should be shown in normal state
     * @see #getToolTipText()
     * @see #setSelectedToolTipText(String)
     * @see #setDisabledToolTipText(String)
     * @see #setDisabledSelectedToolTipText(String)
     */
    @Override
    public void setToolTipText(String text) {
        defaultToolTipText = text;
        updateCurrentToolTipText();
    }

    /**
     * Returns the current tool tip text set, might be {@code null}.
     * <p>
     * <strong>Note:</strong> This is not the getter method for the default tool tip text, set using the method
     * {@code setToolTipText(String)} use {@code getDefaultToolTipText()} instead.
     * </p>
     * 
     * @see #setToolTipText(String)
     * @see #getDefaultToolTipText()
     */
    @Override
    public String getToolTipText() {
        return super.getToolTipText();
    }

    /**
     * Returns the tool tip text that's shown when the toggle button is in normal state or in any other state if there's no
     * custom tool tip text set. Might be {@code null} if no tool tip text was set.
     * 
     * @return the "default" tool tip text or {@code null} if not set
     * @see #setToolTipText(String)
     */
    public String getDefaultToolTipText() {
        return defaultToolTipText;
    }

    /**
     * Sets the tool tip text that will be shown when the toggle button is selected (but not disabled). If the given
     * {@code text} is {@code null} the default tool tip text is shown instead, if set.
     * 
     * @param text the tool tip text that will be shown when the toggle button is selected or {@code null} to show the default
     *            tool tip text, if set
     * @see #getSelectedToolTipText()
     * @see #setToolTipText(String)
     * @see #setDisabledToolTipText(String)
     * @see #setDisabledSelectedToolTipText(String)
     */
    public void setSelectedToolTipText(String text) {
        selectedToolTipText = text;
        addRemoveToolTipTextUpdaterOnSelectionStateChangeAsNeeded();
        updateCurrentToolTipText();
    }

    /**
     * Helper method that takes care to instantiate and add (as an ItemListener) the instance variable
     * {@code toolTipTextUpdaterOnSelectionStateChange} when a tool tip text that depends on the selection state (either
     * selectedToolTipText or disabledSelectedToolTipText) is not {@code null} or set to null and remove (as an ItemListener) if
     * it is.
     * 
     * @see #toolTipTextUpdaterOnSelectionStateChange
     * @see #selectedToolTipText
     * @see #disabledSelectedToolTipText
     * @see #addItemListener(ItemListener)
     * @see #removeItemListener(ItemListener)
     */
    private void addRemoveToolTipTextUpdaterOnSelectionStateChangeAsNeeded() {
        if (selectedToolTipText == null && disabledSelectedToolTipText == null) {
            if (toolTipTextUpdaterOnSelectionStateChange != null) {
                removeItemListener(toolTipTextUpdaterOnSelectionStateChange);
                toolTipTextUpdaterOnSelectionStateChange = null;
            }
        } else if (toolTipTextUpdaterOnSelectionStateChange == null) {
            toolTipTextUpdaterOnSelectionStateChange = new ToolTipTextUpdaterOnSelectionStateChange();
            addItemListener(toolTipTextUpdaterOnSelectionStateChange);
        }
    }

    /**
     * Returns the tool tip text that's shown, if non-{@code null}, when the toggle button is selected.
     * 
     * @return the "selected" tool tip text or {@code null} if not set
     * @see #setDisabledSelectedToolTipText(String)
     */
    public String getSelectedToolTipText() {
        return selectedToolTipText;
    }

    /**
     * Sets the tool tip text that will be shown when the toggle button is disabled. If the given {@code text} is {@code null}
     * the default tool tip text is shown instead, if set.
     * 
     * @param text the tool tip text that will be shown when the toggle button is disabled or {@code null} to show the default
     *            tool tip text, if set
     * @see #getDisabledToolTipText()
     * @see #setToolTipText(String)
     * @see #setSelectedToolTipText(String)
     * @see #setDisabledSelectedToolTipText(String)
     */
    public void setDisabledToolTipText(String text) {
        disabledToolTipText = text;
        updateCurrentToolTipText();
    }

    /**
     * Returns the tool tip text that's shown, if non-{@code null}, when the toggle button is disabled.
     * 
     * @return the "disabled" tool tip text or {@code null} if not set
     * @see #setDisabledToolTipText(String)
     */
    public String getDisabledToolTipText() {
        return disabledToolTipText;
    }

    /**
     * Sets the tool tip text that will be shown when the toggle button is disabled and selected. If the given {@code text} is
     * {@code null} the default tool tip text is shown instead, if set.
     * 
     * @param text the tool tip text that will be shown when the toggle button is disabled and selected or {@code null} to show
     *            the default tool tip text, if set
     * @see #getDisabledSelectedToolTipText()
     * @see #setToolTipText(String)
     * @see #setSelectedToolTipText(String)
     * @see #setDisabledToolTipText(String)
     */
    public void setDisabledSelectedToolTipText(String text) {
        disabledSelectedToolTipText = text;
        addRemoveToolTipTextUpdaterOnSelectionStateChangeAsNeeded();
        updateCurrentToolTipText();
    }

    /**
     * Returns the tool tip text that's shown, if non-{@code null}, when the toggle button is disabled and selected.
     * 
     * @return the "disabled selected" tool tip text or {@code null} if not set
     * @see #setDisabledSelectedToolTipText(String)
     */
    public String getDisabledSelectedToolTipText() {
        return disabledSelectedToolTipText;
    }

    /**
     * An {@code ItemListener} that updates the tool tip text of a {@code ZapToggleButton} instance on selection state changes
     * by calling the method {@code ZapToggleButton#updateCurrentToolTipText()}.
     * <p>
     * <strong>Note:</strong> It's used an {@code ItemListener} instead of overriding the method
     * {@code AbstractButton#setSelected(boolean)} because the selection state of the toggle button can be changed using its
     * {@code ButtonModel} in which case the method {@code AbstractButton#setSelected(boolean)} is not called, as opposed to the
     * enabled state which the method {@code AbstractButton#setEnabled(boolean)} is called and overridden. For more details see
     * the implementation of {@code AbstractButton}'s inner class {@code Handler}.
     * </p>
     * 
     * @see ItemListener
     * @see ZapToggleButton#updateCurrentToolTipText()
     * @see javax.swing.AbstractButton#setSelected(boolean)
     * @see javax.swing.AbstractButton#getModel()
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    private class ToolTipTextUpdaterOnSelectionStateChange implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent evt) {
            if (ItemEvent.ITEM_STATE_CHANGED == evt.getID()) {
                updateCurrentToolTipText();
            }
        }
    }

}
