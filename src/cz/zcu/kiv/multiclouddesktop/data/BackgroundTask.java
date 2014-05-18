package cz.zcu.kiv.multiclouddesktop.data;

/**
 * cz.zcu.kiv.multiclouddesktop.data/BackgroundTask.java			<br /><br />
 *
 * List of all operations that can be executed on the background.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum BackgroundTask {

	NONE,
	LOAD,					// consists of the QUOTA and LIST_FOLDER operation for all cloud storages
	REFRESH,				// consists of the QUOTA and LIST_FOLDER operation
	INFO,
	QUOTA,
	UPLOAD,
	DOWNLOAD,
	MULTI_DOWNLOAD,
	LIST_FOLDER,
	CREATE_FOLDER,
	RENAME,
	MOVE,
	COPY,
	DELETE,
	SEARCH

}
