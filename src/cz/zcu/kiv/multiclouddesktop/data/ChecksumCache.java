package cz.zcu.kiv.multiclouddesktop.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * cz.zcu.kiv.multiclouddesktop.data/ChecksumCache.java			<br /><br />
 *
 * Bean for holding checksum information in local file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ChecksumCache {

	/** Modification date of the checksum cache. */
	private Date modified;
	/** Modification date of the remote caches. */
	private Map<String, Date> remote;
	/** List of remote file checksums. */
	@JsonProperty("data")
	private List<Checksum> checksums;

	/**
	 * Returns list of remote file checksums.
	 * @return List of remote file checksums.
	 */
	public List<Checksum> getChecksums() {
		return checksums;
	}

	/**
	 * Returns modification date of the checksum cache.
	 * @return Modification date of the checksum cache.
	 */
	public Date getModified() {
		return modified;
	}

	/**
	 * Returns the modification dates of remote caches.
	 * @return Modification dated of remote caches.
	 */
	public Map<String, Date> getRemote() {
		return remote;
	}

	/**
	 * Sets list of remote file checksums.
	 * @param checksums List of remote file checksums.
	 */
	public void setChecksums(List<Checksum> checksums) {
		this.checksums = checksums;
	}

	/**
	 * Sets modification date of the checksum cache.
	 * @param modified Modification date of the checksum cache.
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}

	/**
	 * Sets the modification dates of remote caches.
	 * @param remote Modification dates of remote caches.
	 */
	public void setRemote(Map<String, Date> remote) {
		this.remote = remote;
	}

}
