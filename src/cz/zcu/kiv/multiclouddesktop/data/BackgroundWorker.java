package cz.zcu.kiv.multiclouddesktop.data;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;

public class BackgroundWorker extends Thread {

	private final MultiCloud cloud;
	private final JButton btnAbort;
	private final JProgressBar progressBar;

	private final BackgroundCallback<AccountInfo> infoCallback;
	private final BackgroundCallback<Pair<String, AccountQuota>> quotaCallback;
	private final BackgroundCallback<FileInfo> listCallback;
	private final BackgroundCallback<Pair<Boolean, String>> messageCallback;

	private BackgroundTask task;
	private String account;
	private FileInfo src;
	private FileInfo dst;
	private String dstName;
	private boolean showDeleted;
	private boolean showShared;
	/** If the thread should terminate. */
	private boolean terminate;

	public BackgroundWorker(
			MultiCloud cloud,
			JButton abort,
			JProgressBar progress,
			BackgroundCallback<AccountInfo> infoCallback,
			BackgroundCallback<Pair<String, AccountQuota>> quotaCallback,
			BackgroundCallback<FileInfo> listCallback,
			BackgroundCallback<Pair<Boolean, String>> messageCallback
			) {
		this.cloud = cloud;
		this.btnAbort = abort;
		this.progressBar = progress;
		this.infoCallback = infoCallback;
		this.quotaCallback = quotaCallback;
		this.listCallback = listCallback;
		this.messageCallback = messageCallback;
		this.task = BackgroundTask.NONE;
	}

	public synchronized void abort() {
		if (task != BackgroundTask.NONE) {
			cloud.abortOperation();
		}
	}

	public boolean accountInfo(String accountName) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.INFO;
				account = accountName;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public boolean accountQuota(String accountName) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.QUOTA;
				account = accountName;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	private synchronized void beginOperation() {
		progressBar.setIndeterminate(true);
		btnAbort.setEnabled(true);
	}

	private synchronized void finishOperation() {
		progressBar.setIndeterminate(false);
		btnAbort.setEnabled(false);
	}

	public boolean listFolder(String accountName, FileInfo folder, boolean showDeleted, boolean showShared) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.LIST_FOLDER;
				account = accountName;
				src = folder;
				this.showDeleted = showDeleted;
				this.showShared = showShared;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public boolean refresh(String accountName) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.REFRESH;
				account = accountName;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (!shouldTerminate()) {
			synchronized (this) {
				if (task == BackgroundTask.NONE) {
					try {
						System.out.println("worker waiting");
						wait();
					} catch (InterruptedException e) {
						break;
					}
				}
			}
			System.out.println("worker working on " + task);
			if (task != BackgroundTask.NONE) {
				beginOperation();
			}
			switch (task) {
			case REFRESH:
				try {
					AccountQuota quota = cloud.accountQuota(account);
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, new Pair<String, AccountQuota>(account, quota));
					}
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					if (listCallback != null) {
						listCallback.onFinish(task, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, new Pair<Boolean, String>(true, e.getMessage()));
					}
				}
				break;
			case INFO:
				try {
					AccountInfo info = cloud.accountInfo(account);
					if (messageCallback != null) {
						messageCallback.onFinish(task, new Pair<Boolean, String>(false, "Account information obtained."));
					}
					if (infoCallback != null) {
						infoCallback.onFinish(task, info);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, new Pair<Boolean, String>(true, e.getMessage()));
					}
				}
				break;
			case QUOTA:
				try {
					AccountQuota quota = cloud.accountQuota(account);
					if (messageCallback != null) {
						messageCallback.onFinish(task, new Pair<Boolean, String>(false, "Account quota obtained."));
					}
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, new Pair<String, AccountQuota>(account, quota));
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, new Pair<Boolean, String>(true, e.getMessage()));
					}
				}
				break;
			case LIST_FOLDER:
				try {
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					if (listCallback != null) {
						listCallback.onFinish(task, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, new Pair<Boolean, String>(true, e.getMessage()));
					}
				}
				break;
			case CREATE_FOLDER:
				break;
			case RENAME:
				break;
			case MOVE:
				break;
			case COPY:
				break;
			case DELETE:
				break;
			case NONE:
			default:
				break;
			}
			if (task != BackgroundTask.NONE) {
				finishOperation();
			}
			synchronized (this) {
				task = BackgroundTask.NONE;
				System.out.println("worker done");
			}
		}
	}

	public synchronized boolean shouldTerminate() {
		return terminate;
	}

	public synchronized void terminate() {
		if (task != BackgroundTask.NONE) {
			cloud.abortOperation();
			task = BackgroundTask.NONE;
		}
		terminate = true;
		interrupt();
	}

}
