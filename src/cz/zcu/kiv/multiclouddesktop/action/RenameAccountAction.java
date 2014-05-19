package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.dialog.AccountDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/RenameAccountAction.java			<br /><br />
 *
 * Action for renaming an account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RenameAccountAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -4671924894402149594L;

	/** Name of the action. */
	public static final String ACT_NAME = "Rename";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public RenameAccountAction(MultiCloudDesktop parent) {
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
			parent.getMessageCallback().displayError("No account selected to be renamed.");
		} else {
			AccountDialog dialog = new AccountDialog(parent, ACT_NAME, parent.getAccountManager(), parent.getCloudManager(), account);
			dialog.setVisible(true);
			switch (dialog.getOption()) {
			case JOptionPane.OK_OPTION:
				AccountData renamed = dialog.getAccountData();
				parent.actionRenameAccount(account, renamed);
				break;
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Renaming account canceled.");
				break;
			}
		}
	}

}
