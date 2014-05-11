package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

/**
 * cz.zcu.kiv.multiclouddesktop.action/RefreshAction.java			<br /><br />
 *
 * Action for refreshing the user account and the list of current folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RefreshAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 3075395802527770983L;

	/** Name of the action. */
	public static final String ACT_NAME = "Refresh";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public RefreshAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		AccountData account = parent.getAccountList().getSelectedValue();
		if (account == null) {
			parent.getMessageCallback().displayError("No account selected.");
		} else {
			parent.actionRefresh(account.getName());
		}
	}

}
