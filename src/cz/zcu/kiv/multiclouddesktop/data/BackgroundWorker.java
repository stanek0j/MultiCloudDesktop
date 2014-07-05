package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.json.ParentInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.callback.BackgroundCallback;
import cz.zcu.kiv.multiclouddesktop.callback.BrowseCallback;
import cz.zcu.kiv.multiclouddesktop.callback.SearchCallback;
import cz.zcu.kiv.multiclouddesktop.dialog.DialogProgressListener;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.data/BackgroundWorker.java			<br /><br />
 *
 * Background worker for using the MultiCloud library properly.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class BackgroundWorker extends Thread {

	/** Parent frame. */
	private final MultiCloudDesktop parent;
	/** MultiCloud library. */
	private final MultiCloud cloud;
	/** Checksum cache. */
	private final ChecksumProvider cache;
	/** JSON parser instance. */
	private final Json json;
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
	/** Local temporary file. */
	private File tmpFile;
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
	 * @param cloud MultiCloud library.
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
		this.json = Json.getInstance();
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
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				progressBar.setIndeterminate(true);
				btnAbort.setEnabled(true);
			}
		});
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
	 * Clears the synchronization data.
	 * @param node Synchronization data.
	 */
	private void clearLocalStructure(SyncData node) {
		if (node == null) {
			return;
		}
		node.setOrigChecksum(node.getChecksum());
		node.setChecksum(null);
		for (String key: node.getAccounts().keySet()) {
			node.getAccounts().put(key, null);
		}
		for (SyncData content: node.getNodes()) {
			clearLocalStructure(content);
		}
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
	 * Creates all folders along a path supplied.
	 * @param account Account name.
	 * @param structure Path to be created.
	 * @param root Root folder to start creating folders in.
	 * @return The folder at the end of the path.
	 * @throws InterruptedException If the process was interrupted.
	 */
	private FileInfo createFolderStructure(String account, List<SyncData> structure, FileInfo root) throws InterruptedException {
		if (account == null || root == null) {
			return null;
		}
		if (structure.isEmpty()) {
			return root;
		}
		List<SyncData> s = new ArrayList<>();
		s.addAll(structure);
		FileInfo destination = null;
		FileInfo list = null;
		FileInfo folder = root;
		do {
			try {
				list = cloud.listFolder(account, folder);
				if (list != null) {
					SyncData find = s.get(0);
					boolean found = false;
					for (FileInfo f: list.getContent()) {
						if (find.getName().equals(f.getName()) && f.getFileType() == FileType.FOLDER) {
							s.remove(0);
							list = f;
							found = true;
							break;
						}
					}
					if (!found) {
						cloud.createFolder(account, find.getName(), folder);
						list = cloud.listFolder(account, folder);
						if (list != null) {
							for (FileInfo f: list.getContent()) {
								if (find.getName().equals(f.getName()) && f.getFileType() == FileType.FOLDER) {
									s.remove(0);
									list = f;
									break;
								}
							}
						}
					}
				} else {
					break;
				}
				folder = list;
			} catch (MultiCloudException | OAuth2SettingsException e) {
				break;
			}
		} while (!s.isEmpty());
		if (s.isEmpty()) {
			destination = folder;
		}
		return destination;
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
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				progressBar.setIndeterminate(false);
				btnAbort.setEnabled(false);
			}
		});
	}

	/**
	 * Preparing task for computing checksum of a file.
	 * @param accountName Account name.
	 * @param file File to compute checksum of.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
	public boolean checksum(String accountName, FileInfo file, int threads, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.CHECKSUM;
				dialog = progressDialog;
				account = accountName;
				accounts = new String[threads];
				src = file;
				srcs = new FileInfo[threads];
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
	 * Compute missing checksums.
	 * @param node Synchronization data.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the process was interrupted.
	 */
	private void checksumStructure(SyncData node) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		if (node == null) {
			return;
		}
		for (Entry<String, FileInfo> remote: node.getAccounts().entrySet()) {
			FileInfo remoteFile = remote.getValue();
			if (remoteFile != null && remoteFile.getChecksum() == null) {
				try {
					for (int i = 0; i < parent.getPreferences().getThreadsPerAccount(); i++) {
						cloud.addDownloadSource(remote.getKey(), remoteFile);
					}
					cloud.downloadMultiFile(tmpFile, true);
					String checksum = cache.computeChecksum(tmpFile);
					remoteFile.setChecksum(checksum);
					cache.add(remote.getKey(), remoteFile);
				} catch (MultiCloudException | OAuth2SettingsException e) {
					if (aborted) {
						throw e;
					}
				}
			}
		}
		for (SyncData content: node.getNodes()) {
			checksumStructure(content);
		}
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
	 * Reads local file structure and computes checksums for files.
	 * @param file Local file.
	 * @param node Synchronization data.
	 */
	private void readLocalStructure(File file, SyncData node) {
		if (file == null || node == null) {
			return;
		}
		if (file.getName().equals(node.getName()) || file.equals(localFile)) {
			node.setLocalFile(file);
			if (node.getNodes().isEmpty() && file.isFile()) {
				node.setChecksum(cache.computeChecksum(file));
			} else {
				for (SyncData content: node.getNodes()) {
					for (File inner: file.listFiles()) {
						if (inner.getName().equals(content.getName())) {
							readLocalStructure(inner, content);
						}
					}
				}
			}
		}
	}

	/**
	 * Read remote checksum caches into local cache.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the process was interrupted.
	 */
	private void readRemoteCache() throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		ObjectMapper mapper = json.getMapper();
		for (String accountName: cache.getRemoteAccounts()) {
			if (cache.getRemote(accountName) != null) {
				FileInfo metadata = cloud.metadata(accountName, cache.getRemote(accountName));
				if (cache.getRemoteDate(accountName) == null || (metadata != null && metadata.getModified().after(cache.getRemoteDate(accountName)))) {
					cloud.downloadFile(accountName, metadata, tmpFile, true);
					try {
						ChecksumCache remote = mapper.readValue(tmpFile, ChecksumCache.class);
						cache.merge(remote);
					} catch (IOException e) {
						/* ignore file exceptions */
					}
				}
				if (metadata == null) {
					cache.putRemote(accountName, cache.getRemoteRoot(accountName), null);
				}
			}
		}
	}

	/**
	 * Read remote file structure.
	 * @param account Account name.
	 * @param file Remote file.
	 * @param node Synchronization data.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the process was interrupted.
	 */
	private void readRemoteStructure(String account, FileInfo file, SyncData node) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		if (file == null || node == null) {
			return;
		}
		if (file.getName().equals(node.getName()) || file == src) {
			if (node.getNodes().isEmpty() && file.getFileType() == FileType.FILE) {
				if (node.getAccounts().containsKey(account)) {
					node.getAccounts().put(account, file);
				}
			} else {
				FileInfo list = cloud.listFolder(account, file);
				cache.provideChecksum(account, list);
				cache.provideChecksum(account, list.getContent());
				for (SyncData content: node.getNodes()) {
					for (FileInfo inner: list.getContent()) {
						if (inner.getName().equals(content.getName())) {
							readRemoteStructure(account, inner, content);
						}
					}
				}
			}
		}
	}

	/**
	 * Preparing task for refreshing account data.
	 * @param accountName Account name.
	 * @param accountNames Account names.
	 * @param folder Current folder.
	 * @return If the task was initialized.
	 */
	public boolean refresh(String accountName, String[] accountNames, FileInfo folder) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.REFRESH;
				account = accountName;
				accounts = accountNames;
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
		try {
			tmpFile = File.createTempFile("multicloud", ".tmp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ObjectMapper mapper = json.getMapper();
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
				if (messageCallback != null) {
					messageCallback.onFinish(task, "Loading remote checksum caches.", false);
				}
				boolean skip = false;
				FileInfo[] root = null;
				/* get account identifiers */
				for (int i = 0; i < accounts.length; i++) {
					try {
						AccountInfo info = cloud.accountInfo(accounts[i]);
						cache.addAccount(accounts[i], info.getId());
					} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
						synchronized (this) {
							if (aborted) {
								if (messageCallback != null) {
									messageCallback.onFinish(task, "Loading aborted.", true);
								}
								skip = true;
								break;
							}
						}
					}
				}
				/* find and download checksum caches */
				if (!skip) {
					root = new FileInfo[accounts.length];
					for (int i = 0; i < accounts.length; i++) {
						try {
							root[i] = cloud.listFolder(accounts[i], null);
							if (root[i] != null) {
								for (FileInfo f: root[i].getContent()) {
									if (f.getName().equals(ChecksumProvider.CHECKSUM_FILE)) {
										if (cache.getRemoteDate(accounts[i]) == null || f.getModified().after(cache.getRemoteDate(accounts[i]))) {
											cloud.downloadFile(accounts[i], f, tmpFile, true);
											try {
												ChecksumCache remote = mapper.readValue(tmpFile, ChecksumCache.class);
												cache.merge(remote);
											} catch (IOException e) {
												/* ignore file exceptions */
											}
										}
										cache.putRemote(accounts[i], root[i], f);
										break;
									} else {
										cache.putRemote(accounts[i], root[i], null);
									}
								}
							}
						} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
							synchronized (this) {
								if (aborted) {
									if (messageCallback != null) {
										messageCallback.onFinish(task, "Loading aborted.", true);
									}
									skip = true;
									break;
								}
							}
						}
					}
					tmpFile.delete();
				}
				/* upload local checksum cache to remote destinations */
				if (!skip) {
					try {
						writeRemoteCache();
					} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
						synchronized (this) {
							if (aborted) {
								if (messageCallback != null) {
									messageCallback.onFinish(task, "Loading aborted.", true);
								}
								skip = true;
								break;
							}
						}
					}
				}
				/* load quota information */
				if (messageCallback != null) {
					messageCallback.onFinish(task, "Loading account informations.", false);
				}
				if (!skip) {
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
				}
				synchronized (this) {
					if (aborted) {
						if (messageCallback != null) {
							messageCallback.onFinish(task, "Loading aborted.", true);
						}
					} else {
						if (messageCallback != null) {
							messageCallback.onFinish(task, "Loading finished.", false);
						}
					}
				}
				break;
			case SYNCHRONIZE:
				DialogProgressListener listener = parent.getProgressListener();
				boolean failed = false;
				/* analyze local folder */
				listener.getProgressBar().setIndeterminate(true);
				listener.getLabel().setText("Analyzing local files...");
				listener.setReporting(false);
				dialog.preventClosing(true);
				SyncData data = parent.getPreferences().getSyncData();
				clearLocalStructure(data);
				readLocalStructure(localFile, data);
				parent.actionPreferences(parent.getPreferences());
				/* analyze remote folders */
				listener.getLabel().setText("Analyzing remote files...");
				Map<String, FileInfo> syncRoot = new HashMap<>();
				try {
					/* get remote accounts info */
					for (int i = 0; i < accounts.length; i++) {
						AccountInfo info = cloud.accountInfo(accounts[i]);
						cache.addAccount(accounts[i], info.getId());
					}
					/* get remote sync roots and caches */
					readRemoteCache();
					for (int i = 0; i < accounts.length; i++) {
						FileInfo list = cloud.listFolder(accounts[i], null);
						if (list != null) {
							FileInfo remoteCache = null;
							/* find sync folder and cache */
							for (FileInfo f: list.getContent()) {
								if (f.getName().equals(MultiCloudDesktop.SYNC_FOLDER)) {
									syncRoot.put(accounts[i], f);
								}
								if (f.getName().equals(ChecksumProvider.CHECKSUM_FILE)) {
									if (cache.getRemoteDate(accounts[i]) == null || f.getModified().after(cache.getRemoteDate(accounts[i]))) {
										cloud.downloadFile(accounts[i], f, tmpFile, true);
										try {
											ChecksumCache remote = mapper.readValue(tmpFile, ChecksumCache.class);
											cache.merge(remote);
										} catch (IOException e) {
											/* ignore file exceptions */
										}
									}
									remoteCache = f;
									cache.putRemote(accounts[i], list, f);
								}
							}
							if (remoteCache != null) {
								/* update existing checksum cache file */
								cloud.updateFile(accounts[i], list, remoteCache, ChecksumProvider.CHECKSUM_FILE, new File(ChecksumProvider.CHECKSUM_FILE));
								FileInfo metadata = cloud.metadata(accounts[i], remoteCache);
								if (metadata != null) {
									cache.putRemote(accounts[i], list, metadata);
								}
							} else {
								/* upload new checksum cache file */
								cloud.uploadFile(accounts[i], list, ChecksumProvider.CHECKSUM_FILE, true, new File(ChecksumProvider.CHECKSUM_FILE));
								FileInfo r = cloud.listFolder(accounts[i], list);
								if (r != null) {
									for (FileInfo f: r.getContent()) {
										if (f.getName().equals(ChecksumProvider.CHECKSUM_FILE)) {
											cache.putRemote(accounts[i], list, f);
											break;
										}
									}
								}
							}
						}
						/* create sync folder, if not found */
						if (syncRoot.get(accounts[i]) == null) {
							cloud.createFolder(accounts[i], MultiCloudDesktop.SYNC_FOLDER, list);
						}
						if (syncRoot.get(accounts[i]) == null) {
							list = cloud.listFolder(accounts[i], null);
							if (list != null) {
								for (FileInfo f: list.getContent()) {
									if (f.getName().equals(MultiCloudDesktop.SYNC_FOLDER)) {
										syncRoot.put(accounts[i], f);
										break;
									}
								}
							}
						}
					}
					/* traverse through remote folders */
					for (int i = 0; i < accounts.length; i++) {
						src = syncRoot.get(accounts[i]);
						readRemoteStructure(accounts[i], src, data);
					}
					/* compute missing checksums */
					checksumStructure(data);
					writeRemoteCache();
					parent.actionPreferences(parent.getPreferences());
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					failed = true;
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				/* synchronize files */
				listener.getProgressBar().setIndeterminate(false);
				listener.getLabel().setText("Synchronizing...");
				listener.setReporting(true);
				if (!failed) {
					try {
						synchronizeStructure(data, new ArrayList<SyncData>(), syncRoot);
						dialog.preventClosing(false);
						dialog.closeDialog();
						beginOperation();
						for (int i = 0; i < accounts.length; i++) {
							AccountQuota quota = cloud.accountQuota(accounts[i]);
							if (quotaCallback != null) {
								quotaCallback.onFinish(task, accounts[i], quota);
							}
						}
						writeRemoteCache();
						parent.actionPreferences(parent.getPreferences());
					} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
						if (messageCallback != null) {
							messageCallback.onFinish(task, e.getMessage(), true);
						}
					}
				}

				tmpFile.delete();
				synchronized (this) {
					if (dialog != null) {
						dialog.closeDialog();
						dialog = null;
					}
				}
				if (!failed && !aborted) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Synchronization finished.", false);
					}
				}
				break;
			case REFRESH:
				try {
					for (int i = 0; i < accounts.length; i++) {
						AccountInfo info = cloud.accountInfo(accounts[i]);
						cache.addAccount(accounts[i], info.getId());
					}
					AccountQuota quota = cloud.accountQuota(account);
					if (quotaCallback != null) {
						quotaCallback.onFinish(task, account, quota);
					}
					readRemoteCache();
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					if (list != null) {
						FileInfo remoteCache = null;
						for (FileInfo f: list.getContent()) {
							if (f.getName().equals(ChecksumProvider.CHECKSUM_FILE)) {
								if (cache.getRemoteDate(account) == null || f.getModified().after(cache.getRemoteDate(account))) {
									cloud.downloadFile(account, f, tmpFile, true);
									try {
										ChecksumCache remote = mapper.readValue(tmpFile, ChecksumCache.class);
										cache.merge(remote);
									} catch (IOException e) {
										/* ignore file exceptions */
									}
								}
								cache.putRemote(account, list, f);
								remoteCache = f;
								break;
							}
						}
						if (remoteCache != null) {
							/* update existing checksum cache file */
							cloud.updateFile(account, list, remoteCache, ChecksumProvider.CHECKSUM_FILE, new File(ChecksumProvider.CHECKSUM_FILE));
							FileInfo metadata = cloud.metadata(account, remoteCache);
							if (metadata != null) {
								cache.putRemote(account, list, metadata);
							}
						} else {
							/* upload new checksum cache file */
							cloud.uploadFile(account, list, ChecksumProvider.CHECKSUM_FILE, true, new File(ChecksumProvider.CHECKSUM_FILE));
							FileInfo r = cloud.listFolder(account, list);
							if (r != null) {
								for (FileInfo f: r.getContent()) {
									if (f.getName().equals(ChecksumProvider.CHECKSUM_FILE)) {
										cache.putRemote(account, list, f);
										break;
									}
								}
							}
						}
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
					readRemoteCache();
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
					synchronized (this) {
						if (dialog != null) {
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
					writeRemoteCache();
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
				synchronized (this) {
					if (dialog != null) {
						dialog.closeDialog();
						dialog = null;
					}
				}
				break;
			case MULTI_UPLOAD:
				try {
					String checksum = cache.computeChecksum(localFile);
					readRemoteCache();
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
					synchronized (this) {
						if (dialog != null) {
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
					writeRemoteCache();
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				synchronized (this) {
					if (dialog != null) {
						dialog.closeDialog();
						dialog = null;
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
				synchronized (this) {
					if (dialog != null) {
						dialog.closeDialog();
						dialog = null;
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
				synchronized (this) {
					if (dialog != null) {
						dialog.closeDialog();
						dialog = null;
					}
				}
				break;
			case LIST_FOLDER:
				try {
					readRemoteCache();
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
					readRemoteCache();
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
					readRemoteCache();
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
					writeRemoteCache();
					if (messageCallback != null) {
						messageCallback.onFinish(task, "File or folder renamed.", false);
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
					readRemoteCache();
					FileInfo moved = cloud.move(account, src, dst, dstName);
					if (moved != null) {
						moved.setChecksum(src.getChecksum());
						cache.update(account, src, moved);
					}
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
					parent.setCurrentFolder(task, list);
					if (moved != null) {
						writeRemoteCache();
					}
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
					readRemoteCache();
					FileInfo copied = cloud.copy(account, src, dst, dstName);
					if (copied != null) {
						copied.setChecksum(src.getChecksum());
						cache.add(account, copied);
					}
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
					parent.setCurrentFolder(task, list);
					if (copied != null) {
						writeRemoteCache();
					}
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
			case TRANSFER:
				try {
					readRemoteCache();
					/* download file from original source */
					dialog.preventClosing(true);
					for (int i = 0; i < accounts.length; i++) {
						if (srcs[i] != null) {
							cloud.addDownloadSource(accounts[i], srcs[i]);
						}
					}
					long start = System.currentTimeMillis();
					cloud.downloadMultiFile(tmpFile, true);
					/* compute checksum locally */
					String checksum = cache.computeChecksum(tmpFile);
					/* upload file to remote destination */
					parent.getProgressListener().getComponents(); // reset dialog
					dialog.preventClosing(false);
					if (dst != null) {
						cloud.updateFile(account, src, dst, dstName, tmpFile);
					} else {
						cloud.uploadFile(account, src, dstName, true, tmpFile);
					}
					beginOperation();
					synchronized (this) {
						if (dialog != null) {
							dialog = null;
						}
					}
					/* delete source file, if the file should have been moved */
					if (overwrite) {
						cloud.delete(accounts[0], srcs[0]);
						cache.remove(accounts[0], srcs[0]);
					}
					double time = (System.currentTimeMillis() - start) / 1000.0;
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Transfer finished in " + String.format("%.2f", time) + " seconds.", false);
					}
					/* refresh account and data list */
					AccountQuota srcQuota = null;
					if (overwrite) {
						srcQuota = cloud.accountQuota(accounts[0]);
					}
					AccountQuota dstQuota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, src, showDeleted, showShared);
					if (list != null) {
						for (FileInfo content: list.getContent()) {
							if (!src.getContent().contains(content)) {
								/* new file uploaded */
								if (content.getName().equals(dstName) && content.getSize() == srcs[0].getSize()) {
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
					writeRemoteCache();
					if (quotaCallback != null) {
						if (overwrite) {
							quotaCallback.onFinish(task, accounts[0], srcQuota);
						}
						quotaCallback.onFinish(task, account, dstQuota);
					}
					if (listCallback != null) {
						listCallback.onFinish(task, account, list);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					synchronized (this) {
						if (dialog != null) {
							dialog.closeDialog();
							dialog = null;
						}
					}
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				/* clean up */
				tmpFile.delete();
				break;
			case DELETE:
				try {
					readRemoteCache();
					cloud.delete(account, src);
					cache.remove(account, src);
					AccountQuota quota = cloud.accountQuota(account);
					FileInfo list = cloud.listFolder(account, dst, showDeleted, showShared);
					if (list != null) {
						cache.provideChecksum(account, list);
						cache.provideChecksum(account, list.getContent());
					}
					parent.setCurrentFolder(task, list);
					writeRemoteCache();
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
								if (f.getParents().isEmpty()) {
									f.setPath("(?)/" + f.getName());
									continue;
								}
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
					readRemoteCache();
					for (int i = 0; i < accounts.length; i++) {
						if (srcs[i] != null) {
							cloud.addDownloadSource(accounts[i], srcs[i]);
						}
					}
					cloud.downloadMultiFile(tmpFile, true);
					synchronized (this) {
						if (dialog != null) {
							dialog = null;
						}
					}
					if (messageCallback != null) {
						messageCallback.onFinish(task, "File downloaded, computing checksum.", false);
					}
					beginOperation();
					String checksum = cache.computeChecksum(tmpFile);
					src.setChecksum(checksum);
					cache.add(account, src);
					writeRemoteCache();
					if (messageCallback != null) {
						messageCallback.onFinish(task, "Checksum computed.", false);
					}
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
					if (messageCallback != null) {
						messageCallback.onFinish(task, e.getMessage(), true);
					}
				}
				tmpFile.delete();
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
	 * Preparing task for synchronization of folders.
	 * @param accountNames Account names.
	 * @param syncFolder Local synchronization folder.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
	public boolean synchronize(String[] accountNames, File syncFolder, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.SYNCHRONIZE;
				dialog = progressDialog;
				accounts = accountNames;
				localFile = syncFolder;
				ready = true;
				notifyAll();
			}
		}
		return ready;
	}

	/**
	 * Recursive method for synchronizing content of remote folders.
	 * @param node Synchronization data.
	 * @param folderStructure Path to current location.
	 * @param syncRoot Root folder for synchronization.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the process was interrupted.
	 */
	private void synchronizeStructure(SyncData node, List<SyncData> folderStructure, Map<String, FileInfo> syncRoot) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		boolean exists = true;
		if (node.getChecksum() == null) {
			/* folder or no local file */
			exists = false;
		}
		if (exists) {
			Map<String, List<Entry<String, FileInfo>>> conflicted = new HashMap<>();
			Map<String, FileInfo> downloadList = new HashMap<>();
			Map<String, FileInfo> uploadList = new HashMap<>();
			boolean skip = false;
			for (Entry<String, FileInfo> remote: node.getAccounts().entrySet()) {
				FileInfo remoteFile = remote.getValue();
				if (remoteFile == null) {
					/* no remote file */
					uploadList.put(remote.getKey(), remoteFile);
				} else {
					if (remoteFile.getChecksum() != null) {
						if (!remoteFile.getChecksum().equals(node.getChecksum())) {
							/* remote file not matching local */
							if (!remoteFile.getChecksum().equals(node.getOrigChecksum())) {
								/* conflicted file */
								if (conflicted.containsKey(remoteFile.getChecksum())) {
									conflicted.get(remoteFile.getChecksum()).add(remote);
								} else {
									List<Entry<String, FileInfo>> list = new ArrayList<>();
									list.add(remote);
									conflicted.put(remoteFile.getChecksum(), list);
								}
							} else {
								/* old version */
								uploadList.put(remote.getKey(), remoteFile);
							}
						}
					} else {
						/* no remote checksum - skip file */
					}
				}
			}
			/* resolve conflict */
			if (!conflicted.isEmpty()) {
				int i = 0;
				JComponent[] components = new JComponent[conflicted.size() + 6];
				components[i++] = new JLabel("Inconsistency between local and remote file detected. Affected file:");
				StringBuilder sbl = new StringBuilder();
				sbl.append("[Sync folder]/");
				for (SyncData folder: folderStructure) {
					sbl.append(folder.getName() + "/");
				}
				sbl.append(node.getName());
				JLabel lblFile = new JLabel(sbl.toString());
				lblFile.setBorder(new EmptyBorder(4, 8, 16, 8));
				lblFile.setFont(new Font(lblFile.getFont().getFontName(), Font.BOLD, lblFile.getFont().getSize()));
				components[i++] = lblFile;
				components[i++] = new JLabel("Choose appropriate action:");
				ButtonGroup buttons = new ButtonGroup();
				JRadioButton uploadLocal = new JRadioButton("Upload local file.");
				buttons.add(uploadLocal);
				components[i++] = uploadLocal;
				String[] checksum = new String[conflicted.size()];
				JRadioButton[] downloadRemote = new JRadioButton[conflicted.size()];
				int j = i;
				for (Entry<String, List<Entry<String, FileInfo>>> conflict: conflicted.entrySet()) {
					StringBuilder sb = new StringBuilder();
					for (Entry<String, FileInfo> entry: conflict.getValue()) {
						if (sb.length() != 0) {
							sb.append(", ");
						}
						sb.append(entry.getKey());
					}
					JRadioButton download = new JRadioButton("Download file from: " + sb.toString() + ".");
					buttons.add(download);
					downloadRemote[i - j] = download;
					checksum[i - j] = conflict.getKey();
					components[i++] = download;
				}
				JRadioButton ignoreConflict = new JRadioButton("Ignore remote conflicted files.");
				buttons.add(ignoreConflict);
				components[i++] = ignoreConflict;
				JRadioButton skipFile = new JRadioButton("Skip synchronization of this file.");
				buttons.add(skipFile);
				components[i++] = skipFile;
				int option = JOptionPane.showConfirmDialog(parent, components, "Conflicted files", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				switch (option) {
				case JOptionPane.OK_OPTION:
					if (uploadLocal.isSelected()) {
						for (List<Entry<String, FileInfo>> list: conflicted.values()) {
							for (Entry<String, FileInfo> entry: list) {
								uploadList.put(entry.getKey(), entry.getValue());
							}
						}
					} else if (ignoreConflict.isSelected()) {
						/* do nothing */
					} else if (skipFile.isSelected()) {
						skip = true;
					} else {
						for (int k = 0; k < downloadRemote.length; k++) {
							if (downloadRemote[k].isSelected()) {
								node.setChecksum(checksum[k]);
								List<Entry<String, FileInfo>> list = conflicted.get(checksum[k]);
								for (Entry<String, FileInfo> entry: list) {
									downloadList.put(entry.getKey(), entry.getValue());
								}
								for (Entry<String, FileInfo> entry: node.getAccounts().entrySet()) {
									if (!downloadList.containsKey(entry.getKey())) {
										uploadList.put(entry.getKey(), entry.getValue());
									}
								}
								break;
							}
						}
					}
					break;
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					skip = true;
					break;
				}
			}
			if (!skip) {
				try {
					/* download remote file */
					if (!downloadList.isEmpty()) {
						for (int i = 0; i < parent.getPreferences().getThreadsPerAccount(); i++) {
							for (Entry<String, FileInfo> entry: downloadList.entrySet()) {
								cloud.addDownloadSource(entry.getKey(), entry.getValue());
							}
						}
						cloud.downloadMultiFile(node.getLocalFile(), true);
					}
					/* upload local file */
					Map<String, FileInfo> destinations = new HashMap<>();
					for (Entry<String, FileInfo> entry: uploadList.entrySet()) {
						FileInfo destination = createFolderStructure(entry.getKey(), folderStructure, syncRoot.get(entry.getKey()));
						destinations.put(entry.getKey(), destination);
						if (entry.getValue() != null) {
							cloud.addUpdateDestination(entry.getKey(), destination, entry.getValue(), node.getName());
						} else {
							cloud.addUploadDestination(entry.getKey(), destination, node.getName());
						}
					}
					if (!uploadList.isEmpty()) {
						cloud.updateMultiFile(node.getLocalFile());
					}
					/* update cache */
					FileInfo list = null;
					for (Entry<String, FileInfo> entry: destinations.entrySet()) {
						if (entry.getValue() != null) {
							list = cloud.listFolder(entry.getKey(), entry.getValue());
							for (FileInfo content: list.getContent()) {
								if (!entry.getValue().getContent().contains(content)) {
									/* new file uploaded */
									if (content.getName().equals(node.getLocalFile().getName()) && content.getSize() == node.getLocalFile().length()) {
										content.setChecksum(node.getChecksum());
										cache.add(entry.getKey(), content);
										break;
									}
								} else {
									FileInfo dst = uploadList.get(entry.getKey());
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
										content.setChecksum(node.getChecksum());
										cache.add(entry.getKey(), content);
										break;
									}
								}
							}
						}
					}
				} catch (MultiCloudException | OAuth2SettingsException e) {
					e.printStackTrace();
					if (aborted) {
						throw e;
					}
				}
			}
		}
		List<SyncData> structure = new ArrayList<>();
		structure.addAll(folderStructure);
		if (!node.isRoot()) {
			structure.add(node);
		}
		for (SyncData content: node.getNodes()) {
			synchronizeStructure(content, structure, syncRoot);
		}
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
	 * Preparing task for file transfer between accounts.
	 * @param srcAccount Source account name.
	 * @param source Source file to be transferred.
	 * @param threads Number of download threads.
	 * @param move If the source file should be moved.
	 * @param dstAccount Destination account name.
	 * @param folder Destination folder to upload to.
	 * @param destination Destination file to be updated.
	 * @param destinationName New file name.
	 * @param progressDialog Progress dialog.
	 * @return If the task was initialized.
	 */
	public boolean transfer(String srcAccount, FileInfo source, int threads, boolean move, String dstAccount, FileInfo folder, FileInfo destination, String destinationName, ProgressDialog progressDialog) {
		boolean ready = false;
		synchronized (this) {
			if (task == BackgroundTask.NONE) {
				task = BackgroundTask.TRANSFER;
				dialog = progressDialog;
				account = dstAccount;
				accounts = new String[threads];
				src = folder;
				srcs = new FileInfo[threads];
				dst = destination;
				dstName = destinationName;
				overwrite = move;
				for (int i = 0; i < threads; i++) {
					accounts[i] = srcAccount;
					srcs[i] = source;
				}
				ready = true;
				notifyAll();
			}
		}
		return ready;
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

	/**
	 * Writes local checksum cache to remote destinations.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the process was interrupted.
	 */
	private void writeRemoteCache() throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		for (String accountName: cache.getRemoteAccounts()) {
			FileInfo remoteRoot = cache.getRemoteRoot(accountName);
			FileInfo remote = cache.getRemote(accountName);
			if (remoteRoot != null) {
				if (remote != null) {
					/* update existing metadata */
					cloud.updateFile(accountName, remoteRoot, remote, ChecksumProvider.CHECKSUM_FILE, new File(ChecksumProvider.CHECKSUM_FILE));
					FileInfo metadata = cloud.metadata(accountName, remote);
					if (metadata != null) {
						cache.putRemote(accountName, remoteRoot, metadata);
					}
				} else {
					/* write new metadata */
					cloud.uploadFile(accountName, remoteRoot, ChecksumProvider.CHECKSUM_FILE, true, new File(ChecksumProvider.CHECKSUM_FILE));
					FileInfo r = cloud.listFolder(accountName, remoteRoot);
					if (r != null) {
						for (FileInfo f: r.getContent()) {
							if (f.getName().equals(ChecksumProvider.CHECKSUM_FILE)) {
								cache.putRemote(accountName, remoteRoot, f);
								break;
							}
						}
					}
				}
			}
		}
	}

}
