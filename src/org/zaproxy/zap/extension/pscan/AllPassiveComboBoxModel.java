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
package org.zaproxy.zap.extension.pscan;

import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;

/**
 *
 */
public class AllPassiveComboBoxModel<T> extends DefaultComboBoxModel<T> {
    
    // Internationalization map
    private Map<String, String> i18nToStr;
    private ExtensionPassiveScan pscanExt;

    /**
     * 
     */
    public AllPassiveComboBoxModel() {
    }

    /**
     * 
     * @param selected 
     */
    @Override
    public void setSelectedItem(Object selected) {
        // Check if the value is empty
        String value = (String)selected;

        if ((value != null) && !value.isEmpty()) {
            // Set the value for all passive plugins
            ExtensionPassiveScan pscan = getExtension();
            AlertThreshold at = AlertThreshold.valueOf(i18nToStr(value));

            // Passive plugins could be disabled so the loader returns null
            // in this case we've to do nothing
            if (pscan != null) {
                pscan.setAllScannerThreshold(at);
            }
        }
    }

    /**
     * 
     * @return 
     */
    @Override
    public Object getSelectedItem() {
        ExtensionPassiveScan pscan = getExtension();
        if (pscan != null) {
            AlertThreshold at = pscan.getAllScannerThreshold();
            if (at != null) {
                return strToI18n(at.name());
            }
        }
        
        return "";
    }

    /**
     * Check if the passive ext is disabled
     * @return 
     */
    public boolean isEnabled() {
        return (getExtension() != null);        
    }

    /**
     * Get back the Extension object
     * @return 
     */
    private ExtensionPassiveScan getExtension() {
        if (pscanExt == null) {
            pscanExt = (ExtensionPassiveScan)Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);                    
        }
        
        return pscanExt;
    }
    
    /**
     * 
     * @param str
     * @return 
     */
    private String strToI18n(String str) {
        // I18n's threshold enums
        return Constant.messages.getString("ascan.policy.level." + str.toLowerCase());
    }

    /**
     * 
     * @param str
     * @return 
     */
    private String i18nToStr(String str) {
        // Converts to i18n'ed names back to the enum names
        if (i18nToStr == null) {
            i18nToStr = new HashMap<>();
            for (Plugin.AlertThreshold at : Plugin.AlertThreshold.values()) {
                i18nToStr.put(this.strToI18n(at.name()), at.name());
            }
        }
        
        return i18nToStr.get(str);
    }    
}
