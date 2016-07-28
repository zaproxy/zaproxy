/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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

public final class TimeStampUtils {
     
	private static final String DEFAULT_TIME_STAMP_FORMAT =  Constant.messages.getString("timestamp.format.default");
	private static final String TIME_STAMP_DELIMITER =  Constant.messages.getString("timestamp.format.delimiter");
	private static final String SAFE_TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss"; // Just in-case something goes wrong in translation
    
	/** 
     * Default constructor 
     */
	private TimeStampUtils() {
		
	}
	
    /** 
     * Gets the current time stamp based on the DEFAULT format.
     * The DEFAULT format is defined in Messages.properties.
     * @return current formatted time stamp as {@code String}
     */
	public static String currentDefaultFormattedTimeStamp() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIME_STAMP_FORMAT);
			final String formattedTimeStamp = sdf.format(new Date());
			return formattedTimeStamp;
		} catch (IllegalArgumentException e) {
			SimpleDateFormat sdf = new SimpleDateFormat(SAFE_TIME_STAMP_FORMAT);
			final String formattedTimeStamp = sdf.format(new Date());
			return formattedTimeStamp;
		}
	}
	
   /** 
    * Gets the current date/time based on the provided {@code format} 
    * which is a SimpleDateFormat string. If application of the provided
    * {@code format} fails a default format is used. 
    * The DEFAULT is defined in Messages.properties.
    * @see SimpleDateFormat
    * @param format a {@code String} representing the date format
    * @return current formatted time stamp as {@code String}
    */
	public static String currentFormattedTimeStamp(String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			final String formattedTimeStamp = sdf.format(new Date());
			return formattedTimeStamp; 
		} catch (IllegalArgumentException e) {
			return(TimeStampUtils.currentDefaultFormattedTimeStamp());  
		} catch (NullPointerException e) {
			return(TimeStampUtils.currentDefaultFormattedTimeStamp());  
		} 
	}
	
	
   /** 
    * Returns the provided {@code message} along with a date/time based 
    * on the provided {@code format} which is a SimpleDateFormat string. 
    * If application of the provided {@code format} fails a default format is used. 
    * The DEFAULT format is defined in Messages.properties.
    * @param message the message to be time stamped
    * @param format the format to be used in creating the time stamp
    * @return a time stamp in the designated format along with the original message
    * 
    * @see SimpleDateFormat
    */
	public static String getTimeStampedMessage(String message, String format){
		StringBuilder timeStampedMessage = new StringBuilder(format.length()+TIME_STAMP_DELIMITER.length()+message.length()+2);
		
		timeStampedMessage.append(currentFormattedTimeStamp(format)); //Timestamp
		timeStampedMessage.append(' ').append(TIME_STAMP_DELIMITER).append(' '); //Padded Delimiter
		timeStampedMessage.append(message); //Original message
		
		return timeStampedMessage.toString();
	}
	
}

