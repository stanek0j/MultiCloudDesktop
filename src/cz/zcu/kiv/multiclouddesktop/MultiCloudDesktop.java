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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multiclouddesktop.action.AddAccountAction;
import cz.zcu.kiv.multiclouddesktop.action.AuthorizeAction;
import cz.zcu.kiv.multiclouddesktop.action.CopyAction;
import cz.zcu.kiv.multiclouddesktop.action.CreateFolderAction;
import cz.zcu.kiv.multiclouddesktop.action.CutAction;
import cz.zcu.kiv.multiclouddesktop.action.DeleteAction;
import cz.zcu.kiv.multiclouddesktop.action.DownloadAction;
import cz.zcu.kiv.multiclouddesktop.action.ExitAction;
import cz.zcu.kiv.multiclouddesktop.action.FindAction;
import cz.zcu.kiv.multiclouddesktop.action.InformationAction;
import cz.zcu.kiv.multiclouddesktop.action.MultiDownloadAction;
import cz.zcu.kiv.multiclouddesktop.action.PasteAction;
import cz.zcu.kiv.multiclouddesktop.action.PreferencesAction;
import cz.zcu.kiv.multiclouddesktop.action.PropertiesAction;
import cz.zcu.kiv.multiclouddesktop.action.QuotaAction;
import cz.zcu.kiv.multiclouddesktop.action.RefreshAction;
import cz.zcu.kiv.multiclouddesktop.action.RemoveAccountAction;
import cz.zcu.kiv.multiclouddesktop.action.RenameAccountAction;
import cz.zcu.kiv.multiclouddesktop.action.RenameAction;
import cz.zcu.kiv.multiclouddesktop.action.TransferType;
import cz.zcu.kiv.multiclouddesktop.action.UploadAction;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.AccountDataListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.data.AccountInfoCallback;
import cz.zcu.kiv.multiclouddesktop.data.AccountQuotaCallback;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundTask;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundWorker;
import cz.zcu.kiv.multiclouddesktop.data.FileInfoCallback;
import cz.zcu.kiv.multiclouddesktop.data.FileInfoListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.data.MessageCallback;
import cz.zcu.kiv.multiclouddesktop.data.Preferences;
import cz.zcu.kiv.multiclouddesktop.data.SearchCallback;
import cz.zcu.kiv.multiclouddesktop.dialog.AuthorizeDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.DialogProgressListener;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

public class MultiCloudDesktop extends JFrame {

	/** Serialization constant. */
	private static final long serialVersionUID = -1394767380063338580L;

	/** Default file for holding preferences. */
	public static final String PREFS_FILE = "preferences.json";
	/** Application name. */
	private static final String APP_NAME = "MultiCloudDesktop";
	/** The application frame. */
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
	private final JMenuItem mntmFind;
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

	private final JPopupMenu popupAccountMenu;
	private final JMenuItem mntmAddAccountPop;
	private final JMenuItem mntmAuthorizePop;
	private final JMenuItem mntmInformationPop;
	private final JMenuItem mntmQuotaPop;
	private final JMenuItem mntmRenameAccountPop;
	private final JMenuItem mntmRemoveAccountPop;

	private final JPopupMenu popupMenu;
	private final JMenuItem mntmRefreshPop;
	private final JMenuItem mntmFindPop;
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

	private final Action actPreferences;
	private final Action actExit;

	private final Action actAddAccount;
	private final Action actAuthorize;
	private final Action actInformation;
	private final Action actQuota;
	private final Action actRenameAccount;
	private final Action actRemoveAccount;

	private final Action actRefresh;
	private final Action actFind;
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
	private TransferType transferType;
	private Preferences prefs;
	private final Object lock;
	/** JSON parser instance. */
	private final Json json;

	public MultiCloudDesktop() {
		loader = MultiCloudDesktop.class.getClassLoader();
		transferFile = null;
		transferType = TransferType.NONE;
		lock = new Object();
		json = Json.getInstance();
		prefs = preferencesLoad();

		setMinimumSize(new Dimension(720, 480));
		setPreferredSize(new Dimension(720, 480));
		setSize(new Dimension(720, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setTitle(APP_NAME);

		ImageIcon icnAbort = null;
		ImageIcon icnFolder = null;
		ImageIcon icnFolderSmall = null;
		ImageIcon icnFile = null;
		ImageIcon icnFileSmall = null;
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
			icnFolderSmall = new ImageIcon(ImageIO.read(loader.getResourceAsStream("folder_small.png")));
			icnFile = new ImageIcon(ImageIO.read(loader.getResourceAsStream("file.png")));
			icnFileSmall = new ImageIcon(ImageIO.read(loader.getResourceAsStream("file_small.png")));
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
							worker.listFolder(account.getName(), null);
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
						worker.listFolder(account.getName(), null);
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
				accountList.setSelectedIndex(accountList.locationToIndex(event.getPoint()));
				popupAccountMenu.show(event.getComponent(), event.getX(), event.getY());
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
		dataList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				FileInfo file = dataList.getSelectedValue();
				if (event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_ENTER) {
					if (file != null && file.getFileType() == FileType.FOLDER) {
						worker.listFolder(currentAccount, file);
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
						worker.listFolder(currentAccount, file);
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
		dataRenderer = new FileInfoListCellRenderer(icnFolder, icnFolderSmall, icnFile, icnFileSmall);
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
		infoCallback = new AccountInfoCallback(this);
		quotaCallback = new AccountQuotaCallback(this, accountList);
		listCallback = new FileInfoCallback(this, accountList, dataList);

		messageCallback = new MessageCallback(this, lblStatus, prefs.isShowErrorDialog());
		worker = new BackgroundWorker(this, cloud, btnAbort, progressBar, infoCallback, quotaCallback, listCallback, messageCallback);
		worker.start();
		String[] accounts = new String[accountModel.getSize()];
		for (int i = 0; i < accountModel.getSize(); i++) {
			accounts[i] = accountModel.get(i).getName();
		}
		worker.load(accounts);
		refreshCurrentPath();

		addWindowListener(new WindowAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void windowClosing(WindowEvent event) {
				actionClose();
			}
		});

		actPreferences = new PreferencesAction(this);
		actExit = new ExitAction(this);

		actAddAccount = new AddAccountAction(this);
		actAuthorize = new AuthorizeAction(this);
		actInformation = new InformationAction(this);
		actQuota = new QuotaAction(this);
		actRenameAccount = new RenameAccountAction(this);
		actRemoveAccount = new RemoveAccountAction(this);

		actRefresh = new RefreshAction(this);
		actFind = new FindAction(this, icnFolderSmall, icnFileSmall);
		actUpload = new UploadAction(this, null);
		actDownload = new DownloadAction(this, null);
		actMultiDownload = new MultiDownloadAction(this, null, icnFolderSmall, icnFileSmall);
		actCreateFolder = new CreateFolderAction(this);
		actRename = new RenameAction(this);
		actDelete = new DeleteAction(this);
		actCut = new CutAction(this);
		actCopy = new CopyAction(this);
		actPaste = new PasteAction(this);
		actProperties = new PropertiesAction(this);

		dataList.getActionMap().put(TransferHandler.getCutAction().getValue(CutAction.ACT_NAME), TransferHandler.getCutAction());
		dataList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), TransferHandler.getCutAction());
		dataList.getActionMap().put(TransferHandler.getCopyAction().getValue(CopyAction.ACT_NAME), TransferHandler.getCopyAction());
		dataList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), TransferHandler.getCopyAction());
		dataList.getActionMap().put(TransferHandler.getPasteAction().getValue(PasteAction.ACT_NAME), TransferHandler.getPasteAction());
		dataList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), TransferHandler.getPasteAction());

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		mnFile.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnFile);

		mntmPreferences = new JMenuItem();
		mntmPreferences.setAction(actPreferences);
		mnFile.add(mntmPreferences);

		mntmExit = new JMenuItem();
		mntmExit.setAction(actExit);
		mnFile.add(mntmExit);

		mnAccount = new JMenu("Account");
		mnAccount.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnAccount);

		mntmAddAccount = new JMenuItem();
		mntmAddAccount.setAction(actAddAccount);
		mnAccount.add(mntmAddAccount);

		mntmAuthorize = new JMenuItem("Authorize");
		mntmAuthorize.setAction(actAuthorize);
		mnAccount.add(mntmAuthorize);

		mntmInformation = new JMenuItem();
		mntmInformation.setAction(actInformation);
		mnAccount.add(mntmInformation);

		mntmQuota = new JMenuItem();
		mntmQuota.setAction(actQuota);
		mnAccount.add(mntmQuota);

		mntmRenameAccount = new JMenuItem();
		mntmRenameAccount.setAction(actRenameAccount);
		mnAccount.add(mntmRenameAccount);

		mntmRemoveAccount = new JMenuItem();
		mntmRemoveAccount.setAction(actRemoveAccount);
		mnAccount.add(mntmRemoveAccount);

		popupAccountMenu = new JPopupMenu();

		mntmAddAccountPop = new JMenuItem();
		mntmAddAccountPop.setAction(actAddAccount);
		popupAccountMenu.add(mntmAddAccountPop);

		mntmAuthorizePop = new JMenuItem();
		mntmAuthorizePop.setAction(actAuthorize);
		popupAccountMenu.add(mntmAuthorizePop);

		mntmInformationPop = new JMenuItem();
		mntmInformationPop.setAction(actInformation);
		popupAccountMenu.add(mntmInformationPop);

		mntmQuotaPop = new JMenuItem();
		mntmQuotaPop.setAction(actQuota);
		popupAccountMenu.add(mntmQuotaPop);

		mntmRenameAccountPop = new JMenuItem();
		mntmRenameAccountPop.setAction(actRenameAccount);
		popupAccountMenu.add(mntmRenameAccountPop);

		mntmRemoveAccountPop = new JMenuItem();
		mntmRemoveAccountPop.setAction(actRemoveAccount);
		popupAccountMenu.add(mntmRemoveAccountPop);

		mnOperation = new JMenu("Operation");
		mnOperation.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnOperation);

		mntmRefresh = new JMenuItem();
		mntmRefresh.setAction(actRefresh);
		mnOperation.add(mntmRefresh);

		mntmFind = new JMenuItem();
		mntmFind.setAction(actFind);
		mnOperation.add(mntmFind);

		mnOperation.addSeparator();

		mntmUpload = new JMenuItem();
		mntmUpload.setAction(actUpload);
		mnOperation.add(mntmUpload);

		mntmDownload = new JMenuItem();
		mntmDownload.setAction(actDownload);
		mnOperation.add(mntmDownload);

		mntmMultiDownload = new JMenuItem();
		mntmMultiDownload.setAction(actMultiDownload);
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

		mntmFindPop = new JMenuItem();
		mntmFindPop.setAction(actFind);
		popupMenu.add(mntmFindPop);

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

		actionPreferences(prefs);
	}

	public synchronized void actionAbort() {
		worker.abort();
	}

	public synchronized void actionAddAccount(AccountData account) {
		try {
			cloud.createAccount(account.getName(), account.getCloud());
			accountModel.addElement(account);
			messageCallback.displayMessage("Added new account.");
		} catch (MultiCloudException e) {
			messageCallback.displayError(e.getMessage());
		}
	}

	public synchronized void actionAuthorize(final AccountData account, final AuthorizeDialog dialog) {
		Thread t = new Thread() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				try {
					cloud.authorizeAccount(account.getName(), new BrowserCallback(dialog, window));
				} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e) {
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
				t.join(1000);
			} catch (InterruptedException e) {
				/* interrupted exception */
			}
			if (!dialog.isFailed()) {
				messageCallback.displayMessage("Account authorized.");
				account.setAuthorized(true);
				worker.refresh(account.getName(), null);
			}
		}
	}

	public synchronized void actionClose() {
		worker.terminate();
		dispose();
		System.exit(0);
	}

	public synchronized void actionCopy(FileInfo file) {
		transferFile = file;
		transferType = TransferType.COPY;
	}

	public synchronized void actionCreateFolder(String name) {
		worker.createFolder(currentAccount, name, currentFolder);
	}

	public synchronized void actionCut(FileInfo file) {
		transferFile = file;
		transferType = TransferType.MOVE;
	}

	public synchronized void actionDelete(FileInfo file) {
		worker.delete(currentAccount, file, currentFolder);
	}

	public synchronized void actionDownload(FileInfo file, File target, boolean overwrite, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		worker.download(currentAccount, file, target, overwrite, dialog);
		/*
		String[] accounts = new String[5];
		FileInfo[] files = new FileInfo[5];
		for (int i = 0; i < 5; i++) {
			accounts[i] = currentAccount;
			files[i] = file;
		}
		worker.multiDownload(accounts, files, target, overwrite, dialog);
		 */
	}

	public synchronized void actionInformation(AccountData account) {
		worker.accountInfo(account.getName());
	}

	public synchronized void actionMultiDownload(String[] accounts, FileInfo[] files, File target, boolean overwrite, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		worker.multiDownload(accounts, files, target, overwrite, dialog);
	}

	public synchronized void actionPaste(String name) {
		switch (transferType) {
		case COPY:
			messageCallback.displayMessage("Copying file...");
			worker.copy(currentAccount, transferFile, currentFolder, name);
			break;
		case MOVE:
			messageCallback.displayMessage("Moving file...");
			worker.move(currentAccount, transferFile, currentFolder, name);
			break;
		case NONE:
		default:
			break;
		}
		transferFile = null;
		transferType = TransferType.NONE;
	}

	public synchronized void actionPreferences(Preferences preferences) {
		prefs = preferences;
		preferencesSave();
		switch (prefs.getDisplayType()) {
		case ICONS:
			dataList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			dataList.setFixedCellWidth(80);
			dataList.setFixedCellHeight(96);
			break;
		case LINES:
		default:
			dataList.setLayoutOrientation(JList.VERTICAL);
			dataList.setFixedCellWidth(-1);
			dataList.setFixedCellHeight(-1);
			break;
		}
		worker.setShowDeleted(prefs.isShowDeleted());
		worker.setShowShared(prefs.isShowShared());
		messageCallback.setShowErrorDialog(prefs.isShowErrorDialog());
	}

	public synchronized void actionQuota(AccountData account) {
		worker.accountQuota(account.getName());
	}

	public synchronized void actionRefresh(String accountName) {
		synchronized (lock) {
			if (accountName != null && !accountName.equals(currentAccount)) {
				currentFolder = null;
				currentPath.clear();
			}
		}
		worker.refresh(accountName, currentFolder);
	}

	public synchronized void actionRemoveAccount(AccountData account) {
		try {
			cloud.deleteAccount(account.getName());
			accountModel.removeElement(account);
			messageCallback.displayMessage("Account removed.");
		} catch (MultiCloudException e) {
			messageCallback.displayError(e.getMessage());
		}
	}

	public synchronized void actionRename(String name, FileInfo file) {
		worker.rename(currentAccount, name, file, currentFolder);
	}

	public synchronized void actionRenameAccount(AccountData original, AccountData renamed) {
		try {
			cloud.renameAccount(original.getName(), renamed.getName());
			accountModel.removeElement(original);
			accountModel.addElement(renamed);
			messageCallback.displayMessage("Account renamed.");
		} catch (MultiCloudException e) {
			messageCallback.displayError(e.getMessage());
		}
	}

	public synchronized boolean actionSearch(String account, String query, SearchCallback callback) {
		return worker.search(account, query, callback);
	}

	public synchronized void actionUpload(File file, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		worker.upload(currentAccount, file, currentFolder, dialog);
	}

	public JList<AccountData> getAccountList() {
		return accountList;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public CloudManager getCloudManager() {
		return cloudManager;
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

	public JList<FileInfo> getDataList() {
		return dataList;
	}

	public MessageCallback getMessageCallback() {
		return messageCallback;
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

	public Preferences getPreferences() {
		return prefs;
	}

	public DialogProgressListener getProgressListener() {
		return progressListener;
	}

	public synchronized FileInfo getTransferFile() {
		return transferFile;
	}

	/**
	 * Load preferences from a file.
	 * @return Loaded preferences.
	 */
	private Preferences preferencesLoad() {
		Preferences loadedPrefs = new Preferences();
		try {
			ObjectMapper mapper = json.getMapper();
			loadedPrefs = mapper.readValue(new File(PREFS_FILE), Preferences.class);
		} catch (IOException e) {
			messageCallback.displayError("Preferences file not found.");
		}
		return loadedPrefs;
	}

	/**
	 * Save preferences to a file.
	 */
	private void preferencesSave() {
		try {
			ObjectMapper mapper = json.getMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(PREFS_FILE), prefs);
		} catch (IOException e) {
			messageCallback.displayError("Failed to save preferences.");
		}
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

	public void setCurrentFolder(BackgroundTask task, FileInfo currentFolder) {
		synchronized (lock) {
			this.currentFolder = currentFolder;
			if (task == BackgroundTask.LIST_FOLDER) {
				if (currentFolder.getName() != null && currentFolder.getName().equals("..")) {
					currentPath.removeLast();
				} else {
					currentPath.add(currentFolder);
				}
			}
			if (currentPath.isEmpty()) {
				currentPath.add(currentFolder);
			}
			refreshCurrentPath();
		}
	}

}
