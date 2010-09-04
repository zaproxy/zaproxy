/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.parosproxy.paros.extension.history;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;

public class HistoryFilter {

	public static final String NOTES_IGNORE = "Ignore";
	public static final String NOTES_PRESENT = "Present";
	public static final String NOTES_ABSENT = "Absent";
	public static final String [] NOTES_OPTIONS = {NOTES_IGNORE, NOTES_PRESENT, NOTES_ABSENT};
	
	private List<String> methodList = new ArrayList<String>();
	private List<Integer> codeList = new ArrayList<Integer>();
	private List<String> riskList = new ArrayList<String>();
	private List<String> reliabilityList = new ArrayList<String>();
	private List<String> tagList = new ArrayList<String>();
	private String note = null;
	
	private List<String> convertStr(Object[] objects) {
		List<String> list = new ArrayList<String>();
		for (Object obj : objects) {
			list.add(obj.toString());
		}
		return list;
	}
	private List<Integer> convertInt(Object[] objects) {
		List<Integer> list = new ArrayList<Integer>();
		for (Object obj : objects) {
			list.add(Integer.parseInt(obj.toString()));
		}
		return list;
	}
	public void setMethods(Object[] methods) {
		methodList.clear();
		methodList.addAll(convertStr(methods));
	}
	public void setCodes(Object[] codes) {
		codeList.clear();
		codeList.addAll(convertInt(codes));
	}
	public void setTags(Object[] tags) {
		tagList.clear();
		tagList.addAll(convertStr(tags));
	}
	public void setRisks(Object[] risks) {
		riskList.clear();
		riskList.addAll(convertStr(risks));
	}
	public void setReliabilities(Object[] reliabilities) {
		reliabilityList.clear();
		reliabilityList.addAll(convertStr(reliabilities));
	}
	
	public void reset () {
		this.methodList.clear();
		this.codeList.clear();
		this.tagList.clear();
		this.riskList.clear();
		this.reliabilityList.clear();
		this.note = null;
	}
	
	public boolean matches(HistoryReference historyRef) {
		try {
			if (methodList.size() > 0 && 
					! methodList.contains(historyRef.getHttpMessage().
							getRequestHeader().getMethod())) {
				return false;
			}
			if (codeList.size() > 0 &&  
					! codeList.contains(Integer.valueOf(historyRef.getHttpMessage().
							getResponseHeader().getStatusCode()))) {
				return false;
			}
			boolean foundTag = false;
			List <String> historyTags = historyRef.getTags();
			if (tagList.size() > 0) {
				for (String tag: historyTags) {
					if (tagList.contains(tag)) {
						foundTag = true;
					}
				}
				if (! foundTag) {
					return false;
				}
			}
			boolean foundAlert = false;
			List <Alert> historyAlerts = historyRef.getAlerts();
			if (riskList.size() > 0 || reliabilityList.size() > 0) {
				for (Alert alert: historyAlerts) {
					if ((riskList.size() == 0 || 
							riskList.contains(Alert.MSG_RISK[alert.getRisk()])) &&
						(reliabilityList.size() == 0 ||
							reliabilityList.contains(Alert.MSG_RELIABILITY[alert.getReliability()]))) {
						foundAlert = true;
					}
				}
				if (! foundAlert) {
					return false;
				}
			}
			if (note != null && ! note.equals(NOTES_IGNORE)) {
				String noteStr = historyRef.getHttpMessage().getNote();
				boolean notePresent = noteStr != null && noteStr.length() > 0;
				if (note.equals(NOTES_PRESENT) != notePresent) {
					return false;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	public String toShortString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter: ");
		boolean empty = true;
		if (methodList.size() > 0) {
			if (empty) {
				sb.append("ON ");
			}
			empty = false;
			sb.append("Methods");
		}
		if (codeList.size() > 0) {
			if (empty) {
				sb.append("ON ");
			} else {
				sb.append(", ");
			}
			empty = false;
			sb.append("Codes");
		}
		if (tagList.size() > 0) {
			if (empty) {
				sb.append("ON ");
			} else {
				sb.append(", ");
			}
			empty = false;
			sb.append("Tags");
		}
		if (riskList.size() > 0 || reliabilityList.size() > 0) {
			if (empty) {
				sb.append("ON ");
			} else {
				sb.append(", ");
			}
			empty = false;
			sb.append("Alerts");
		}
		if (note != null && ! note.equals(NOTES_IGNORE)) {
			if (empty) {
				sb.append("ON ");
			} else {
				sb.append(", ");
			}
			empty = false;
			sb.append("Notes");
		}
		if (empty) {
			sb.append("OFF");
		}
		return sb.toString();
	}
	
	public String toLongString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter: ");
		boolean empty = true;
		if (methodList.size() > 0) {
			empty = false;
			sb.append("Methods: ");
			for (String method : methodList) {
				sb.append(method);
				sb.append(" ");
			}
		}
		if (codeList.size() > 0) {
			empty = false;
			sb.append("Codes: ");
			Integer lastCode = null;
			boolean inBlock = false;
			for (Integer code : codeList) {
				if (lastCode == null) {
					// very first one
					sb.append(code);
				} else if (code.intValue() == lastCode.intValue() + 1) {
					// next in sequence
					inBlock = true;
				} else if (inBlock){
					// no longer in a consecutive set of codes
					sb.append("-");
					sb.append(lastCode);
					sb.append(" ");
					sb.append(code);
					inBlock = false;
				} else {
					// Not in a block of codes
					sb.append(" ");
					sb.append(code);
				}
				lastCode = code;
			}
			if (inBlock) {
				// finish off the series
				sb.append("-");
				sb.append(lastCode);
				sb.append(" ");
			}
		}
		if (tagList.size() > 0) {
			empty = false;
			sb.append("Tags: ");
			for (String tag : tagList) {
				sb.append(tag);
				sb.append(" ");
			}
		}
		if (riskList.size() > 0 || reliabilityList.size() > 0) {
			empty = false;
			sb.append("Alerts: ");
			for (String risk : riskList) {
				sb.append(risk);
				sb.append(" ");
			}
			for (String rel : reliabilityList) {
				sb.append(rel);
				sb.append(" ");
			}
		}
		if (note != null && ! note.equals(NOTES_IGNORE)) {
			empty = false;
			sb.append("Notes: " + note);
		}
		if (empty) {
			sb.append("OFF");
		}
		return sb.toString();
	}
	
	public void setNote(Object selectedItem) {
		if (selectedItem == null) {
			note = null;
		} else {
			note = selectedItem.toString();
		}
	}
}
