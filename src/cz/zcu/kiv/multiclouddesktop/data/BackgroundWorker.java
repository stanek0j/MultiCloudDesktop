package cz.zcu.kiv.multiclouddesktop.data;

import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.ParentInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

public class BackgroundWorker extends Thread {

	private final MultiCloudDesktop parent;
	private final MultiCloud cloud;
	private final JButton btnAbort;
	private final JProgressBar progressBar;

	private final BackgroundCallback<AccountInfo> infoCallback;
	private final BackgroundCallback<AccountQuota> quotaCallback;
	private final BackgroundCallback<FileInfo> listCallback;
	private final BackgroundCallback<Boolean> messageCallback;

	private BackgroundTask task;
	private ProgressDialog dialog;
	private SearchCallback searchCallback;
	private BrowseCallback browseCallback;
	private String account;
	private String[] accounts;
	private FileInfo src;
	private FileInfo[] srcs;
	private FileInfo dst;
	private FileInfo[] dsts;
	private String dstName;
	private File localFile;
	private boolean overwrite;
	private boolean showDeleted;
	private boolean showShared;
	/** If the thread should terminate. */
	private boolean terminate;
	private boolean aborted;

	public BackgroundWorker(
			MultiCloudDesktop parent,
			MultiCloud cloud,
			JButton abort,
			JProgressBar progress,
			BackgroundCallback<AccountInfo> infoCallback,
			BackgroundCallback<AccountQuota> quotaCallback,
			BackgroundCallback<FileInfo> listCallback,
			BackgroundCallback<Boolean> messageCallback
			) {
		this.parent = parent;
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
			dialog = null;
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

	public boolean browse(String accountName, FileInfo folder, BrowseCallback callback) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.BROWSE;
				browseCallback = callback;
				account = accountName;
				src = folder;
				ready = true;
				notifyAll();
			}
		}
		return ready;
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

	public boolean createFolder(String accountName, String name, FileInfo folder) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.CREATE_FOLDER;
				account = accountName;
				dst = folder;
				dstName = name;
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

	public boolean download(String accountName, FileInfo file, File target, boolean overwriteTarget, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.DOWNLOAD;
				dialog = progressDialog;
				account = accountName;
				src = file;
				localFile = target;
				overwrite = overwriteTarget;
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

	public boolean listFolder(String accountName, FileInfo folder) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.LIST_FOLDER;
				account = accountName;
				src = folder;
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

	public boolean multiDownload(String[] accountNames, FileInfo[] files, File target, boolean overwriteTarget, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.MULTI_DOWNLOAD;
				dialog = progressDialog;
				accounts = accountNames;
				srcs = files;
				localFile = target;
				overwrite = overwriteTarget;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public boolean multiUpload(String accountName, String[] accountNames, File file, FileInfo folder, FileInfo[] folders, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.MULTI_UPLOAD;
				dialog = progressDialog;
				account= accountName;
				accounts = accountNames;
				localFile = file;
				dst = folder;
				dsts = folders;
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

	public boolean rename(String accountName, String name, FileInfo file, FileInfo folder) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.RENAME;
				account = accountName;
				src = file;
				dst = folder;
				dstName = name;
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
			if (task != BackgroundTask.NONE && dialog == null) {
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
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					parent.setCurrentAccount(account);
					parent.setCurrentFolder(task, list);
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
			case BROWSE:
				FileInfo browse = null;
				try {
					browse = cloud.listFolder(account, src, showDeleted, showShared);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Folder listed.", false);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				if (browseCallback != null) {
					browseCallback.onFinish(task, account, browse);
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
			case UPLOAD:
				try {
					long start = System.currentTimeMillis();
					cloud.uploadFile(account, dst, localFile.getName(), true, localFile);
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (dialog != null) {
						synchronized (this) {
							dialog = null;
						}
					}
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Upload finished in " + String.format("%.2f", time) + " seconds.", false);
					}
					beginOperation();
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
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
			case MULTI_UPLOAD:
				try {
					for (int i = 0; i < accounts.length; i++) {
						if (dsts[i] != null) {
							cloud.addUploadDestination(accounts[i], dsts[i], localFile.getName());
						}
					}
					long start = System.currentTimeMillis();
					cloud.uploadMultiFile(true, localFile);
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (dialog != null) {
						synchronized (this) {
							dialog = null;
						}
					}
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Upload finished in " + String.format("%.2f", time) + " seconds.", false);
					}
					beginOperation();
					for (int i = 0; i < accounts.length; i++) {
						AccountQuota quota = cloud.accountQuota(accounts[i]);
						if (quotaCallback != null) {
							quotaCallback.onFinish(task, accounts[i], quota);
						}
					}
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case DOWNLOAD:
				try {
					long start = System.currentTimeMillis();
					cloud.downloadFile(account, src, localFile, overwrite);
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Download finished in " + String.format("%.2f", time) + " seconds.", false);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				break;
			case MULTI_DOWNLOAD:
				try {
					for (int i = 0; i < accounts.length; i++) {
						if (srcs[i] != null) {
							cloud.addDownloadSource(accounts[i], srcs[i]);
						}
					}
					long start = System.currentTimeMillis();
					cloud.downloadMultiFile(localFile, overwrite);
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Download finished in " + String.format("%.2f", time) + " seconds.", false);
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
					parent.setCurrentAccount(account);
					parent.setCurrentFolder(task, list);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Folder listed.", false);
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
			case CREATE_FOLDER:
				try {
					cloud.createFolder(account, dstName, dst);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Folder created.", false);
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
			case RENAME:
				try {
					cloud.rename(account, src, dstName);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Folder created.", false);
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
			case SEARCH:
				List<FileInfo> result = null;
				try {
					result = cloud.search(account, dstName, showDeleted);
					if (result != null) {
						for (FileInfo f: result) {
							/* find the path of the file */
							if (f.getPath() == null) {
								StringBuilder sb = new StringBuilder("/" + f.getName());
								ParentInfo parent = f.getParents().get(0);
								String currentId = f.getId();
								do {
									if (parent.getId().equals(currentId)) {
										break;
									}
									FileInfo fromParent = new FileInfo();
									fromParent.setId(parent.getId());
									fromParent.setPath(parent.getPath());
									fromParent.setFileType(FileType.FOLDER);
									FileInfo meta = cloud.metadata(account, fromParent);
									if (meta.getParents().isEmpty()) {
										break;
									} else {
										currentId = parent.getId();
										parent = meta.getParents().get(0);
										sb.insert(0, "/" + meta.getName());
									}
								} while (true);
								f.setPath(sb.toString());
							}
						}
					}
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Search finished.", false);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				if (searchCallback != null) {
					searchCallback.onFinish(task, account, result);
				}
				break;
			case NONE:
			default:
				break;
			}
			if (task != BackgroundTask.NONE && dialog == null) {
				finishOperation();
			}
			synchronized (this) {
				task = BackgroundTask.NONE;
				dialog = null;
				aborted = false;
				System.out.println("worker done");
			}
		}
	}

	public boolean search(String accountName, String query, SearchCallback callback) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.SEARCH;
				searchCallback = callback;
				account = accountName;
				dstName = query;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	public void setShowDeleted(boolean showDeleted) {
		this.showDeleted = showDeleted;
	}

	public void setShowShared(boolean showShared) {
		this.showShared = showShared;
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

	public boolean upload(String accountName, File file, FileInfo folder, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.UPLOAD;
				dialog = progressDialog;
				account = accountName;
				localFile = file;
				dst = folder;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

}
