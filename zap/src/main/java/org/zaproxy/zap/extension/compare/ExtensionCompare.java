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
package org.zaproxy.zap.extension.compare;

import java.awt.EventQueue;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.db.paros.ParosDatabase;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.extension.report.ReportGenerator;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

public class ExtensionCompare extends ExtensionAdaptor
        implements SessionChangedListener, SessionListener {

    private static final String NAME = "ExtensionCompare";

    private static final String CRLF = "\r\n";
    private JMenuItem menuCompare = null;

    private static Logger log = LogManager.getLogger(ExtensionCompare.class);

    public ExtensionCompare() {
        super(NAME);
        this.setOrder(44);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("cmp.name");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookMenu().addReportMenuItem(getMenuCompare());
        }
    }

    @Override
    public void sessionChanged(final Session session) {
        if (EventQueue.isDispatchThread()) {
            sessionChangedEventHandler(session);

        } else {

            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                sessionChangedEventHandler(session);
                            }
                        });
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void sessionChangedEventHandler(Session session) {}

    @Override
    public void sessionScopeChanged(Session session) {}

    private JMenuItem getMenuCompare() {
        if (menuCompare == null) {
            menuCompare = new JMenuItem();
            menuCompare.setText(Constant.messages.getString("cmp.file.menu.compare"));

            menuCompare.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            compareSessions();
                        }
                    });
        }
        return menuCompare;
    }

    private void buildHistoryMap(TableHistory th, Map<String, String> map)
            throws DatabaseException, HttpMalformedHeaderException {

        // Get the first session id
        RecordHistory rh = null;
        for (int i = 0; i < 100; i++) {
            rh = th.read(i);
            if (rh != null) {
                break;
            }
        }
        if (rh == null) {
            return;
        }

        List<Integer> hIds =
                th.getHistoryIdsOfHistType(
                        rh.getSessionId(),
                        HistoryReference.TYPE_PROXIED,
                        HistoryReference.TYPE_ZAP_USER,
                        HistoryReference.TYPE_SPIDER,
                        HistoryReference.TYPE_SPIDER_AJAX);

        for (Integer hId : hIds) {
            RecordHistory recH = th.read(hId);
            URI uri = recH.getHttpMessage().getRequestHeader().getURI();
            String mapKey =
                    recH.getHttpMessage().getRequestHeader().getMethod() + " " + uri.toString();

            // TODO Optionally strip off params?
            if (mapKey.indexOf("?") > -1) {
                mapKey = mapKey.substring(0, mapKey.indexOf("?"));
            }

            String val = map.get(mapKey);
            String code = recH.getHttpMessage().getResponseHeader().getStatusCode() + " ";
            if (val == null) {
                map.put(mapKey, code);
            } else if (val.indexOf(code) < 0) {
                map.put(mapKey, val + code);
            }
        }
    }

    private void compareSessions() {
        JFileChooser chooser =
                new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
        File file = null;
        chooser.setFileFilter(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        } else if (file.isFile() && file.getName().endsWith(".session")) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return Constant.messages.getString("file.format.zap.session");
                    }
                });
        int rc = chooser.showOpenDialog(getView().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            try {
                file = chooser.getSelectedFile();
                if (file == null) {
                    return;
                }
                Model cmpModel = new Model();
                Session session = cmpModel.getSession();

                // log.info("opening session file " + file.getAbsolutePath());
                // WaitMessageDialog waitMessageDialog =
                // getView().getWaitMessageDialog("Loading session file.  Please wait...");
                cmpModel.openSession(file, this);

                // TODO support other implementations in the future
                ParosDatabase db = new ParosDatabase();
                db.setDatabaseParam(new DatabaseParam());
                db.open(file.getAbsolutePath());

                Map<String, String> curMap = new HashMap<>();
                Map<String, String> cmpMap = new HashMap<>();

                // Load the 2 sessions into 2 maps
                this.buildHistoryMap(Model.getSingleton().getDb().getTableHistory(), curMap);
                this.buildHistoryMap(db.getTableHistory(), cmpMap);

                File outputFile = this.getOutputFile();

                if (outputFile != null) {
                    // Write the result to the specified file
                    try {
                        TreeSet<String> sset = new TreeSet<>();
                        // Combine the keys for both maps
                        sset.addAll(curMap.keySet());
                        sset.addAll(cmpMap.keySet());

                        StringBuilder sb = new StringBuilder(500);
                        sb.append("<?xml version=\"1.0\"?>");
                        sb.append(CRLF);
                        sb.append("<report>");
                        sb.append(CRLF);
                        sb.append("<session-names>");
                        sb.append(CRLF);
                        sb.append("<session1>");
                        sb.append(Model.getSingleton().getSession().getSessionName());
                        sb.append("</session1>");
                        sb.append(CRLF);
                        sb.append("<session2>");
                        sb.append(session.getSessionName());
                        sb.append("</session2>");
                        sb.append(CRLF);
                        sb.append("</session-names>");
                        sb.append(CRLF);

                        Iterator<String> iter = sset.iterator();
                        while (iter.hasNext()) {
                            sb.append("<urlrow>");
                            sb.append(CRLF);
                            String key = iter.next();
                            String method = key.substring(0, key.indexOf(" "));
                            String url = key.substring(key.indexOf(" ") + 1);

                            sb.append("<method>");
                            sb.append(method);
                            sb.append("</method>");
                            sb.append(CRLF);

                            sb.append("<url>");
                            sb.append(url);
                            sb.append("</url>");
                            sb.append(CRLF);

                            sb.append("<code1>");
                            if (curMap.containsKey(key)) {
                                sb.append(curMap.get(key));
                            } else {
                                sb.append("---");
                            }
                            sb.append("</code1>");
                            sb.append(CRLF);

                            sb.append("<code2>");
                            if (cmpMap.containsKey(key)) {
                                sb.append(cmpMap.get(key));
                            } else {
                                sb.append("---");
                            }
                            sb.append("</code2>");
                            sb.append(CRLF);

                            sb.append("</urlrow>");
                            sb.append(CRLF);
                        }

                        sb.append("</report>");
                        sb.append(CRLF);

                        String fileName = "reportCompare.xsl";
                        Path xslFile = Paths.get(Constant.getZapInstall(), "xml", fileName);
                        if (Files.exists(xslFile)) {
                            ReportGenerator.stringToHtml(
                                    sb.toString(),
                                    xslFile.toString(),
                                    outputFile.getAbsolutePath());
                        } else {
                            String path = "/org/zaproxy/zap/resources/xml/" + fileName;
                            try (InputStream is =
                                    ExtensionCompare.class.getResourceAsStream(path)) {
                                if (is == null) {
                                    log.error("Bundled file not found: " + path);
                                    return;
                                }
                                ReportGenerator.stringToHtml(
                                        sb.toString(),
                                        new StreamSource(is),
                                        outputFile.getAbsolutePath());
                            }
                        }

                        if (Files.notExists(outputFile.toPath())) {
                            log.info("Not opening report, does not exist: " + outputFile);
                            return;
                        }

                        try {
                            DesktopUtils.openUrlInBrowser(outputFile.toURI());
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            getView()
                                    .showMessageDialog(
                                            Constant.messages.getString(
                                                    "report.complete.warning",
                                                    outputFile.getAbsolutePath()));
                        }

                    } catch (Exception e1) {
                        log.warn(e1.getMessage(), e1);
                    }
                }

                // waitMessageDialog.setVisible(true);

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private File getOutputFile() {

        JFileChooser chooser =
                new WritableFileChooser(getModel().getOptionsParam().getUserDirectory());
        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        Constant.messages.getString("file.format.html"), "htm", "html"));

        File file = null;
        int rc = chooser.showSaveDialog(getView().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                return file;
            }
            String fileNameLc = file.getAbsolutePath().toLowerCase();
            if (!fileNameLc.endsWith(".htm") && !fileNameLc.endsWith(".html")) {
                file = new File(file.getAbsolutePath() + ".html");
            }
            return file;
        }
        return file;
    }

    @Override
    public void sessionOpened(File file, Exception e) {}

    @Override
    public void sessionSaved(Exception e) {}

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("cmp.desc");
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    @Override
    public void sessionSnapshot(Exception e) {
        // Ignore
    }
}
