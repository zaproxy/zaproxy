/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.core;

public class SimpleExample {

	/**
	 * A simple example showing how to use the API to spider and active scan a site
	 * and then retrieve and print out the alerts.
	 * ZAP must be running on the specified host and port for this script to work
	 * @param args
	 */
	public static void main(String[] args) {
		
		String zapHost = "localhost";
		int zapPort = 8090;
		String apikey = "";	// Change this if you have set the apikey in ZAP via Options / API
		
		String target = "http://localhost:8080/bodgeit/";
		
		try {
			
			ClientApi api = new ClientApi(zapHost, zapPort);
			
			// Start spidering the target
			System.out.println("Spider : " + target);
			ApiResponse resp = api.spider.scan(apikey, target);
			String scanid;
			int progress;
			
			// The scan now returns a scan id to support concurrent scanning
			scanid = ((ApiResponseElement)resp).getValue();
			
			// Poll the status until it completes
			while (true) {
				Thread.sleep(1000);
				progress = Integer.parseInt(((ApiResponseElement)api.spider.status(scanid)).getValue());
				System.out.println("Spider progress : " + progress + "%");
				if (progress >= 100) {
					break;
				}
			}
			System.out.println("Spider complete");

			// Give the passive scanner a chance to complete 
			Thread.sleep(2000);

			System.out.println("Active scan : " + target);
			resp = api.ascan.scan(apikey, target, "True", "False");
			
			if (! "OK".equals(((ApiResponseElement)resp).getValue())) {
				System.out.println("Failed to Active Scan target : " + resp.toString(0));
				return;
			}
			
			// Poll the status until it completes
			while (true) {
				Thread.sleep(5000);
				progress = Integer.parseInt(((ApiResponseElement)api.ascan.status()).getValue());
				System.out.println("Active Scan progress : " + progress + "%");
				if (progress >= 100) {
					break;
				}
			}
			System.out.println("Active Scan complete");

			System.out.println("Alerts:");
			System.out.println(new String(api.core.xmlreport(apikey)));
			
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}

}
