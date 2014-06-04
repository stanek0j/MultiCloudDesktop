package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.ListDisplayType;
import cz.zcu.kiv.multiclouddesktop.data.Preferences;
import cz.zcu.kiv.multiclouddesktop.data.SyncData;

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
	/** Button for selecting content synchronization. */
	private final JButton btnSyncContent;
	/** Button for choosing local synchronization folder. */
	private final JButton btnSyncFolder;
	/** Check box for displaying checksum dialog. */
	private final JCheckBox chckShowChecksumDialog;
	/** Check box for showing deleted files. */
	private final JCheckBox chckShowDeleted;
	/** Check box for displaying error dialogs. */
	private final JCheckBox chckShowErrorDialog;
	/** Check box for showing shared files. */
	private final JCheckBox chckShowShared;
	/** Check box for hiding metadata file. */
	private final JCheckBox chckHideMetadata;
	/** Check box for uploading file without overwrite. */
	private final JCheckBox chckUploadNoOverwrite;
	/** Combo box for choosing list display type. */
	private final JComboBox<String> cmbDisplayType;
	/** File chooser. */
	private final JFileChooser chooser;
	/** Label with description for combo box. */
	private final JLabel lblDisplayType;
	/** Label with description for spinner. */
	private final JLabel lblThreads;
	/** Label with description for content synchronization button. */
	private final JLabel lblSyncContent;
	/** Label with description for text field. */
	private final JLabel lblSyncFolder;
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
	/** Panel for holding hide metadata check box. */
	private final JPanel hideMetadataPanel;
	/** Panel for holding upload check box. */
	private final JPanel uploadNoOverwritePanel;
	/** Panel for choosing number of threads per account. */
	private final JPanel threadsPanel;
	/** Panel for selecting synchronization content. */
	private final JPanel syncContentPanel;
	/** Panel for choosing local synchronization folder. */
	private final JPanel syncFolderPanel;
	/** Spinner for selecting number of threads per account. */
	private final JSpinner spnThreads;
	/** Text field for entering local synchronization folder. */
	private final JTextField txtSyncFolder;

	/** Parent frame. */
	private final MultiCloudDesktop parent;
	/** Preferences. */
	private final Preferences prefs;
	/** Synchronization data. */
	private SyncData syncData;
	/** Local synchronization folder. */
	private File syncFolder;
	/** Last synchronization folder used. */
	private File syncFolderOld;
	/** Return code from the dialog. */
	private int option;
	/** Folder icon. */
	private final ImageIcon folder;
	/** File icon. */
	private final ImageIcon file;

	/**
	 * Ctor with necessary parameters.
	 * @param parentFrame Parent frame.
	 * @param title Dialog title.
	 * @param preferences Preferences to be edited.
	 * @param icnFolder Folder icon.
	 * @param icnFile File icon.
	 */
	public PreferencesDialog(MultiCloudDesktop parentFrame, String title, Preferences preferences, ImageIcon icnFolder, ImageIcon icnFile) {
		this.option = JOptionPane.DEFAULT_OPTION;
		this.parent = parentFrame;
		this.prefs = preferences;
		this.folder = icnFolder;
		this.file = icnFile;

		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (prefs.getSyncFolder() != null) {
			syncFolder = new File(prefs.getSyncFolder());
			chooser.setCurrentDirectory(syncFolder);
		}

		lblDisplayType = new JLabel("File display type:");
		lblDisplayType.setBorder(new EmptyBorder(0, 0, 0, 8));
		cmbDisplayType = new JComboBox<>();
		cmbDisplayType.setPreferredSize(new Dimension(400, cmbDisplayType.getPreferredSize().height));
		for (ListDisplayType type: ListDisplayType.values()) {
			cmbDisplayType.addItem(type.getText());
		}
		displayTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		displayTypePanel.setBorder(new EmptyBorder(8, 8, 2, 8));
		displayTypePanel.add(lblDisplayType);
		displayTypePanel.add(cmbDisplayType);

		lblSyncFolder = new JLabel("Synchronization folder:");
		lblSyncFolder.setBorder(new EmptyBorder(0, 0, 0, 8));
		txtSyncFolder = new JTextField();
		txtSyncFolder.setMargin(new Insets(2, 4, 2, 4));
		btnSyncFolder = new JButton(folder);
		btnSyncFolder.setMargin(new Insets(0, 4, 0, 4));
		btnSyncFolder.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				if (syncFolder != null) {
					chooser.setCurrentDirectory(syncFolder);
				}
				int option = chooser.showOpenDialog(parent);
				switch (option) {
				case JFileChooser.APPROVE_OPTION:
					syncFolderOld = syncFolder;
					syncFolder = chooser.getSelectedFile();
					txtSyncFolder.setText(syncFolder.getPath());
					break;
				case JFileChooser.CANCEL_OPTION:
				case JFileChooser.ERROR_OPTION:
				default:
					break;
				}
			}
		});
		txtSyncFolder.setPreferredSize(new Dimension(400 - btnSyncFolder.getPreferredSize().width, btnSyncFolder.getPreferredSize().height - 2));
		syncFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		syncFolderPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		syncFolderPanel.add(lblSyncFolder);
		syncFolderPanel.add(txtSyncFolder);
		syncFolderPanel.add(btnSyncFolder);

		lblSyncContent = new JLabel("Content selection:");
		lblSyncContent.setBorder(new EmptyBorder(0, 0, 0, 8));
		btnSyncContent = new JButton("Select synchronization content");
		btnSyncContent.setMargin(new Insets(4, 20, 4, 20));
		btnSyncContent.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				if (validateOutput()) {
					SynchronizeDialog dialog = new SynchronizeDialog(parent, "Synchronization content", syncFolder, syncData, folder, file);
					dialog.setVisible(true);
					int option = dialog.getOption();
					switch (option) {
					case JOptionPane.OK_OPTION:
						syncData = dialog.getSyncData();
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
					default:
						break;
					}
				}
			}
		});
		syncContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		syncContentPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		syncContentPanel.add(lblSyncContent);
		syncContentPanel.add(btnSyncContent);

		lblThreads = new JLabel("Threads per account:");
		lblThreads.setBorder(new EmptyBorder(0, 0, 0, 8));
		spnThreads = new JSpinner();
		spnThreads.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		threadsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		threadsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		threadsPanel.add(lblThreads);
		threadsPanel.add(spnThreads);

		chckShowDeleted = new JCheckBox("Show deleted files.");
		showDeletedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showDeletedPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceDeleted = new JLabel();
		showDeletedPanel.add(lblSpaceDeleted);
		showDeletedPanel.add(chckShowDeleted);

		chckShowShared = new JCheckBox("Show shared files.");
		showSharedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showSharedPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceShared = new JLabel();
		showSharedPanel.add(lblSpaceShared);
		showSharedPanel.add(chckShowShared);

		chckShowErrorDialog = new JCheckBox("Pop up error dialogs.");
		showErrorDialogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showErrorDialogPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceErrorDialog = new JLabel();
		showErrorDialogPanel.add(lblSpaceErrorDialog);
		showErrorDialogPanel.add(chckShowErrorDialog);

		chckShowChecksumDialog = new JCheckBox("Pop up confirmation dialog for computing checksum.");
		showChecksumDialogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showChecksumDialogPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceChecksumDialog = new JLabel();
		showChecksumDialogPanel.add(lblSpaceChecksumDialog);
		showChecksumDialogPanel.add(chckShowChecksumDialog);

		chckHideMetadata = new JCheckBox("Hide metadata files from file list.");
		hideMetadataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		hideMetadataPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceHideMetadata = new JLabel();
		hideMetadataPanel.add(lblSpaceHideMetadata);
		hideMetadataPanel.add(chckHideMetadata);

		chckUploadNoOverwrite = new JCheckBox("Upload file even if overwrite was not selected (only for single upload).");
		uploadNoOverwritePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		uploadNoOverwritePanel.setBorder(new EmptyBorder(2, 8, 2, 8));
		JLabel lblSpaceUpload = new JLabel();
		uploadNoOverwritePanel.add(lblSpaceUpload);
		uploadNoOverwritePanel.add(chckUploadNoOverwrite);

		int labelWidth = (lblDisplayType.getPreferredSize().width > lblThreads.getPreferredSize().width) ? lblDisplayType.getPreferredSize().width : lblThreads.getPreferredSize().width;
		labelWidth = (lblSyncFolder.getPreferredSize().width > labelWidth) ? lblSyncFolder.getPreferredSize().width : labelWidth;
		labelWidth = (lblSyncContent.getPreferredSize().width > labelWidth) ? lblSyncContent.getPreferredSize().width : labelWidth;
		lblDisplayType.setPreferredSize(new Dimension(labelWidth, lblDisplayType.getPreferredSize().height));
		lblSyncFolder.setPreferredSize(new Dimension(labelWidth, lblSyncFolder.getPreferredSize().height));
		lblSyncContent.setPreferredSize(new Dimension(labelWidth, lblSyncContent.getPreferredSize().height));
		lblThreads.setPreferredSize(new Dimension(labelWidth, lblThreads.getPreferredSize().height));
		lblSpaceDeleted.setPreferredSize(new Dimension(labelWidth, lblSpaceDeleted.getPreferredSize().height));
		lblSpaceShared.setPreferredSize(new Dimension(labelWidth, lblSpaceShared.getPreferredSize().height));
		lblSpaceErrorDialog.setPreferredSize(new Dimension(labelWidth, lblSpaceErrorDialog.getPreferredSize().height));
		lblSpaceChecksumDialog.setPreferredSize(new Dimension(labelWidth, lblSpaceChecksumDialog.getPreferredSize().height));
		lblSpaceHideMetadata.setPreferredSize(new Dimension(labelWidth, lblSpaceHideMetadata.getPreferredSize().height));
		lblSpaceUpload.setPreferredSize(new Dimension(labelWidth, lblSpaceUpload.getPreferredSize().height));

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
		add(syncFolderPanel);
		add(syncContentPanel);
		add(threadsPanel);
		add(showDeletedPanel);
		add(showSharedPanel);
		add(showErrorDialogPanel);
		add(showChecksumDialogPanel);
		add(hideMetadataPanel);
		add(uploadNoOverwritePanel);
		add(buttonPanel);

		cmbDisplayType.setSelectedItem(prefs.getDisplayType().getText());
		spnThreads.setValue(prefs.getThreadsPerAccount());
		chckShowDeleted.setSelected(prefs.isShowDeleted());
		chckShowShared.setSelected(prefs.isShowShared());
		chckShowErrorDialog.setSelected(prefs.isShowErrorDialog());
		chckShowChecksumDialog.setSelected(prefs.isShowChecksumDialog());
		chckHideMetadata.setSelected(prefs.isHideMetadata());
		chckUploadNoOverwrite.setSelected(prefs.isUploadNoOverwrite());
		if (prefs.getSyncFolder() != null) {
			txtSyncFolder.setText(prefs.getSyncFolder());
		}
		syncData = prefs.getSyncData();

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
		prefs.setHideMetadata(chckHideMetadata.isSelected());
		prefs.setUploadNoOverwrite(chckUploadNoOverwrite.isSelected());
		if (txtSyncFolder.getText().trim().isEmpty()) {
			prefs.setSyncFolder(null);
		} else {
			prefs.setSyncFolder(txtSyncFolder.getText());
		}
		prefs.setSyncData(syncData);
		return prefs;
	}

	/**
	 * Validates data entered.
	 * @return If the data is valid.
	 */
	private boolean validateOutput() {
		boolean valid = true;
		if (syncFolderOld == null) {
			syncFolderOld = syncFolder;
		}
		if (txtSyncFolder.getText().trim().isEmpty()) {
			syncFolder = null;
		} else {
			syncFolder = new File(txtSyncFolder.getText().trim());
			if (!syncFolder.isDirectory()) {
				valid = false;
				JOptionPane.showMessageDialog(parent, "Synchronization folder does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				if (syncFolderOld != null && !syncFolder.equals(syncFolderOld)) {
					syncData = null;
					syncFolderOld = syncFolder;
				}
			}
		}
		return valid;
	}

}
