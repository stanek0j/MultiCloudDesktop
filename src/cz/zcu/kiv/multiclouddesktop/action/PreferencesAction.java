package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.Preferences;
import cz.zcu.kiv.multiclouddesktop.dialog.PreferencesDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/PreferencesAction.java			<br /><br />
 *
 * Action for setting up the application.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class PreferencesAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -316014208449021160L;

	/** Name of the action. */
	public static final String ACT_NAME = "Preferences";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public PreferencesAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Preferences prefs = parent.getPreferences();
		PreferencesDialog dialog = new PreferencesDialog(parent, ACT_NAME, prefs);
		dialog.setVisible(true);
		switch (dialog.getOption()) {
		case JOptionPane.OK_OPTION:
			prefs = dialog.getPreferences();
			parent.actionPreferences(prefs);
			parent.getMessageCallback().displayMessage("Preferences updated.");
			break;
		case JOptionPane.CANCEL_OPTION:
		case JOptionPane.CLOSED_OPTION:
		default:
			parent.getMessageCallback().displayMessage("Updating preferences canceled.");
			break;
		}
	}

}
