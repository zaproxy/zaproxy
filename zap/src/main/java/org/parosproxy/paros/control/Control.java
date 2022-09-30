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
// ZAP: 2011/04/16 Support for running ZAP as a daemon
// ZAP: 2011/05/09 Support for API
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/10/29 Support for parameters
// ZAP: 2011/11/20 Changed to use ExtensionFactory
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Changed the method shutdown(boolean) to save the configurations
// of the main http panels and save the configuration file.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/16 Added new initialization method plus ctor for testing purposes.
// ZAP: 2012/06/25 Moved call of init() from ctor to singleton methods to
// initialize singleton variable first. Allows to retrieve the singleton while
// not fully initialized (e.g.: to get another extension in the hook() method of
// and extension).
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/11/01 Issue 411: Allow proxy port to be specified on the command line
// ZAP: 2012/12/06 Issue 428: Added exit method to support the marketplace
// ZAP: 2012/12/27 Hook new persistent connection listener.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/20 Issue 568: Allow extensions to run from the command line
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/08/28 Issue 695: Sites tree doesn't clear on new session created by API
// ZAP: 2013/08/29 Issue 776: Allow add-ons to warn user if they're closing ZAP with unsaved
// resources open
// ZAP: 2013/09/16 Issue 791: Saved sessions are discarded on ZAP's exit
// ZAP: 2014/01/16 Issue 979: Sites and Alerts trees can get corrupted
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/02/27 Issue 1055: Load extensions before plugins
// ZAP: 2014/05/20 Issue 1114: core.newSession doesn't clear Sites
// ZAP: 2014/05/20 Issue 1191: Cmdline session params have no effect
// ZAP: 2014/09/22 Issue 1345: Support Attack mode
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2015/01/29 Issue 1489: Version number in window title
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/02/10 Issue 1208: Search classes/resources in add-ons declared as dependencies
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2015/09/17 Issue 1914: Support multiple add-on directories
// ZAP: 2015/11/04 Issue 1920: Report the host:port ZAP is listening on in daemon mode, or exit if
// it cant
// ZAP: 2016/03/23 Issue 2331: Custom Context Panels not show in existing contexts after
// installation of add-on
// ZAP: 2016/04/22 Issue 2428: Memory leak on session creation/loading
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/09/06 Hook OverrideMessageProxyListener into the Proxy
// ZAP: 2016/10/06 Issue 2855: Added method to allow for testing when a model is required
// ZAP: 2017/03/10 Reset proxy excluded URLs on new session
// ZAP: 2017/03/13 Set global excluded URLs to the proxy when creating a new session or
// initialising.
// ZAP: 2017/03/16 Allow to initialise Control without starting the Local Proxy.
// ZAP: 2017/06/07 Allow to persist the session properties (e.g. name, description).
// ZAP: 2017/08/31 Use helper method I18N.getString(String, Object...).
// ZAP: 2018/01/04 Do not notify extensions if failed to change the session.
// ZAP: 2018/01/12 Save configurations as last shutdown action.
// ZAP: 2019/03/14 Improve error handling on shutdown
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/09/30 Reduce View singleton usage and replace null checks with hasView().
// ZAP: 2019/12/16 Log path of new session.
// ZAP: 2019/12/13 Enable prompting/suggesting a new port when there's a proxy port conflict (Issue
// 2016).
// ZAP: 2020/11/23 Allow to initialise the singleton with an ExtensionLoader for tests.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/14 Remove empty statement.
// ZAP: 2021/09/13 Added setExitStatus.
// ZAP: 2021/11/08 Validate if mandatory add-ons are present.
// ZAP: 2022/02/09 No longer manage the proxy, deprecate related code.
// ZAP: 2022/02/24 Remove code deprecated in 2.5.0
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.control;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.control.AddOnLoader;
import org.zaproxy.zap.control.ControlOverrides;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.utils.ZapHtmlLabel;

/** Overall control with interaction on model and view. */
public class Control extends AbstractControl implements SessionListener {

    public enum Mode {
        safe,
        protect,
        standard,
        attack
    }

    private static Logger log = LogManager.getLogger(Control.class);

    private static Control control = null;
    private MenuFileControl menuFileControl = null;
    private MenuToolsControl menuToolsControl = null;
    private SessionListener lastCallback = null;
    private Mode mode = null;
    private int exitStatus = 0;

    private Control(Model model, View view) {
        super(model, view);
        // ZAP: moved call of init() to singleton methods
    }

    // ZAP: Added constructor that will be used by initSingletonForTesting()
    private Control() {
        super(null, null);
    }

    private boolean init(ControlOverrides overrides) {
        AddOnLoader addOnLoader =
                ExtensionFactory.getAddOnLoader(
                        model.getOptionsParam().getCheckForUpdatesParam().getAddonDirectories());
        if (overrides != null) {
            AddOnCollection addOnCollection = addOnLoader.getAddOnCollection();
            overrides
                    .getMandatoryAddOns()
                    .forEach(
                            id -> {
                                AddOn addOn = addOnCollection.getAddOn(id);
                                if (addOn == null) {
                                    String message =
                                            "The mandatory add-on was not found: "
                                                    + id
                                                    + "\nRefer to https://www.zaproxy.org/docs/developer/ if you are building ZAP from source.";
                                    log.error(message);
                                    throw new IllegalStateException(message);
                                }
                                addOn.setMandatory(true);
                            });
        }

        // Load extensions first as message bundles are loaded as a side effect
        loadExtension();

        if (hasView()) {
            // ZAP: Add site map listeners
            getExtensionLoader().hookSiteMapListener(view.getSiteTreePanel());
        }

        model.postInit();

        return false;
    }

    private boolean hasView() {
        return view != null;
    }

    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated
    public Proxy getProxy() {
        return this.getProxy(null);
    }
    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated
    public Proxy getProxy(ControlOverrides overrides) {
        return new Proxy(model, overrides);
    }

    @Override
    protected void addExtension() {
        ExtensionFactory.loadAllExtension(getExtensionLoader(), model.getOptionsParam());
    }

    public MenuFileControl getMenuFileControl() {
        if (menuFileControl == null) {
            menuFileControl = new MenuFileControl(model, view, this);
        }

        return menuFileControl;
    }

    public MenuToolsControl getMenuToolsControl() {
        if (menuToolsControl == null) {
            menuToolsControl = new MenuToolsControl(model, view, this);
        }
        return menuToolsControl;
    }

    /** Override inherited shutdown to add stopping proxy servers. */
    @Override
    public void shutdown(boolean compact) {
        try {
            if (hasView()) {
                view.getRequestPanel().saveConfig(model.getOptionsParam().getConfig());
                view.getResponsePanel().saveConfig(model.getOptionsParam().getConfig());
            }

            super.shutdown(compact);
        } finally {
            // Ensure all extensions' config changes done during shutdown are saved.
            saveConfigurations();
        }
    }

    private void saveConfigurations() {
        try {
            model.getOptionsParam().getConfig().save();
        } catch (ConfigurationException e) {
            log.error("Error saving configurations:", e);
        }
    }

    public void exit(boolean noPrompt, final File openOnExit) {
        boolean isNewState = model.getSession().isNewState();
        int rootCount = 0;
        if (!Constant.isLowMemoryOptionSet()) {
            rootCount =
                    model.getSession()
                            .getSiteTree()
                            .getChildCount(model.getSession().getSiteTree().getRoot());
        }
        boolean askOnExit =
                hasView()
                        && Model.getSingleton()
                                        .getOptionsParam()
                                        .getViewParam()
                                        .getAskOnExitOption()
                                > 0;
        boolean sessionUnsaved = isNewState && rootCount > 0;

        if (!noPrompt) {
            List<String> list = getExtensionLoader().getUnsavedResources();
            if (sessionUnsaved && askOnExit) {
                list.add(
                        0,
                        Constant.messages.getString("menu.file.exit.message.sessionResNotSaved"));
            }

            String message = null;
            String activeActions = wrapEntriesInLiTags(getExtensionLoader().getActiveActions());
            if (list.size() > 0) {
                String unsavedResources = wrapEntriesInLiTags(list);

                if (activeActions.isEmpty()) {
                    message =
                            Constant.messages.getString(
                                    "menu.file.exit.message.resourcesNotSaved", unsavedResources);
                } else {
                    message =
                            Constant.messages.getString(
                                    "menu.file.exit.message.resourcesNotSavedAndActiveActions",
                                    unsavedResources,
                                    activeActions);
                }
            } else if (!activeActions.isEmpty()) {
                message =
                        Constant.messages.getString(
                                "menu.file.exit.message.activeActions", activeActions);
            }

            if (message != null
                    && view.showConfirmDialog(new ZapHtmlLabel(message)) != JOptionPane.OK_OPTION) {
                return;
            }
        }

        if (sessionUnsaved) {
            control.discardSession();
        }

        Thread t =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                // ZAP: Changed to use the option compact database.
                                try {
                                    control.shutdown(
                                            Model.getSingleton()
                                                    .getOptionsParam()
                                                    .getDatabaseParam()
                                                    .isCompactDatabase());
                                    log.info("{} terminated.", Constant.PROGRAM_TITLE);

                                    if (openOnExit != null && Desktop.isDesktopSupported()) {
                                        try {
                                            log.info(
                                                    "Openning file {}",
                                                    openOnExit.getAbsolutePath());
                                            Desktop.getDesktop().open(openOnExit);
                                        } catch (IOException e) {
                                            log.error(
                                                    "Failed to open file {}",
                                                    openOnExit.getAbsolutePath(),
                                                    e);
                                        }
                                    }
                                } catch (Throwable e) {
                                    log.error("An error occurred while shutting down:", e);
                                } finally {
                                    System.exit(exitStatus);
                                }
                            }
                        },
                        "ZAP-Shutdown");

        if (hasView()) {
            WaitMessageDialog dialog =
                    view.getWaitMessageDialog(
                            Constant.messages.getString("menu.file.shuttingDown")); // ZAP: i18n
            t.start();
            dialog.setVisible(true);
        } else {
            t.start();
        }
    }

    /**
     * Sets the non zero value ZAP will exit cleanly with. ZAP may still exit with a non zero value
     * if a serious error occurs. This will work however ZAP is run but it makes more sense if ZAP
     * is run in cmdline mode. Any add-on can set an exit status so attempts to reset the status to
     * zero will be rejected.
     *
     * @param exitStatus the non zero value ZAP will exit it with
     * @param logMessage the message that will be logged at info level
     * @since 2.11.0
     */
    public void setExitStatus(int exitStatus, String logMessage) {
        if (exitStatus == 0) {
            log.error("Not setting the exit status to zero - culprit: {}", logMessage);
        } else {
            this.exitStatus = exitStatus;
            log.info(logMessage);
        }
    }

    public int getExitStatus() {
        return exitStatus;
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

    public void exitAndDeleteSession(String sessionName) {
        shutdown(false);
        Model.getSingleton().getDb().deleteSession(sessionName);

        log.info("{} terminated.", Constant.PROGRAM_TITLE);
        System.exit(this.getExitStatus());
    }

    public static Control getSingleton() {

        return control;
    }

    public static boolean initSingletonWithView(ControlOverrides overrides) {
        control = new Control(Model.getSingleton(), View.getSingleton());
        return control.init(overrides);
    }

    public static boolean initSingletonWithoutView(ControlOverrides overrides) {
        control = new Control(Model.getSingleton(), null);
        return control.init(overrides);
    }

    /**
     * @deprecated (2.12.0) Use {@link #initSingletonWithoutView(ControlOverrides)} instead. It will
     *     be removed in a future release.
     */
    @Deprecated
    public static void initSingletonWithoutViewAndProxy(ControlOverrides overrides) {
        initSingletonWithoutView(overrides);
    }

    // ZAP: Added method to allow for testing
    public static void initSingletonForTesting() {
        control = new Control();
    }

    // ZAP: Added method to allow for testing when a model is required
    public static void initSingletonForTesting(Model model) {
        control = new Control(model, null);
    }

    /**
     * Initialises the {@code Control} singleton with the given data.
     *
     * <p><strong>Note:</strong> Not part of the public API.
     *
     * @param model the {@code Model} to test with.
     * @param extensionLoader the {@code ExtensionLoader} to test with.
     */
    public static void initSingletonForTesting(Model model, ExtensionLoader extensionLoader) {
        initSingletonForTesting(model);
        control.loader = extensionLoader;
    }

    public void runCommandLine() throws Exception {
        log.debug("runCommand");
        getExtensionLoader().runCommandLine();
    }

    public void runCommandLineNewSession(String fileName) throws Exception {
        log.debug("runCommandLineNewSession {}", fileName);
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);

        model.createAndOpenUntitledDb();
        final Session session = createNewSession();
        model.saveSession(fileName);

        if (hasView()) {
            SwingUtilities.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            view.getSiteTreePanel().getTreeSite().setModel(session.getSiteTree());

                            // refresh display
                            view.getOutputPanel().clear();
                        }
                    });
        }

        log.info("New session file created: {}", Paths.get(fileName).toRealPath());
        control.getExtensionLoader().databaseOpen(model.getDb());
        control.getExtensionLoader().sessionChangedAllPlugin(session);
    }

    /**
     * Creates a new session.
     *
     * @return the newly created session.
     */
    private Session createNewSession() {
        return model.newSession();
    }

    public void runCommandLineOpenSession(String fileName) throws Exception {
        log.debug("runCommandLineOpenSession {}", fileName);
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);

        Session session = Model.getSingleton().getSession();
        Model.getSingleton().openSession(fileName);
        log.info("Session file opened");
        control.getExtensionLoader().databaseOpen(model.getDb());
        control.getExtensionLoader().sessionChangedAllPlugin(session);
    }

    /**
     * @deprecated (2.12.0) The proxy is no longer managed by Control. It will be removed in a
     *     future release.
     */
    @Deprecated
    public void setExcludeFromProxyUrls(List<String> urls) {}

    public void openSession(final File file, final SessionListener callback) {
        log.info("Open Session");
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);
        lastCallback = callback;
        model.openSession(file, this);
        // The session is opened in a thread, so notify the listeners via the callback
    }

    public void openSession(final String fileName, final SessionListener callback) {
        log.info("Open Session");
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);
        lastCallback = callback;
        model.openSession(fileName, this);
        // The session is opened in a thread, so notify the listeners via the callback
    }

    public Session newSession() throws Exception {
        log.info("New Session");
        closeSessionAndCreateAndOpenUntitledDb();
        final Session session = createNewSession();
        getExtensionLoader().databaseOpen(model.getDb());
        getExtensionLoader().sessionChangedAllPlugin(session);

        if (hasView()) {
            SwingUtilities.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            view.getSiteTreePanel().getTreeSite().setModel(session.getSiteTree());
                            view.getSiteTreePanel().reloadContextTree();
                        }
                    });

            // refresh display
            view.getOutputPanel().clear();
        }

        try {
            model.getDb()
                    .getTableSession()
                    .insert(session.getSessionId(), session.getSessionName());
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }

        return session;
    }

    /**
     * Closes the old session and creates and opens an untitled database.
     *
     * @throws Exception if an error occurred while creating or opening the database.
     */
    private void closeSessionAndCreateAndOpenUntitledDb() throws Exception {
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);
        model.closeSession();
        log.info("Create and Open Untitled Db");
        model.createAndOpenUntitledDb();
    }

    public void newSession(String fileName, final SessionListener callback) {
        log.info("New Session");
        try {
            closeSessionAndCreateAndOpenUntitledDb();
            lastCallback = callback;
            createNewSession();
            model.saveSession(fileName, this);
        } catch (Exception e) {
            if (lastCallback != null) {
                lastCallback.sessionSaved(e);
                lastCallback = null;
            }
        }
    }

    public void saveSession(final String fileName) {
        this.saveSession(fileName, null);
    }

    public void saveSession(final String fileName, final SessionListener callback) {
        log.info("Save Session");
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);
        lastCallback = callback;
        model.saveSession(fileName, this);
        // The session is saved in a thread, so notify the listeners via the callback
    }

    /**
     * Persists the properties (e.g. name, description) of the current session.
     *
     * <p>Should be called only by "core" classes.
     *
     * @throws Exception if an error occurred while persisting the properties.
     * @since 2.7.0
     */
    public void persistSessionProperties() throws Exception {
        model.persistSessionProperties();
        getExtensionLoader().sessionPropertiesChangedAllPlugin(model.getSession());
    }

    public void snapshotSession(final String fileName, final SessionListener callback) {
        log.info("Snapshot Session");
        lastCallback = callback;
        model.snapshotSession(fileName, this);
        // The session is saved in a thread, so notify the listeners via the callback
    }

    public void discardSession() {
        log.info("Discard Session");
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);
        model.discardSession();
        getExtensionLoader().sessionChangedAllPlugin(null);
    }

    @Override
    public void sessionOpened(File file, Exception e) {
        notifyExtensionsSessionChanged(e);

        if (lastCallback != null) {
            lastCallback.sessionOpened(file, e);
            lastCallback = null;
        }
    }

    /**
     * Notifies the extensions that the session changed, if the given exception is {@code null}.
     *
     * @param exception the exception that happened when changing the session, or {@code null} if
     *     none.
     */
    private void notifyExtensionsSessionChanged(Exception exception) {
        if (exception == null) {
            getExtensionLoader().databaseOpen(model.getDb());
            getExtensionLoader().sessionChangedAllPlugin(model.getSession());
        }
    }

    @Override
    public void sessionSaved(Exception e) {
        notifyExtensionsSessionChanged(e);

        if (lastCallback != null) {
            lastCallback.sessionSaved(e);
            lastCallback = null;
        }
    }

    @Override
    public void sessionSnapshot(Exception e) {
        if (lastCallback != null) {
            lastCallback.sessionSnapshot(e);
            lastCallback = null;
        }
    }

    public void sessionScopeChanged() {
        getExtensionLoader().sessionScopeChangedAllPlugin(model.getSession());
    }

    public Mode getMode() {
        if (mode == null) {
            mode = Mode.valueOf(model.getOptionsParam().getViewParam().getMode());
        }
        return mode;
    }

    public void setMode(Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            getExtensionLoader().sessionModeChangedAllPlugin(mode);
            model.getOptionsParam().getViewParam().setMode(mode.name());
        }
    }
}
