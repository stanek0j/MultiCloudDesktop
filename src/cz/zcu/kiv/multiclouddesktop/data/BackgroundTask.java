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
	REFRESH,				// consists of the QUOTA and LIST_FOLDER operation
	INFO,
	QUOTA,
	LIST_FOLDER,
	CREATE_FOLDER,			// operation followed by the REFRESH
	RENAME,					// operation followed by the REFRESH
	MOVE,					// operation followed by the REFRESH
	COPY,					// operation followed by the REFRESH
	DELETE					// operation followed by the REFRESH

}