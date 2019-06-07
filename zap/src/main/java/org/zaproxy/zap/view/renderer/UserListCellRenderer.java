/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view.renderer;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.users.User;

/**
 * A renderer for properly displaying the name of an User in a ComboBox. If the user is disabled,
 * the list item is disabled and marked as such.
 */
public class UserListCellRenderer extends BasicComboBoxRenderer {
    private static final long serialVersionUID = 3654541772447187317L;
    private static final String DISABLED_STRING =
            " (" + Constant.messages.getString("generic.value.disabled") + ')';

    private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);

    @Override
    @SuppressWarnings("rawtypes")
    public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value != null) {
            User user = (User) value;
            if (!user.isEnabled()) setText(user.getName() + DISABLED_STRING);
            else setText(user.getName());

            setEnabled(user.isEnabled());
        }
        setBorder(BORDER);

        return this;
    }
}
