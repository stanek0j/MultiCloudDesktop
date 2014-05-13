package cz.zcu.kiv.multiclouddesktop.dialog;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/OverwriteFileChooser.java			<br /><br />
 *
 * File chooser with option for approving file overwriting.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OverwriteFileChooser extends JFileChooser {

	/** Serialization constant. */
	private static final long serialVersionUID = -4490849383065033280L;

	/** If the file should be overwritten. */
	private boolean overwrite;

	/**
	 * Empty ctor.
	 */
	public OverwriteFileChooser() {
		super();
		overwrite = false;
	}

	/**
	 * Ctor with default folder.
	 * @param folder Default folder.
	 */
	public OverwriteFileChooser(File folder) {
		super(folder);
		overwrite = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void approveSelection() {
		if (getDialogType() == SAVE_DIALOG) {
			File selectedFile = getSelectedFile();
			if ((selectedFile != null) && selectedFile.exists()) {
				int option = JOptionPane.showConfirmDialog(this, "Do you want to overwrite existing file?", "Overwrite file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				switch (option) {
				case JOptionPane.YES_OPTION:
					overwrite = true;
					break;
				case JOptionPane.NO_OPTION:
				case JOptionPane.CLOSED_OPTION:
				default:
					overwrite = false;
					return;
				}
			}
		}
		super.approveSelection();
	}

	/**
	 * Returns if the file should be overwritten.
	 * @return If the file should be overwritten.
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

}
