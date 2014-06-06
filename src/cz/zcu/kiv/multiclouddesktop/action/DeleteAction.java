package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/DeleteAction.java			<br /><br />
 *
 * Action for deleting selected file or folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class DeleteAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 1274376976234942701L;

	/** Name of the action. */
	public static final String ACT_NAME = "Delete";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public DeleteAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (!parent.getDataList().hasFocus()) {
			return;
		}
		FileInfo file = parent.getDataList().getSelectedValue();
		if (file == null) {
			parent.getMessageCallback().displayError("No item selected.");
		} else {
			if (file.getName().equals("..")) {
				parent.getMessageCallback().displayError("Cannot delete parent folder from this location.");
			} else {
				int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete the item?", ACT_NAME, JOptionPane.YES_NO_OPTION);
				switch (option) {
				case JOptionPane.YES_OPTION:
					parent.actionDelete(file);
					break;
				case JOptionPane.NO_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					parent.getMessageCallback().displayMessage("Deletion canceled.");
					break;
				}
			}
		}
	}

}
