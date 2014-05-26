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
	/** Checksum cache. */
	private final ChecksumProvider cache;
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
	 * @param cache Checksum cache.
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
			ChecksumProvider cache,
			JButton abort,
			JProgressBar progress,
			BackgroundCallback<AccountInfo> infoCallback,
			BackgroundCallback<AccountQuota> quotaCallback,
			BackgroundCallback<FileInfo> listCallback,
			BackgroundCallback<Boolean> messageCallback
			) {
		this.parent = parent;
		this.cloud = cloud;
		this.cache = cache;
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
	 * Preparing task for computing checksum of a file.
	 * @param accountName Account name.
	 * @param file File to compute checksum of.
	 * @param target Local temporary file.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
	public boolean checksum(String accountName, FileInfo file, File target, int threads, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.CHECKSUM;
				dialog = progressDialog;
				account = accountName;
				accounts = new String[threads];
				src = file;
				srcs = new FileInfo[threads];
				localFile = target;
				overwrite = true;
				for (int i = 0; i < threads; i++) {
					accounts[i] = accountName;
					srcs[i] = file;
				}
				ready = true;
				notifyAll();
			}
		}
		return ready;
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
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					if (browse != null) {
						cache.provideChecksum(account, browse);
						cache.provideChecksum(account, browse.getContent());
					}
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
					String checksum = cache.computeChecksum(localFile);
					long start = System.currentTimeMillis();
					if (dst != null) {
						cloud.updateFile(account, src, dst, localFile.getName(), localFile);
					} else {
						cloud.uploadFile(account, src, localFile.getName(), overwrite, localFile);
					}
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Upload finished in " + String.format("%.2f", time) + " seconds.", false);
					}
					beginOperation();
					if (dialog != null) {
						synchronized (this) {
							dialog = null;
						}
					}
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					if (list != null) {
						for (FileInfo content: list.getContent()) {
							if (!src.getContent().contains(content)) {
								/* new file uploaded */
								if (content.getName().equals(localFile.getName()) && content.getSize() == localFile.length()) {
									content.setChecksum(checksum);
									cache.add(account, content);
									break;
								}
							} else {
								if (dst == null) {
									continue;
								}
								/* match ID if present */
								boolean condId = false;
								if ((content.getId() == null) && (dst.getId() == null)) {
									condId = true;
								} else if ((content.getId() != null) && (dst.getId() != null)) {
									condId = content.getId().equals(dst.getId());
								} else {
									continue;
								}
								/* match PATH if present */
								boolean condPath = false;
								if ((content.getPath() == null) && (dst.getPath() == null)) {
									condPath = true;
								} else if ((content.getPath() != null) && (dst.getPath() != null)) {
									condPath = content.getPath().equals(dst.getPath());
								} else {
									continue;
								}
								/* match NAME */
								boolean condName = (dst != null && content.getName().equals(dst.getName()));
								if (condId && condPath && condName) {
									content.setChecksum(checksum);
									cache.add(account, content);
									break;
								}
							}
						}
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					String checksum = cache.computeChecksum(localFile);
					for (int i = 0; i < accounts.length; i++) {
						if (srcs[i] != null) {
							if (dsts[i] != null) {
								cloud.addUpdateDestination(accounts[i], srcs[i], dsts[i], localFile.getName());
							} else {
								cloud.addUploadDestination(accounts[i], srcs[i], localFile.getName());
							}
						}
					}
					long start = System.currentTimeMillis();
					cloud.updateMultiFile(localFile);
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Upload finished in " + String.format("%.2f", time) + " seconds.", false);
					}
					beginOperation();
					if (dialog != null) {
						synchronized (this) {
							dialog = null;
						}
					}
					for (int i = 0; i < accounts.length; i++) {
						AccountQuota quota = cloud.accountQuota(accounts[i]);
						if (quotaCallback != null) {
							quotaCallback.onFinish(task, accounts[i], quota);
						}
					}
					FileInfo list = null;
					for (int i = 0; i < accounts.length; i++) {
						list = cloud.listFolder(accounts[i], srcs[i], showDeleted, showShared);
						for (FileInfo content: list.getContent()) {
							if (!srcs[i].getContent().contains(content)) {
								/* new file uploaded */
								if (content.getName().equals(localFile.getName()) && content.getSize() == localFile.length()) {
									content.setChecksum(checksum);
									cache.add(accounts[i], content);
									break;
								}
							} else {
								if (dsts == null) {
									continue;
								}
								/* match ID if present */
								boolean condId = false;
								if ((content.getId() == null) && (dsts[i].getId() == null)) {
									condId = true;
								} else if ((content.getId() != null) && (dsts[i].getId() != null)) {
									condId = content.getId().equals(dsts[i].getId());
								} else {
									continue;
								}
								/* match PATH if present */
								boolean condPath = false;
								if ((content.getPath() == null) && (dsts[i].getPath() == null)) {
									condPath = true;
								} else if ((content.getPath() != null) && (dsts[i].getPath() != null)) {
									condPath = content.getPath().equals(dsts[i].getPath());
								} else {
									continue;
								}
								/* match NAME */
								boolean condName = (dsts[i] != null && content.getName().equals(dsts[i].getName()));
								if (condId && condPath && condName) {
									content.setChecksum(checksum);
									cache.add(accounts[i], content);
									break;
								}
							}
						}
					}
					list = cloud.listFolder(account, src, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					FileInfo renamed = cloud.rename(account, src, dstName);
					renamed.setChecksum(src.getChecksum());
					cache.update(account, src, renamed);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					FileInfo moved = cloud.move(account, src, dst, dstName);
					moved.setChecksum(src.getChecksum());
					cache.update(account, src, moved);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					FileInfo copied = cloud.copy(account, src, dst, dstName);
					copied.setChecksum(src.getChecksum());
					cache.add(account, copied);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
					cache.remove(account, src);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
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
						cache.provideChecksum(account, result);
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
			case CHECKSUM:
				try {
					for (int i = 0; i < accounts.length; i++) {
						if (srcs[i] != null) {
							cloud.addDownloadSource(accounts[i], srcs[i]);
						}
					}
					cloud.downloadMultiFile(localFile, overwrite);
					if (dialog != null) {
						synchronized (this) {
							dialog = null;
						}
					}
					if (messageCallback != null) {
						messageCallback.onFinish(task, "File downloaded, computing checksum.", false);
					}
					beginOperation();
					String checksum = cache.computeChecksum(localFile);
					src.setChecksum(checksum);
					cache.add(account, src);
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Checksum computed.", false);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				localFile.delete();
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
