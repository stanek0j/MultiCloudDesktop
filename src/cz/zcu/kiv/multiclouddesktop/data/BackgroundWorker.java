package cz.zcu.kiv.multiclouddesktop.data;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

public class BackgroundWorker extends Thread {

	private final MultiCloud cloud;
	private final JButton btnAbort;
	private final JProgressBar progressBar;

	private final BackgroundCallback<AccountInfo> infoCallback;
	private final BackgroundCallback<AccountQuota> quotaCallback;
	private final BackgroundCallback<FileInfo> listCallback;
	private final BackgroundCallback<Boolean> messageCallback;

	private BackgroundTask task;
	private String account;
	private String[] accounts;
	private FileInfo src;
	private FileInfo dst;
	private String dstName;
	private boolean showDeleted;
	private boolean showShared;
	/** If the thread should terminate. */
	private boolean terminate;
	private boolean aborted;

	public BackgroundWorker(
			MultiCloud cloud,
			JButton abort,
			JProgressBar progress,
			BackgroundCallback<AccountInfo> infoCallback,
			BackgroundCallback<AccountQuota> quotaCallback,
			BackgroundCallback<FileInfo> listCallback,
			BackgroundCallback<Boolean> messageCallback
			) {
		this.cloud = cloud;
		this.btnAbort = abort;
		this.progressBar = progress;
		this.infoCallback = infoCallback;
		this.quotaCallback = quotaCallback;
		this.listCallback = listCallback;
		this.messageCallback = messageCallback;
		this.task = BackgroundTask.NONE;
		this.aborted = false;
	}

	public synchronized void abort() {
		if (task != BackgroundTask.NONE) {
			cloud.abortOperation();
			aborted = true;
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

	public boolean copy(String accounName, FileInfo file, FileInfo destination, String destinationName) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.COPY;
				account = accounName;
				src = file;
				dst = destination;
				dstName = destinationName;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public boolean delete(String accountName, FileInfo file, FileInfo folder) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.DELETE;
				account = accountName;
				src = file;
				dst = folder;
				ready = true;
				notifyAll();
			}
		}
		return ready;
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

	public boolean load(String[] accountNames) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.LOAD;
				accounts = accountNames;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public boolean move(String accounName, FileInfo file, FileInfo destination, String destinationName) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.MOVE;
				account = accounName;
				src = file;
				dst = destination;
				dstName = destinationName;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public boolean refresh(String accountName, FileInfo folder) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.REFRESH;
				account = accountName;
				src = folder;
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
			case LOAD:
				for (int i = 0; i < accounts.length; i++) {
					try {
						AccountQuota quota = cloud.accountQuota(accounts[i]);
						if (quotaCallback != null) {
							quotaCallback.onFinish(task, accounts[i], quota);
						}
					} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
						synchronized (this) {
							if (aborted) {
								if (messageCallback != null) {
									messageCallback.onFinish(task, "Loading aborted.", true);
								}
								break;
							}
						}
					}
				}
				break;
			case REFRESH:
				try {
					AccountQuota quota = cloud.accountQuota(account);
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, account, quota);
					}
					if (src != null) {
						System.out.println("folder: " + src.getName());
					}
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					MultiCloudDesktop.getWindow().setCurrentAccount(account);
					MultiCloudDesktop.getWindow().setCurrentFolder(list);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Account refreshed.", false);
					}
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case INFO:
				try {
					AccountInfo info = cloud.accountInfo(account);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Account information obtained.", false);
					}
					if (infoCallback != null) {
						infoCallback.onFinish(task, account, info);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case QUOTA:
				try {
					AccountQuota quota = cloud.accountQuota(account);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Account quota obtained.", false);
					}
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, account, quota);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case LIST_FOLDER:
				try {
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					MultiCloudDesktop.getWindow().setCurrentAccount(account);
					MultiCloudDesktop.getWindow().setCurrentFolder(list);
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case CREATE_FOLDER:
				break;
			case RENAME:
				break;
			case MOVE:
				try {
					cloud.move(account, src, dst, dstName);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "File moved.", false);
					}
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, account, quota);
					}
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case COPY:
				try {
					cloud.copy(account, src, dst, dstName);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "File copied.", false);
					}
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, account, quota);
					}
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case DELETE:
				try {
					cloud.delete(account, src);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Deleted.", false);
					}
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, account, quota);
					}
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
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
				aborted = false;
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
