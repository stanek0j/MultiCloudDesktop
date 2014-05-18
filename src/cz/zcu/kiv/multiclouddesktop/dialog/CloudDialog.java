package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.CloudDialogListCellRenderer;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/CloudDialog.java			<br /><br />
 *
 * Dialog for selecting multiple user accounts.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CloudDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 6763785773295846295L;

	/** Cancel button. */
	private final JButton btnCancel;
	/** Confirmation button. */
	private final JButton btnOk;
	/** List with user accounts. */
	private final JList<AccountData> accountList;
	/** Label with description for the account list. */
	private final JLabel lblAccount;
	/** Label with message for the user. */
	private final JLabel lblMessage;
	/** Panel for holding buttons. */
	private final JPanel buttonPanel;
	/** Panel for choosing cloud storage service providers. */
	private final JPanel accountPanel;
	/** Panel for user message. */
	private final JPanel messagePanel;

	/** Return code from the dialog. */
	private int option;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param title Dialog title.
	 * @param accountManager Account manager.
	 * @param message Message for the user.
	 */
	public CloudDialog(Frame parent, String title, AccountManager accountManager, String message) {
		this.option = JOptionPane.DEFAULT_OPTION;

		lblAccount = new JLabel("Accounts:");
		lblAccount.setBorder(new EmptyBorder(2, 0, 0, 8));
		lblAccount.setVerticalAlignment(JLabel.TOP);
		CloudDialogListCellRenderer renderer = new CloudDialogListCellRenderer(new Font(lblAccount.getFont().getFontName(), Font.BOLD, lblAccount.getFont().getSize()), lblAccount.getFont());
		DefaultListModel<AccountData> model = new DefaultListModel<>();
		for (AccountSettings account: accountManager.getAllAccountSettings()) {
			if (account.isAuthorized()) {
				AccountData data = new AccountData();
				data.setName(account.getAccountId());
				data.setCloud(account.getSettingsId());
				model.addElement(data);
			}
		}
		accountList = new JList<>();
		accountList.setVisibleRowCount(-1);
		accountList.setCellRenderer(renderer);
		accountList.setModel(model);
		accountList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		accountList.setSelectionInterval(0, model.getSize() - 1);
		JScrollPane accountPane = new JScrollPane();
		accountPane.setPreferredSize(new Dimension(300, 180));
		accountPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		accountPane.setViewportView(accountList);
		JLabel lblEmpty = new JLabel();
		lblEmpty.setPreferredSize(lblAccount.getPreferredSize());
		lblMessage = new JLabel(message);
		accountPanel = new JPanel(new BorderLayout());
		accountPanel.setBorder(new EmptyBorder(8, 8, 0, 8));
		accountPanel.add(lblAccount, BorderLayout.WEST);
		accountPanel.add(accountPane, BorderLayout.CENTER);
		messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		messagePanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		messagePanel.add(lblEmpty);
		messagePanel.add(lblMessage);
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
		buttonPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
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
		add(messagePanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Returns the return code from the dialog.
	 * @return Return code from the dialog.
	 */
	public int getOption() {
		return option;
	}

	/**
	 * Returns the selected accounts.
	 * @return Selected accounts.
	 */
	public AccountData[] getSelectedAccounts() {
		int[] indices = accountList.getSelectedIndices();
		AccountData[] selected = new AccountData[indices.length];
		for (int i = 0; i < indices.length; i++) {
			selected[i] = accountList.getModel().getElementAt(indices[i]);
		}
		return selected;
	}

}
