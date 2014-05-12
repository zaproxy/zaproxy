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

package org.zaproxy.zap.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.parosproxy.paros.Constant;

/**
 * Class to facilitate implementation of timestamps on various outputs
 * @author kingthorin+owaspzap@gmail.com
 */

public class TimeStamp {
     
	private static final String TIMESTAMPFORMAT_DEFAULT =  Constant.messages.getString("timestampformat.default");
		
    /** 
     * get the current date/time based on the DEFAULT format
     * @return aDate is a formatted String
     */
	public String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMPFORMAT_DEFAULT);
		final String aDate = sdf.format(new Date());
		return aDate; 
		}
	
   /** 
    * get the current date/time based on the provided SimpleDateFormat or DEFAULT
    * if application of the provided SimpleDateFormat fails
    * @param inSDF is a String
    * @return aDate is a formatted String
    */
	public String getTimeStamp(String inSDF) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(inSDF);
			final String aDate = sdf.format(new Date());
			return aDate; 
		} catch (Exception e) {
			SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMPFORMAT_DEFAULT);
			final String aDate = sdf.format(new Date());
			return aDate; 
		}
	}
	
}

