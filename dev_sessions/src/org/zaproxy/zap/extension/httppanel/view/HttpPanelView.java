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

package org.zaproxy.zap.extension.httppanel.view;

import javax.swing.JComponent;

import org.apache.commons.configuration.FileConfiguration;
import org.zaproxy.zap.extension.httppanel.Message;


public interface HttpPanelView {

    
    public String getConfigName();

    public String getName();

    public int getPosition();

    public JComponent getPane();

    void setSelected(boolean selected);

    
    public void save();

    
    public HttpPanelViewModel getModel();
    
    
    public boolean isEnabled(Message aMessage);

    
    public boolean hasChanged();

    
    public boolean isEditable();

    
    public void setEditable(boolean editable);

    
    public void setParentConfigurationKey(String configurationKey);

    
    public void loadConfiguration(FileConfiguration configuration);

    
    public void saveConfiguration(FileConfiguration configuration);
}
