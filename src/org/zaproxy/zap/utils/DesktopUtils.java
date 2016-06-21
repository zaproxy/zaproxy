package org.zaproxy.zap.utils;

import java.awt.Desktop;
import java.net.URI;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class DesktopUtils {
	
	private static enum BrowserInvoker {desktop, browserlauncher, none};

	private static BrowserInvoker DEFAULT_INVOKER = BrowserInvoker.desktop;
	// Initialize the default invoker to desktop, if it's supported
	static {
		if (Desktop.isDesktopSupported())
			DEFAULT_INVOKER = BrowserInvoker.desktop;
		else
			DEFAULT_INVOKER = BrowserInvoker.browserlauncher;
	}

	private static BrowserInvoker invoker = null; 
    private static BrowserLauncher launcher = null;

    private static Logger log = Logger.getLogger(DesktopUtils.class);

	public static boolean openUrlInBrowser (URI uri) {
		
		try {
			switch (getInvoker()) {
			case desktop:
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(uri);
					return true;
				}
				break;
			case browserlauncher:
				getBrowserLauncher().openURLinBrowser(uri.toString());
				return true;
			case none:
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			invoker = BrowserInvoker.none;
		}
		return false;
		
	}
	
	public static boolean openUrlInBrowser (String uri) {
		try {
			return openUrlInBrowser(new URI(uri));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			invoker = BrowserInvoker.none;
		}
		return false;
	}
	
	public static boolean openUrlInBrowser (org.apache.commons.httpclient.URI uri) {
		try {
			return openUrlInBrowser(uri.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			invoker = BrowserInvoker.none;
		}
		return false;
	}
	
	public static boolean canOpenUrlInBrowser () {
		switch (getInvoker()) {
		case desktop:
        		return Desktop.isDesktopSupported();
		case browserlauncher:
				return true;
		case none:
				return false;
		default:
				return false;
		}
		
	}
	
	private static BrowserInvoker getInvoker() {
		if (invoker == null) {
			try {
				invoker = BrowserInvoker.valueOf(
						Model.getSingleton().getOptionsParam().getConfig().getString("TBA", DEFAULT_INVOKER.name()));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				invoker = DEFAULT_INVOKER;
			}
		}
		return invoker;
	}
	
	private static BrowserLauncher getBrowserLauncher() 
			throws BrowserLaunchingInitializingException, UnsupportedOperatingSystemException {
		if (launcher == null) {
			createBrowserLauncher();
		}
		return launcher;
	}
	
	private static synchronized void createBrowserLauncher()
			throws BrowserLaunchingInitializingException, UnsupportedOperatingSystemException {
		if (launcher == null) {
			launcher = new BrowserLauncher();
		}
	}
}
