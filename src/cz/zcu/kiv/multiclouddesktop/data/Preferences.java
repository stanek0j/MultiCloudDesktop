package cz.zcu.kiv.multiclouddesktop.data;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	/** Default value for uploading file without overwrite. */
	public static final boolean DEFAULT_UPLOAD_NO_OVERWRITE = false;
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
	/** If file should be uploaded without overwrite. */
	@JsonProperty("upload_no_overwrite")
	private boolean uploadNoOverwrite;
	/** Local folder for listing. */
	private String folder;

	/**
	 * Empty ctor for filling in default values.
	 */
	public Preferences() {
		displayType = DEFAULT_DISPLAY_TYPE;
		threadsPerAccount = DEFAULT_THREADS_PER_ACCOUNT;
		showDeleted = DEFAULT_SHOW_DELETED;
		showShared = DEFAULT_SHOW_SHARED;
		showErrorDialog = DEFAULT_SHOW_ERROR_DIALOG;
		uploadNoOverwrite = DEFAULT_UPLOAD_NO_OVERWRITE;
		folder = DEFAULT_FOLDER;
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
	 * Returns the number of download threads per account.
	 * @return Number of download threads per account.
	 */
	public int getThreadsPerAccount() {
		return threadsPerAccount;
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
	 * Sets if the shared files should be displayed.
	 * @param showShared If the shared files should be displayed.
	 */
	public void setShowShared(boolean showShared) {
		this.showShared = showShared;
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
