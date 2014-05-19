package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

/**
 * cz.zcu.kiv.multiclouddesktop.action/RemoveAccountAction.java			<br /><br />
 *
 * Action for removing an account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RemoveAccountAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 2094518688973720627L;

	/** Name of the action. */
	public static final String ACT_NAME = "Remove";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public RemoveAccountAction(MultiCloudDesktop parent) {
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
			JOptionPane.showMessageDialog(parent, "No account selected for removal.", ACT_NAME, JOptionPane.ERROR_MESSAGE);
		} else {
			int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to remove this account?", ACT_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch (option) {
			case JOptionPane.YES_OPTION:
				parent.actionRemoveAccount(account);
				break;
			case JOptionPane.NO_OPTION:
			case JOptionPane.CLOSED_OPTION:
			default:
				parent.getMessageCallback().displayMessage("Account removal canceled.");
				break;
			}
		}
	}

}
