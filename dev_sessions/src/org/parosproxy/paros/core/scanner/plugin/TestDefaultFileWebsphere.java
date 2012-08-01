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
// ZAP: 2012/08/01 Removed the "(non-Javadoc)" comments.
package org.parosproxy.paros.core.scanner.plugin;

import org.parosproxy.paros.core.scanner.AbstractDefaultFilePlugin;
import org.parosproxy.paros.core.scanner.Category;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
@Deprecated
public class TestDefaultFileWebsphere extends AbstractDefaultFilePlugin {

	// ZAP Depreciated by Brute Force scanner
	@Override
	public boolean isDepreciated() {
		return true;
	}

    @Override
    public int getId() {
        return 20006;
    }

    @Override
    public String getName() {
        
        return "IBM WebSphere default files";
    }
    

    @Override
    public String[] getDependency() {
        return null;
    }

    @Override
    public String getDescription() {
        return "IBM WebSphere 4.0/5.0 example files are found.";
    }
    
    @Override
    public int getCategory() {
        return Category.SERVER;
    }
    
    @Override
    public String getSolution() {
        return "Remove example files.";
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

		addTest("servlet","HelloWorldServlet");
		addTest("servlet","SimpleServlet");
		addTest("servlet","aphtpassword");

		addTest("webapp/examples","HitCount");

		// for admin.war
		addTest("admin","logon.jsp,secure/logon.jsp");		

		// default_app.war
		addTest("servlet","snoop,snoop2,SnoopServlet,hello,ErrorReporter");

		// examples.ear
		addTest("webapp/examples","login.html,simple.jsp,ErrorServlet,ping,showcfg,HitCount,verify,HelloPervasive");
		addTest("WebSphereSamples",",YourCo/main.html");
		addTest("WebSphereSamples/SingleSamples","Increment/increment.html,AccountAndTransfer/create.html");
		addTest("/","WebSphereSamples/");

		// PetStore samples
		addTest("estore","index.html,annotated-index.html,populate");
		addTest("/","theme");

		// default indexable directory
		addTest("/","IBMWebAS");
		

    }

 
    
    
}
