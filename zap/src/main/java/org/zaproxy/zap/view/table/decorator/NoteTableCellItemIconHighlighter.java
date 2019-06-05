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

public class NoteTableCellItemIconHighlighter extends AbstractTableCellItemIconHighlighter {

    private static final Icon NOTE_ICON =
            new ImageIcon(
                    NoteTableCellItemIconHighlighter.class.getResource(
                            "/resource/icon/16/022.png"));

    public NoteTableCellItemIconHighlighter(final int columnIndex) {
        super(columnIndex);
    }

    @Override
    protected Icon getIcon(final Object cellItem) {
        return getAlertIcon((Boolean) cellItem);
    }

    private static Icon getAlertIcon(final boolean hasNote) {
        if (hasNote) {
            return NOTE_ICON;
        }
        return null;
    }

    @Override
    protected boolean isHighlighted(final Object cellItem) {
        return (Boolean) cellItem;
    }
}
