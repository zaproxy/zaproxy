/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.junit.wavsep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.zaproxy.clientapi.core.Alert;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.Alert.Reliability;
import org.zaproxy.clientapi.core.Alert.Risk;

public class WavsepStatic {

	private static Pattern staticPatternParam = Pattern.compile("&", Pattern.CASE_INSENSITIVE);
	
	// Change these statics to match you local setup
	private static String wavsepHost = "http://localhost:8080";
	private static String zapHost = "localhost";
	private static int zapPort = 8090;
	private static int sleepInMs = 1000;
	
	// Alerts for use in require/exclude lists, in alphabetical order - add any new ones required
	public static Alert CROSS_SITE_SCRIPTING = 
			new Alert("Cross Site Scripting", "", Risk.High, null, "", "");
	public static Alert INFO_DISCLOSURE_IN_URL = 
			new Alert("Information disclosure - sensitive informations in URL", "", Risk.Informational, Reliability.Warning, "", "");
	public static Alert X_CONTENT_TYPE_HEADER_MISSING = 
			new Alert("X-Content-Type-Options header missing", "", Risk.Low, Reliability.Warning, "", "");
	public static Alert X_FRAME_OPTIONS_HEADER_MISSING = 
			new Alert("X-Frame-Options header not set", "", Risk.Informational, Reliability.Warning, "", ""); 


	private static ClientApi initClientApi() throws Exception {
		ClientApi client = new ClientApi(zapHost, zapPort);
		client.newSession();
		return client;
	}
	
	// Note that this method is a copy of the code in org.parosproxy.paros.model
	private static TreeSet<String> getParamNameSet(String params) throws Exception {
	    TreeSet<String> set = new TreeSet<>();
	    String[] keyValue = staticPatternParam.split(params);
		String key = null;
		int pos = 0;
		for (int i=0; i<keyValue.length; i++) {
			key = null;
			pos = keyValue[i].indexOf('=');
			if (pos > 0) {
				// param found
				key = keyValue[i].substring(0,pos);

				//!!! note: this means param not separated by & and = is not parsed
			} else {
				key = keyValue[i];
			}
			
			if (key != null) {
				set.add(key);
			}
		}
		
		return set;
	}
    
	// Note that this method is a copy of the code in org.parosproxy.paros.model
    private static String getQueryParamString(SortedSet<String> querySet) {
    	StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = querySet.iterator();
        for (int i=0; iterator.hasNext(); i++) {
            String name = iterator.next();
            if (name == null) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            if (name.length() > 40) {
            	// Truncate
            	name = name.substring(0, 40);
            }
            sb.append(name);
        }

        String result = "";
        if (sb.length()>0) {
        	result = sb.insert(0, '(').append(')').toString();
        } 
        
        return result;
	}
    
    private static Alert[] setAlertsUrl(Alert[] alerts, String url) {
    	String baseUrl = url;
    	if (baseUrl.indexOf("?") > 0) {
    		baseUrl = url.substring(0, url.indexOf("?"));
    	}
    	for (Alert alert : alerts) {
    		alert.setUrl(baseUrl + ".*");
    	}
    	return alerts;
    }
	
	public static void genericTest(String relativeUrl, Alert[] ignoreAlerts, Alert[] requireAlerts) throws Exception {
		String url = wavsepHost + relativeUrl;

		ClientApi client = initClientApi();

		client.accessUrl(url);
		Thread.sleep(sleepInMs);
		
		String nodeName =  url;
		
		if (nodeName.indexOf("?") >0) {
	        String query = new URI(nodeName, true).getQuery();
	        if (query == null) {
	            query = "";
	        }
	        nodeName = nodeName.substring(0, nodeName.indexOf("?")) + getQueryParamString(getParamNameSet(query));
		}

		// Dont actually seem to need to spider the URLs...
		//client.spiderUrl(nodeName);
		//Thread.sleep(sleepInMs);
		
		client.activeScanUrl(nodeName);
		Thread.sleep(sleepInMs);

		List<Alert> ignoreAlertsList = new ArrayList<>(ignoreAlerts.length);
		ignoreAlertsList.addAll(Arrays.asList(setAlertsUrl(ignoreAlerts, url)));

		List<Alert> requireAlertsList = new ArrayList<>(requireAlerts.length);
		requireAlertsList.addAll(Arrays.asList(setAlertsUrl(requireAlerts, url)));
		
		client.checkAlerts(ignoreAlertsList, requireAlertsList);
	}

}
