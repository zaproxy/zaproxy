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
package org.zaproxy.zap.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ProxyListenerLogEventPublisher;
import org.parosproxy.paros.model.HistoryReferenceEventPublisher;
import org.parosproxy.paros.model.SiteMapEventPublisher;
import org.zaproxy.zap.extension.alert.AlertEventPublisher;
import org.zaproxy.zap.extension.ascan.ActiveScanEventPublisher;
import org.zaproxy.zap.extension.brk.BreakEventPublisher;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

/**
 * Class that contains/provides all built-in (core) components (i.e. extensions and active/passive
 * scanners).
 *
 * <p>This class means we don't have to search the core jar for components which was significantly
 * impacting the start time (along with search in add-ons).
 *
 * <p><strong>Note:</strong> If you add a new 'core' component then you will need to add it to this
 * class (in alphabetic order please). Note that ideally we would prefer new functionality to be
 * defined in add-ons as this means we can update them without 'full' releases. The lists could have
 * been maintained in a manifest file as per the add-ons, but having it as code means that it's
 * immediately apparent if someone moves or deletes a component from the core.
 *
 * @since 2.4.0
 */
public final class CoreFunctionality {

    private static List<Extension> builtInExtensions;
    private static List<AbstractPlugin> builtInActiveScanRules;
    private static List<PluginPassiveScanner> builtInPassiveScanRules;

    static {
        // Register core event bus publishers asap
        ActiveScanEventPublisher.getPublisher();
        AlertEventPublisher.getPublisher();
        BreakEventPublisher.getPublisher();
        HistoryReferenceEventPublisher.getPublisher();
        ProxyListenerLogEventPublisher.getPublisher();
        SiteMapEventPublisher.getPublisher();
    }

    /**
     * Returns an unmodifiable list containing all built-in (core) {@code Extension}s.
     *
     * @return an unmodifiable list containing all built-in extensions
     * @see Extension
     */
    public static List<Extension> getBuiltInExtensions() {
        if (builtInExtensions == null) {
            createExtensions();
        }
        return builtInExtensions;
    }

    private static synchronized void createExtensions() {
        if (builtInExtensions == null) {
            ArrayList<Extension> extensions = new ArrayList<>();
            extensions.add(new org.parosproxy.paros.extension.edit.ExtensionEdit());
            extensions.add(new org.parosproxy.paros.extension.history.ExtensionHistory());
            extensions.add(new org.parosproxy.paros.extension.option.ExtensionOption());
            extensions.add(new org.zaproxy.zap.extension.alert.ExtensionAlert());
            extensions.add(new org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF());
            extensions.add(new org.zaproxy.zap.extension.api.ExtensionAPI());
            extensions.add(new org.zaproxy.zap.extension.ascan.ExtensionActiveScan());
            extensions.add(new org.zaproxy.zap.extension.authentication.ExtensionAuthentication());
            extensions.add(new org.zaproxy.zap.extension.authorization.ExtensionAuthorization());
            extensions.add(new org.zaproxy.zap.extension.autoupdate.ExtensionAutoUpdate());
            extensions.add(new org.zaproxy.zap.extension.brk.ExtensionBreak());
            extensions.add(new org.zaproxy.zap.extension.compare.ExtensionCompare());
            extensions.add(new org.zaproxy.zap.extension.ext.ExtensionExtension());
            extensions.add(new org.zaproxy.zap.extension.forceduser.ExtensionForcedUser());
            extensions.add(
                    new org.zaproxy.zap.extension.globalexcludeurl.ExtensionGlobalExcludeURL());
            extensions.add(new org.zaproxy.zap.extension.help.ExtensionHelp());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.component.all
                            .ExtensionHttpPanelComponentAll());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.view.hex.ExtensionHttpPanelHexView());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.view.image
                            .ExtensionHttpPanelImageView());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.view.paramtable
                            .ExtensionHttpPanelRequestFormTableView());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.view.paramtable
                            .ExtensionHttpPanelRequestQueryCookieTableView());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.view.posttable
                            .ExtensionRequestPostTableView());
            extensions.add(
                    new org.zaproxy.zap.extension.httppanel.view.syntaxhighlight
                            .ExtensionHttpPanelSyntaxHighlightTextView());
            extensions.add(new org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions());
            extensions.add(new org.zaproxy.zap.extension.keyboard.ExtensionKeyboard());
            extensions.add(new org.zaproxy.zap.extension.log4j.ExtensionLog4j());
            extensions.add(new org.zaproxy.zap.extension.params.ExtensionParams());
            extensions.add(new org.zaproxy.zap.extension.pscan.ExtensionPassiveScan());
            extensions.add(new org.zaproxy.zap.extension.ruleconfig.ExtensionRuleConfig());
            extensions.add(new org.zaproxy.zap.extension.script.ExtensionScript());
            extensions.add(new org.zaproxy.zap.extension.search.ExtensionSearch());
            extensions.add(new org.zaproxy.zap.extension.sessions.ExtensionSessionManagement());
            extensions.add(new org.zaproxy.zap.extension.siterefresh.ExtensionSitesRefresh());
            extensions.add(new org.zaproxy.zap.extension.stats.ExtensionStats());
            extensions.add(new org.zaproxy.zap.extension.stdmenus.ExtensionStdMenus());
            extensions.add(new org.zaproxy.zap.extension.uiutils.ExtensionUiUtils());
            extensions.add(new org.zaproxy.zap.extension.users.ExtensionUserManagement());
            extensions.add(new org.zaproxy.zap.extension.custompages.ExtensionCustomPages());
            extensions.trimToSize();

            builtInExtensions = Collections.unmodifiableList(extensions);
        }
    }

    /**
     * Returns an unmodifiable list containing all built-in (core) active scanners.
     *
     * @return an unmodifiable list containing all built-in active scanners
     * @see AbstractPlugin
     */
    public static List<AbstractPlugin> getBuiltInActiveScanRules() {
        if (builtInActiveScanRules == null) {
            createActiveScanRules();
        }
        return builtInActiveScanRules;
    }

    private static synchronized void createActiveScanRules() {
        if (builtInActiveScanRules == null) {
            ArrayList<AbstractPlugin> rules = new ArrayList<>();
            rules.add(new org.zaproxy.zap.extension.ascan.ScriptsActiveScanner());
            rules.trimToSize();

            for (AbstractPlugin rule : rules) {
                rule.setStatus(AddOn.Status.release);
            }

            builtInActiveScanRules = Collections.unmodifiableList(rules);
        }
    }

    /**
     * Returns an unmodifiable list containing all built-in (core) passive scanners.
     *
     * @return an unmodifiable list containing all built-in passive scanners
     * @see PluginPassiveScanner
     */
    public static List<PluginPassiveScanner> getBuiltInPassiveScanRules() {
        if (builtInPassiveScanRules == null) {
            createPassiveScanRules();
        }
        return builtInPassiveScanRules;
    }

    private static synchronized void createPassiveScanRules() {
        if (builtInPassiveScanRules == null) {
            ArrayList<PluginPassiveScanner> rules = new ArrayList<>();
            rules.add(new org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner());
            rules.add(new org.zaproxy.zap.extension.pscan.scanner.ScriptsPassiveScanner());
            rules.add(new org.zaproxy.zap.extension.pscan.scanner.StatsPassiveScanner());
            rules.trimToSize();

            for (PluginPassiveScanner rule : rules) {
                rule.setStatus(AddOn.Status.release);
            }

            builtInPassiveScanRules = Collections.unmodifiableList(rules);
        }
    }
}
