/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.control;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.extension.Extension;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class CoreFunctionality {
	
	private static List<Extension> builtInExtensions = null;
	private static List<AbstractPlugin> builtInActiveScanRules = null;
	private static List<PluginPassiveScanner> builtInPassiveScanRules = null;

	/**
	 * This static method returns all of the 'build in' extensions, ie those not defined in add-ons.
	 * This method means we dont have to search the core jar for add-ons which was significantly impacting the start time.
	 * If you add a new 'core' extension then you will need to add it to this list (in alphabetic oder please).
	 * Note that ideally we would prefer new functionality to be defined in add-ons as this means we can update them
	 * without 'full' releases.
	 * This list could have been maintained in a manifest file as per the add-ons, but having is as code means that its
	 * immediately apparent if someone moves or deletes an extension from the core.
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static List<Extension> getBuiltInExtensions() {
		if (builtInExtensions == null) {
			builtInExtensions = new ArrayList<Extension>();
			builtInExtensions.add(new org.parosproxy.paros.extension.edit.ExtensionEdit());
			builtInExtensions.add(new org.parosproxy.paros.extension.filter.ExtensionFilter());
			builtInExtensions.add(new org.parosproxy.paros.extension.history.ExtensionHistory());
			builtInExtensions.add(new org.parosproxy.paros.extension.manualrequest.ExtensionManualRequestEditor());
			builtInExtensions.add(new org.parosproxy.paros.extension.option.ExtensionOption());
			builtInExtensions.add(new org.parosproxy.paros.extension.report.ExtensionReport());
			builtInExtensions.add(new org.parosproxy.paros.extension.state.ExtensionState());
			builtInExtensions.add(new org.zaproxy.zap.extension.alert.ExtensionAlert());
			builtInExtensions.add(new org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF());
			builtInExtensions.add(new org.zaproxy.zap.extension.api.ExtensionAPI());
			builtInExtensions.add(new org.zaproxy.zap.extension.ascan.ExtensionActiveScan());
			builtInExtensions.add(new org.zaproxy.zap.extension.authentication.ExtensionAuthentication());
			builtInExtensions.add(new org.zaproxy.zap.extension.auth.ExtensionAuth());
			builtInExtensions.add(new org.zaproxy.zap.extension.authorization.ExtensionAuthorization());
			builtInExtensions.add(new org.zaproxy.zap.extension.autoupdate.ExtensionAutoUpdate());
			builtInExtensions.add(new org.zaproxy.zap.extension.brk.ExtensionBreak());
			builtInExtensions.add(new org.zaproxy.zap.extension.compare.ExtensionCompare());
			builtInExtensions.add(new org.zaproxy.zap.extension.dynssl.ExtensionDynSSL());
			builtInExtensions.add(new org.zaproxy.zap.extension.encoder2.ExtensionEncoder2());
			builtInExtensions.add(new org.zaproxy.zap.extension.ext.ExtensionExtension());
			builtInExtensions.add(new org.zaproxy.zap.extension.forceduser.ExtensionForcedUser());
			builtInExtensions.add(new org.zaproxy.zap.extension.fuzz.ExtensionFuzz());
			builtInExtensions.add(new org.zaproxy.zap.extension.globalexcludeurl.ExtensionGlobalExcludeURL());
			builtInExtensions.add(new org.zaproxy.zap.extension.help.ExtensionHelp());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.component.all.ExtensionHttpPanelComponentAll());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.amf.ExtensionHttpPanelAMFView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.hex.ExtensionHttpPanelHexView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.image.ExtensionHttpPanelImageView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.largerequest.ExtensionHttpPanelLargeRequestView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.largeresponse.ExtensionHttpPanelLargeResponseView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.paramtable.ExtensionHttpPanelRequestFormTableView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.paramtable.ExtensionHttpPanelRequestQueryCookieTableView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.posttable.ExtensionRequestPostTableView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.ExtensionHttpPanelSyntaxHighlightTextView());
			builtInExtensions.add(new org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions());
			builtInExtensions.add(new org.zaproxy.zap.extension.keyboard.ExtensionKeyboard());
			builtInExtensions.add(new org.zaproxy.zap.extension.log4j.ExtensionLog4j());
			builtInExtensions.add(new org.zaproxy.zap.extension.multiFuzz.ExtensionFuzz());
			builtInExtensions.add(new org.zaproxy.zap.extension.multiFuzz.impl.http.ExtensionRegister());
			builtInExtensions.add(new org.zaproxy.zap.extension.params.ExtensionParams());
			builtInExtensions.add(new org.zaproxy.zap.extension.pscan.ExtensionPassiveScan());
			builtInExtensions.add(new org.zaproxy.zap.extension.reauth.ExtensionReauth());
			builtInExtensions.add(new org.zaproxy.zap.extension.script.ExtensionScript());
			builtInExtensions.add(new org.zaproxy.zap.extension.search.ExtensionSearch());
			builtInExtensions.add(new org.zaproxy.zap.extension.sessions.ExtensionSessionManagement());
			builtInExtensions.add(new org.zaproxy.zap.extension.siterefresh.ExtensionSitesRefresh());
			builtInExtensions.add(new org.zaproxy.zap.extension.spider.ExtensionSpider());
			builtInExtensions.add(new org.zaproxy.zap.extension.stdmenus.ExtensionStdMenus());
			builtInExtensions.add(new org.zaproxy.zap.extension.uiutils.ExtensionUiUtils());
			builtInExtensions.add(new org.zaproxy.zap.extension.users.ExtensionUserManagement());

		}
		return builtInExtensions;
	}

	public static List<AbstractPlugin> getBuiltInActiveScanRules() {
		if (builtInActiveScanRules == null) {
			builtInActiveScanRules = new ArrayList<AbstractPlugin>();
			builtInActiveScanRules.add(new org.zaproxy.zap.extension.ascan.ScriptsActiveScanner());
			
			for (AbstractPlugin ap : builtInActiveScanRules) {
				ap.setStatus(AddOn.Status.release);
			}
		}
		return builtInActiveScanRules;
	}

	public static List<PluginPassiveScanner> getBuiltInPassiveScanRules() {
		if (builtInPassiveScanRules == null) {
			builtInPassiveScanRules = new ArrayList<PluginPassiveScanner>();
			builtInPassiveScanRules.add(new org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner());
			builtInPassiveScanRules.add(new org.zaproxy.zap.extension.pscan.scanner.ScriptsPassiveScanner());
			
			for (PluginPassiveScanner ap : builtInPassiveScanRules) {
				ap.setStatus(AddOn.Status.release);
			}
		}
		return builtInPassiveScanRules;
	}

}
