package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.dialog.AuthorizeDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/AuthorizeAction.java			<br /><br />
 *
 * Action for authorizing an account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AuthorizeAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 617922111623576081L;

	/** Name of the action. */
	public static final String ACT_NAME = "Authorize";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public AuthorizeAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		AccountData account = parent.getAccountList().getSelectedValue();
		if (account != null) {
			AuthorizeDialog dialog = new AuthorizeDialog(parent, "Waiting for authorization", ACT_NAME);
			parent.actionAuthorize(account, dialog);
		} else {
			parent.getMessageCallback().displayError("No account selected.");
		}
	}

}
