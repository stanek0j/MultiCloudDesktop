package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.PasteDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/PasteAction.java			<br /><br />
 *
 * Action for saving selected file for the paste operation.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class PasteAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 6954345925183188362L;

	/** Name of the action. */
	public static final String ACT_NAME = "Paste";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public PasteAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		FileInfo file = parent.getTransferFile();
		if (file == null) {
			parent.getMessageCallback().displayError("No file ready for pasting.");
		} else {
			PasteDialog dialog = new PasteDialog(parent, ACT_NAME, file);
			dialog.setVisible(true);
			switch (dialog.getOption()) {
			case JOptionPane.OK_OPTION:
				boolean found = false;
				for (int i = 0; i < parent.getDataList().getModel().getSize(); i++) {
					FileInfo f = parent.getDataList().getModel().getElementAt(i);
					if (f.getName().equals(dialog.getFileName())) {
						found = true;
						break;
					}
				}
				if (found && !dialog.isOverwrite()) {
					parent.getMessageCallback().displayError("File already exists.");
				} else {
					parent.actionPaste(dialog.getFileName());
				}
				break;
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Pasting canceled.");
				break;
			}
		}
	}

}
