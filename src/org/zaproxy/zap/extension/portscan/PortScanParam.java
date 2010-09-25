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
package org.zaproxy.zap.extension.portscan;

import org.parosproxy.paros.common.AbstractParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PortScanParam extends AbstractParam {

	private static final String MAX_PORT = "portscan.maxPort";
	private static final String THREAD_PER_SCAN = "portscan.threadPerHost";
	
	public static final int DEFAULT_MAX_PORT = 9216;
	public static final int DEFAULT_THREAD_PER_SCAN = 3;
		
	private int maxPort = DEFAULT_MAX_PORT;
	private int threadPerScan = 1;
	
	
    /**
     * @param rootElementName
     */
    public PortScanParam() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse(){
		try {
			setThreadPerScan(getConfig().getInt(THREAD_PER_SCAN, 1));
		} catch (Exception e) {}
		try {
			setMaxPort(getConfig().getInt(MAX_PORT, 2));
		} catch (Exception e) {}

    }

    public int getThreadPerScan() {
        return threadPerScan;
    }
    
    public void setThreadPerScan(int threadPerHost) {
        this.threadPerScan = threadPerHost;
        getConfig().setProperty(THREAD_PER_SCAN, Integer.toString(this.threadPerScan));

    }

    /**
     * @return Returns the thread.
     */
    public int getMaxPort() {
        return maxPort;
    }
    /**
     * @param thread The thread to set.
     */
    public void setMaxPort(int maxPort) {
        this.maxPort = maxPort;
		getConfig().setProperty(MAX_PORT, Integer.toString(this.maxPort));

    }
	
}
