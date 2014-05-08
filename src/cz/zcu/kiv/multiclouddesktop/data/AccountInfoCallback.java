package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.data/AccountInfoCallback.java			<br /><br />
 *
 * Callback for displaying information about user account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountInfoCallback implements BackgroundCallback<AccountInfo> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, AccountInfo result) {
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
		JOptionPane.showMessageDialog(MultiCloudDesktop.getWindow(), content, "Account information", JOptionPane.INFORMATION_MESSAGE);
	}

}
