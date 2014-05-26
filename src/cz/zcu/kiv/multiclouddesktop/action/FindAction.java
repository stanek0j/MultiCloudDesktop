package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.dialog.SearchDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/FindAction.java			<br /><br />
 *
 * Action for searching for folders and files.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FindAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -1003184337855193781L;

	/** Name of the action. */
	public static final String ACT_NAME = "Find";

	/** Folder icon. */
	private final ImageIcon icnFolder;
	/** File icon. */
	private final ImageIcon icnFile;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param folder Folder icon.
	 * @param file File icon.
	 */
	public FindAction(MultiCloudDesktop parent, ImageIcon folder, ImageIcon file) {
		super(parent);
		icnFolder = folder;
		icnFile = file;
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		FileInfo file = parent.getDataList().getSelectedValue();
		String match = null;
		if (file != null) {
			match = file.getName();
		}
		SearchDialog dialog = new SearchDialog(parent, ACT_NAME, null, match, null, icnFolder, icnFile, null);
		dialog.setVisible(true);
		parent.getMessageCallback().displayMessage("Search finished.");
	}

}
