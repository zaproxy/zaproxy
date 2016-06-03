/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP development team
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

package org.zaproxy.zap.extension.alert;

import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.EventPublisher;

public class AlertEventPublisher implements EventPublisher {
	
	private static AlertEventPublisher publisher = null;
	public static final String ALERT_ADDED_EVENT	= "alert.added";
	public static final String ALERT_CHANGED_EVENT = "alert.changed";
	public static final String ALERT_REMOVED_EVENT	= "alert.removed";
	public static final String ALL_ALERTS_REMOVED_EVENT	= "alert.all.removed";
	
    public static final String ALERT_ID = "alertId";
	/**
	 * Indicates the {@code HistoryReference} ID of the alert.
	 * <p>
	 * The field is available in the events {@link #ALERT_ADDED_EVENT}, {@link #ALERT_CHANGED_EVENT} and
	 * {@link #ALERT_REMOVED_EVENT}.
	 * 
	 * @since 2.5.0
	 */
	public static final String HISTORY_REFERENCE_ID = "historyId";

	@Override
	public String getPublisherName() {
		return AlertEventPublisher.class.getCanonicalName();
	}

	public static synchronized AlertEventPublisher getPublisher() {
		if (publisher == null) {
			publisher = new AlertEventPublisher(); 
	        ZAP.getEventBus().registerPublisher(publisher, 
	        		new String[] {ALERT_ADDED_EVENT, ALERT_CHANGED_EVENT, ALERT_REMOVED_EVENT, ALL_ALERTS_REMOVED_EVENT});

		}
		return publisher;
	}
}
