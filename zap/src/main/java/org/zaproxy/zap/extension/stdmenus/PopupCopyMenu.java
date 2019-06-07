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
package org.zaproxy.zap.extension.stdmenus;

import java.awt.Component;
import javax.swing.text.JTextComponent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

public class PopupCopyMenu extends ExtensionPopupMenuItem {
    private static final long serialVersionUID = 1L;
    private JTextComponent lastInvoker = null;

    /** @return Returns the lastInvoker. */
    public JTextComponent getLastInvoker() {
        return lastInvoker;
    }

    /** This method initializes */
    public PopupCopyMenu() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setText(Constant.messages.getString("copy.copy.popup"));
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent && !(invoker instanceof RSyntaxTextArea)) {
            this.setEnabled(((JTextComponent) invoker).getSelectedText() != null);

            this.lastInvoker = (JTextComponent) invoker;
            return true;
        }

        this.lastInvoker = null;
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
