package cz.zcu.kiv.multiclouddesktop;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.Box;
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
import cz.zcu.kiv.multiclouddesktop.action.AboutAction;
import cz.zcu.kiv.multiclouddesktop.action.AddAccountAction;
import cz.zcu.kiv.multiclouddesktop.action.AuthorizeAction;
import cz.zcu.kiv.multiclouddesktop.action.ChecksumAction;
import cz.zcu.kiv.multiclouddesktop.action.CopyAction;
import cz.zcu.kiv.multiclouddesktop.action.CreateFolderAction;
import cz.zcu.kiv.multiclouddesktop.action.CutAction;
import cz.zcu.kiv.multiclouddesktop.action.DeleteAction;
import cz.zcu.kiv.multiclouddesktop.action.DownloadAction;
import cz.zcu.kiv.multiclouddesktop.action.ExitAction;
import cz.zcu.kiv.multiclouddesktop.action.FindAction;
import cz.zcu.kiv.multiclouddesktop.action.InformationAction;
import cz.zcu.kiv.multiclouddesktop.action.MultiDownloadAction;
import cz.zcu.kiv.multiclouddesktop.action.MultiUploadAction;
import cz.zcu.kiv.multiclouddesktop.action.PasteAction;
import cz.zcu.kiv.multiclouddesktop.action.PreferencesAction;
import cz.zcu.kiv.multiclouddesktop.action.PropertiesAction;
import cz.zcu.kiv.multiclouddesktop.action.QuotaAction;
import cz.zcu.kiv.multiclouddesktop.action.RefreshAction;
import cz.zcu.kiv.multiclouddesktop.action.RemoveAccountAction;
import cz.zcu.kiv.multiclouddesktop.action.RenameAccountAction;
import cz.zcu.kiv.multiclouddesktop.action.RenameAction;
import cz.zcu.kiv.multiclouddesktop.action.SynchronizeAction;
import cz.zcu.kiv.multiclouddesktop.action.TransferType;
import cz.zcu.kiv.multiclouddesktop.action.UploadAction;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.AccountDataListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.data.AccountInfoCallback;
import cz.zcu.kiv.multiclouddesktop.data.AccountQuotaCallback;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundTask;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundWorker;
import cz.zcu.kiv.multiclouddesktop.data.BrowseCallback;
import cz.zcu.kiv.multiclouddesktop.data.ChecksumProvider;
import cz.zcu.kiv.multiclouddesktop.data.FileInfoCallback;
import cz.zcu.kiv.multiclouddesktop.data.FileInfoListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.data.MessageCallback;
import cz.zcu.kiv.multiclouddesktop.data.Preferences;
import cz.zcu.kiv.multiclouddesktop.data.SearchCallback;
import cz.zcu.kiv.multiclouddesktop.dialog.AuthorizeDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.DialogProgressListener;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

/**
 * cz.zcu.kiv.multiclouddesktop/MultiCloudDesktop.java			<br /><br />
 *
 * Main frame of the multicloud desktop application.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloudDesktop extends JFrame {

	/** Serialization constant. */
	private static final long serialVersionUID = -1394767380063338580L;

	/** Default file for holding preferences. */
	public static final String PREFS_FILE = "preferences.json";
	/** Application name. */
	public static final String APP_NAME = "MultiCloudDesktop";
	/** The application frame. */
	private static MultiCloudDesktop window;

	/**
	 * Main entering point of the application.
	 * @param args Arguments passed.
	 */
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

	/** Panel for holding account information. */
	private final JPanel accountPanel;
	/** Scroll pane of the account list. */
	private final JScrollPane accountScrollPane;
	/** Model of the account list. */
	private final DefaultListModel<AccountData> accountModel;
	/** List of accounts. */
	private final JList<AccountData> accountList;
	/** Account list renderer. */
	private final AccountDataListCellRenderer accountRenderer;
	/** Panel for holding content of the accounts. */
	private final JPanel dataPanel;
	/** Scroll pane of the data list. */
	private final JScrollPane dataScrollPane;
	/** Model of the data list. */
	private final DefaultListModel<FileInfo> dataModel;
	/** List of files and folder. */
	private final JList<FileInfo> dataList;
	/** Data list renderer. */
	private final FileInfoListCellRenderer dataRenderer;
	/** Panel for holding current path. */
	private final JPanel pathPanel;
	/** Label for displaying current path. */
	private final JLabel lblPath;
	/** Panel for holding status information.  */
	private final JPanel statusPanel;
	/** Label with information messages. */
	private final JLabel lblStatus;
	/** Panel for displaying task progress. */
	private final JPanel progressPanel;
	/** Progress bar of the currently running task. */
	private final JProgressBar progressBar;
	/** Button for aborting current task. */
	private final JButton btnAbort;

	/** Main application menu. */
	private final JMenuBar menuBar;
	/** File menu. */
	private final JMenu mnFile;
	/** Synchronize menu item. */
	private final JMenuItem mntmSynchronize;
	/** Preferences menu item. */
	private final JMenuItem mntmPreferences;
	/** Exit menu item. */
	private final JMenuItem mntmExit;
	/** Account menu. */
	private final JMenu mnAccount;
	/** Add account menu item. */
	private final JMenuItem mntmAddAccount;
	/** Authorize account menu item. */
	private final JMenuItem mntmAuthorize;
	/** Account information menu item. */
	private final JMenuItem mntmInformation;
	/** Account quota menu item. */
	private final JMenuItem mntmQuota;
	/** Rename account menu item. */
	private final JMenuItem mntmRenameAccount;
	/** Remove account menu item. */
	private final JMenuItem mntmRemoveAccount;
	/** Operation menu. */
	private final JMenu mnOperation;
	/** Refresh menu item. */
	private final JMenuItem mntmRefresh;
	/** Find menu item. */
	private final JMenuItem mntmFind;
	/** Upload menu item. */
	private final JMenuItem mntmUpload;
	/** Multiple upload menu item. */
	private final JMenuItem mntmMultiUpload;
	/** Download menu item. */
	private final JMenuItem mntmDownload;
	/** Multiple download menu item. */
	private final JMenuItem mntmMultiDownload;
	/** Create folder menu item. */
	private final JMenuItem mntmCreateFolder;
	/** Rename menu item. */
	private final JMenuItem mntmRename;
	/** Delete menu item. */
	private final JMenuItem mntmDelete;
	/** Cut menu item. */
	private final JMenuItem mntmCut;
	/** Copy menu item. */
	private final JMenuItem mntmCopy;
	/** Paste menu item. */
	private final JMenuItem mntmPaste;
	/** Checksum menu item. */
	private final JMenuItem mntmChecksum;
	/** Properties menu item. */
	private final JMenuItem mntmProperties;

	/** Horizontal glue for separating menus. */
	private final Component horizontalGlue;

	/** Help menu. */
	private final JMenu mnHelp;
	/** About menu item. */
	private final JMenuItem mntmAbout;

	/** Pop up menu for account list. */
	private final JPopupMenu popupAccountMenu;
	/** Add account menu item. */
	private final JMenuItem mntmAddAccountPop;
	/** Authorize account menu item. */
	private final JMenuItem mntmAuthorizePop;
	/** Account information menu item. */
	private final JMenuItem mntmInformationPop;
	/** Account quota menu item. */
	private final JMenuItem mntmQuotaPop;
	/** Rename account menu item. */
	private final JMenuItem mntmRenameAccountPop;
	/** Remove account menu item. */
	private final JMenuItem mntmRemoveAccountPop;

	/** Pop up menu for data list. */
	private final JPopupMenu popupMenu;
	/** Refresh menu item. */
	private final JMenuItem mntmRefreshPop;
	/** Find menu item. */
	private final JMenuItem mntmFindPop;
	/** Upload menu item. */
	private final JMenuItem mntmUploadPop;
	/** Multiple upload menu item. */
	private final JMenuItem mntmMultiUploadPop;
	/** Download menu item. */
	private final JMenuItem mntmDownloadPop;
	/** Multiple download menu item. */
	private final JMenuItem mntmMultiDownloadPop;
	/** Create folder menu item. */
	private final JMenuItem mntmCreateFolderPop;
	/** Rename menu item. */
	private final JMenuItem mntmRenamePop;
	/** Delete menu item. */
	private final JMenuItem mntmDeletePop;
	/** Cut menu item. */
	private final JMenuItem mntmCutPop;
	/** Copy menu item. */
	private final JMenuItem mntmCopyPop;
	/** Paste menu item. */
	private final JMenuItem mntmPastePop;
	/** Checksum menu item. */
	private final JMenuItem mntmChecksumPop;
	/** Properties menu item. */
	private final JMenuItem mntmPropertiesPop;

	/** Action for synchronizing folders. */
	private final Action actSynchronize;
	/** Action for displaying preferences. */
	private final Action actPreferences;
	/** Action for exiting application. */
	private final Action actExit;

	/** Action for adding account. */
	private final Action actAddAccount;
	/** Action for authorizing account. */
	private final Action actAuthorize;
	/** Action for getting account information. */
	private final Action actInformation;
	/** Action for getting account quota. */
	private final Action actQuota;
	/** Action for renaming account. */
	private final Action actRenameAccount;
	/** Action for removing account. */
	private final Action actRemoveAccount;

	/** Action for refreshing data list. */
	private final Action actRefresh;
	/** Action for searching for files and folders. */
	private final Action actFind;
	/** Action for uploading files. */
	private final Action actUpload;
	/** Action for uploading files to multiple destinations. */
	private final Action actMultiUpload;
	/** Action for downloading files. */
	private final Action actDownload;
	/** Action for downloading files from multiple sources. */
	private final Action actMultiDownload;
	/** Action for creating folder. */
	private final Action actCreateFolder;
	/** Action for renaming file or folder. */
	private final Action actRename;
	/** Action for deleting file or folder. */
	private final Action actDelete;
	/** Action for moving files. */
	private final Action actCut;
	/** Action for copying files. */
	private final Action actCopy;
	/** Action for pasting files. */
	private final Action actPaste;
	/** Action for computing checksums. */
	private final Action actChecksum;
	/** Action for displaying file or folder properties. */
	private final Action actProperties;

	/** Action for displaying information about application. */
	private final Action actAbout;

	/** Multicloud library. */
	private final MultiCloud cloud;
	/** Account manager. */
	private final AccountManager accountManager;
	/** Cloud manager. */
	private final CloudManager cloudManager;
	/** Progress listener. */
	private final DialogProgressListener progressListener;
	/** Class loader. */
	private final ClassLoader loader;
	/** Account information callback. */
	private final AccountInfoCallback infoCallback;
	/** Account quota callback. */
	private final AccountQuotaCallback quotaCallback;
	/** File list callback. */
	private final FileInfoCallback listCallback;
	/** Message callback. */
	private final MessageCallback messageCallback;

	/** Background thread for executing tasks. */
	private final BackgroundWorker worker;
	/** Current path. */
	private final LinkedList<FileInfo> currentPath;
	/** Current account. */
	private String currentAccount;
	/** Current folder. */
	private FileInfo currentFolder;
	/** Source account of the transferred file. */
	private String transferAccount;
	/** File to be moved or copied. */
	private FileInfo transferFile;
	/** Move or copy transfer. */
	private TransferType transferType;
	/** Preferences of the user. */
	private Preferences prefs;
	/** Lock object. */
	private final Object lock;
	/** JSON parser instance. */
	private final Json json;
	/** Checksum cache for remote files. */
	private final ChecksumProvider cache;

	/**
	 * Empty ctor.
	 */
	public MultiCloudDesktop() {
		loader = MultiCloudDesktop.class.getClassLoader();
		transferAccount = null;
		transferFile = null;
		transferType = TransferType.NONE;
		lock = new Object();
		json = Json.getInstance();
		cache = new ChecksumProvider();

		setMinimumSize(new Dimension(720, 480));
		setPreferredSize(new Dimension(720, 480));
		setSize(new Dimension(720, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setTitle(APP_NAME);

		/* load icons */
		ImageIcon icnAbort = null;
		ImageIcon icnFolder = null;
		ImageIcon icnFolderSmall = null;
		ImageIcon icnFile = null;
		ImageIcon icnFileSmall = null;
		ImageIcon icnBadFileSmall = null;
		ImageIcon icnCloud = null;
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
			icnBadFileSmall = new ImageIcon(ImageIO.read(loader.getResourceAsStream("bad_file_small.png")));
			icnCloud = new ImageIcon(ImageIO.read(loader.getResourceAsStream("cloud_96.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* initialize library */
		cloud = new MultiCloud();
		progressListener = new DialogProgressListener(200);
		cloud.setListener(progressListener);
		accountManager = cloud.getSettings().getAccountManager();
		cloudManager = cloud.getSettings().getCloudManager();
		cloud.validateAccounts();

		/* create GUI */
		/* account list */
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
						if (account.isAuthorized()) {
							synchronized (lock) {
								currentFolder = null;
								currentPath.clear();
							}
							worker.listFolder(account.getName(), null);
						} else {
							actAuthorize.actionPerformed(null);
						}
						break;
					case KeyEvent.VK_DELETE:
						actRemoveAccount.actionPerformed(null);
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
						if (account.isAuthorized()) {
							synchronized (lock) {
								currentFolder = null;
								currentPath.clear();
							}
							worker.listFolder(account.getName(), null);
						} else {
							actAuthorize.actionPerformed(null);
						}
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

		/* data list */
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

		/* status panel */
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

		/* localization objects and background worker */
		prefs = preferencesLoad();
		currentPath = new LinkedList<>();
		infoCallback = new AccountInfoCallback(this);
		quotaCallback = new AccountQuotaCallback(this, accountList);
		listCallback = new FileInfoCallback(this, accountList, dataList);
		messageCallback = new MessageCallback(this, lblStatus, prefs.isShowErrorDialog());
		worker = new BackgroundWorker(this, cloud, cache, btnAbort, progressBar, infoCallback, quotaCallback, listCallback, messageCallback);
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

		/* shared actions */
		actSynchronize = new SynchronizeAction(this);
		actPreferences = new PreferencesAction(this, icnFolderSmall);
		actExit = new ExitAction(this);

		actAddAccount = new AddAccountAction(this);
		actAuthorize = new AuthorizeAction(this);
		actInformation = new InformationAction(this);
		actQuota = new QuotaAction(this);
		actRenameAccount = new RenameAccountAction(this);
		actRemoveAccount = new RemoveAccountAction(this);

		actRefresh = new RefreshAction(this);
		actFind = new FindAction(this, icnFolderSmall, icnFileSmall);
		actUpload = new UploadAction(this);
		actMultiUpload = new MultiUploadAction(this, icnFolderSmall, icnFileSmall);
		actDownload = new DownloadAction(this);
		actMultiDownload = new MultiDownloadAction(this, icnFolderSmall, icnFileSmall, icnBadFileSmall);
		actCreateFolder = new CreateFolderAction(this);
		actRename = new RenameAction(this);
		actDelete = new DeleteAction(this);
		actCut = new CutAction(this);
		actCopy = new CopyAction(this);
		actPaste = new PasteAction(this);
		actChecksum = new ChecksumAction(this);
		actProperties = new PropertiesAction(this);

		actAbout = new AboutAction(this, icnCloud);

		/* transfer handling shortcuts */
		dataList.getActionMap().put(TransferHandler.getCutAction().getValue(CutAction.ACT_NAME), TransferHandler.getCutAction());
		dataList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), TransferHandler.getCutAction());
		dataList.getActionMap().put(TransferHandler.getCopyAction().getValue(CopyAction.ACT_NAME), TransferHandler.getCopyAction());
		dataList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), TransferHandler.getCopyAction());
		dataList.getActionMap().put(TransferHandler.getPasteAction().getValue(PasteAction.ACT_NAME), TransferHandler.getPasteAction());
		dataList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), TransferHandler.getPasteAction());

		/* main and pop up menus */
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		mnFile.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnFile);

		mntmSynchronize = new JMenuItem();
		mntmSynchronize.setAction(actSynchronize);
		mnFile.add(mntmSynchronize);

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

		mntmMultiUpload = new JMenuItem();
		mntmMultiUpload.setAction(actMultiUpload);
		mnOperation.add(mntmMultiUpload);

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

		mntmChecksum = new JMenuItem();
		mntmChecksum.setAction(actChecksum);
		mnOperation.add(mntmChecksum);

		mntmProperties = new JMenuItem();
		mntmProperties.setAction(actProperties);
		mnOperation.add(mntmProperties);

		horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);

		mnHelp = new JMenu("Help");
		mnHelp.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem();
		mntmAbout.setAction(actAbout);
		mnHelp.add(mntmAbout);

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

		mntmMultiUploadPop = new JMenuItem();
		mntmMultiUploadPop.setAction(actMultiUpload);
		popupMenu.add(mntmMultiUploadPop);

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

		mntmChecksumPop = new JMenuItem();
		mntmChecksumPop.setAction(actChecksum);
		popupMenu.add(mntmChecksumPop);

		mntmPropertiesPop = new JMenuItem();
		mntmPropertiesPop.setAction(actProperties);
		popupMenu.add(mntmPropertiesPop);

		/* loading preferences */
		actionPreferences(prefs);
	}

	/**
	 * Abort currently running task.
	 */
	public synchronized void actionAbort() {
		worker.abort();
	}

	/**
	 * Action callback for adding account.
	 * @param account Account.
	 */
	public synchronized void actionAddAccount(AccountData account) {
		try {
			cloud.createAccount(account.getName(), account.getCloud());
			accountModel.addElement(account);
			messageCallback.displayMessage("Added new account.");
		} catch (MultiCloudException e) {
			messageCallback.displayError(e.getMessage());
		}
	}

	/**
	 * Action callback for authorizing account.
	 * @param account Account to be authorized.
	 * @param dialog Authorization dialog.
	 */
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

	/**
	 * Action callback for browsing files.
	 * @param account Account name.
	 * @param folder Folder to browse.
	 * @param callback Browsing callback.
	 * @return If the task can be executed right now.
	 */
	public synchronized boolean actionBrowse(String account, FileInfo folder, BrowseCallback callback) {
		return worker.browse(account, folder, callback);
	}

	/**
	 * Action callback for closing the application.
	 */
	public synchronized void actionClose() {
		worker.terminate();
		dispose();
		System.exit(0);
	}

	/**
	 * Action callback for copying files.
	 * @param file File to be copied.
	 */
	public synchronized void actionCopy(FileInfo file) {
		transferAccount = currentAccount;
		transferFile = file;
		transferType = TransferType.COPY;
	}

	/**
	 * Action callback for creating a folder.
	 * @param name Folder name.
	 */
	public synchronized void actionCreateFolder(String name) {
		worker.createFolder(currentAccount, name, currentFolder);
	}

	/**
	 * Action callback for cutting files.
	 * @param file File to be moved.
	 */
	public synchronized void actionCut(FileInfo file) {
		transferAccount = currentAccount;
		transferFile = file;
		transferType = TransferType.MOVE;
	}

	/**
	 * Action callback for deleting files and folders.
	 * @param file File or folder to be deleted.
	 */
	public synchronized void actionDelete(FileInfo file) {
		worker.delete(currentAccount, file, currentFolder);
	}

	/**
	 * Action callback for downloading a file.
	 * @param file File to be downloaded.
	 * @param target Local file to save the data to.
	 * @param overwrite If the local file should be overwritten.
	 * @param dialog Progress dialog.
	 */
	public synchronized void actionDownload(FileInfo file, File target, boolean overwrite, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		if (target.isDirectory()) {
			prefs.setFolder(target.getPath());
		} else {
			prefs.setFolder(target.getParent());
		}
		preferencesSave();
		int threads = prefs.getThreadsPerAccount();
		if (threads == 1) {
			worker.download(currentAccount, file, target, overwrite, dialog);
		} else {
			String[] accounts = new String[threads];
			FileInfo[] files = new FileInfo[threads];
			for (int i = 0; i < threads; i++) {
				accounts[i] = currentAccount;
				files[i] = file;
			}
			worker.multiDownload(accounts, files, target, overwrite, dialog);
		}
	}

	/**
	 * Action callback for computing remote file checksum.
	 * @param file File for computing checksum.
	 * @param dialog Progress dialog.
	 */
	public synchronized void actionChecksum(FileInfo file, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		worker.checksum(currentAccount, file, prefs.getThreadsPerAccount(), dialog);
	}

	/**
	 * Action callback for getting account information.
	 * @param account Account name.
	 */
	public synchronized void actionInformation(AccountData account) {
		worker.accountInfo(account.getName());
	}

	/**
	 * Action callback for downloading file from multiple sources.
	 * @param accounts Account names.
	 * @param files Files to be downloaded.
	 * @param target Local file to save the data to.
	 * @param overwrite If the local file should be overwritten.
	 * @param dialog Progress dialog.
	 */
	public synchronized void actionMultiDownload(String[] accounts, FileInfo[] files, File target, boolean overwrite, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		if (target.isDirectory()) {
			prefs.setFolder(target.getPath());
		} else {
			prefs.setFolder(target.getParent());
		}
		preferencesSave();
		int threads = prefs.getThreadsPerAccount();
		if (threads == 1) {
			worker.multiDownload(accounts, files, target, overwrite, dialog);
		} else {
			String[] a = new String[accounts.length * threads];
			FileInfo[] f = new FileInfo[files.length * threads];
			for (int i = 0; i < threads; i++) {
				for (int j = 0; j < accounts.length; j++) {
					int index = i * accounts.length + j;
					a[index] = accounts[j];
					f[index] = files[j];
				}
			}
			worker.multiDownload(a, f, target, overwrite, dialog);
		}
	}

	/**
	 * Action callback for uploading file to multiple destinations.
	 * @param accounts Account names.
	 * @param folders Destination folders.
	 * @param file File to be uploaded.
	 * @param dialog Progress dialog.
	 */
	public synchronized void actionMultiUpload(String[] accounts, FileInfo[] folders, FileInfo[] existing, File file, ProgressDialog dialog) {
		progressListener.setDialog(dialog);
		if (file.isDirectory()) {
			prefs.setFolder(file.getPath());
		} else {
			prefs.setFolder(file.getParent());
		}
		preferencesSave();
		worker.multiUpload(currentAccount, accounts, file, currentFolder, folders, existing, dialog);
	}

	/**
	 * Action callback for pasting a file.
	 * @param name New name for the file.
	 * @param existing File to be updated.
	 * @param dialog Progress dialog.
	 */
	public synchronized void actionPaste(String name, FileInfo existing, ProgressDialog dialog) {
		if (currentAccount.equals(transferAccount)) {
			switch (transferType) {
			case COPY:
				messageCallback.displayMessage("Copying file...");
				worker.copy(currentAccount, transferFile, currentFolder, name);
				break;
			case MOVE:
				messageCallback.displayMessage("Moving file...");
				worker.move(currentAccount, transferFile, currentFolder, name);
				transferAccount = null;
				transferFile = null;
				transferType = TransferType.NONE;
				break;
			case NONE:
			default:
				break;
			}
		} else {
			progressListener.setDialog(dialog);
			messageCallback.displayMessage("Transfering file between accounts...");
			worker.transfer(transferAccount, transferFile, prefs.getThreadsPerAccount(), (transferType == TransferType.MOVE), currentAccount, currentFolder, existing, name, dialog);
			if (transferType == TransferType.MOVE) {
				transferAccount = null;
				transferFile = null;
				transferType = TransferType.NONE;
			}
		}
	}

	/**
	 * Action callback for using the preferences.
	 * @param preferences Preferences to be applied.
	 */
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

	/**
	 * Action callback for getting account quota.
	 * @param account Account name.
	 */
	public synchronized void actionQuota(AccountData account) {
		worker.accountQuota(account.getName());
	}

	/**
	 * Action callback for refreshing account data.
	 * @param accountName Account name.
	 */
	public synchronized void actionRefresh(String accountName) {
		synchronized (lock) {
			if (accountName != null && !accountName.equals(currentAccount)) {
				currentFolder = null;
				currentPath.clear();
			}
		}
		worker.refresh(accountName, currentFolder);
	}

	/**
	 * Action callback for removing account.
	 * @param account Account to be removed.
	 */
	public synchronized void actionRemoveAccount(AccountData account) {
		try {
			cloud.deleteAccount(account.getName());
			accountModel.removeElement(account);
			cache.removeAccount(account.getName());
			messageCallback.displayMessage("Account removed.");
		} catch (MultiCloudException e) {
			messageCallback.displayError(e.getMessage());
		}
	}

	/**
	 * Action callback for renaming file or folder.
	 * @param name New name for the file or folder.
	 * @param file File or folder to be renamed.
	 */
	public synchronized void actionRename(String name, FileInfo file) {
		worker.rename(currentAccount, name, file, currentFolder);
	}

	/**
	 * Action callback for renaming account.
	 * @param original Account to be renamed.
	 * @param renamed New name of the account.
	 */
	public synchronized void actionRenameAccount(AccountData original, AccountData renamed) {
		try {
			cloud.renameAccount(original.getName(), renamed.getName());
			accountModel.removeElement(original);
			accountModel.addElement(renamed);
			cache.renameAccount(original.getName(), renamed.getName());
			messageCallback.displayMessage("Account renamed.");
		} catch (MultiCloudException e) {
			messageCallback.displayError(e.getMessage());
		}
	}

	/**
	 * Action callback for searching for a file or folder.
	 * @param account Account name.
	 * @param query Search query.
	 * @param callback Search callback.
	 * @return If the task can be executed right now.
	 */
	public synchronized boolean actionSearch(String account, String query, SearchCallback callback) {
		return worker.search(account, query, callback);
	}

	public synchronized void actionSynchronize() {

	}

	/**
	 * Action callback for uploading a file.
	 * @param file File to be uploaded.
	 * @param dialog Progress dialog.
	 * @param existing Existing file to be updated.
	 * @param overwrite If the file contents should be updated.
	 */
	public synchronized void actionUpload(File file, ProgressDialog dialog, FileInfo existing, boolean overwrite) {
		progressListener.setDialog(dialog);
		if (file.isDirectory()) {
			prefs.setFolder(file.getPath());
		} else {
			prefs.setFolder(file.getParent());
		}
		preferencesSave();
		worker.upload(currentAccount, file, currentFolder, existing, overwrite, dialog);
	}

	/**
	 * Returns the account list.
	 * @return Account list.
	 */
	public JList<AccountData> getAccountList() {
		return accountList;
	}

	/**
	 * Returns the account manager.
	 * @return Account manager.
	 */
	public AccountManager getAccountManager() {
		return accountManager;
	}

	/**
	 * Returns the cloud manager.
	 * @return Cloud manager.
	 */
	public CloudManager getCloudManager() {
		return cloudManager;
	}

	/**
	 * Returns the current account.
	 * @return Current account.
	 */
	public String getCurrentAccount() {
		synchronized (lock) {
			return currentAccount;
		}
	}

	/**
	 * Returns the current folder.
	 * @return Current folder.
	 */
	public FileInfo getCurrentFolder() {
		synchronized (lock) {
			return currentFolder;
		}
	}

	/**
	 * Returns the data list.
	 * @return data list.
	 */
	public JList<FileInfo> getDataList() {
		return dataList;
	}

	/**
	 * Returns the message callback.
	 * @return Message callback.
	 */
	public MessageCallback getMessageCallback() {
		return messageCallback;
	}

	/**
	 * Returns the parent folder of the current folder.
	 * @return Parent folder.
	 */
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

	/**
	 * Returns the user preferences.
	 * @return
	 */
	public Preferences getPreferences() {
		return prefs;
	}

	/**
	 * Returns the progress listener.
	 * @return Progress listener.
	 */
	public DialogProgressListener getProgressListener() {
		return progressListener;
	}

	/**
	 * Returns the account name to move or copy file from.
	 * @return Account name to move or copy file from.
	 */
	public synchronized String getTransferAccount() {
		return transferAccount;
	}

	/**
	 * Returns the file to be moved or copied.
	 * @return File to be moved or copied.
	 */
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
			if (messageCallback != null) {
				messageCallback.displayError("Preferences file not found.");
			}
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

	/**
	 * Refreshes the current path.
	 */
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

	/**
	 * Sets the current account.
	 * @param currentAccount Current account.
	 */
	public void setCurrentAccount(String currentAccount) {
		synchronized (lock) {
			this.currentAccount = currentAccount;
		}
	}

	/**
	 * Sets the current folder.
	 * @param task Task that invoked this method.
	 * @param currentFolder Current folder.
	 */
	public void setCurrentFolder(BackgroundTask task, FileInfo currentFolder) {
		synchronized (lock) {
			this.currentFolder = currentFolder;
			if (task == BackgroundTask.LIST_FOLDER && currentFolder != null) {
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
