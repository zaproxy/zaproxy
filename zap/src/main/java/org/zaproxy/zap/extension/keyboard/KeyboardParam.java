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
package org.zaproxy.zap.extension.keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.KeyStroke;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class KeyboardParam extends AbstractParam {

    private static final Logger logger = Logger.getLogger(KeyboardParam.class);

    private static final String KEYBOARD_BASE_KEY = "keyboard";
    
    private static final String ALL_SHORTCUTS_KEY = KEYBOARD_BASE_KEY + ".shortcuts";
    
    private static final String MENU_ITEM_KEY = "menu";
    private static final String KEYCODE_KEY = "keycode";
    private static final String MODIFIERS_KEY = "modifiers";
    
    private Map<String, KeyStroke> map = null;
    
    public KeyboardParam() {
    }

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_SHORTCUTS_KEY);
            map = new HashMap<String, KeyStroke>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(MENU_ITEM_KEY, "");
                if (name.length() > 0) {
                    map.put(name, KeyStroke.getKeyStroke(sub.getInt(KEYCODE_KEY, 0), sub.getInt(MODIFIERS_KEY, 0), false));
                }
            }
        } catch (ConversionException e) {
            logger.error("Error while loading keyboard shortcuts " + e.getMessage(), e);
        }
    }

    public KeyStroke getShortcut(String i18nKey) {
		return map.get(i18nKey);
	}
	
	public void setShortcut(String i18nKey, KeyStroke keyStroke) {
		map.put(i18nKey, keyStroke);
	}
	
	protected void setConfigs() {
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_SHORTCUTS_KEY);

        int i= 0;
        for (Entry<String, KeyStroke> entry : map.entrySet()) {
            String elementBaseKey = ALL_SHORTCUTS_KEY + "(" + i + ").";
            getConfig().setProperty(elementBaseKey + MENU_ITEM_KEY, entry.getKey());
            if (entry.getValue() != null) {
	            getConfig().setProperty(elementBaseKey + KEYCODE_KEY, entry.getValue().getKeyCode());
	            getConfig().setProperty(elementBaseKey + MODIFIERS_KEY, entry.getValue().getModifiers());
            } else {
	            getConfig().setProperty(elementBaseKey + KEYCODE_KEY, 0);
	            getConfig().setProperty(elementBaseKey + MODIFIERS_KEY, 0);
            }
            i++;
        }
	}

}
