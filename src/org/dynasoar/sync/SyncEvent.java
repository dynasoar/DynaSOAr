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
	private DynasoarService service = null;
	private ServiceEventType type = null;
	private String WARHash = null;
	private String ConfigHash = null;
	private byte[] WARfile = null;

	public SyncEvent(DynasoarService service, ServiceEventType type) {
		this.service = service;
		this.type = type;
	}

	public ServiceEventType getType() {
		return this.type;
	}

	public DynasoarService getService() {
		return this.service;
	}

	public void setWARMD5Hash(String WARHash) {
		this.WARHash = WARHash;
	}

	public void setConfigMD5Hash(String ConfigHash) {
		this.ConfigHash = ConfigHash;
	}

	public String getWARMD5Hash() {
		return this.WARHash;
	}

	public String getConfigMD5Hash() {
		return this.ConfigHash;
	}

	public void setWARfile(byte WARfile[]) {
		System.arraycopy(WARfile, 0, this.WARfile, 0, WARfile.length);
	}

	public byte[] getWARfile() {
		return WARfile;
	}

}
