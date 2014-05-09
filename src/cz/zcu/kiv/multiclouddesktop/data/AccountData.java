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
	 * Sets the name of the account.
	 * @param name Name of the account.
	 */
	public void setName(String name) {
		this.name = name;
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

}
