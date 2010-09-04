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
package org.parosproxy.paros.core.scanner.plugin;

import org.parosproxy.paros.core.scanner.AbstractDefaultFilePlugin;
import org.parosproxy.paros.core.scanner.Category;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestDefaultFileColdFusion extends AbstractDefaultFilePlugin {


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 20001;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        
        return "Cold Fusion default file";
    }


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    public String getDescription() {
        return "ColdFusion MX default files are found.";
    }
    
    public int getCategory() {
        return Category.SERVER;
    }
    
    public String getSolution() {
        return "Remove default files.";
    }

    public String getReference() {
        return "";
    }
    
    
    public void init() {
        super.init();
        createURI();
    }
    
    private void createURI() {
        
		// ColdFusion MX		
		addTest("CFIDE/administrator","aboutcf.cfm,Application.cfm, checkfile.cfm,enter.cfm,header.cfm,homefile.cfm,homepage.cfm,index.cfm,left.cfm,linkdirect.cfm,login.cfm,logout.cfm,navserver.cfm,right.cfm,tabs.cfm,welcome.cfm,welcomedoc.cfm,welcomeexapps.cfm,welcomefooter.cfm,welcomegetstart.cfm");

    }

 
    
    
}
