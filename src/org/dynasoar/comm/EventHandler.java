package org.dynasoar.comm;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dynasoar.service.ServiceChangeEvent;
import org.dynasoar.service.ServiceMonitor;
import org.dynasoar.sync.SyncEvent;
import org.dynasoar.util.Event;

/**
 * Responsible for handling all the node-level events or delegating them to
 * other handlers.
 * 
 * @author Rakshit Menpara
 */
public abstract class EventHandler implements EventListener {
	private List<Event> events = null;
	private static Logger logger = Logger.getLogger(EventHandler.class);
	private Class<? extends Event> eventType = null;

	public EventHandler(Class<? extends Event> eventType) {
		this.eventType = eventType;
		this.events = new ArrayList<Event>();
	}

	public void emitEvent(Event event) {
		// Add event to the processing list
		this.events.add(event);

		// Resume the thread to process it
		logger.info("New event added.");
		this.notify();
	}

	/**
	 * Processes or delegates all the events available in the list.
	 */
	public void processEvents() {
		Iterator<Event> i = events.iterator();
		while (i.hasNext()) {
			Event current = i.next();

			// Check if this handler supports processing current event
			if (this.eventType.isInstance(current)) {
				logger.info("Processing event: " + current.toString());
				this.handle(current);
			} else {
				this.delegate(current);
			}
		}

		// Clear the event queue once processed
		events.clear();
	}

	private void delegate(Event event) {
		if (event instanceof ServiceChangeEvent) {
			ServiceMonitor.newEvent(event);
		} else if (event instanceof NodeChangeEvent) {
			NodeCommunicator.newEvent(event);
		} else if (event instanceof SyncEvent) {
			NodeCommunicator.newEvent(event);
		} else {
			logger.error("Event could not be handled.");
		}
	}

	public abstract void handle(Event event);
}
