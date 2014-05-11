package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/CopyAction.java			<br /><br />
 *
 * Action for saving selected file for the copy operation.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CopyAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 2065161618207780833L;

	/** Name of the action. */
	public static final String ACT_NAME = "Copy";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public CopyAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		parent.getDataList().getActionMap().put(TransferHandler.getCopyAction().getValue(NAME), TransferHandler.getCopyAction());
		parent.getDataList().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), TransferHandler.getCopyAction());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		FileInfo file = parent.getDataList().getSelectedValue();
		if (file == null) {
			JOptionPane.showMessageDialog(parent, "No file selected.", ACT_NAME, JOptionPane.ERROR_MESSAGE);
		} else {
			if (file.getFileType() == FileType.FOLDER) {
				JOptionPane.showMessageDialog(parent, "Select a file.", ACT_NAME, JOptionPane.ERROR_MESSAGE);
			} else {
				parent.actionCopy(file);
				parent.getMessageCallback().displayMessage("File ready to be copied.");
			}
		}
	}

}
