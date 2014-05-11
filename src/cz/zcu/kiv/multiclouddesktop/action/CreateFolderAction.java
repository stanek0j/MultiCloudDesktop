package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.RenameDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/CreateFolderAction.java			<br /><br />
 *
 * Action for creating a new folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CreateFolderAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -2686800695894550816L;

	/** Name of the action. */
	public static final String ACT_NAME = "Create folder";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public CreateFolderAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		RenameDialog dialog = new RenameDialog(parent, ACT_NAME, null);
		dialog.setVisible(true);
		switch (dialog.getOption()) {
		case JOptionPane.OK_OPTION:
			parent.actionCreateFolder(dialog.getFileName());
			break;
		case JOptionPane.CANCEL_OPTION:
		case JOptionPane.CLOSED_OPTION:
		default:
			parent.getMessageCallback().displayMessage("Folder creation canceled.");
			break;
		}
	}

}
