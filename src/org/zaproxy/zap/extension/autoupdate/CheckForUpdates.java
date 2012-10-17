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
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;

public class CheckForUpdates extends SwingWorker<String, String> {

    //private static final String GF_ZAP_LATEST_OLD = "http://zaproxy.googlecode.com/svn/wiki/LatestVersion.wiki";
	// The short URL means that the number of checkForUpdates can be tracked - see http://goo.gl/info/sIhXE
    private static final String GF_ZAP_LATEST_XML_SHORT = "http://goo.gl/sIhXE";
    private static final String GF_ZAP_DAILY_XML_SHORT = "http://goo.gl/7RBIp";
    // The long URL is a failsafe ;)
    private static final String GF_ZAP_LATEST_XML_FULL = "http://code.google.com/p/zaproxy/wiki/LatestVersionXml";
    
    private static final String ZAP_START_TAG = "&lt;ZAP&gt;";
    private static final String ZAP_END_TAG = "&lt;/ZAP&gt;";
    private static final String ZAP_D_START_TAG = "&lt;ZAP_D&gt;";
    private static final String ZAP_D_END_TAG = "&lt;/ZAP_D&gt;";
	private HttpSender httpSender = null;

    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
    private ExtensionAutoUpdate extension = null;
    String latestVersionName = null;

    public CheckForUpdates (ExtensionAutoUpdate ext) {
		this.extension = ext;
	}
	
	@Override
	protected String doInBackground() throws Exception {
        latestVersionName = getLatestVersionName(Constant.isDailyBuild());
		return latestVersionName;
	}
	
	private boolean isNewerVersion (String progVersionStr, String latestVersionStr, boolean checkDaily) {
		boolean newerVersion = false;
		if (Constant.isDevBuild(progVersionStr)) {
			return false;
		} else if (checkDaily && Constant.isDailyBuild(progVersionStr)) {
        	// Will just be a 'dated' version, which we can just use a string compare on
			return progVersionStr.compareTo(latestVersionStr) < 0;
        } else if (latestVersionStr != null && latestVersionStr.length() > 0) {
	    	// Compare the versions
	    	String [] progVersion = progVersionStr.split("\\.");
	    	String [] latestVersion = latestVersionStr.split("\\.");
	    	//boolean newerVersion = false;
	    	for (int i = 0; i < progVersion.length; i++) {
	    		if (Constant.ALPHA_VERSION.equals(progVersion[i]) ||
	    				Constant.BETA_VERSION.equals(progVersion[i])) {
	    			// Alpha and beta versions will only ever appear in the progVersion,
	    			// everything has matched up to now so its a newer 'release' quality version
	    			newerVersion = true;
	    			break;
	    		} else if (i < latestVersion.length) {
    				int progElement;
    				int latestElement;
					try {
						progElement = Integer.parseInt(progVersion[i]);
						latestElement = Integer.parseInt(latestVersion[i]);
						if (progVersion[i].equals(latestVersion[i])) {
							// this element is the same, keep going
							continue;
						} else if (latestElement > progElement) {
							// Previous elements were the same, latest element newer
        					newerVersion = true;
        					break;
        				} else {
							// Previous elements were the same, latest element older
        					// This can happen for alpha & beta releases
        					break;
        				}
					} catch (NumberFormatException e) {
						logger.error(e.getMessage(), e);
						//System.out.println("Error: " + e);
	    			}
	    		}
	    	}
	    	if (!newerVersion  && latestVersionStr.startsWith(progVersionStr) 
	    			&& latestVersion.length > progVersion.length) {
	    		// All matched up to the progVersion, but the latestVersion is longer and therefore newer
				newerVersion = true;
	    	}
		}
		return newerVersion;
	}
	
	@Override
	public void done() {
		extension.checkComplete(
				this.isNewerVersion(Constant.PROGRAM_VERSION, latestVersionName, true), 
				latestVersionName);
	}

    private String getLatestVersionName(boolean isDaily) {
        String newVersionName;
        if (isDaily) {
        	newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_DAILY_XML_SHORT);
        } else {
        	newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_LATEST_XML_SHORT);
        }
        if (newVersionName.length() == 0) {
        	// Shortened version failed, try going direct
            newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_LATEST_XML_FULL);
        }

        if (httpSender != null) {
	        httpSender.shutdown();
	        httpSender = null;
        }
        
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
            
            if (Constant.isDailyBuild()) {
            	// Check the dated version instead of the standard release format
	            int startIndex = resBody.indexOf(ZAP_D_START_TAG);
	            if (startIndex > 0) {
	            	startIndex += ZAP_D_START_TAG.length();
	                int endIndex = resBody.indexOf(ZAP_D_END_TAG, startIndex);
	            	newVersionName = resBody.substring(startIndex, endIndex ); 
	            }
            } else {
	            int startIndex = resBody.indexOf(ZAP_START_TAG);
	            if (startIndex > 0) {
	            	startIndex += ZAP_START_TAG.length();
	                int endIndex = resBody.indexOf(ZAP_END_TAG, startIndex);
	            	newVersionName = resBody.substring(startIndex, endIndex ); 
	            }
            }
            
        } catch (Exception e) {
        	logger.error("Failed to access " + url, e);
            newVersionName = "";
        }
        
        return newVersionName;
    }

    private HttpSender getHttpSender() {
        if (httpSender == null) {
            httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true, HttpSender.CHECK_FOR_UPDATES_INITIATOR);
        }
        return httpSender;
    }
    
	private void compareVersions (String progVersionStr, String latestVersionStr) {
		this.compareVersions(progVersionStr, latestVersionStr, false);
	}
	
	private void compareVersions (String progVersionStr, String latestVersionStr, boolean checkDaily) {
		if (this.isNewerVersion(progVersionStr, latestVersionStr, checkDaily)) {
			System.out.println(progVersionStr + "\tis older than " + latestVersionStr);
		} else {
			System.out.println(progVersionStr + "\tis NOT older than " + latestVersionStr);
		}
	}
    
	public static void main(String[] args) throws Exception {
		// Sanity tests ;)
		CheckForUpdates cfu = new CheckForUpdates(null);
		System.out.println("These should all be older:");
		cfu.compareVersions("1.3.4", "1.4");
		cfu.compareVersions("1.3.4", "2.0");
		cfu.compareVersions("1.4", "1.4.1");
		cfu.compareVersions("1.4.1", "1.4.2");
		cfu.compareVersions("1.4.2", "1.4.11");
		cfu.compareVersions("1.4.alpha.1", "1.4");
		cfu.compareVersions("1.4.beta.1", "1.5");
		cfu.compareVersions("D-2012-08-01", "D-2012-08-02", true);
		cfu.compareVersions("D-2012-01-01", "D-2013-10-10", true);
		System.out.println();
		System.out.println("These should all NOT be older:");
		cfu.compareVersions("1.4", "1.4");
		cfu.compareVersions("1.4", "1.3.4");
		cfu.compareVersions("1.4.2", "1.4.1");
		cfu.compareVersions("1.4.20", "1.4.11");
		cfu.compareVersions("1.4.alpha.1", "1.3.4");
		cfu.compareVersions("Dev Build", "1.5");
		cfu.compareVersions("D-2012-08-02", "D-2012-08-01", true);
		cfu.compareVersions("D-2013-10-10", "D-2012-01-01", true);
		System.out.println();
		System.out.println("These should cause errors:");
		cfu.compareVersions("1.4.1", "1.4.beta.2");
		cfu.compareVersions("1.4.theta.1", "1.4.3");
	}


}
