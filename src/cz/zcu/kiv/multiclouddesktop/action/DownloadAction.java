package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.OverwriteFileChooser;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/DownloadAction.java			<br /><br />
 *
 * Action for downloading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class DownloadAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -8641750110144077902L;

	/** Name of the action. */
	public static final String ACT_NAME = "Download";
	/** File chooser. */
	private final OverwriteFileChooser chooser;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public DownloadAction(MultiCloudDesktop parent) {
		super(parent);
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
				chooser.setCurrentDirectory(new File(parent.getPreferences().getFolder()));
				chooser.setSelectedFile(new File(file.getName()));
				int option = chooser.showSaveDialog(parent);
				switch (option) {
				case JFileChooser.APPROVE_OPTION:
					File target = chooser.getSelectedFile();
					ProgressDialog dialog = new ProgressDialog(parent, parent.getProgressListener().getComponents(), ACT_NAME);
					parent.actionDownload(file, target, chooser.isOverwrite(), dialog);
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
					parent.getMessageCallback().displayMessage("Download canceled.");
					break;
				}
			}
		}
	}

}
