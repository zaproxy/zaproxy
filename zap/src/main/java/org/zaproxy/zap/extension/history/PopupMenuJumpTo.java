/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.zaproxy.zap.extension.history;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.ZapAction;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

@SuppressWarnings("serial")
public class PopupMenuJumpTo extends PopupMenuItemHistoryReferenceContainer {

    public static final String IDENTIFIER = "zap.history.jumpto";

    private static final Logger LOGGER = LogManager.getLogger(PopupMenuJumpTo.class);
    private static final List<Integer> DISPLAYED_HISTORY_TYPES =
            List.of(
                    HistoryReference.TYPE_PROXIED,
                    HistoryReference.TYPE_ZAP_USER,
                    HistoryReference.TYPE_PROXY_CONNECT);
    private static ZapNumberSpinner idSpinner = new ZapNumberSpinner(1, 1, Integer.MAX_VALUE);

    private final ExtensionHistory extension;
    private final JumpToAction jumpAction;

    public PopupMenuJumpTo(ExtensionHistory extension) {
        super(Constant.messages.getString("history.jumpto.popup.label"));

        this.extension = extension;
        this.jumpAction = new JumpToAction();
        updateAccelerator(jumpAction.getAccelerator());
    }

    public ZapAction getJumpAction() {
        return jumpAction;
    }

    public void updateAccelerator(KeyStroke keyStroke) {
        if (keyStroke != null) {
            setAccelerator(keyStroke);
        }
    }

    public static KeyStroke getDefaultAccelerator() {
        return KeyStroke.getKeyStroke(
                KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    }

    @Override
    protected boolean isButtonEnabledForHistoryReference(HistoryReference historyReference) {
        return DISPLAYED_HISTORY_TYPES.contains(historyReference.getHistoryType());
    }

    @Override
    public void performAction(HistoryReference href) {
        jump();
    }

    private void jump() {
        int option =
                JOptionPane.showOptionDialog(
                        null,
                        new Object[] {
                            Constant.messages.getString("history.jumpto.message"), idSpinner
                        },
                        Constant.messages.getString("history.jumpto.title"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null);
        if (option == JOptionPane.OK_OPTION) {
            int historyId = idSpinner.getValue();
            if (extension.getHistoryReferencesTable().getModel().getEntryRowIndex(historyId)
                    != -1) {
                extension.showInHistory(extension.getHistoryReference(historyId));
                return;
            }
            List<Integer> list = extension.getHistoryIds();
            if (list.isEmpty()) {
                return;
            }
            int nearestIdx = Math.abs(Collections.binarySearch(list, historyId)) - 1;
            if (nearestIdx >= list.size()) {
                nearestIdx = list.size() - 1;
            }
            HistoryReference nearestRef = extension.getHistoryReference(list.get(nearestIdx));
            extension.showInHistory(nearestRef);
            LOGGER.debug(
                    "Jumping to nearest ID: {}, after request for: {}",
                    nearestRef.getHistoryId(),
                    historyId);
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    private class JumpToAction extends ZapAction {

        JumpToAction() {
            super(
                    IDENTIFIER,
                    Constant.messages.getString("history.jumpto.popup.label"),
                    PopupMenuJumpTo.getDefaultAccelerator());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jump();
        }
    }
}
