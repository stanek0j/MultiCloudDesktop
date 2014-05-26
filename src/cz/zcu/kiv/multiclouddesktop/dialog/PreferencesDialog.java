package cz.zcu.kiv.multiclouddesktop.dialog;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multiclouddesktop.data.ListDisplayType;
import cz.zcu.kiv.multiclouddesktop.data.Preferences;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/PreferencesDialog.java			<br /><br />
 *
 * Dialog for setting up user preferences.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class PreferencesDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = -5676604829122435456L;

	/** Cancel button. */
	private final JButton btnCancel;
	/** Confirmation button. */
	private final JButton btnOk;
	/** Check box for displaying checksum dialog. */
	private final JCheckBox chckShowChecksumDialog;
	/** Check box for showing deleted files. */
	private final JCheckBox chckShowDeleted;
	/** Check box for displaying error dialogs. */
	private final JCheckBox chckShowErrorDialog;
	/** Check box for showing shared files. */
	private final JCheckBox chckShowShared;
	/** Check box for uploading file without overwrite. */
	private final JCheckBox chckUploadNoOverwrite;
	/** Combo box for choosing list display type. */
	private final JComboBox<String> cmbDisplayType;
	/** Label with description for combo box. */
	private final JLabel lblDisplayType;
	/** Label with description for spinner. */
	private final JLabel lblThreads;
	/** Panel for holding buttons. */
	private final JPanel buttonPanel;
	/** Panel for choosing list display type. */
	private final JPanel displayTypePanel;
	/** Panel for holding checksum dialog check box. */
	private final JPanel showChecksumDialogPanel;
	/** Panel for holding deleted check box. */
	private final JPanel showDeletedPanel;
	/** Panel for holding error dialog check box. */
	private final JPanel showErrorDialogPanel;
	/** Panel for holding shared check box. */
	private final JPanel showSharedPanel;
	/** Panel for holding upload check box. */
	private final JPanel uploadNoOverwritePanel;
	/** Panel for choosing number of threads per account. */
	private final JPanel threadsPanel;
	/** Spinner for selecting number of threads per account. */
	private final JSpinner spnThreads;

	/** Preferences. */
	private final Preferences prefs;
	/** Return code from the dialog. */
	private int option;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param title Dialog title.
	 * @param preferences Preferences to be edited.
	 */
	public PreferencesDialog(Frame parent, String title, Preferences preferences) {
		this.option = JOptionPane.DEFAULT_OPTION;
		this.prefs = preferences;

		lblDisplayType = new JLabel("File display type:");
		lblDisplayType.setBorder(new EmptyBorder(0, 0, 0, 8));
		cmbDisplayType = new JComboBox<>();
		cmbDisplayType.setPreferredSize(new Dimension(400, cmbDisplayType.getPreferredSize().height));
		for (ListDisplayType type: ListDisplayType.values()) {
			cmbDisplayType.addItem(type.getText());
		}
		lblThreads = new JLabel("Threads per account:");
		lblThreads.setBorder(new EmptyBorder(0, 0, 0, 8));
		spnThreads = new JSpinner();
		spnThreads.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		int labelWidth = (lblDisplayType.getPreferredSize().width > lblThreads.getPreferredSize().width) ? lblDisplayType.getPreferredSize().width : lblThreads.getPreferredSize().width;
		lblDisplayType.setPreferredSize(new Dimension(labelWidth, lblDisplayType.getPreferredSize().height));
		lblThreads.setPreferredSize(new Dimension(labelWidth, lblThreads.getPreferredSize().height));
		displayTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		displayTypePanel.setBorder(new EmptyBorder(8, 8, 2, 8));
		displayTypePanel.add(lblDisplayType);
		displayTypePanel.add(cmbDisplayType);
		threadsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		threadsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		threadsPanel.add(lblThreads);
		threadsPanel.add(spnThreads);
		chckShowDeleted = new JCheckBox("Show deleted files.");
		showDeletedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showDeletedPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceDeleted = new JLabel();
		lblSpaceDeleted.setPreferredSize(new Dimension(labelWidth, lblSpaceDeleted.getPreferredSize().height));
		showDeletedPanel.add(lblSpaceDeleted);
		showDeletedPanel.add(chckShowDeleted);
		chckShowShared = new JCheckBox("Show shared files.");
		showSharedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showSharedPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceShared = new JLabel();
		lblSpaceShared.setPreferredSize(new Dimension(labelWidth, lblSpaceShared.getPreferredSize().height));
		showSharedPanel.add(lblSpaceShared);
		showSharedPanel.add(chckShowShared);
		chckShowErrorDialog = new JCheckBox("Pop up error dialogs.");
		showErrorDialogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showErrorDialogPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceErrorDialog = new JLabel();
		lblSpaceErrorDialog.setPreferredSize(new Dimension(labelWidth, lblSpaceErrorDialog.getPreferredSize().height));
		showErrorDialogPanel.add(lblSpaceErrorDialog);
		showErrorDialogPanel.add(chckShowErrorDialog);
		chckShowChecksumDialog = new JCheckBox("Pop up confirmation dialog for computing checksum.");
		showChecksumDialogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showChecksumDialogPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceChecksumDialog = new JLabel();
		lblSpaceChecksumDialog.setPreferredSize(new Dimension(labelWidth, lblSpaceChecksumDialog.getPreferredSize().height));
		showChecksumDialogPanel.add(lblSpaceChecksumDialog);
		showChecksumDialogPanel.add(chckShowChecksumDialog);
		chckUploadNoOverwrite = new JCheckBox("Upload file even if overwrite was not selected (only for single upload).");
		uploadNoOverwritePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		uploadNoOverwritePanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceUpload = new JLabel();
		lblSpaceUpload.setPreferredSize(new Dimension(labelWidth, lblSpaceUpload.getPreferredSize().height));
		uploadNoOverwritePanel.add(lblSpaceUpload);
		uploadNoOverwritePanel.add(chckUploadNoOverwrite);
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

		add(displayTypePanel);
		add(threadsPanel);
		add(showDeletedPanel);
		add(showSharedPanel);
		add(showErrorDialogPanel);
		add(showChecksumDialogPanel);
		add(uploadNoOverwritePanel);
		add(buttonPanel);

		cmbDisplayType.setSelectedItem(prefs.getDisplayType().getText());
		spnThreads.setValue(prefs.getThreadsPerAccount());
		chckShowDeleted.setSelected(prefs.isShowDeleted());
		chckShowShared.setSelected(prefs.isShowShared());
		chckShowErrorDialog.setSelected(prefs.isShowErrorDialog());
		chckShowChecksumDialog.setSelected(prefs.isShowChecksumDialog());
		chckUploadNoOverwrite.setSelected(prefs.isUploadNoOverwrite());

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
	 * Returns the preferences.
	 * @return Preferences.
	 */
	public Preferences getPreferences() {
		prefs.setDisplayType(ListDisplayType.fromString((String) cmbDisplayType.getSelectedItem()));
		prefs.setThreadsPerAccount((int) spnThreads.getValue());
		prefs.setShowDeleted(chckShowDeleted.isSelected());
		prefs.setShowShared(chckShowShared.isSelected());
		prefs.setShowErrorDialog(chckShowErrorDialog.isSelected());
		prefs.setShowChecksumDialog(chckShowChecksumDialog.isSelected());
		prefs.setUploadNoOverwrite(chckUploadNoOverwrite.isSelected());
		return prefs;
	}

}
