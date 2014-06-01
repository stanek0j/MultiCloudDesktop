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
import cz.zcu.kiv.multiclouddesktop.dialog.BrowseDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.CloudDialog;
import cz.zcu.kiv.multiclouddesktop.dialog.DialogProgressListener;
import cz.zcu.kiv.multiclouddesktop.dialog.OverwriteFileChooser;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/MultiUploadAction.java			<br /><br />
 *
 * Action for uploading a file to multiple destinations.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiUploadAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 8019781061456880099L;

	/** Name of the action. */
	public static final String ACT_NAME = "Multi upload";

	/** File chooser. */
	private final JFileChooser chooser;
	/** Folder icon. */
	private final ImageIcon icnFolder;
	/** File icon. */
	private final ImageIcon icnFile;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param icnFolder Folder icon.
	 * @param icnFile File icon.
	 */
	public MultiUploadAction(MultiCloudDesktop parent, ImageIcon icnFolder, ImageIcon icnFile) {
		super(parent);
		this.icnFolder = icnFolder;
		this.icnFile = icnFile;
		chooser = new OverwriteFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (parent.getCurrentFolder() == null) {
			parent.getMessageCallback().displayError("No destination folder selected.");
			return;
		}
		/* choose local file for upload */
		File file = null;
		chooser.setCurrentDirectory(new File(parent.getPreferences().getFolder()));
		int option = chooser.showOpenDialog(parent);
		switch (option) {
		case JFileChooser.APPROVE_OPTION:
			file = chooser.getSelectedFile();
			break;
		case JFileChooser.CANCEL_OPTION:
		case JFileChooser.ERROR_OPTION:
		default:
			parent.getMessageCallback().displayMessage("Upload cancelled.");
			return;
		}
		/* select clouds */
		AccountData[] accounts = null;
		CloudDialog cDialog = new CloudDialog(parent, ACT_NAME, parent.getAccountManager(), "Choose accounts to upload the file to.");
		cDialog.setVisible(true);
		option = cDialog.getOption();
		switch (option) {
		case JOptionPane.OK_OPTION:
			accounts = cDialog.getSelectedAccounts();
			break;
		case JOptionPane.CANCEL_OPTION:
		case JOptionPane.CLOSED_OPTION:
		default:
			parent.getMessageCallback().displayMessage("Upload cancelled.");
			return;
		}
		if (accounts == null || accounts.length == 0) {
			parent.getMessageCallback().displayError("No destination accounts selected.");
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
		/* select folders on other clouds */
		boolean allMatched = true;
		for (AccountData account: accounts) {
			if (!account.isMatched()) {
				allMatched = false;
			}
		}
		FileInfo[] output = new FileInfo[accounts.length];
		if (!allMatched) {
			BrowseDialog bDialog = new BrowseDialog(parent, ACT_NAME, parent.getCurrentFolder(), accounts, icnFolder, icnFile);
			bDialog.setVisible(true);
			option = bDialog.getOption();
			switch (option) {
			case JOptionPane.OK_OPTION:
				output = bDialog.getOutput();
				break;
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Upload cancelled.");
				return;
			}
		}
		FileInfo[] existing = new FileInfo[output.length];
		for (int i = 0; i < output.length; i++) {
			existing[i] = null;
			if (output[i] != null) {
				for (FileInfo content: output[i].getContent()) {
					if (content.getFileType() == FileType.FILE && content.getName().equals(file.getName())) {
						String msg = "File '" + file.getName() + "' already exists at the specified location of " + accounts[i].getName() + ".\nDo you want to overwrite the remote file?";
						option = JOptionPane.showConfirmDialog(parent, msg, ACT_NAME, JOptionPane.YES_NO_OPTION);
						switch (option) {
						case JOptionPane.YES_OPTION:
							existing[i] = content;
							break;
						case JOptionPane.NO_OPTION:
						case JOptionPane.CLOSED_OPTION:
						default:
							output[i] = null;
							break;
						}
					}
				}
			}
		}
		DialogProgressListener listener = parent.getProgressListener();
		listener.setDivisor(accounts.length);
		ProgressDialog dialog = new ProgressDialog(parent, listener.getComponents(), ACT_NAME);
		String[] accountNames = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) {
			accountNames[i] = accounts[i].getName();
			if (allMatched) {
				output[i] = parent.getCurrentFolder();
			}
		}
		parent.actionMultiUpload(accountNames, output, existing, file, dialog);
		dialog.setVisible(true);
		if (dialog.isAborted()) {
			parent.actionAbort();
		}
		listener.setDivisor(1);
	}

}
