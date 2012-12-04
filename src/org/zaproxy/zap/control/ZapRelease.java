/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
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

package org.zaproxy.zap.control;

import java.net.URL;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class ZapRelease {
	private String version;
	private URL url;
	private String fileName;
	private long size;
	private String releaseNotes;

    private static final Logger logger = Logger.getLogger(ZapRelease.class);

	public ZapRelease() {
	}
	
	public ZapRelease(String version) {
		this.version = version;
	}
	
	public ZapRelease(String version, URL url, String fileName, long size, String releaseNotes) {
		super();
		this.version = version;
		this.url = url;
		this.fileName = fileName;
		this.size = size;
		this.releaseNotes = releaseNotes;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getReleaseNotes() {
		return releaseNotes;
	}

	public void setReleaseNotes(String releaseNotes) {
		this.releaseNotes = releaseNotes;
	}
	
	public boolean isNewerThan (String otherVersion, boolean isCheckDaily) {
		boolean newerVersion = false;
		if (Constant.isDevBuild(this.version)) {
			// A dev build is always treated as the most recent 
			return true;
		} else if (Constant.isDevBuild(otherVersion)) {
			return false;
		} else if (isCheckDaily) {
        	// Will just be a 'dated' version, which we can just use a string compare on
			return otherVersion.compareTo(this.version) < 0;
		} else if (otherVersion == null) {
			return true;
        } else {
	    	// Compare the versions
	    	String [] versionArray = this.version.split("\\.");
	    	String [] otherArray = otherVersion.split("\\.");
	    	//boolean newerVersion = false;
	    	for (int i = 0; i < versionArray.length; i++) {
	    		if (Constant.ALPHA_VERSION.equals(versionArray[i]) ||
	    				Constant.BETA_VERSION.equals(versionArray[i])) {
	    			// Alpha and beta versions will only ever appear in this.version,
	    			// everything has matched up to now so its a newer 'release' quality version
	    			newerVersion = false;
	    			break;
	    		} else if (i < otherArray.length) {
    				int versionElement;
    				int otherElement;
					try {
						versionElement = Integer.parseInt(versionArray[i]);
						otherElement = Integer.parseInt(otherArray[i]);
						if (versionArray[i].equals(otherArray[i])) {
							// this element is the same, keep going
							continue;
						} else if ( versionElement > otherElement) {
							// Previous elements were the same, latest element newer
        					newerVersion = true;
        					break;
        				} else {
							// Previous elements were the same, latest element older
        					// This can happen for alpha & beta releases
        					break;
        				}
					} catch (NumberFormatException e) {
						logger.error("Invalid release number: " + this.version + " / " + otherVersion, e);
	    			}
	    		}
	    	}
	    	if (!newerVersion  && this.version.startsWith(otherVersion) 
	    			&& otherArray.length > versionArray.length) {
	    		// All matched up to the progVersion, but the latestVersion is longer and therefore newer
				newerVersion = true;
	    	}
		}
		return newerVersion;
	}
	
	
}
