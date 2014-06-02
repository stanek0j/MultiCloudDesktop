package cz.zcu.kiv.multiclouddesktop.callback;

import cz.zcu.kiv.multiclouddesktop.data.BackgroundTask;

/**
 * cz.zcu.kiv.multiclouddesktop.callback/BackgroundCallback.java			<br /><br />
 *
 * Interface for returning values from {@link cz.zcu.kiv.multiclouddesktop.data.BackgroundWorker} after the desired work is done.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 * @param <T> Return type of the operation.
 */
public interface BackgroundCallback<T> {

	/**
	 * Return the parameterized result after operation is done.
	 * @param task Task that has finished.
	 * @param accountName Name of the account related.
	 * @param result Result of the operation.
	 */
	void onFinish(BackgroundTask task, String accountName, T result);

}
