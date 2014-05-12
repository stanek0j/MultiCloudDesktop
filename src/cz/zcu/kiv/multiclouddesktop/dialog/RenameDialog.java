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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;

public class RenameDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 4461519094704491214L;

	/** Cancel button. */
	private final JButton btnCancel;
	/** Confirmation button. */
	private final JButton btnOk;
	/** Label with description for name text field. */
	private final JLabel lblName;
	/** Label for invalid name error response. */
	private final JLabel lblNameErr;
	/** Panel for holding buttons. */
	private final JPanel buttonPanel;
	/** Panel for displaying error on entering name. */
	private final JPanel nameErrPanel;
	/** Panel for entering name. */
	private final JPanel namePanel;
	/** Text field for entering name. */
	private final JTextField txtName;

	/** Return code from the dialog. */
	private int option;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param title Dialog title.
	 * @param file File to be renamed.
	 */
	public RenameDialog(Frame parent, String title, FileInfo file) {
		this.option = JOptionPane.DEFAULT_OPTION;

		if (file != null && file.getFileType() == FileType.FILE) {
			lblName = new JLabel("File name:");
		} else {
			lblName = new JLabel("Folder name:");
		}
		lblName.setBorder(new EmptyBorder(0, 0, 0, 8));
		txtName = new JTextField();
		txtName.setPreferredSize(new Dimension(200, txtName.getPreferredSize().height));
		if (file != null) {
			txtName.setText(file.getName());
		}
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
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Returns the entered file name.
	 * @return File name.
	 */
	public String getFileName() {
		return txtName.getText();
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
		/* empty name */
		if (txtName.getText().trim().isEmpty()) {
			lblNameErr.setText("Name cannot be empty.");
			valid = false;
		}
		return valid;
	}

}
