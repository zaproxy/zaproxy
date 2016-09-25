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
// ZAP: 2011/12/04 Support deleting alerts
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/01/23 Changed the method compareTo to compare the fields correctly
// with each other.
// ZAP: 2012/03/15 Changed the methods toPluginXML and getUrlParamXML to use the class
// StringBuilder instead of StringBuffer and replaced some string concatenations with
// calls to the method append of the class StringBuilder.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/02 Changed to not create a new String in the setters.
// ZAP: 2012/07/10 Issue 323: Added getIconUrl()
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2012/12/19 Code Cleanup: Moved array brackets from variable name to type
// ZAP: 2013/07/12 Issue 713: Add CWE and WASC numbers to issues
// ZAP: 2013/09/08 Issue 691: Handle old plugins
// ZAP: 2013/11/16 Issue 866: Alert keeps HttpMessage longer than needed when HistoryReference is set/available
// ZAP: 2014/04/10 Issue 1042: Having significant issues opening a previous session
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2015/01/04 Issue 1419: Include alert's evidence in HTML report
// ZAP: 2014/01/04 Issue 1475: Alerts with different name from same scanner might not be shown in report
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2015/08/24 Issue 1849: Option to merge related issues in reports
// ZAP: 2015/11/16 Issue 1555: Rework inclusion of HTML tags in reports 
// ZAP: 2016/02/26 Deprecate alert as an element of Alert in favour of name
// ZAP: 2016/05/25 Normalise equals/hashCode/compareTo
// ZAP: 2016/08/10 Issue 2757: Alerts with different request method are considered the same
// ZAP: 2016/08/25 Initialise the method to an empty string
// ZAP: 2016/09/20 JavaDoc tweaks

package org.parosproxy.paros.core.scanner;

import java.net.URL;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.extension.report.ReportGenerator;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


public class Alert implements Comparable<Alert>  {

	public static final int RISK_INFO 	= 0;
	public static final int RISK_LOW 	= 1;
	public static final int RISK_MEDIUM = 2;
	public static final int RISK_HIGH 	= 3;

	// ZAP: Added FALSE_POSITIVE
	public static final int CONFIDENCE_FALSE_POSITIVE = 0;
	/**
	 * @deprecated (2.4.0) Replaced by {@link #CONFIDENCE_LOW} confidence.
	 * SUSPICIOUS reliability has been deprecated in favour of using CONFIDENCE_LOW confidence.
	 */
	@Deprecated
	public static final int SUSPICIOUS = 1;
	public static final int CONFIDENCE_LOW = 1;
	/**
	 * @deprecated (2.4.0) Replaced by {@link #CONFIDENCE_MEDIUM} confidence.
	 * WARNING reliability has been deprecated in favour of using CONFIDENCE_MEDIUM confidence.
	 */
	@Deprecated
	public static final int WARNING = 2;
	public static final int CONFIDENCE_MEDIUM = 2;
	public static final int CONFIDENCE_HIGH = 3;
	public static final int CONFIDENCE_USER_CONFIRMED = 4;
	
	public static final String[] MSG_RISK = {"Informational", "Low", "Medium", "High"};
	// ZAP: Added "false positive"
	/**
	 * @deprecated (2.4.0) Replaced by {@link #MSG_CONFIDENCE}.
	 * Use of reliability has been deprecated in favour of using confidence.
	 */
	@Deprecated
	public static final String[] MSG_RELIABILITY = {"False Positive", "Low", "Medium", "High", "Confirmed"};
	public static final String[] MSG_CONFIDENCE = {"False Positive", "Low", "Medium", "High", "Confirmed"};
	
	
	private int		alertId = -1;	// ZAP: Changed default alertId
	private int		pluginId = 0;
	private String name = "";
	private int risk = RISK_INFO;
	private int confidence = CONFIDENCE_MEDIUM;
	private String 	description = "";
	private String 	uri = "";
	private String 	param = "";
	private String 	attack = "";
	private String 	otherInfo = "";
	private String 	solution = "";
	private String	reference = "";
	private String 	evidence = "";
	private int cweId = -1;
	private int wascId = -1;
	// Tempory ref - should be cleared asap after use
	private HttpMessage message = null;
	// ZAP: Added sourceHistoryId to Alert
	private int		sourceHistoryId = 0;
	private HistoryReference historyRef = null;
	// ZAP: Added logger
	private static final Logger logger = Logger.getLogger(Alert.class);
	// Cache this info so that we dont have to keep a ref to the HttpMessage
	private String method = "";
	private String postData;
	private URI msgUri = null;
	
	public Alert(int pluginId) {
		this.pluginId = pluginId;
		
	}
	
	public Alert(int pluginId, int risk, int confidence, String name) {
		this(pluginId);
		setRiskConfidence(risk, confidence);
		setName(name);
	}

	public Alert(RecordAlert recordAlert) {
	    this(recordAlert.getPluginId(), recordAlert.getRisk(), recordAlert.getConfidence(), recordAlert.getAlert());
	    // ZAP: Set the alertId
	    this.alertId = recordAlert.getAlertId();
        try {
        	HistoryReference hRef = new HistoryReference(recordAlert.getHistoryId());
            setDetail(recordAlert.getDescription(), recordAlert.getUri(), 
            		recordAlert.getParam(), recordAlert.getAttack(), recordAlert.getOtherInfo(), 
            		recordAlert.getSolution(), recordAlert.getReference(),
            		recordAlert.getEvidence(), recordAlert.getCweId(), recordAlert.getWascId(),
            		null);

            setHistoryRef(hRef);
        } catch (HttpMalformedHeaderException e) {
        	// ZAP: Just an indication the history record doesnt exist
        	logger.debug(e.getMessage(), e);
        } catch (Exception e) {
        	// ZAP: Log the exception
        	logger.error(e.getMessage(), e);
        }
	    
	}
	
	public Alert(RecordAlert recordAlert, HistoryReference ref) {
	    this(recordAlert.getPluginId(), recordAlert.getRisk(), recordAlert.getConfidence(), recordAlert.getAlert());
	    // ZAP: Set the alertId
	    this.alertId = recordAlert.getAlertId();
        setDetail(recordAlert.getDescription(), recordAlert.getUri(), 
        		recordAlert.getParam(), recordAlert.getAttack(), recordAlert.getOtherInfo(), 
        		recordAlert.getSolution(), recordAlert.getReference(), 
        		recordAlert.getEvidence(), recordAlert.getCweId(), recordAlert.getWascId(),
        		null);
        setHistoryRef(ref);
	}
	/**
	 * @deprecated  (2.4.0) Replaced by {@link #setRiskConfidence(int, int)}.
	 * Use of reliability has been deprecated in favour of using confidence
	 * @param risk the new risk
	 * @param confidence the new confidence
	 */
	@Deprecated
	public void setRiskReliability(int risk, int confidence) {
		this.risk = risk;
		this.confidence = confidence;
	}
	
	public void setRiskConfidence(int risk, int confidence) {
		this.risk = risk;
		this.confidence = confidence;
	}
	/**
	 * @deprecated (2.5.0) Replaced by {@link #setName}.
	 * Use of alert has been deprecated in favour of using name.
	 * @param alert the new name
	 */
	@Deprecated
	public void setAlert(String alert) {
	    setName(alert);
	}
	/**
	 * Sets the name of the alert to name
	 * @param name the name to set for the alert
	 * @since 2.5.0
	 */
	public void setName(String name) {
	    if (name == null) return;
	    this.name = name;
	}
	
	/**
	 * Sets the details of the alert.
	 * 
	 * @param description the description of the alert
	 * @param uri the URI that has the issue
	 * @param param the parameter that has the issue
	 * @param attack the attack that triggers the issue
	 * @param otherInfo other information about the issue
	 * @param solution the solution for the issue
	 * @param reference references about the issue
	 * @param msg the HTTP message that triggers/triggered the issue
	 * @deprecated (2.2.0) Replaced by
	 *             {@link #setDetail(String, String, String, String, String, String, String, String, int, int, HttpMessage)}. It
	 *             will be removed in a future release.
	 */
	@Deprecated
	public void setDetail(String description, String uri, String param, String attack, String otherInfo, 
			String solution, String reference, HttpMessage msg) {
		setDetail(description, uri, param, attack, otherInfo, solution, reference, "", -1, -1, msg);
	}

	/**
	 * Sets the details of the alert.
	 * 
	 * @param description the description of the alert
	 * @param uri the URI that has the issue
	 * @param param the parameter that has the issue
	 * @param attack the attack that triggers the issue
	 * @param otherInfo other information about the issue
	 * @param solution the solution for the issue
	 * @param reference references about the issue
	 * @param evidence the evidence (in the HTTP response) that the issue exists
	 * @param cweId the CWE ID of the issue
	 * @param wascId the WASC ID of the issue
	 * @param msg the HTTP message that triggers/triggered the issue
	 * @since 2.2.0
	 */
	public void setDetail(String description, String uri, String param, String attack, String otherInfo, 
			String solution, String reference, String evidence, int cweId, int wascId, HttpMessage msg) {
		setDescription(description);
		setUri(uri);
		setParam(param);
		setAttack(attack);
		setOtherInfo(otherInfo);
		setSolution(solution);
		setReference(reference);
		setMessage(msg);
		setEvidence(evidence);
		setCweId(cweId);
		setWascId(wascId);
		if (msg != null) {
			setHistoryRef(msg.getHistoryRef());
		}
	}

	private void setDetail(String description, String uri, String param, String attack, String otherInfo, 
			String solution, String reference, HistoryReference href) {
		setDescription(description);
		setUri(uri);
		setParam(param);
		setAttack(attack);
		setOtherInfo(otherInfo);
		setSolution(solution);
		setReference(reference);
		setHistoryRef(href);
	}

	public void setUri(String uri) {
    	// ZAP: Cope with null
	    if (uri == null) return;
	    // ZAP: Changed to not create a new String.
		this.uri = uri;
	}
	
	
	public void setDescription(String description) {
	    if (description == null) return;
	    // ZAP: Changed to not create a new String.
		this.description = description;
	}
	
	public void setParam(String param) {
	    if (param == null) return;
	    // ZAP: Changed to not create a new String.
		this.param = param;
	}
	
	public void setOtherInfo(String otherInfo) {
	    if (otherInfo == null) return;
	    // ZAP: Changed to not create a new String.
		this.otherInfo = otherInfo;
	}

	public void setSolution(String solution) {
	    if (solution == null) return;
	    // ZAP: Changed to not create a new String.
		this.solution = solution;
	}

	public void setReference(String reference) {
	    if (reference == null) return;
	    // ZAP: Changed to not create a new String.
		this.reference = reference;
	}

	public void setMessage(HttpMessage message) {
	    if (message != null) {
	    	this.message = message;
	    	this.method = message.getRequestHeader().getMethod();
	    	this.postData = message.getRequestBody().toString();
	    	this.msgUri = message.getRequestHeader().getURI();
	    } else {
	    	// Used to clear the ref so we dont hold onto it
	    	this.message = null;
	    }
	}
	
	@Override
	public int compareTo(Alert alert2) {
		if (risk < alert2.risk) {
			return -1;
		} else if (risk > alert2.risk) {
			return 1;
		}
		
		if (confidence < alert2.confidence) {
			return -1;
		} else if (confidence > alert2.confidence) {
			return 1;
		}
		
		if (pluginId < alert2.pluginId) {
			return -1;
		} else if (pluginId > alert2.pluginId) {
			return 1;
		}

		int result = name.compareToIgnoreCase(alert2.name);
		if (result != 0) {
			return result;
		}

		result = method.compareToIgnoreCase(alert2.method);
		if (result != 0) {
			return result;
		}
		
		// ZAP: changed to compare the field uri with alert2.uri
		result = uri.compareToIgnoreCase(alert2.uri);
		if (result != 0) {
			return result;
		}
		
		// ZAP: changed to compare the field param with alert2.param
		result = param.compareToIgnoreCase(alert2.param);
		if (result != 0) {
			return result;
		}
		
		result = otherInfo.compareToIgnoreCase(alert2.otherInfo);
		if (result != 0) {
			return result;
		}

		result = compareStrings(evidence, alert2.evidence);
		if (result != 0) {
			return result;
		}

		return compareStrings(attack, alert2.attack);
	}

	private int compareStrings(String string, String otherString) {
		if (string == null) {
			if (otherString == null) {
				return 0;
			}
			return -1;
		} else if (otherString == null) {
			return 1;
		}
		return string.compareTo(otherString);
	}


	/**
	Override equals.  Alerts are equal if the plugin id, alert, other info, uri and param is the same.
	*/
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		Alert item = (Alert) obj;
		if (risk != item.risk) {
			return false;
		}
		if (confidence != item.confidence) {
			return false;
		}
		if (pluginId != item.pluginId) {
			return false;
		}
		if (!name.equals(item.name)) {
			return false;
		}
		if (!method.equalsIgnoreCase(item.method)) {
			return false;
		}
		if (!uri.equalsIgnoreCase(item.uri)) {
			return false;
		}
		if (!param.equalsIgnoreCase(item.param)) {
			return false;
		}
		if (!otherInfo.equalsIgnoreCase(item.otherInfo)) {
			return false;
		}
		if (evidence == null) {
			if (item.evidence != null) {
				return false;
			}
		} else if (!evidence.equals(item.evidence)) {
			return false;
		}
		if (attack == null) {
			if (item.attack != null) {
				return false;
			}
		} else if (!attack.equals(item.attack)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + risk;
		result = prime * result + confidence;
		result = prime * result + ((evidence == null) ? 0 : evidence.hashCode());
		result = prime * result + name.hashCode();
		result = prime * result + otherInfo.hashCode();
		result = prime * result + param.hashCode();
		result = prime * result + pluginId;
		result = prime * result + method.hashCode();
		result = prime * result + uri.hashCode();
		result = prime * result + ((attack == null) ? 0 : attack.hashCode());
		return result;
	}

	/**
	 * Creates a new instance of {@code Alert} with same members.
	 * @return a new {@code Alert} instance
	 */
	public Alert newInstance() {
		Alert item = new Alert(this.pluginId);
		item.setRiskConfidence(this.risk, this.confidence);
		item.setName(this.name);
		item.setDetail(this.description, this.uri, this.param, this.attack, this.otherInfo, this.solution, this.reference, this.historyRef);
		return item;
	}
	
	public String toPluginXML(String urls) {
		StringBuilder sb = new StringBuilder(150); // ZAP: Changed the type to StringBuilder.
		sb.append("<alertitem>\r\n");
		sb.append("  <pluginid>").append(pluginId).append("</pluginid>\r\n");
		sb.append("  <alert>").append(replaceEntity(name)).append("</alert>\r\n"); //Deprecated in 2.5.0, maintain for compatibility with custom code
		sb.append("  <name>").append(replaceEntity(name)).append("</name>\r\n");
		sb.append("  <riskcode>").append(risk).append("</riskcode>\r\n");
		sb.append("  <confidence>").append(confidence).append("</confidence>\r\n");
		sb.append("  <riskdesc>").append(replaceEntity(MSG_RISK[risk] + " (" + MSG_CONFIDENCE[confidence] + ")")).append("</riskdesc>\r\n");
        sb.append("  <desc>").append(replaceEntity(paragraph(description))).append("</desc>\r\n");

        sb.append(urls);

        sb.append("  <solution>").append(replaceEntity(paragraph(solution))).append("</solution>\r\n");
        // ZAP: Added otherInfo to the report
        if (otherInfo != null && otherInfo.length() > 0) {
            sb.append("  <otherinfo>").append(replaceEntity(paragraph(otherInfo))).append("</otherinfo>\r\n");
        }
		sb.append("  <reference>" ).append(replaceEntity(paragraph(reference))).append("</reference>\r\n");
		if (cweId > 0) {
			sb.append("  <cweid>" ).append(cweId).append("</cweid>\r\n");
		}
		if (wascId > 0) {
			sb.append("  <wascid>" ).append(wascId).append("</wascid>\r\n");
		}
		
		sb.append("</alertitem>\r\n");
		return sb.toString();
	}
   
	public String replaceEntity(String text) {
		String result = null;
		if (text != null) {
			result = ReportGenerator.entityEncode(text);
		}
		return result;
	}
	
	public String paragraph(String text) {
		return "<p>" + text.replaceAll("\\r\\n","</p><p>").replaceAll("\\n","</p><p>") + "</p>";
	}
	/**
	 * @deprecated (2.5.0) Replaced by {@link #getName}.
	 * Use of alert has been deprecated in favour of using name.
	 * @return Returns the alert.
	 */
	@Deprecated
    public String getAlert() {
        return name;
    }
	/**
	 * @return Returns the name of the alert.
	 * @since 2.5.0
	 */
    public String getName() {
        return name;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @return Returns the id.
     */
    public int getPluginId() {
        return pluginId;
    }
    /**
     * @return Returns the message.
     */
    public HttpMessage getMessage() {
    	if (this.message != null) {
    		return this.message;
    	}
    	if (this.historyRef != null) {
    		try {
				return this.historyRef.getHttpMessage();
			} catch (HttpMalformedHeaderException | DatabaseException e) {
	        	logger.error(e.getMessage(), e);
			}
    	}
        return null;
    }
    /**
     * @return Returns the otherInfo.
     */
    public String getOtherInfo() {
        return otherInfo;
    }
    /**
     * @return Returns the param.
     */
    public String getParam() {
        return param;
    }
    /**
     * @return Returns the reference.
     */
    public String getReference() {
        return reference;
    }
    /**
     * @deprecated (2.4.0) Replaced by {@link #getConfidence()}.
     * @return the reliability.
     */
    @Deprecated
    public int getReliability() {
        return confidence;
    }
    
    /**
     * @return Returns the confidence.
     */
    public int getConfidence() {
        return confidence;
    }
    
    /**
     * @return Returns the risk.
     */
    public int getRisk() {
        return risk;
    }
    
    public URL getIconUrl() {
    	if (confidence == Alert.CONFIDENCE_FALSE_POSITIVE) {
    		// Special case - theres no risk - use the green flag
			return Constant.OK_FLAG_IMAGE_URL;
    	}

    	switch (risk) {
    	case Alert.RISK_INFO:
    		return Constant.INFO_FLAG_IMAGE_URL;
    	case Alert.RISK_LOW:
    		return Constant.LOW_FLAG_IMAGE_URL;
    	case Alert.RISK_MEDIUM:
    		return Constant.MED_FLAG_IMAGE_URL;
    	case Alert.RISK_HIGH:
    		return Constant.HIGH_FLAG_IMAGE_URL;
    	}
    	return null;
    }
    /**
     * @return Returns the solution.
     */
    public String getSolution() {
        return solution;
    }
    /**
     * @return Returns the uri.
     */
    public String getUri() {
        return uri;
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
    
    public String getUrlParamXML() {
    	StringBuilder sb = new StringBuilder(200); // ZAP: Changed the type to StringBuilder.
        sb.append("  <uri>").append(replaceEntity(uri)).append("</uri>\r\n");
        sb.append("  <method>").append(replaceEntity(method)).append("</method>\r\n");
        if (param != null && param.length() > 0) {
        	sb.append("  <param>").append(replaceEntity(param)).append("</param>\r\n");
        }
        if (attack != null && attack.length() > 0) {
        	sb.append("  <attack>").append(replaceEntity(attack)).append("</attack>\r\n");
        }
        if (evidence != null && evidence.length() > 0) {
            sb.append("  <evidence>").append(replaceEntity(evidence)).append("</evidence>\r\n");
        }
        return sb.toString();
    }

	public int getSourceHistoryId() {
		return sourceHistoryId;
	}

	public void setSourceHistoryId(int sourceHistoryId) {
		this.sourceHistoryId = sourceHistoryId;
	}
	
	public HistoryReference getHistoryRef () {
		return this.historyRef;
	}

	public void setHistoryRef(HistoryReference historyRef) {
		this.historyRef = historyRef;
		if (historyRef != null) {
			this.message = null;
			this.method = historyRef.getMethod();
			this.msgUri = historyRef.getURI();
			this.postData = historyRef.getRequestBody();
			this.sourceHistoryId = historyRef.getHistoryId();
		}
	}

	public String getAttack() {
		return attack;
	}

	public void setAttack(String attack) {
		this.attack = attack;
	}

	public String getMethod() {
		return method;
	}

	public String getPostData() {
		return postData;
	}

	public URI getMsgUri() {
		return msgUri;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	public int getCweId() {
		return cweId;
	}

	public void setCweId(int cweId) {
		this.cweId = cweId;
	}

	public int getWascId() {
		return wascId;
	}

	public void setWascId(int wascId) {
		this.wascId = wascId;
	}
    
}	