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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ActiveScanAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(ActiveScanAPI.class);

	private static final String PREFIX = "ascan";
    private static final String ACTION_SCAN = "scan";
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

	private static final String VIEW_STATUS = "status";
	private static final String VIEW_SCANS = "scans";
	private static final String VIEW_MESSAGES_IDS = "messagesIds";
	private static final String VIEW_ALERTS_IDS = "alertsIds";
	private static final String VIEW_EXCLUDED_FROM_SCAN = "excludedFromScan";
	private static final String VIEW_SCANNERS = "scanners";
	private static final String VIEW_POLICIES = "policies";

	private static final String PARAM_URL = "url";
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_RECURSE = "recurse";
    private static final String PARAM_JUST_IN_SCOPE = "inScopeOnly";
	private static final String PARAM_IDS = "ids";
	private static final String PARAM_ID = "id";
	private static final String PARAM_ATTACK_STRENGTH = "attackStrength";
	private static final String PARAM_ALERT_THRESHOLD = "alertThreshold";
	private static final String PARAM_POLICY_ID = "policyId";
	private static final String PARAM_SCAN_ID = "scanId";

	private ExtensionActiveScan extension;

	private final ExtensionAlert extensionAlert;

	/**
	 * The {@code Lock} for exclusive access of instance variables related to multiple active scans.
	 * 
	 * @see #activeScans
	 * @see #scanIdCounter
	 * @see #lastActiveScanAvailable
	 */
	private final Lock activeScansLock;

	/**
	 * The counter used to give an unique ID to active scans.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code activeScansLock}.
	 * </p>
	 * 
	 * @see #activeScansLock
	 * @see #scanURL(String, boolean, boolean)
	 */
	private int scanIdCounter;

	/**
	 * A map that contains all {@code ActiveApiScan}s created (and not yet removed). Used to control (i.e. pause/resume and
	 * stop) the multiple active scans and get its results. The instance variable is never {@code null}. The map key is the ID
	 * of the scan.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code activeScansLock}.
	 * </p>
	 * 
	 * @see #activeScansLock
	 * @see #scanURL(String, boolean, boolean)
	 * @see #scanIdCounter
	 */
	private Map<Integer, ActiveApiScan> activeScans;

	/**
	 * The last {@code ActiveApiScan} available. Might be {@code null}, when no scan was created or all scans were removed.
	 * <p>
	 * The multiple active scans are accessed/controlled using its ID but to keep backward compatibility we keep a reference to
	 * the last scan so it's still possible to get the status without using a scan ID.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code activeScansLock}.
	 * </p>
	 * 
	 * @see #activeScansLock
	 * @see #scanURL(String, boolean, boolean)
	 */
	private ActiveApiScan lastActiveScanAvailable;
	
	public ActiveScanAPI (ExtensionActiveScan extension, ExtensionAlert extensionAlert) {
		this.activeScansLock = new ReentrantLock();
		this.extension = extension;
		this.extensionAlert = extensionAlert;
		this.activeScans = new HashMap<>();
        this.addApiAction(new ApiAction(ACTION_SCAN, new String[] {PARAM_URL}, new String[] {PARAM_RECURSE, PARAM_JUST_IN_SCOPE}));
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
		this.addApiAction(new ApiAction(ACTION_ENABLE_ALL_SCANNERS));
		this.addApiAction(new ApiAction(ACTION_DISABLE_ALL_SCANNERS));
		this.addApiAction(new ApiAction(ACTION_ENABLE_SCANNERS, new String[] {PARAM_IDS}));
		this.addApiAction(new ApiAction(ACTION_DISABLE_SCANNERS, new String[] {PARAM_IDS}));
		this.addApiAction(new ApiAction(ACTION_SET_ENABLED_POLICIES, new String[] {PARAM_IDS}));
		this.addApiAction(new ApiAction(ACTION_SET_POLICY_ATTACK_STRENGTH, new String[] { PARAM_ID, PARAM_ATTACK_STRENGTH }));
		this.addApiAction(new ApiAction(ACTION_SET_POLICY_ALERT_THRESHOLD, new String[] { PARAM_ID, PARAM_ALERT_THRESHOLD }));
		this.addApiAction(new ApiAction(ACTION_SET_SCANNER_ATTACK_STRENGTH, new String[] { PARAM_ID, PARAM_ATTACK_STRENGTH }));
		this.addApiAction(new ApiAction(ACTION_SET_SCANNER_ALERT_THRESHOLD, new String[] { PARAM_ID, PARAM_ALERT_THRESHOLD }));

		this.addApiView(new ApiView(VIEW_STATUS, null, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_MESSAGES_IDS, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_ALERTS_IDS, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_SCANS));
		this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_SCAN));
		this.addApiView(new ApiView(VIEW_SCANNERS, null, new String[] {PARAM_POLICY_ID}));
		this.addApiView(new ApiView(VIEW_POLICIES));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		switch(name) {
		case ACTION_SCAN:
			String url = params.getString(PARAM_URL);
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_URL);
			}
			int scanId = scanURL(
					params.getString(PARAM_URL),
					this.getParam(params, PARAM_RECURSE, true),
					this.getParam(params, PARAM_JUST_IN_SCOPE, false));

			return new ApiResponseElement(name, Integer.toString(scanId));

		case ACTION_PAUSE_SCAN:
			getActiveScan(params).pause();
			break;
		case ACTION_RESUME_SCAN:
			getActiveScan(params).resume();
			break;
		case ACTION_STOP_SCAN:
			getActiveScan(params).stop();
			break;
		case ACTION_REMOVE_SCAN:
			activeScansLock.lock();
			try {
				ActiveApiScan activeScan = activeScans.remove(Integer.valueOf(params.getInt(PARAM_SCAN_ID)));
				if (activeScan == null) {
					throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
				}
				activeScan.stop();

				if (lastActiveScanAvailable == activeScan) {
					if (activeScans.isEmpty()) {
						lastActiveScanAvailable = null;
					} else {
						ActiveApiScan[] scans = new ActiveApiScan[activeScans.size()];
						scans = activeScans.values().toArray(scans);
						lastActiveScanAvailable = scans[scans.length - 1];
					}
				}
			} finally {
				activeScansLock.unlock();
			}
			break;
		case ACTION_PAUSE_ALL_SCANS:
			activeScansLock.lock();
			try {
				for (ActiveApiScan scan : activeScans.values()) {
					scan.pause();
				}
			} finally {
				activeScansLock.unlock();
			}
			break;
		case ACTION_RESUME_ALL_SCANS:
			activeScansLock.lock();
			try {
				for (ActiveApiScan scan : activeScans.values()) {
					scan.resume();
				}
			} finally {
				activeScansLock.unlock();
			}
			break;
		case ACTION_STOP_ALL_SCANS:
			activeScansLock.lock();
			try {
				for (ActiveApiScan scan : activeScans.values()) {
					scan.stop();
				}
			} finally {
				activeScansLock.unlock();
			}
			break;
		case ACTION_REMOVE_ALL_SCANS:
			removeAllScans();
            break;
		case ACTION_CLEAR_EXCLUDED_FROM_SCAN:
			try {
				Session session = Model.getSingleton().getSession();
				session.setExcludeFromScanRegexs(new ArrayList<String>());
			} catch (SQLException e) {
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
			Control.getSingleton().getPluginFactory().setAllPluginEnabled(true);
			break;
		case ACTION_DISABLE_ALL_SCANNERS:
			Control.getSingleton().getPluginFactory().setAllPluginEnabled(false);
			break;
		case ACTION_ENABLE_SCANNERS:
			setScannersEnabled(params, true);
			break;
		case ACTION_DISABLE_SCANNERS:
			setScannersEnabled(params, false);
			break;
		case ACTION_SET_ENABLED_POLICIES:
			setEnabledPolicies(getParam(params, PARAM_IDS, "").split(","));
			break;
		case ACTION_SET_POLICY_ATTACK_STRENGTH:
			int policyId = getPolicyIdFromParamId(params);
			Plugin.AttackStrength attackStrength = getAttackStrengthFromParamAttack(params);

			for (Plugin scanner : Control.getSingleton().getPluginFactory().getAllPlugin()) {
				if (scanner.getCategory() == policyId) {
					scanner.setAttackStrength(attackStrength);
				}
			}
			break;
		case ACTION_SET_POLICY_ALERT_THRESHOLD:
			policyId = getPolicyIdFromParamId(params);
			Plugin.AlertThreshold alertThreshold = getAlertThresholdFromParamAlertThreshold(params);

			for (Plugin scanner : Control.getSingleton().getPluginFactory().getAllPlugin()) {
				if (scanner.getCategory() == policyId) {
					setAlertThresholdToScanner(alertThreshold, scanner);
				}
			}
			break;
		case ACTION_SET_SCANNER_ATTACK_STRENGTH:
			Plugin scanner = getScannerFromParamId(params);
			scanner.setAttackStrength(getAttackStrengthFromParamAttack(params));
			break;
		case ACTION_SET_SCANNER_ALERT_THRESHOLD:
			alertThreshold = getAlertThresholdFromParamAlertThreshold(params);
			setAlertThresholdToScanner(alertThreshold, getScannerFromParamId(params));
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		return ApiResponseElement.OK;
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
	private ActiveApiScan getActiveScan(JSONObject params) throws ApiException {
		activeScansLock.lock();
		try {
			int id = getParam(params, PARAM_SCAN_ID, -1);

			if (id == -1) {
				return lastActiveScanAvailable;
			}

			ActiveApiScan activeScan = activeScans.get(Integer.valueOf(id));
			if (activeScan == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
			}

			return activeScan;
		} finally {
			activeScansLock.unlock();
		}
	}

	private void setScannersEnabled(JSONObject params, boolean enabled) {
		String[] ids = getParam(params, PARAM_IDS, "").split(",");
		if (ids.length > 0) {
			for (String id : ids) {
				try {
					Plugin scanner = Control.getSingleton().getPluginFactory().getPlugin(Integer.valueOf(id.trim()).intValue());
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

	private static void setEnabledPolicies(String[] ids) {
		Control.getSingleton().getPluginFactory().setAllPluginEnabled(false);
		if (ids.length > 0) {
			for (String id : ids) {
				try {
					int policyId = Integer.valueOf(id.trim()).intValue();
					if (hasPolicyWithId(policyId)) {
						for (Plugin scanner : Control.getSingleton().getPluginFactory().getAllPlugin()) {
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

	private Plugin getScannerFromParamId(JSONObject params) throws ApiException {
		final int id = getParam(params, PARAM_ID, -1);
		if (id == -1) {
			throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_ID);
		}
		Plugin scanner = Control.getSingleton().getPluginFactory().getPlugin(id);
		if (scanner == null) {
			throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ID);
		}
		return scanner;
	}

	private int scanURL(String url, boolean scanChildren, boolean scanJustInScope) throws ApiException {
		// Try to find node
		SiteNode startNode;
		try {
			startNode = Model.getSingleton().getSession().getSiteTree().findNode(new URI(url, true));
			if (startNode == null) {
				throw new ApiException(ApiException.Type.URL_NOT_FOUND);
			}
		} catch (URIException e) {
			throw new ApiException(ApiException.Type.URL_NOT_FOUND);
		}

		activeScansLock.lock();
		try {
			int scanId = scanIdCounter++;
			ActiveApiScan activeApiScan = new ActiveApiScan(
					extension,
					extensionAlert,
					url,
					startNode,
					scanChildren,
					scanJustInScope,
					scanId);
			activeScans.put(Integer.valueOf(scanId), activeApiScan);
			activeApiScan.start();
			lastActiveScanAvailable = activeApiScan;

			return scanId;
		} finally {
			activeScansLock.unlock();
		}
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponse result;

		switch(name) {
		case VIEW_STATUS:
			ActiveApiScan activeScan = getActiveScan(params);
			int progress = 0;
			if (activeScan != null) {
				progress = activeScan.getProgress();
			}
			result = new ApiResponseElement(name, String.valueOf(progress));
			break;
		case VIEW_SCANS:
			ApiResponseList resultList = new ApiResponseList(name);
			activeScansLock.lock();
			try {
				for (ActiveApiScan scan : activeScans.values()) {
					Map<String, String> map = new HashMap<>();
					map.put("id", Integer.toString(scan.getId()));
					map.put("progress", Integer.toString(scan.getProgress()));
					map.put("state", scan.getState());
					resultList.addItem(new ApiResponseSet("scan", map));
				}
			} finally {
				activeScansLock.unlock();
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
			List<Plugin> scanners = Control.getSingleton().getPluginFactory().getAllPlugin();

			int policyId = getParam(params, PARAM_POLICY_ID, -1);
			if (policyId != -1 && !hasPolicyWithId(policyId)) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_POLICY_ID);
			}
			resultList = new ApiResponseList(name);
			for (Plugin scanner : scanners) {
				if (policyId == -1 || policyId == scanner.getCategory()) {
					resultList.addItem(new ScannerApiResponse(scanner));
				}
			}

			result = resultList;
			break;
		case VIEW_POLICIES:
			String[] policies = Category.getAllNames();

			resultList = new ApiResponseList(name);
			for (String policy : policies) {
				policyId = Category.getCategory(policy);
				Plugin.AttackStrength attackStrength = getPolicyAttackStrength(policyId);
				Plugin.AlertThreshold alertThreshold = getPolicyAlertThreshold(policyId);
				Map<String, String> map = new HashMap<>();
				map.put("id", String.valueOf(policyId));
				map.put("name", policy);
				map.put("attackStrength", attackStrength == null ? "" : String.valueOf(attackStrength));
				map.put("alertThreshold", alertThreshold == null ? "" : String.valueOf(alertThreshold));
				map.put("enabled", String.valueOf(isPolicyEnabled(policyId)));
				resultList.addItem(new ApiResponseSet("policy", map));
			}

			result = resultList;
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

	private static boolean isPolicyEnabled(int policy) {
		for (Plugin scanner : Control.getSingleton().getPluginFactory().getAllPlugin()) {
			if (scanner.getCategory() == policy && !scanner.isEnabled()) {
				return false;
			}
		}
		return true;
	}
	
	private Plugin.AttackStrength getPolicyAttackStrength(int policyId) {
		Plugin.AttackStrength attackStrength = null;
		for (Plugin scanner : Control.getSingleton().getPluginFactory().getAllPlugin()) {
			if (scanner.getCategory() == policyId) {
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

	private Plugin.AlertThreshold getPolicyAlertThreshold(int policyId) {
		Plugin.AlertThreshold alertThreshold = null;
		for (Plugin scanner : Control.getSingleton().getPluginFactory().getAllPlugin()) {
			if (scanner.getCategory() == policyId) {
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

	private static class ScannerApiResponse extends ApiResponse {

		final Map<String, String> scannerData;
		final ApiResponseList dependencies;

		public ScannerApiResponse(Plugin scanner) {
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

			boolean allDepsAvailable = Control.getSingleton().getPluginFactory().hasAllDependenciesAvailable(scanner);
			scannerData.put("allDependenciesAvailable", Boolean.toString(allDepsAvailable));

			dependencies = new ApiResponseList("dependencies");
			for (Plugin dependency : Control.getSingleton().getPluginFactory().getDependencies(scanner)) {
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
		activeScansLock.lock();
		try {
			removeAllScans();
			this.activeScans = new HashMap<>();
		} finally {
			activeScansLock.unlock();
		}
	}

	private void removeAllScans() {
		activeScansLock.lock();
		try {
			for (Iterator<ActiveApiScan> it = activeScans.values().iterator(); it.hasNext();) {
				it.next().stop();
				it.remove();
			}
			lastActiveScanAvailable = null;
		} finally {
			activeScansLock.unlock();
		}
	}

	private static class ActiveApiScan implements ScanListenner, ScannerListener {

		private static enum State {
			NOT_STARTED,
			RUNNING,
			PAUSED,
			FINISHED
		};

		private final Lock lock;

		private final int id;

		private final List<Integer> hRefs;

		private final List<Integer> alerts;

		private final ActiveScan activeScan;

		private final ExtensionAlert extensionAlert;

		private State state;

		private int progress;

		public ActiveApiScan(
				ExtensionActiveScan extension,
				ExtensionAlert extensionAlert,
				String url,
				SiteNode startNode,
				boolean scanChildren,
				boolean scanJustInScope,
				int scanId) {
			lock = new ReentrantLock();
			id = scanId;

			hRefs = Collections.synchronizedList(new ArrayList<Integer>());
			alerts = Collections.synchronizedList(new ArrayList<Integer>());

			this.extensionAlert = extensionAlert;

			state = State.NOT_STARTED;

			activeScan = new ActiveScan(url, extension.getScannerParam(), extension.getModel()
					.getOptionsParam()
					.getConnectionParam(), null, Control.getSingleton().getPluginFactory().clone()) {

				@Override
				public void notifyNewMessage(HttpMessage msg) {
					HistoryReference hRef = msg.getHistoryRef();
					if (hRef == null) {
						try {
							hRef = new HistoryReference(Model.getSingleton().getSession(), HistoryReference.TYPE_TEMPORARY, msg);
							msg.setHistoryRef(null);
							hRefs.add(Integer.valueOf(hRef.getHistoryId()));
						} catch (HttpMalformedHeaderException | SQLException e) {
							log.error(e.getMessage(), e);
						}
					} else {
						hRefs.add(Integer.valueOf(hRef.getHistoryId()));
					}
				}
			};

			activeScan.setJustScanInScope(scanJustInScope);
			activeScan.setStartNode(startNode);
			activeScan.setScanChildren(scanChildren);
		}

		/**
		 * Returns the ID of the scan.
		 *
		 * @return the ID of the scan
		 */
		public int getId() {
			return id;
		}

		/**
		 * Returns the {@code String} representation of the scan state (not started, running, paused or finished).
		 *
		 * @return the {@code String} representation of the scan state.
		 */
		public String getState() {
			lock.lock();
			try {
				return state.toString();
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Returns the progress of the scan, an integer between 0 and 100.
		 *
		 * @return the progress of the scan.
		 */
		public int getProgress() {
			return progress;
		}

		/**
		 * Starts the scan.
		 * <p>
		 * The call to this method has no effect if the scan was already started.
		 * </p>
		 */
		public void start() {
			lock.lock();
			try {
				if (State.NOT_STARTED.equals(state)) {
					activeScan.addScannerListener(this);
					activeScan.start();
					state = State.RUNNING;
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Pauses the scan.
		 * <p>
		 * The call to this method has no effect if the scan is not running.
		 * </p>
		 */
		public void pause() {
			lock.lock();
			try {
				if (State.RUNNING.equals(state)) {
					activeScan.pauseScan();
					state = State.PAUSED;
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Resumes the scan.
		 * <p>
		 * The call to this method has no effect if the scan is not paused.
		 * </p>
		 */
		public void resume() {
			lock.lock();
			try {
				if (State.PAUSED.equals(state)) {
					activeScan.resumeScan();
					state = State.RUNNING;
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Stops the scan.
		 * <p>
		 * The call to this method has no effect if the scan was not yet started or has already finished.
		 * </p>
		 */
		public void stop() {
			lock.lock();
			try {
				if (!State.NOT_STARTED.equals(state) && !State.FINISHED.equals(state)) {
					activeScan.stopScan();
					state = State.FINISHED;
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Returns the IDs of all messages sent/created during the scan. The message must be recreated with a HistoryReference.
		 * <p>
		 * <strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing to do so might result in
		 * {@code ConcurrentModificationException}.
		 * </p>
		 *
		 * @return the IDs of all the messages sent/created during the scan
		 * @see HistoryReference
		 * @see ConcurrentModificationException
		 */
		public List<Integer> getMessagesIds() {
			return hRefs;
		}

		/**
		 * Returns the IDs of all alerts raised during the scan.
		 * <p>
		 * <strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing to do so might result in
		 * {@code ConcurrentModificationException}.
		 * </p>
		 *
		 * @return the IDs of all the alerts raised during the scan
		 * @see ConcurrentModificationException
		 */
		public List<Integer> getAlertsIds() {
			return alerts;
		}

		@Override
		public void alertFound(Alert alert) {
			extensionAlert.alertFound(alert, alert.getHistoryRef());

			int alertId = alert.getAlertId();
			if (alertId != -1) {
				alerts.add(Integer.valueOf(alert.getAlertId()));
			}
		}

		@Override
		public void hostComplete(String hostAndPort) {
		}

		@Override
		public void hostNewScan(String hostAndPort, HostProcess hostThread) {
		}

		@Override
		public void hostProgress(String hostAndPort, String msg, int percentage) {
			this.progress = percentage;
		}

		@Override
		public void notifyNewMessage(HttpMessage msg) {
		}

		@Override
		public void scannerComplete() {
			lock.lock();
			try {
				state = State.FINISHED;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void scanFinshed(String host) {
		}

		@Override
		public void scanProgress(String host, int progress, int maximum) {
		}
	}
}
