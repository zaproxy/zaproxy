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
package org.zaproxy.zap.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;

public class ContextExportDialog extends StandardFieldsDialog {

    private static final long serialVersionUID = 1L;

    private static final String CONTEXT_FIELD = "context.export.label.context";
    private static final String DIR_FIELD = "context.export.label.dir";
    private static final String FILE_FIELD = "context.export.label.file";
    private static final String OVERWRITE_FIELD = "context.export.label.overwrite";

    private static final String CONTEXT_EXT = ".context";

    public ContextExportDialog(Frame owner) {
        super(owner, "context.export.title", new Dimension(400, 250));
        this.addContextSelectField(CONTEXT_FIELD, null);
        this.addFileSelectField(
                DIR_FIELD, Constant.getContextsDir(), JFileChooser.DIRECTORIES_ONLY, null);
        this.addTextField(FILE_FIELD, null);
        this.addCheckBoxField(OVERWRITE_FIELD, false);

        super.addFieldListener(
                CONTEXT_FIELD,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Context ctx = getContextValue(CONTEXT_FIELD);
                        if (ctx != null) {
                            String fileName = ctx.getName() + CONTEXT_EXT;
                            setFieldValue(FILE_FIELD, fileName);
                        }
                    }
                });
    }

    private File getSelectedFile() {
        if (this.isEmptyField(DIR_FIELD) || this.isEmptyField(FILE_FIELD)) {
            return null;
        }
        return new File(this.getStringValue(DIR_FIELD), this.getStringValue(FILE_FIELD));
    }

    @Override
    public void save() {
        try {
            Model.getSingleton()
                    .getSession()
                    .exportContext(getContextValue(CONTEXT_FIELD), getSelectedFile());
        } catch (ConfigurationException e) {
            View.getSingleton()
                    .showWarningDialog(
                            this,
                            Constant.messages.getString("context.export.error", e.getMessage()));
        }
    }

    @Override
    public String validateFields() {
        File f = this.getSelectedFile();
        if (this.getContextValue(CONTEXT_FIELD) == null) {
            return Constant.messages.getString("context.export.error.nocontext");
        }
        if (f == null) {
            return Constant.messages.getString("context.export.error.nofile");
        } else if (f.exists() & !this.getBoolValue(OVERWRITE_FIELD)) {
            return Constant.messages.getString("context.export.error.exists");
        } else if (!f.getParentFile().canWrite()) {
            return Constant.messages.getString("context.export.error.noaccess");
        }
        return null;
    }

    /**
     * Sets the selected context.
     *
     * <p>The call to this method has no effect if the context does not exist in the session.
     *
     * @param context the context to be selected, {@code null} to clear the selection.
     * @since 2.6.0
     */
    public void setSelectedContext(Context context) {
        setContextValue(CONTEXT_FIELD, context);
    }
}
