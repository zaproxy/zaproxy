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
package org.zaproxy.zap.extension.api;

import java.util.ArrayList;
import java.util.List;

public class ApiElement {

	private String name = null;
	private String descriptionTag = null;
	private List<String> mandatoryParamNames = new ArrayList<>();
	private List<String> optionalParamNames = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApiElement(String name) {
		super();
		this.name = name;
	}
	
	public ApiElement(String name, List<String> mandatoryParamNames) {
		this(name, mandatoryParamNames, null);
	}
	
	public ApiElement(String name, List<String> mandatoryParamNames, List<String> optionalParamNames) {
		super();
		this.name = name;
		if (this.mandatoryParamNames != null) {
			this.mandatoryParamNames = mandatoryParamNames;
		}
		if (this.optionalParamNames != null) {
			this.optionalParamNames = optionalParamNames;
		}
	}
	
	public ApiElement(String name, String[] mandatoryParamNames) {
		this(name, mandatoryParamNames, null);
	}
	
	public ApiElement(String name, String[] mandatoryParamNames, String[] optionalParamNames) {
		super();
		this.name = name;
		this.setMandatoryParamNames(mandatoryParamNames);
		this.setOptionalParamNames(optionalParamNames);
	}
	
	public void setMandatoryParamNames(String[] paramNames) {
		if (paramNames != null) {
			this.mandatoryParamNames = new ArrayList<>(paramNames.length);
			for (String param : paramNames) {
				this.mandatoryParamNames.add(param);
			}
		}
	}
	
	public void setMandatoryParamNames(List<String> paramNames) {
		this.mandatoryParamNames = paramNames;
	}

	public List<String> getMandatoryParamNames() {
		return mandatoryParamNames;
	}

	public String getDescriptionTag() {
		return descriptionTag;
	}

	public void setDescriptionTag(String descriptionTag) {
		this.descriptionTag = descriptionTag;
	}

	public List<String> getOptionalParamNames() {
		return optionalParamNames;
	}
	
	public void setOptionalParamNames(String[] optionalParamNames) {
		if (optionalParamNames != null) {
			this.optionalParamNames = new ArrayList<>(optionalParamNames.length);
			for (String param : optionalParamNames) {
				this.optionalParamNames.add(param);
			}
		}
	}

	public void setOptionalParamNames(List<String> optionalParamNames) {
		this.optionalParamNames = optionalParamNames;
	}

}
