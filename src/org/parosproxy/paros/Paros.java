/*
 * Created on May 19, 2004
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
package org.parosproxy.paros;


import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.SSLConnector;
import org.parosproxy.paros.view.AboutWindow;
import org.parosproxy.paros.view.LicenseFrame;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.view.ProxyDialog;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Paros {
    

    static {
	    
	    // set SSLConnector as socketfactory in HttpClient.
	    ProtocolSocketFactory sslFactory = null;
	    try {
	        Protocol protocol = Protocol.getProtocol("https");
	        sslFactory = protocol.getSocketFactory();
	    } catch (Exception e) {
			// ZAP: Print the exception - log not yet initialised
	    	e.printStackTrace();
	    }
	    if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
	        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new SSLConnector(), 443));
	    }
    }
    
    private static Log log = null;
    
    
	public static void main(String[] args) throws Exception {
	    Paros paros = new Paros();
	    paros.init(args);
        Constant.getInstance();
        String msg = Constant.PROGRAM_NAME + " " + Constant.PROGRAM_VERSION + " started.";
        log = LogFactory.getLog(Paros.class);
	    log.info(msg);
	    
	    try {
	        paros.run();
	    } catch (Exception e) {
	        log.fatal(e.getStackTrace());
	        throw e;
	    }
		
	}


    
    private CommandLine cmdLine = null;
	

	/**
	 * Initialization without dependence on any data model nor view creation.
	 * @param args
	 */
	private void init(String[] args) {

	    //HttpSender.setUserAgent(Constant.USER_AGENT);
	    try {
	        cmdLine = new CommandLine(args);
	    } catch (Exception e) {
	        System.out.println(CommandLine.getHelpGeneral());
	        System.exit(1);
	    }

	    Locale.setDefault(Locale.ENGLISH);
	    
		try {
  			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
		}
	    // ZAP: Nimbus looks better than the default L&F (IMHO), so use if available
	    try {
	        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	            if ("Nimbus".equals(info.getName())) {
	                UIManager.setLookAndFeel(info.getClassName());
	                break;
	            }
	        }
	    } catch (UnsupportedLookAndFeelException e) {
	        // handle exception
	    } catch (ClassNotFoundException e) {
	        // handle exception
	    } catch (InstantiationException e) {
	        // handle exception
	    } catch (IllegalAccessException e) {
	        // handle exception
	    }
        
//	    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//	    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//	    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");

	    
	}
	
	private void run() throws Exception {
	    
		// ZAP: Disabled update checking for now!
        //checkUpdate();
	    
	    AboutWindow aboutWindow = null;
	    // ZAP: Display help after licence
	    boolean shown = false;
	    if (cmdLine.isGUI()) {
		    shown = showLicense();
	        aboutWindow = new AboutWindow();
	        aboutWindow.setVisible(true);
	    }
	    
	    Model.getSingleton().init();
	    Model.getSingleton().getOptionsParam().setGUI(cmdLine.isGUI());
	    
	    // ZAP: Prompt for proxy details if set
	    if (Model.getSingleton().getOptionsParam().getConnectionParam().isProxyChainPrompt()) {
			ProxyDialog dialog = new ProxyDialog(View.getSingleton().getMainFrame(), true);
			dialog.init(Model.getSingleton().getOptionsParam());
			dialog.setVisible(true);
	    }

		
		if (Model.getSingleton().getOptionsParam().isGUI()) {
		    runGUI();
		    aboutWindow.dispose();
		    if (shown) {
		    	ExtensionHelp.showHelp();
		    }
	    } else {
	        runCommandLine();
	    }
	    
	}
	
	private void runCommandLine() {
	    int rc = 0;
	    String help = "";
	    
	    Control.initSingletonWithoutView();
	    Control control = Control.getSingleton();
	    
	    // no view initialization

	    try {
	        control.getExtensionLoader().hookCommandLineListener(cmdLine);
	        if (cmdLine.isEnabled(CommandLine.HELP) || cmdLine.isEnabled(CommandLine.HELP2)) {
	            help = cmdLine.getHelp();
	            System.out.println(help);
	        } else {
	        
	            control.runCommandLineNewSession(cmdLine.getArgument(CommandLine.NEW_SESSION));
		    
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {}
	        }
		    rc = 0;
	    } catch (Exception e) {
	        log.error(e.getMessage());
	        System.out.println(e.getMessage());
	        rc = 1;
	    } finally {
            control.shutdown(false);
    	    log.info(Constant.PROGRAM_TITLE + " terminated.");
	    }
	    System.exit(rc);
	}
	
	
	
	private void runGUI() throws ClassNotFoundException, Exception {

	    Control.initSingletonWithView();
	    Control control = Control.getSingleton();
	    View view = View.getSingleton();
	    view.postInit();
	    view.getMainFrame().setExtendedState(Frame.MAXIMIZED_BOTH);		
	    view.getMainFrame().setVisible(true);
	    view.setStatus("");

	    control.getMenuFileControl().newSession(false);

	}
	
	// ZAP: Return true if licence shown and accepted
	private boolean showLicense() {
		boolean shown = false;
//	    if (!(new File("license/AcceptedLicense")).exists()){
        if (!(new File(Constant.getInstance().ACCEPTED_LICENSE)).exists()){
	        
	        LicenseFrame license = new LicenseFrame();
	        license.setVisible(true);
	        while (!license.isAccepted()) {
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {}
	        }
	        shown = true;
	    }
	    
	    try{
//	        FileWriter fo = new FileWriter("license/AcceptedLicense");
            FileWriter fo = new FileWriter(Constant.getInstance().ACCEPTED_LICENSE);
	        fo.close();
	    }catch (IOException ie){
	        JOptionPane.showMessageDialog(new JFrame(), "Unknown Error. Please report to the author.");
	        System.exit(1);
	    }
	    return shown;
	}
    
    /**
     * If update file exist, run it and exit.
     *
     */
    private void checkUpdate() {
        
        // for Linux platforms, this will be done in the startserver.sh
        
        if (!Constant.isWindows()) {
            return;
        }
        
        File file = new File(System.getProperty("user.dir") + File.separator + "parosnew.exe");
        String dir = System.getProperty("user.dir");
        String[] cmdArray = {
                "cmd.exe",
                "/C",
                "autoupd.bat",
                "\"" + dir + "\""
        };
        
        if (file.exists()) {

            try {
                Process p = Runtime.getRuntime().exec(cmdArray, null, new File(dir));
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
   
        }
    }
    
}

