package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

public class SynchronizeDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 7009880397181595733L;

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

		lblBrowse = new JLabel("Local folder:");
		lblBrowse.setBorder(new EmptyBorder(2, 2, 2, 2));
		browseTree = new JTree();
		JScrollPane browsePane = new JScrollPane();
		browsePane.setPreferredSize(new Dimension(200, 200));
		browsePane.setViewportView(browseTree);
		browsePanel = new JPanel();
		browsePanel.setLayout(new BoxLayout(browsePanel, BoxLayout.PAGE_AXIS));
		browsePanel.add(lblBrowse);
		browsePanel.add(browsePane);

		lblAccount = new JLabel("Accounts:");
		lblAccount.setBorder(new EmptyBorder(2, 2, 2, 2));
		DefaultListModel<AccountData> model = new DefaultListModel<>();
		for (AccountSettings account: parent.getAccountManager().getAllAccountSettings()) {
			if (account.isAuthorized()) {
				AccountData data = new AccountData();
				data.setName(account.getAccountId());
				data.setCloud(account.getSettingsId());
				model.addElement(data);
			}
		}
		accountList = new JList<>();
		accountList.setVisibleRowCount(-1);
		//accountList.setCellRenderer(renderer);
		accountList.setModel(model);
		accountList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		accountList.setSelectionInterval(0, model.getSize() - 1);
		JScrollPane accountPane = new JScrollPane();
		accountPane.setPreferredSize(new Dimension(150, 200));
		accountPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		accountPane.setViewportView(accountList);
		accountPanel = new JPanel();
		accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.PAGE_AXIS));
		accountPanel.add(lblAccount);
		accountPanel.add(accountPane);

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
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				option = JOptionPane.CLOSED_OPTION;
			}
		});

		add(commonPanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parentFrame);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

}
