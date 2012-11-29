/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.core;

import net.sf.json.JSONObject;

import java.util.HashMap;

public class Alert {

    public enum Risk {Informational, Low, Medium, High};
	public enum Reliability {Suspicious, Warning};
	
	private String alert;
	private Risk risk;
	private Reliability reliability;
	private String url;
	private String other;
	private String param;
    private String attack;
    private String description;
    private String reference;
    private String solution;
	
	public Alert (JSONObject json) {
		this(json.get("alert").toString(), 
				json.get("url").toString(), 
				Risk.valueOf(json.get("risk").toString()), 
				Reliability.valueOf(json.get("reliability").toString()), 
				json.get("param").toString(), 
				json.get("other").toString(),
                json.get("attack").toString(),
                json.get("description").toString(),
                json.get("reference").toString(),
                json.get("solution").toString());
	}

    public Alert(String alert, String url, String riskStr, String reliabilityStr,
                 String param, String other) {
        super();
        this.alert = alert;
        this.url = url;
        this.other = other;
        this.param = param;
        if (riskStr != null) {
            this.risk = Risk.valueOf(riskStr);
        }
        if (reliabilityStr != null) {
            this.reliability = Reliability.valueOf(reliabilityStr);
        }
    }
	
	public Alert(String alert, String url, Risk risk, Reliability reliability, 
			String param, String other, String attack, String description, String reference, String solution) {
		super();
		this.alert = alert;
		this.risk = risk;
		this.reliability = reliability;
		this.url = url;
		this.other = other;
		this.param = param;
        this.attack = attack;
        this.description = description;
        this.reference = reference;
        this.solution = solution;
	}

    public Alert(String alert, String url, Risk risk, Reliability reliability,
                 String param, String other) {
        super();
        this.alert = alert;
        this.risk = risk;
        this.reliability = reliability;
        this.url = url;
        this.other = other;
        this.param = param;
    }
	
	public Alert(String alert, String url, Risk risk, Reliability reliability) {
		super();
		this.alert = alert;
		this.risk = risk;
		this.reliability = reliability;
		this.url = url;
	}

	public Alert(String alert, String url) {
		super();
		this.alert = alert;
		this.url = url;
	}

	public String getAlert() {
		return alert;
	}
	public void setAlert(String alert) {
		this.alert = alert;
	}
	public Risk getRisk() {
		return risk;
	}
	public void setRisk(Risk risk) {
		this.risk = risk;
	}
	public void setRisk(String risk) {
		this.risk = Risk.valueOf(risk);
	}
	public Reliability getReliability() {
		return reliability;
	}
	public void setReliability(Reliability reliability) {
		this.reliability = reliability;
	}
	public void setReliability(String reliability) {
		this.reliability = Reliability.valueOf(reliability);
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}

    public String getAttack() {
        return attack;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getSolution() {
        return solution;
    }

	public boolean matches (Alert alertFilter) {
		boolean matches = true;
		if (alertFilter.getAlert() != null && ! alertFilter.getAlert().equals(alert) ) {
			matches = false;
		}
		if (alertFilter.getRisk() != null && ! alertFilter.getRisk().equals(risk) ) {
			matches = false;
		}
		if (alertFilter.getReliability() != null && ! alertFilter.getReliability().equals(reliability) ) {
			matches = false;
		}

		return matches;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\tAlert: ").append(getAlert()).append(", ");
		sb.append("Risk: ");
		if (getRisk() != null) {
			sb.append(getRisk().name());
		} else {
			sb.append("null");
		}
		sb.append(", ");
		sb.append("Reliability: ");
		if (getReliability() != null) {
			sb.append(getReliability().name());
		} else {
			sb.append("null");
		}
		sb.append(", ");
		sb.append("Url: ").append(getUrl()).append(", ");
		sb.append("Param: ").append(getParam()).append(", ");
		sb.append("Other: ").append(getOther());
		return sb.toString();
	}
	
}
