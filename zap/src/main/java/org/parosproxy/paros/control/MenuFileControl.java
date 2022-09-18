/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
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
// ZAP: 2011/05/15 Improved error logging
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/06/11 Changed to call the method Control.shutdown(boolean) with the
// parameter set as true.
// ZAP: 2012/06/19 Changed the method sessionOpened(File,Exception) to not call
// the method ExtensionLoader.sessionChangedAllPlugin, now it's done in the
// class Control.
// ZAP: 2012/07/02 Changed to use the new database compact option in the method
// exit().
// ZAP: 2012/07/23 Removed parameter from View.getSessionDialog call.
// ZAP: 2012/12/06 Issue 428: Moved exit code to control to support the marketplace
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/08/05 Proper call for starting Session Properties dialog
// ZAP: 2013/08/28 Issue 695: Sites tree doesn't clear on new session created by API
// ZAP: 2014/05/20 Issue 1191: Cmdline session params have no effect
// ZAP: 2014/12/22 Issue 1476: Display contexts in the Sites tree
// ZAP: 2015/01/29 Issue 1489: Version number in window title
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog
// ZAP: 2015/04/02 Issue 321: Support multiple databases
// ZAP: 2015/12/14 Log exception and internationalise error message
// ZAP: 2016/10/26 Issue 1952: Do not allow Contexts with same name
// ZAP: 2017/02/25 Issue 2618: Let the user select the name for snapshots
// ZAP: 2017/06/01 Issue 3555: setTitle() functionality moved in order to ensure consistent
// application
// ZAP: 2017/06/20 Inform of active actions before changing the session.
// ZAP: 2017/08/31 Use helper method I18N.getString(String, Object...).
// ZAP: 2017/11/22 Do not allow to snapshot the session with active actions (Issue 3711).
// ZAP: 2017/12/15 Confirm when overwriting session file (Issue 4153).
// ZAP: 2018/01/01 Prevent the selection of the current session on save/snapshot.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/14 Remove redundant type arguments.
// ZAP: 2022/08/05 Address warns with Java 18.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.control;

import java.awt.EventQueue;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordSession;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.model.IllegalContextNameException;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.ContextExportDialog;
import org.zaproxy.zap.view.PersistSessionDialog;
import org.zaproxy.zap.view.SessionTableSelectDialog;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

public class MenuFileControl implements SessionListener {

    private static Logger log = LogManager.getLogger(MenuFileControl.class);

    private View view = null;
    private Model model = null;
    private Control control = null;
    private WaitMessageDialog waitMessageDialog = null;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");

    public MenuFileControl(Model model, View view, Control control) {
        this.view = view;
        this.model = model;
        this.control = control;
    }

    public void exit() {
        control.exit(false, null);
    }

    public void newSession(boolean isPromptNewSession) throws ClassNotFoundException, Exception {

        if (isPromptNewSession) {
            if (!informStopActiveActions()) {
                return;
            }

            // ZAP: i18n
            if (model.getSession().isNewState()) {
                if (view.showConfirmDialog(Constant.messages.getString("menu.file.discardSession"))
                        != JOptionPane.OK_OPTION) {
                    return;
                }
                control.discardSession();
            } else if (view.showConfirmDialog(Constant.messages.getString("menu.file.closeSession"))
                    != JOptionPane.OK_OPTION) {
                return;
            }
        }

        int newSessionOption = model.getOptionsParam().getDatabaseParam().getNewSessionOption();

        if (model.getOptionsParam().getDatabaseParam().isNewSessionPrompt()) {
            PersistSessionDialog psd = new PersistSessionDialog(View.getSingleton().getMainFrame());
            // Set up the default option - i.e. the same one the user chose last time
            switch (newSessionOption) {
                case DatabaseParam.NEW_SESSION_TIMESTAMPED:
                    psd.setTimestampChosen();
                    break;
                case DatabaseParam.NEW_SESSION_USER_SPECIFIED:
                    psd.setPersistChosen();
                    break;
                case DatabaseParam.NEW_SESSION_TEMPORARY:
                    psd.setTemporaryChosen();
                    break;
                default:
                    break;
            }

            psd.setVisible(true);

            if (psd.isTimestampChosen()) {
                newSessionOption = DatabaseParam.NEW_SESSION_TIMESTAMPED;
            } else if (psd.isPersistChosen()) {
                newSessionOption = DatabaseParam.NEW_SESSION_USER_SPECIFIED;
            } else {
                newSessionOption = DatabaseParam.NEW_SESSION_TEMPORARY;
            }
            // Save for next time
            model.getOptionsParam().getDatabaseParam().setNewSessionOption(newSessionOption);
            model.getOptionsParam().getDatabaseParam().setNewSessionPrompt(!psd.isDontAskAgain());
        }

        switch (newSessionOption) {
            case DatabaseParam.NEW_SESSION_TIMESTAMPED:
                String filename = getTimestampFilename();
                if (filename != null) {
                    this.newSession(filename);
                } else {
                    control.newSession();
                }
                break;
            case DatabaseParam.NEW_SESSION_USER_SPECIFIED:
                control.newSession();
                this.saveAsSession();
                break;
            default:
                control.newSession();
                break;
        }
    }

    private boolean informStopActiveActions() {
        String activeActions = wrapEntriesInLiTags(control.getExtensionLoader().getActiveActions());
        if (!activeActions.isEmpty()) {
            String message =
                    Constant.messages.getString("menu.file.session.activeactions", activeActions);
            if (view.showConfirmDialog(new ZapHtmlLabel(message)) != JOptionPane.OK_OPTION) {
                return false;
            }
        }
        return true;
    }

    private static String wrapEntriesInLiTags(List<String> entries) {
        if (entries.isEmpty()) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder(entries.size() * 15);
        for (String entry : entries) {
            strBuilder.append("<li>");
            strBuilder.append(entry);
            strBuilder.append("</li>");
        }
        return strBuilder.toString();
    }

    private String getTimestampFilename() {
        File dir = new File(Constant.getZapHome(), "sessions");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        String timestamp = dateFormat.format(new Date());
        File tmpFile = new File(dir, timestamp + ".session");
        return tmpFile.getAbsolutePath();
    }

    public boolean newSession(String fileName) {
        final Object[] created = {Boolean.TRUE};
        waitMessageDialog =
                view.getWaitMessageDialog(
                        Constant.messages.getString("menu.file.newSession.wait.dialogue"));
        control.newSession(
                fileName,
                new SessionListener() {

                    @Override
                    public void sessionSnapshot(Exception e) {}

                    @Override
                    public void sessionSaved(final Exception e) {
                        if (EventQueue.isDispatchThread()) {
                            if (e == null) {
                                view.getSiteTreePanel()
                                        .getTreeSite()
                                        .setModel(model.getSession().getSiteTree());
                            } else {
                                view.showWarningDialog(
                                        Constant.messages.getString("menu.file.newSession.error"));
                                log.error(
                                        "Error creating session file {}",
                                        model.getSession().getFileName(),
                                        e);
                                created[0] = Boolean.FALSE;
                            }

                            if (waitMessageDialog != null) {
                                waitMessageDialog.setVisible(false);
                                waitMessageDialog = null;
                            }
                        } else {
                            EventQueue.invokeLater(
                                    new Runnable() {

                                        @Override
                                        public void run() {
                                            sessionSaved(e);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void sessionOpened(File file, Exception e) {}
                });

        waitMessageDialog.setVisible(true);
        return created[0] == Boolean.TRUE;
    }

    public boolean openSession(String session) {
        final Object[] opened = {Boolean.TRUE};
        File sessionFile = new File(session);
        waitMessageDialog =
                view.getWaitMessageDialog(Constant.messages.getString("menu.file.loadSession"));
        log.info("opening session file {}", sessionFile.getAbsolutePath());
        control.openSession(
                sessionFile,
                new SessionListener() {

                    @Override
                    public void sessionSnapshot(Exception e) {}

                    @Override
                    public void sessionSaved(Exception e) {}

                    @Override
                    public void sessionOpened(final File file, final Exception e) {
                        if (EventQueue.isDispatchThread()) {
                            if (e != null) {
                                view.showWarningDialog(
                                        Constant.messages.getString("menu.file.openSession.error"));
                                log.error(
                                        "error opening session file {}",
                                        model.getSession().getFileName(),
                                        e);
                                opened[0] = Boolean.FALSE;
                            }

                            view.getSiteTreePanel()
                                    .getTreeSite()
                                    .setModel(model.getSession().getSiteTree());

                            if (waitMessageDialog != null) {
                                waitMessageDialog.setVisible(false);
                                waitMessageDialog = null;
                            }
                        } else {
                            EventQueue.invokeLater(
                                    new Runnable() {

                                        @Override
                                        public void run() {
                                            sessionOpened(file, e);
                                        }
                                    });
                        }
                    }
                });
        waitMessageDialog.setVisible(true);

        return opened[0] == Boolean.TRUE;
    }

    public void openSession() {
        if (!informStopActiveActions()) {
            return;
        }

        // TODO extract into db specific classes??
        if (Database.DB_TYPE_HSQLDB.equals(model.getDb().getType())) {
            this.openFileBasedSession();
        } else {
            this.openDbBasedSession();
        }
    }

    private void openFileBasedSession() {
        JFileChooser chooser = new JFileChooser(model.getOptionsParam().getUserDirectory());
        chooser.setFileHidingEnabled(
                false); // By default ZAP on linux puts timestamped sessions under a 'dot' directory
        File file = null;
        chooser.setFileFilter(SessionFileChooser.SESSION_FILE_FILTER);
        int rc = chooser.showOpenDialog(view.getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            try {
                file = chooser.getSelectedFile();
                if (file == null) {
                    return;
                }
                model.getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
                log.info("opening session file {}", file.getAbsolutePath());
                waitMessageDialog =
                        view.getWaitMessageDialog(
                                Constant.messages.getString("menu.file.loadSession"));
                control.openSession(file, this);
                waitMessageDialog.setVisible(true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void openDbBasedSession() {
        try {
            List<String> sessionList = new ArrayList<>();
            for (RecordSession rs : model.getDb().getTableSession().listSessions()) {
                sessionList.add("" + rs.getSessionId());
            }
            SessionTableSelectDialog ssd =
                    new SessionTableSelectDialog(View.getSingleton().getMainFrame(), sessionList);
            ssd.setVisible(true);

            if (ssd.getSelectedSession() != null) {
                waitMessageDialog =
                        view.getWaitMessageDialog(
                                Constant.messages.getString("menu.file.loadSession"));
                control.openSession(ssd.getSelectedSession(), this);
                waitMessageDialog.setVisible(true);
            }

        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void saveSession() {
        Session session = model.getSession();

        if (session.isNewState()) {
            view.showWarningDialog("Please use Save As...");
            return;
        }

        try {
            waitMessageDialog =
                    view.getWaitMessageDialog(
                            Constant.messages.getString("menu.file.savingSession")); // ZAP: i18n
            control.saveSession(session.getFileName(), this);
            log.info("saving session file {}", session.getFileName());
            // ZAP: If the save is quick the dialog can already be null here
            if (waitMessageDialog != null) {
                waitMessageDialog.setVisible(true);
            }

        } catch (Exception e) {
            view.showWarningDialog(
                    Constant.messages.getString("menu.file.savingSession.error")); // ZAP: i18n
            log.error("error saving session file {}", session.getFileName());
            log.error(e.getMessage(), e);
        }
    }

    public void saveAsSession() {
        if (!informStopActiveActions()) {
            return;
        }

        Session session = model.getSession();

        JFileChooser chooser =
                new SessionFileChooser(model.getOptionsParam().getUserDirectory(), session);
        // ZAP: set session name as file name proposal
        File fileproposal = new File(session.getSessionName());
        if (session.getFileName() != null && session.getFileName().trim().length() > 0) {
            // if there is already a file name, use it
            fileproposal = new File(session.getFileName());
        }
        chooser.setSelectedFile(fileproposal);
        File file = null;
        int rc = chooser.showSaveDialog(view.getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }
            String fileName = createSessionFileName(file);

            try {
                waitMessageDialog =
                        view.getWaitMessageDialog(
                                Constant.messages.getString(
                                        "menu.file.savingSession")); // ZAP: i18n
                control.saveSession(fileName, this);
                log.info("save as session file {}", session.getFileName());
                waitMessageDialog.setVisible(true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static String createSessionFileName(File file) {
        String fileName = file.getAbsolutePath();
        if (!fileName.endsWith(".session")) {
            fileName += ".session";
        }
        return fileName;
    }

    public void saveSnapshot() {
        String activeActions = wrapEntriesInLiTags(control.getExtensionLoader().getActiveActions());
        if (!activeActions.isEmpty()) {
            view.showMessageDialog(
                    Constant.messages.getString("menu.file.snapshot.activeactions", activeActions));
            return;
        }

        Session session = model.getSession();

        JFileChooser chooser =
                new SessionFileChooser(model.getOptionsParam().getUserDirectory(), session);
        // ZAP: set session name as file name proposal
        File fileproposal = new File(session.getSessionName());
        if (session.getFileName() != null && session.getFileName().trim().length() > 0) {
            String proposedFileName;
            // if there is already a file name, use it and add a timestamp
            proposedFileName = StringUtils.removeEnd(session.getFileName(), ".session");
            proposedFileName += "-" + dateFormat.format(new Date()) + ".session";
            fileproposal = new File(proposedFileName);
        }
        chooser.setSelectedFile(fileproposal);
        File file = null;
        int rc = chooser.showSaveDialog(view.getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }
            String fileName = createSessionFileName(file);

            try {
                waitMessageDialog =
                        view.getWaitMessageDialog(
                                Constant.messages.getString(
                                        "menu.file.savingSnapshot")); // ZAP: i18n
                control.snapshotSession(fileName, this);
                log.info("Snapshotting: {} as {}", session.getFileName(), fileName);
                waitMessageDialog.setVisible(true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void properties() {
        // ZAP: proper call of existing method
        View.getSingleton().showSessionDialog(model.getSession(), null);
    }

    @Override
    public void sessionOpened(File file, Exception e) {
        if (e == null) {
            // ZAP: Removed the statement that called the method
            // ExtensionLoader.sessionChangedAllPlugin, now it's done in the
            // class Control.
        } else {
            view.showWarningDialog(Constant.messages.getString("menu.file.openSession.errorFile"));
            if (file != null) {
                log.error("Error opening session file {}", file.getAbsolutePath(), e);
            } else {
                // File is null for table based sessions (i.e. non HSQLDB)
                log.error(e.getMessage(), e);
            }
        }

        if (waitMessageDialog != null) {
            waitMessageDialog.setVisible(false);
            waitMessageDialog = null;
        }
    }

    @Override
    public void sessionSaved(Exception e) {
        if (e != null) {
            view.showWarningDialog(
                    Constant.messages.getString("menu.file.savingSession.error")); // ZAP: i18n
            log.error("error saving session file {}", model.getSession().getFileName(), e);
            log.error(e.getMessage(), e);
        }

        if (waitMessageDialog != null) {
            waitMessageDialog.setVisible(false);
            waitMessageDialog = null;
        }
    }

    @Override
    public void sessionSnapshot(Exception e) {
        if (e != null) {
            view.showWarningDialog(
                    Constant.messages.getString("menu.file.snapshotSession.error")); // ZAP: i18n
            log.error("error saving snapshot file {}", model.getSession().getFileName(), e);
            log.error(e.getMessage(), e);
        }

        if (waitMessageDialog != null) {
            waitMessageDialog.setVisible(false);
            waitMessageDialog = null;
        }
    }

    /** Prompt the user to export a context */
    public void importContext() {
        JFileChooser chooser = new JFileChooser(Constant.getContextsDir());
        File file = null;
        chooser.setFileFilter(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        } else if (file.isFile() && file.getName().endsWith(".context")) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return Constant.messages.getString("file.format.zap.context");
                    }
                });

        int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            try {
                file = chooser.getSelectedFile();
                if (file == null || !file.exists()) {
                    return;
                }
                // Import the context
                Model.getSingleton().getSession().importContext(file);

                // Show the dialog
                View.getSingleton()
                        .showSessionDialog(
                                Model.getSingleton().getSession(),
                                Constant.messages.getString("context.list"),
                                true);

            } catch (IllegalContextNameException e) {
                String detailError;
                if (e.getReason() == IllegalContextNameException.Reason.EMPTY_NAME) {
                    detailError = Constant.messages.getString("context.error.name.empty");
                } else if (e.getReason() == IllegalContextNameException.Reason.DUPLICATED_NAME) {
                    detailError = Constant.messages.getString("context.error.name.duplicated");
                } else {
                    detailError = Constant.messages.getString("context.error.name.unknown");
                }
                View.getSingleton()
                        .showWarningDialog(
                                Constant.messages.getString("context.import.error", detailError));
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
                View.getSingleton()
                        .showWarningDialog(
                                Constant.messages.getString(
                                        "context.import.error", e1.getMessage()));
            }
        }
    }

    /** Prompt the user to export a context */
    public void exportContext() {
        ContextExportDialog exportDialog =
                new ContextExportDialog(View.getSingleton().getMainFrame());
        exportDialog.setVisible(true);
    }

    @SuppressWarnings("serial")
    private static class SessionFileChooser extends WritableFileChooser {

        public static final FileFilter SESSION_FILE_FILTER =
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory()
                                || file.isFile() && file.getName().endsWith(".session");
                    }

                    @Override
                    public String getDescription() {
                        return Constant.messages.getString("file.format.zap.session");
                    }
                };

        private static final long serialVersionUID = 1L;

        private final Session currentSession;

        public SessionFileChooser(File currentDirectory, Session currentSession) {
            super(currentDirectory);

            setFileFilter(SESSION_FILE_FILTER);
            this.currentSession = currentSession;
        }

        @Override
        public void approveSelection() {
            File file = getSelectedFile();
            if (file != null) {
                File sessionFile = new File(createSessionFileName(file));
                setSelectedFile(sessionFile);

                if (!currentSession.isNewState()) {
                    File currentFile = new File(currentSession.getFileName());
                    if (currentFile.getAbsolutePath().equals(sessionFile.getAbsolutePath())) {
                        showErrorDialog(
                                Constant.messages.getString(
                                        "menu.file.error.selectedCurrentSession.msg"),
                                Constant.messages.getString(
                                        "menu.file.error.selectedCurrentSession.title"));
                        return;
                    }
                }
            }
            super.approveSelection();
        }
    }
}
