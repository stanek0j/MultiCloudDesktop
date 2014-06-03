package cz.zcu.kiv.multiclouddesktop.data;

import java.util.ArrayList;
import java.util.List;

/**
 * cz.zcu.kiv.multiclouddesktop.data/SyncData.java			<br /><br />
 *
 * Synchronization data entry.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SyncData {

	/** File name. */
	private String name;
	/** Accounts to synchronize to. */
	private List<String> accounts;
	/** Child nodes. */
	private List<SyncData> nodes;

	/**
	 * Empty ctor.
	 */
	public SyncData() {
		accounts = new ArrayList<>();
		nodes = new ArrayList<>();
	}

	/**
	 * Returns selected synchronization accounts.
	 * @return Selected synchronization accounts.
	 */
	public List<String> getAccounts() {
		return accounts;
	}

	/**
	 * Returns file name.
	 * @return File name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns child nodes.
	 * @return Child nodes.
	 */
	public List<SyncData> getNodes() {
		return nodes;
	}

	/**
	 * Sets selected synchronization accounts.
	 * @param accounts Selected synchronization accounts.
	 */
	public void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Sets file name.
	 * @param name File name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets child nodes.
	 * @param nodes Child nodes.
	 */
	public void setNodes(List<SyncData> nodes) {
		this.nodes = nodes;
	}

}
