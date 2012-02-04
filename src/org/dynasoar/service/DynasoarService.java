package org.dynasoar.service;

import java.util.ArrayList;
import java.util.List;

public class DynasoarService {
	private String shortName = null;
	private String name = null;
	private boolean deployed = false;
	private List<String> deployNodes = new ArrayList<String>();

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public void addDeployNode(String nodeName) {
		this.deployNodes.add(nodeName);
	}

	public void removeDeployNode(String nodeName) {
		this.deployNodes.remove(nodeName);
	}

	public DynasoarService updateNode(DynasoarService node) {
		this.setDeployed(node.isDeployed());
		this.setName(node.getName());

		return this;
	}
}
