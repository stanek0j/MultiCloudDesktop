package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/ExitAction.java			<br /><br />
 *
 * Action for terminating the application.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ExitAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -4776153097513535416L;

	/** Name of the action. */
	public static final String ACT_NAME = "Exit";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public ExitAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		parent.actionClose();
	}

}
