package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

/**
 * cz.zcu.kiv.multiclouddesktop.action/QuotaAction.java			<br /><br />
 *
 * Action for displaying the quota of the user account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class QuotaAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -5715047077392436975L;

	/** Name of the action. */
	public static final String ACT_NAME = "Quota";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public QuotaAction(MultiCloudDesktop parent) {
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
			parent.getMessageCallback().displayError("No account selected for listing its quota.");
		} else {
			parent.actionQuota(account);
		}
	}

}
