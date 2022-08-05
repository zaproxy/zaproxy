/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.history;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

/** @deprecated (2.12.0) see the exim add-on */
@Deprecated
@SuppressWarnings("serial")
public class PopupMenuExportURLs extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;
    protected final Extension extension;

    private static Logger log = LogManager.getLogger(PopupMenuExportURLs.class);

    /**
     * Constructs a {@code PopupMenuExportURLs} with the given label and extension.
     *
     * @param label the label of the menu item
     * @param extension the extension to access the model and view, must not be {@code null}.
     * @throws IllegalArgumentException if the given {@code extension} is {@code null}.
     */
    public PopupMenuExportURLs(String label, Extension extension) {
        super(label);

        if (extension == null) {
            throw new IllegalArgumentException("Parameter extension must not be null.");
        }
        this.extension = extension;

        this.addActionListener(
                new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        performAction();
                    }
                });
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if ("treeSite".equals(invoker.getName())) {
            JTree sitesTree = (JTree) invoker;
            setEnabled(sitesTree.getRowCount() > 1);
            return true;
        }
        return false;
    }

    protected void performAction() {
        File file = getOutputFile();
        if (file == null) {
            return;
        }
        writeURLs(
                file,
                getOutputSet(
                        (SiteNode)
                                extension
                                        .getView()
                                        .getSiteTreePanel()
                                        .getTreeSite()
                                        .getModel()
                                        .getRoot()));
    }

    protected SortedSet<String> getOutputSet(SiteNode startingPoint) {
        SortedSet<String> outputSet = new TreeSet<>();
        Enumeration<?> en = (startingPoint.preorderEnumeration());
        while (en.hasMoreElements()) {
            SiteNode node = (SiteNode) en.nextElement();
            if (node.isRoot()) {
                continue;
            }
            outputSet.add(node.getHistoryReference().getURI().toString());
        }
        return outputSet;
    }

    protected void writeURLs(File file, SortedSet<String> aSet) {

        boolean html =
                file.getName().toLowerCase().endsWith(".htm")
                        || file.getName().toLowerCase().endsWith(".html");

        BufferedWriter fw = null;
        try {
            fw = new BufferedWriter(new FileWriter(file, false));

            for (String item : aSet) {
                item = html ? wrapHTML(item) : item;
                fw.write(item);
                fw.newLine();
            }

        } catch (Exception e1) {
            log.warn("An error occurred while writing the URLs:", e1);
            extension
                    .getView()
                    .showWarningDialog(
                            Constant.messages.getString("file.save.error")
                                    + file.getAbsolutePath());
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception e2) {
                log.warn(e2.getStackTrace(), e2);
            }
        }
    }

    private String wrapHTML(String input) {
        StringBuilder sb = new StringBuilder(50);
        sb.append("<a href=\"").append(input).append("\">");
        sb.append(input).append("</a><br>");

        return sb.toString();
    }

    protected File getOutputFile() {
        FileNameExtensionFilter textFilesFilter =
                new FileNameExtensionFilter(
                        Constant.messages.getString("file.format.ascii"), "txt");
        FileNameExtensionFilter htmlFilesFilter =
                new FileNameExtensionFilter(
                        Constant.messages.getString("file.format.html"), "html", "htm");
        WritableFileChooser chooser =
                new WritableFileChooser(extension.getModel().getOptionsParam().getUserDirectory()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void approveSelection() {
                        File file = getSelectedFile();
                        if (file != null) {
                            String ext = null;
                            String filePath = file.getAbsolutePath();
                            String fileNameLc = filePath.toLowerCase(Locale.ROOT);
                            if (htmlFilesFilter.equals(getFileFilter())) {
                                if (!fileNameLc.endsWith(".htm") && !fileNameLc.endsWith(".html")) {
                                    ext = ".html";
                                }
                            } else if (!fileNameLc.endsWith(".txt")) {
                                ext = ".txt";
                            }

                            if (ext != null) {
                                setSelectedFile(new File(filePath + ext));
                            }
                        }

                        super.approveSelection();
                    }
                };

        chooser.addChoosableFileFilter(textFilesFilter);
        chooser.addChoosableFileFilter(htmlFilesFilter);
        chooser.setFileFilter(textFilesFilter);

        int rc = chooser.showSaveDialog(extension.getView().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}
