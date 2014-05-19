package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.dialog.AccountDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.action/AddAccountAction.java			<br /><br />
 *
 * Action for adding new user account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AddAccountAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -8868730847412032057L;

	/** Name of the action. */
	public static final String ACT_NAME = "Add";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public AddAccountAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		AccountDialog dialog = new AccountDialog(parent, ACT_NAME, parent.getAccountManager(), parent.getCloudManager(), null);
		dialog.setVisible(true);
		switch (dialog.getOption()) {
		case JOptionPane.OK_OPTION:
			AccountData account = dialog.getAccountData();
			parent.actionAddAccount(account);
			break;
		case JOptionPane.CANCEL_OPTION:
		case JOptionPane.CLOSED_OPTION:
		default:
			parent.getMessageCallback().displayMessage("Adding account canceled.");
			break;
		}
	}

}
