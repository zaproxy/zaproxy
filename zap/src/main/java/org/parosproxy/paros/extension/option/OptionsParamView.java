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
// ZAP: 2011/06/02 Warn the first time the user double clicks on a tab
// ZAP: 2012/03/15 Removed the options of the http panels.
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2013/12/13 Added support for optional names in tabs.
// ZAP: 2014/03/23 Issue 589: Move Reveal extension to ZAP extensions project
// ZAP: 2014/04/25 Issue 642: Add timestamps to Output tab(s)
// ZAP: 2014/10/07 Issue 1357: Hide unused tabs
// ZAP: 2014/10/09 Issue 1359: Options for splash screen
// ZAP: 2014/12/16 Issue 1466: Config option for 'large display' size
// ZAP: 2015/03/04 Added dev build warning option
// ZAP: 2016/04/04 Do not require a restart to show/hide the tool bar
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2016/04/27 Save, always, the Locale as String
// ZAP: 2016/05/13 Add options to confirm removal of exclude from proxy, scanner and spider regexes
// ZAP: 2017/05/29 Add option to use system's locale for formatting.
// ZAP: 2017/09/26 Use helper methods to read the configurations.
// ZAP: 2018/01/25 Remove unused constant LOCALES.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/02/27 Added support for selecting the look and feel.
// ZAP: 2018/06/11 Added options for Work Panels Font.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/02/24 Persist the class of the selected look and feel.
// ZAP: 2020/09/29 Add support for dynamic Look and Feel switching (Issue 6201)
// ZAP: 2020/10/26 Update pop up menus when changing look and feel.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/09/16 Add support for enabling app integration in containers
// ZAP: 2022/02/25 Deprecate options no longer in use.
// ZAP: 2022/02/26 Remove code deprecated in 2.5.0
// ZAP: 2022/04/28 Add set and get of the open recent menu
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.extension.option;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;

// ZAP: Added support for selecting the locale

public class OptionsParamView extends AbstractParam {

    private static final Logger LOG = LogManager.getLogger(OptionsParamView.class);

    private static final String DEFAULT_TIME_STAMP_FORMAT =
            Constant.messages.getString("timestamp.format.datetime");

    public static final String BASE_VIEW_KEY = "view";

    private static final String SHOW_TEXT_ICONS = "view.showTabNames";
    private static final String PROCESS_IMAGES = "view.processImages";
    public static final String LOCALE = "view.locale";
    public static final String DISPLAY_OPTION = "view.displayOption";
    private static final String RESPONSE_PANEL_POS_KEY =
            BASE_VIEW_KEY + ".messagePanelsPosition.lastSelectedPosition";
    public static final String BRK_PANEL_VIEW_OPTION = "view.brkPanelView";
    public static final String SHOW_MAIN_TOOLBAR_OPTION = "view.showMainToolbar";
    public static final String DEFAULT_LOCALE = "en_GB";
    public static final String ADVANCEDUI_OPTION = "view.advancedview";
    public static final String WMUIHANDLING_OPTION = "view.uiWmHandling";
    public static final String ASKONEXIT_OPTION = "view.askOnExit";
    public static final String WARN_ON_TAB_DOUBLE_CLICK_OPTION = "view.warnOnTabDoubleClick";
    public static final String MODE_OPTION = "view.mode";
    public static final String TAB_PIN_OPTION = "view.tab.pin";
    public static final String OUTPUT_TAB_TIMESTAMPING_OPTION = "view.outputTabsTimeStampsOption";
    public static final String OUTPUT_TAB_TIMESTAMP_FORMAT = "view.outputTabsTimeStampsFormat";

    /** The configuration key used to save/load the option {@link #showLocalConnectRequests}. */
    private static final String SHOW_LOCAL_CONNECT_REQUESTS = "view.showLocalConnectRequests";

    /** The configuration key used to save/load the option {@link #useSystemsLocaleForFormat}. */
    private static final String USE_SYSTEMS_LOCALE_FOR_FORMAT_KEY =
            BASE_VIEW_KEY + ".usesystemslocaleformat";

    public static final String SPLASHSCREEN_OPTION = "view.splashScreen";
    /** @deprecated (2.12.0) No longer in use. */
    @Deprecated public static final String LARGE_REQUEST_SIZE = "view.largeRequest";
    /** @deprecated (2.12.0) No longer in use. */
    @Deprecated public static final String LARGE_RESPONSE_SIZE = "view.largeResponse";

    public static final String FONT_NAME = "view.fontName";
    public static final String FONT_SIZE = "view.fontSize";
    public static final String SCALE_IMAGES = "view.scaleImages";
    public static final String SHOW_DEV_WARNING = "view.showDevWarning";
    public static final String LOOK_AND_FEEL = "view.lookAndFeel";
    public static final String LOOK_AND_FEEL_CLASS = "view.lookAndFeelClass";
    public static final String ALLOW_APP_INTEGRATION_IN_CONTAINERS = "view.allowAppsInContainers";

    /**
     * The default look and feel: Flat Light.
     *
     * @since 2.10.0
     */
    public static final String DEFAULT_LOOK_AND_FEEL_NAME = "Flat Light";

    public static final String DEFAULT_LOOK_AND_FEEL_CLASS = "com.formdev.flatlaf.FlatLightLaf";
    public static final LookAndFeelInfo DEFAULT_LOOK_AND_FEEL =
            new LookAndFeelInfo(DEFAULT_LOOK_AND_FEEL_NAME, DEFAULT_LOOK_AND_FEEL_CLASS);

    private static final String CONFIRM_REMOVE_PROXY_EXCLUDE_REGEX_KEY =
            "view.confirmRemoveProxyExcludeRegex";
    private static final String CONFIRM_REMOVE_SCANNER_EXCLUDE_REGEX_KEY =
            "view.confirmRemoveScannerExcludeRegex";
    private static final String CONFIRM_REMOVE_SPIDER_EXCLUDE_REGEX_KEY =
            "view.confirmRemoveSpiderExcludeRegex";
    private static final String FONT_NAME_POSTFIX = "Name";
    private static final String FONT_SIZE_POSTFIX = "Size";
    private static final String RECENT_SESSIONS_KEY = BASE_VIEW_KEY + ".recentsessions.path";

    private int advancedViewEnabled = 0;
    private int processImages = 0;
    private int showMainToolbar = 1;
    private String configLocale = "";
    private String locale = "";
    private int displayOption = 1;
    private String responsePanelPosition;
    private int brkPanelViewOption = 0;
    private int askOnExitEnabled = 1;
    private int wmUiHandlingEnabled = 0;
    private boolean warnOnTabDoubleClick = false;
    private boolean showTabNames = true;
    private String mode = Mode.standard.name();
    private boolean outputTabTimeStampingEnabled = false;
    private String outputTabTimeStampFormat = DEFAULT_TIME_STAMP_FORMAT;
    private Map<FontUtils.FontType, String> fontTypePrefixes =
            new EnumMap<>(FontUtils.FontType.class);
    private Map<FontUtils.FontType, Integer> fontSizes = new EnumMap<>(FontUtils.FontType.class);
    private Map<FontUtils.FontType, String> fontNames = new EnumMap<>(FontUtils.FontType.class);
    private List<String> recentSessions;

    /**
     * Flag that indicates if the HTTP CONNECT requests received by the local proxy should be
     * (persisted and) shown in the UI.
     *
     * @see #SHOW_LOCAL_CONNECT_REQUESTS
     * @see #isShowLocalConnectRequests()
     * @see #setShowLocalConnectRequests(boolean)
     */
    private boolean showLocalConnectRequests;

    private boolean showSplashScreen = true;
    private boolean scaleImages = true;
    private boolean showDevWarning = true;
    private LookAndFeelInfo lookAndFeelInfo = DEFAULT_LOOK_AND_FEEL;
    private boolean allowAppIntegrationInContainers;

    private boolean confirmRemoveProxyExcludeRegex;
    private boolean confirmRemoveScannerExcludeRegex;
    private boolean confirmRemoveSpiderExcludeRegex;

    /**
     * Flag that indicates if the system's locale should be used for formatting.
     *
     * @see #USE_SYSTEMS_LOCALE_FOR_FORMAT_KEY
     * @see #isUseSystemsLocaleForFormat()
     * @see #setUseSystemsLocaleForFormat(boolean)
     */
    private boolean useSystemsLocaleForFormat;

    public OptionsParamView() {
        fontTypePrefixes.put(FontUtils.FontType.general, "font");
        fontTypePrefixes.put(FontUtils.FontType.workPanels, "workPanelsFont");

        for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
            fontNames.put(fontType, "");
            fontSizes.put(fontType, -1);
        }
    }

    @Override
    protected void parse() {
        showTabNames = getBoolean(SHOW_TEXT_ICONS, true);
        processImages = getInt(PROCESS_IMAGES, 0);
        configLocale = getString(LOCALE, null); // No default
        locale = getString(LOCALE, DEFAULT_LOCALE);
        useSystemsLocaleForFormat = getBoolean(USE_SYSTEMS_LOCALE_FOR_FORMAT_KEY, true);
        displayOption = getInt(DISPLAY_OPTION, 1);
        responsePanelPosition =
                getString(
                        RESPONSE_PANEL_POS_KEY,
                        WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE.name());
        brkPanelViewOption = getInt(BRK_PANEL_VIEW_OPTION, 0);
        showMainToolbar = getInt(SHOW_MAIN_TOOLBAR_OPTION, 1);
        advancedViewEnabled = getInt(ADVANCEDUI_OPTION, 0);
        wmUiHandlingEnabled = getInt(WMUIHANDLING_OPTION, 0);
        askOnExitEnabled = getInt(ASKONEXIT_OPTION, 1);
        warnOnTabDoubleClick = getBoolean(WARN_ON_TAB_DOUBLE_CLICK_OPTION, true);
        mode = getString(MODE_OPTION, Mode.standard.name());
        outputTabTimeStampingEnabled = getBoolean(OUTPUT_TAB_TIMESTAMPING_OPTION, false);
        outputTabTimeStampFormat =
                getString(OUTPUT_TAB_TIMESTAMP_FORMAT, DEFAULT_TIME_STAMP_FORMAT);

        showLocalConnectRequests = getBoolean(SHOW_LOCAL_CONNECT_REQUESTS, false);

        showSplashScreen = getBoolean(SPLASHSCREEN_OPTION, true);

        for (FontUtils.FontType fontType : FontUtils.FontType.values()) {
            fontNames.put(fontType, getString(getFontNameConfKey(fontType), ""));
            fontSizes.put(fontType, getInt(getFontSizeConfKey(fontType), -1));
        }

        scaleImages = getBoolean(SCALE_IMAGES, true);
        showDevWarning = getBoolean(SHOW_DEV_WARNING, true);
        lookAndFeelInfo =
                new LookAndFeelInfo(
                        getString(LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL.getName()),
                        getString(LOOK_AND_FEEL_CLASS, DEFAULT_LOOK_AND_FEEL.getClassName()));

        allowAppIntegrationInContainers = getBoolean(ALLOW_APP_INTEGRATION_IN_CONTAINERS, false);

        this.confirmRemoveProxyExcludeRegex =
                getBoolean(CONFIRM_REMOVE_PROXY_EXCLUDE_REGEX_KEY, false);

        this.confirmRemoveScannerExcludeRegex =
                getBoolean(CONFIRM_REMOVE_SCANNER_EXCLUDE_REGEX_KEY, false);

        this.confirmRemoveSpiderExcludeRegex =
                getBoolean(CONFIRM_REMOVE_SPIDER_EXCLUDE_REGEX_KEY, false);

        recentSessions = new ArrayList<>();
        Stream.of(getConfig().getStringArray(RECENT_SESSIONS_KEY)).forEach(recentSessions::add);
    }

    /** @return Returns the skipImage. */
    public int getProcessImages() {
        return processImages;
    }

    /** @param processImages 0 = not to process. Other = process images */
    public void setProcessImages(int processImages) {
        this.processImages = processImages;
        getConfig().setProperty(PROCESS_IMAGES, Integer.toString(processImages));
    }

    public boolean isProcessImages() {
        return !(processImages == 0);
    }

    /**
     * Tells whether or not the main tool bar should be shown.
     *
     * @return {@code true} if the main tool bar should be shown, {@code false} otherwise.
     * @since 2.5.0
     */
    public boolean isShowMainToolbar() {
        return showMainToolbar != 0;
    }

    /**
     * Sets whether or not the main tool bar should be shown.
     *
     * @param show {@code true} if the main tool bar should be shown, {@code false} otherwise.
     * @since 2.5.0
     */
    public void setShowMainToolbar(boolean show) {
        this.showMainToolbar = show ? 1 : 0;
        getConfig().setProperty(SHOW_MAIN_TOOLBAR_OPTION, showMainToolbar);
    }

    /**
     * @return the locale, which should be used. It will return a default value, if nothing was
     *     configured yet. Never null
     * @see #getConfigLocale()
     */
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        if (locale != null) {
            this.locale = locale;
            getConfig().setProperty(LOCALE, locale);
        }
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(locale.getLanguage());
            if (locale.getCountry().length() > 0) sb.append("_").append(locale.getCountry());
            if (locale.getVariant().length() > 0) sb.append("_").append(locale.getVariant());
            setLocale(sb.toString());
        }
    }

    /**
     * @return The really configured locale, can be null
     * @see #getLocale()
     */
    public String getConfigLocale() {
        return configLocale;
    }

    public boolean getShowTabNames() {
        return showTabNames;
    }

    public void setShowTabNames(boolean showTabNames) {
        this.showTabNames = showTabNames;
        getConfig().setProperty(SHOW_TEXT_ICONS, showTabNames);
    }

    public int getBrkPanelViewOption() {
        return brkPanelViewOption;
    }

    public void setBrkPanelViewOption(int brkPanelViewIdx) {
        brkPanelViewOption = brkPanelViewIdx;
        getConfig().setProperty(BRK_PANEL_VIEW_OPTION, Integer.toString(brkPanelViewOption));
    }

    public int getDisplayOption() {
        return displayOption;
    }

    public void setDisplayOption(int displayOption) {
        this.displayOption = displayOption;
        getConfig().setProperty(DISPLAY_OPTION, Integer.toString(displayOption));
    }

    /**
     * Gets the name of the current response panel position.
     *
     * @return the name of the current position
     * @since 2.5.0
     * @see org.parosproxy.paros.view.WorkbenchPanel.ResponsePanelPosition
     */
    public String getResponsePanelPosition() {
        return responsePanelPosition;
    }

    /**
     * Sets the name of the current response panel position.
     *
     * @param position the name of the position
     * @since 2.5.0
     */
    public void setResponsePanelPosition(String position) {
        this.responsePanelPosition = position;
        getConfig().setProperty(RESPONSE_PANEL_POS_KEY, position);
    }

    public int getAdvancedViewOption() {
        return advancedViewEnabled;
    }

    public void setAdvancedViewOption(int isEnabled) {
        advancedViewEnabled = isEnabled;
        getConfig().setProperty(ADVANCEDUI_OPTION, Integer.toString(isEnabled));
    }

    public void setAskOnExitOption(int isEnabled) {
        askOnExitEnabled = isEnabled;
        getConfig().setProperty(ASKONEXIT_OPTION, Integer.toString(isEnabled));
    }

    public int getAskOnExitOption() {
        return askOnExitEnabled;
    }

    public void setWmUiHandlingOption(int isEnabled) {
        wmUiHandlingEnabled = isEnabled;
        getConfig().setProperty(WMUIHANDLING_OPTION, Integer.toString(isEnabled));
    }

    public int getWmUiHandlingOption() {
        return wmUiHandlingEnabled;
    }

    public boolean getWarnOnTabDoubleClick() {
        return warnOnTabDoubleClick;
    }

    public void setWarnOnTabDoubleClick(boolean warnOnTabDoubleClick) {
        this.warnOnTabDoubleClick = warnOnTabDoubleClick;
        getConfig().setProperty(WARN_ON_TAB_DOUBLE_CLICK_OPTION, warnOnTabDoubleClick);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        getConfig().setProperty(MODE_OPTION, mode);
    }

    public void setOutputTabTimeStampingEnabled(boolean enabled) {
        outputTabTimeStampingEnabled = enabled;
        getConfig().setProperty(OUTPUT_TAB_TIMESTAMPING_OPTION, enabled);
    }

    public boolean isOutputTabTimeStampingEnabled() {
        return outputTabTimeStampingEnabled;
    }

    public void setOutputTabTimeStampsFormat(String format) {
        outputTabTimeStampFormat = format;
        getConfig().setProperty(OUTPUT_TAB_TIMESTAMP_FORMAT, format);
    }

    public String getOutputTabTimeStampsFormat() {
        return outputTabTimeStampFormat;
    }

    /**
     * Sets whether or not the HTTP CONNECT requests received by the local proxy should be
     * (persisted and) shown in the UI.
     *
     * @param showConnectRequests {@code true} if the HTTP CONNECT requests should be shown, {@code
     *     false} otherwise
     * @since 2.5.0
     * @see #isShowLocalConnectRequests()
     */
    public void setShowLocalConnectRequests(boolean showConnectRequests) {
        if (showLocalConnectRequests != showConnectRequests) {
            showLocalConnectRequests = showConnectRequests;
            getConfig().setProperty(SHOW_LOCAL_CONNECT_REQUESTS, showConnectRequests);
        }
    }

    /**
     * Tells whether or not the HTTP CONNECT requests received by the local proxy should be
     * (persisted and) shown in the UI.
     *
     * <p>The default is to not show the HTTP CONNECT requests.
     *
     * @return {@code true} if the HTTP CONNECT requests should be shown, {@code false} otherwise
     * @since 2.5.0
     * @see #setShowLocalConnectRequests(boolean)
     */
    public boolean isShowLocalConnectRequests() {
        return showLocalConnectRequests;
    }

    public boolean isShowSplashScreen() {
        return showSplashScreen;
    }

    public void setShowSplashScreen(boolean showSplashScreen) {
        this.showSplashScreen = showSplashScreen;
        getConfig().setProperty(SPLASHSCREEN_OPTION, showSplashScreen);
    }

    /** @deprecated (2.12.0) No longer in use. */
    @Deprecated
    public int getLargeRequestSize() {
        return 100000;
    }

    /** @deprecated (2.12.0) No longer in use. */
    @Deprecated
    public void setLargeRequestSize(int largeRequestSize) {}

    /** @deprecated (2.12.0) No longer in use. */
    @Deprecated
    public int getLargeResponseSize() {
        return 100000;
    }

    /** @deprecated (2.12.0) No longer in use. */
    @Deprecated
    public void setLargeResponseSize(int largeResponseSize) {}

    /** @since 2.11.0 */
    public boolean isAllowAppIntegrationInContainers() {
        return allowAppIntegrationInContainers;
    }

    /** @since 2.11.0 */
    public void setAllowAppIntegrationInContainers(boolean allowAppIntegrationInContainers) {
        this.allowAppIntegrationInContainers = allowAppIntegrationInContainers;
        getConfig()
                .setProperty(ALLOW_APP_INTEGRATION_IN_CONTAINERS, allowAppIntegrationInContainers);
    }

    /**
     * @deprecated (2.8.0) Replaced by {@link
     *     #getFontSize(org.zaproxy.zap.utils.FontUtils.FontType)}.
     */
    @Deprecated
    public int getFontSize() {
        return getFontSize(FontUtils.FontType.general);
    }

    public int getFontSize(FontUtils.FontType fontType) {
        return fontSizes.get(fontType);
    }

    /**
     * @deprecated (2.8.0) Replaced by {@link #setFontSize(org.zaproxy.zap.utils.FontUtils.FontType,
     *     int)}.
     */
    @Deprecated
    public void setFontSize(int fontSize) {
        setFontSize(FontUtils.FontType.general, fontSize);
    }

    public void setFontSize(FontUtils.FontType fontType, int fontSize) {
        fontSizes.put(fontType, fontSize);
        getConfig().setProperty(getFontSizeConfKey(fontType), fontSize);
    }

    /**
     * @deprecated (2.8.0) Replaced by {@link
     *     #getFontName(org.zaproxy.zap.utils.FontUtils.FontType)}.
     */
    @Deprecated
    public String getFontName() {
        return getFontName(FontUtils.FontType.general);
    }

    public String getFontName(FontUtils.FontType fontType) {
        return fontNames.get(fontType);
    }

    /**
     * @deprecated (2.8.0) Replaced by {@link #setFontName(org.zaproxy.zap.utils.FontUtils.FontType,
     *     String)}.
     */
    @Deprecated
    public void setFontName(String fontName) {
        setFontName(FontUtils.FontType.general, fontName);
    }

    public void setFontName(FontUtils.FontType fontType, String fontName) {
        fontNames.put(fontType, fontName);
        getConfig().setProperty(getFontNameConfKey(fontType), fontName);
    }

    /**
     * Gets the the name of the selected look and feel.
     *
     * @return the name, might be {@code null} or empty if none selected (i.e. using default).
     * @see #getLookAndFeelInfo()
     * @since 2.8.0
     */
    public String getLookAndFeel() {
        return this.lookAndFeelInfo.getName();
    }

    /**
     * Sets the name of the selected look and feel.
     *
     * @param lookAndFeel the name.
     * @since 2.8.0
     * @deprecated (2.10.0) Use {@link #setLookAndFeelInfo(LookAndFeelInfo)} instead, which
     *     preserves the class of the look and feel.
     */
    @Deprecated
    public void setLookAndFeel(String lookAndFeel) {
        setLookAndFeelInfo(new LookAndFeelInfo(lookAndFeel, ""));
    }

    /**
     * Gets the info of the selected look and feel.
     *
     * @return the info of the look and feel.
     * @since 2.10.0
     * @see #getLookAndFeel()
     */
    public LookAndFeelInfo getLookAndFeelInfo() {
        return this.lookAndFeelInfo;
    }

    /**
     * Sets the info of the selected look and feel.
     *
     * @param lookAndFeelInfo the info of the look and feel.
     * @throws NullPointerException if the given parameter is null.
     * @since 2.10.0
     */
    public void setLookAndFeelInfo(LookAndFeelInfo lookAndFeelInfo) {
        LookAndFeelInfo oldLookAndFeel = this.lookAndFeelInfo;
        this.lookAndFeelInfo = Objects.requireNonNull(lookAndFeelInfo);

        if (!oldLookAndFeel.getClassName().equals(this.getLookAndFeelInfo().getClassName())) {
            // Only dynamically apply the LaF if its changed
            getConfig().setProperty(LOOK_AND_FEEL, lookAndFeelInfo.getName());
            getConfig().setProperty(LOOK_AND_FEEL_CLASS, lookAndFeelInfo.getClassName());

            if (View.isInitialised()) {
                final JDialog dialog = new SwitchingLookAndFeelDialog();
                dialog.setVisible(true);
                // Wait for 1/2 sec to allow the warning dialog to be rendered
                Timer timer =
                        new Timer(
                                500,
                                e -> {
                                    try {
                                        UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                                        Arrays.asList(Window.getWindows()).stream()
                                                .forEach(SwingUtilities::updateComponentTreeUI);
                                        View.getSingleton()
                                                .getPopupList()
                                                .forEach(SwingUtilities::updateComponentTreeUI);
                                    } catch (Exception e2) {
                                        LOG.warn(
                                                "Failed to set the look and feel: {}",
                                                e2.getMessage());
                                    } finally {
                                        dialog.setVisible(false);
                                        dialog.dispose();
                                    }
                                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    public boolean isScaleImages() {
        return scaleImages;
    }

    public void setScaleImages(boolean scaleImages) {
        this.scaleImages = scaleImages;
        getConfig().setProperty(SCALE_IMAGES, scaleImages);
    }

    public boolean isShowDevWarning() {
        return showDevWarning;
    }

    public void setShowDevWarning(boolean showDevWarning) {
        this.showDevWarning = showDevWarning;
        getConfig().setProperty(SHOW_DEV_WARNING, showDevWarning);
    }

    public boolean isConfirmRemoveProxyExcludeRegex() {
        return this.confirmRemoveProxyExcludeRegex;
    }

    public void setConfirmRemoveProxyExcludeRegex(boolean confirmRemove) {
        this.confirmRemoveProxyExcludeRegex = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_PROXY_EXCLUDE_REGEX_KEY, confirmRemove);
    }

    public boolean isConfirmRemoveScannerExcludeRegex() {
        return this.confirmRemoveScannerExcludeRegex;
    }

    public void setConfirmRemoveScannerExcludeRegex(boolean confirmRemove) {
        this.confirmRemoveScannerExcludeRegex = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_SCANNER_EXCLUDE_REGEX_KEY, confirmRemove);
    }

    public boolean isConfirmRemoveSpiderExcludeRegex() {
        return this.confirmRemoveSpiderExcludeRegex;
    }

    public void setConfirmRemoveSpiderExcludeRegex(boolean confirmRemove) {
        this.confirmRemoveSpiderExcludeRegex = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_SPIDER_EXCLUDE_REGEX_KEY, confirmRemove);
    }

    /**
     * Sets whether or not the system's locale should be used for formatting.
     *
     * @param useSystemsLocale {@code true} if the system's locale should be used for formatting,
     *     {@code false} otherwise.
     * @since 2.7.0
     * @see #isUseSystemsLocaleForFormat()
     * @see java.util.Locale.Category#FORMAT
     */
    public void setUseSystemsLocaleForFormat(boolean useSystemsLocale) {
        if (useSystemsLocaleForFormat != useSystemsLocale) {
            useSystemsLocaleForFormat = useSystemsLocale;
            getConfig().setProperty(USE_SYSTEMS_LOCALE_FOR_FORMAT_KEY, useSystemsLocaleForFormat);
        }
    }

    /**
     * Tells whether or not the system's locale should be used for formatting.
     *
     * @return {@code true} if the system's locale should be used for formatting, {@code false}
     *     otherwise.
     * @since 2.7.0
     * @see #setUseSystemsLocaleForFormat(boolean)
     * @see java.util.Locale.Category#FORMAT
     */
    public boolean isUseSystemsLocaleForFormat() {
        return useSystemsLocaleForFormat;
    }

    private String getFontConfKey(FontUtils.FontType fontType, String postfix) {
        StringBuilder result = new StringBuilder();
        result.append("view.");
        result.append(fontTypePrefixes.get(fontType));
        result.append(postfix);
        return result.toString();
    }

    private String getFontNameConfKey(FontUtils.FontType fontType) {
        return getFontConfKey(fontType, FONT_NAME_POSTFIX);
    }

    private String getFontSizeConfKey(FontUtils.FontType fontType) {
        return getFontConfKey(fontType, FONT_SIZE_POSTFIX);
    }

    private static class SwitchingLookAndFeelDialog extends AbstractDialog {
        private static final long serialVersionUID = 1L;
        private JPanel mainPanel;

        public SwitchingLookAndFeelDialog() {
            super(View.getSingleton().getMainFrame(), false);
            this.setContentPane(getMainPanel());
            this.pack();
        }

        private JPanel getMainPanel() {
            if (mainPanel == null) {
                mainPanel = new JPanel();
                mainPanel.add(
                        new ZapHtmlLabel(
                                Constant.messages.getString("view.options.warn.applylaf")));
            }
            return mainPanel;
        }
    }

    public List<String> getRecentSessions() {
        return recentSessions;
    }

    public void addLatestSession(String path) {
        int index = recentSessions.indexOf(path);
        if (index == 0) {
            return;
        }

        if (index > 0) {
            recentSessions.remove(index);
        }

        recentSessions.add(0, path);

        if (recentSessions.size() > 10) {
            recentSessions.subList(10, recentSessions.size()).clear();
        }

        getConfig().clearProperty(RECENT_SESSIONS_KEY);
        for (int i = 0; i < recentSessions.size(); ++i) {
            getConfig().setProperty(RECENT_SESSIONS_KEY + "(" + i + ")", recentSessions.get(i));
        }
    }
}
