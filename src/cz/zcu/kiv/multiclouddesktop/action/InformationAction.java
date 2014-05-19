package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

/**
 * cz.zcu.kiv.multiclouddesktop.action/InformationAction.java			<br /><br />
 *
 * Action for displaying information about the user of an account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class InformationAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -1076663645781826503L;

	/** Name of the action. */
	public static final String ACT_NAME = "Information";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public InformationAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		AccountData account = parent.getAccountList().getSelectedValue();
		if (account == null) {
			parent.getMessageCallback().displayError("No account selected for listing its basic information.");
		} else {
			parent.actionInformation(account);
		}
	}

}
