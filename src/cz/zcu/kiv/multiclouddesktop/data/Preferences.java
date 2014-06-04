package cz.zcu.kiv.multiclouddesktop.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multiclouddesktop.data/Preferences.java			<br /><br />
 *
 * Bean for holding preferences data.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class Preferences {

	/** Default display type. */
	public static final ListDisplayType DEFAULT_DISPLAY_TYPE = ListDisplayType.LINES;
	/** Default number of threads per account. */
	public static final int DEFAULT_THREADS_PER_ACCOUNT = 1;
	/** Default value for showing deleted files. */
	public static final boolean DEFAULT_SHOW_DELETED = false;
	/** Default value for showing shared files. */
	public static final boolean DEFAULT_SHOW_SHARED = false;
	/** Default value for showing error dialogs. */
	public static final boolean DEFAULT_SHOW_ERROR_DIALOG = true;
	/** Default value for showing checksum dialog. */
	public static final boolean DEFAULT_SHOW_CHECKSUM_DIALOG = true;
	/** Default value for hiding metadata file. */
	public static final boolean DEFAULT_HIDE_METADATA = true;
	/** Default value for uploading file without overwrite. */
	public static final boolean DEFAULT_UPLOAD_NO_OVERWRITE = false;
	/** Default local synchronization folder. */
	public static final String DEFAULT_SYNC_FOLDER = null;
	/** Default local folder for listing. */
	public static final String DEFAULT_FOLDER = ".";

	/** Display type of the items. */
	@JsonProperty("display_type")
	private ListDisplayType displayType;
	/** Number of download threads per account. */
	@JsonProperty("threads_per_account")
	private int threadsPerAccount;
	/** If the deleted files should be displayed. */
	@JsonProperty("show_deleted")
	private boolean showDeleted;
	/** If the shared files should be displayed. */
	@JsonProperty("show_shared")
	private boolean showShared;
	/** If the error dialogs should pop up. */
	@JsonProperty("show_error_dialog")
	private boolean showErrorDialog;
	/** If the checksum dialog should be displayed. */
	@JsonProperty("show_checksum_dialog")
	private boolean showChecksumDialog;
	/** If the metadata file should be hidden. */
	@JsonProperty("hide_metadata")
	private boolean hideMetadata;
	/** If file should be uploaded without overwrite. */
	@JsonProperty("upload_no_overwrite")
	private boolean uploadNoOverwrite;
	/** Local synchronization folder. */
	@JsonProperty("sync_folder")
	private String syncFolder;
	/** Local folder for listing. */
	private String folder;
	/** Synchronization data. */
	@JsonProperty("sync_data")
	private SyncData syncData;

	/**
	 * Empty ctor for filling in default values.
	 */
	public Preferences() {
		displayType = DEFAULT_DISPLAY_TYPE;
		threadsPerAccount = DEFAULT_THREADS_PER_ACCOUNT;
		showDeleted = DEFAULT_SHOW_DELETED;
		showShared = DEFAULT_SHOW_SHARED;
		showErrorDialog = DEFAULT_SHOW_ERROR_DIALOG;
		showChecksumDialog = DEFAULT_SHOW_CHECKSUM_DIALOG;
		hideMetadata = DEFAULT_HIDE_METADATA;
		uploadNoOverwrite = DEFAULT_UPLOAD_NO_OVERWRITE;
		syncFolder = DEFAULT_SYNC_FOLDER;
		folder = DEFAULT_FOLDER;
		syncData = null;
	}

	/**
	 * Returns the display type of the items.
	 * @return Display type of the items.
	 */
	public ListDisplayType getDisplayType() {
		return displayType;
	}

	/**
	 * Returns the local folder for listing.
	 * @return Local folder for listing.
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * Returns the synchronization data.
	 * @return Synchronization data.
	 */
	public SyncData getSyncData() {
		return syncData;
	}

	/**
	 * Returns local synchronization folder.
	 * @return Local synchronization folder.
	 */
	public String getSyncFolder() {
		return syncFolder;
	}

	/**
	 * Returns the number of download threads per account.
	 * @return Number of download threads per account.
	 */
	public int getThreadsPerAccount() {
		return threadsPerAccount;
	}

	/**
	 * Returns if the metadata file should be hidden.
	 * @return If the metadata file should be hidden.
	 */
	public boolean isHideMetadata() {
		return hideMetadata;
	}

	/**
	 * Returns if the deleted files should be displayed.
	 * @return If the deleted files should be displayed.
	 */
	public boolean isShowDeleted() {
		return showDeleted;
	}

	/**
	 * Returns if the error dialogs should pop up.
	 * @return If the error dialogs should pop up.
	 */
	public boolean isShowErrorDialog() {
		return showErrorDialog;
	}

	/**
	 * Returns if the checksum dialog should be displayed.
	 * @return If the checksum dialog should be displayed.
	 */
	public boolean isShowChecksumDialog() {
		return showChecksumDialog;
	}

	/**
	 * Returns if the shared files should be displayed.
	 * @return If the shared files should be displayed.
	 */
	public boolean isShowShared() {
		return showShared;
	}

	/**
	 * Returns if file should be uploaded without overwrite.
	 * @return If file should be uploaded without overwrite.
	 */
	public boolean isUploadNoOverwrite() {
		return uploadNoOverwrite;
	}

	/**
	 * Removes account from synchronization data.
	 * @param node Root of the tree structure.
	 * @param oldName Account to be removed.
	 */
	public void removeAccount(SyncData node, String oldName) {
		if (node == null || Utils.isNullOrEmpty(oldName)) {
			return;
		}
		if (node.getAccounts().containsKey(oldName)) {
			node.getAccounts().remove(oldName);
		}
		for (SyncData inner: node.getNodes()) {
			removeAccount(inner, oldName);
		}
	}

	/**
	 * Renames account in synchronization data.
	 * @param node Root of the tree structure.
	 * @param oldName Old name to rename from.
	 * @param newName New name to rename to.
	 */
	public void renameAccount(SyncData node, String oldName, String newName) {
		if (node == null || Utils.isNullOrEmpty(oldName) || Utils.isNullOrEmpty(newName)) {
			return;
		}
		if (node.getAccounts().containsKey(oldName)) {
			FileInfo value = node.getAccounts().get(oldName);
			node.getAccounts().remove(oldName);
			node.getAccounts().put(newName, value);
		}
		for (SyncData inner: node.getNodes()) {
			renameAccount(inner, oldName, newName);
		}
	}

	/**
	 * Sets the display type of the items.
	 * @param displayType Display type of the items.
	 */
	public void setDisplayType(ListDisplayType displayType) {
		this.displayType = displayType;
	}

	/**
	 * Sets the local folder for listing.
	 * @param folder Local folder for listing.
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Sets if the metadata file should be hidden.
	 * @param hideMetadata If the metadata file should be hidden.
	 */
	public void setHideMetadata(boolean hideMetadata) {
		this.hideMetadata = hideMetadata;
	}

	/**
	 * Sets if the deleted files should be displayed.
	 * @param showDeleted If the deleted files should be displayed.
	 */
	public void setShowDeleted(boolean showDeleted) {
		this.showDeleted = showDeleted;
	}

	/**
	 * Sets if the error dialogs should pop up.
	 * @param showErrorDialog If the error dialogs should pop up.
	 */
	public void setShowErrorDialog(boolean showErrorDialog) {
		this.showErrorDialog = showErrorDialog;
	}

	/**
	 * Sets if the checksum dialog should be displayed.
	 * @param showChecksumDialog If the checksum dialog should be displayed.
	 */
	public void setShowChecksumDialog(boolean showChecksumDialog) {
		this.showChecksumDialog = showChecksumDialog;
	}

	/**
	 * Sets if the shared files should be displayed.
	 * @param showShared If the shared files should be displayed.
	 */
	public void setShowShared(boolean showShared) {
		this.showShared = showShared;
	}

	/**
	 * Sets the synchronization data.
	 * @param syncData Synchronization data.
	 */
	public void setSyncData(SyncData syncData) {
		this.syncData = syncData;
	}

	/**
	 * Sets local synchronization folder.
	 * @param syncFolder Local synchronization folder.
	 */
	public void setSyncFolder(String syncFolder) {
		this.syncFolder = syncFolder;
	}

	/**
	 * Sets the number of download threads per account.
	 * @param threadsPerAccount Number of download threads per account.
	 */
	public void setThreadsPerAccount(int threadsPerAccount) {
		this.threadsPerAccount = threadsPerAccount;
	}

	/**
	 * Sets if file should be uploaded without overwrite.
	 * @param uploadNoOverwrite If file should be uploaded without overwrite.
	 */
	public void setUploadNoOverwrite(boolean uploadNoOverwrite) {
		this.uploadNoOverwrite = uploadNoOverwrite;
	}

}
