package org.dynasoar.sync;

import org.dynasoar.comm.CommEvent;
import org.dynasoar.service.DynasoarService;
import org.dynasoar.service.ServiceEventType;

/**
 * Represents a sync event. e.g. serviceConfig changed/added/removed, war file
 * changed
 * 
 * @author Rakshit Menpara
 */
public class SyncEvent implements CommEvent {

	public SyncEvent(DynasoarService service, ServiceEventType type) {

	}
}
