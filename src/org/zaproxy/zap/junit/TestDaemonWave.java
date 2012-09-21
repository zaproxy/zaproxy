package org.zaproxy.zap.junit;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Test;
import org.zaproxy.zap.ZAP;

public class TestDaemonWave {

	private List<String> openUrlViaProxy (Proxy proxy, String apiurl) throws Exception {
		List<String> response = new ArrayList<>();
		URL url = new URL(apiurl);
		HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
		uc.connect();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));

		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response.add(inputLine);
		}

		in.close();
		return response;
	}
	
	private void startZAP () throws Exception {
		ZAP.main(new String[]{"-daemon"});
		
		Thread.sleep(5000);
	}
	
	private void stopZAP (Proxy proxy) throws Exception {
		// TODO not found a reliable way of doing this inline yet :(
	}
	
	@Test
	public void testDaemonWave () throws Exception {
		startZAP();
		
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8090));

		// View (empty) hosts
		assertEquals("[[]]", openUrlViaProxy(proxy, "http://zap/json/core/view/hosts").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <hosts/>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/hosts").toString());
		
		// View (empty) sites
		assertEquals("[[]]", openUrlViaProxy(proxy, "http://zap/json/core/view/sites").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <sites/>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/sites").toString());

		// View (empty) URLs
		assertEquals("[[]]", openUrlViaProxy(proxy, "http://zap/json/core/view/urls").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <urls/>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/urls").toString());

		// View (empty) alerts
		assertEquals("[[]]", openUrlViaProxy(proxy, "http://zap/json/core/view/alerts").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <alerts/>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/alerts").toString());
		
		// Access one page via the proxy
		openUrlViaProxy(proxy, "http://localhost:8080/zap-wave/");
		Thread.sleep(2000);
		
		// View hosts (localhost)
		assertEquals("[[\"localhost\"]]", openUrlViaProxy(proxy, "http://zap/json/core/view/hosts").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <hosts><host type=\"string\">localhost</host></hosts>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/hosts").toString());

		// View sites (localhost)
		assertEquals("[[\"http://localhost:8080\"]]", openUrlViaProxy(proxy, "http://zap/json/core/view/sites").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <sites><site type=\"string\">http://localhost:8080</site></sites>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/sites").toString());

		
		// View URLs (2)
		assertEquals("[[\"http://localhost:8080\",\"http://localhost:8080/zap-wave/\"]]", 
				openUrlViaProxy(proxy, "http://zap/json/core/view/urls").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <urls><url type=\"string\">http://localhost:8080</url>" +
					"<url type=\"string\">http://localhost:8080/zap-wave/</url></urls>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/urls").toString());

		// Spider status - not started
		assertEquals("[[\"0\"]]", openUrlViaProxy(proxy, "http://zap/json/spider/view/status").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <status><percent type=\"string\">0</percent></status>]", 
				openUrlViaProxy(proxy, "http://zap/xml/spider/view/status").toString());

		// Spider
		assertEquals("[[\"OK\"]]", openUrlViaProxy(proxy, "http://zap/json/spider/action/scan/?url=http://localhost:8080/zap-wave/").toString());

		// wait till finished
		while ( ! openUrlViaProxy(proxy, "http://zap/json/spider/view/status").toString().equals("[[\"100\"]]")) {
			Thread.sleep(1000);
		}

		JSONArray jsonResponse;

		// Check all urls spidered
		jsonResponse = JSONArray.fromObject(openUrlViaProxy(proxy, "http://zap/json/core/view/urls").toString());
		assertEquals(1, jsonResponse.size());
		assertEquals(true, jsonResponse.get(0) instanceof JSONArray);
		assertEquals(38, ((JSONArray)jsonResponse.get(0)).size());

		// Ensure the passive scanning has been done
		Thread.sleep(5000);

		// View alerts (3)
		JSONArray alerts = JSONArray.fromObject(openUrlViaProxy(proxy, "http://zap/json/core/view/alerts").toString());
		assertEquals(1, alerts.size());
		assertEquals(true, alerts.get(0) instanceof JSONArray);
		assertEquals(3, ((JSONArray)alerts.get(0)).size());
		
		// Active Scan status - not started
		assertEquals("[[\"0\"]]", openUrlViaProxy(proxy, "http://zap/json/ascan/view/status").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <status><percent type=\"string\">0</percent></status>]",
				openUrlViaProxy(proxy, "http://zap/xml/ascan/view/status").toString());

		// Active Scan
		assertEquals("[[\"OK\"]]", openUrlViaProxy(proxy, "http://zap/json/ascan/action/scan/?url=http://localhost:8080/").toString());

		// wait till finished
		while ( ! openUrlViaProxy(proxy, "http://zap/json/ascan/view/status").toString().equals("[[\"100\"]]")) {
			Thread.sleep(1000);
		}

		// View alerts (11)
		// TODO check this is the right number!
		alerts = JSONArray.fromObject(openUrlViaProxy(proxy, "http://zap/json/core/view/alerts").toString());
		assertEquals(1, alerts.size());
		assertEquals(true, alerts.get(0) instanceof JSONArray);
		assertEquals(11, ((JSONArray)alerts.get(0)).size());
		
		// Bad view
		assertEquals("[{\"code\":\"bad_view\",\"message\":\"Bad View\"}]", 
				openUrlViaProxy(proxy, "http://zap/json/core/view/xxx").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <error><code type=\"string\">bad_view</code><message type=\"string\">Bad View</message></error>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/view/xxx").toString());

		// Bad action
		assertEquals("[{\"code\":\"bad_action\",\"message\":\"Bad Action\"}]", 
				openUrlViaProxy(proxy, "http://zap/json/core/action/xxx").toString());
		assertEquals("[<?xml version=\"1.0\" encoding=\"UTF-8\"?>, <error><code type=\"string\">bad_action</code><message type=\"string\">Bad Action</message></error>]", 
				openUrlViaProxy(proxy, "http://zap/xml/core/action/xxx").toString());

		stopZAP(proxy);

	}
	
	public static void main(String[] args) throws Exception {
		TestDaemonWave test = new TestDaemonWave();
		test.testDaemonWave ();
	}

}
