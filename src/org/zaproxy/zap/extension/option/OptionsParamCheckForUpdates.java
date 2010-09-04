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
package org.zaproxy.zap.extension.option;

import org.parosproxy.paros.common.AbstractParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsParamCheckForUpdates extends AbstractParam {

	public static final String CHECK_ON_START = "start.checkForUpdates";
	
	private int checkOnStart = 0;
	private boolean unset = true;
	
    /**
     * @param rootElementName
     */
    public OptionsParamCheckForUpdates() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse() {
        
	    checkOnStart = getConfig().getInt(CHECK_ON_START, 0);
		unset = getConfig().getString(CHECK_ON_START, "").equals("");
    }

	/**
	 * @return Returns the skipImage.
	 */
	public int getCheckOnStart() {
		return checkOnStart;
	}
	
	public boolean isCheckOnStartUnset() {
		return unset;
	}
	
	/**
	 * @param processImages 0 = not to process.  Other = process images
	 * 
	 */
	public void setChckOnStart(int checkOnStart) {
		this.checkOnStart = checkOnStart;
		getConfig().setProperty(CHECK_ON_START, Integer.toString(checkOnStart));
	}
	
	public boolean isCheckOnStart() {
		return !(checkOnStart == 0);
	}
}
