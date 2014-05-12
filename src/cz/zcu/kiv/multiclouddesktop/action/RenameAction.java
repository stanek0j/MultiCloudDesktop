package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.RenameDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/RenameAction.java			<br /><br />
 *
 * Action for renaming file or folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RenameAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 8727445968530461522L;

	/** Name of the action. */
	public static final String ACT_NAME = "Rename";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public RenameAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
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
			RenameDialog dialog = new RenameDialog(parent, ACT_NAME, file);
			dialog.setVisible(true);
			switch (dialog.getOption()) {
			case JOptionPane.OK_OPTION:
				parent.actionRename(dialog.getFileName(), file);
				break;
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Renaming canceled.");
				break;
			}
		}
	}

}
