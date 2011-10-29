/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP development team
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
package org.zaproxy.zap.extension.params;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

import org.parosproxy.paros.db.RecordParam;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;

public class SiteParameters {
	private ExtensionParams extension;
	private String site;
	private ParamsTableModel model = new ParamsTableModel();
	private Map<String, HtmlParameterStats> cookieParams = new HashMap<String, HtmlParameterStats>();
	private Map<String, HtmlParameterStats> urlParams = new HashMap<String, HtmlParameterStats>();
	private Map<String, HtmlParameterStats> formParams = new HashMap<String, HtmlParameterStats>();
	
	public SiteParameters(ExtensionParams extension, String site) {
		this.extension = extension;
		this.site = site;
	}
	
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	
	public HtmlParameterStats getParam(HtmlParameter.Type type, String name) {
		switch (type) {
		case cookie: return cookieParams.get(name);
		case url: return  urlParams.get(name);
		case form: return  formParams.get(name);
		}
		return null;
	}
	
	public HtmlParameterStats addParam(String site, HtmlParameter param) {
		Map<String, HtmlParameterStats> params = null;
		HtmlParameterStats p;
		
		switch (param.getType()) {
		case cookie: params = cookieParams; break;
		case url: params = urlParams; break;
		case form: params = formParams; break;
		}
		
		if (params.containsKey(param.getName())) {
			p = params.get(param.getName());
			p.incTimesUsed();
			p.addValue(param.getValue());
		} else {
			// Its a new one
			p = new HtmlParameterStats(site, param.getName(), param.getType(), param.getValue(), param.getFlags());
			// Flag standard session ids
			if ( ! param.getType().equals(Type.form) && 
					this.extension.getStdSessionParamNames().contains(param.getName().toLowerCase())) {
				// Cookies and URL params can be session params
				p.addFlag("session");
			}
			
			params.put(param.getName(), p);
			model.addHtmlParameterStats(p);
		}
		return p;
	}

	public ParamsTableModel getModel() {
		return model;
	}
	
	private Set<String> stringToSet (String str) {
		Set<String> set = new HashSet<String>();
		// TODO handle encoded commas?
		String[] array = str.split(",");
		for (String s : array) {
			set.add(s);
		}
		return set;
	}

	public void addParam(String site2, RecordParam param) {
		Map<String, HtmlParameterStats> params = null;
		
		HtmlParameter.Type type = HtmlParameter.Type.valueOf(param.getType());
		switch (type ) {
		case cookie: params = cookieParams; break;
		case url: params = urlParams; break;
		case form: params = formParams; break;
		}
		// These should all be new
		HtmlParameterStats p = new HtmlParameterStats(param.getParamId(), site, param.getName(), param.getType(), param.getUsed(), 
									stringToSet(param.getValues()), stringToSet(param.getFlags()));
		params.put(param.getName(), p);
		model.addHtmlParameterStats(p);
		
	}

	public JSONArray toJSON() {
		JSONArray result = new JSONArray();

		Collection<HtmlParameterStats> cookieValues = cookieParams.values();
		for (HtmlParameterStats param : cookieValues) {
			result.add(param.toJSON());
		}
		Collection<HtmlParameterStats> urlValues = urlParams.values();
		for (HtmlParameterStats param : urlValues) {
			result.add(param.toJSON());
		}
		Collection<HtmlParameterStats> formValues = formParams.values();
		for (HtmlParameterStats param : formValues) {
			result.add(param.toJSON());
		}
		return result;
	}
}
