package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.json.CloudSettings;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

public class AccountDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 6531785115255798245L;

	/** Cancel button. */
	private final JButton btnCancel;
	/** Confirmation button. */
	private final JButton btnOk;
	/** Combo box for choosing cloud storage service provider. */
	private final JComboBox<CloudSettings> cmbCloud;
	/** Label with description for combo box. */
	private final JLabel lblCloud;
	/** Label for invalid combo box selection error response. */
	private final JLabel lblCloudErr;
	/** Label with description for name text field. */
	private final JLabel lblName;
	/** Label for invalid name error response. */
	private final JLabel lblNameErr;
	/** Panel for holding buttons. */
	private final JPanel buttonPanel;
	/** Panel for displaying error on combo box. */
	private final JPanel cloudErrPanel;
	/** Panel for choosing cloud storage service provider. */
	private final JPanel cloudPanel;
	/** Panel for displaying error on entering name. */
	private final JPanel nameErrPanel;
	/** Panel for entering name. */
	private final JPanel namePanel;
	/** Text field for entering name. */
	private final JTextField txtName;

	/** Account manager. */
	private final AccountManager accountManager;

	/** Account to be edited. */
	private AccountData account;
	/** Return code from the dialog. */
	private int option;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param title Dialog title.
	 * @param accountManager Account manager.
	 * @param cloudManager Cloud manager.
	 * @param account Account to be edited.
	 */
	public AccountDialog(Frame parent, String title, AccountManager accountManager, CloudManager cloudManager, AccountData account) {
		this.accountManager = accountManager;
		this.account = account;
		this.option = JOptionPane.DEFAULT_OPTION;

		lblName = new JLabel("Account name:");
		lblName.setBorder(new EmptyBorder(0, 0, 0, 8));
		txtName = new JTextField();
		txtName.setPreferredSize(new Dimension(200, txtName.getPreferredSize().height));
		lblCloud = new JLabel("Cloud storage:");
		lblCloud.setBorder(new EmptyBorder(0, 0, 0, 8));
		cmbCloud = new JComboBox<>();
		cmbCloud.setPreferredSize(new Dimension(200, cmbCloud.getPreferredSize().height));
		for (CloudSettings cs: cloudManager.getAllCloudSettings()) {
			cmbCloud.addItem(cs);
		}
		if (account != null) {
			txtName.setText(account.getName());
			cmbCloud.setSelectedItem(cloudManager.getCloudSettings(account.getCloud()));
			cmbCloud.setEnabled(false);
		}
		int labelWidth = (lblName.getPreferredSize().width > lblCloud.getPreferredSize().width) ? lblName.getPreferredSize().width : lblCloud.getPreferredSize().width;
		lblName.setPreferredSize(new Dimension(labelWidth, lblName.getPreferredSize().height));
		lblCloud.setPreferredSize(new Dimension(labelWidth, lblCloud.getPreferredSize().height));
		namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		namePanel.setBorder(new EmptyBorder(8, 8, 0, 8));
		namePanel.add(lblName);
		namePanel.add(txtName);
		lblNameErr = new JLabel();
		lblNameErr.setBorder(new EmptyBorder(0, 4, 0, 0));
		lblNameErr.setForeground(Color.RED);
		lblNameErr.setPreferredSize(new Dimension(200, lblName.getPreferredSize().height));
		nameErrPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		nameErrPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		nameErrPanel.add(lblNameErr);
		cloudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		cloudPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		cloudPanel.add(lblCloud);
		cloudPanel.add(cmbCloud);
		lblCloudErr = new JLabel();
		lblCloudErr.setBorder(new EmptyBorder(0, 4, 0, 0));
		lblCloudErr.setForeground(Color.RED);
		lblCloudErr.setPreferredSize(new Dimension(200, lblCloud.getPreferredSize().height));
		cloudErrPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		cloudErrPanel.setBorder(new EmptyBorder(2, 8, 4, 8));
		cloudErrPanel.add(lblCloudErr);
		btnOk = new JButton("OK");
		btnOk.setMargin(new Insets(4, 20, 4, 20));
		btnOk.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				if (validateFields()) {
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
		buttonPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		buttonPanel.add(btnOk);
		buttonPanel.add(btnCancel);

		setTitle(title);
		setAlwaysOnTop(true);
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

		add(namePanel);
		add(nameErrPanel);
		add(cloudPanel);
		add(cloudErrPanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Returns the edited account.
	 * @return Edited account.
	 */
	public AccountData getAccountData() {
		return account;
	}

	/**
	 * Returns the return code from the dialog.
	 * @return Return code from the dialog.
	 */
	public int getOption() {
		return option;
	}

	/**
	 * Validates the input data from the user.
	 * @return If the data passed the validation.
	 */
	private boolean validateFields() {
		boolean valid = true;
		lblNameErr.setText(null);
		lblCloudErr.setText(null);
		/* empty name */
		if (txtName.getText().trim().isEmpty()) {
			lblNameErr.setText("Account name cannot be empty.");
			valid = false;
		}
		/* existing name */
		if (accountManager.getIdentifiers().contains(txtName.getText())) {
			lblNameErr.setText("Account name already taken.");
			valid = false;
		}
		CloudSettings cloud = (CloudSettings) cmbCloud.getSelectedItem();
		/* no cloud selected */
		if (cloud == null) {
			lblCloudErr.setText("No cloud storage selected.");
			valid = false;
		}
		/* valid */
		if (valid) {
			if (account != null) {
				AccountData edited = new AccountData();
				edited.setName(txtName.getText());
				edited.setCloud(account.getCloud());
				edited.setTotalSpace(account.getTotalSpace());
				edited.setFreeSpace(account.getFreeSpace());
				edited.setUsedSpace(account.getUsedSpace());
				edited.setAuthorized(account.isAuthorized());
				account = edited;
			} else {
				account = new AccountData();
				account.setName(txtName.getText());
				account.setCloud(cloud.getSettingsId());
			}
		}
		return valid;
	}

}
