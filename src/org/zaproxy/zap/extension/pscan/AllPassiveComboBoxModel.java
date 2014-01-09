/*
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
public class AllPassiveComboBoxModel extends DefaultComboBoxModel {

    // Passive Scan Property manager
    private ExtensionPassiveScan pscan;
    
    // Internationalization map
    private Map<String, String> i18nToStr;

    /**
     * 
     */
    public AllPassiveComboBoxModel() {
        pscan = (ExtensionPassiveScan)Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);
    }

    /**
     * 
     * @param selected 
     */
    @Override
    public void setSelectedItem(Object selected) {
        // Check if the value is empty
        String value = (String)selected;

        if (!value.isEmpty()) {
            // Set the value for all passive plugins
            AlertThreshold at = AlertThreshold.valueOf(i18nToStr(value));
            pscan.setAllScannerThreshold(at);
        }
    }

    /**
     * 
     * @return 
     */
    @Override
    public Object getSelectedItem() {
        AlertThreshold at = pscan.getAllScannerThreshold();
        return (at == null) ? "" : strToI18n(at.name());
    }

    /**
     * 
     * @param str
     * @return 
     */
    private String strToI18n(String str) {
        // I18n's threshold and strength enums
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
            i18nToStr = new HashMap();
            for (Plugin.AlertThreshold at : Plugin.AlertThreshold.values()) {
                i18nToStr.put(this.strToI18n(at.name()), at.name());
            }
        }
        
        return i18nToStr.get(str);
    }    
}
