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

package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.scanner.ExtensionScanner;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;


public class ProxyListenerPassiveScan implements ProxyListener {

	@SuppressWarnings("unused")
	private ExtensionPassiveScan extension = null;
	private List<PassiveScanDefn> passiveScanners = new ArrayList<PassiveScanDefn>();

	private ExtensionHistory extHist = null; 
	private ExtensionScanner extScan = null; 
	
	private Logger logger = Logger.getLogger(this.getClass());

	public ProxyListenerPassiveScan(ExtensionPassiveScan extensionPassiveScan) {
		extension = extensionPassiveScan;
	}

	private void gotMatch(PassiveScanDefn scanner, HttpMessage msg) {
		
		if (extHist == null) {
			extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory");
			extScan = (ExtensionScanner) Control.getSingleton().getExtensionLoader().getExtension("ExtensionScanner");
		}
		
		logger.debug("Scanner " + scanner.getName() + 
				" matched URL " + msg.getRequestHeader().getURI().toString() +
				" action = " + scanner.getType().toString());
		HistoryReference hRef = msg.getHistoryRef();
		
		switch (scanner.getType()) {
		case TAG:
			String tag = scanner.getConfig(); 
			msg.addTag(tag);
			if (hRef != null) {
				// If the hRef has been created before msg gets updated then
				// the tag wont have been stored in the db, so double check
				// and add in if missing
				if (! hRef.getTags().contains(tag)) {
					hRef.addTag(tag);
					extHist.getHistoryList().notifyItemChanged(hRef);
				}
			}
			break;
		case ALERT:
  		    Alert alert = scanner.getAlert(msg);
			if (hRef != null) {
				// If the hRef has been created before msg gets updated then
				// the alerts wont have been stored in the db, so double check
				// and add in if missing
				// TODO of course this wont help if the href doesnt exist...
				alert.setSourceHistoryId(hRef.getHistoryId());
				hRef.addAlert(alert);
				extHist.getHistoryList().notifyItemChanged(hRef);
			}
		    // Raise the alert
			extScan.alertFound(alert);

			break;
		}
	}
	/*
	public void onHttpRequestSend(HttpMessage msg) {
		for (PassiveScanDefn scanner : passiveScanners) {
			if (scanner.getRequestHeaderPattern() != null) {
				Matcher m = scanner.getRequestHeaderPattern().matcher(
						msg.getRequestHeader().toString());
				if (m.find()) {
					// Scanner matches, so do what it wants...
					gotMatch(scanner, msg);
				}
			}
			if (scanner.getRequestUrlPattern() != null) {
				Matcher m = scanner.getRequestUrlPattern().matcher(
						msg.getRequestHeader().getURI().toString());
				if (m.find()) {
					// Scanner matches, so do what it wants...
					gotMatch(scanner, msg);
				}
			}
		}
	}

	public void onHttpResponseReceive(HttpMessage msg) {
		for (PassiveScanDefn scanner : passiveScanners) {
			if (scanner.getResponseHeaderPattern() != null) {
				Matcher m = scanner.getResponseHeaderPattern().matcher(
						msg.getResponseHeader().toString());
				if (m.find()) {
					// Scanner matches, so do what it wants...
					gotMatch(scanner, msg);
				}
			}
			if (scanner.getResponseBodyPattern() != null) {
				Matcher m = scanner.getResponseBodyPattern().matcher(
						msg.getResponseBody().toString());
				if (m.find()) {
					// Scanner matches, so do what it wants...
					gotMatch(scanner, msg);
				}
			}
		}
	}
*/
	public void onHttpRequestSend(HttpMessage msg) {
		for (PassiveScanDefn scanner : passiveScanners) {
			if (scanner.scanHttpRequestSend(msg)) {
				gotMatch(scanner, msg);
			}
		}
	}

	public void onHttpResponseReceive(HttpMessage msg) {
		// This does work, but is commented out until it can be used effectively...
		Document document = null;
		/*
		UserAgentContext context = new SimpleUserAgentContext();
		DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
		// A document URI and a charset should be provided.
		String charSet = msg.getResponseHeader().getCharset();
		if (charSet == null || charSet.length() == 0) {
			charSet = "UTF-8";
		}
		try {
			System.out.println("PreParsed doc charSet=" + charSet);
			document = dbi.parse(
					new InputSourceImpl(
						new ByteArrayInputStream(msg.getResponseBody().toString().getBytes(charSet)),
						msg.getRequestHeader().getURI().toString(),
						charSet));
			
			//InputSource inputSource = new InputSource(new StringReader(msg.getResponseBody().toString()));
			//document = (HTMLDocument) dbi.createDocument(inputSource);
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		for (PassiveScanDefn scanner : passiveScanners) {
			if (scanner.scanHttpResponseReceive(msg, document)) {
				gotMatch(scanner, msg);
			}
		}
	}
	protected void add (PassiveScanDefn scanner) {
		passiveScanners.add(scanner);
	}
	
	protected void remove (PassiveScanDefn scanner) {
		passiveScanners.remove(scanner);
	}

	protected List<PassiveScanDefn> list () {
		return this.passiveScanners;
	}
	
	protected PassiveScanDefn getDefn(int index) {
		return this.passiveScanners.get(index);
	}
	
	protected PassiveScanDefn getDefn(String name) {
		for (PassiveScanDefn scanner : passiveScanners) {
			if (scanner.getName().equals(name)) {
				return scanner;
			}
		}
		return null;
	}

	public void save(PassiveScanDefn defn) {
		passiveScanners.remove(defn);
		passiveScanners.add(defn);
	}
}
