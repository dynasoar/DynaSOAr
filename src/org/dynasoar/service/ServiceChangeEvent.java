package org.dynasoar.service;

import org.dynasoar.util.Event;

/**
 * Represents a Service change event. e.g. Service added/removed/changed
 * 
 * @author Rakshit Menpara
 */
public class ServiceChangeEvent implements Event {
	private DynasoarService service = null;
	private ServiceEventType type = null;

	public ServiceChangeEvent(DynasoarService service, ServiceEventType type) {
		this.service = service;
		this.type = type;
	}

	public ServiceEventType getType() {
		return this.type;
	}

	public String getServiceName() {
		return this.service.getName();
	}

	public DynasoarService getService() {
		return this.service;
	}

	@Override
	public String toString() {
		return this.service.getName() + ": " + this.type;
	}
}
