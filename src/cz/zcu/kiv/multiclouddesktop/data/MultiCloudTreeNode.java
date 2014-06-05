package cz.zcu.kiv.multiclouddesktop.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import cz.zcu.kiv.multicloud.json.FileInfo;

/**
 * cz.zcu.kiv.multiclouddesktop.data/MultiCloudTreeNode.java			<br /><br />
 *
 * Tree node for holding additional information about the node.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloudTreeNode extends DefaultMutableTreeNode {

	/** Serialization constant. */
	private static final long serialVersionUID = 7377046544705041439L;

	/** Name of the node. */
	private final String name;
	/** Local folder or file. */
	private final File file;
	/** Accounts to synchronize to. */
	private final Map<String, FileInfo> accounts;
	/** Checksum of the file. */
	private String checksum;

	/**
	 * Ctor with local file supplied.
	 * @param file Local file.
	 */
	public MultiCloudTreeNode(File file) {
		this.name = file.getName();
		this.file = file;
		this.accounts = new HashMap<>();
	}

	/**
	 * Ctor with local file and account list supplied.
	 * @param file Local file.
	 * @param accounts Account list.
	 */
	public MultiCloudTreeNode(File file, Map<String, FileInfo> accounts) {
		this.name = file.getName();
		this.file = file;
		if (file.isDirectory()) {
			this.accounts = new HashMap<>();
		} else {
			this.accounts = accounts;
		}
	}

	/**
	 * Ctor with local file and custom name.
	 * @param file Local file.
	 * @param name Custom name.
	 */
	public MultiCloudTreeNode(File file, String name) {
		this.name = name;
		this.file = file;
		this.accounts = new HashMap<>();
	}

	/**
	 * Returns selected synchronization accounts.
	 * @return Selected synchronization accounts.
	 */
	public Map<String, FileInfo> getAccounts() {
		return accounts;
	}

	/**
	 * Returns local file.
	 * @return Local file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the checksum of the file.
	 * @return Checksum of the file.
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * Returns the name of the node.
	 * @return Name of the node.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the checksum of the file.
	 * @param checksum Checksum of the file.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

}
