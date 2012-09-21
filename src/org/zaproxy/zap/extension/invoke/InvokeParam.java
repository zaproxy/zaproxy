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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.common.AbstractParam;

public class InvokeParam extends AbstractParam {

    private static final String INVOKE = "invoke";
    private static final String INVOKE_NAME = "name";
    private static final String INVOKE_COMMAND = "command";
    private static final String INVOKE_PARAMS = "parameters";
    private static final String INVOKE_OUTPUT = "output";
    private static final String INVOKE_NOTE = "note";
    private static final String INVOKE_DIRECTORY = "directory";

	private List<InvokableApp> listInvoke = new ArrayList<>();
	
	public InvokeParam() {
	}
	
	@Override
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
            
            File dir = null;
            String directory = getConfig().getString(getInvoke(i, INVOKE_DIRECTORY));
            if (directory != null && directory.length() > 0) {
            	dir = new File (directory);
            }
            
            InvokableApp auth = new InvokableApp(
                    getConfig().getString(getInvoke(i, INVOKE_NAME)),
                    dir,
                    getConfig().getString(getInvoke(i, INVOKE_COMMAND)),
                    getConfig().getString(getInvoke(i, INVOKE_PARAMS)),
                    getConfig().getBoolean(getInvoke(i, INVOKE_OUTPUT), true),
                    getConfig().getBoolean(getInvoke(i, INVOKE_NOTE), false)
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
            getConfig().clearProperty(getInvoke(i, INVOKE_DIRECTORY));
            getConfig().clearProperty(getInvoke(i, INVOKE_COMMAND));            
            getConfig().clearProperty(getInvoke(i, INVOKE_PARAMS));
            getConfig().clearProperty(getInvoke(i, INVOKE_OUTPUT));
            getConfig().clearProperty(getInvoke(i, INVOKE_NOTE));
            getConfig().clearProperty(INVOKE + ".A"+i);
        }
        for (int i=0; i<listAuth.size(); i++) {
            app = listAuth.get(i);            
            getConfig().setProperty(getInvoke(i, INVOKE_NAME), app.getDisplayName());
            if (app.getWorkingDirectory() != null) {
                getConfig().setProperty(getInvoke(i, INVOKE_DIRECTORY), app.getWorkingDirectory().getAbsolutePath());
            }
            getConfig().setProperty(getInvoke(i, INVOKE_COMMAND), app.getFullCommand());
            getConfig().setProperty(getInvoke(i, INVOKE_PARAMS), app.getParameters());
            getConfig().setProperty(getInvoke(i, INVOKE_OUTPUT), app.isCaptureOutput());
            getConfig().setProperty(getInvoke(i, INVOKE_NOTE), app.isOutputNote());
        }
        
    }

    private String getInvoke(int i, String name) {
        return INVOKE + ".A" + i + "." + name;
    }
}
