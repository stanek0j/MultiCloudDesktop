package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.MultiCloudTreeNode;
import cz.zcu.kiv.multiclouddesktop.data.SyncData;
import cz.zcu.kiv.multiclouddesktop.renderer.SynchronizeDialogListCellRenderer;
import cz.zcu.kiv.multiclouddesktop.renderer.SynchronizeDialogTreeCellRenreder;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/SynchronizeDialog.java			<br /><br />
 *
 * Dialog for selecting synchronization accounts of local items.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SynchronizeDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 7009880397181595733L;

	/** Apply to sub-items button. */
	private final JButton btnApply;
	/** Cancel button. */
	private final JButton btnCancel;
	/** Confirmation button. */
	private final JButton btnOk;
	/** List with user accounts. */
	private final JList<AccountData> accountList;
	/** Label with description for combo box. */
	private final JLabel lblAccount;
	/** Label for list of results. */
	private final JLabel lblBrowse;
	/** Panel for choosing account. */
	private final JPanel accountPanel;
	/** Panel for holding buttons. */
	private final JPanel buttonPanel;
	/** Panel for displaying local folder. */
	private final JPanel browsePanel;
	/** Panel for displaying local folder and account panels. */
	private final JPanel commonPanel;
	/** Tree for showing local folder structure. */
	private final JTree browseTree;

	/** Parent frame. */
	private final MultiCloudDesktop parent;
	/** Return code from the dialog. */
	private int option;

	/**
	 * Ctor with necessary parameters.
	 * @param parentFrame Parent frame.
	 * @param title Dialog title.
	 * @param icnFolder Folder icon.
	 * @param icnFile File icon.
	 */
	public SynchronizeDialog(MultiCloudDesktop parentFrame, String title, ImageIcon folder, ImageIcon file) {
		parent = parentFrame;

		lblBrowse = new JLabel("Local folder content:");
		lblBrowse.setAlignmentX(Component.LEFT_ALIGNMENT);
		lblBrowse.setBorder(new EmptyBorder(2, 2, 2, 2));
		browseTree = new JTree();
		browseTree.addTreeSelectionListener(new TreeSelectionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void valueChanged(TreeSelectionEvent event) {
				MultiCloudTreeNode node = (MultiCloudTreeNode) event.getPath().getLastPathComponent();
				for (int i = 0; i < accountList.getModel().getSize(); i++) {
					accountList.getModel().getElementAt(i).setMatched(false);
				}
				if (node.getFile().isFile()) {
					btnApply.setEnabled(false);
					for (String account: node.getAccounts()) {
						for (int i = 0; i < accountList.getModel().getSize(); i++) {
							AccountData data = accountList.getModel().getElementAt(i);
							if (account.equals(data.getName())) {
								data.setMatched(true);
								break;
							}
						}
					}
				} else {
					btnApply.setEnabled(true);
				}
				accountList.revalidate();
				accountList.repaint();
			}
		});
		browseTree.setCellRenderer(new SynchronizeDialogTreeCellRenreder(folder, file));
		browseTree.setRowHeight((folder.getIconHeight() > file.getIconHeight()) ? folder.getIconHeight() + 2 : file.getIconHeight() + 2);
		browseTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		loadLocalFileStructure();
		importSyncData(parent.getPreferences().getSyncData(), (MultiCloudTreeNode) browseTree.getModel().getRoot());
		JScrollPane browsePane = new JScrollPane();
		browsePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		browsePane.setPreferredSize(new Dimension(240, 200));
		browsePane.setViewportView(browseTree);
		browsePanel = new JPanel();
		browsePanel.setLayout(new BoxLayout(browsePanel, BoxLayout.PAGE_AXIS));
		browsePanel.add(lblBrowse);
		browsePanel.add(browsePane);

		lblAccount = new JLabel("Accounts:");
		lblAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblAccount.setBorder(new EmptyBorder(2, 2, 2, 2));
		lblAccount.setMaximumSize(new Dimension(180, lblAccount.getPreferredSize().height));
		SynchronizeDialogListCellRenderer renderer = new SynchronizeDialogListCellRenderer(new Font(lblAccount.getFont().getFontName(), Font.BOLD, lblAccount.getFont().getSize()), lblAccount.getFont());
		DefaultListModel<AccountData> model = new DefaultListModel<>();
		for (AccountSettings account: parent.getAccountManager().getAllAccountSettings()) {
			if (account.isAuthorized()) {
				AccountData data = new AccountData();
				data.setName(account.getAccountId());
				data.setCloud(account.getSettingsId());
				data.setMatched(false);
				model.addElement(data);
			}
		}
		accountList = new JList<>();
		accountList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				AccountData item = accountList.getSelectedValue();
				MultiCloudTreeNode node = (MultiCloudTreeNode) browseTree.getSelectionPath().getLastPathComponent();
				if (item != null) {
					item.setMatched(!item.isMatched());
					if (node != null) {
						if (item.isMatched()) {
							node.getAccounts().add(item.getName());
						} else {
							node.getAccounts().remove(item.getName());
						}
					}
					revalidate();
					repaint();
				}
			}
		});
		accountList.setVisibleRowCount(-1);
		accountList.setCellRenderer(renderer);
		accountList.setModel(model);
		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane accountPane = new JScrollPane();
		accountPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		accountPane.setPreferredSize(new Dimension(180, 180));
		accountPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		accountPane.setViewportView(accountList);
		btnApply = new JButton("Apply to all sub-items");
		btnApply.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				List<String> selected = new ArrayList<>();
				for (int i = 0; i < accountList.getModel().getSize(); i++) {
					AccountData account = accountList.getModel().getElementAt(i);
					if (account.isMatched()) {
						selected.add(account.getName());
					}
				}
				MultiCloudTreeNode root = (MultiCloudTreeNode) browseTree.getSelectionPath().getLastPathComponent();
				applyToTree(root, selected);
			}
		});
		btnApply.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnApply.setEnabled(false);
		btnApply.setMargin(new Insets(4, 20, 4, 20));
		btnApply.setPreferredSize(new Dimension(180, btnApply.getPreferredSize().height));
		accountPanel = new JPanel();
		accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.PAGE_AXIS));
		accountPanel.add(lblAccount);
		accountPanel.add(accountPane);
		accountPanel.add(btnApply);

		commonPanel = new JPanel(new BorderLayout(4, 0));
		commonPanel.setBorder(new EmptyBorder(4, 4, 2, 4));
		commonPanel.add(browsePanel, BorderLayout.CENTER);
		commonPanel.add(accountPanel, BorderLayout.EAST);

		btnOk = new JButton("OK");
		btnOk.setMargin(new Insets(4, 20, 4, 20));
		btnOk.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				option = JOptionPane.OK_OPTION;
				parent.getPreferences().setSyncData(exportSyncData((MultiCloudTreeNode) browseTree.getModel().getRoot(), null));
				dispose();
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
			public void windowClosing(WindowEvent event) {
				super.windowClosing(event);
				option = JOptionPane.CLOSED_OPTION;
			}
		});

		add(commonPanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parentFrame);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Applies account selection to all nodes in the tree.
	 * @param node Root of the tree.
	 * @param accounts Accounts to synchronize to.
	 */
	private void applyToTree(MultiCloudTreeNode node, List<String> accounts) {
		if (node == null) {
			return;
		}
		if (node.getFile().isDirectory()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				MultiCloudTreeNode inner = (MultiCloudTreeNode) node.getChildAt(i);
				applyToTree(inner, accounts);
			}
		} else {
			node.getAccounts().clear();
			for (String account: accounts) {
				if (!node.getAccounts().contains(account)) {
					node.getAccounts().add(account);
				}
			}
		}
	}

	/**
	 * Exports the tree structure to {@link cz.zcu.kiv.multiclouddesktop.data.SyncData} structures.
	 * @param node Root node of the tree.
	 * @param parent Parent data structure.
	 * @return Data structure.
	 */
	private SyncData exportSyncData(MultiCloudTreeNode node, SyncData parent) {
		if (node == null) {
			return null;
		}
		SyncData data = new SyncData();
		data.setName(node.getName());
		if (node.getFile().isDirectory()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				MultiCloudTreeNode inner = (MultiCloudTreeNode) node.getChildAt(i);
				exportSyncData(inner, data);
			}
		} else {
			data.getAccounts().addAll(node.getAccounts());
		}
		if (parent != null) {
			parent.getNodes().add(data);
		}
		return data;
	}

	/**
	 * Returns the return code from the dialog.
	 * @return Return code from the dialog.
	 */
	public int getOption() {
		return option;
	}

	/**
	 * Imports synchronization preferences from {@link cz.zcu.kiv.multiclouddesktop.data.SyncData} structures to tree structure.
	 * @param data Data structure.
	 * @param node Tree node.
	 */
	private void importSyncData(SyncData data, MultiCloudTreeNode node) {
		if (data == null || node == null) {
			return;
		}
		if (data.getName().equals(node.getName())) {
			if (data.getNodes().isEmpty() && node.getFile().isFile()) {
				node.getAccounts().addAll(data.getAccounts());
			} else {
				for (SyncData content: data.getNodes()) {
					for (int i = 0; i < node.getChildCount(); i++) {
						MultiCloudTreeNode inner = (MultiCloudTreeNode) node.getChildAt(i);
						if (content.getName().equals(inner.getName())) {
							importSyncData(content, inner);
						}
					}
				}
			}
		}
	}

	/**
	 * Loads local file into tree structure.
	 * @param folder Folder to be loaded.
	 * @param parent Parent node to add children to.
	 * @return Created tree node.
	 */
	private MultiCloudTreeNode loadLocalFiles(File folder, MultiCloudTreeNode parent) {
		MultiCloudTreeNode node = null;
		if (parent == null) {
			node = new MultiCloudTreeNode(folder, "Synchronization folder");
		} else {
			node = new MultiCloudTreeNode(folder);
		}
		if (folder.isDirectory()) {
			for (File content: folder.listFiles()) {
				loadLocalFiles(content, node);
			}
		}
		if (parent != null) {
			parent.add(node);
		}
		return node;
	}

	/**
	 * Method for loading local synchronization folder to tree structure.
	 */
	private void loadLocalFileStructure() {
		String syncFolder = parent.getPreferences().getSyncFolder();
		if (syncFolder != null) {
			File root = new File(syncFolder);
			MultiCloudTreeNode rootNode = loadLocalFiles(root, null);
			browseTree.setModel(new DefaultTreeModel(rootNode));
		}
	}

}