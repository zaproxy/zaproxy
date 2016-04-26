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

package org.parosproxy.paros.model;

import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.EventPublisher;

public class SiteMapEventPublisher implements EventPublisher {
	
	private static SiteMapEventPublisher publisher = null;
	public static final String SITE_NODE_ADDED_EVENT	= "siteNode.added";
	public static final String SITE_NODE_REMOVED_EVENT	= "siteNode.removed";
	public static final String SITE_ADDED_EVENT	= "site.added";
	public static final String SITE_REMOVED_EVENT	= "site.removed";
	
	@Override
	public String getPublisherName() {
		return SiteMapEventPublisher.class.getCanonicalName();
	}

	public static synchronized SiteMapEventPublisher getPublisher() {
		if (publisher == null) {
			publisher = new SiteMapEventPublisher(); 
	        ZAP.getEventBus().registerPublisher(publisher, 
	        		new String[] {SITE_NODE_ADDED_EVENT, SITE_NODE_REMOVED_EVENT, SITE_ADDED_EVENT, SITE_REMOVED_EVENT});

		}
		return publisher;
	}
}
