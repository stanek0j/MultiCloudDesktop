package cz.zcu.kiv.multiclouddesktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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

	private final JPanel accountsPanel;
	private final JScrollPane accountScrollPane;
	private final DefaultListModel<AccountData> accountModel;
	private final JList<AccountData> accountList;
	private final AccountDataListCellRenderer accountRenderer;
	private final JPanel dataPanel;
	private final JScrollPane dataScrollPane;
	private final DefaultListModel<FileInfo> dataModel;
	private final JList<FileInfo> dataList;
	private final FileInfoListCellRenderer dataRenderer;
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
	private final JMenuItem mntmCut;
	private final JMenuItem mntmCopy;
	private final JMenuItem mntmPaste;
	private final JMenuItem mntmDelete;

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

	private String currentAccount;
	private final Stack<FileInfo> parentFolders;
	private FileInfo currentFolder;
	private FileInfo transferFile;

	public MultiCloudDesktop() {
		loader = MultiCloudDesktop.class.getClassLoader();

		setMinimumSize(new Dimension(720, 480));
		setPreferredSize(new Dimension(720, 480));
		setSize(new Dimension(720, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setTitle(APP_NAME);

		Icon icnAbort = null;
		Icon icnFolder = null;
		Icon icnFile = null;
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

		accountsPanel = new JPanel();
		getContentPane().add(accountsPanel, BorderLayout.WEST);
		accountsPanel.setLayout(new BorderLayout(0, 0));

		accountScrollPane = new JScrollPane();
		accountScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		accountsPanel.add(accountScrollPane);

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
				if (account != null) {
					if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
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

		dataModel = new DefaultListModel<>();
		dataList = new JList<>();
		dataList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
			}
		});
		dataList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				FileInfo file = dataList.getSelectedValue();
				if (file != null && file.getFileType() == FileType.FOLDER) {
					if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
						worker.listFolder(currentAccount, file, false, false);
					}
				}
			}
		});
		dataList.setFixedCellWidth(80);
		dataList.setFixedCellHeight(96);
		dataRenderer = new FileInfoListCellRenderer(icnFolder, icnFile);
		dataList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
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

		parentFolders = new Stack<>();
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

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		mnFile.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnFile);

		mntmPreferences = new JMenuItem("Preferences");
		mntmPreferences.setPreferredSize(new Dimension(127, 22));
		mnFile.add(mntmPreferences);

		mntmExit = new JMenuItem("Exit");
		mntmExit.setPreferredSize(new Dimension(127, 22));
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
		mntmAddAccount.setPreferredSize(new Dimension(127, 22));
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
		mntmAuthorize.setPreferredSize(new Dimension(127, 22));
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
		mntmInformation.setPreferredSize(new Dimension(127, 22));
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
		mntmQuota.setPreferredSize(new Dimension(127, 22));
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
		mntmRenameAccount.setPreferredSize(new Dimension(127, 22));
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
		mntmRemoveAccount.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmRemoveAccount);

		mnOperation = new JMenu("Operation");
		mnOperation.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnOperation);

		mntmRefresh = new JMenuItem("Refresh");
		mntmRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account == null) {
					JOptionPane.showMessageDialog(window, "No account selected.", "Refresh", JOptionPane.ERROR_MESSAGE);
				} else {
					worker.refresh(account.getName(), currentFolder);
				}
			}
		});
		mntmRefresh.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmRefresh);

		mntmUpload = new JMenuItem("Upload");
		mntmUpload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JTextField firstName = new JTextField();
				JTextField lastName = new JTextField();
				JPasswordField password = new JPasswordField();
				final JComponent[] inputs = new JComponent[] {
						new JLabel("First"),
						firstName,
						new JLabel("Last"),
						lastName,
						new JLabel("Password"),
						password
				};
				JOptionPane.showMessageDialog(window, inputs, "My custom dialog", JOptionPane.PLAIN_MESSAGE);
				System.out.println("You entered " +
						firstName.getText() + ", " +
						lastName.getText() + ", " +
						String.copyValueOf(password.getPassword()));
			}
		});
		mntmUpload.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmUpload);

		mntmDownload = new JMenuItem("Download");
		mntmDownload.setPreferredSize(new Dimension(127, 22));
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
		mntmMultiDownload.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmMultiDownload);

		mntmCreateFolder = new JMenuItem("Create folder");
		mntmCreateFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				worker.listFolder("GD", null, false, false);
			}
		});
		mntmCreateFolder.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCreateFolder);

		mntmRename = new JMenuItem("Rename");
		mntmRename.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmRename);

		mntmCut = new JMenuItem("Cut");
		mntmCut.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCut);

		mntmCopy = new JMenuItem("Copy");
		mntmCopy.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mntmPaste.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmPaste);

		mntmDelete = new JMenuItem("Delete");
		mntmDelete.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmDelete);
	}

	public String getCurrentAccount() {
		return currentAccount;
	}

	public FileInfo getCurrentFolder() {
		return currentFolder;
	}

	public FileInfo peekParentFolder() {
		System.out.println("stack: " + parentFolders.size());
		FileInfo parent = null;
		try {
			parent = parentFolders.peek();
			if (parent != null) {
				System.out.println("peek " + parent.getName());
			} else {
				System.out.println("peek null");
			}
		} catch (EmptyStackException e) {
		}
		return parent;
	}

	public void popParentFolder() {
		try {
			FileInfo parent = parentFolders.pop();
			System.out.println("popped " + parent.getPath());
		} catch (EmptyStackException e) {
		}
	}

	public void pushParentFolder(FileInfo parent) {
		if (parent != null) {
			System.out.println("pushed in " + parent.getPath());
			parent.setName("..");
			parentFolders.push(parent);
		}
	}

	public void setCurrentAccount(String currentAccount) {
		this.currentAccount = currentAccount;
	}

	public void setCurrentFolder(FileInfo currentFolder) {
		this.currentFolder = currentFolder;
	}

}
