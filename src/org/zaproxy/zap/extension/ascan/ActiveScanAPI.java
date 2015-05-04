/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ActiveScanAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(ActiveScanAPI.class);

	private static final String PREFIX = "ascan";
    private static final String ACTION_SCAN = "scan";
	private static final String ACTION_SCAN_AS_USER = "scanAsUser";
	private static final String ACTION_PAUSE_SCAN = "pause";
	private static final String ACTION_RESUME_SCAN = "resume";
	private static final String ACTION_STOP_SCAN = "stop";
	private static final String ACTION_PAUSE_ALL_SCANS = "pauseAllScans";
	private static final String ACTION_RESUME_ALL_SCANS = "resumeAllScans";
	private static final String ACTION_STOP_ALL_SCANS = "stopAllScans";
	private static final String ACTION_REMOVE_SCAN = "removeScan";
	private static final String ACTION_REMOVE_ALL_SCANS = "removeAllScans";

	private static final String ACTION_EXCLUDE_FROM_SCAN = "excludeFromScan";
	private static final String ACTION_CLEAR_EXCLUDED_FROM_SCAN = "clearExcludedFromScan";
	private static final String ACTION_ENABLE_ALL_SCANNERS = "enableAllScanners";
	private static final String ACTION_DISABLE_ALL_SCANNERS = "disableAllScanners";
	private static final String ACTION_ENABLE_SCANNERS = "enableScanners";
	private static final String ACTION_DISABLE_SCANNERS = "disableScanners";
	private static final String ACTION_SET_ENABLED_POLICIES = "setEnabledPolicies";
	private static final String ACTION_SET_POLICY_ATTACK_STRENGTH = "setPolicyAttackStrength";
	private static final String ACTION_SET_POLICY_ALERT_THRESHOLD = "setPolicyAlertThreshold";
	private static final String ACTION_SET_SCANNER_ATTACK_STRENGTH = "setScannerAttackStrength";
	private static final String ACTION_SET_SCANNER_ALERT_THRESHOLD = "setScannerAlertThreshold";
	private static final String ACTION_ADD_SCAN_POLICY = "addScanPolicy";
	private static final String ACTION_REMOVE_SCAN_POLICY = "removeScanPolicy";

	private static final String VIEW_STATUS = "status";
	private static final String VIEW_SCANS = "scans";
	private static final String VIEW_MESSAGES_IDS = "messagesIds";
	private static final String VIEW_ALERTS_IDS = "alertsIds";
	private static final String VIEW_EXCLUDED_FROM_SCAN = "excludedFromScan";
	private static final String VIEW_SCANNERS = "scanners";
	// TODO rename? Note any changes like this to the existing API must be clearly documented to users
	private static final String VIEW_POLICIES = "policies";
	private static final String VIEW_SCAN_POLICY_NAMES = "scanPolicyNames";
	private static final String VIEW_ATTACK_MODE_QUEUE = "attackModeQueue";
	private static final String VIEW_SCAN_PROGRESS = "scanProgress";

	private static final String PARAM_URL = "url";
	private static final String PARAM_CONTEXT_ID = "contextId";
	private static final String PARAM_USER_ID = "userId";
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_RECURSE = "recurse";
    private static final String PARAM_JUST_IN_SCOPE = "inScopeOnly";
	private static final String PARAM_IDS = "ids";
	private static final String PARAM_ID = "id";
	private static final String PARAM_ATTACK_STRENGTH = "attackStrength";
	private static final String PARAM_ALERT_THRESHOLD = "alertThreshold";
	private static final String PARAM_SCAN_POLICY_NAME = "scanPolicyName";
	// TODO rename to categoryId? Note any changes like this to the existing API must be clearly documented to users
	private static final String PARAM_CATEGORY_ID = "policyId";
	private static final String PARAM_SCAN_ID = "scanId";
	private static final String PARAM_METHOD = "method";
	private static final String PARAM_POST_DATA = "postData";

	private ExtensionActiveScan controller = null;

	public ActiveScanAPI (ExtensionActiveScan controller) {
		this.controller = controller;
        this.addApiAction(new ApiAction(ACTION_SCAN,
        		new String[] {PARAM_URL}, 
        		new String[] {PARAM_RECURSE, PARAM_JUST_IN_SCOPE, PARAM_SCAN_POLICY_NAME, PARAM_METHOD, PARAM_POST_DATA}));
		this.addApiAction(new ApiAction(
				ACTION_SCAN_AS_USER,
				new String[] { PARAM_URL, PARAM_CONTEXT_ID, PARAM_USER_ID },
				new String[] { PARAM_RECURSE, PARAM_SCAN_POLICY_NAME, PARAM_METHOD, PARAM_POST_DATA }));
		this.addApiAction(new ApiAction(ACTION_PAUSE_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_RESUME_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_STOP_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_REMOVE_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_PAUSE_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_RESUME_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_STOP_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_REMOVE_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_CLEAR_EXCLUDED_FROM_SCAN));
		this.addApiAction(new ApiAction(ACTION_EXCLUDE_FROM_SCAN, new String[] {PARAM_REGEX}));
		this.addApiAction(new ApiAction(ACTION_ENABLE_ALL_SCANNERS, null, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_DISABLE_ALL_SCANNERS, null, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_ENABLE_SCANNERS, new String[] {PARAM_IDS}));
		this.addApiAction(new ApiAction(ACTION_DISABLE_SCANNERS, new String[] {PARAM_IDS}));
		this.addApiAction(new ApiAction(ACTION_SET_ENABLED_POLICIES, new String[] {PARAM_IDS}));
		this.addApiAction(new ApiAction(ACTION_SET_POLICY_ATTACK_STRENGTH,
				new String[] { PARAM_ID, PARAM_ATTACK_STRENGTH }, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_SET_POLICY_ALERT_THRESHOLD,
				new String[] { PARAM_ID, PARAM_ALERT_THRESHOLD }, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_SET_SCANNER_ATTACK_STRENGTH,
				new String[] { PARAM_ID, PARAM_ATTACK_STRENGTH }, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_SET_SCANNER_ALERT_THRESHOLD,
				new String[] { PARAM_ID, PARAM_ALERT_THRESHOLD }, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_ADD_SCAN_POLICY, new String[] {PARAM_SCAN_POLICY_NAME}));
		this.addApiAction(new ApiAction(ACTION_REMOVE_SCAN_POLICY, new String[] {PARAM_SCAN_POLICY_NAME}));

		this.addApiView(new ApiView(VIEW_STATUS, null, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_SCAN_PROGRESS, null, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_MESSAGES_IDS, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_ALERTS_IDS, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_SCANS));
		this.addApiView(new ApiView(VIEW_SCAN_POLICY_NAMES));
		this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_SCAN));
		this.addApiView(new ApiView(VIEW_SCANNERS, null, new String[] {PARAM_SCAN_POLICY_NAME, PARAM_CATEGORY_ID}));
		this.addApiView(new ApiView(VIEW_POLICIES, null, new String[] {PARAM_SCAN_POLICY_NAME, PARAM_CATEGORY_ID}));
		this.addApiView(new ApiView(VIEW_ATTACK_MODE_QUEUE));

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		ScanPolicy policy;
		int policyId;

		User user = null;
		try {
			switch(name) {
			case ACTION_SCAN_AS_USER:
				String urlUserScan = ApiUtils.getNonEmptyStringParam(params, PARAM_URL);
				int userID = ApiUtils.getIntParam(params, PARAM_USER_ID);
				ExtensionUserManagement usersExtension = Control.getSingleton()
						.getExtensionLoader()
						.getExtension(ExtensionUserManagement.class);
				if (usersExtension == null) {
					throw new ApiException(Type.NO_IMPLEMENTOR, ExtensionUserManagement.NAME);
				}
				Context context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
				if (!context.isIncluded(urlUserScan)) {
					throw new ApiException(Type.URL_NOT_IN_CONTEXT, PARAM_CONTEXT_ID);
				}
				user = usersExtension.getContextUserAuthManager(context.getIndex()).getUserById(userID);
				if (user == null) {
					throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
				}

				// Same behaviour but with addition of the user to scan
				// $FALL-THROUGH$
			case ACTION_SCAN:
				String url = params.getString(PARAM_URL);
				if (url == null || url.length() == 0) {
					throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_URL);
				}

				boolean scanJustInScope = user == null ? this.getParam(params, PARAM_JUST_IN_SCOPE, false) : false;

				String policyName = null;
				policy = null;

				try {
					policyName = params.getString(PARAM_SCAN_POLICY_NAME);
				} catch (Exception e1) {
					// Ignore
				}
				try {
					if (policyName != null && policyName.length() > 0) {
						// Not specified, use the default one
						log.debug("handleApiAction scan policy =" + policyName);
						policy = controller.getPolicyManager().getPolicy(policyName);
					}
				} catch (ConfigurationException e) {
					throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_POLICY_NAME);
				}
				String method = this.getParam(params, PARAM_METHOD, HttpRequestHeader.GET);
				if (method.trim().length() == 0) {
					method = HttpRequestHeader.GET;
				}
				if (! Arrays.asList(HttpRequestHeader.METHODS).contains(method)) {
					throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_METHOD);
				}

				int scanId = scanURL(
						params.getString(PARAM_URL),
						user,
						this.getParam(params, PARAM_RECURSE, true),
						scanJustInScope,
						method,
						this.getParam(params, PARAM_POST_DATA, ""),
						policy);

				return new ApiResponseElement(name, Integer.toString(scanId));

			case ACTION_PAUSE_SCAN:
				getActiveScan(params).pauseScan();
				break;
			case ACTION_RESUME_SCAN:
				getActiveScan(params).resumeScan();
				break;
			case ACTION_STOP_SCAN:
				getActiveScan(params).stopScan();
				break;
			case ACTION_REMOVE_SCAN:
				GenericScanner2 activeScan = controller.removeScan(Integer.valueOf(params.getInt(PARAM_SCAN_ID)));
				if (activeScan == null) {
					throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
				}
				break;
			case ACTION_PAUSE_ALL_SCANS:
				controller.pauseAllScans();
				break;
			case ACTION_RESUME_ALL_SCANS:
				controller.resumeAllScans();
				break;
			case ACTION_STOP_ALL_SCANS:
				controller.stopAllScans();
				break;
			case ACTION_REMOVE_ALL_SCANS:
				controller.removeAllScans();
			    break;
			case ACTION_CLEAR_EXCLUDED_FROM_SCAN:
				try {
					Session session = Model.getSingleton().getSession();
					session.setExcludeFromScanRegexs(new ArrayList<String>());
				} catch (DatabaseException e) {
					throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
			    break;
			case ACTION_EXCLUDE_FROM_SCAN:
				String regex = params.getString(PARAM_REGEX);
				try {
					Session session = Model.getSingleton().getSession();
					session.addExcludeFromScanRegexs(regex);
				} catch (Exception e) {
					throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_REGEX);
				}
				break;
			case ACTION_ENABLE_ALL_SCANNERS:
				policy = getScanPolicyFromParams(params);
				policy.getPluginFactory().setAllPluginEnabled(true);
				policy.save();
				break;
			case ACTION_DISABLE_ALL_SCANNERS:
				policy = getScanPolicyFromParams(params);
				policy.getPluginFactory().setAllPluginEnabled(false);
				policy.save();
				break;
			case ACTION_ENABLE_SCANNERS:
				policy = getScanPolicyFromParams(params);
				setScannersEnabled(policy, getParam(params, PARAM_IDS, "").split(","), true);
				policy.save();
				break;
			case ACTION_DISABLE_SCANNERS:
				policy = getScanPolicyFromParams(params);
				setScannersEnabled(policy, getParam(params, PARAM_IDS, "").split(","), false);
				policy.save();
				break;
			case ACTION_SET_ENABLED_POLICIES:
				policy = getScanPolicyFromParams(params);
				setEnabledPolicies(policy, getParam(params, PARAM_IDS, "").split(","));
				policy.save();
				break;
			case ACTION_SET_POLICY_ATTACK_STRENGTH:
				policyId = getPolicyIdFromParamId(params);
				policy = getScanPolicyFromParams(params);
				Plugin.AttackStrength attackStrength = getAttackStrengthFromParamAttack(params);

				for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
					if (scanner.getCategory() == policyId) {
						scanner.setAttackStrength(attackStrength);
					}
				}
				policy.save();
				break;
			case ACTION_SET_POLICY_ALERT_THRESHOLD:
				policyId = getPolicyIdFromParamId(params);
				policy = getScanPolicyFromParams(params);
				Plugin.AlertThreshold alertThreshold1 = getAlertThresholdFromParamAlertThreshold(params);

				for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
					if (scanner.getCategory() == policyId) {
						setAlertThresholdToScanner(alertThreshold1, scanner);
					}
				}
				policy.save();
				break;
			case ACTION_SET_SCANNER_ATTACK_STRENGTH:
				policy = getScanPolicyFromParams(params);
				Plugin scanner = getScannerFromParamId(policy, params);
				scanner.setAttackStrength(getAttackStrengthFromParamAttack(params));
				policy.save();
				break;
			case ACTION_SET_SCANNER_ALERT_THRESHOLD:
				policy = getScanPolicyFromParams(params);
				AlertThreshold alertThreshold2 = getAlertThresholdFromParamAlertThreshold(params);
				setAlertThresholdToScanner(alertThreshold2, getScannerFromParamId(policy, params));
				policy.save();
				break;
			case ACTION_ADD_SCAN_POLICY:
				String newPolicyName = params.getString(PARAM_SCAN_POLICY_NAME);
				if (controller.getPolicyManager().getAllPolicyNames().contains(newPolicyName)) {
					throw new ApiException(ApiException.Type.ALREADY_EXISTS, PARAM_SCAN_POLICY_NAME);
				}
				if (! controller.getPolicyManager().isLegalPolicyName(newPolicyName)) {
					throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_SCAN_POLICY_NAME);
				}
				policy = controller.getPolicyManager().getTemplatePolicy();
				policy.setName(newPolicyName);
				controller.getPolicyManager().savePolicy(policy);
				break;
			case ACTION_REMOVE_SCAN_POLICY:
				// Check it exists
				policy = getScanPolicyFromParams(params);
				if (controller.getPolicyManager().getAllPolicyNames().size() == 1) {
					// Dont remove the last one
					throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, "You are not allowed to remove the last scan policy");
				}
				controller.getPolicyManager().deletePolicy(policy.getName());
				break;
			default:
				throw new ApiException(ApiException.Type.BAD_ACTION);
			}
		} catch (ConfigurationException e) {
			throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
		}
		return ApiResponseElement.OK;
	}

	private ScanPolicy getScanPolicyFromParams(JSONObject params) throws ApiException {
		String policyName = null;;
		try {
			policyName = params.getString(PARAM_SCAN_POLICY_NAME);
		} catch (Exception e1) {
			// Ignore
		}
		if (policyName == null || policyName.length() == 0) {
			// Not specified, use the default one
			return controller.getPolicyManager().getDefaultScanPolicy();
		}
		try {
			return controller.getPolicyManager().getPolicy(policyName);
		} catch (ConfigurationException e) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_POLICY_NAME);
		}
	}

	/**
	 * Returns a {@code ActiveApiScan} from the available {@code activeScans} or the {@code lastActiveScanAvailable}. If a scan
	 * ID ({@code PARAM_SCAN_ID}) is present in the given {@code params} it will be used to the get the {@code ActiveApiScan}
	 * from the available {@code activeScans}, otherwise it's returned the {@code lastActiveScanAvailable}.
	 *
	 * @param params the parameters of the API call
	 * @return the {@code ActiveApiScan} with the given scan ID or, if not present, the {@code lastActiveScanAvailable}
	 * @throws ApiException if there's no scan with the given scan ID
	 * @see #PARAM_SCAN_ID
	 * @see #activeScans
	 * @see #lastActiveScanAvailable
	 */
	private ActiveScan getActiveScan(JSONObject params) throws ApiException {
		int id = getParam(params, PARAM_SCAN_ID, -1);

		GenericScanner2 activeScan = null;

		if (id == -1) {
			activeScan = controller.getLastScan();
		} else {
			activeScan = controller.getScan(Integer.valueOf(id));
		}

		if (activeScan == null) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
		}

		return (ActiveScan)activeScan;
	}

	private void setScannersEnabled(ScanPolicy policy, String [] ids, boolean enabled) throws ConfigurationException, ApiException {
		if (ids.length > 0) {
			for (String id : ids) {
				try {
					Plugin scanner = policy.getPluginFactory().getPlugin(Integer.valueOf(id.trim()).intValue());
					if (scanner != null) {
						setScannerEnabled(scanner, enabled);
					}
				} catch (NumberFormatException e) {
					log.warn("Failed to parse scanner ID: ", e);
				}
			}
		}
	}

	private static void setScannerEnabled(Plugin scanner, boolean enabled) {
		scanner.setEnabled(enabled);
		if (enabled && scanner.getAlertThreshold() == Plugin.AlertThreshold.OFF) {
			scanner.setAlertThreshold(Plugin.AlertThreshold.DEFAULT);
		}
	}

	private void setEnabledPolicies(ScanPolicy policy, String[] ids) {
		policy.getPluginFactory().setAllPluginEnabled(false);
		if (ids.length > 0) {
			for (String id : ids) {
				try {
					int policyId = Integer.valueOf(id.trim()).intValue();
					if (hasPolicyWithId(policyId)) {
						for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
							if (scanner.getCategory() == policyId) {
							    setScannerEnabled(scanner, true);
							}
						}
					}
				} catch (NumberFormatException e) {
					log.warn("Failed to parse policy ID: ", e);
				}
			}
		}
	}

	private static boolean hasPolicyWithId(int policyId) {
		return Arrays.asList(Category.getAllNames()).contains(Category.getName(policyId));
	}

	private int getPolicyIdFromParamId(JSONObject params) throws ApiException {
		final int id = getParam(params, PARAM_ID, -1);
		if (id == -1) {
			throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_ID);
		}
		if (!hasPolicyWithId(id)) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ID);
		}
		return id;
	}

	private Plugin.AttackStrength getAttackStrengthFromParamAttack(JSONObject params) throws ApiException {
		final String paramAttackStrength = params.getString(PARAM_ATTACK_STRENGTH).trim().toUpperCase();
		try {
			return Plugin.AttackStrength.valueOf(paramAttackStrength);
		} catch (IllegalArgumentException e) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ATTACK_STRENGTH);
		}
	}

	private Plugin.AlertThreshold getAlertThresholdFromParamAlertThreshold(JSONObject params) throws ApiException {
		final String paramAlertThreshold = params.getString(PARAM_ALERT_THRESHOLD).trim().toUpperCase();
		try {
			return Plugin.AlertThreshold.valueOf(paramAlertThreshold);
		} catch (IllegalArgumentException e) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ALERT_THRESHOLD);
		}
	}

	private static void setAlertThresholdToScanner(Plugin.AlertThreshold alertThreshold, Plugin scanner) {
		scanner.setAlertThreshold(alertThreshold);
		scanner.setEnabled(!Plugin.AlertThreshold.OFF.equals(alertThreshold));
	}

	private Plugin getScannerFromParamId(ScanPolicy policy, JSONObject params) throws ApiException {
		final int id = getParam(params, PARAM_ID, -1);
		if (id == -1) {
			throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_ID);
		}
		Plugin scanner = policy.getPluginFactory().getPlugin(id);
		if (scanner == null) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ID);
		}
		return scanner;
	}

	private int scanURL(String url, User user, boolean scanChildren, boolean scanJustInScope, String method, String postData, ScanPolicy policy) throws ApiException {
		// Try to find node
		StructuralNode node;
		
		try {
			node = SessionStructure.find(Model.getSingleton().getSession().getSessionId(), new URI(url, false), method, postData);
			if (node == null) {
				throw new ApiException(ApiException.Type.URL_NOT_FOUND);
			}
			Target target = new Target(node);
			target.setRecurse(scanChildren);
			target.setInScopeOnly(scanJustInScope);
			if (user != null) {
				target.setContext(user.getContext());
			}

			Object [] objs = new Object[]{};
			if (policy != null) {
				objs = new Object[]{policy};
			}

			return controller.startScan(null, target, user, objs);
		} catch(ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
		}

	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponse result;
		ActiveScan activeScan = null;
		ScanPolicy policy;
		int categoryId;

		switch(name) {
		case VIEW_STATUS:
			activeScan = getActiveScan(params);
			int progress = 0;
			if (activeScan != null) {
				progress = activeScan.getProgress();
			}
			result = new ApiResponseElement(name, String.valueOf(progress));
			break;
		case VIEW_SCANS:
			ApiResponseList resultList = new ApiResponseList(name);
			for (GenericScanner2 scan : controller.getAllScans()) {
				Map<String, String> map = new HashMap<>();
				map.put("id", Integer.toString(scan.getScanId()));
				map.put("progress", Integer.toString(scan.getProgress()));
				map.put("state", ((ActiveScan)scan).getState().name());
				resultList.addItem(new ApiResponseSet("scan", map));
			}
			result = resultList;
			break;
		case VIEW_SCAN_PROGRESS:
			resultList = new ApiResponseList(name);
			activeScan = getActiveScan(params);
			if (activeScan != null) {
				for (HostProcess hp : activeScan.getHostProcesses()) {
					ApiResponseList hpList = new ApiResponseList("HostProcess");
					resultList.addItem(new ApiResponseElement("id", XMLStringUtil.escapeControlChrs(hp.getHostAndPort())));

					for (Plugin plugin : hp.getCompleted()) {
						ApiResponseList pList = new ApiResponseList("Plugin");
						pList.addItem(new ApiResponseElement("name", XMLStringUtil.escapeControlChrs(plugin.getName())));
						pList.addItem(new ApiResponseElement("id", Integer.toString(plugin.getId())));
						pList.addItem(new ApiResponseElement("status", "Complete"));
						long timeTaken = plugin.getTimeFinished().getTime() - plugin.getTimeStarted().getTime();
						pList.addItem(new ApiResponseElement("timeInMs", Long.toString(timeTaken)));
						hpList.addItem(pList);
			        }

			        for (Plugin plugin : hp.getRunning()) {
						ApiResponseList pList = new ApiResponseList("Plugin");
						int pc = (int)(hp.getTestCurrentCount(plugin) * 100 / hp.getTestTotalCount());
						pList.addItem(new ApiResponseElement("name", XMLStringUtil.escapeControlChrs(plugin.getName())));
						pList.addItem(new ApiResponseElement("id", Integer.toString(plugin.getId())));
						pList.addItem(new ApiResponseElement("status", pc + "%"));
						long timeTaken = new Date().getTime() - plugin.getTimeStarted().getTime();
						pList.addItem(new ApiResponseElement("timeInMs", Long.toString(timeTaken)));
						hpList.addItem(pList);
			        }

			        for (Plugin plugin : hp.getPending()) {
						ApiResponseList pList = new ApiResponseList("Plugin");
						pList.addItem(new ApiResponseElement("name", XMLStringUtil.escapeControlChrs(plugin.getName())));
						pList.addItem(new ApiResponseElement("id", Integer.toString(plugin.getId())));
						pList.addItem(new ApiResponseElement("status", "Pending"));
						pList.addItem(new ApiResponseElement("timeInMs", "0"));
						hpList.addItem(pList);
			        }
					resultList.addItem(hpList);

				}
			}
			result = resultList;
			break;
		case VIEW_MESSAGES_IDS:
			resultList = new ApiResponseList(name);
			activeScan = getActiveScan(params);
			if (activeScan != null) {
				synchronized (activeScan.getMessagesIds()) {
					for (Integer id : activeScan.getMessagesIds()) {
						resultList.addItem(new ApiResponseElement("id", id.toString()));
					}
				}
			}
			result = resultList;
			break;
		case VIEW_ALERTS_IDS:
			resultList = new ApiResponseList(name);
			activeScan = getActiveScan(params);
			if (activeScan != null) {
				synchronized (activeScan.getAlertsIds()) {
					for (Integer id : activeScan.getAlertsIds()) {
						resultList.addItem(new ApiResponseElement("id", id.toString()));
					}
				}
			}
			result = resultList;
			break;
		case VIEW_EXCLUDED_FROM_SCAN:
			result = new ApiResponseList(name);
			Session session = Model.getSingleton().getSession();
			List<String> regexs = session.getExcludeFromScanRegexs();
			for (String regex : regexs) {
				((ApiResponseList)result).addItem(new ApiResponseElement("regex", regex));
			}
			break;
		case VIEW_SCANNERS:
			policy = getScanPolicyFromParams(params);
			List<Plugin> scanners = policy.getPluginFactory().getAllPlugin();

			categoryId = getParam(params, PARAM_CATEGORY_ID, -1);
			if (categoryId != -1 && !hasPolicyWithId(categoryId)) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_CATEGORY_ID);
			}
			resultList = new ApiResponseList(name);
			for (Plugin scanner : scanners) {
				if (categoryId == -1 || categoryId == scanner.getCategory()) {
					resultList.addItem(new ScannerApiResponse(policy, scanner));
				}
			}

			result = resultList;
			break;
		case VIEW_POLICIES:
			policy = getScanPolicyFromParams(params);
			String[] policies = Category.getAllNames();

			resultList = new ApiResponseList(name);
			for (String pluginName : policies) {
				categoryId = Category.getCategory(pluginName);
				Plugin.AttackStrength attackStrength = getPolicyAttackStrength(policy, categoryId);
				Plugin.AlertThreshold alertThreshold = getPolicyAlertThreshold(policy, categoryId);
				Map<String, String> map = new HashMap<>();
				map.put("id", String.valueOf(categoryId));
				map.put("name", pluginName);
				map.put("attackStrength", attackStrength == null ? "" : String.valueOf(attackStrength));
				map.put("alertThreshold", alertThreshold == null ? "" : String.valueOf(alertThreshold));
				map.put("enabled", String.valueOf(isPolicyEnabled(policy, categoryId)));
				resultList.addItem(new ApiResponseSet("policy", map));
			}

			result = resultList;
			break;
		case VIEW_SCAN_POLICY_NAMES:
			resultList = new ApiResponseList(name);
			for (String policyName : controller.getPolicyManager().getAllPolicyNames()) {
				resultList.addItem(new ApiResponseElement("policy", policyName));
			}
			result = resultList;
			break;
		case VIEW_ATTACK_MODE_QUEUE:
			result = new ApiResponseElement(name, String.valueOf(controller.getAttackModeStackSize()));
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

	private boolean isPolicyEnabled(ScanPolicy policy, int category) {
		for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
			if (scanner.getCategory() == category && !scanner.isEnabled()) {
				return false;
			}
		}
		return true;
	}

	private Plugin.AttackStrength getPolicyAttackStrength(ScanPolicy policy, int categoryd) {
		Plugin.AttackStrength attackStrength = null;
		for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
			if (scanner.getCategory() == categoryd) {
				if (attackStrength == null) {
					attackStrength = scanner.getAttackStrength(true);
				} else if (!attackStrength.equals(scanner.getAttackStrength(true))) {
					// Not all the same
					return null;
				}
			}
		}
		return attackStrength;
	}

	private Plugin.AlertThreshold getPolicyAlertThreshold(ScanPolicy policy, int categoryId) {
		Plugin.AlertThreshold alertThreshold = null;
		for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
			if (scanner.getCategory() == categoryId) {
				if (alertThreshold == null) {
					alertThreshold = scanner.getAlertThreshold(true);
				} else if (!alertThreshold.equals(scanner.getAlertThreshold(true))) {
					// Not all the same
					return null;
				}
			}
		}
		return alertThreshold;
	}

	private class ScannerApiResponse extends ApiResponse {

		final Map<String, String> scannerData;
		final ApiResponseList dependencies;

		public ScannerApiResponse(ScanPolicy policy, Plugin scanner) {
			super("scanner");

			scannerData = new HashMap<>();
			scannerData.put("id", String.valueOf(scanner.getId()));
			scannerData.put("name", scanner.getName());
			scannerData.put("cweId", String.valueOf(scanner.getCweId()));
			scannerData.put("wascId", String.valueOf(scanner.getWascId()));
			scannerData.put("attackStrength", String.valueOf(scanner.getAttackStrength(true)));
			scannerData.put("alertThreshold", String.valueOf(scanner.getAlertThreshold(true)));
			scannerData.put("policyId", String.valueOf(scanner.getCategory()));
			scannerData.put("enabled", String.valueOf(scanner.isEnabled()));

			boolean allDepsAvailable = policy.getPluginFactory().hasAllDependenciesAvailable(scanner);
			scannerData.put("allDependenciesAvailable", Boolean.toString(allDepsAvailable));

			dependencies = new ApiResponseList("dependencies");
			for (Plugin dependency : policy.getPluginFactory().getDependencies(scanner)) {
				dependencies.addItem(new ApiResponseElement("dependency", Integer.toString(dependency.getId())));
			}
		}

		@Override
		public void toXML(Document doc, Element parent) {
			parent.setAttribute("type", "set");
			for (Entry<String, String> val : scannerData.entrySet()) {
				Element el = doc.createElement(val.getKey());
				el.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(val.getValue())));
				parent.appendChild(el);
			}

			Element el = doc.createElement(dependencies.getName());
			dependencies.toXML(doc, el);
			parent.appendChild(el);
		}

		@Override
		public JSON toJSON() {
			JSONObject jo = new JSONObject();
			for (Entry<String, String> val : scannerData.entrySet()) {
				jo.put(val.getKey(), val.getValue());
			}
			jo.put(dependencies.getName(), ((JSONObject) dependencies.toJSON()).getJSONArray(dependencies.getName()));
			return jo;
		}

		@Override
		public void toHTML(StringBuilder sb) {
			sb.append("<h2>" + this.getName() + "</h2>\n");
			sb.append("<table border=\"1\">\n");
			for (Entry<String, String> val : scannerData.entrySet()) {
				sb.append("<tr><td>\n");
				sb.append(val.getKey());
				sb.append("</td><td>\n");
				sb.append(StringEscapeUtils.escapeHtml(val.getValue()));
				sb.append("</td></tr>\n");
			}
			sb.append("<tr><td>\n");
			sb.append(dependencies.getName());
			sb.append("</td><td>\n");
			sb.append("<table border=\"1\">\n");
			for (ApiResponse resp : this.dependencies.getItems()) {
				sb.append("<tr><td>\n");
				resp.toHTML(sb);
				sb.append("</td></tr>\n");
			}
			sb.append("</table>\n");
			sb.append("</td></tr>\n");
			sb.append("</table>\n");
		}

		@Override
		public String toString(int indent) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indent; i++) {
				sb.append("	");
			}
			sb.append("ScannerApiResponse ");
			sb.append(this.getName());
			sb.append(" : [\n");
			for (Entry<String, String> val : scannerData.entrySet()) {
				for (int i = 0; i < indent + 1; i++) {
					sb.append("\t");
				}
				sb.append(val.getKey());
				sb.append(" = ");
				sb.append(val.getValue());
				sb.append("\n");
			}
			dependencies.toString(indent + 1);
			for (int i = 0; i < indent; i++) {
				sb.append("\t");
			}
			sb.append("]\n");
			return sb.toString();
		}
	}

	void reset() {
		controller.removeAllScans();
	}
}
