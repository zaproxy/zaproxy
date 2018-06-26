package org.zaproxy.zap.utils;

import java.awt.Desktop;
import java.net.URI;

import org.apache.log4j.Logger;

public class DesktopUtils {
	
	private static enum BrowserInvoker {desktop, none};

	private static BrowserInvoker invoker = Desktop.isDesktopSupported() ? BrowserInvoker.desktop : BrowserInvoker.none;

    private static Logger log = Logger.getLogger(DesktopUtils.class);

	public static boolean openUrlInBrowser (URI uri) {
		
		try {
			if (invoker == BrowserInvoker.desktop) {
				Desktop.getDesktop().browse(uri);
				return true;
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
		return invoker == BrowserInvoker.desktop;
	}
}
