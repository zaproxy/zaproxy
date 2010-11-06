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
package org.zaproxy.zap.extension.bruteforce;

import org.parosproxy.paros.common.AbstractParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BruteForceParam extends AbstractParam {

	private static final String THREAD_PER_SCAN = "bruteforce.threadPerHost";
	
	public static final int DEFAULT_THREAD_PER_SCAN = 10;
		
	private int threadPerScan = DEFAULT_THREAD_PER_SCAN;
	
	
    /**
     * @param rootElementName
     */
    public BruteForceParam() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse(){
		try {
			setThreadPerScan(getConfig().getInt(THREAD_PER_SCAN, DEFAULT_THREAD_PER_SCAN));
		} catch (Exception e) {}
    }

    public int getThreadPerScan() {
        return threadPerScan;
    }
    
    public void setThreadPerScan(int threadPerHost) {
        this.threadPerScan = threadPerHost;
        getConfig().setProperty(THREAD_PER_SCAN, Integer.toString(this.threadPerScan));

    }


}
