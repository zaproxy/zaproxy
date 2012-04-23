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
package org.zaproxy.zap.extension.fuzz;

import org.parosproxy.paros.common.AbstractParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FuzzerParam extends AbstractParam {

	private static final String DEFAULT_CATEGORY = "fuzzer.defaultCategory";
	private static final String THREADS_PER_SCAN = "fuzzer.threadPerScan";
	private static final String DELAY_IN_MS = "fuzzer.delayInMs";
		
	private String defaultCategory = "XSS";
	private int threadPerScan = 5;
	private int delayInMs = 0;
	
    /**
     * @param rootElementName
     */
    public FuzzerParam() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    @Override
    protected void parse(){
        
		try {
			setThreadPerScan(getConfig().getInt(THREADS_PER_SCAN, 1));
		} catch (Exception e) {}
		try {
			setDefaultCategory(getConfig().getString(DEFAULT_CATEGORY, "XSS"));
		} catch (Exception e) {}
		try {
			setDelayInMs(getConfig().getInt(DELAY_IN_MS, 0));
		} catch (Exception e) {}

    }

	public void setDelayInMs(int delayInMs) {
		this.delayInMs = delayInMs;
		getConfig().setProperty(DELAY_IN_MS, Integer.toString(this.delayInMs));
	}

	public int getDelayInMs() {
		return delayInMs;
	}

	public String getDefaultCategory() {
		return defaultCategory;
	}

	public void setDefaultCategory(String defaultCategory) {
		this.defaultCategory = defaultCategory;
        getConfig().setProperty(DEFAULT_CATEGORY, this.defaultCategory);
	}

	public int getThreadPerScan() {
		return threadPerScan;
	}

	public void setThreadPerScan(int threadPerScan) {
		this.threadPerScan = threadPerScan;
        getConfig().setProperty(THREADS_PER_SCAN, Integer.toString(this.threadPerScan));
	}
	
}
