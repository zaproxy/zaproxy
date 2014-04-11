package org.zaproxy.zap.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VulnerabilitiesI18NMap {
	
	private Map<String, List<Vulnerability>> vulnerabilitiesListI18NMap = new HashMap<String, List<Vulnerability>>();
	private Map<String, HashMap<String, Vulnerability>> vulnerabilitiesMapI18NMap = new HashMap<String, HashMap<String, Vulnerability>>();
	
	public void putVulnerabilitiesList(String localName, List<Vulnerability> list) {
		vulnerabilitiesListI18NMap.put(localName, list);
		vulnerabilitiesMapI18NMap.put(localName, createVulnerabilitiesMap(list));
	}

	private HashMap<String, Vulnerability> createVulnerabilitiesMap(List<Vulnerability> list) {
		HashMap<String, Vulnerability> map = new HashMap<String, Vulnerability>();
		for (Vulnerability vulnerability : list) {
			map.put(vulnerability.getId(), vulnerability);	
		}
		return map;
	}

	/**
	 * Return the vulnerabilities list for a the locale as defined by Constant.getLocale. If 
	 * there is no specific local, fall back on the default.
	 * @return
	 */
	public List<Vulnerability> getVulnerabilityList(String locale) {
		List<Vulnerability> list = vulnerabilitiesListI18NMap.get(locale);
		
		if (list != null) {
			return list;
		} else {
			return vulnerabilitiesListI18NMap.get(null);
		}
	}
	
	public Vulnerability getVulnerabilityByName(String name, String locale) {
		HashMap<String, Vulnerability> map = vulnerabilitiesMapI18NMap.get(locale);
		if (map == null) {
			map = vulnerabilitiesMapI18NMap.get(null);
		}
		
		return map.get(name);
	}
}
