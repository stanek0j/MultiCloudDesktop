package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.callback.BrowseCallback;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.renderer.SearchDialogListCellRenderer;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/BrowseDialog.java			<br /><br />
 *
 * Dialog for browsing and selecting folders.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class BrowseDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = -2251532826283188410L;

	/** Abort button. */
	private final JButton btnAbort;
	/** Cancel button. */
	private final JButton btnCancel;
	/** Confirmation button. */
	private final JButton btnOk;
	/** Refresh button. */
	private final JButton btnRefresh;
	/** Combo box for choosing account. */
	private final JComboBox<AccountData> cmbAccount;
	/** Label with description for combo box. */
	private final JLabel lblAccount;
	/** Label for list of results. */
	private final JLabel lblResults;
	/** Label for status label. */
	private final JLabel lblStatus;
	/** Label for displaying status. */
	private final JLabel lblStatusText;
	/** List of the results. */
	private final JList<FileInfo> resultsList;
	/** Panel for choosing account. */
	private final JPanel accountPanel;
	/** Panel for holding buttons. */
	private final JPanel buttonPanel;
	/** Panel for displaying results. */
	private final JPanel resultsPanel;
	/** Panel for displaying status. */
	private final JPanel statusPanel;

	/** Parent frame. */
	private final MultiCloudDesktop parent;
	/** List callback. */
	private final BrowseCallback callback;
	/** Return code from the dialog. */
	private int option;
	/** Array of accounts to choose from. */
	private final AccountData[] accounts;
	/** File data to be returned as output. */
	private final FileInfo[] output;
	/** Output index to be skipped. */
	private int skipIndex;
	/** Currently displayed folder. */
	private FileInfo currentFolder;
	/** Current path. */
	private final LinkedList<FileInfo> currentPath;
	/** If the folder was refreshed. */
	private boolean refresh;

	/**
	 * Ctor with necessary parameters.
	 * @param parentFrame Parent frame.
	 * @param title Dialog title.
	 * @param original Original folder selected.
	 * @param selectedAccounts Accounts to be searched.
	 * @param icnFolder Folder icon.
	 * @param icnFile File icon.
	 */
	public BrowseDialog(MultiCloudDesktop parentFrame, String title, FileInfo original, AccountData[] selectedAccounts, ImageIcon folder, ImageIcon file) {
		parent = parentFrame;
		this.accounts = selectedAccounts;
		this.option = JOptionPane.DEFAULT_OPTION;
		this.skipIndex = -1;
		this.currentPath = new LinkedList<>();
		this.currentFolder = null;

		lblAccount = new JLabel("Account:");
		lblAccount.setBorder(new EmptyBorder(0, 0, 0, 8));
		cmbAccount = new JComboBox<>();
		cmbAccount.setPreferredSize(new Dimension(300, cmbAccount.getPreferredSize().height));
		if (selectedAccounts != null) {
			output = new FileInfo[selectedAccounts.length];
			for (int i = 0; i < selectedAccounts.length; i++) {
				if (!selectedAccounts[i].isMatched()) {
					cmbAccount.addItem(selectedAccounts[i]);
				} else {
					skipIndex = i;
					output[i] = original;
				}
			}
		} else {
			for (AccountSettings account: parent.getAccountManager().getAllAccountSettings()) {
				if (account.isAuthorized()) {
					AccountData data = new AccountData();
					data.setName(account.getAccountId());
					data.setCloud(account.getSettingsId());
					cmbAccount.addItem(data);
				}
			}
			output = new FileInfo[cmbAccount.getItemCount()];
		}
		cmbAccount.addItemListener(new ItemListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					((DefaultListModel<FileInfo>) resultsList.getModel()).clear();
					clearCurrnetPath();
					browse(null, false);
				}
			}
		});
		accountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		accountPanel.setBorder(new EmptyBorder(8, 8, 2, 8));
		accountPanel.add(lblAccount);
		accountPanel.add(cmbAccount);
		lblResults = new JLabel("Folder:");
		lblResults.setBorder(new EmptyBorder(0, 0, 0, 8));
		lblResults.setVerticalAlignment(JLabel.TOP);
		resultsList = new JList<FileInfo>();
		resultsList.setVisibleRowCount(-1);
		resultsList.setCellRenderer(new SearchDialogListCellRenderer(null, false, folder, file, null));
		resultsList.setModel(new DefaultListModel<FileInfo>());
		resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsList.addKeyListener(new KeyAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void keyPressed(KeyEvent event) {
				FileInfo file = resultsList.getSelectedValue();
				if (event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_ENTER) {
					if (file != null && file.getFileType() == FileType.FOLDER) {
						browse(file, false);
					}
				}
			}
		});
		resultsList.addMouseListener(new MouseAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void mouseClicked(MouseEvent event) {
				FileInfo file = resultsList.getSelectedValue();
				if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
					if (file != null && file.getFileType() == FileType.FOLDER) {
						browse(file, false);
					}
				}
			}
		});
		JScrollPane resultsPane = new JScrollPane();
		resultsPane.setPreferredSize(new Dimension(300, 180));
		resultsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		resultsPane.setViewportView(resultsList);
		resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		resultsPanel.add(lblResults, BorderLayout.WEST);
		resultsPanel.add(resultsPane, BorderLayout.CENTER);
		lblStatus = new JLabel("Status:");
		lblStatus.setBorder(new EmptyBorder(0, 0, 0, 8));
		lblStatusText = new JLabel("Refresh to show files.");
		lblStatusText.setBorder(new EmptyBorder(0, 4, 0, 4));
		statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		statusPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		statusPanel.add(lblStatus);
		statusPanel.add(lblStatusText);
		int labelWidth = (lblResults.getPreferredSize().width > lblAccount.getPreferredSize().width) ? lblResults.getPreferredSize().width : lblAccount.getPreferredSize().width;
		labelWidth = (labelWidth > lblStatus.getPreferredSize().width) ? labelWidth : lblStatus.getPreferredSize().width;
		lblAccount.setPreferredSize(new Dimension(labelWidth, lblAccount.getPreferredSize().height));
		lblResults.setPreferredSize(new Dimension(labelWidth, lblResults.getPreferredSize().height));
		lblStatus.setPreferredSize(new Dimension(labelWidth, lblStatus.getPreferredSize().height));
		callback = new BrowseCallback(this, resultsList);
		btnRefresh = new JButton("Refresh");
		btnRefresh.setMargin(new Insets(4, 20, 4, 20));
		btnRefresh.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				browse(getCurrentFolder(), true);
			}
		});
		btnAbort = new JButton("Abort");
		btnAbort.setMargin(new Insets(4, 20, 4, 20));
		btnAbort.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				parent.actionAbort();
				finishBrowse("Aborted.");
			}
		});
		btnOk = new JButton("OK");
		btnOk.setMargin(new Insets(4, 20, 4, 20));
		btnOk.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				if (validateOutput()) {
					option = JOptionPane.OK_OPTION;
					dispose();
				}
			}
		});
		btnCancel = new JButton("Cancel");
		btnCancel.setMargin(new Insets(4, 20, 4, 20));
		btnCancel.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				option = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(2, 0, 4, 0));
		buttonPanel.add(btnRefresh);
		buttonPanel.add(btnAbort);
		buttonPanel.add(btnOk);
		buttonPanel.add(btnCancel);

		setTitle(title);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				option = JOptionPane.CLOSED_OPTION;
			}
		});

		add(accountPanel);
		add(resultsPanel);
		add(statusPanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parentFrame);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Browses the selected folder.
	 * @param file Folder to browse.
	 */
	private synchronized void browse(FileInfo file, boolean refresh) {
		this.refresh = refresh;
		AccountData account = (AccountData) cmbAccount.getSelectedItem();
		lblStatusText.setForeground(Color.BLACK);
		lblStatusText.setText("Listing folder...");
		cmbAccount.setEnabled(false);
		btnRefresh.setEnabled(false);
		btnOk.setEnabled(false);
		btnCancel.setEnabled(false);
		if (!parent.actionBrowse(account.getName(), file, callback)) {
			finishBrowse("Wait for other operation to finish.");
		}

	}

	/**
	 * Clears the current path.
	 */
	public synchronized void clearCurrnetPath() {
		currentFolder = null;
		currentPath.clear();
	}

	/**
	 * Enables all the fields for browsing to the next destination.
	 * @param result Text describing the result.
	 */
	public synchronized void finishBrowse(String result) {
		if (result == null) {
			lblStatusText.setForeground(Color.RED);
			lblStatusText.setText("Listing folder failed.");
		} else {
			lblStatusText.setForeground(Color.BLACK);
			lblStatusText.setText(result);
		}
		int index = cmbAccount.getSelectedIndex();
		if (index > -1) {
			if (index >= skipIndex) {
				index++;
			}
			output[index] = getCurrentFolder();
			AccountData selected = (AccountData) cmbAccount.getSelectedItem();
			if (selected != null && accounts != null) {
				selected.setPath(getCurrentPath());
				cmbAccount.revalidate();
				cmbAccount.repaint();
			}
		}
		cmbAccount.setEnabled(true);
		btnRefresh.setEnabled(true);
		btnOk.setEnabled(true);
		btnCancel.setEnabled(true);
		resultsList.revalidate();
		resultsList.repaint();
		refresh = false;
	}

	/**
	 * Returns the current folder.
	 * @return Current folder.
	 */
	public synchronized FileInfo getCurrentFolder() {
		return currentFolder;
	}

	/**
	 * Returns the current path;
	 * @return Current path;
	 */
	private synchronized String getCurrentPath() {
		StringBuilder sb = new StringBuilder();
		for (FileInfo f: currentPath) {
			if (!Utils.isNullOrEmpty(f.getName())) {
				sb.append("/");
				sb.append(f.getName());
			}
		}
		if (sb.length() == 0) {
			sb.append("/");
		}
		return sb.toString();
	}

	/**
	 * Returns the list for displaying results.
	 * @return List of results.
	 */
	public synchronized JList<FileInfo> getList() {
		return resultsList;
	}

	/**
	 * Returns the return code from the dialog.
	 * @return Return code from the dialog.
	 */
	public int getOption() {
		return option;
	}

	/**
	 * Returns the folder data gathered.
	 * @return Folder data gathered.
	 */
	public FileInfo[] getOutput() {
		return output;
	}

	/**
	 * Returns the parent folder.
	 * @return Parent folder.
	 */
	public synchronized FileInfo getParentFolder() {
		FileInfo parent = null;
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
		return parent;
	}

	/**
	 * Returns the parent frame.
	 * @return Parent frame.
	 */
	public MultiCloudDesktop getParentFrame() {
		return parent;
	}

	/**
	 * Sets the current folder.
	 * @param currentFolder Current folder.
	 */
	public synchronized void setCurrentFolder(FileInfo currentFolder) {
		this.currentFolder = currentFolder;
		if (!refresh) {
			if (currentFolder.getName() != null && currentFolder.getName().equals("..")) {
				currentPath.removeLast();
			} else {
				currentPath.add(currentFolder);
			}
		}
		if (currentPath.isEmpty()) {
			currentPath.add(currentFolder);
		}
	}

	/**
	 * Validates the stored data for output.
	 * @return If the stored data passed the validation.
	 */
	private boolean validateOutput() {
		boolean valid = true;
		valid = false;
		for (FileInfo out: output) {
			if (out != null) {
				valid = true;
			}
		}
		return valid;
	}

}
