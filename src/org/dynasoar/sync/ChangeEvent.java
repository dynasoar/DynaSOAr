package org.dynasoar.sync;

public interface ChangeEvent {
	public void fileCreated(String path);

	public void fileModified(String path);

	public void fileRemoved(String path);
}
