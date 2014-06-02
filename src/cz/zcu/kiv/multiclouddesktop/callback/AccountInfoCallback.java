package cz.zcu.kiv.multiclouddesktop.callback;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundTask;

/**
 * cz.zcu.kiv.multiclouddesktop.callback/AccountInfoCallback.java			<br /><br />
 *
 * Callback for displaying information about user account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountInfoCallback implements BackgroundCallback<AccountInfo> {

	/** Parent frame. */
	private final Frame parent;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public AccountInfoCallback(Frame parent) {
		this.parent = parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, String accountName, AccountInfo result) {
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblName = new JLabel("User name:");
		lblName.setPreferredSize(new Dimension(60, lblName.getPreferredSize().height));
		JLabel lblNameTxt = new JLabel(result.getName());
		lblNameTxt.setFont(new Font(lblNameTxt.getFont().getFontName(), Font.BOLD, lblNameTxt.getFont().getSize()));
		namePanel.add(lblName);
		namePanel.add(lblNameTxt);
		JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblId = new JLabel("User ID:");
		lblId.setPreferredSize(new Dimension(60, lblId.getPreferredSize().height));
		JLabel lblIdTxt = new JLabel(result.getId());
		lblIdTxt.setFont(new Font(lblIdTxt.getFont().getFontName(), Font.BOLD, lblIdTxt.getFont().getSize()));
		idPanel.add(lblId);
		idPanel.add(lblIdTxt);
		JComponent[] content = new JComponent[] {
				namePanel,
				idPanel
		};
		JOptionPane.showMessageDialog(parent, content, "Account information", JOptionPane.PLAIN_MESSAGE);
	}

}
