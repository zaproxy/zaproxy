/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import org.parosproxy.paros.core.scanner.Alert;

public class TextAlertTree {

    private TextAlertTree() {}

    /**
     * Returns a YAML representation of the Alert tree. Only for use in testing / debugging!
     *
     * @param model The Alert tree model
     * @return a YAML representation of the Alert tree
     * @since 2.17.0
     */
    public static String toString(AlertTreeModel model) {
        StringBuilder sb = new StringBuilder();

        dumpRoot(model.getRoot(), sb);

        return sb.toString();
    }

    private static void dumpRoot(AlertNode root, StringBuilder sb) {
        sb.append("- Alerts\n");
        root.children().asIterator().forEachRemaining(child -> dumpAlert((AlertNode) child, sb));
    }

    private static void dumpAlert(AlertNode node, StringBuilder sb) {
        sb.append("  - ");
        sb.append(Alert.MSG_RISK[node.getRisk()]);
        sb.append(": ");
        sb.append(node.getNodeName());
        sb.append("\n");
        node.children()
                .asIterator()
                .forEachRemaining(child -> dumpAlertInstance((AlertNode) child, sb));
    }

    private static void dumpAlertInstance(AlertNode node, StringBuilder sb) {
        sb.append("    - ");
        sb.append(node.getNodeName());
        sb.append("\n");
    }
}
