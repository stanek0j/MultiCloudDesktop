package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * cz.zcu.kiv.multiclouddesktop.action/RefreshAction.java			<br /><br />
 *
 * Action for refreshing the user account and the list of current folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RefreshAction extends AbstractAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 3075395802527770983L;

	/** Name of the action. */
	public static final String ACT_NAME = "Refresh";

	public RefreshAction() {
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

	}

}
