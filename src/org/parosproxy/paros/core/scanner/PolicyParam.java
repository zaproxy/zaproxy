/*
 *
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
package org.parosproxy.paros.core.scanner;

import java.util.List;

import org.parosproxy.paros.common.FileXML;
import org.w3c.dom.Element;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PolicyParam extends FileXML {

	private static final String POLICY = "policy";

	private static final String ENABLED = "enabled";
	
	private static final String[] PATH_ENABLED = {POLICY, ENABLED};
	
	
	/**
     * @param rootElementName
     */
    public PolicyParam(String rootElementName) {
        super(rootElementName);
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse() throws Exception {
        parseEnabledTest();
    }
    
    public void setTestList(Plugin testList) {
        
    }
    
    
    public void setEnabledTest() {
        
        List listTest = PluginFactory.getAllPlugin();
        Element root = getElement(POLICY);
        removeElement(root, ENABLED);
        for (int i=0; i<listTest.size(); i++) {
            Plugin test = (Plugin) listTest.get(i);            
            if (test.isEnabled()) {
                addElement(root, ENABLED, Integer.toString(test.getId()));
            }
        }
    }
    
    private void parseEnabledTest() {
        Element[] elements = getElements(POLICY);
        Element element = null;
        if (elements == null || elements.length == 0) {
            return;
        }
        
        PluginFactory.setAllPluginEnabled(false);
        for (int i=0; i<elements.length; i++) {
            element = elements[i];
            if (getValue(element, ENABLED).equals("")) {
                continue;
            }
            int id = Integer.parseInt(getValue(element, ENABLED));
            PluginFactory.getPlugin(i).setEnabled(true);            
        }
    }
    
}
