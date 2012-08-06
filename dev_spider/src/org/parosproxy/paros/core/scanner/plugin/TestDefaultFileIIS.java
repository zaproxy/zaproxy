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
public class TestDefaultFileIIS extends AbstractDefaultFilePlugin {

	// ZAP Depreciated by Brute Force scanner
	@Override
	public boolean isDepreciated() {
		return true;
	}

    @Override
    public int getId() {
        return 20002;
    }

    @Override
    public String getName() {
        
        return "IIS default file";
    }
    
    @Override
    public String[] getDependency() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Microsoft IIS 4.0, 5.0 or 6.0 default files are found.";
    }
    
    @Override
    public int getCategory() {
        return Category.SERVER;
    }
    
    @Override
    public String getSolution() {
        return "Remove default files and virtual directories.";
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

        addTest("/","iisstart.asp,postinfo.html,_vti_inf.html");
        addTest("msadc","msadcs.dll");
        
        addTest("_vti_bin", "fpcount.exe,shtml.dll");
        addTest("_vti_bin/_vti_adm", "admin.dll");
        addTest("_vti_bin/_vti_aut", "author.dll");
        addTest("_cti_pvt", "access.cnf,botinfs.cnf,bots.cnf,deptodoc.btr,dectodep.btr,linkinfo.cnf,service.cnf,services.cnf,svcacl.cnf,uniqperm.cnf,writeto.cnf");
        addTest("_vti_bin,_private,_vti_cnf,_vti_log,_vti_pvt,_vti_script,vti_txt","/");
        addTest("IISSamples/sdk/asp/docs","CodeBrws.asp,ColorPicker.asp,libcodebrws.inc,sampfram.asp,toolbar.asp");
        addTest("IISSamples/sdk/asp/applications","Application_JScript.asp,Application_VBScript.asp,Session_JScript.asp,Session_VBScript.asp");
        addTest("IISSamples/sdk/asp/database","AddDelete_VBScript.asp,LimitRows_VBScript.asp,MultiScrolling_VBScript.asp,SimpleQuery_VBScript.asp,StoredProcedures_VBScript.asp,Update_VBScript.asp");
        
        addTest("msadc/samples/selector","showcode.asp");
        addTest("msadc","samples/adctest.asp");
        
        addTest("_private,_vti_bin,_vti_pvt,_vti_log,_vti_txt,_vti_cnf","/");
        
        
        // Win NT IIS 4.0
        addTest("/iissamples","default/samples.asp");
        addTest("/iissamples/ExAir", "default.asp,IEPush/Channel.asp,Catalog/Catalog.asp,FunNGames/FunNGames.asp,HowToReachUs.asp,About.asp");
        addTest("/iisadmin","htmldocs/iisdocs.htm");
        addTest("/scripts/tools","dsnform.exe,getdrvrs.exe,mkilog.exe,newdsn.exe");
        addTest("/iisadmpwd","achg.htr,aexp.htr,aexp2.htr,aexp2b.htr,aexp3.htr,aexp4.htr,aexp4b.htr,anot.htr,anot3.htr");
        addTest("/msadc","adcjavas.inc,adcvbs.inc,msadcs.dll,readme.txt");
        addTest("/samples/dbsamp","dbsamp.htm,dbsamp1.htm,dbsamp2.htm,dbsamp3.htm");
        addTest("/samples/gbook","query.htm,register.htm");
        addTest("/samples/htmlsamp","htmlsamp.htm,styles.htm,styles2.htm,tables.htm");
        addTest("/samples/isapi","drop.htm,favlist.htm,isapi.htm,srch.htm");
        addTest("/samples/sampsite","about.htm,catalog.htm,default.htm,process.htm,results.htm,sampsite.htm,sendme.htm,taste.htm");
        
        // Win 2003 server IIS 6.0
        addTest("/","iisstart.htm");

    }

 
    
    
}
