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
package org.zaproxy.zap.eventBus;

/**
 * Interface for the event bus used to publish events in ZAP
 * @author simon
 *
 */
public interface EventBus {

	/**
	 * Register a publisher - only registered publishers can publish events
	 * @param publisher the publisher
	 * @param eventTypes the full set of event types the publisher can publish
	 */
	void registerPublisher(EventPublisher publisher, String[] eventTypes);
	
	/**
	 * Unregister the publisher
	 * @param publisher
	 */
	void unregisterPublisher(EventPublisher publisher);
	
	/**
	 * Register the consumer for the specified publisher - this consumer will receive all events from the publisher 
	 * @param consumer
	 * @param publisherName
	 */
	void registerConsumer (EventConsumer consumer, String publisherName);
	
	/**
	 * Register the consumer for the specified publisher - this consumer will only receive the 
	 * specified events from the publisher 
	 * @param consumer
	 * @param publisherName
	 * @param eventTypes
	 */
	void registerConsumer (EventConsumer consumer, String publisherName, String[] eventTypes);
	
	/**
	 * Unregister the consumer
	 * @param consumer
	 * @param publisherName
	 */
	void unregisterConsumer(EventConsumer consumer, String publisherName);
	
	/**
	 * Publish the specified event synchronously
	 * @param publisher
	 * @param event
	 */
	void publishSyncEvent(EventPublisher publisher, Event event);
}
