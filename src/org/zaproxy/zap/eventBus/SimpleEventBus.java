package org.zaproxy.zap.eventBus;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A very simple event bus
 * @author simon
 *
 */
public class SimpleEventBus implements EventBus {
	
	private Map<String, RegisteredPublisher> nameToPublisher = new HashMap<String, RegisteredPublisher>();
	private List<RegisteredConsumer> danglingConsumers = new ArrayList<RegisteredConsumer>();

	private static Logger log = Logger.getLogger(SimpleEventBus.class);

	@Override
	public void registerPublisher(EventPublisher publisher, String[] eventTypes) {
		if (publisher == null) {
			throw new InvalidParameterException("Publisher must not be null");
		}
		if (eventTypes == null || eventTypes.length == 0) {
			throw new InvalidParameterException("At least one event type must be specified");
		}
		if (this.nameToPublisher.get(publisher.getPublisherName()) != null) {
			throw new InvalidParameterException("Publisher with name " + publisher.getPublisherName() + 
					" already registered by " + this.nameToPublisher.get(publisher.getPublisherName())
						.getPublisher().getClass().getCanonicalName());
		}
		log.debug("registerPublisher " + publisher.getPublisherName());
		
		RegisteredPublisher regProd = new RegisteredPublisher(publisher, eventTypes);
		
		this.nameToPublisher.put(publisher.getPublisherName(), regProd);

		// Check to see if there are any cached consumers
		for (RegisteredConsumer regCon : this.danglingConsumers) {
			if (regCon.getPublisherName().equals(publisher.getPublisherName())) {
				regProd.addComsumer(regCon);
				this.danglingConsumers.remove(regCon);
				break;
			}
		}

	}

	@Override
	public void unregisterPublisher(EventPublisher publisher) {
		if (publisher == null) {
			throw new InvalidParameterException("Publisher must not be null");
		}
		log.debug("unregisterPublisher " + publisher.getPublisherName());
		RegisteredPublisher regPub = nameToPublisher.remove(publisher.getPublisherName());
		if (regPub == null) {
			throw new InvalidParameterException("Publisher with name " + publisher.getPublisherName() + 
					" not registered");
		}
	}

	@Override
	public void registerConsumer(EventConsumer consumer, String publisherName) {
		this.registerConsumer(consumer, publisherName, null);
	}

	@Override
	public void registerConsumer(EventConsumer consumer, String publisherName,
			String[] eventTypes) {
		if (consumer == null) {
			throw new InvalidParameterException("Consumer must not be null");
		}
		log.debug("registerConsumer " + consumer.getClass().getCanonicalName() + " for " + publisherName);
		RegisteredPublisher publisher = this.nameToPublisher.get(publisherName);
		if (publisher == null) {
			// Cache until the publisher registers
			this.danglingConsumers.add(new RegisteredConsumer(consumer, eventTypes, publisherName));
		} else {
			publisher.addComsumer(consumer, eventTypes);
		}
	}

	@Override
	public void unregisterConsumer(EventConsumer consumer, String publisherName) {
		if (consumer == null) {
			throw new InvalidParameterException("Consumer must not be null");
		}
		log.debug("unregisterConsumer " + consumer.getClass().getCanonicalName() + " for " + publisherName);
		RegisteredPublisher publisher = this.nameToPublisher.get(publisherName);
		if (publisher != null) {
			publisher.removeComsumer(consumer);
		} else {
			// Check to see if its cached waiting for the publisher
			for (RegisteredConsumer regCon : this.danglingConsumers) {
				if (regCon.getConsumer().equals(consumer)) {
					this.danglingConsumers.remove(regCon);
					break;
				}
			}
		}
	}


	@Override
	public void publishSyncEvent(EventPublisher publisher, Event event) {
		if (publisher == null) {
			throw new InvalidParameterException("Publisher must not be null");
		}
		RegisteredPublisher regPublisher = this.nameToPublisher.get(publisher.getPublisherName());
		if (regPublisher == null) {
			throw new InvalidParameterException("Publisher not registered: " + publisher.getPublisherName());
		}
		log.debug("publishSyncEvent " + event.getEventType() + " from " + publisher.getPublisherName());
		boolean foundType = false;
		for (String type : regPublisher.getEventTypes()) {
			if (event.getEventType().equals(type)) {
				foundType = true;
				break;
			}
		}
		if (! foundType) {
			throw new InvalidParameterException("Event type: " + event.getEventType() + 
					" not registered for publisher: " + publisher.getPublisherName());
		}

		for (RegisteredConsumer regCon : regPublisher.getConsumers()) {
			String[] eventTypes = regCon.getEventTypes();
			boolean isListeningforEvent = false;
			if (eventTypes == null) {
				// They are listening for all events from this publisher
				isListeningforEvent = true;
			} else {
				for (String type : eventTypes) {
					if (event.getEventType().equals(type)) {
						isListeningforEvent = true;
						break;
					}
				}
			}
			if (isListeningforEvent) {
				try {
					regCon.getConsumer().eventReceived(event);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private class RegisteredConsumer {
		private EventConsumer consumer;
		private String[] eventTypes;
		private String publisherName;
		
		public RegisteredConsumer(EventConsumer consumer, String[] eventTypes) {
			this.consumer = consumer;
			this.eventTypes = eventTypes;
		}
		public RegisteredConsumer(EventConsumer consumer, String[] eventTypes, String publisherName) {
			this.consumer = consumer;
			this.eventTypes = eventTypes;
			this.publisherName = publisherName;
		}
		public EventConsumer getConsumer() {
			return consumer;
		}
		public String[] getEventTypes() {
			return eventTypes;
		}
		public String getPublisherName() {
			return publisherName;
		}
	}

	private class RegisteredPublisher {
		private EventPublisher publisher;
		private String[] eventTypes;
		private List<RegisteredConsumer> consumers = new ArrayList<RegisteredConsumer>();
		
		public RegisteredPublisher(EventPublisher publisher, String[] eventTypes) {
			super();
			this.publisher = publisher;
			this.eventTypes = eventTypes;
		}
		public EventPublisher getPublisher() {
			return publisher;
		}
		public String[] getEventTypes() {
			return eventTypes;
		}
		public List<RegisteredConsumer> getConsumers() {
			return consumers;
		}
		public void addComsumer(RegisteredConsumer consumer) {
			this.consumers.add(consumer);
		}
		public void addComsumer(EventConsumer consumer, String [] eventTypes) {
			this.consumers.add(new RegisteredConsumer(consumer, eventTypes));
		}
		public void removeComsumer(EventConsumer consumer) {
			for (RegisteredConsumer cons : consumers) {
				if (cons.getConsumer().equals(consumer)) {
					this.consumers.remove(cons);
					return;
				}
			}
		}
	}

}
