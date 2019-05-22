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

    String getName();

    String getCaptionName();
    
    String getTargetViewName();

    int getPosition();

    JComponent getPane();

    void setSelected(boolean selected);
    
    void save();
    
    HttpPanelViewModel getModel();
    
    boolean isEnabled(Message aMessage);

    boolean hasChanged();
    
    boolean isEditable();

    void setEditable(boolean editable);

    void setParentConfigurationKey(String configurationKey);

    void loadConfiguration(FileConfiguration configuration);

    void saveConfiguration(FileConfiguration configuration);
}
