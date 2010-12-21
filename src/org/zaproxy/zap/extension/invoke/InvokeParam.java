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
package org.zaproxy.zap.extension.invoke;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.common.AbstractParam;

public class InvokeParam extends AbstractParam {

    private static final String INVOKE = "invoke";
    private static final String INVOKE_NAME = "name";
    private static final String INVOKE_COMMAND = "command";
    private static final String INVOKE_PARAMS = "parameters";
    private static final String INVOKE_OUTPUT = "output";

	private List<InvokableApp> listInvoke = new ArrayList<InvokableApp>();
	
	public InvokeParam() {
	}
	
	protected void parse() {
        listInvoke.clear();

        String host = "";
        for (int i=0; host != null; i++) {

            host = getConfig().getString(getInvoke(i, INVOKE_NAME));
            if (host == null) {
                   break;
            }
            
            if (host.equals("")) {
                break;
            }
            
            InvokableApp auth = new InvokableApp(
                    getConfig().getString(getInvoke(i, INVOKE_NAME)),
                    getConfig().getString(getInvoke(i, INVOKE_COMMAND)),
                    getConfig().getString(getInvoke(i, INVOKE_PARAMS)),
                    getConfig().getBoolean(getInvoke(i, INVOKE_OUTPUT), true)
                    );
            listInvoke.add(auth);
        }
	}


    public List<InvokableApp> getListInvoke() {
        return listInvoke;
    }

    public void setListInvoke(List<InvokableApp> listAuth) {
        this.listInvoke = listAuth;
        InvokableApp app = null;
        
        for (int i=0; i<((listAuth.size() > 100)? listAuth.size(): 100); i++) {
            // clearProperty doesn't work.  So set all host name to blank as a workaround.
            getConfig().clearProperty(getInvoke(i, INVOKE_NAME));            
            getConfig().clearProperty(getInvoke(i, INVOKE_COMMAND));            
            getConfig().clearProperty(getInvoke(i, INVOKE_PARAMS));
            getConfig().clearProperty(getInvoke(i, INVOKE_OUTPUT));
            getConfig().clearProperty(INVOKE + ".A"+i);
        }
        for (int i=0; i<listAuth.size(); i++) {
            app = (InvokableApp) listAuth.get(i);            
            getConfig().setProperty(getInvoke(i, INVOKE_NAME), app.getDisplayName());
            getConfig().setProperty(getInvoke(i, INVOKE_COMMAND), app.getFullCommand());
            getConfig().setProperty(getInvoke(i, INVOKE_PARAMS), app.getParameters());
            getConfig().setProperty(getInvoke(i, INVOKE_OUTPUT), app.isCaptureOutput());
        }
        
    }

    private String getInvoke(int i, String name) {
        return INVOKE + ".A" + i + "." + name;
    }
}
