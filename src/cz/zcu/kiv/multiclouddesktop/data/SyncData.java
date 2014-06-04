package cz.zcu.kiv.multiclouddesktop.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cz.zcu.kiv.multicloud.json.FileInfo;

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
	private Map<String, FileInfo> accounts;
	/** Child nodes. */
	private List<SyncData> nodes;
	/** Remote files. */
	@JsonIgnore
	private List<FileInfo> remote;
	/** Checksum of the file. */
	private String checksum;
	/** If the file was changed. */
	@JsonIgnore
	private boolean changed;

	/**
	 * Empty ctor.
	 */
	public SyncData() {
		accounts = new HashMap<>();
		nodes = new ArrayList<>();
		remote = new ArrayList<>();
	}

	/**
	 * Returns selected synchronization accounts.
	 * @return Selected synchronization accounts.
	 */
	public Map<String, FileInfo> getAccounts() {
		return accounts;
	}

	/**
	 * Returns the checksum of the file.
	 * @return Checksum of the file.
	 */
	public String getChecksum() {
		return checksum;
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
	 * Returns remote files.
	 * @return Remote files.
	 */
	public List<FileInfo> getRemote() {
		return remote;
	}

	/**
	 * Returns if the file was changed.
	 * @return If the file was changed.
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Sets selected synchronization accounts.
	 * @param accounts Selected synchronization accounts.
	 */
	public void setAccounts(Map<String, FileInfo> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Sets if the file was changed.
	 * @param changed If the file was changed.
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * Sets the checksum of the file.
	 * @param checksum Checksum of the file.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
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

	/**
	 * Sets remote files.
	 * @param remote Remote files.
	 */
	public void setRemote(List<FileInfo> remote) {
		this.remote = remote;
	}

}
