package cz.zcu.kiv.multiclouddesktop.data;

/**
 * cz.zcu.kiv.multiclouddesktop.data/AccountData.java			<br /><br />
 *
 * Class for holding the information about an account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountData {

	/** Name of the account. */
	private String name;
	/** Cloud storage service provider for the account. */
	private String cloud;
	/** Total space available. */
	private long totalSpace;
	/** Free space available. */
	private long freeSpace;
	/** Used up space. */
	private long usedSpace;
	/** If the account is authorized. */
	private boolean authorized;
	/** If the account is listed. */
	private boolean listed;
	/** If the file is matched for this account. */
	private boolean matched;
	/** Path to the selected folder. */
	private String path;

	/**
	 * Returns the cloud storage service provider for the account.
	 * @return Cloud storage service provider for the account.
	 */
	public String getCloud() {
		return cloud;
	}

	/**
	 * Returns the free space available.
	 * @return Free space available.
	 */
	public long getFreeSpace() {
		return freeSpace;
	}

	/**
	 * Returns the name of the account.
	 * @return Name of the account.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the path to the selected folder.
	 * @return Path to the selected folder.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the total space available.
	 * @return Total space available.
	 */
	public long getTotalSpace() {
		return totalSpace;
	}

	/**
	 * Return the used up space.
	 * @return Used up space.
	 */
	public long getUsedSpace() {
		return usedSpace;
	}

	/**
	 * Returns if the account is authorized.
	 * @return If the account is authorized.
	 */
	public boolean isAuthorized() {
		return authorized;
	}

	/**
	 * Returns if the account is listed.
	 * @return If the account is listed.
	 */
	public boolean isListed() {
		return listed;
	}

	/**
	 * Returns if the file is matched for this account.
	 * @return If the file is matched for this account.
	 */
	public boolean isMatched() {
		return matched;
	}

	/**
	 * Sets if the account is authorized.
	 * @param authorized If the account is authorized.
	 */
	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	/**
	 * Sets the cloud storage service provider for the account.
	 * @param cloud Cloud storage service provider for the account.
	 */
	public void setCloud(String cloud) {
		this.cloud = cloud;
	}

	/**
	 * Sets the free space available.
	 * @param freeSpace Free space available.
	 */
	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

	/**
	 * Sets if the account is listed.
	 * @param listed If the account is listed.
	 */
	public void setListed(boolean listed) {
		this.listed = listed;
	}

	/**
	 * Sets if the file is matched for this account.
	 * @param matched If the file is matched for this account.
	 */
	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	/**
	 * Sets the name of the account.
	 * @param name Name of the account.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the path to the selected folder.
	 * @param path Path to the selected folder.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Sets the total space available.
	 * @param totalSpace Total space available.
	 */
	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}

	/**
	 * Sets the used up space.
	 * @param usedSpace Used up space.
	 */
	public void setUsedSpace(long usedSpace) {
		this.usedSpace = usedSpace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name + ((matched) ? " (file matched)" : "") + ((path != null) ? " (" + path + ")" : "");
	}

}
