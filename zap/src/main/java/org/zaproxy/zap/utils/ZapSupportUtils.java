/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.apache.commons.lang.WordUtils;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnLoader;
import org.zaproxy.zap.control.ExtensionFactory;

/**
 * A class containing utility methods that can be used for support purposes. The functions in this
 * class return details relevant for support and tickets prefixed with labels.
 *
 * @since 2.7.0
 */
public final class ZapSupportUtils {

    private static final String NEWLINE = System.lineSeparator();

    private ZapSupportUtils() {}

    public static String getProductName() {
        return Constant.PROGRAM_NAME;
    }

    public static String getVersion() {
        return Constant.messages.getString("support.version.label")
                + " "
                + Constant.PROGRAM_VERSION;
    }

    public static String getZapHomeDirectory() {
        return Constant.messages.getString("support.home.directory.label")
                + " "
                + Constant.getZapHome();
    }

    /**
     * Gets the installation directory (preceded with the corresponding label).
     *
     * @return the installation directory
     * @since 2.8.0
     */
    public static String getZapInstallDirectory() {
        return Constant.messages.getString("support.install.directory.label")
                + " "
                + Constant.getZapInstall();
    }

    public static String getOperatingSystem() {
        return Constant.messages.getString("support.operating.system.label")
                + " "
                + System.getProperty("os.name");
    }

    /**
     * Gets the architecture (preceded with the corresponding label).
     *
     * @return the architecture.
     * @since 2.12.0
     */
    public static String getArch() {
        return Constant.messages.getString("support.arch.label")
                + " "
                + System.getProperty("os.arch");
    }

    public static String getJavaVersionVendor() {
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        return Constant.messages.getString("support.java.version.label")
                + " "
                + javaVendor
                + " "
                + javaVersion;
    }

    public static String getLocaleDisplay() {
        return Constant.messages.getString("support.locale.display.label")
                + " "
                + Locale.getDefault(Locale.Category.DISPLAY);
    }

    public static String getLocaleFormat() {
        return Constant.messages.getString("support.locale.format.label")
                + " "
                + Locale.getDefault(Locale.Category.FORMAT);
    }

    public static String getLocaleSystem() {
        return Constant.messages.getString("support.locale.system.label")
                + " "
                + Constant.getSystemsLocale();
    }

    public static String getLookAndFeel() {
        LookAndFeel laf = UIManager.getLookAndFeel();
        String strLaf;
        if (laf != null) {
            strLaf = laf.getName() + " (" + laf.getClass().getCanonicalName() + ")";
        } else {
            strLaf = "[null]";
        }
        return Constant.messages.getString("support.laf.label") + " " + strLaf;
    }

    public static String getInstalledAddons() {
        AddOnLoader addOnLoader = ExtensionFactory.getAddOnLoader();
        List<AddOn> sortedAddOns =
                new ArrayList<>(addOnLoader.getAddOnCollection().getInstalledAddOns());
        Collections.sort(
                sortedAddOns,
                new Comparator<AddOn>() {

                    @Override
                    public int compare(AddOn addOn, AddOn otherAddOn) {
                        return addOn.getId().compareTo(otherAddOn.getId());
                    }
                });
        return Constant.messages.getString("support.installed.addons.label") + " " + sortedAddOns;
    }

    /**
     * Gets the default charset (preceded with the corresponding label).
     *
     * @return the default charset.
     * @since 2.12.0
     */
    public static String getDefaultCharset() {
        return Constant.messages.getString("support.charset.default.label")
                + " "
                + Charset.defaultCharset();
    }

    public static String getAll(boolean formatted) {
        StringBuilder installedAddons = new StringBuilder(200);
        if (formatted) {
            installedAddons.append(NEWLINE);
            installedAddons.append(WordUtils.wrap(getInstalledAddons(), 60)).append(NEWLINE);
            installedAddons.append(NEWLINE);
        } else {
            installedAddons.append(getInstalledAddons()).append(NEWLINE);
        }

        StringBuilder supportDetailsBuilder = new StringBuilder(300);

        supportDetailsBuilder.append(getProductName()).append(NEWLINE);
        supportDetailsBuilder.append(getVersion()).append(NEWLINE);
        supportDetailsBuilder.append(installedAddons);
        supportDetailsBuilder.append(getOperatingSystem()).append(NEWLINE);
        supportDetailsBuilder.append(getArch()).append(NEWLINE);
        supportDetailsBuilder.append(getJavaVersionVendor()).append(NEWLINE);
        supportDetailsBuilder.append(getLocaleSystem()).append(NEWLINE);
        supportDetailsBuilder.append(getLocaleDisplay()).append(NEWLINE);
        supportDetailsBuilder.append(getLocaleFormat()).append(NEWLINE);
        supportDetailsBuilder.append(getDefaultCharset()).append(NEWLINE);
        supportDetailsBuilder.append(getZapHomeDirectory()).append(NEWLINE);
        supportDetailsBuilder.append(getZapInstallDirectory()).append(NEWLINE);
        supportDetailsBuilder.append(getLookAndFeel()).append(NEWLINE);

        return supportDetailsBuilder.toString();
    }
}
