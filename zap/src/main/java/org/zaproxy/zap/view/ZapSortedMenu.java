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
package org.zaproxy.zap.view;

import java.awt.Component;
import java.text.Collator;
import java.util.Comparator;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/** <strong>Note:</strong> Not part of the public API. */
@SuppressWarnings("serial")
public class ZapSortedMenu extends JMenu {

    private static final long serialVersionUID = 1L;

    private final Comparator<JMenuItem> comparator;

    public ZapSortedMenu() {
        this((item1, item2) -> Collator.getInstance().compare(item1.getText(), item2.getText()));
    }

    public ZapSortedMenu(Comparator<JMenuItem> comparator) {
        this.comparator = comparator;
    }

    @Override
    public JMenuItem add(JMenuItem menuItem) {
        super.add(menuItem, getSortedIndex(menuItem));
        return menuItem;
    }

    @Override
    public Component add(Component c) {
        return super.add(c, getSortedIndex(c));
    }

    @Override
    public Component add(Component c, int index) {
        return add(c);
    }

    @Override
    public void insert(String s, int pos) {
        add(s);
    }

    @Override
    public JMenuItem insert(JMenuItem mi, int pos) {
        return add(mi);
    }

    @Override
    public JMenuItem insert(Action a, int pos) {
        return add(a);
    }

    private int getSortedIndex(Component comp) {
        if (!(comp instanceof JMenuItem)) {
            return -1;
        }
        int pos = 0;
        for (int i = 0; i < getItemCount(); ++i) {
            if (comparator.compare((JMenuItem) comp, getItem(i)) < 0) {
                break;
            }
            pos = i + 1;
        }
        return pos;
    }
}
