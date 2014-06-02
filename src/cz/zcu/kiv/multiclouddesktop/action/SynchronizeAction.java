package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.KeyStroke;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/SynchronizeAction.java			<br /><br />
 *
 * Action for synchronizing content of local folder with remote storages.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SynchronizeAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -574517728180984464L;

	/** Name of the action. */
	public static final String ACT_NAME = "Synchronize";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public SynchronizeAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (parent.getPreferences().getSyncFolder() == null) {
			parent.getMessageCallback().displayError("Synchronization folder not set.");
		} else {
			File sync = new File(parent.getPreferences().getSyncFolder());
		}
	}

}
