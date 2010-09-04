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
public class TestDefaultFileDomino extends AbstractDefaultFilePlugin {


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 20006;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        
        return "Lotus Domino default files";
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
        return "Lotus Domino default files found.";
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
		
        addTest("/", "homepage.nsf,admin4.nsf,agentrunner.nsf,busytime.nsf,catalog.nsf,certsrv.nsf");
        addTest("/", "dspug.nsf,events4.nsf,log.nsf,mail.box,mtatbls.nsf,names.nsf,reports.nsf");
        addTest("/", "statmail.nsf,webadmin.nsf,?Open,?OpenServer,admin5.nsf,admin.nsf,alog.nsf");
        addTest("/","bookmarks.nsf,certa.nsf,certlog.nsf,chatlog.nsf,collect4.nsf,dba4.nsf,dclf.nsf");
        addTest("/","decsadm.nsf,deslog.nsf,domadmin.nsf,domadmin.nsf,domcfg.nsf,domguide.nsf");
        addTest("/", "events5.nsf,events.nsf,event.nsf,log4a.nsf,mab.nsf,nntppost.nsf,ntsync45.nsf");
        addTest("/","perweb.nsf,quickplacequickplacemain.nsf,schema50.nsf,setupweb.nsf");
        addTest("/","setup.nsf,smbcfg.nsf,srvnam.htm,statrep.nsf,stauths.nsf,stautht.nsf,stconfig.nsf");
        addTest("/","stconf.nsf,stdnaset.nsf,stdomino.nsf,stlog.nsf,stsrc.nsf,vpuserinfo.nsf,web.nsf");
        addTest("/","852566C90012664F,a_domlog.nsf,bookmark.nsf,clbusy.nsf,cldbdir.nsf,clusta4.nsf");
        addTest("/","da.nsf,DEASAppDesign.nsf,DEASLog01.nsf,DEASLog02.nsf,DEASLog03.nsf,DEASLog04.nsf,DEASLog05.nsf,DEASLog.nsf,decslog.nsf");
        addTest("/","DEESAdmin.nsf,dirassist.nsf,doladmin.nsf,domlog.nsf,");
        addTest("/iNotes/Forms5.nsf","$DefaultNav");
        addTest("/","jotter.nsf,leiadm.nsf,leilog.nsf,leivlt.nsf,l_domlog.nsf");
        addTest("/","mail10.box,mail1.box,mail2.box,mail3.box,mail4.box,mail5.box,mail6.box,mail7.box,mail8.box,mail9.box");
        addTest("/","msdwda.nsf,mtstore.nsf");
        addTest("/nntp","nd000001.nsf,nd000002.nsf,nd000003.nsf");
        addTest("/","qpadmin.nsf");
        addTest("/quickplace/quickplace","main.nsf");
        addTest("/sample","siregw46.nsf");
        addTest("/","smconf.nsf,smency.nsf,smhelp.nsf,smmsg.nsf,smquar.nsf,smsolar.nsf,smtime.nsf");
        addTest("/","smtpibwq.nsf,smtpobwq.nsf,smtp.box,smtp.nsf,smvlog.nsf,streg.nsf,userreg.nsf");

    }

 
    
    
}
