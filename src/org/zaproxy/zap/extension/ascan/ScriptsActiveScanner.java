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

package org.zaproxy.zap.extension.ascan;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class ScriptsActiveScanner extends AbstractAppParamPlugin {

	private ExtensionScript extension = null;

    private static Logger logger = Logger.getLogger(ScriptsActiveScanner.class);
    /**
     * A {@code Set} containing the scripts that do not implement {@code ActiveScript2}, to show an error if those scripts do
     * not implement {@code ActiveScript} (thus not implementing any of the required interfaces).
     * 
     * @see #scan()
     * @see #scan(HttpMessage, String, String)
     */
    private Set<ScriptWrapper> scriptsNoInterface = new HashSet<>();
	
    @Override
    public int getId() {
        return 50000;
    }

    @Override
    public String getName() {
    	return Constant.messages.getString("ascan.scripts.activescanner.title");
    }

    @Override
    public String[] getDependency() {
        return null;
    }

    @Override
    public String getDescription() {
        return "N/A";
    }

    @Override
    public int getCategory() {
        return Category.MISC;
    }

    @Override
    public String getSolution() {
        return "N/A";
    }

    @Override
    public String getReference() {
        return "N/A";
    }

    @Override
    public void init() {
        if (shouldSkipScan()) {
            getParent().pluginSkipped(this, Constant.messages.getString("ascan.scripts.skip.reason"));
        }
    }

    /**
     * Tells whether or not the scanner should be skipped. The scanner should be skipped when the {@code ExtensionScript} is not
     * enabled, when there are no scripts, or if there are none is enabled.
     *
     * @return {@code true} if the scanner should be skipped, {@code false} otherwise
     */
    private boolean shouldSkipScan() {
        if (this.getExtension() == null) {
            return true;
        }

        List<ScriptWrapper> scripts = getActiveScripts();
        if (scripts.isEmpty()) {
            return true;
        }

        for (ScriptWrapper script : scripts) {
            if (script.isEnabled()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the scripts of active script type.
     * <p>
     * <strong>Note:</strong> this method should be called only when {@code getExtension()} returns non-{@code null}.
     *
     * @return a {@code List} containing the scripts with active script type, never {@code null}
     * @see #getExtension()
     * @see ExtensionActiveScan#SCRIPT_TYPE_ACTIVE
     */
    private List<ScriptWrapper> getActiveScripts() {
        return this.getExtension().getScripts(ExtensionActiveScan.SCRIPT_TYPE_ACTIVE);
    }

	private ExtensionScript getExtension() {
		if (extension == null) {
			extension = (ExtensionScript) Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.NAME);
		}
		return extension;
	}

    @Override
    public void scan() {
		List<ScriptWrapper> scripts = this.getActiveScripts();
			
		for (Iterator<ScriptWrapper> it = scripts.iterator(); it.hasNext() && !isStop();) {
			ScriptWrapper script = it.next();
			try {
				if (script.isEnabled()) {
					ActiveScript2 s = extension.getInterface(script, ActiveScript2.class);
					
					if (s != null) {
						HttpMessage msg = this.getNewMsg();
						logger.debug("Calling script " + script.getName() + " scanNode for " + msg.getRequestHeader().getURI());
						s.scanNode(this, msg);
					} else {
						scriptsNoInterface.add(script);
					}
				}
				
			} catch (Exception e) {
				extension.handleScriptException(script, e);
			}
		}

		if (!isStop()) {
			super.scan();
		}
		scriptsNoInterface.clear();
    }

    @Override
    public void scan(HttpMessage msg, String param, String value) {
		List<ScriptWrapper> scripts = this.getActiveScripts();
			
		for (Iterator<ScriptWrapper> it = scripts.iterator(); it.hasNext() && !isStop();) {
			ScriptWrapper script = it.next();
			try {
				if (script.isEnabled()) {
					ActiveScript s = extension.getInterface(script, ActiveScript.class);
					
					if (s != null) {
						logger.debug("Calling script " + script.getName() + " scan for " + msg.getRequestHeader().getURI() +
								"param=" + param + " value=" + value);
						s.scan(this, msg, param, value);
						
					} else if (scriptsNoInterface.contains(script)) {
						extension.handleFailedScriptInterface(
								script,
								Constant.messages.getString("ascan.scripts.interface.active.error", script.getName()));
					}
				}
				
			} catch (Exception e) {
				extension.handleScriptException(script, e);
			}
		}
	}

    @Override
    public boolean isStop() {
        return super.isStop();
    }
    
    public String setParam(HttpMessage msg, String param, String value) {
    	return super.setParameter(msg, param, value);
    }

    @Override
    public void sendAndReceive(HttpMessage msg) throws HttpException, IOException {
        super.sendAndReceive(msg);
    }
    
    @Override
    public void sendAndReceive(HttpMessage msg, boolean isFollowRedirect) throws HttpException, IOException {
    	super.sendAndReceive(msg, isFollowRedirect);
    }

    @Override
    public void sendAndReceive(HttpMessage msg, boolean isFollowRedirect, boolean handleAntiCSRF) throws HttpException, IOException {
    	super.sendAndReceive(msg, isFollowRedirect, handleAntiCSRF);
    }

	public void raiseAlert(int risk, int confidence, String name, String description, String uri, 
			String param, String attack, String otherInfo, String solution, String evidence, 
			int cweId, int wascId, HttpMessage msg) {
		super.bingo(risk, confidence, name, description, uri, param, attack, 
				otherInfo, solution, evidence, cweId, wascId, msg);
	}

	@Override
	public int getRisk() {
		return Alert.RISK_INFO;
	}

	@Override
	public int getCweId() {
		return 0;
	}

	@Override
	public int getWascId() {
		return 0;
	}

}
