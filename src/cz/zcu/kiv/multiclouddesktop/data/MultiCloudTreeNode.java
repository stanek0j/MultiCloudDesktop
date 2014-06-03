package cz.zcu.kiv.multiclouddesktop.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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
	private final List<String> accounts;

	/**
	 * Ctor with local file supplied.
	 * @param file Local file.
	 */
	public MultiCloudTreeNode(File file) {
		this.name = file.getName();
		this.file = file;
		this.accounts = new ArrayList<>();
	}

	/**
	 * Ctor with local file and account list supplied.
	 * @param file Local file.
	 * @param accounts Account list.
	 */
	public MultiCloudTreeNode(File file, List<String> accounts) {
		this.name = file.getName();
		this.file = file;
		if (file.isDirectory()) {
			this.accounts = new ArrayList<>();
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
		this.accounts = new ArrayList<>();
	}

	/**
	 * Returns selected synchronization accounts.
	 * @return Selected synchronization accounts.
	 */
	public List<String> getAccounts() {
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
	 * Returns the name of the node.
	 * @return Name of the node.
	 */
	public String getName() {
		return name;
	}

}
