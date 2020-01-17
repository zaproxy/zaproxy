/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.view.panelsearch.items;

import javax.swing.JComboBox;

public class ComboBoxElement {
    private Object item;
    private int itemIndex;
    private JComboBox<Object> comboBox;

    public ComboBoxElement(Object item, int itemIndex, JComboBox<Object> comboBox) {
        this.item = item;
        this.itemIndex = itemIndex;
        this.comboBox = comboBox;
    }

    public Object getItem() {
        return item;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public JComboBox<Object> getComboBox() {
        return comboBox;
    }
}
