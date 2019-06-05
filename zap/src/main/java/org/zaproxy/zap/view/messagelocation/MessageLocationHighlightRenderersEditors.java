/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.view.messagelocation;

import java.util.HashMap;
import java.util.Map;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * A container of editors and renderers of {@code MessageLocationHighlight}s.
 *
 * @since 2.4.0
 * @see MessageLocationHighlight
 * @see TableCellEditor
 * @see TableCellRenderer
 */
public final class MessageLocationHighlightRenderersEditors {

    private static MessageLocationHighlightRenderersEditors instance = null;

    private Map<Class<? extends MessageLocationHighlight>, TableCellEditor> editors;
    private Map<Class<? extends MessageLocationHighlight>, TableCellRenderer> renderers;

    private MessageLocationHighlightRenderersEditors() {
        editors = new HashMap<>();
        renderers = new HashMap<>();
    }

    public static MessageLocationHighlightRenderersEditors getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new MessageLocationHighlightRenderersEditors();
        }
    }

    public void addEditor(
            Class<? extends MessageLocationHighlight> classHighlight, TableCellEditor editor) {
        editors.put(classHighlight, editor);
    }

    public TableCellEditor getEditor(Class<? extends MessageLocationHighlight> classHighlight) {
        return editors.get(classHighlight);
    }

    public void removeEditor(Class<? extends MessageLocationHighlight> classHighlight) {
        editors.remove(classHighlight);
    }

    public void addRenderer(
            Class<? extends MessageLocationHighlight> classHighlight, TableCellRenderer renderer) {
        renderers.put(classHighlight, renderer);
    }

    public TableCellRenderer getRenderer(Class<? extends MessageLocationHighlight> classHighlight) {
        return renderers.get(classHighlight);
    }

    public void removeRenderer(Class<? extends MessageLocationHighlight> classHighlight) {
        renderers.remove(classHighlight);
    }
}
