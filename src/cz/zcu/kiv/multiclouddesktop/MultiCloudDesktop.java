package cz.zcu.kiv.multiclouddesktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multiclouddesktop.action.CopyAction;
import cz.zcu.kiv.multiclouddesktop.action.CreateFolderAction;
import cz.zcu.kiv.multiclouddesktop.action.CutAction;
import cz.zcu.kiv.multiclouddesktop.action.DeleteAction;
import cz.zcu.kiv.multiclouddesktop.action.DownloadAction;
import cz.zcu.kiv.multiclouddesktop.action.MultiDownloadAction;
import cz.zcu.kiv.multiclouddesktop.action.PasteAction;
import cz.zcu.kiv.multiclouddesktop.action.PropertiesAction;
import cz.zcu.kiv.multiclouddesktop.action.RefreshAction;
import cz.zcu.kiv.multiclouddesktop.action.RenameAction;
import cz.zcu.kiv.multiclouddesktop.action.UploadAction;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.AccountDataListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.data.AccountInfoCallback;
import cz.zcu.kiv.multiclouddesktop.data.AccountQuotaCallback;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundWorker;
import cz.zcu.kiv.multiclouddesktop.data.FileInfoCallback;
import cz.zcu.kiv.multiclouddesktop.data.FileInfoListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.data.MessageCallback;
import cz.zcu.kiv.multiclouddesktop.dialog.AccountDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.AuthorizeDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.DialogProgressListener;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

public class MultiCloudDesktop extends JFrame {

	/** Serialization constant. */
	private static final long serialVersionUID = -1394767380063338580L;

	private static final String APP_NAME = "MultiCloudDesktop";

	private static MultiCloudDesktop window;

	public static MultiCloudDesktop getWindow() {
		return window;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				window = new MultiCloudDesktop();
				window.setVisible(true);
			}
		});
	}
	private final JPanel accountPanel;
	private final JScrollPane accountScrollPane;
	private final DefaultListModel<AccountData> accountModel;
	private final JList<AccountData> accountList;
	private final AccountDataListCellRenderer accountRenderer;
	private final JPanel dataPanel;
	private final JScrollPane dataScrollPane;
	private final DefaultListModel<FileInfo> dataModel;
	private final JList<FileInfo> dataList;
	private final FileInfoListCellRenderer dataRenderer;
	private final JPanel pathPanel;
	private final JLabel lblPath;
	private final JPanel statusPanel;
	private final JLabel lblStatus;
	private final JPanel progressPanel;
	private final JProgressBar progressBar;
	private final JButton btnAbort;

	private final JMenuBar menuBar;
	private final JMenu mnFile;
	private final JMenuItem mntmPreferences;
	private final JMenuItem mntmExit;
	private final JMenu mnAccount;
	private final JMenuItem mntmAddAccount;
	private final JMenuItem mntmAuthorize;
	private final JMenuItem mntmInformation;
	private final JMenuItem mntmQuota;
	private final JMenuItem mntmRenameAccount;
	private final JMenuItem mntmRemoveAccount;
	private final JMenu mnOperation;
	private final JMenuItem mntmRefresh;
	private final JMenuItem mntmUpload;
	private final JMenuItem mntmDownload;
	private final JMenuItem mntmMultiDownload;
	private final JMenuItem mntmCreateFolder;
	private final JMenuItem mntmRename;
	private final JMenuItem mntmDelete;
	private final JMenuItem mntmCut;
	private final JMenuItem mntmCopy;
	private final JMenuItem mntmPaste;
	private final JMenuItem mntmProperties;

	private final JPopupMenu popupMenu;
	private final JMenuItem mntmRefreshPop;
	private final JMenuItem mntmUploadPop;
	private final JMenuItem mntmDownloadPop;
	private final JMenuItem mntmMultiDownloadPop;
	private final JMenuItem mntmCreateFolderPop;
	private final JMenuItem mntmRenamePop;
	private final JMenuItem mntmDeletePop;
	private final JMenuItem mntmCutPop;
	private final JMenuItem mntmCopyPop;
	private final JMenuItem mntmPastePop;
	private final JMenuItem mntmPropertiesPop;

	private final Action actRefresh;
	private final Action actUpload;
	private final Action actDownload;
	private final Action actMultiDownload;
	private final Action actCreateFolder;
	private final Action actRename;
	private final Action actDelete;
	private final Action actCut;
	private final Action actCopy;
	private final Action actPaste;
	private final Action actProperties;

	private final MultiCloud cloud;
	private final AccountManager accountManager;
	private final CloudManager cloudManager;
	private final DialogProgressListener progressListener;
	private final ClassLoader loader;
	private final AccountInfoCallback infoCallback;
	private final AccountQuotaCallback quotaCallback;
	private final FileInfoCallback listCallback;
	private final MessageCallback messageCallback;

	private final BackgroundWorker worker;
	private final LinkedList<FileInfo> currentPath;
	private String currentAccount;
	private FileInfo currentFolder;
	private FileInfo transferFile;
	private final Object lock;

	public MultiCloudDesktop() {
		loader = MultiCloudDesktop.class.getClassLoader();
		lock = new Object();

		setMinimumSize(new Dimension(720, 480));
		setPreferredSize(new Dimension(720, 480));
		setSize(new Dimension(720, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setTitle(APP_NAME);

		ImageIcon icnAbort = null;
		ImageIcon icnFolder = null;
		ImageIcon icnFile = null;
		try {
			List<BufferedImage> images = new ArrayList<>();
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_16.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_24.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_32.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_36.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_48.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_64.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_72.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_96.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_128.png")));
			images.add(ImageIO.read(loader.getResourceAsStream("cloud_256.png")));
			setIconImages(images);
			icnAbort = new ImageIcon(ImageIO.read(loader.getResourceAsStream("abort.png")));
			icnFolder = new ImageIcon(ImageIO.read(loader.getResourceAsStream("folder.png")));
			icnFile = new ImageIcon(ImageIO.read(loader.getResourceAsStream("file.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		cloud = new MultiCloud();
		progressListener = new DialogProgressListener(200);
		cloud.setListener(progressListener);
		accountManager = cloud.getSettings().getAccountManager();
		cloudManager = cloud.getSettings().getCloudManager();
		cloud.validateAccounts();

		accountPanel = new JPanel();
		getContentPane().add(accountPanel, BorderLayout.WEST);
		accountPanel.setLayout(new BorderLayout(0, 0));

		accountScrollPane = new JScrollPane();
		accountScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		accountPanel.add(accountScrollPane);

		accountModel = new DefaultListModel<>();
		for (AccountSettings account: accountManager.getAllAccountSettings()) {
			AccountData data = new AccountData();
			data.setName(account.getAccountId());
			data.setCloud(account.getSettingsId());
			data.setAuthorized(account.isAuthorized());
			accountModel.addElement(data);
		}
		accountList = new JList<>();
		accountList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account != null) {
					switch (event.getKeyCode()) {
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_SPACE:
						if (account != null) {
							synchronized (lock) {
								currentFolder = null;
								currentPath.clear();
							}
							worker.listFolder(account.getName(), null, false, false);
						}
						break;
					case KeyEvent.VK_DELETE:
						break;
					default:
						break;
					}
				}
			}
		});
		accountList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
					if (account != null) {
						synchronized (lock) {
							currentFolder = null;
							currentPath.clear();
						}
						worker.listFolder(account.getName(), null, false, false);
					}
				}
			}
		});
		accountRenderer = new AccountDataListCellRenderer(accountList.getFont().deriveFont(Font.BOLD, 14.0f), accountList.getFont());
		accountList.setVisibleRowCount(-1);
		accountScrollPane.setViewportView(accountList);
		accountList.setCellRenderer(accountRenderer);
		accountList.setFixedCellWidth(200);
		accountList.setFixedCellHeight(68);
		accountList.setModel(accountModel);
		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		dataPanel = new JPanel();
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));

		dataScrollPane = new JScrollPane();
		dataPanel.add(dataScrollPane, BorderLayout.CENTER);

		pathPanel = new JPanel();
		dataPanel.add(pathPanel, BorderLayout.NORTH);
		pathPanel.setLayout(new BorderLayout(0, 0));

		lblPath = new JLabel();
		lblPath.setBorder(new EmptyBorder(4, 8, 4, 8));
		pathPanel.add(lblPath, BorderLayout.CENTER);

		dataModel = new DefaultListModel<>();
		dataList = new JList<>();
		dataList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		dataList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				FileInfo file = dataList.getSelectedValue();
				if (event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_ENTER) {
					if (file != null && file.getFileType() == FileType.FOLDER) {
						worker.listFolder(currentAccount, file, false, false);
					}
				}
			}
		});
		dataList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				FileInfo file = dataList.getSelectedValue();
				if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
					if (file != null && file.getFileType() == FileType.FOLDER) {
						worker.listFolder(currentAccount, file, false, false);
					}
				}
			}
			@Override
			public void mousePressed(MouseEvent event) {
				if (event.isPopupTrigger()) {
					showMenu(event);
				}
			}
			@Override
			public void mouseReleased(MouseEvent event) {
				if (event.isPopupTrigger()) {
					showMenu(event);
				}
			}
			private void showMenu(MouseEvent event) {
				dataList.setSelectedIndex(dataList.locationToIndex(event.getPoint()));
				popupMenu.show(event.getComponent(), event.getX(), event.getY());
			}
		});
		if (dataList.getLayoutOrientation() == JList.HORIZONTAL_WRAP) {
			dataList.setFixedCellWidth(80);
			dataList.setFixedCellHeight(96);
		}
		dataRenderer = new FileInfoListCellRenderer(icnFolder, icnFile);
		dataList.setVisibleRowCount(-1);
		dataScrollPane.setViewportView(dataList);
		dataList.setCellRenderer(dataRenderer);
		dataList.setModel(dataModel);
		dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		statusPanel = new JPanel();
		statusPanel.setMaximumSize(new Dimension(32767, 25));
		statusPanel.setBorder(null);
		statusPanel.setPreferredSize(new Dimension(10, 25));
		statusPanel.setMinimumSize(new Dimension(10, 25));
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BorderLayout(0, 0));

		lblStatus = new JLabel();
		lblStatus.setBorder(new EmptyBorder(4, 8, 4, 8));
		statusPanel.add(lblStatus, BorderLayout.CENTER);

		progressPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) progressPanel.getLayout();
		flowLayout.setVgap(2);
		statusPanel.add(progressPanel, BorderLayout.EAST);

		progressBar = new JProgressBar();
		progressBar.setMaximumSize(new Dimension(32767, 15));
		progressBar.setPreferredSize(new Dimension(186, 15));
		progressPanel.add(progressBar);

		btnAbort = new JButton(icnAbort);
		btnAbort.setEnabled(false);
		btnAbort.setPreferredSize(new Dimension(21, 21));
		btnAbort.setMargin(new Insets(2, 2, 2, 2));
		btnAbort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				worker.abort();
			}
		});
		progressPanel.add(btnAbort);

		currentPath = new LinkedList<>();
		infoCallback = new AccountInfoCallback();
		quotaCallback = new AccountQuotaCallback(accountList);
		listCallback = new FileInfoCallback(accountList, dataList);

		messageCallback = new MessageCallback(lblStatus);
		worker = new BackgroundWorker(cloud, btnAbort, progressBar, infoCallback, quotaCallback, listCallback, messageCallback);
		worker.start();
		String[] accounts = new String[accountModel.getSize()];
		for (int i = 0; i < accountModel.getSize(); i++) {
			accounts[i] = accountModel.get(i).getName();
		}
		worker.load(accounts);
		refreshCurrentPath();

		actRefresh = new RefreshAction(this, accountList);
		actUpload = new UploadAction();
		actDownload = new DownloadAction();
		actMultiDownload = new MultiDownloadAction();
		actCreateFolder = new CreateFolderAction();
		actRename = new RenameAction();
		actDelete = new DeleteAction();
		actCut = new CutAction();
		actCopy = new CopyAction();
		actPaste = new PasteAction();
		actProperties = new PropertiesAction(this, dataList);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		mnFile.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnFile);

		mntmPreferences = new JMenuItem("Preferences");
		mnFile.add(mntmPreferences);

		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				worker.terminate();
				dispose();
			}
		});
		mnFile.add(mntmExit);

		mnAccount = new JMenu("Account");
		mnAccount.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnAccount);

		mntmAddAccount = new JMenuItem("Add");
		mntmAddAccount.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountDialog dialog = new AccountDialog(window, "Add new account", accountManager, cloudManager, null);
				dialog.setVisible(true);
				switch (dialog.getOption()) {
				case JOptionPane.OK_OPTION:
					AccountData account = dialog.getAccountData();
					try {
						cloud.createAccount(account.getName(), account.getCloud());
						accountModel.addElement(account);
						messageCallback.displayMessage("Added new account.");
					} catch (MultiCloudException e) {
						messageCallback.displayError(e.getMessage());
					}
					break;
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					messageCallback.displayMessage("Adding account canceled.");
					break;
				}
			}
		});
		mnAccount.add(mntmAddAccount);

		mntmAuthorize = new JMenuItem("Authorize");
		mntmAuthorize.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				if (accountList.getSelectedIndex() > -1) {
					final AccountData account = accountList.getSelectedValue();
					final AuthorizeDialog dialog = new AuthorizeDialog(window, "Waiting for authorization", "Authorization");
					Thread t = new Thread() {
						/**
						 * {@inheritDoc}
						 */
						@Override
						public void run() {
							try {
								cloud.authorizeAccount(account.getName(), new BrowserCallback(dialog, window));
							} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
								dialog.setAlwaysOnTop(false);
								dialog.setFailed(true);
								messageCallback.displayError(e.getMessage());
							}
							dialog.closeDialog();
						};
					};
					t.start();
					dialog.setVisible(true);
					if (dialog.isAborted()) {
						cloud.abortAuthorization();
						messageCallback.displayMessage("Authorization aborted.");
					} else {
						try {
							t.join();
						} catch (InterruptedException e) {
							messageCallback.displayError(e.getMessage());
						}
						if (!dialog.isFailed()) {
							messageCallback.displayMessage("Account authorized.");
							account.setAuthorized(true);
							worker.refresh(account.getName(), null);
						}
					}
				} else {
					messageCallback.displayError("No account selected. Select one first.");
				}
			}
		});
		mnAccount.add(mntmAuthorize);

		mntmInformation = new JMenuItem("Information");
		mntmInformation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account == null) {
					messageCallback.displayError("No account selected for listing its basic information.");
				} else {
					worker.accountInfo(account.getName());
				}
			}
		});
		mnAccount.add(mntmInformation);

		mntmQuota = new JMenuItem("Quota");
		mntmQuota.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account == null) {
					messageCallback.displayError("No account selected for listing its quota.");
				} else {
					worker.accountQuota(account.getName());
				}
			}
		});
		mnAccount.add(mntmQuota);

		mntmRenameAccount = new JMenuItem("Rename");
		mntmRenameAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account == null) {
					messageCallback.displayError("No account selected to be renamed.");
				} else {
					AccountDialog dialog = new AccountDialog(window, "Rename account", accountManager, cloudManager, account);
					dialog.setVisible(true);
					switch (dialog.getOption()) {
					case JOptionPane.OK_OPTION:
						AccountData renamed = dialog.getAccountData();
						try {
							cloud.renameAccount(account.getName(), renamed.getName());
							accountModel.removeElement(account);
							accountModel.addElement(renamed);
							messageCallback.displayMessage("Account renamed.");
						} catch (MultiCloudException e) {
							messageCallback.displayError(e.getMessage());
						}
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
					default:
						messageCallback.displayMessage("Renaming account canceled.");
						break;
					}
				}
			}
		});
		mnAccount.add(mntmRenameAccount);

		mntmRemoveAccount = new JMenuItem("Remove");
		mntmRemoveAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account == null) {
					JOptionPane.showMessageDialog(window, "No account selected for removal.", "Remove account", JOptionPane.ERROR_MESSAGE);
				} else {
					int result = JOptionPane.showConfirmDialog(window, "Are you sure you want to remove this account?", "Remove account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					switch (result) {
					case JOptionPane.OK_OPTION:
						try {
							cloud.deleteAccount(account.getName());
							accountModel.removeElement(account);
							messageCallback.displayMessage("Account removed.");
						} catch (MultiCloudException e) {
							messageCallback.displayError(e.getMessage());
						}
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
					default:
						messageCallback.displayMessage("Account removal canceled.");
						break;
					}
				}
			}
		});
		mnAccount.add(mntmRemoveAccount);

		mnOperation = new JMenu("Operation");
		mnOperation.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnOperation);

		mntmRefresh = new JMenuItem();
		mntmRefresh.setAction(actRefresh);
		mnOperation.add(mntmRefresh);

		mnOperation.addSeparator();

		mntmUpload = new JMenuItem();
		mntmUpload.setAction(actUpload);
		mnOperation.add(mntmUpload);

		mntmDownload = new JMenuItem();
		mntmDownload.setAction(actDownload);
		mnOperation.add(mntmDownload);

		mntmMultiDownload = new JMenuItem("Multi download");
		mntmMultiDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ProgressDialog dialog = new ProgressDialog(window, progressListener.getComponents(), "Multi download");
				dialog.setVisible(true);
				System.out.println(dialog.isAborted());
			}
		});
		mnOperation.add(mntmMultiDownload);

		mnOperation.addSeparator();

		mntmCreateFolder = new JMenuItem();
		mntmCreateFolder.setAction(actCreateFolder);
		mnOperation.add(mntmCreateFolder);

		mntmRename = new JMenuItem();
		mntmRename.setAction(actRename);
		mnOperation.add(mntmRename);

		mntmDelete = new JMenuItem();
		mntmDelete.setAction(actDelete);
		mnOperation.add(mntmDelete);

		mnOperation.addSeparator();

		mntmCut = new JMenuItem();
		mntmCut.setAction(actCut);
		mnOperation.add(mntmCut);

		mntmCopy = new JMenuItem();
		mntmCopy.setAction(actCopy);
		mnOperation.add(mntmCopy);

		mntmPaste = new JMenuItem();
		mntmPaste.setAction(actPaste);
		mnOperation.add(mntmPaste);

		mnOperation.addSeparator();

		mntmProperties = new JMenuItem();
		mntmProperties.setAction(actProperties);
		mnOperation.add(mntmProperties);

		popupMenu = new JPopupMenu();

		mntmRefreshPop = new JMenuItem();
		mntmRefreshPop.setAction(actRefresh);
		popupMenu.add(mntmRefreshPop);

		popupMenu.addSeparator();

		mntmUploadPop = new JMenuItem();
		mntmUploadPop.setAction(actUpload);
		popupMenu.add(mntmUploadPop);

		mntmDownloadPop = new JMenuItem();
		mntmDownloadPop.setAction(actDownload);
		popupMenu.add(mntmDownloadPop);

		mntmMultiDownloadPop = new JMenuItem();
		mntmMultiDownloadPop.setAction(actMultiDownload);
		popupMenu.add(mntmMultiDownloadPop);

		popupMenu.addSeparator();

		mntmCreateFolderPop = new JMenuItem();
		mntmCreateFolderPop.setAction(actCreateFolder);
		popupMenu.add(mntmCreateFolderPop);

		mntmRenamePop = new JMenuItem();
		mntmRenamePop.setAction(actRename);
		popupMenu.add(mntmRenamePop);

		mntmDeletePop = new JMenuItem();
		mntmDeletePop.setAction(actDelete);
		popupMenu.add(mntmDeletePop);

		popupMenu.addSeparator();

		mntmCutPop = new JMenuItem();
		mntmCutPop.setAction(actCut);
		popupMenu.add(mntmCutPop);

		mntmCopyPop = new JMenuItem();
		mntmCopyPop.setAction(actCopy);
		popupMenu.add(mntmCopyPop);

		mntmPastePop = new JMenuItem();
		mntmPastePop.setAction(actPaste);
		popupMenu.add(mntmPastePop);

		popupMenu.addSeparator();

		mntmPropertiesPop = new JMenuItem();
		mntmPropertiesPop.setAction(actProperties);
		popupMenu.add(mntmPropertiesPop);

	}

	public boolean actionRefresh(String accountName) {
		return worker.refresh(accountName, currentFolder);
	}

	public String getCurrentAccount() {
		synchronized (lock) {
			return currentAccount;
		}
	}

	public FileInfo getCurrentFolder() {
		synchronized (lock) {
			return currentFolder;
		}
	}

	public FileInfo getParentFolder() {
		FileInfo parent = null;
		synchronized (lock) {
			if (currentPath.size() > 0) {
				FileInfo p = currentPath.get(currentPath.size() - 1);
				if (currentPath.size() > 1) {
					p = currentPath.get(currentPath.size() - 2);
				}
				parent = new FileInfo();
				parent.setContent(p.getContent());
				parent.setDeleted(p.isDeleted());
				parent.setFileType(p.getFileType());
				parent.setId(p.getId());
				parent.setIsRoot(p.isRoot());
				parent.setMimeType(p.getMimeType());
				parent.setName("..");
				parent.setParents(p.getParents());
				parent.setPath(p.getPath());
				parent.setShared(p.isShared());
				parent.setSize(p.getSize());
			}

		}
		return parent;
	}

	private void refreshCurrentPath() {
		StringBuilder sb = new StringBuilder();
		synchronized (lock) {
			for (FileInfo f: currentPath) {
				if (!Utils.isNullOrEmpty(f.getName())) {
					sb.append("/ ");
					sb.append(f.getName());
					sb.append(" ");
				}
			}
		}
		if (sb.length() == 0) {
			sb.append("/");
		}
		/* clip the string from the left side */
		FontMetrics metrics = lblPath.getFontMetrics(lblPath.getFont());
		Insets insets = lblPath.getBorder().getBorderInsets(lblPath);
		int width = lblPath.getSize().width - insets.left - insets.right;
		if (width > 0) {
			if (metrics.stringWidth(sb.toString()) > width) {
				sb.insert(0, "...");
			}
			while (metrics.stringWidth(sb.toString()) > width) {
				sb.deleteCharAt(3);
			}
		}
		lblPath.setText(sb.toString());
	}

	public void setCurrentAccount(String currentAccount) {
		synchronized (lock) {
			this.currentAccount = currentAccount;
		}
	}

	public void setCurrentFolder(FileInfo currentFolder) {
		synchronized (lock) {
			this.currentFolder = currentFolder;
			if (currentFolder.getName() != null && currentFolder.getName().equals("..")) {
				currentPath.removeLast();
			} else {
				currentPath.add(currentFolder);
			}
			refreshCurrentPath();
		}
	}
}
