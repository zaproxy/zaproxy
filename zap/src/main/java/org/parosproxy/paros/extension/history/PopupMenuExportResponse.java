/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2005 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/01/12 Reflected the rename of the class ExtensionPopupMenu to
// ExtensionPopupMenuItem.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/03/23 Changed to a JMenuItem.
// ZAP: 2016/07/25 Remove String constructor (unused/unnecessary)
// ZAP: 2018/08/15 Added null check
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/11/05 Use WritableFileChooser for saves.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/04/08 Set/name logger (LOG) fullcaps as constant, use LF as EOL for text file content
// ZAP: 2022/02/08 Use isEmpty where applicable.
// ZAP: 2022/05/13 Deprecated for relocation to exim.
// ZAP: 2022/08/05 Address warns with Java 18 (Issue 7389).
package org.parosproxy.paros.extension.history;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

/** @deprecated (2.12.0) see the exim add-on */
@Deprecated
@SuppressWarnings("serial")
public class PopupMenuExportResponse extends JMenuItem {

    private static final long serialVersionUID = 1L;
    private static final String NEWLINE = "\n";

    // ZAP: Added logger
    private static final Logger LOG = LogManager.getLogger(PopupMenuExportResponse.class);

    private ExtensionHistory extension = null;

    public PopupMenuExportResponse() {
        super(Constant.messages.getString("history.export.response.popup")); // ZAP: i18n

        this.addActionListener(
                new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {

                        List<HistoryReference> hrefs = extension.getSelectedHistoryReferences();
                        if (hrefs.isEmpty()) {
                            extension
                                    .getView()
                                    .showWarningDialog(
                                            Constant.messages.getString(
                                                    "history.export.response.select.warning")); // ZAP: i18n
                            return;
                        }

                        File file = getOutputFile();
                        if (file == null) {
                            return;
                        }

                        boolean isAppend = true;
                        if (file.exists()) {
                            int rc =
                                    extension
                                            .getView()
                                            .showYesNoCancelDialog(
                                                    Constant.messages.getString(
                                                            "file.overwrite.warning"));
                            if (rc == JOptionPane.CANCEL_OPTION) {
                                return;
                            } else if (rc == JOptionPane.YES_OPTION) {
                                isAppend = false;
                            }
                        }

                        try (BufferedOutputStream bos =
                                new BufferedOutputStream(new FileOutputStream(file, isAppend)); ) {

                            for (HistoryReference href : hrefs) {
                                HttpMessage msg = null;
                                msg = href.getHttpMessage();
                                exportHistory(msg, bos);
                            }

                        } catch (Exception e1) {
                            extension
                                    .getView()
                                    .showWarningDialog(
                                            Constant.messages.getString("file.save.error")
                                                    + file.getAbsolutePath()
                                                    + ".");
                            // ZAP: Log exceptions
                            LOG.warn(e1.getMessage(), e1);
                        }
                    }
                });
    }

    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }

    private void exportHistory(HttpMessage msg, BufferedOutputStream bos) {

        try {
            if (!msg.getResponseHeader().isEmpty()) {
                String boundary =
                        NEWLINE
                                + "==== "
                                + msg.getHistoryRef().getHistoryId()
                                + " =========="
                                + NEWLINE;
                bos.write(boundary.getBytes());

                bos.write(msg.getResponseBody().getBytes());
            }

        } catch (Exception e) {
            // ZAP: Log exceptions
            LOG.warn(e.getMessage(), e);
        }
    }

    private File getOutputFile() {

        String filename = "untitled.txt";

        JFileChooser chooser =
                new WritableFileChooser(extension.getModel().getOptionsParam().getUserDirectory());
        if (filename.length() > 0) {
            chooser.setSelectedFile(new File(filename));
        }

        File file = null;
        int rc = chooser.showSaveDialog(extension.getView().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                return file;
            }

            return file;
        }
        return file;
    }
}
