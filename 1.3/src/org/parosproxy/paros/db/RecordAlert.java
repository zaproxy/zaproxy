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


package org.parosproxy.paros.db;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RecordAlert {
    
    private int alertId = -1;
    private int scanId = 0;
    private int pluginId = 0;
    private String alert = "";
    private int risk = 0;
    private int reliability = 0;
    private String description = "";
    private String uri = "";
    private String param = "";
    private String otherInfo = "";
    private String solution = "";
    private String reference = "";
    private int historyId = 0;
    // ZAP: Added sourceHistoryId to RecordAlert - this is the original record that 'caused' the alert
    private int sourceHistoryId = 0;
    
	public RecordAlert() {
		
	}

	public RecordAlert(int alertId, int scanId, int pluginId, String alert, 
			int risk, int reliability, String description, String uri, String query, 
			String otherInfo, String solution, String reference, int historyId,
			int sourceHistoryId) {
	    setAlertId(alertId);
	    setScanId(scanId);
	    setPluginId(pluginId);
	    setAlert(alert);
	    setRisk(risk);
	    setReliability(reliability);
	    setDescription(description);
	    setUri(uri);
	    setParam(query);
	    setOtherInfo(otherInfo);
	    setSolution(solution);
	    setReference(reference);
	    setHistoryId(historyId);
	    setSourceHistoryId(sourceHistoryId);
	}

	

    /**
     * @return Returns the alert.
     */
    public String getAlert() {
        return alert;
    }
    /**
     * @param alert The alert to set.
     */
    public void setAlert(String alert) {
        this.alert = alert;
    }
    /**
     * @return Returns the alertId.
     */
    public int getAlertId() {
        return alertId;
    }
    /**
     * @param alertId The alertId to set.
     */
    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the otherInfo.
     */
    public String getOtherInfo() {
        return otherInfo;
    }
    /**
     * @param otherInfo The otherInfo to set.
     */
    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }
    /**
     * @return Returns the pluginId.
     */
    public int getPluginId() {
        return pluginId;
    }
    /**
     * @param pluginId The pluginId to set.
     */
    public void setPluginId(int pluginId) {
        this.pluginId = pluginId;
    }
    /**
     * @return Returns the query.
     */
    public String getParam() {
        return param;
    }
    /**
     * @param query The query to set.
     */
    public void setParam(String query) {
        this.param = query;
    }
    /**
     * @return Returns the reference.
     */
    public String getReference() {
        return reference;
    }
    /**
     * @param reference The reference to set.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }
    /**
     * @return Returns the reliability.
     */
    public int getReliability() {
        return reliability;
    }
    /**
     * @param reliability The reliability to set.
     */
    public void setReliability(int reliability) {
        this.reliability = reliability;
    }
    /**
     * @return Returns the risk.
     */
    public int getRisk() {
        return risk;
    }
    /**
     * @param risk The risk to set.
     */
    public void setRisk(int risk) {
        this.risk = risk;
    }
    /**
     * @return Returns the scanId.
     */
    public int getScanId() {
        return scanId;
    }
    /**
     * @param scanId The scanId to set.
     */
    public void setScanId(int scanId) {
        this.scanId = scanId;
    }
    /**
     * @return Returns the solution.
     */
    public String getSolution() {
        return solution;
    }
    /**
     * @param solution The solution to set.
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }
    /**
     * @return Returns the uri.
     */
    public String getUri() {
        return uri;
    }
    /**
     * @param uri The uri to set.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
    /**
     * @return Returns the historyId.
     */
    public int getHistoryId() {
        return historyId;
    }
    /**
     * @param historyId The historyId to set.
     */
    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }
    /**
     * @return Returns the sourceHistoryId.
     */
    public int getSourceHistoryId() {
        return sourceHistoryId;
    }
    /**
     * @param sourceHistoryId The sourceHistoryId to set.
     */
    public void setSourceHistoryId(int sourceHistoryId) {
        this.sourceHistoryId = sourceHistoryId;
    }
}
