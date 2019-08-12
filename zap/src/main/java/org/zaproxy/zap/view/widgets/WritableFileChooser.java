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
package org.zaproxy.zap.view.widgets;

import java.io.File;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;

public class WritableFileChooser extends JFileChooser {

    private static final long serialVersionUID = -8600149638325315049L;

    private static final int MINIMUM_SPACE_REQUIREMENT_MB = 5000000;

    public WritableFileChooser() {
        super();
    }

    public WritableFileChooser(File currentDirectory) {
        super(currentDirectory);
    }

    @Override
    public void approveSelection() {
        File selectedFile = getSelectedFile();

        File checkFile = selectedFile;
        boolean fileExists = checkFile.exists();
        if (!fileExists) {
            checkFile = checkFile.getParentFile();
        }
        if (checkFile.getUsableSpace() < MINIMUM_SPACE_REQUIREMENT_MB) {
            int result =
                    JOptionPane.showConfirmDialog(
                            this,
                            Constant.messages.getString(
                                    "writable.file.chooser.write.diskspace.warning.dialog.message"),
                            Constant.messages.getString(
                                    "writable.file.chooser.write.diskspace.warning.dialog.title"),
                            JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (!Files.isWritable(selectedFile.getParentFile().toPath())) {
            warnNotWritable(
                    "writable.file.chooser.write.permission.dir.dialog.message",
                    selectedFile.getParentFile().getAbsolutePath());

            return;
        }
        if (fileExists) {
            if (!Files.isWritable(selectedFile.toPath())) {
                warnNotWritable(
                        "writable.file.chooser.write.permission.file.dialog.message",
                        selectedFile.getAbsolutePath());
                return;
            }

            int result =
                    JOptionPane.showConfirmDialog(
                            this,
                            Constant.messages.getString(
                                    "writable.file.chooser.write.overwrite.dialog.message"),
                            Constant.messages.getString(
                                    "writable.file.chooser.write.overwrite.dialog.title"),
                            JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        // Store the user directory as the currently selected one
        Model.getSingleton().getOptionsParam().setUserDirectory(getCurrentDirectory());
        super.approveSelection();
    }

    /**
     * Convenience method that shows an error dialogue with the given message and title.
     *
     * <p>The {@code parent} of the error dialogue is this file chooser.
     *
     * @param message the error message.
     * @param title the title of the dialogue.
     * @since 2.8.0
     */
    protected void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void warnNotWritable(String i18nKeyMessage, String path) {
        showErrorDialog(
                Constant.messages.getString(i18nKeyMessage, path),
                Constant.messages.getString("writable.file.chooser.write.permission.dialog.title"));
    }
}
