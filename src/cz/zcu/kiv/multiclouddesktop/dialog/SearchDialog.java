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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.SearchCallback;
import cz.zcu.kiv.multiclouddesktop.data.SearchDialogListCellRenderer;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/SearchDialog.java			<br /><br />
 *
 * Dialog for searching for files.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SearchDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = -4804883317598301581L;

	/** Abort button. */
	private final JButton btnAbort;
	/** Cancel button. */
	private final JButton btnCancel;
	/** Find button. */
	private final JButton btnFind;
	/** Confirmation button. */
	private final JButton btnOk;
	/** Combo box for choosing account. */
	private final JComboBox<AccountData> cmbAccount;
	/** Label with description for combo box. */
	private final JLabel lblAccount;
	/** Label with description for query text field. */
	private final JLabel lblQuery;
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
	/** Panel for entering search query. */
	private final JPanel queryPanel;
	/** Panel for displaying results. */
	private final JPanel resultsPanel;
	/** Panel for displaying status. */
	private final JPanel statusPanel;
	/** Text field for entering search query. */
	private final JTextField txtQuery;

	/** Parent frame. */
	private final MultiCloudDesktop parent;
	/** File to be matched. */
	private final FileInfo match;
	/** Search callback. */
	private final SearchCallback callback;
	/** Return code from the dialog. */
	private int option;
	/** Array of accounts to choose from. */
	private final AccountData[] accounts;
	/** File data to be returned as output. */
	private final FileInfo[] output;
	/** Output index to be skipped. */
	private int skipIndex;

	/**
	 * Ctor with necessary parameters.
	 * @param parentFrame Parent frame.
	 * @param title Dialog title.
	 * @param matchFile File to search for.
	 * @param matchString String to search for.
	 * @param selectedAccounts Accounts to be searched.
	 * @param folder Folder icon.
	 * @param file File icon.
	 * @param badFile Bad file icon.
	 */
	public SearchDialog(MultiCloudDesktop parentFrame, String title, FileInfo matchFile, String matchString, AccountData[] selectedAccounts, ImageIcon folder, ImageIcon file, ImageIcon badFile) {
		parent = parentFrame;
		this.match = matchFile;
		this.accounts = selectedAccounts;
		this.option = JOptionPane.DEFAULT_OPTION;
		this.skipIndex = -1;

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
					output[i] = matchFile;
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
				if (match != null) {
					((DefaultListModel<FileInfo>) resultsList.getModel()).clear();
				}
			}
		});
		lblQuery = new JLabel("Query:");
		lblQuery.setBorder(new EmptyBorder(0, 0, 0, 8));
		txtQuery = new JTextField();
		if (matchFile != null) {
			txtQuery.setText(matchFile.getName());
		} else if (matchString != null) {
			txtQuery.setText(matchString);
		}
		txtQuery.setPreferredSize(new Dimension(300, txtQuery.getPreferredSize().height));
		accountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		accountPanel.setBorder(new EmptyBorder(8, 8, 2, 8));
		accountPanel.add(lblAccount);
		accountPanel.add(cmbAccount);
		queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		queryPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		queryPanel.add(lblQuery);
		queryPanel.add(txtQuery);
		lblResults = new JLabel("Results:");
		lblResults.setBorder(new EmptyBorder(0, 0, 0, 8));
		lblResults.setVerticalAlignment(JLabel.TOP);
		resultsList = new JList<FileInfo>();
		resultsList.setVisibleRowCount(-1);
		resultsList.setCellRenderer(new SearchDialogListCellRenderer(matchFile, true, folder, file, badFile));
		resultsList.setModel(new DefaultListModel<FileInfo>());
		resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsList.addListSelectionListener(new ListSelectionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void valueChanged(ListSelectionEvent event) {
				int index = cmbAccount.getSelectedIndex();
				if (index > -1 && event.getValueIsAdjusting()) {
					if (index >= skipIndex && skipIndex > -1) {
						index++;
					}
					output[index] = resultsList.getSelectedValue();
					AccountData selected = (AccountData) cmbAccount.getSelectedItem();
					if (selected != null && match != null && accounts != null) {
						selected.setMatched(true);
						cmbAccount.revalidate();
						cmbAccount.repaint();
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
		lblStatusText = new JLabel("Enter search query.");
		lblStatusText.setBorder(new EmptyBorder(0, 4, 0, 4));
		statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		statusPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		statusPanel.add(lblStatus);
		statusPanel.add(lblStatusText);
		int labelWidth = (lblQuery.getPreferredSize().width > lblAccount.getPreferredSize().width) ? lblQuery.getPreferredSize().width : lblAccount.getPreferredSize().width;
		labelWidth = (labelWidth > lblResults.getPreferredSize().width) ? labelWidth : lblResults.getPreferredSize().width;
		labelWidth = (labelWidth > lblStatus.getPreferredSize().width) ? labelWidth : lblStatus.getPreferredSize().width;
		lblQuery.setPreferredSize(new Dimension(labelWidth, lblQuery.getPreferredSize().height));
		lblAccount.setPreferredSize(new Dimension(labelWidth, lblAccount.getPreferredSize().height));
		lblResults.setPreferredSize(new Dimension(labelWidth, lblResults.getPreferredSize().height));
		lblStatus.setPreferredSize(new Dimension(labelWidth, lblStatus.getPreferredSize().height));
		callback = new SearchCallback(matchFile, this);
		btnFind = new JButton("Find");
		btnFind.setMargin(new Insets(4, 20, 4, 20));
		btnFind.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				if (validateFields()) {
					AccountData account = (AccountData) cmbAccount.getSelectedItem();
					lblStatusText.setForeground(Color.BLACK);
					lblStatusText.setText("Searching...");
					cmbAccount.setEnabled(false);
					txtQuery.setEnabled(false);
					btnFind.setEnabled(false);
					btnOk.setEnabled(false);
					btnCancel.setEnabled(false);
					if (!parent.actionSearch(account.getName(), txtQuery.getText(), callback)) {
						finishSearch("Wait for other operation to finish.");
					}
				}
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
				finishSearch("Aborted.");
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
		buttonPanel.add(btnFind);
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
		add(queryPanel);
		add(resultsPanel);
		add(statusPanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parentFrame);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Enables all the fields for filling in the next request.
	 * @param result Text describing the result.
	 */
	public synchronized void finishSearch(String result) {
		if (result == null) {
			lblStatusText.setForeground(Color.RED);
			lblStatusText.setText("Search failed.");
		} else {
			lblStatusText.setForeground(Color.BLACK);
			lblStatusText.setText(result);
		}
		cmbAccount.setEnabled(true);
		txtQuery.setEnabled(true);
		btnFind.setEnabled(true);
		btnOk.setEnabled(true);
		btnCancel.setEnabled(true);
		resultsList.revalidate();
		resultsList.repaint();
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
	 * Returns the file data gathered.
	 * @return File data gathered.
	 */
	public FileInfo[] getOutput() {
		return output;
	}

	/**
	 * Validates the input data from the user.
	 * @return If the data passed the validation.
	 */
	private boolean validateFields() {
		boolean valid = true;
		/* empty query string */
		if (txtQuery.getText().trim().isEmpty()) {
			lblStatusText.setForeground(Color.RED);
			lblStatusText.setText("Search query cannot be empty.");
			valid = false;
		}
		return valid;
	}

	/**
	 * Validates the stored data for output.
	 * @return If the stored data passed the validation.
	 */
	private boolean validateOutput() {
		boolean valid = true;
		if (match != null) {
			valid = false;
			for (FileInfo out: output) {
				if (out != null) {
					valid = true;
				}
			}
		}
		return valid;
	}

}
