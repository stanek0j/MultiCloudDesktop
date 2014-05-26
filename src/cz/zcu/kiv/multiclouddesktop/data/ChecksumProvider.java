package cz.zcu.kiv.multiclouddesktop.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.Json;

/**
 * cz.zcu.kiv.multiclouddesktop.data/ChecksumProvider.java			<br /><br />
 *
 * Class for providing checksums from local cache.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ChecksumProvider {

	/** Local file for storing checksums. */
	public static final String CHECKSUM_FILE = ".multicloud";

	/** Local checksum cache. */
	private ChecksumCache cache;
	/** JSON parser instance. */
	private final Json json;

	/**
	 * Empty ctor.
	 */
	public ChecksumProvider() {
		json = Json.getInstance();
		cacheLoad();
	}

	/**
	 * Add new checksum to the cache.
	 * @param account Account name.
	 * @param file File information.
	 */
	public synchronized void add(String account, FileInfo file) {
		if (file.getChecksum() != null) {
			boolean matched = false;
			for (Checksum ch: cache.getChecksums()) {
				if (matchChange(ch, account, file)) {
					ch.setId(file.getId());
					ch.setPath(file.getPath());
					ch.setName(file.getName());
					ch.setModified(file.getModified());
					ch.setSize(file.getSize());
					ch.setChecksum(file.getChecksum());
					matched = true;
					break;
				}
			}
			if (!matched) {
				Checksum checksum = new Checksum(account, file);
				cache.getChecksums().add(checksum);
			}
			cacheSave();
		}
	}

	/**
	 * Load checksum cache from local file.
	 */
	private void cacheLoad() {
		try {
			ObjectMapper mapper = json.getMapper();
			cache = mapper.readValue(new File(CHECKSUM_FILE), ChecksumCache.class);
		} catch (IOException e) {
			cache = new ChecksumCache();
			cache.setChecksums(new LinkedList<Checksum>());
		}
	}

	/**
	 * Save checksum cache to local file.
	 */
	private void cacheSave() {
		try {
			cache.setModified(new Date());
			ObjectMapper mapper = json.getMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CHECKSUM_FILE), cache);
		} catch (IOException e) {
			/* ignore if saving failed */
		}
	}

	/**
	 * Compute checksum of the provided file.
	 * @param file File to compute checksum for.
	 * @return Checksum of the file.
	 */
	public String computeChecksum(File file) {
		byte[] buffer = new byte[1024];
		int read;
		StringBuilder sb = new StringBuilder();
		try {
			FileInputStream fis = new FileInputStream(file);
			MessageDigest digest = MessageDigest.getInstance("MD5");
			DigestInputStream dis = new DigestInputStream(fis, digest);
			do {
				read = fis.read(buffer);
				if (read > 0) {
					digest.update(buffer, 0, read);
				}
			} while (read != -1);
			dis.close();
			for (byte b: digest.digest()) {
				sb.append(String.format("%02x", b & 0xff));
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Compute checksum from the provided stream.
	 * @param stream Stream to compute checksum for.
	 * @return Checksum of the stream.
	 */
	public String computeChecksum(InputStream stream) {
		byte[] buffer = new byte[1024];
		int read;
		StringBuilder sb = new StringBuilder();
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			DigestInputStream dis = new DigestInputStream(stream, digest);
			do {
				read = stream.read(buffer);
				if (read > 0) {
					digest.update(buffer, 0, read);
				}
			} while (read != -1);
			dis.close();
			for (byte b: digest.digest()) {
				sb.append(String.format("%02x", b & 0xff));
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Test if the cache contains a specified checksum.
	 * @param checksum Checksum to be found.
	 * @return If the checksum was found.
	 */
	public synchronized boolean contains(Checksum checksum) {
		boolean found = false;
		for (Checksum ch: cache.getChecksums()) {
			/* match ID if present */
			boolean condId = false;
			if ((checksum.getId() == null) && (ch.getId() == null)) {
				condId = true;
			} else if ((checksum.getId() != null) && (ch.getId() != null)) {
				condId = checksum.getId().equals(ch.getId());
			} else {
				continue;
			}
			/* match PATH if present */
			boolean condPath = false;
			if ((checksum.getPath() == null) && (ch.getPath() == null)) {
				condPath = true;
			} else if ((checksum.getPath() != null) && (ch.getPath() != null)) {
				condPath = checksum.getPath().equals(ch.getPath());
			} else {
				continue;
			}
			/* match NAME */
			boolean condName = checksum.getName().equals(ch.getName());
			/* accept modified remote file */
			boolean condMod = checksum.getModified().after(ch.getModified()) || checksum.getModified().equals(ch.getModified());
			/* return result */
			if (condId && condPath && condName && condMod) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Test if the cache contains a specified checksum.
	 * @param account Account name.
	 * @param file File information.
	 * @return If the checksum was found.
	 */
	public synchronized boolean contains(String account, FileInfo file) {
		return contains(new Checksum(account, file));
	}

	/**
	 * Check if the checksum from the cache matches provided file information.
	 * @param checksum Checksum from the cache.
	 * @param account Account name.
	 * @param file File information.
	 * @return If the file information match.
	 */
	public synchronized boolean match(Checksum checksum, String account, FileInfo file) {
		/* match ID if present */
		boolean condId = false;
		if ((checksum.getId() == null) && (file.getId() == null)) {
			condId = true;
		} else if ((checksum.getId() != null) && (file.getId() != null)) {
			condId = checksum.getId().equals(file.getId());
		} else {
			return false;
		}
		/* match PATH if present */
		boolean condPath = false;
		if ((checksum.getPath() == null) && (file.getPath() == null)) {
			condPath = true;
		} else if ((checksum.getPath() != null) && (file.getPath() != null)) {
			condPath = checksum.getPath().equals(file.getPath());
		} else {
			return false;
		}
		/* match NAME */
		boolean condName = checksum.getName().equals(file.getName());
		/* match last modification date */
		boolean condMod = checksum.getModified().equals(file.getModified());
		/* match SIZE */
		boolean condSize = checksum.getSize() == file.getSize();
		/* return result */
		return (condId && condPath && condName && condMod && condSize);
	}

	/**
	 * Check if the checksum from the cache matches provided file information, allowing modified remote file.
	 * @param checksum Checksum from the cache.
	 * @param account Account name.
	 * @param file File information.
	 * @return If the file information match.
	 */
	public synchronized boolean matchChange(Checksum checksum, String account, FileInfo file) {
		/* match ID if present */
		boolean condId = false;
		if ((checksum.getId() == null) && (file.getId() == null)) {
			condId = true;
		} else if ((checksum.getId() != null) && (file.getId() != null)) {
			condId = checksum.getId().equals(file.getId());
		} else {
			return false;
		}
		/* match PATH if present */
		boolean condPath = false;
		if ((checksum.getPath() == null) && (file.getPath() == null)) {
			condPath = true;
		} else if ((checksum.getPath() != null) && (file.getPath() != null)) {
			condPath = checksum.getPath().equals(file.getPath());
		} else {
			return false;
		}
		/* match NAME */
		boolean condName = checksum.getName().equals(file.getName());
		/* accept modified remote file */
		boolean condMod = checksum.getModified().before(file.getModified());
		/* return result */
		return (condId && condPath && condName && condMod);
	}

	/**
	 * Merges supplied cache into local cache.
	 * @param mergeCache Supplied cache.
	 */
	public synchronized void merge(ChecksumCache mergeCache) {
		if (cache != null) {
			/* determine newer cache */
			ChecksumCache temp;
			if (cache.getModified().after(mergeCache.getModified())) {
				temp = mergeCache;
			} else {
				temp = cache;
				cache = mergeCache;
			}
			/* merge the old entries into newer cache */
			for (Checksum ch: temp.getChecksums()) {
				if (!contains(ch)) {
					cache.getChecksums().add(ch);
				}
			}
		}
	}

	/**
	 * Provide checksum from the cache.
	 * @param account Account name.
	 * @param file File to be provided with checksum.
	 */
	public synchronized void provideChecksum(String account, FileInfo file) {
		for (Checksum ch: cache.getChecksums()) {
			if (match(ch, account, file)) {
				file.setChecksum(ch.getChecksum());
				break;
			}
		}
	}

	/**
	 * Provide checksums from the cache.
	 * @param account Account name.
	 * @param files Files to be provided with checksums.
	 */
	public synchronized void provideChecksum(String account, List<FileInfo> files) {
		for (FileInfo file: files) {
			provideChecksum(account, file);
		}
	}

	/**
	 * Remove checksum from the cache.
	 * @param account Account name.
	 * @param file File information.
	 */
	public synchronized void remove(String account, FileInfo file) {
		Checksum matched = null;
		for (Checksum ch: cache.getChecksums()) {
			if (match(ch, account, file)) {
				matched = ch;
				break;
			}
		}
		if (matched != null) {
			cache.getChecksums().remove(matched);
			cacheSave();
		}
	}

	/**
	 * Remove all checksums for an account.
	 * @param account Account name.
	 */
	public synchronized void removeAccount(String account) {
		List<Checksum> remove = new ArrayList<>();
		for (Checksum ch: cache.getChecksums()) {
			if (ch.getAccount().equals(account)) {
				remove.add(ch);
			}
		}
		if (!remove.isEmpty()) {
			cache.getChecksums().removeAll(remove);
			cacheSave();
		}
	}

	/**
	 * Change the account name of all checksums.
	 * @param oldAccount Old account name to be changed.
	 * @param newAccount New account name.
	 */
	public synchronized void renameAccount(String oldAccount, String newAccount) {
		if (oldAccount == null || newAccount == null || oldAccount.equals(newAccount)) {
			return;
		}
		for (Checksum ch: cache.getChecksums()) {
			if (ch.getAccount().equals(oldAccount)) {
				ch.setAccount(newAccount);
			}
		}
		cacheSave();
	}

	/**
	 * Update checksum in the cache.
	 * @param account Account name.
	 * @param oldFile File information to be updated.
	 * @param newFile New file information.
	 */
	public synchronized void update(String account, FileInfo oldFile, FileInfo newFile) {
		for (Checksum ch: cache.getChecksums()) {
			if (match(ch, account, oldFile)) {
				ch.setId(newFile.getId());
				ch.setPath(newFile.getPath());
				ch.setName(newFile.getName());
				ch.setModified(newFile.getModified());
				ch.setSize(newFile.getSize());
				ch.setChecksum(newFile.getChecksum());
				cacheSave();
				break;
			}
		}
	}

}
