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
 * cz.zcu.kiv.multiclouddesktop.action/CutAction.java			<br /><br />
 *
 * Action for saving selected file for the move operation.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CutAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -6961865747565353462L;

	/** Name of the action. */
	public static final String ACT_NAME = "Cut";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public CutAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		parent.getDataList().getActionMap().put(TransferHandler.getCutAction().getValue(NAME), TransferHandler.getCutAction());
		parent.getDataList().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), TransferHandler.getCutAction());
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
				parent.actionCut(file);
				parent.getMessageCallback().displayMessage("File ready to be moved.");
			}
		}
	}

}
