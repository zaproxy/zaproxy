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
// ZAP: 2011/08/04 Changed to support new HttpPanel interface
// ZAP: 2012/04/26 Removed the method setStatus(String), no longer used.
// ZAP: 2012/07/23 Added method getSessionDialog() to expose functionality.
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/05/02 Removed redundant public modifiers from interface method declarations
// ZAP: 2016/03/22 Allow to remove ContextPanelFactory
// ZAP: 2016/04/14 Allow to display a message
// ZAP: 2017/10/20 Allow to obtain default delete keyboard shortcut (Issue 3626).
// ZAP: 2018/07/17 Allow to obtain a KeyStroke with menu shortcut key mask.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2022/05/29 Allow to obtain the OptionsDialog.
// ZAP: 2022/11/07 Add convenience methods for help and options buttons.
package org.parosproxy.paros.extension;

import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.MainFrame;
import org.parosproxy.paros.view.MainPopupMenu;
import org.parosproxy.paros.view.OptionsDialog;
import org.parosproxy.paros.view.OutputPanel;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.SiteMapPanel;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.ContextPanelFactory;

public interface ViewDelegate {

    MainFrame getMainFrame();

    SiteMapPanel getSiteTreePanel();

    OutputPanel getOutputPanel();

    /**
     * Gets the Options dialogue.
     *
     * @return the Options dialogue.
     * @since 2.12.0
     */
    OptionsDialog getOptionsDialog();

    // ZAP: expose dialog
    SessionDialog getSessionDialog();

    int showConfirmDialog(String msg);

    int showYesNoCancelDialog(String msg);

    void showWarningDialog(String msg);

    void showMessageDialog(String msg);

    WaitMessageDialog getWaitMessageDialog(String msg);

    MainPopupMenu getPopupMenu();

    // ZAP: Removed the method setStatus(String), no longer used.

    HttpPanelRequest getRequestPanel();

    HttpPanelResponse getResponsePanel();

    /**
     * Adds the given context panel factory to the view delegate.
     *
     * <p>The factory will be called whenever a panel is required for a context and notified when a
     * context (or contexts) are no longer needed.
     *
     * @param contextPanelFactory the context panel factory that should be added
     * @throws IllegalArgumentException if the context panel factory is {@code null}.
     * @see #removeContextPanelFactory(ContextPanelFactory)
     */
    void addContextPanelFactory(ContextPanelFactory contextPanelFactory);

    /**
     * Removes the given context panel factory from the view delegate, and any previously created
     * panels for the contexts.
     *
     * @param contextPanelFactory the context panel factory that should be removed
     * @throws IllegalArgumentException if the context panel factory is {@code null}.
     * @since 2.5.0
     * @see #addContextPanelFactory(ContextPanelFactory)
     */
    void removeContextPanelFactory(ContextPanelFactory contextPanelFactory);

    /**
     * Displays the given {@code message} in the main message panels (Request/Response).
     *
     * <p>If the given {@code message} is {@code null} the panels are cleared.
     *
     * @param message the message to display
     * @since 2.5.0
     * @see #getRequestPanel()
     * @see #getResponsePanel()
     */
    void displayMessage(Message message);

    /**
     * Gets the default {@link KeyStroke} used to delete items (e.g. {@code HistoryReference},
     * {@code Alert}) show in the view (e.g. History tab, Alerts tree).
     *
     * @return the {@code KeyStroke} to delete items.
     * @since 2.7.0
     */
    KeyStroke getDefaultDeleteKeyStroke();

    /**
     * Convenience method that returns a key stroke with the menu shortcut key mask already applied
     * along with the given values.
     *
     * @param keyCode the keyboard key.
     * @param modifiers the key modifiers.
     * @param onKeyRelease {@code true} if on key release, {@code false} otherwise.
     * @return the KeyStroke.
     * @since 2.8.0
     * @see KeyStroke#getKeyStroke(int, int, boolean)
     */
    @SuppressWarnings("deprecation")
    default KeyStroke getMenuShortcutKeyStroke(int keyCode, int modifiers, boolean onKeyRelease) {
        // XXX Use getMenuShortcutKeyMaskEx() (and remove warn suppression) when targeting Java 10+
        return KeyStroke.getKeyStroke(
                keyCode,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifiers,
                onKeyRelease);
    }

    /**
     * Gets an options button, accepting a tooltip and options panel name (to be displayed when
     * opening the options dialog).
     *
     * @param tooltip the text to be displayed in the button's tooltip.
     * @param optionsPanelName the name of the panel to be displayed when the Options dialog is
     *     opened.
     * @return the options button, never {@code null}.
     * @since 2.13.0
     */
    default JButton getOptionsButton(String tooltip, String optionsPanelName) {
        JButton optionsButton = new JButton();
        optionsButton.setToolTipText(tooltip);
        optionsButton.setIcon(
                DisplayUtils.getScaledIcon(
                        new ImageIcon(
                                ViewDelegate.class.getResource("/resource/icon/16/041.png"))));

        optionsButton.addActionListener(
                e -> Control.getSingleton().getMenuToolsControl().options(optionsPanelName));
        return optionsButton;
    }

    /**
     * Gets a help button, accepting a help reference to set the initially displayed content.
     *
     * @param focus the help reference indicating the content to display when help is opened.
     * @return the help button, never {@code null}.
     * @since 2.13.0
     */
    default JButton getHelpButton(String focus) {
        JButton helpButton = new JButton();
        helpButton.setIcon(ExtensionHelp.getHelpIcon());
        helpButton.setToolTipText(Constant.messages.getString("help.dialog.button.tooltip"));
        helpButton.addActionListener(e -> ExtensionHelp.showHelp(focus));
        return helpButton;
    }
}
