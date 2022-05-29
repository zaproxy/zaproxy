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
// ZAP: 2011/10/29 Support for parameters
// ZAP: 2011/11/02 Added brute force options
// ZAP: 2011/11/15 Removed the getConfig method and the config field, now it's
// used the method of the base class.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/04/25 Added type argument to generic type.
// ZAP: 2012/05/03 Changed the type of one variable in the method getParamSet.
// ZAP: 2012/06/30 Added the instance variable databaseParam and the  method
// getDatabaseParam() and changed the method parse() to also load the database
// configurations.
// ZAP: 2012/12/31 Removed brute force options
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014-02-04 Added GlobalExcludeURL functionality:  Issue: TODO - insert list here.
// ZAP: 2014/03/23 Issue 1097: Move "Run applications" (invoke) extension to zap-extensions project
// ZAP: 2015/04/09 Generify getParamSet(Class) to avoid unnecessary casts
// ZAP: 2016/11/17 Issue 2701 Support Factory Reset
// ZAP: 2016/12/06 Add ExtensionParam
// ZAP: 2018/08/15 Move AntiCsrfParam to ExtensionAntiCSRF
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/25 Change the default value of userDirectory from null to the user's home directory.
// ZAP: 2022/02/09 Deprecate methods related to core proxy options.
// ZAP: 2022/05/20 Deprecate methods related to core connection options.
// ZAP: 2022/05/29 Deprecate methods related to core client certificates.
package org.parosproxy.paros.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfParam;
import org.zaproxy.zap.extension.api.OptionsParamApi;
import org.zaproxy.zap.extension.autoupdate.OptionsParamCheckForUpdates;
import org.zaproxy.zap.extension.ext.ExtensionParam;
import org.zaproxy.zap.extension.globalexcludeurl.GlobalExcludeURLParam;

public class OptionsParam extends AbstractParam {

    private static final Logger logger = LogManager.getLogger(OptionsParam.class);

    //	private static final String ROOT = "Options";
    // ZAP: User directory now stored in the config file
    private static final String USER_DIR = "userDir";

    @SuppressWarnings("deprecation")
    private org.parosproxy.paros.core.proxy.ProxyParam proxyParam =
            new org.parosproxy.paros.core.proxy.ProxyParam();

    @SuppressWarnings("deprecation")
    private org.parosproxy.paros.network.ConnectionParam connectionParam =
            new org.parosproxy.paros.network.ConnectionParam();

    private OptionsParamView viewParam = new OptionsParamView();

    @SuppressWarnings("deprecation")
    private org.parosproxy.paros.extension.option.OptionsParamCertificate certificateParam =
            new org.parosproxy.paros.extension.option.OptionsParamCertificate();
    // ZAP: Added many instance variables for new functionality.
    private OptionsParamCheckForUpdates checkForUpdatesParam = new OptionsParamCheckForUpdates();
    private OptionsParamApi apiParam = new OptionsParamApi();
    private GlobalExcludeURLParam globalExcludeURLParam = new GlobalExcludeURLParam();

    @SuppressWarnings("deprecation")
    private ch.csnc.extension.util.OptionsParamExperimentalSliSupport experimentalFeaturesParam =
            new ch.csnc.extension.util.OptionsParamExperimentalSliSupport();

    /** The database configurations. */
    // ZAP: Added the instance variable.
    private DatabaseParam databaseParam = new DatabaseParam();

    private ExtensionParam extensionParam = new ExtensionParam();

    private Vector<AbstractParam> paramSetList = new Vector<>();
    private Map<Class<? extends AbstractParam>, AbstractParam> abstractParamsMap = new HashMap<>();
    private boolean gui = true;
    private File userDirectory = SystemUtils.getUserHome();

    public OptionsParam() {}

    /**
     * @return Returns the connectionParam.
     * @deprecated (2.12.0) Use the network add-on instead.
     */
    @Deprecated
    public org.parosproxy.paros.network.ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    /**
     * @deprecated (2.12.0) Use the network add-on instead.
     * @return Returns the proxyParam.
     */
    @Deprecated
    public org.parosproxy.paros.core.proxy.ProxyParam getProxyParam() {
        return proxyParam;
    }

    /**
     * @deprecated (2.12.0) Use the network add-on instead.
     * @param proxyParam The proxyParam to set.
     */
    @Deprecated
    public void setProxyParam(org.parosproxy.paros.core.proxy.ProxyParam proxyParam) {
        this.proxyParam = proxyParam;
    }

    /**
     * @param connectionParam The connectionParam to set.
     * @deprecated (2.12.0)
     */
    @Deprecated
    public void setConnectionParam(org.parosproxy.paros.network.ConnectionParam connectionParam) {
        this.connectionParam = connectionParam;
    }

    /** @param viewParam The viewParam to set. */
    public void setViewParam(OptionsParamView viewParam) {
        this.viewParam = viewParam;
    }

    /** @return Returns the viewParam. */
    public OptionsParamView getViewParam() {
        return viewParam;
    }

    /** @return Returns the viewParam. */
    public OptionsParamCheckForUpdates getCheckForUpdatesParam() {
        return checkForUpdatesParam;
    }

    /**
     * @param certificateParam The certificateParam to set.
     * @deprecated (2.12.0)
     */
    @Deprecated
    public void setCertificateParam(
            org.parosproxy.paros.extension.option.OptionsParamCertificate certificateParam) {
        this.certificateParam = certificateParam;
    }

    /**
     * @return Returns the certificateParam.
     * @deprecated (2.12.0)
     */
    @Deprecated
    public org.parosproxy.paros.extension.option.OptionsParamCertificate getCertificateParam() {
        return certificateParam;
    }

    public void addParamSet(AbstractParam paramSet) {
        paramSetList.add(paramSet);
        abstractParamsMap.put(paramSet.getClass(), paramSet);
        paramSet.load(getConfig());
    }

    public void removeParamSet(AbstractParam paramSet) {
        paramSetList.remove(paramSet);
        abstractParamsMap.remove(paramSet.getClass());
    }

    public <T extends AbstractParam> T getParamSet(Class<T> clazz) {
        if (clazz != null) {
            AbstractParam abstractParam = abstractParamsMap.get(clazz);
            if (abstractParam != null) {
                return clazz.cast(abstractParam);
            }
        }
        return null;
    }

    // ZAP: Removed the method getConfig().

    @Override
    protected void parse() {
        getViewParam().load(getConfig());
        getCheckForUpdatesParam().load(getConfig());
        getApiParam().load(getConfig());
        getGlobalExcludeURLParam().load(getConfig());
        getDatabaseParam().load(getConfig());
        getExtensionParam().load(getConfig());

        String userDir = null;
        try {
            userDir = getConfig().getString(USER_DIR);
            if (userDir != null) {
                this.userDirectory = new File(userDir);
            }
        } catch (Exception e) {
            // In a previous release the userdir was set as a file
            try {
                File file = (File) getConfig().getProperty(USER_DIR);
                if (file != null && file.isDirectory()) {
                    this.userDirectory = file;
                }
            } catch (Exception e1) {
                logger.error(e1.getMessage(), e1);
            }
        }
    }

    public void reloadConfigParamSets() {
        for (int i = 0; i < paramSetList.size(); i++) {
            paramSetList.get(i).load(getConfig());
        }
    }

    public void resetAll() {
        for (int i = 0; i < paramSetList.size(); i++) {
            paramSetList.get(i).reset();
        }
    }

    public boolean isGUI() {
        return gui;
    }

    public void setGUI(boolean gui) {
        this.gui = gui;
    }

    /** @return Returns the currentFolder. */
    public File getUserDirectory() {
        return userDirectory;
    }

    /** @param currentDirectory The currentFolder to set. */
    public void setUserDirectory(File currentDirectory) {
        this.userDirectory = currentDirectory;
        // ZAP: User directory now stored in the config file
        getConfig().setProperty(USER_DIR, currentDirectory.getAbsolutePath());
        try {
            getConfig().save();
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Gets the anti-csrf extension's options.
     *
     * @return the anti-csrf options.
     * @deprecated (2.8.0) Use {@link org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF
     *     ExtensionAntiCSRF} to manage the tokens, if the {@code AntiCsrfParam} is really needed
     *     use {@link #getParamSet(Class)} instead.
     */
    @Deprecated
    public AntiCsrfParam getAntiCsrfParam() {
        return getParamSet(AntiCsrfParam.class);
    }

    // ZAP: Added getter.
    public GlobalExcludeURLParam getGlobalExcludeURLParam() {
        return globalExcludeURLParam;
    }

    public OptionsParamApi getApiParam() {
        return apiParam;
    }

    /** @deprecated (2.12.0) */
    @Deprecated
    public ch.csnc.extension.util.OptionsParamExperimentalSliSupport
            getExperimentalFeaturesParam() {
        return experimentalFeaturesParam;
    }

    /**
     * Gets the database configurations.
     *
     * @return the database configurations
     */
    // ZAP: Added the method.
    public DatabaseParam getDatabaseParam() {
        return databaseParam;
    }

    /**
     * Gets the extensions' enabled state configurations.
     *
     * @return the extensions' enabled state configurations.
     * @since 2.6.0
     */
    public ExtensionParam getExtensionParam() {
        return extensionParam;
    }
}
