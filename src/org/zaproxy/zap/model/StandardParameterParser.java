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
package org.zaproxy.zap.model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;

public class StandardParameterParser implements ParameterParser {
	
	private static final String CONFIG_KV_PAIR_SEPARATORS = "kvps";
	private static final String CONFIG_KV_SEPARATORS = "kvs";
	private static final String CONFIG_STRUCTURAL_PARAMS = "struct";

	private Context context;
	private Pattern keyValuePairSeparatorPattern;
	private Pattern keyValueSeparatorPattern;
	private String keyValuePairSeparators;
	private String keyValueSeparators;
	private List<String> structuralParameters = new ArrayList<String>();

    private static Logger log = Logger.getLogger(StandardParameterParser.class);

	public StandardParameterParser(String keyValuePairSeparators, String keyValueSeparators) throws PatternSyntaxException {
		super();
		this.setKeyValuePairSeparators(keyValuePairSeparators);
		this.setKeyValueSeparators(keyValueSeparators);
	}

	public StandardParameterParser() {
		this("&", "=");
	}

	private Pattern getKeyValuePairSeparatorPattern() {
		return this.keyValuePairSeparatorPattern;
	}

	private Pattern getKeyValueSeparatorPattern() {
		return this.keyValueSeparatorPattern;
	}

	@Override
	public void init(String config) {
		try {
			JSONObject json = JSONObject.fromObject(config);
			this.setKeyValuePairSeparators(json.getString(CONFIG_KV_PAIR_SEPARATORS));
			this.setKeyValueSeparators(json.getString(CONFIG_KV_SEPARATORS));
			JSONArray ja = json.getJSONArray(CONFIG_STRUCTURAL_PARAMS);
			for (Object obj : ja.toArray()) {
				this.structuralParameters.add(obj.toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@Override
	public String getConfig() {
		JSONObject json = new JSONObject();
		json.put(CONFIG_KV_PAIR_SEPARATORS, this.getKeyValuePairSeparators());
		json.put(CONFIG_KV_SEPARATORS, this.getKeyValueSeparators());
		
		JSONArray ja = new JSONArray();
		ja.addAll(this.structuralParameters);
		json.put(CONFIG_STRUCTURAL_PARAMS, ja);
		
		return json.toString();
	}
		
	@Override
	public Map<String, String> getParams(HttpMessage msg, HtmlParameter.Type type) {
		Map<String, String> map = new HashMap<String, String>();
	    if (msg == null) {
	    	return map;
	    }
	    try {
			switch (type) {
			case form:	return this.parse(msg.getRequestBody().toString());
			case url:	return this.parse(msg.getRequestHeader().getURI().getQuery());
			default:
						throw new InvalidParameterException("Type not supported: " + type);
			}
		} catch (URIException e) {
			log.error(e.getMessage(), e);
		}
		return map;
	}

	private void setKeyValueSeparatorPattern(Pattern keyValueSeparatorPattern) {
		this.keyValueSeparatorPattern = keyValueSeparatorPattern;
	}

	private void setKeyValuePairSeparatorPattern(Pattern keyValuePairSeparatorPattern) {
		this.keyValuePairSeparatorPattern = keyValuePairSeparatorPattern;
	}
	
	public String getKeyValuePairSeparators() {
		return keyValuePairSeparators;
	}

	public void setKeyValuePairSeparators(String keyValuePairSeparators) throws PatternSyntaxException {
		this.setKeyValuePairSeparatorPattern(Pattern.compile("[" + keyValuePairSeparators + "]"));
		this.keyValuePairSeparators = keyValuePairSeparators;
	}

	public String getKeyValueSeparators() {
		return keyValueSeparators;
	}

	public void setKeyValueSeparators(String keyValueSeparators) throws PatternSyntaxException {
		this.setKeyValueSeparatorPattern(Pattern.compile("[" + keyValueSeparators + "]"));
		this.keyValueSeparators = keyValueSeparators;
	}
	
	@Override
	public String getDefaultKeyValuePairSeparator() {
		if (this.keyValuePairSeparators != null && this.keyValuePairSeparators.length() > 0) {
			return this.keyValuePairSeparators.substring(0, 1);
		}
		// The default
		return "&";
	}

	@Override
	public String getDefaultKeyValueSeparator() {
		if (this.keyValueSeparators != null && this.keyValueSeparators.length() > 0) {
			return this.keyValueSeparators.substring(0, 1);
		}
		// The default
		return "=";
	}


	public List<String> getStructuralParameters() {
		return Collections.unmodifiableList(structuralParameters);
	}

	public void setStructuralParameters(List<String> structuralParameters) {
		this.structuralParameters.clear();
		this.structuralParameters.addAll(structuralParameters);
	}

	@Override
	public Map<String, String> parse(String paramStr) {
		Map<String, String> map = new HashMap<String, String>();
	    
		if (paramStr != null) {
		    String[] keyValue = this.getKeyValuePairSeparatorPattern().split(paramStr);
			for (int i=0; i<keyValue.length; i++) {
				try {
				    String[] keyEqValue = this.getKeyValueSeparatorPattern().split(keyValue[i]);
				    if (keyEqValue.length == 1) {
						map.put(keyEqValue[0], "");
				    } else if (keyEqValue.length > 1) {
						map.put(keyEqValue[0], keyEqValue[1]);
				    }
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return map;
	}
	
	@Override
	public StandardParameterParser clone() {
		StandardParameterParser spp = new StandardParameterParser(this.getKeyValuePairSeparators(), this.getKeyValueSeparators());
		spp.setStructuralParameters(this.getStructuralParameters());
		return spp;
	}

	@Override
	public List<String> getTreePath(URI uri) throws URIException {
		return this.getTreePath(uri, true);
	}

	private List<String> getTreePath(URI uri, boolean incStructParams) throws URIException {
		String path = uri.getPath();
		List<String> list = new ArrayList<String>();
		if (path != null) {
			Context context = this.getContext();
			if (context != null) {
				String uriStr = uri.toString();
				boolean changed = false;
				for (StructuralNodeModifier ddn : context.getDataDrivenNodes()) {
					Matcher m = ddn.getPattern().matcher(uriStr);
					if (m.find()){ 
						if (m.groupCount() == 3) {
							path = m.group(1) + SessionStructure.DATA_DRIVEN_NODE_PREFIX + 
									ddn.getName() + SessionStructure.DATA_DRIVEN_NODE_POSTFIX + 
									m.group(3);
							if (!path.startsWith("/")) {
								// Should always start with a slash;)
								path = "/" + path;
							}
							changed = true;
						} else if (m.groupCount() == 2) {
							path = m.group(1) + SessionStructure.DATA_DRIVEN_NODE_PREFIX + 
									ddn.getName() + SessionStructure.DATA_DRIVEN_NODE_POSTFIX;
							if (!path.startsWith("/")) {
								// Should always start with a slash;)
								path = "/" + path;
							}
							changed = true;
						}
					}
				}
				if (changed) {
					log.debug("Changed path from " + uri.getPath() + " to " + path);
				}
			}
			
			// Note: Start from the 2nd path element as the first on is always the empty string due
			// to the split
			String[] pathList = path.split("/");
			for (int i = 1; i < pathList.length; i++) {
				list.add(pathList[i]);
			}
		}
		if (incStructParams) {
			// Add any structural params (url param) in key order
			Map<String, String> urlParams = this.parse(uri.getQuery());
			List<String> keys = new ArrayList<String>(urlParams.keySet());
			Collections.sort(keys);
			for (String key: keys) {
				if (this.structuralParameters.contains(key)) {
					list.add(urlParams.get(key));
				}
			}
		}

		return list;
	}
	
	@Override
	public List<String> getTreePath(HttpMessage msg) throws URIException {
		URI uri = msg.getRequestHeader().getURI();

		List<String> list = getTreePath(uri);
		
		// Add any structural params (form params) in key order
		Map<String, String> formParams = this.parse(msg.getRequestBody().toString());
		List<String> keys = new ArrayList<String>(formParams.keySet());
		Collections.sort(keys);
		for (String key: keys) {
			if (this.structuralParameters.contains(key)) {
				list.add(formParams.get(key));
			}
		}

		return list;
	}

	@Override
	public String getAncestorPath(URI uri, int depth) throws URIException {
		// If the depth is 0, return an empty path
		String path = uri.getPath();
		if (depth == 0 || path == null) {
			return "";
		}
		List<String> pathList = getTreePath(uri, false);

		// Add the 'normal' (plus data driven) path elements 
		// until we finish them or we reach the desired depth
		StringBuilder parentPath = new StringBuilder(path.length());
		for (int i = 0; i < pathList.size() && depth > 0; i++, depth--) {
			String element = pathList.get(i);
			parentPath.append('/');
			if (element.startsWith(SessionStructure.DATA_DRIVEN_NODE_PREFIX)) {
				// Its a data driven node - use the regex pattern instead
				parentPath.append(SessionStructure.DATA_DRIVEN_NODE_REGEX);
			} else {
				parentPath.append(element);
			}
		}
		// If we're done or we have no structural parameters, just return
		if (depth == 0 || structuralParameters.isEmpty()) {
			return parentPath.toString();
		}

		// Add the 'structural params' path elements
		boolean firstElement = true;
		Map<String, String> urlParams = this.parse(uri.getQuery());
		for (Entry<String, String> param : urlParams.entrySet()) {
			if (this.structuralParameters.contains(param.getKey())) {
				if (firstElement) {
					firstElement = false;
					parentPath.append('?');
				} else {
					parentPath.append(keyValuePairSeparators);
				}
				parentPath.append(param.getKey()).append(keyValueSeparators).append(param.getValue());
				if ((--depth) == 0) {
					break;
				}
			}
		}
		return parentPath.toString();
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public Context getContext() {
		return context;
	}

}
