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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.parosproxy.paros.db.RecordParam;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;

public class SiteParameters {
	private ExtensionParams extension;
	private String site;
	private ParamsTableModel model = new ParamsTableModel();
	private Map<String, HtmlParameterStats> cookieParams = new HashMap<>();
	private Map<String, HtmlParameterStats> urlParams = new HashMap<>();
	private Map<String, HtmlParameterStats> formParams = new HashMap<>();

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

	/**
	 * Tells whether or not this site has any parameters (cookies, query or form parameters).
	 *
	 * @return {@code true} if this site has parameters, {@code false} otherwise.
	 * @since 2.5.0
	 */
	public boolean hasParams() {
		return !cookieParams.isEmpty() || !urlParams.isEmpty() || !formParams.isEmpty();
	}

	public HtmlParameterStats getParam(HtmlParameter.Type type, String name) {
		switch (type) {
		case cookie:
			return cookieParams.get(name);
		case url:
			return urlParams.get(name);
		case form:
			return formParams.get(name);
		}
		return null;
	}

	public List<HtmlParameterStats> getParams(HtmlParameter.Type type) {
		List<HtmlParameterStats> params = new ArrayList<>();
		switch (type) {
		case cookie:
			for (HtmlParameterStats param : this.cookieParams.values()) {
				params.add(param);
			}
			break;
		case url:
			for (HtmlParameterStats param : this.urlParams.values()) {
				params.add(param);
			}
			break;
		case form:
			for (HtmlParameterStats param : this.formParams.values()) {
				params.add(param);
			}
			break;
		}
		return params;
	}

	public List<HtmlParameterStats> getParams() {
		List<HtmlParameterStats> params = new ArrayList<>();
		for (HtmlParameterStats param : this.cookieParams.values()) {
			params.add(param);
		}
		for (HtmlParameterStats param : this.urlParams.values()) {
			params.add(param);
		}
		for (HtmlParameterStats param : this.formParams.values()) {
			params.add(param);
		}
		return params;
	}

	public HtmlParameterStats addParam(String site, HtmlParameter param, HttpMessage msg) {
		Map<String, HtmlParameterStats> params = null;
		HtmlParameterStats p;

		switch (param.getType()) {
		case cookie:
			params = cookieParams;
			break;
		case url:
			params = urlParams;
			break;
		case form:
			params = formParams;
			break;
		}

		if (params != null && params.containsKey(param.getName())) {
			p = params.get(param.getName());
			p.incTimesUsed();
			p.addValue(param.getValue());
		} else {
			// It's a new parameter
			p = new HtmlParameterStats(site, param.getName(), param.getType(), param.getValue(), param.getFlags());

			// If the HttpSessions extension is active, check if the token is a session token and,
			// if it is, mark it so
			ExtensionHttpSessions extSession = extension.getExtensionHttpSessions();
			if (extSession != null) {
				if (param.getType().equals(Type.cookie) && extSession.isSessionToken(site, param.getName())) {
					// Only Cookies can be session params
					// TODO: Add support for URL tokens
					p.addFlag(HtmlParameter.Flags.session.name());
				}
			}

			if (params == null) {
				params = new HashMap<>();
			}
			params.put(param.getName(), p);
			model.addHtmlParameterStats(p);
		}
		return p;
	}

	public ParamsTableModel getModel() {
		return model;
	}

	private Set<String> stringToSet(String str) {
		Set<String> set = new HashSet<>();
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
		switch (type) {
		case cookie:
			params = cookieParams;
			break;
		case url:
			params = urlParams;
			break;
		case form:
			params = formParams;
			break;
		}
		// These should all be new
		HtmlParameterStats p = new HtmlParameterStats(param.getParamId(), site, param.getName(), param.getType(),
				param.getUsed(), stringToSet(param.getValues()), stringToSet(param.getFlags()));
		if (params != null) {
			params.put(param.getName(), p);
			model.addHtmlParameterStats(p);
		}
	}
}
