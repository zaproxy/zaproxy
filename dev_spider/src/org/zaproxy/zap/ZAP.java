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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.SSLConnector;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.autoupdate.ExtensionAutoUpdate;
import org.zaproxy.zap.extension.dynssl.DynSSLParam;
import org.zaproxy.zap.extension.dynssl.DynamicSSLWelcomeDialog;
import org.zaproxy.zap.extension.dynssl.ExtensionDynSSL;
import org.zaproxy.zap.spider.Spider;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.utils.ClassLoaderUtil;
import org.zaproxy.zap.utils.LocaleUtils;
import org.zaproxy.zap.view.LicenseFrame;
import org.zaproxy.zap.view.LocaleDialog;
import org.zaproxy.zap.view.ProxyDialog;


public class ZAP {

    private static Logger log = null;
    private CommandLine cmdLine = null;

    static {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());

	    // set SSLConnector as socketfactory in HttpClient.
	    ProtocolSocketFactory sslFactory = null;
	    try {
	        final Protocol protocol = Protocol.getProtocol("https");
	        sslFactory = protocol.getSocketFactory();
	    } catch (final Exception e) {
			// Print the exception - log not yet initialised
	    	e.printStackTrace();
	    }
	    if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
	        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new SSLConnector(), 443));
	    }
    }

	public static void main(String[] args) throws Exception {
	    final ZAP zap = new ZAP();
	    zap.init(args);
        Constant.getInstance();
        final String msg = Constant.PROGRAM_NAME + " " + Constant.PROGRAM_VERSION + " started.";
        
        BasicConfigurator.configure();
        
        log = Logger.getLogger(ZAP.class);
	    log.info(msg);

	    try {
	        zap.run();
	    } catch (final Exception e) {
	        log.fatal(e.getMessage(), e);
	        //throw e;
	        System.exit(1);
	    }

	}

	/**
	 * Initialization without dependence on any data model nor view creation.
	 * @param args
	 */
	private void init(String[] args) {
		try {
			// lang directory includes all of the language files
			final File langDir = new File ("lang");
			if (langDir.exists() && langDir.isDirectory()) {
				ClassLoaderUtil.addFile("lang");
			} else {
				System.out.println("Warning: failed to load language files from " + langDir.getAbsolutePath());
			}
			// Load all of the jars in the lib directory
			final File libDir = new File("lib");
			if (libDir.exists() && libDir.isDirectory()) {
				final File[] files = libDir.listFiles();
				for (final File file : files) {
					if (file.getName().toLowerCase(Locale.ENGLISH).endsWith("jar")) {
						ClassLoaderUtil.addFile(file);
					}
				}
			} else {
				System.out.println("Warning: failed to load jar files from " + libDir.getAbsolutePath());
			}
		} catch (final Exception e) {
			System.out.println("Failed loading jars: " + e);
		}

	    //HttpSender.setUserAgent(Constant.USER_AGENT);
	    try {
	        cmdLine = new CommandLine(args);
	    } catch (final Exception e) {
	        System.out.println(CommandLine.getHelpGeneral());
	        System.exit(1);
	    }

	}

	private void run() throws Exception {
	    
		final boolean isGUI = cmdLine.isGUI();
		
	    boolean firstTime = false;
	    if (isGUI) {
		    try {
		    	// Get the systems Look and Feel
		    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		    	// Set Nimbus LaF if available and system is not OSX
		    	if (!Constant.isMacOsX()) {
			        for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			            if ("Nimbus".equals(info.getName())) {
			                UIManager.setLookAndFeel(info.getClassName());
			                break;
			            }
			        }
		    	}
		    } catch (final UnsupportedLookAndFeelException e) {
		        // handle exception
		    } catch (final ClassNotFoundException e) {
		        // handle exception
		    } catch (final InstantiationException e) {
		        // handle exception
		    } catch (final IllegalAccessException e) {
		        // handle exception
		    }
		    
		    firstTime = showLicense();
	    }

	    try {
			Model.getSingleton().init();
	    } catch (final java.io.FileNotFoundException e) {
	    	if (isGUI) {
	    		JOptionPane.showMessageDialog(null,
	    				Constant.messages.getString("start.db.error"),
	    				Constant.messages.getString("start.title.error"),
	    				JOptionPane.ERROR_MESSAGE);
	    	}
    		System.out.println(Constant.messages.getString("start.db.error"));
    		System.out.println(e.getLocalizedMessage());

	    	throw e;
	    }
	    Model.getSingleton().getOptionsParam().setGUI(isGUI);

		if (isGUI) {

		    // Prompt for language if not set
			String locale = Model.getSingleton().getOptionsParam().getViewParam().getConfigLocale();
		    if (locale == null || locale.length() == 0) {
	        	// Dont use a parent of the MainFrame - that will initialise it with English!
				final Locale userloc = determineUsersSystemLocale();
		    	if (userloc == null) {
		    		// Only show the dialog, when the user's language can't be guessed.
					final LocaleDialog dialog = new LocaleDialog(null, true);
					dialog.init(Model.getSingleton().getOptionsParam());
					dialog.setVisible(true);
				} else {
					Model.getSingleton().getOptionsParam().getViewParam().setLocale(userloc);
				}
				Constant.setLocale(Model.getSingleton().getOptionsParam().getViewParam().getLocale());
				Model.getSingleton().getOptionsParam().getViewParam().getConfig().save();
		    }

		    // Prompt for proxy details if set
		    if (Model.getSingleton().getOptionsParam().getConnectionParam().isProxyChainPrompt()) {
				final ProxyDialog dialog = new ProxyDialog(View.getSingleton().getMainFrame(), true);
				dialog.init(Model.getSingleton().getOptionsParam());
				dialog.setVisible(true);
		    }

		    View.setDisplayOption(Model.getSingleton().getOptionsParam().getViewParam().getDisplayOption());
		    runGUI();
		    

		    if (firstTime) {
		    	// Disabled for now - we have too many popups occuring when you first start up
		    	// be nice to have a clean start up wizard...
		    	// ExtensionHelp.showHelp();
		    } else {
		    	// Dont auto check for updates the first time, no chance of any proxy having been set
			    final ExtensionAutoUpdate eau = (ExtensionAutoUpdate)
		    	Control.getSingleton().getExtensionLoader().getExtension("ExtensionAutoUpdate");
			    if (eau != null) {
			    	eau.checkForUpdates(false);
			    }
		    }
		    
		    // check root certificate
		    ExtensionDynSSL extension = (ExtensionDynSSL) Control.getSingleton().getExtensionLoader().getExtension("ExtensionDynSSL");
		    if (extension != null) {
			    DynSSLParam dynsslparam = extension.getParams();
			    if (dynsslparam.getRootca() == null) {
			    	DynamicSSLWelcomeDialog dlg = new DynamicSSLWelcomeDialog(View.getSingleton().getMainFrame(), true);
			    	dlg.setVisible(true);
			    	dlg.dispose();
			    }
		    }
	    } else if (cmdLine.isDaemon()) {
	    	runDaemon();
	    	Thread.sleep(1000);
	        //TODO: Debugging purpose
			HttpMessage msg=null;
			try {
				msg = new HttpMessage(new HttpRequestHeader(HttpRequestHeader.GET, new URI("http://www.prosc.ro",
						true), HttpHeader.HTTP11));
			} catch (URIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (HttpMalformedHeaderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Spider spider=new Spider(new SpiderParam(),Model.getSingleton().getOptionsParam().getConnectionParam(), Model.getSingleton());
			spider.addSeed(msg);
			spider.start();
	    } else {
	        runCommandLine();
	    }

	}

	/**
	 * Determines the {@link Locale} of the current user's system.
	 * It will match the {@link Locale#getDefault()} with the available
	 * locales from ZAPs translation files. It may return null, if the users
	 * system locale is not in the list of available translations of ZAP.
	 * @return
	 */
	private Locale determineUsersSystemLocale() {
		Locale userloc = null;
		final Locale systloc = Locale.getDefault();
		// first, try full match
		for (String ls : LocaleUtils.getAvailableLocales()){
			String[] langArray = ls.split("_");
		    if (langArray.length == 1) {
		    	if (systloc.getLanguage().equals(langArray[0])) {
		    		userloc = systloc;
		    		break;
		    	}
		    }
		    if (langArray.length == 2) {
		    	if (systloc.getLanguage().equals(langArray[0]) && systloc.getCountry().equals(langArray[1])) {
		    		userloc = systloc;
		    		break;
		    	}
		    }
		    if (langArray.length == 3) {
		    	if (systloc.getLanguage().equals(langArray[0]) && systloc.getCountry().equals(langArray[1]) &&  systloc.getVariant().equals(langArray[2])) {
		    		userloc = systloc;
		    		break;
		    	}
		    }
		}
		if (userloc == null) {
			// second, try partial language match
			for (String ls : LocaleUtils.getAvailableLocales()){
				String[] langArray = ls.split("_");
				if (systloc.getLanguage().equals(langArray[0])) {
					if (langArray.length == 1) {
						userloc = new Locale(langArray[0]);
					} else if (langArray.length == 2) {
						userloc = new Locale(langArray[0], langArray[1]);
					} else if (langArray.length == 3) {
						userloc = new Locale(langArray[0], langArray[1], langArray[2]);
					}
					break;
				}
			}
		}
		return userloc;
	}

	private void runCommandLine() {
	    int rc = 0;
	    String help = "";

	    Control.initSingletonWithoutView();
	    final Control control = Control.getSingleton();

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
	            } catch (final InterruptedException e) {}
	        }
		    rc = 0;
	    } catch (final Exception e) {
	        log.error(e.getMessage(), e);
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
	    final Control control = Control.getSingleton();
	    final View view = View.getSingleton();
	    view.postInit();
	    view.getMainFrame().setVisible(true);

	    control.getMenuFileControl().newSession(false);

	}

	private void runDaemon() throws Exception {
		// start in a background thread
        final Thread t = new Thread(new Runnable() {
            @Override
			public void run() {
        		Control.initSingletonWithoutView();
            }});
        t.start();
	}

	private boolean showLicense() {
		boolean shown = false;
		
		File acceptedLicenseFile = new File(Constant.getInstance().ACCEPTED_LICENSE);
		
        if (!acceptedLicenseFile.exists()){
	        final LicenseFrame license = new LicenseFrame();
	        license.setVisible(true);
	        while (!license.isAccepted()) {
	            try {
	                Thread.sleep(100);
	            } catch (final InterruptedException e) {
	            	log.error(e.getMessage(), e);
	            }
	        }
	        shown = true;

	        try{
	            acceptedLicenseFile.createNewFile();
	        }catch (final IOException ie){
	            JOptionPane.showMessageDialog(new JFrame(), Constant.messages.getString("start.unknown.error"));
	            log.error(ie.getMessage(), ie);
	            System.exit(1);
	        }
	    }
	    
	    return shown;
	}

	private static final class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {
		private static final Logger logger = Logger.getLogger(UncaughtExceptionLogger.class);

		private static boolean loggerConfigured = false;
		
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (!(e instanceof ThreadDeath)) {
				if (loggerConfigured || isLoggerConfigured()) {
					logger.error("Exception in thread \"" + t.getName() + "\"", e);
				} else {
					System.err.println("Exception in thread \"" + t.getName() + "\"");
					e.printStackTrace();
				}
			}
		}
		
		private static boolean isLoggerConfigured() {
			if (loggerConfigured) {
				return true;
			}
			
			@SuppressWarnings("unchecked")
			Enumeration<Appender> appenders = LogManager.getRootLogger().getAllAppenders();
			if (appenders.hasMoreElements()) {
				loggerConfigured = true;
			} else {
				
				@SuppressWarnings("unchecked")
				Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
				while (loggers.hasMoreElements()) {
					Logger c = loggers.nextElement();
					if (c.getAllAppenders().hasMoreElements()) {
						loggerConfigured = true;
						break;
					}
				}
			}
			
			return loggerConfigured;
		}
	}

}
