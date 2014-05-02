package cz.zcu.kiv.multiclouddesktop.data;

public class AccountData {

	private String name;
	private String cloud;
	private long totalSpace;
	private long freeSpace;
	private long usedSpace;

	public String getCloud() {
		return cloud;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public String getName() {
		return name;
	}

	public long getTotalSpace() {
		return totalSpace;
	}

	public long getUsedSpace() {
		return usedSpace;
	}

	public void setCloud(String cloud) {
		this.cloud = cloud;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}

	public void setUsedSpace(long usedSpace) {
		this.usedSpace = usedSpace;
	}

}
