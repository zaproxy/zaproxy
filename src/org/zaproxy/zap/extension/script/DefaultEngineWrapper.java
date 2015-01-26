/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP development team
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
package org.zaproxy.zap.extension.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class DefaultEngineWrapper extends ScriptEngineWrapper {

    private Map<String, String> templateMap = new HashMap<>();

    private static Logger logger = Logger.getLogger(DefaultEngineWrapper.class);

	public DefaultEngineWrapper(ScriptEngine engine) {
		super(engine);
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getSyntaxStyle() {
		return null;
	}

	@Override
	public String getTemplate(String type) {
		if (! templateMap.containsKey(type)) {
			templateMap.put(type, this.getStringReource(
					this.getLanguageName().toLowerCase() + File.separator + type.toLowerCase() + 
					"-template." + this.getExtensions().get(0)));
		}
		return templateMap.get(type);
	}
	
	private String getStringReource(String resourceName) {
		
        File file = new File(ExtensionScript.TEMPLATES_DIR, resourceName);
        if ( ! file.exists()) {
			logger.debug("No template at: " + file.getAbsolutePath());
	        file = new File(Constant.getZapHome() + File.separator + ExtensionScript.TEMPLATES_DIR, resourceName);
	        if ( ! file.exists()) {
				logger.debug("No template at: " + file.getAbsolutePath());
				return "";
	        }
        }

		StringBuilder sb = new StringBuilder();
		try {
		    BufferedReader fr = new BufferedReader(new FileReader(file));
	        String line;
	        try {
				while ((line = fr.readLine()) != null) {
				    sb.append(line);
				    sb.append("\n");
				}
			} finally {
		        fr.close();
			}
            return sb.toString();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@Override
	public List<String> getExtensions() {
		return this.getEngine().getFactory().getExtensions();
	}

	@Override
	public boolean isTextBased() {
		return true;
	}

	@Override
	public boolean isRawEngine() {
		return true;
	}

	@Override
	public boolean isSupportsMissingTemplates() {
		return true;
	}

    @Override
    public boolean isDefaultTemplate(ScriptWrapper script) {
        return false;
    }

}
