package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/UploadAction.java			<br /><br />
 *
 * Action for uploading a file to the cloud storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class UploadAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -197711305278596976L;

	/** Name of the action. */
	public static final String ACT_NAME = "Upload";

	/** File chooser. */
	private final JFileChooser chooser;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public UploadAction(MultiCloudDesktop parent) {
		super(parent);
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (parent.getCurrentAccount() == null) {
			parent.getMessageCallback().displayError("No account selected.");
		} else {
			chooser.setCurrentDirectory(new File(parent.getPreferences().getFolder()));
			int option = chooser.showOpenDialog(parent);
			switch (option) {
			case JFileChooser.APPROVE_OPTION:
				boolean update = false;
				File file = chooser.getSelectedFile();
				FileInfo remote = parent.getCurrentFolder();
				FileInfo existing = null;
				for (FileInfo content: remote.getContent()) {
					if (content.getFileType() == FileType.FILE && content.getName().equals(file.getName())) {
						existing = content;
						option = JOptionPane.showConfirmDialog(parent, "Do you want to overwrite the remote file?", ACT_NAME, JOptionPane.YES_NO_OPTION);
						switch (option) {
						case JOptionPane.YES_OPTION:
							update = true;
							break;
						case JOptionPane.NO_OPTION:
						case JOptionPane.CLOSED_OPTION:
						default:
							update = false;
							if (!parent.getPreferences().isUploadNoOverwrite()) {
								parent.getMessageCallback().displayMessage("Upload cancelled - overwriting not allowed.");
								return;
							}
							break;
						}
					}
				}
				ProgressDialog dialog = new ProgressDialog(parent, parent.getProgressListener().getComponents(), ACT_NAME);
				parent.actionUpload(file, dialog, existing, update);
				dialog.setVisible(true);
				if (dialog.isAborted()) {
					parent.actionAbort();
				}
				break;
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Upload cancelled.");
				break;
			}
		}
	}

}
