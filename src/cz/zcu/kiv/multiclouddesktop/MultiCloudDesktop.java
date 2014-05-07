package cz.zcu.kiv.multiclouddesktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
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
import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.AccountDataListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.dialog.AccountDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.AuthorizeDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.DialogProgressListener;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

public class MultiCloudDesktop extends JFrame {

	/** Serialization constant. */
	private static final long serialVersionUID = -1394767380063338580L;

	private static final String APP_NAME = "MultiCloudDesktop";

	private static MultiCloudDesktop window;

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
	private final JPanel contentPanel;
	private final JScrollPane dataScrollPane;
	private final JList<FileInfo> dataList;
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
	private final JMenuItem mntmUpload;
	private final JMenuItem mntmDownload;
	private final JMenuItem mntmMultiDownload;
	private final JMenuItem mntmCreateFolder;
	private final JMenuItem mntmRename;
	private final JMenuItem mntmMove;
	private final JMenuItem mntmCopy;
	private final JMenuItem mntmDelete;

	private final MultiCloud cloud;
	private final AccountManager accountManager;
	private final CloudManager cloudManager;
	private final DialogProgressListener progressListener;
	private final ClassLoader loader;

	public MultiCloudDesktop() {
		loader = MultiCloudDesktop.class.getClassLoader();

		setMinimumSize(new Dimension(720, 480));
		setPreferredSize(new Dimension(720, 480));
		setSize(new Dimension(720, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setTitle(APP_NAME);

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
		accountRenderer = new AccountDataListCellRenderer(accountList.getFont().deriveFont(Font.BOLD, 14.0f), accountList.getFont());
		accountList.setVisibleRowCount(-1);
		accountScrollPane.setViewportView(accountList);
		accountList.setCellRenderer(accountRenderer);
		accountList.setFixedCellWidth(200);
		accountList.setFixedCellHeight(68);
		accountList.setModel(accountModel);
		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		dataScrollPane = new JScrollPane();
		contentPanel.add(dataScrollPane, BorderLayout.CENTER);

		dataList = new JList<>();
		dataList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		dataList.setVisibleRowCount(-1);
		dataScrollPane.setViewportView(dataList);

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

		btnAbort = new JButton("");
		btnAbort.setEnabled(false);
		btnAbort.setMargin(new Insets(2, 2, 2, 2));
		btnAbort.setIcon(new ImageIcon("C:\\Users\\dbx\\git\\MultiCloudDesktop\\images\\abort.png"));
		btnAbort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		progressPanel.add(btnAbort);

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
						displayMessage("Added new account.");
					} catch (MultiCloudException e) {
						displayError(e.getMessage());
					}
					break;
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					displayMessage("Adding account canceled.");
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
						@Override
						public void run() {
							try {
								cloud.authorizeAccount(account.getName(), new BrowserCallback(dialog, window));
							} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
								dialog.setAlwaysOnTop(false);
								dialog.setFailed(true);
								displayError(e.getMessage());
							}
							dialog.closeDialog();
						};
					};
					t.start();
					dialog.setVisible(true);
					if (dialog.isAborted()) {
						cloud.abortAuthorization();
						displayMessage("Authorization aborted.");
					} else {
						try {
							t.join();
						} catch (InterruptedException e) {
							displayError(e.getMessage());
						}
						if (!dialog.isFailed()) {
							displayMessage("Account authorized.");
							account.setAuthorized(true);
						}
					}
				} else {
					displayError("No account selected. Select one first.");
				}
			}
		});
		mntmAuthorize.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmAuthorize);

		mntmInformation = new JMenuItem("Information");
		mntmInformation.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmInformation);

		mntmQuota = new JMenuItem("Quota");
		mntmQuota.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmQuota);

		mntmRenameAccount = new JMenuItem("Rename");
		mntmRenameAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AccountData account = accountList.getSelectedValue();
				if (account == null) {
					JOptionPane.showMessageDialog(window, "No account selected for removal.", "Rename account", JOptionPane.ERROR_MESSAGE);
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
							displayMessage("Account renamed.");
						} catch (MultiCloudException e) {
							displayError(e.getMessage());
						}
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
					default:
						displayMessage("Renaming account canceled.");
						break;
					}				}
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
							displayMessage("Account removed.");
						} catch (MultiCloudException e) {
							displayError(e.getMessage());
						}
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
					default:
						displayMessage("Account removal canceled.");
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
		mnOperation.add(mntmMultiDownload);

		mntmCreateFolder = new JMenuItem("Create folder");
		mntmCreateFolder.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCreateFolder);

		mntmRename = new JMenuItem("Rename");
		mntmRename.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmRename);

		mntmMove = new JMenuItem("Move");
		mntmMove.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmMove);

		mntmCopy = new JMenuItem("Copy");
		mntmCopy.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCopy);

		mntmDelete = new JMenuItem("Delete");
		mntmDelete.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmDelete);
	}

	private void displayError(String message) {
		lblStatus.setForeground(Color.RED);
		lblStatus.setText("Error: " + message);
		JOptionPane.showMessageDialog(window, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void displayMessage(String message) {
		lblStatus.setForeground(Color.BLACK);
		lblStatus.setText(message);
	}

}
