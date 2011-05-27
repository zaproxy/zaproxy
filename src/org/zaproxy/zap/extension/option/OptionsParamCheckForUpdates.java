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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamCheckForUpdates extends AbstractParam {

	public static final String CHECK_ON_START = "start.checkForUpdates";
	public static final String DAY_LAST_CHECKED = "start.dayLastChecked";
	
	private int checkOnStart = 0;
	// Day last checked is used to ensure if the user has agreed then we only check the first time ZAP is run every day
	private String dayLastChecked = null; 
	private boolean unset = true;
    private static Log log = LogFactory.getLog(OptionsParamCheckForUpdates.class);

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
	    dayLastChecked = getConfig().getString(DAY_LAST_CHECKED, "");
	    // There was a bug in 1.2.0 where it defaulted silently to dont check
	    // We now use the lack of a dayLastChecked value to indicate we should reprompt the user.
		unset = getConfig().getString(CHECK_ON_START, "").equals("") || dayLastChecked.length() == 0;
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
		if (dayLastChecked.length() == 0) {
			dayLastChecked = "Never";
			getConfig().setProperty(DAY_LAST_CHECKED, dayLastChecked);
		}
	}
	
	public boolean isCheckOnStart() {
		if (checkOnStart == 0) {
			log.debug("isCheckForStart - false");
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		if (today.equals(dayLastChecked)) {
			log.debug("isCheckForStart - already checked today");
			return false;
		}
		getConfig().setProperty(DAY_LAST_CHECKED, today);
		try {
			getConfig().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
		}
		
		return true;
	}
}
