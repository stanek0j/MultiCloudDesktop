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

/**
 * cz.zcu.kiv.multiclouddesktop.data/BackgroundWorker.java			<br /><br />
 *
 * Background worker for using the multicloud library properly.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class BackgroundWorker extends Thread {

	/** Parent frame. */
	private final MultiCloudDesktop parent;
	/** Multicloud library. */
	private final MultiCloud cloud;
	/** Abort button. */
	private final JButton btnAbort;
	/** Progress bar. */
	private final JProgressBar progressBar;

	/** Account information callback. */
	private final BackgroundCallback<AccountInfo> infoCallback;
	/** Account quota callback. */
	private final BackgroundCallback<AccountQuota> quotaCallback;
	/** File list callback. */
	private final BackgroundCallback<FileInfo> listCallback;
	/** Message callback. */
	private final BackgroundCallback<Boolean> messageCallback;

	/** Task to be completed. */
	private BackgroundTask task;
	/** Progress dialog. */
	private ProgressDialog dialog;
	/** Search callback. */
	private SearchCallback searchCallback;
	/** Browsing callback. */
	private BrowseCallback browseCallback;
	/** User account. */
	private String account;
	/** User accounts. */
	private String[] accounts;
	/** Source file. */
	private FileInfo src;
	/** Source files. */
	private FileInfo[] srcs;
	/** Destination file. */
	private FileInfo dst;
	/** Destination files. */
	private FileInfo[] dsts;
	/** Destination file name. */
	private String dstName;
	/** Local file. */
	private File localFile;
	/** If the file should be overwritten. */
	private boolean overwrite;
	/** If deleted files should be displayed. */
	private boolean showDeleted;
	/** If shared files should be displayed. */
	private boolean showShared;
	/** If the thread should terminate. */
	private boolean terminate;
	/** If the task was aborted. */
	private boolean aborted;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param cloud Multicloud library.
	 * @param abort Abort button.
	 * @param progress Progress bar.
	 * @param infoCallback Account Information callback.
	 * @param quotaCallback Account quota callback.
	 * @param listCallback File list callback.
	 * @param messageCallback Message callback.
	 */
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

	/**
	 * Method for aborting current task.
	 */
	public synchronized void abort() {
		if (task != BackgroundTask.NONE) {
			cloud.abortOperation();
			dialog = null;
			aborted = true;
		}
	}

	/**
	 * Preparing task for getting account information.
	 * @param accountName Account name.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for getting account quota.
	 * @param accountName Account name.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Enabling user controls on the parent frame.
	 */
	private synchronized void beginOperation() {
		progressBar.setIndeterminate(true);
		btnAbort.setEnabled(true);
	}

	/**
	 * Preparing task for browsing folders.
	 * @param accountName Account name.
	 * @param folder Folder to browse.
	 * @param callback Browsing callback.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for copying files.
	 * @param accounName Account name.
	 * @param file File to be copied.
	 * @param destination Destination to copy to.
	 * @param destinationName New file name.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for creating new folder.
	 * @param accountName Account name.
	 * @param name Name of the new folder.
	 * @param folder Parent folder.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for deleting file or folder.
	 * @param accountName Account name.
	 * @param file File or folder to be deleted.
	 * @param folder Parent folder.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for downloading a file.
	 * @param accountName Account name.
	 * @param file File to be downloaded.
	 * @param target Local file to save the file to.
	 * @param overwriteTarget If the local file should be overwritten.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Disabling the user controls.
	 */
	private synchronized void finishOperation() {
		progressBar.setIndeterminate(false);
		btnAbort.setEnabled(false);
	}

	/**
	 * Preparing task for listing folder contents.
	 * @param accountName Account name.
	 * @param folder Folder to be listed.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for loading basic account data.
	 * @param accountNames Account names.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for moving a file.
	 * @param accounName Account name.
	 * @param file File to be moved.
	 * @param destination Destination to move to.
	 * @param destinationName New file name.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for downloading file from multiple sources.
	 * @param accountNames Account names.
	 * @param files Source files to be downloaded.
	 * @param target Local file to save the file to.
	 * @param overwriteTarget If the local file should be overwritten.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Preparing task for uploading file to multiple cloud storages.
	 * @param accountName Original account name.
	 * @param accountNames Account names.
	 * @param file File to be uploaded.
	 * @param folder Original destination folder.
	 * @param folders Destination folders.
	 * @param remote Destination files to be updated.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
	public boolean multiUpload(String accountName, String[] accountNames, File file, FileInfo folder, FileInfo[] folders, FileInfo[] remote, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.MULTI_UPLOAD;
				dialog = progressDialog;
				account = accountName;
				accounts = accountNames;
				localFile = file;
				src = folder;
				srcs = folders;
				dsts = remote;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	/**
	 * Preparing task for refreshing account data.
	 * @param accountName Account name.
	 * @param folder Current folder.
	 * @return If the task was initialized.
	 */
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
	 * Preparing task for  renaming a file or folder.
	 * @param accountName Account name.
	 * @param name New file or folder name.
	 * @param file File or folder to be renamed.
	 * @param folder Parent folder.
	 * @return If the task was initialized.
	 */
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
						wait();
					} catch (InterruptedException e) {
						break;
					}
				}
			}
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
					if (dst != null) {
						cloud.updateFile(account, src, dst, localFile.getName(), localFile);
					} else {
						cloud.uploadFile(account, src, localFile.getName(), overwrite, localFile);
					}
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
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					parent.setCurrentFolder(task, list);
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
					if (dialog != null) {
						synchronized (this) {
							dialog.closeDialog();
							dialog = null;
						}
					}
				}
				break;
			case MULTI_UPLOAD:
				try {
					for (int i = 0; i < accounts.length; i++) {
						if (srcs[i] != null) {
							if (dsts[i] != null) {
								System.out.println("adding for update: " + accounts[i]);
								cloud.addUpdateDestination(accounts[i], srcs[i], dsts[i], localFile.getName());
							} else {
								System.out.println("adding for upload: " + accounts[i]);
								cloud.addUploadDestination(accounts[i], srcs[i], localFile.getName());
							}
						}
					}
					long start = System.currentTimeMillis();
					cloud.updateMultiFile(localFile);
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
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					parent.setCurrentFolder(task, list);
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
					if (dialog != null) {
						synchronized (this) {
							dialog.closeDialog();
							dialog = null;
						}
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
					parent.setCurrentFolder(task, list);
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
					parent.setCurrentFolder(task, list);
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
					parent.setCurrentFolder(task, list);
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
					parent.setCurrentFolder(task, list);
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
					parent.setCurrentFolder(task, list);
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
			}
		}
	}

	/**
	 * Preparing task for searching for a file or folder.
	 * @param accountName Account name.
	 * @param query Search query.
	 * @param callback Search callback.
	 * @return If the task was initialized.
	 */
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

	/**
	 * Sets if deleted files should be displayed.
	 * @param showDeleted If deleted files should be displayed.
	 */
	public void setShowDeleted(boolean showDeleted) {
		this.showDeleted = showDeleted;
	}

	/**
	 * Sets if shared files should be displayed.
	 * @param showShared If shared files should be displayed.
	 */
	public void setShowShared(boolean showShared) {
		this.showShared = showShared;
	}

	/**
	 * Returns if the worker should terminate.
	 * @return If the worker should terminate.
	 */
	public synchronized boolean shouldTerminate() {
		return terminate;
	}

	/**
	 * Termination of the worker.
	 */
	public synchronized void terminate() {
		if (task != BackgroundTask.NONE) {
			cloud.abortOperation();
			task = BackgroundTask.NONE;
		}
		terminate = true;
		interrupt();
	}

	/**
	 * Preparing task for uploading a file to cloud storage.
	 * @param accountName Account name.
	 * @param file Local file to be uploaded.
	 * @param folder Destination folder to upload to.
	 * @param remote Destination file to be updated.
	 * @param update If the file contents should be updated.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
	public boolean upload(String accountName, File file, FileInfo folder, FileInfo remote, boolean update, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.UPLOAD;
				dialog = progressDialog;
				account = accountName;
				localFile = file;
				src = folder;
				dst = remote;
				overwrite = update;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

}
