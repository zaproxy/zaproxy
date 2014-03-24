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

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class InvokeParam extends AbstractParam {
    
    private static final Logger logger = Logger.getLogger(InvokeParam.class);

    private static final String INVOKE_BASE_KEY = "invoke";
    private static final String ALL_APPS_KEY = INVOKE_BASE_KEY + ".apps.app";
    private static final String APP_NAME_KEY = "name";
    private static final String APP_COMMAND_KEY = "command";
    private static final String APP_DIRECTORY_KEY = "directory";
    private static final String APP_PARAMS_KEY = "parameters";
    private static final String APP_OUTPUT_KEY = "output";
    private static final String APP_NOTE_KEY = "note";
    private static final String APP_ENABLED_KEY = "enabled";
    
    private static final String CONFIRM_REMOVE_APP_KEY = INVOKE_BASE_KEY + ".confirmRemoveApp";

	private List<InvokableApp> listInvoke = new ArrayList<>(0);
	private List<InvokableApp> listInvokeEnabled = new ArrayList<>(0);
	
	private boolean confirmRemoveApp = true;
	
	public InvokeParam() {
	}
	
	@Override
	protected void parse() {
        listInvoke.clear();
        
        ArrayList<InvokableApp> enabledApps = null;
        try {
            List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_APPS_KEY);
            this.listInvoke = new ArrayList<>(fields.size());
            enabledApps = new ArrayList<>(fields.size());
            List<String> tempListNames = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(APP_NAME_KEY, "");
                if (!"".equals(name) && !tempListNames.contains(name)) {
                    tempListNames.add(name);
                    
                    File dir = null;
                    String directory = sub.getString(APP_DIRECTORY_KEY, "");
                    if (directory.length() > 0) {
                        dir = new File (directory);
                    }
                    
                    InvokableApp app = new InvokableApp(
                            name,
                            dir,
                            sub.getString(APP_COMMAND_KEY),
                            sub.getString(APP_PARAMS_KEY),
                            sub.getBoolean(APP_OUTPUT_KEY, true),
                            sub.getBoolean(APP_NOTE_KEY, false));
                    
                    app.setEnabled(sub.getBoolean(APP_ENABLED_KEY, true));
                    
                    listInvoke.add(app);
                    
                    if (app.isEnabled()) {
                        enabledApps.add(app);
                    }
                }
            }
            enabledApps.trimToSize();
            this.listInvokeEnabled = enabledApps;
        } catch (ConversionException e) {
            logger.error("Error while loading invoke applications: " + e.getMessage(), e);
        }

        try {
            this.confirmRemoveApp = getConfig().getBoolean(CONFIRM_REMOVE_APP_KEY, true);
        } catch (ConversionException e) {
            logger.error("Error while loading the confirm remove option: " + e.getMessage(), e);
        }
	}

    public List<InvokableApp> getListInvoke() {
        return listInvoke;
    }

    public List<InvokableApp> getListInvokeEnabled() {
        return listInvokeEnabled;
    }

    public void setListInvoke(List<InvokableApp> listInvoke) {
        this.listInvoke = listInvoke;
        
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_APPS_KEY);

        ArrayList<InvokableApp> enabledApps = new ArrayList<>(listInvoke.size());
        for (int i = 0, size = listInvoke.size(); i < size; ++i) {
            String elementBaseKey = ALL_APPS_KEY + "(" + i + ").";
            InvokableApp app = listInvoke.get(i);
            
            getConfig().setProperty(elementBaseKey + APP_NAME_KEY, app.getDisplayName());
            File file = app.getWorkingDirectory();
            getConfig().setProperty(elementBaseKey + APP_DIRECTORY_KEY, file != null ? file.getAbsolutePath() : "");
            getConfig().setProperty(elementBaseKey + APP_COMMAND_KEY, app.getFullCommand());
            getConfig().setProperty(elementBaseKey + APP_PARAMS_KEY, app.getParameters());
            getConfig().setProperty(elementBaseKey + APP_OUTPUT_KEY, Boolean.valueOf(app.isCaptureOutput()));
            getConfig().setProperty(elementBaseKey + APP_NOTE_KEY, Boolean.valueOf(app.isOutputNote()));
            
            getConfig().setProperty(elementBaseKey + APP_ENABLED_KEY, Boolean.valueOf(app.isEnabled()));

            if (app.isEnabled()) {
                enabledApps.add(app);
            }
        }
        
        enabledApps.trimToSize();
        this.listInvokeEnabled = enabledApps;
    }
    
    public boolean isConfirmRemoveApp() {
        return this.confirmRemoveApp;
    }
    
    public void setConfirmRemoveApp(boolean confirmRemove) {
        this.confirmRemoveApp = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_APP_KEY, Boolean.valueOf(confirmRemoveApp));
    }
}
