package org.dynasoar.comm;

import java.util.EventListener;

/**
 * Responsible for handling all the node-level events or delegating them to
 * other handlers.
 * 
 * @author Rakshit Menpara
 */
public interface EventHandler extends EventListener {
	public void handle(Event event);
}
