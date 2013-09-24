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
// ZAP: 2011/08/30 Support for scanner levels
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/06/07 Added targetParam options
// ZAP: 2012/07/30 Issue 43: Added support for Scope
// ZAP: 2012/08/07 Renamed Level to AlertThreshold and added support for AttackStrength
// ZAP: 2012/08/31 Enabled control of AttackStrength
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/04/26 Issue 652: Added option to not delete records on shutdown
// ZAP: 2013/09/23 Issue 795: Allow param types scanned to be configured via UI
// ZAP: 2013/09/24 Issue 797: Limit number of ascan results listed to speed up scans

package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;

public class ScannerParam extends AbstractParam {

	private static final String HOST_PER_SCAN = "scanner.hostPerScan";
	private static final String THREAD_PER_HOST = "scanner.threadPerHost";
	// ZAP: Added support for delayInMs
	private static final String DELAY_IN_MS = "scanner.delayInMs";
	private static final String HANDLE_ANTI_CSRF_TOKENS = "scanner.antiCSFR";
	private static final String DELETE_RECORDS_ON_SHUTDOWN = "scanner.deleteOnShutdown";
	private static final String LEVEL = "scanner.level";
	private static final String STRENGTH = "scanner.strength";
	private static final String MAX_RESULTS_LIST = "scanner.maxResults";
	private static final String TARGET_PARAMS_URL = "scanner.params.url";
	private static final String TARGET_PARAMS_FORM = "scanner.params.form";
	private static final String TARGET_PARAMS_COOKIE = "scanner.params.cookie";
	// Currently included in TARGET_PARAMS_FORM
	//private static final String TARGET_PARAMS_MULTIPART_FORM = "scanner.params.multiform";
	private static final String TARGET_PARAMS_GWT = "scanner.params.gwt";
	private static final String TARGET_PARAMS_XML = "scanner.params.xml";
	private static final String TARGET_PARAMS_JSON = "scanner.params.json";
	private static final String TARGET_PARAMS_HEADER = "scanner.params.header";
	private static final String TARGET_PARAMS_ODATA_ID = "scanner.params.odata.id";
	private static final String TARGET_PARAMS_ODATA_FILTER = "scanner.params.odata.filter";
		
	private int hostPerScan = 2;
	private int threadPerHost = 1;
	private int delayInMs = 0;
	private int maxResultsToList = 1000;
	private boolean handleAntiCSRFTokens = false;
	private boolean deleteRequestsOnShutdown = true;
	private Plugin.AlertThreshold alertThreshold = AlertThreshold.MEDIUM; 
	private Plugin.AttackStrength attackStrength = AttackStrength.MEDIUM;
	
	private boolean targetParamsUrl = true;
	private boolean targetParamsForm = true;
	private boolean targetParamsCookie = true;
	private boolean targetParamsMultiPartForm = true;
	private boolean targetParamsGWT = true;
	private boolean targetParamsXML = true;
	private boolean targetParamsJSON = true;
	private boolean targetParamsHeader = true;
	private boolean targetParamsODataId = true;
	private boolean targetParamsODataFilter = true;

    public ScannerParam() {
    }

    @Override
    protected void parse(){
        
		try {
			this.threadPerHost = getConfig().getInt(THREAD_PER_HOST, 1);
		} catch (Exception e) {}
		try {
			this.hostPerScan = getConfig().getInt(HOST_PER_SCAN, 2);
		} catch (Exception e) {}
		try {
			this.delayInMs = getConfig().getInt(DELAY_IN_MS, 0);
		} catch (Exception e) {}
		try {
			this.maxResultsToList = getConfig().getInt(MAX_RESULTS_LIST, 1000);
		} catch (Exception e) {}
		try {
			this.handleAntiCSRFTokens = getConfig().getBoolean(HANDLE_ANTI_CSRF_TOKENS, false);
		} catch (Exception e) {}
		try {
			this.deleteRequestsOnShutdown = getConfig().getBoolean(DELETE_RECORDS_ON_SHUTDOWN, true);
		} catch (Exception e) {}
		try {
			this.alertThreshold = AlertThreshold.valueOf(getConfig().getString(LEVEL, AlertThreshold.MEDIUM.name()));
		} catch (Exception e) {}
		try {
			this.attackStrength = AttackStrength.valueOf(getConfig().getString(STRENGTH, AttackStrength.MEDIUM.name()));
		} catch (Exception e) {}
		try {
			this.targetParamsUrl = getConfig().getBoolean(TARGET_PARAMS_URL, true);
		} catch (Exception e) {}
		try {
			this.targetParamsForm = getConfig().getBoolean(TARGET_PARAMS_FORM, true);
		} catch (Exception e) {}
		try {
			this.targetParamsCookie = getConfig().getBoolean(TARGET_PARAMS_COOKIE, true);
		} catch (Exception e) {}
		try {
			// Currently reuses TARGET_PARAMS_FORM
			this.targetParamsMultiPartForm = getConfig().getBoolean(TARGET_PARAMS_FORM, true);
		} catch (Exception e) {}
		try {
			this.targetParamsGWT = getConfig().getBoolean(TARGET_PARAMS_GWT, true);
		} catch (Exception e) {}
		try {
			this.targetParamsXML = getConfig().getBoolean(TARGET_PARAMS_XML, true);
		} catch (Exception e) {}
		try {
			this.targetParamsJSON = getConfig().getBoolean(TARGET_PARAMS_JSON, true);
		} catch (Exception e) {}
		try {
			this.targetParamsHeader = getConfig().getBoolean(TARGET_PARAMS_HEADER, true);
		} catch (Exception e) {}
		try {
			this.targetParamsODataId = getConfig().getBoolean(TARGET_PARAMS_ODATA_ID, true);
		} catch (Exception e) {}
		try {
			this.targetParamsODataFilter = getConfig().getBoolean(TARGET_PARAMS_ODATA_FILTER, true);
		} catch (Exception e) {}

    }

    public int getThreadPerHost() {
        return threadPerHost;
    }
    
    public void setThreadPerHost(int threadPerHost) {
        this.threadPerHost = threadPerHost;
        getConfig().setProperty(THREAD_PER_HOST, Integer.toString(this.threadPerHost));

    }

    /**
     * @return Returns the thread.
     */
    public int getHostPerScan() {
        return hostPerScan;
    }
    /**
     * @param hostPerScan The thread to set.
     */
    public void setHostPerScan(int hostPerScan) {
        this.hostPerScan = hostPerScan;
		getConfig().setProperty(HOST_PER_SCAN, Integer.toString(this.hostPerScan));

    }

	public int getMaxResultsToList() {
		return maxResultsToList;
	}

	public void setMaxResultsToList(int maxResultsToList) {
		this.maxResultsToList = maxResultsToList;
        getConfig().setProperty(MAX_RESULTS_LIST, Integer.toString(this.maxResultsToList));
	}

	public void setDelayInMs(int delayInMs) {
		this.delayInMs = delayInMs;
		getConfig().setProperty(DELAY_IN_MS, Integer.toString(this.delayInMs));
	}

	public int getDelayInMs() {
		return delayInMs;
	}

	public boolean getHandleAntiCSRFTokens() {
		return handleAntiCSRFTokens;
	}

	public void setHandleAntiCSRFTokens(boolean handleAntiCSRFTokens) {
		this.handleAntiCSRFTokens = handleAntiCSRFTokens;
		getConfig().setProperty(HANDLE_ANTI_CSRF_TOKENS, handleAntiCSRFTokens);
	}
	
    public boolean isDeleteRequestsOnShutdown() {
		return deleteRequestsOnShutdown;
	}

	public void setDeleteRequestsOnShutdown(boolean deleteRequestsOnShutdown) {
		this.deleteRequestsOnShutdown = deleteRequestsOnShutdown;
		getConfig().setProperty(DELETE_RECORDS_ON_SHUTDOWN, deleteRequestsOnShutdown);
	}

	public Plugin.AlertThreshold getAlertThreshold() {
		return alertThreshold;
	}

    public void setAlertThreshold(Plugin.AlertThreshold level) {
		this.alertThreshold = level;
		getConfig().setProperty(LEVEL, level.name());
	}

    public void setAlertThreshold(String level) {
		this.setAlertThreshold(AlertThreshold.valueOf(level));
	}

    public Plugin.AttackStrength getAttackStrength() {
		return attackStrength;
	}

    public void setAttackStrength(Plugin.AttackStrength strength) {
		this.attackStrength = strength;
		getConfig().setProperty(STRENGTH, strength.name());
	}

    public void setAttackStrength(String strength) {
		this.setAttackStrength(AttackStrength.valueOf(strength));
	}

	public boolean isTargetParamsUrl() {
		return targetParamsUrl;
	}

	public void setTargetParamsUrl(boolean targetParamsUrl) {
		this.targetParamsUrl = targetParamsUrl;
		getConfig().setProperty(TARGET_PARAMS_URL, this.targetParamsUrl);
	}

	public boolean isTargetParamsForm() {
		return targetParamsForm;
	}

	public void setTargetParamsForm(boolean targetParamsForm) {
		this.targetParamsForm = targetParamsForm;
		getConfig().setProperty(TARGET_PARAMS_FORM, this.targetParamsForm);
	}

	public boolean isTargetParamsCookie() {
		return targetParamsCookie;
	}

	public void setTargetParamsCookie(boolean targetParamsCookie) {
		this.targetParamsCookie = targetParamsCookie;
		getConfig().setProperty(TARGET_PARAMS_COOKIE, this.targetParamsCookie);
	}

	public boolean isTargetParamsMultiPartForm() {
		return targetParamsMultiPartForm;
	}

	/*
	public void setTargetParamsMultiPartForm(boolean targetParamsMultiPartForm) {
		this.targetParamsMultiPartForm = targetParamsMultiPartForm;
		getConfig().setProperty(TARGET_PARAMS_MULTIPART_FORM, this.targetParamsMultiPartForm);
	}
	*/

	public boolean isTargetParamsGWT() {
		return targetParamsGWT;
	}

	public void setTargetParamsGWT(boolean targetParamsGWT) {
		this.targetParamsGWT = targetParamsGWT;
		getConfig().setProperty(TARGET_PARAMS_GWT, this.targetParamsGWT);
	}

	public boolean isTargetParamsXML() {
		return targetParamsXML;
	}

	public void setTargetParamsXML(boolean targetParamsXML) {
		this.targetParamsXML = targetParamsXML;
		getConfig().setProperty(TARGET_PARAMS_XML, this.targetParamsXML);
	}

	public boolean isTargetParamsJSON() {
		return targetParamsJSON;
	}

	public void setTargetParamsJSON(boolean targetParamsJSON) {
		this.targetParamsJSON = targetParamsJSON;
		getConfig().setProperty(TARGET_PARAMS_JSON, this.targetParamsJSON);
	}

	public boolean isTargetParamsHeader() {
		return targetParamsHeader;
	}

	public void setTargetParamsHeader(boolean targetParamsHeader) {
		this.targetParamsHeader = targetParamsHeader;
		getConfig().setProperty(TARGET_PARAMS_HEADER, this.targetParamsHeader);
	}

	public boolean isTargetParamsODataId() {
		return targetParamsODataId;
	}

	public void setTargetParamsODataId(boolean targetParamsODataId) {
		this.targetParamsODataId = targetParamsODataId;
		getConfig().setProperty(TARGET_PARAMS_ODATA_ID, this.targetParamsODataId);
	}

	public boolean isTargetParamsODataFilter() {
		return targetParamsODataFilter;
	}

	public void setTargetParamsODataFilter(boolean targetParamsODataFilter) {
		this.targetParamsODataFilter = targetParamsODataFilter;
		getConfig().setProperty(TARGET_PARAMS_ODATA_FILTER, this.targetParamsODataFilter);
	}
	
}
