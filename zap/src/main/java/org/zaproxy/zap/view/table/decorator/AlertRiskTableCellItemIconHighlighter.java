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
package org.zaproxy.zap.view.table.decorator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.view.table.AlertRiskTableCellItem;

/**
 * An highlighter for {@code AlertRiskTableCellItem} showing an appropriate icon for the risk value.
 *
 * @see AlertRiskTableCellItem
 */
public class AlertRiskTableCellItemIconHighlighter extends AbstractTableCellItemIconHighlighter {

    private static final Icon[] ALERT_ICONS = {
        new ImageIcon(Constant.INFO_FLAG_IMAGE_URL),
        new ImageIcon(Constant.LOW_FLAG_IMAGE_URL),
        new ImageIcon(Constant.MED_FLAG_IMAGE_URL),
        new ImageIcon(Constant.HIGH_FLAG_IMAGE_URL)
    };

    private static final int ALERT_ICONS_LENGTH = ALERT_ICONS.length;

    public AlertRiskTableCellItemIconHighlighter(int columnIndex) {
        super(columnIndex);
    }

    @Override
    protected Icon getIcon(Object cellItem) {
        return getAlertIcon(((AlertRiskTableCellItem) cellItem).getRisk());
    }

    private static Icon getAlertIcon(int risk) {
        if (risk < 0 || risk >= ALERT_ICONS_LENGTH) {
            return null;
        }
        return ALERT_ICONS[risk];
    }

    @Override
    protected boolean isHighlighted(Object cellItem) {
        final int risk = ((AlertRiskTableCellItem) cellItem).getRisk();

        switch (risk) {
            case Alert.RISK_INFO:
            case Alert.RISK_LOW:
            case Alert.RISK_MEDIUM:
            case Alert.RISK_HIGH:
                return true;
            default:
                return false;
        }
    }
}
