package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.ProgressDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/ChecksumAction.java			<br /><br />
 *
 * Action for computing checksum of a remote file. The remote file is downloaded to temporary folder and deleted after computation finishes.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ChecksumAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -4327368209951641859L;

	/** Name of the action. */
	public static final String ACT_NAME = "Compute checksum";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public ChecksumAction(MultiCloudDesktop parent) {
		super(parent);
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
				int option = JOptionPane.YES_OPTION;
				if (parent.getPreferences().isShowChecksumDialog()) {
					JLabel msgInfo = new JLabel("This action requires download of the entire file.");
					JLabel msgQuestion = new JLabel("Do you want to proceed?");
					msgQuestion.setBorder(new EmptyBorder(2, 0, 12, 0));
					JCheckBox hide = new JCheckBox("Do not show this dialog again.");
					JComponent[] components = new JComponent[] {
							msgInfo,
							msgQuestion,
							hide
					};
					option = JOptionPane.showConfirmDialog(parent, components, ACT_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (hide.isSelected()) {
						parent.getPreferences().setShowChecksumDialog(false);
						parent.actionPreferences(parent.getPreferences());
					}
				}
				switch (option) {
				case JOptionPane.YES_OPTION:
					ProgressDialog dialog = new ProgressDialog(parent, parent.getProgressListener().getComponents(), ACT_NAME);
					parent.actionChecksum(file,dialog);
					dialog.setVisible(true);
					if (dialog.isAborted()) {
						parent.actionAbort();
					}
					break;
				case JOptionPane.NO_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					parent.getMessageCallback().displayMessage("Checksum computation cancelled.");
					break;
				}
			}
		}
	}

}
