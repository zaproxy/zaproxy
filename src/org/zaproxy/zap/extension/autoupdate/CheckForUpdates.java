/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy Project
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
package org.zaproxy.zap.extension.autoupdate;

import java.io.IOException;

import javax.swing.SwingWorker;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;

public class CheckForUpdates extends SwingWorker<String, String> {

    //private static final String GF_ZAP_LATEST_OLD = "http://zaproxy.googlecode.com/svn/wiki/LatestVersion.wiki";
	// The short URL means that the number of checkForUpdates can be tracked - see http://goo.gl/info/QfCpK
    private static final String GF_ZAP_LATEST_XML_SHORT = "http://goo.gl/QfCpK";
    // The long URL is a failsafe ;)
    private static final String GF_ZAP_LATEST_XML_FULL = "http://code.google.com/p/zaproxy/wiki/LatestVersionXml";
    
    private static final String ZAP_START_TAG = "&lt;ZAP&gt;";
    private static final String ZAP_END_TAG = "&lt;/ZAP&gt;";
	private HttpSender httpSender = null;

    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
    private ExtensionAutoUpdate extension = null;
    String latestVersionName = null;

    public CheckForUpdates (ExtensionAutoUpdate ext) {
		this.extension = ext;
	}
	
	@Override
	protected String doInBackground() throws Exception {
        latestVersionName = getLatestVersionName();
		return latestVersionName;
	}
	
	@Override
	public void done() {
		extension.checkComplete(latestVersionName);
	}

    private String getLatestVersionName() {
        String newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_LATEST_XML_SHORT);
        if (newVersionName.length() == 0) {
        	// Shortened version failed, try going direct
            newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_LATEST_XML_FULL);
        }

        httpSender.shutdown();
        httpSender = null;
        
        return newVersionName;
    }
    
    private String getLatestVersionNameFromUrl(String url) {
        String newVersionName = "";
        HttpMessage msg = null;
        String resBody = null;
        
        try {
            msg = new HttpMessage(new URI(url, true));
            getHttpSender().sendAndReceive(msg,true);
            if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
            	logger.error("Failed to access " + url +
            			" response " + msg.getResponseHeader().getStatusCode());
                throw new IOException();
            }
            resBody = msg.getResponseBody().toString();
            
            int startIndex = resBody.indexOf(ZAP_START_TAG);
            if (startIndex > 0) {
            	startIndex += ZAP_START_TAG.length();
                int endIndex = resBody.indexOf(ZAP_END_TAG, startIndex);
            	newVersionName = resBody.substring(startIndex, endIndex ); 
            }
            
        } catch (Exception e) {
        	logger.error("Failed to access " + url, e);
            newVersionName = "";
        }
        
        return newVersionName;
    }

    private HttpSender getHttpSender() {
        if (httpSender == null) {
            httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true);
        }
        return httpSender;
    }

}
