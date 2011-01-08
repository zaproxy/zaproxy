/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap;

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
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.SSLConnector;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.autoupdate.ExtensionAutoUpdate;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.view.AboutWindow;
import org.zaproxy.zap.view.LicenseFrame;
import org.zaproxy.zap.view.LocaleDialog;
import org.zaproxy.zap.view.ProxyDialog;

public class ZAP {
    
    private static Log log = null;
    private CommandLine cmdLine = null;

    static {
	    
	    // set SSLConnector as socketfactory in HttpClient.
	    ProtocolSocketFactory sslFactory = null;
	    try {
	        Protocol protocol = Protocol.getProtocol("https");
	        sslFactory = protocol.getSocketFactory();
	    } catch (Exception e) {
			// Print the exception - log not yet initialised
	    	e.printStackTrace();
	    }
	    if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
	        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new SSLConnector(), 443));
	    }
    }
    
	public static void main(String[] args) throws Exception {
	    ZAP zap = new ZAP();
	    zap.init(args);
        Constant.getInstance();
        String msg = Constant.PROGRAM_NAME + " " + Constant.PROGRAM_VERSION + " started.";
        log = LogFactory.getLog(ZAP.class);
	    log.info(msg);
	    
	    try {
	        zap.run();
	    } catch (Exception e) {
	        log.fatal(e.getStackTrace());
	        throw e;
	    }
		
	}

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
	    	// Get the systems Look and Feel
	    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    	
	    	// Set Nimbus LaF if available and system is not OSX
	    	if (!Constant.isMacOsX()) {
		        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		            if ("Nimbus".equals(info.getName())) {
		                UIManager.setLookAndFeel(info.getClassName());
		                break;
		            }
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
	    
	    AboutWindow aboutWindow = null;
	    boolean firstTime = false;
	    if (cmdLine.isGUI()) {
		    firstTime = showLicense();
	        aboutWindow = new AboutWindow();
	        aboutWindow.setVisible(true);
	    }
	    
	    Model.getSingleton().init();
	    Model.getSingleton().getOptionsParam().setGUI(cmdLine.isGUI());
	    
	    // Prompt for language if not set
	    if (Model.getSingleton().getOptionsParam().getViewParam().getConfigLocale() == null) {
        	// Dont use a parent of the MainFrame - that will initialise it with English! 
			LocaleDialog dialog = new LocaleDialog(null, true);
			dialog.init(Model.getSingleton().getOptionsParam());
			dialog.setVisible(true);
			Constant.setLocale(Model.getSingleton().getOptionsParam().getViewParam().getLocale());
			Model.getSingleton().getOptionsParam().getViewParam().getConfig().save();
	    }

	    // Prompt for proxy details if set
	    if (Model.getSingleton().getOptionsParam().getConnectionParam().isProxyChainPrompt()) {
			ProxyDialog dialog = new ProxyDialog(View.getSingleton().getMainFrame(), true);
			dialog.init(Model.getSingleton().getOptionsParam());
			dialog.setVisible(true);
	    }
		
		if (Model.getSingleton().getOptionsParam().isGUI()) {
			View.setDisplayOption(Model.getSingleton().getOptionsParam().getViewParam().getDisplayOption());
		    runGUI();
		    aboutWindow.dispose();
		    
		    if (firstTime) {
		    	ExtensionHelp.showHelp();
		    } else {
		    	// Dont auto check for updates the first time, no chance of any proxy having been set
			    ExtensionAutoUpdate eau = (ExtensionAutoUpdate)
		    	Control.getSingleton().getExtensionLoader().getExtension("ExtensionAutoUpdate");
			    if (eau != null) {
			    	eau.checkForUpdates(false);
			    }
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
	
	private boolean showLicense() {
		boolean shown = false;
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
            FileWriter fo = new FileWriter(Constant.getInstance().ACCEPTED_LICENSE);
	        fo.close();
	    }catch (IOException ie){
	        JOptionPane.showMessageDialog(new JFrame(), "Unknown Error. Please report to the author.");
	        System.exit(1);
	    }
	    return shown;
	}
    
}

