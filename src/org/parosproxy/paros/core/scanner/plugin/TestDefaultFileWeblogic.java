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
public class TestDefaultFileWeblogic extends AbstractDefaultFilePlugin {


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 20004;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        
        return "BEA WebLogic example files";
    }
    

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSummary()
     */
    public String getDescription() {
        return "BEA WebLogic server 8.1 example files are found";
    }
    
    public int getCategory() {
        return Category.SERVER;
    }
    
    public String getSolution() {
        return "Remove example files.";
    }
    
    public String getReference() {
        return "";
    }

    public void init() {
        super.init();
        createURI();
    }
    
    private void createURI() {
		// WebLogic server 8.1 examples

		addTest("/patient","register.do,login.do");
		addTest("/admin", "login.do");
		addTest("/physican", "login.do");

		addTest("/examplesWebApp","index.jsp");
		// order parser example
		addTest("/examplesWebApp","OrderParser.jsp?xmlfile=C:/bea/weblogic81/samples/server/examples/src/examples/xml/orderParser/order.xml");
		// web services example
		addTest("/examplesWebApp","WebservicesEJB.jsp");
		// enterprise java bean example		
		addTest("/examplesWebApp","EJBeanManagedClient.jsp");
		// JSP example
		addTest("/examplesWebApp","InteractiveQuery.jsp");
		// servlet example
		addTest("/examplesWebApp","SessionServlet");

		// ***********************
		
		// WebLogic 8.1 Integration end-to-end examples
		
		addTest("/e2ePortalProject","Login.portal");
		
		// WebLogic Administration portal - not an example
		addTest("/portalAppAdmin","login.jsp");
		
		// weblogic Integration Administration console
		addTest("/","wliconsole");
    }

 
    
    
}
