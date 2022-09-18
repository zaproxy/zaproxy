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
package org.zaproxy.zap;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnLoader;
import org.zaproxy.zap.control.AddOnRunIssuesUtils;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.model.SessionUtils;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.LocaleUtils;
import org.zaproxy.zap.view.LocaleDialog;

/**
 * The bootstrap process for GUI mode.
 *
 * @since 2.4.2
 */
public class GuiBootstrap extends ZapBootstrap {

    private static final Logger logger = LogManager.getLogger(GuiBootstrap.class);

    /**
     * Flag that indicates whether or not the look and feel was already set.
     *
     * @see #setupLookAndFeel()
     */
    private boolean lookAndFeelSet;

    public GuiBootstrap(CommandLine cmdLineArgs) {
        super(cmdLineArgs);
    }

    @Override
    public int start() {
        int rc = super.start();
        if (rc != 0) {
            return rc;
        }

        logger.info(getStartingMessage());

        if (GraphicsEnvironment.isHeadless()) {
            String headlessMessage =
                    Constant.messages.getString("start.gui.headless", CommandLine.HELP);
            logger.fatal(headlessMessage);
            System.err.println(headlessMessage);
            return 1;
        }

        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        startImpl();
                    }
                });
        return 0;
    }

    private void startImpl() {
        setX11AwtAppClassName();
        setDefaultViewLocale(Constant.getLocale());

        init();
    }

    private void setX11AwtAppClassName() {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        // See JDK-6528430 : need system property to override default WM_CLASS
        //     http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6528430
        // Based on NetBeans workaround linked from the issue:
        Class<?> toolkitClass = defaultToolkit.getClass();
        if ("sun.awt.X11.XToolkit".equals(toolkitClass.getName())) {
            try {
                Field awtAppClassName = toolkitClass.getDeclaredField("awtAppClassName");
                awtAppClassName.setAccessible(true);
                awtAppClassName.set(null, Constant.PROGRAM_NAME);
            } catch (Exception e) {
                logger.warn("Failed to set awt app class name: {}", e.getMessage());
            }
        }
    }

    /** Initialises the {@code Model}, {@code View} and {@code Control}. */
    private void init() {
        try {
            initModel();
            setupLookAndFeel();
        } catch (Exception e) {
            setupLookAndFeel();
            if (e instanceof FileNotFoundException) {
                JOptionPane.showMessageDialog(
                        null,
                        Constant.messages.getString("start.db.error"),
                        Constant.messages.getString("start.title.error"),
                        JOptionPane.ERROR_MESSAGE);
            }
            logger.fatal("Failed to initialise: {}", e.getMessage(), e);
            System.err.println(e.getMessage());
            System.exit(1);
        }

        UIManager.put("PasswordField.showRevealButton", true);

        OptionsParam options = Model.getSingleton().getOptionsParam();
        OptionsParamView viewParam = options.getViewParam();

        for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
            FontUtils.setDefaultFont(
                    fontType, viewParam.getFontName(fontType), viewParam.getFontSize(fontType));
        }

        setupLocale(options);

        if (viewParam.isUseSystemsLocaleForFormat()) {
            Locale.setDefault(Locale.Category.FORMAT, Constant.getSystemsLocale());
        }

        View.getSingleton().showSplashScreen();

        Thread bootstrap =
                new Thread(
                        new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    initControlAndPostViewInit();

                                } catch (IllegalStateException e) {
                                    JOptionPane.showMessageDialog(
                                            View.getSingleton().getSplashScreen(),
                                            Constant.messages.getString(
                                                    "start.gui.dialog.fatal.error.init"),
                                            Constant.PROGRAM_NAME,
                                            JOptionPane.ERROR_MESSAGE);
                                    System.exit(1);
                                } catch (Throwable e) {
                                    if (!Constant.isDevMode()) {
                                        ErrorInfo errorInfo =
                                                new ErrorInfo(
                                                        Constant.messages.getString(
                                                                "start.gui.dialog.fatal.error.title"),
                                                        Constant.messages.getString(
                                                                "start.gui.dialog.fatal.error.message"),
                                                        null,
                                                        null,
                                                        e,
                                                        null,
                                                        null);
                                        JXErrorPane errorPane = new JXErrorPane();
                                        errorPane.setErrorInfo(errorInfo);
                                        JXErrorPane.showDialog(
                                                View.getSingleton().getSplashScreen(), errorPane);
                                    }
                                    View.getSingleton().hideSplashScreen();

                                    logger.fatal("Failed to initialise GUI: ", e);

                                    // We must exit otherwise EDT would keep ZAP running.
                                    System.exit(1);
                                }

                                warnAddOnsAndExtensionsNoLongerRunnable();

                                HeadlessBootstrap.checkForUpdates();
                            }
                        });
        bootstrap.setName("ZAP-BootstrapGUI");
        bootstrap.setDaemon(false);
        bootstrap.start();
    }

    /**
     * Initialises the {@code Control} and does post {@code View} initialisations.
     *
     * @throws Exception if an error occurs during initialisation
     * @see Control
     * @see View
     */
    private void initControlAndPostViewInit() throws Exception {
        Control.initSingletonWithView(getControlOverrides());

        final Control control = Control.getSingleton();
        final View view = View.getSingleton();

        EventQueue.invokeAndWait(
                new Runnable() {

                    @Override
                    public void run() {
                        view.postInit();
                        view.getMainFrame().setVisible(true);

                        boolean createNewSession = true;
                        if (getArgs().isEnabled(CommandLine.SESSION)
                                && getArgs().isEnabled(CommandLine.NEW_SESSION)) {
                            view.showWarningDialog(
                                    Constant.messages.getString(
                                            "start.gui.cmdline.invalid.session.options",
                                            CommandLine.SESSION,
                                            CommandLine.NEW_SESSION,
                                            Constant.getZapHome()));

                        } else if (getArgs().isEnabled(CommandLine.SESSION)) {
                            String path = getArgs().getArgument(CommandLine.SESSION);
                            Path sessionPath = null;
                            try {
                                sessionPath = SessionUtils.getSessionPath(path);
                            } catch (IllegalArgumentException e) {
                                logger.warn(
                                        "An error occurred while resolving the session path:", e);
                            }

                            if (sessionPath == null) {
                                view.showWarningDialog(
                                        Constant.messages.getString(
                                                "start.gui.cmdline.session.path.invalid", path));
                            } else if (!Files.exists(sessionPath)) {
                                view.showWarningDialog(
                                        Constant.messages.getString(
                                                "start.gui.cmdline.session.does.not.exist",
                                                Constant.getZapHome()));

                            } else {
                                createNewSession =
                                        !control.getMenuFileControl()
                                                .openSession(
                                                        sessionPath.toAbsolutePath().toString());
                            }

                        } else if (getArgs().isEnabled(CommandLine.NEW_SESSION)) {
                            String path = getArgs().getArgument(CommandLine.NEW_SESSION);
                            Path sessionPath = null;
                            try {
                                sessionPath = SessionUtils.getSessionPath(path);
                            } catch (IllegalArgumentException e) {
                                logger.warn(
                                        "An error occurred while resolving the session path:", e);
                            }

                            if (sessionPath == null) {
                                view.showWarningDialog(
                                        Constant.messages.getString(
                                                "start.gui.cmdline.session.path.invalid", path));
                            } else if (Files.exists(sessionPath)) {
                                view.showWarningDialog(
                                        Constant.messages.getString(
                                                "start.gui.cmdline.newsession.already.exist",
                                                Constant.getZapHome()));

                            } else {
                                createNewSession =
                                        !control.getMenuFileControl()
                                                .newSession(
                                                        sessionPath.toAbsolutePath().toString());
                            }
                        }
                        view.hideSplashScreen();

                        if (createNewSession) {
                            try {
                                control.getMenuFileControl().newSession(false);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                View.getSingleton()
                                        .showWarningDialog(
                                                Constant.messages.getString(
                                                        "menu.file.newSession.error"));
                            }
                        }
                    }
                });

        try {
            // Allow extensions to pick up command line args in GUI mode
            control.getExtensionLoader().hookCommandLineListener(getArgs());
            control.runCommandLine();

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            view.showWarningDialog(e.getMessage());
                        }
                    });
        }
    }

    /**
     * Sets the default {@code Locale} for Swing components.
     *
     * @param locale the locale that will be set as default locale for Swing components
     * @see JComponent#setDefaultLocale(Locale)
     */
    private static void setDefaultViewLocale(Locale locale) {
        JComponent.setDefaultLocale(locale);
    }

    /**
     * Setups Swing's look and feel.
     *
     * <p><strong>Note:</strong> Should be called only after calling {@link #initModel()}. The look
     * and feel set up might initialise some network classes (e.g. {@link java.net.InetAddress
     * InetAddress}) preventing some ZAP options from being correctly applied.
     */
    private void setupLookAndFeel() {
        if (lookAndFeelSet) {
            return;
        }
        lookAndFeelSet = true;

        UIManager.addAuxiliaryLookAndFeel(new ZapLookAndFeel());

        if (Constant.isMacOsX()) {
            OsXGui.setup();
        } else if (Constant.isWindows()) {
            UIManager.put("TitlePane.useWindowDecorations", false);
        }

        if (setLookAndFeel(System.getProperty("swing.defaultlaf"))) {
            return;
        }

        OptionsParam options = Model.getSingleton().getOptionsParam();

        if (setLookAndFeel(getLookAndFeelClassname(options.getViewParam().getLookAndFeel()))
                || setLookAndFeel(options.getViewParam().getLookAndFeelInfo().getClassName())) {
            return;
        }

        if (!Constant.isMacOsX() && setLookAndFeel(getLookAndFeelClassname("Nimbus"))) {
            return;
        }

        setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private static String getLookAndFeelClassname(String lookAndFeelName) {
        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        String lookAndFeelClassname = "";
        for (UIManager.LookAndFeelInfo look : looks) {
            if (look.getName().equals(lookAndFeelName)) {
                lookAndFeelClassname = look.getClassName();
                break;
            }
        }
        return lookAndFeelClassname;
    }

    private static boolean setLookAndFeel(String lookAndFeelClassname) {
        if (StringUtils.isNotEmpty(lookAndFeelClassname)) {
            try {
                UIManager.setLookAndFeel(lookAndFeelClassname);
                return true;
            } catch (final UnsupportedLookAndFeelException
                    | ClassNotFoundException
                    | InstantiationException
                    | IllegalAccessException e) {
                logger.warn("Failed to set the look and feel: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Setups ZAP's and GUI {@code Locale}, if not previously defined. Otherwise it's determined
     * automatically or, if not possible, by asking the user to choose one of the supported locales.
     *
     * @param options ZAP's options, used to check if a locale was already defined and save it if
     *     not.
     * @see #setDefaultViewLocale(Locale)
     * @see Constant#setLocale(String)
     */
    private void setupLocale(OptionsParam options) {
        // Prompt for language if not set
        String locale = options.getViewParam().getConfigLocale();
        if (locale == null || locale.length() == 0) {

            // Don't use a parent of the MainFrame - that will initialise it
            // with English!
            final Locale userloc = determineUsersSystemLocale();
            if (userloc == null) {
                // Only show the dialog, when the user's language can't be
                // guessed.
                setDefaultViewLocale(Constant.getSystemsLocale());
                final LocaleDialog dialog = new LocaleDialog(null, true);
                dialog.init(options);
                dialog.setVisible(true);

            } else {
                options.getViewParam().setLocale(userloc);
            }

            setDefaultViewLocale(createLocale(options.getViewParam().getLocale().split("_")));

            Constant.setLocale(Model.getSingleton().getOptionsParam().getViewParam().getLocale());
            try {
                options.getViewParam().getConfig().save();
            } catch (ConfigurationException e) {
                logger.warn("Failed to save locale: ", e);
            }
        }
    }

    /**
     * Determines the {@link Locale} of the current user's system.
     *
     * <p>It will match the {@link Constant#getSystemsLocale()} with the available locales from
     * ZAP's translation files.
     *
     * <p>It may return {@code null}, if the user's system locale is not in the list of available
     * translations of ZAP.
     *
     * @return the {@code Locale} that best matches the user's locale, or {@code null} if none found
     */
    private static Locale determineUsersSystemLocale() {
        Locale userloc = null;
        final Locale systloc = Constant.getSystemsLocale();
        // first, try full match
        for (String ls : LocaleUtils.getAvailableLocales()) {
            String[] langArray = ls.split("_");
            if (langArray.length == 1) {
                if (systloc.getLanguage().equals(langArray[0])) {
                    userloc = systloc;
                    break;
                }
            }

            if (langArray.length == 2) {
                if (systloc.getLanguage().equals(langArray[0])
                        && systloc.getCountry().equals(langArray[1])) {
                    userloc = systloc;
                    break;
                }
            }

            if (langArray.length == 3) {
                if (systloc.getLanguage().equals(langArray[0])
                        && systloc.getCountry().equals(langArray[1])
                        && systloc.getVariant().equals(langArray[2])) {
                    userloc = systloc;
                    break;
                }
            }
        }

        if (userloc == null) {
            // second, try partial language match
            for (String ls : LocaleUtils.getAvailableLocales()) {
                String[] langArray = ls.split("_");
                if (systloc.getLanguage().equals(langArray[0])) {
                    userloc = createLocale(langArray);
                    break;
                }
            }
        }

        return userloc;
    }

    private static Locale createLocale(String[] localeFields) {
        if (localeFields == null || localeFields.length == 0) {
            return null;
        }

        Locale.Builder localeBuilder = new Locale.Builder();
        localeBuilder.setLanguage(localeFields[0]);

        if (localeFields.length >= 2) {
            localeBuilder.setRegion(localeFields[1]);
        }

        if (localeFields.length >= 3) {
            localeBuilder.setVariant(localeFields[2]);
        }

        return localeBuilder.build();
    }

    /**
     * Warns, through a dialogue, about add-ons and extensions that are no longer runnable because
     * of changes in its dependencies.
     */
    private static void warnAddOnsAndExtensionsNoLongerRunnable() {
        final AddOnLoader addOnLoader = ExtensionFactory.getAddOnLoader();
        List<String> idsAddOnsNoLongerRunning =
                addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun();
        if (idsAddOnsNoLongerRunning.isEmpty()) {
            return;
        }

        List<AddOn> addOnsNoLongerRunning = new ArrayList<>(idsAddOnsNoLongerRunning.size());
        for (String id : idsAddOnsNoLongerRunning) {
            addOnsNoLongerRunning.add(addOnLoader.getAddOnCollection().getAddOn(id));
        }

        AddOnRunIssuesUtils.showWarningMessageAddOnsNotRunnable(
                Constant.messages.getString("start.gui.warn.addOnsOrExtensionsNoLongerRunning"),
                addOnLoader.getAddOnCollection(),
                addOnsNoLongerRunning);
    }
}
