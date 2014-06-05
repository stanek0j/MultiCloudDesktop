package cz.zcu.kiv.multiclouddesktop.data;

import java.io.File;
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
	/** Checksum of the file. */
	private String checksum;
	/** Original checksum of the file before synchronization. */
	@JsonIgnore
	private String origChecksum;
	/** Local file to be synchronized. */
	@JsonIgnore
	private File localFile;
	/** If the node is root. */
	@JsonIgnore
	private boolean root;

	/**
	 * Empty ctor.
	 */
	public SyncData() {
		accounts = new HashMap<>();
		nodes = new ArrayList<>();
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
	 * Returns local file.
	 * @return Local file.
	 */
	public File getLocalFile() {
		return localFile;
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
	 * Returns the original checksum of the file.
	 * @return Original checksum of the file.
	 */
	public String getOrigChecksum() {
		return origChecksum;
	}

	/**
	 * Returns if the node is root.
	 * @return If the node is root.
	 */
	public boolean isRoot() {
		return root;
	}

	/**
	 * Sets selected synchronization accounts.
	 * @param accounts Selected synchronization accounts.
	 */
	public void setAccounts(Map<String, FileInfo> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Sets the checksum of the file.
	 * @param checksum Checksum of the file.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * Sets local file.
	 * @param localFile Local file.
	 */
	public void setLocalFile(File localFile) {
		this.localFile = localFile;
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
	 * Sets the original checksum of the file.
	 * @param origChecksum Original checksum of the file.
	 */
	public void setOrigChecksum(String origChecksum) {
		this.origChecksum = origChecksum;
	}

	/**
	 * Sets if the node is root.
	 * @param root If the node is root.
	 */
	public void setRoot(boolean root) {
		this.root = root;
	}

}
