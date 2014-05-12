package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

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
	 * @param folder Default folder.
	 */
	public UploadAction(MultiCloudDesktop parent, File folder) {
		super(parent);
		chooser = new JFileChooser(folder);
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
			int option = chooser.showOpenDialog(parent);
			switch (option) {
			case JFileChooser.APPROVE_OPTION:
				File file = chooser.getSelectedFile();
				ProgressDialog dialog = new ProgressDialog(parent, parent.getProgressListener().getComponents(), ACT_NAME);
				parent.actionUpload(file, dialog);
				dialog.setVisible(true);
				if (dialog.isAborted()) {
					parent.actionAbort();
				}
				break;
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Upload canceled.");
				break;
			}
		}
	}

}
