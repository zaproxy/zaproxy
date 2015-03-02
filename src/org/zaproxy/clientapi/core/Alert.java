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


public class Alert {

    public enum Risk {Informational, Low, Medium, High};
	/**
	 * @deprecated (2.4.0) Replaced by {@link Confidence}.
	 * Use of reliability has been deprecated in favour of using confidence.
	 */
	@Deprecated
	public enum Reliability {Suspicious, Warning};
	public enum Confidence {Low, Medium, High, Confirmed};
	
	private String alert;
	private Risk risk;
	/**
	 * @deprecated (2.4.0) Replaced by {@link Confidence}.
	 * Use of reliability has been deprecated in favour of using confidence
	 */
	@Deprecated
	private Reliability reliability;
	private Confidence confidence;
	private String url;
	private String other;
	private String param;
    private String attack;
    private String evidence;
    private String description;
    private String reference;
    private String solution;
    private int cweId;
    private int wascId;
	
    public Alert(String alert, String url, String riskStr, String confidenceStr,
                 String param, String other) {
        super();
        this.alert = alert;
        this.url = url;
        this.other = other;
        this.param = param;
        if (riskStr != null) {
            this.risk = Risk.valueOf(riskStr);
        }
        if (confidenceStr != null) {
            this.confidence = Confidence.valueOf(confidenceStr);
        }
    }
	
	public Alert(String alert, String url, Risk risk, Confidence confidence, 
			String param, String other, String attack, String description, String reference, String solution,
			String evidence, int cweId, int wascId) {
		super();
		this.alert = alert;
		this.risk = risk;
		this.confidence = confidence;
		this.url = url;
		this.other = other;
		this.param = param;
        this.attack = attack;
        this.description = description;
        this.reference = reference;
        this.solution = solution;
        this.evidence = evidence;
        this.cweId = cweId;
        this.wascId = wascId;
	}

    public Alert(String alert, String url, Risk risk, Confidence confidence,
                 String param, String other) {
        super();
        this.alert = alert;
        this.risk = risk;
        this.confidence = confidence;
        this.url = url;
        this.other = other;
        this.param = param;
    }
	
	public Alert(String alert, String url, Risk risk, Confidence confidence) {
		super();
		this.alert = alert;
		this.risk = risk;
		this.confidence = confidence;
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
	/**
	 * @deprecated
	 * {@link #getConfidence()}
	 * Use of reliability has been deprecated in favour of using confidence
	 */
	@Deprecated
	public Reliability getReliability() {
		return reliability;
	}
	/**
	 * @deprecated
	 * {@link #setConfidence(Confidence)}
	 * Use of reliability has been deprecated in favour of using confidence
	 */
	@Deprecated
	public void setReliability(Reliability reliability) {
		this.reliability = reliability;
	}
	/**
	 * @deprecated
	 * {@link #setConfidence(String)}
	 * Use of reliability has been deprecated in favour of using confidence
	 */
	@Deprecated
	public void setReliability(String reliability) {
		this.reliability = Reliability.valueOf(reliability);
	}
	public Confidence getConfidence() {
		return confidence;
	}
	public void setConfidence(Confidence confidence) {
		this.confidence = confidence;
	}
	public void setConfidence(String confidence) {
		this.confidence = Confidence.valueOf(confidence);
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

	public String getEvidence() {
		return evidence;
	}

	public int getCweId() {
		return cweId;
	}

	public int getWascId() {
		return wascId;
	}

	public boolean matches (Alert alertFilter) {
		boolean matches = true;
		if (alertFilter.getAlert() != null && ! alertFilter.getAlert().equals(alert) ) {
			matches = false;
		}
		if (alertFilter.getRisk() != null && ! alertFilter.getRisk().equals(risk) ) {
			matches = false;
		}
		if (alertFilter.getConfidence() != null && ! alertFilter.getConfidence().equals(confidence) ) {
			matches = false;
		}

		return matches;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alert == null) ? 0 : alert.hashCode());
		result = prime * result + ((attack == null) ? 0 : attack.hashCode());
		result = prime * result + cweId;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((evidence == null) ? 0 : evidence.hashCode());
		result = prime * result + ((other == null) ? 0 : other.hashCode());
		result = prime * result + ((param == null) ? 0 : param.hashCode());
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		result = prime * result + ((confidence == null) ? 0 : confidence.hashCode());
		result = prime * result + ((risk == null) ? 0 : risk.hashCode());
		result = prime * result + ((solution == null) ? 0 : solution.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + wascId;
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		Alert otherAlert = (Alert) object;
		if (alert == null) {
			if (otherAlert.alert != null) {
				return false;
			}
		} else if (!alert.equals(otherAlert.alert)) {
			return false;
		}
		if (attack == null) {
			if (otherAlert.attack != null) {
				return false;
			}
		} else if (!attack.equals(otherAlert.attack)) {
			return false;
		}
		if (cweId != otherAlert.cweId) {
			return false;
		}
		if (description == null) {
			if (otherAlert.description != null) {
				return false;
			}
		} else if (!description.equals(otherAlert.description)) {
			return false;
		}
		if (evidence == null) {
			if (otherAlert.evidence != null) {
				return false;
			}
		} else if (!evidence.equals(otherAlert.evidence)) {
			return false;
		}
		if (this.other == null) {
			if (otherAlert.other != null) {
				return false;
			}
		} else if (!this.other.equals(otherAlert.other)) {
			return false;
		}
		if (param == null) {
			if (otherAlert.param != null) {
				return false;
			}
		} else if (!param.equals(otherAlert.param)) {
			return false;
		}
		if (reference == null) {
			if (otherAlert.reference != null) {
				return false;
			}
		} else if (!reference.equals(otherAlert.reference)) {
			return false;
		}
		if (confidence != otherAlert.confidence) {
			return false;
		}
		if (risk != otherAlert.risk) {
			return false;
		}
		if (solution == null) {
			if (otherAlert.solution != null) {
				return false;
			}
		} else if (!solution.equals(otherAlert.solution)) {
			return false;
		}
		if (url == null) {
			if (otherAlert.url != null) {
				return false;
			}
		} else if (!url.equals(otherAlert.url)) {
			return false;
		}
		if (wascId != otherAlert.wascId) {
			return false;
		}
		return true;
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
		sb.append("Confidence: ");
		if (getConfidence() != null) {
			sb.append(getConfidence().name());
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
