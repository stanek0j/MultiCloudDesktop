package cz.zcu.kiv.multiclouddesktop.data;

import java.util.Date;

import cz.zcu.kiv.multicloud.json.FileInfo;

/**
 * cz.zcu.kiv.multiclouddesktop.data/Checksum.java			<br /><br />
 *
 * Bean for holding important metadata information about remote files.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class Checksum {

	/** Account to which the data is related to. */
	private String account;
	/** Resource identifier. */
	private String id;
	/** Resource path. */
	private String path;
	/** Resource name. */
	private String name;
	/** Size in bytes of the resource. */
	private long size;
	/** MD5 checksum of the file. */
	private String checksum;
	/** Creation date. */
	private Date created;
	/** Last modification date. */
	private Date modified;

	/**
	 * Empty ctor.
	 */
	public Checksum() {
		account = null;
		id = null;
		path = null;
		name = null;
		size = 0;
		checksum = null;
		created = null;
		modified = null;
	}

	/**
	 * Ctor with necessary parameters.
	 * @param account Account name.
	 * @param file File information.
	 */
	public Checksum(String account, FileInfo file) {
		this.account = account;
		this.id = file.getId();
		this.path = file.getPath();
		this.name = file.getName();
		this.size = file.getSize();
		this.checksum = file.getChecksum();
		this.created = file.getCreated();
		this.modified = file.getModified();
	}

	/**
	 * Returns the account to which the data is related to.
	 * @return Account to which the data is related to.
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * Returns the creation date.
	 * @return Creation date.
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Returns the MD5 checksum of the file.
	 * @return MD5 checksum of the file.
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * Returns the identifier of the resource.
	 * @return Resource identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the last modification date.
	 * @return Last modification date.
	 */
	public Date getModified() {
		return modified;
	}

	/**
	 * Returns the name of the resource.
	 * @return Resource name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the path of the resource.
	 * @return Resource path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the size of the resource.
	 * @return Size of the resource.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Sets the account to which the data is related to.
	 * @param account Account to which the data is related to.
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * Sets the creation date.
	 * @param created Creation date.
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Sets the MD5 checksum of the file.
	 * @param checksum MD5 checksum of the file.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * Sets the identifier of the resource.
	 * @param id Resource identifier.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the last modification date.
	 * @param modified Last modification date.
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}

	/**
	 * Sets the name of the resource.
	 * @param name Resource name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the path of the resource.
	 * @param path Resource path.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Sets the size of the resource.
	 * @param size Size of the resource.
	 */
	public void setSize(long size) {
		this.size = size;
	}

}
