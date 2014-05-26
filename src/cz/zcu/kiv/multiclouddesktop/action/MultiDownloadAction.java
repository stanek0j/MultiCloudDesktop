package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.dialog.CloudDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.OverwriteFileChooser;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.SearchDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/MultiDownloadAction.java			<br /><br />
 *
 * Action for downloading file from multiple sources.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiDownloadAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 7738755962573524993L;

	/** Name of the action. */
	public static final String ACT_NAME = "Multi download";

	/** File chooser. */
	private final OverwriteFileChooser chooser;
	/** Folder icon. */
	private final ImageIcon icnFolder;
	/** File icon. */
	private final ImageIcon icnFile;
	/** Bad file icon. */
	private final ImageIcon icnBadFile;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param icnFolder Folder icon.
	 * @param icnFile File icon.
	 * @param icnBadFile Bad file icon.
	 */
	public MultiDownloadAction(MultiCloudDesktop parent, ImageIcon icnFolder, ImageIcon icnFile, ImageIcon icnBadFile) {
		super(parent);
		this.icnFolder = icnFolder;
		this.icnFile = icnFile;
		this.icnBadFile = icnBadFile;
		chooser = new OverwriteFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		FileInfo file = parent.getDataList().getSelectedValue();
		if (file == null) {
			parent.getMessageCallback().displayError("No file selected.");
		} else {
			if (file.getFileType() == FileType.FOLDER) {
				JOptionPane.showMessageDialog(parent, "Select a file.", ACT_NAME, JOptionPane.ERROR_MESSAGE);
			} else {
				int option = 0;
				/* select clouds */
				AccountData[] accounts = null;
				CloudDialog cDialog = new CloudDialog(parent, ACT_NAME, parent.getAccountManager(), "Choose accounts to download the file from.");
				cDialog.setVisible(true);
				option = cDialog.getOption();
				switch (option) {
				case JOptionPane.OK_OPTION:
					accounts = cDialog.getSelectedAccounts();
					break;
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					parent.getMessageCallback().displayMessage("Download cancelled.");
					return;
				}
				if (accounts == null || accounts.length == 0) {
					parent.getMessageCallback().displayError("No source accounts selected.");
					return;
				}
				/* match file to listed cloud */
				for (int i = 0; i < parent.getAccountList().getModel().getSize(); i++) {
					AccountData account = parent.getAccountList().getModel().getElementAt(i);
					account.setMatched(false);
					if (account.isListed()) {
						account.setMatched(true);
						for (int j = 0; j < accounts.length; j++) {
							if (accounts[j].getName().equals(account.getName())) {
								accounts[j].setMatched(true);
							}
						}
					}
				}
				/* find matches on other clouds */
				SearchDialog sDialog = new SearchDialog(parent, ACT_NAME, file, null, accounts, icnFolder, icnFile, icnBadFile);
				sDialog.setVisible(true);
				option = sDialog.getOption();
				switch (option) {
				case JOptionPane.OK_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					parent.getMessageCallback().displayMessage("Download cancelled.");
					return;
				}
				/* confirm use of non-matching sources for file download, if possible */
				boolean matched = true;
				boolean possible = true;
				if (file.getChecksum() != null) {
					for (FileInfo f: sDialog.getOutput()) {
						if (f != null) {
							if (f.getChecksum() != null) {
								if (!file.getChecksum().equals(f.getChecksum())) {
									matched = false;
									break;
								}
							} else {
								matched = false;
								break;
							}
						}
					}
				} else {
					String lastChecksum = null;
					for (FileInfo f: sDialog.getOutput()) {
						if (f.getChecksum() != null) {
							if (lastChecksum == null) {
								lastChecksum = f.getChecksum();
							} else {
								if (!f.getChecksum().equals(lastChecksum)) {
									possible = false;
									break;
								}
							}
						}
					}
					matched = false;
				}
				if (possible) {
					if (!matched) {
						option = JOptionPane.showConfirmDialog(parent, "Selected source files may not match. Downloading them into single file may result in corrupted file.\nTo prevent this issue, compute the checksum for all the source files first.\nDo you wish to continue with the download anyway?", ACT_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						switch (option) {
						case JOptionPane.YES_OPTION:
							break;
						case JOptionPane.NO_OPTION:
						case JOptionPane.CLOSED_OPTION:
						default:
							parent.getMessageCallback().displayMessage("Download cancelled.");
							return;
						}
					}
				} else {
					JOptionPane.showMessageDialog(parent, "Some of the selected files contain non-matching checksums.\nDownload from multiple sources not possible.", ACT_NAME, JOptionPane.ERROR_MESSAGE);
					return;
				}
				/* choose local file and download */
				chooser.setCurrentDirectory(new File(parent.getPreferences().getFolder()));
				chooser.setSelectedFile(new File(file.getName()));
				option = chooser.showSaveDialog(parent);
				switch (option) {
				case JFileChooser.APPROVE_OPTION:
					File target = chooser.getSelectedFile();
					ProgressDialog dialog = new ProgressDialog(parent, parent.getProgressListener().getComponents(), ACT_NAME);
					String[] accountNames = new String[accounts.length];
					for (int i = 0; i < accounts.length; i++) {
						accountNames[i] = accounts[i].getName();
					}
					parent.actionMultiDownload(accountNames, sDialog.getOutput(), target, chooser.isOverwrite(), dialog);
					dialog.setVisible(true);
					if (dialog.isAborted()) {
						parent.actionAbort();
						if (!chooser.isOverwrite()) {
							target.delete();
						}
					}
					break;
				case JFileChooser.CANCEL_OPTION:
				case JFileChooser.ERROR_OPTION:
				default:
					parent.getMessageCallback().displayMessage("Download cancelled.");
					return;
				}
			}
		}
	}

}
