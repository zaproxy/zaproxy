package org.zaproxy.zap.junit;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;

/**
 * This test loads the image http://code.google.com/p/zaproxy/logo
 * to assert a working proxy.
 */
public class ProxyThreadTest extends BaseZapProxyTest {
	
	@Test
	public void receiveImage() throws Exception {
		URL url = new URL("http://code.google.com/p/zaproxy/logo");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		assertEquals("image/png", connection.getContentType());
		
		connection.disconnect();
	}

	@Test
	public void receiveSecureImage() throws Exception {
		URL url = new URL("https://code.google.com/p/zaproxy/logo");
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		
		assertEquals("image/png", connection.getContentType());
		
		connection.disconnect();
	}
}
