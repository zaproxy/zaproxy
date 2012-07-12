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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/02 Added @Deprecated annotation to the class.
package org.parosproxy.paros.core.scanner.plugin;

import org.parosproxy.paros.core.scanner.AbstractDefaultFilePlugin;
import org.parosproxy.paros.core.scanner.Category;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
@Deprecated
public class TestDefaultFileJrun extends AbstractDefaultFilePlugin {

	// ZAP Depreciated by Brute Force scanner
	@Override
	public boolean isDepreciated() {
		return true;
	}

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getId()
     */
    @Override
    public int getId() {
        return 20003;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getName()
     */
    @Override
    public String getName() {
        
        return "Macromedia JRun default files";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getSummary()
     */
    @Override
    public String getDescription() {
        return "JRun 4.0 default files are found or application information disclosure.";
    }
    
    @Override
    public int getCategory() {
        return Category.SERVER;
    }
    
    @Override
    public String getSolution() {
        return "Remove default files.";
    }
    
    @Override
    public String getReference() {
        return "";
    }

    @Override
    public void init() {
        super.init();
        createURI();
    }
    
    private void createURI() {
		addTest("/WEB-INF","web.xml");		// info leak by Foundstone
		addTest("/WEB-INF","webapp.properties");	//dito

		// JRUN 4.0
		addTest("/jstl-war","index.html");
		addTest("/compass","logon.jsp");
		addTest("/travelnet","home.jsp");
		addTest("/worldmusic/action","cdlist");
		addTest("/worldmusic/action","catalog");
		addTest("/techniques/servlets","index.html");
		addTest("/ws-client","loanCalculation.jsp");
		addTest("/flash/java/javabean","FlashJavaBean.html");
		addTest("/SmarTicketApp","index.html");
		addTest("/","databasenotes.html");
    }

 
    
    
}
