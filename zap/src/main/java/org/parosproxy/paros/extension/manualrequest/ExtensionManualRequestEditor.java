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

// ZAP: 2011/08/04 Changed for cleanup
// ZAP: 2011/11/20 Set order
// ZAP: 2012/03/15 Changed to reset the message of the ManualRequestEditorDialog
// when a new session is created. Added the key configuration to the
// ManualRequestEditorDialog.
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/02 ManualRequestEditorDialog changed to receive Message instead
// of HttpMessage. Changed logger to static.
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/11/21 Heavily refactored extension to support non-HTTP messages.
// ZAP: 2013/01/25 Added method removeManualSendEditor().
// ZAP: 2013/02/06 Issue 499: NullPointerException while uninstalling an add-on
// with a manual request editor
// ZAP: 2014/03/23 Issue 1094: Change ExtensionManualRequestEditor to only add view components if in
// GUI mode
// ZAP: 2014/08/14 Issue 1292: NullPointerException while attempting to remove an unregistered
// ManualRequestEditorDialog
// ZAP: 2014/12/12 Issue 1449: Added help button
// ZAP: 2015/03/16 Issue 1525: Further database independence changes
// ZAP: 2016/06/20 Removed unnecessary/unused constructor
// ZAP: 2017/04/07 Added getUIName()
// ZAP: 2017/06/06 Clear dialogues in EDT.
// ZAP: 2018/02/23 Issue 1161: Fix Session Tracking button sync
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/10/04 Add dialog/menu icon.
// ZAP: 2022/06/13 Add HrefTypeInfo on hook.
// ZAP: 2022/09/14 Deprecate the class.
package org.parosproxy.paros.extension.manualrequest;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.ImageIcon;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.view.HrefTypeInfo;

/** @deprecated (2.12.0) Replaced by Requester add-on. */
@Deprecated
public class ExtensionManualRequestEditor extends ExtensionAdaptor
        implements SessionChangedListener {

    private Map<Class<? extends Message>, ManualRequestEditorDialog> dialogues = new HashMap<>();
    private org.parosproxy.paros.extension.manualrequest.http.impl.ManualHttpRequestEditorDialog
            httpSendEditorDialog;

    /** Name of this extension. */
    public static final String NAME = "ExtensionManualRequest";

    public ExtensionManualRequestEditor() {
        super(NAME);
        this.setOrder(36);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("manReq.name");
    }

    @Override
    public void initView(ViewDelegate view) {
        super.initView(view);

        // add default manual request editor
        httpSendEditorDialog =
                new org.parosproxy.paros.extension.manualrequest.http.impl
                        .ManualHttpRequestEditorDialog(true, "manual", "ui.dialogs.manreq");
        httpSendEditorDialog.setTitle(Constant.messages.getString("manReq.dialog.title"));

        addManualSendEditor(httpSendEditorDialog);
    }

    /**
     * Should be called before extension is initialized via its {@link #hook(ExtensionHook)} method.
     *
     * @param dialogue
     */
    public void addManualSendEditor(ManualRequestEditorDialog dialogue) {
        dialogues.put(dialogue.getMessageType(), dialogue);
    }

    public void removeManualSendEditor(Class<? extends Message> messageType) {
        // remove from list
        ManualRequestEditorDialog dialogue = dialogues.remove(messageType);

        if (dialogue != null) {
            // remove from GUI
            dialogue.clear();
            dialogue.dispose();

            if (getView() != null) {
                // unload menu items
                ExtensionLoader extLoader = Control.getSingleton().getExtensionLoader();
                extLoader.removeToolsMenuItem(dialogue.getMenuItem());
            }
        }
    }

    /**
     * Get special manual send editor to add listeners, etc.
     *
     * @param type
     * @return
     */
    public ManualRequestEditorDialog getManualSendEditor(Class<? extends Message> type) {
        return dialogues.get(type);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        extensionHook.addHrefType(
                new HrefTypeInfo(
                        HistoryReference.TYPE_ZAP_USER,
                        Constant.messages.getString("view.href.type.name.manual"),
                        hasView() ? ExtensionManualRequestEditor.getIcon() : null));

        if (getView() != null) {
            for (Entry<Class<? extends Message>, ManualRequestEditorDialog> dialogue :
                    dialogues.entrySet()) {
                extensionHook.getHookMenu().addToolsMenuItem(dialogue.getValue().getMenuItem());
            }

            extensionHook.addSessionListener(this);
            extensionHook.addOptionsChangedListener(httpSendEditorDialog);
        }
    }

    @Override
    public String getAuthor() {
        return Constant.PAROS_TEAM;
    }

    @Override
    public void sessionChanged(Session session) {
        if (EventQueue.isDispatchThread()) {
            for (Entry<Class<? extends Message>, ManualRequestEditorDialog> dialogue :
                    dialogues.entrySet()) {
                dialogue.getValue().clear();
                dialogue.getValue().setDefaultMessage();
            }
            return;
        }

        EventQueue.invokeLater(() -> sessionChanged(session));
    }

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public void sessionScopeChanged(Session session) {}

    @Override
    public void sessionModeChanged(Mode mode) {
        Boolean isEnabled = null;
        switch (mode) {
            case safe:
                isEnabled = false;
                break;
            case protect:
            case standard:
            case attack:
                isEnabled = true;
                break;
        }

        if (isEnabled != null) {
            for (Entry<Class<? extends Message>, ManualRequestEditorDialog> dialog :
                    dialogues.entrySet()) {
                dialog.getValue().setEnabled(isEnabled);
            }
        }
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    public static final ImageIcon getIcon() {
        return new ImageIcon(
                ExtensionManualRequestEditor.class.getResource("/resource/icon/16/hand.png"));
    }
}
