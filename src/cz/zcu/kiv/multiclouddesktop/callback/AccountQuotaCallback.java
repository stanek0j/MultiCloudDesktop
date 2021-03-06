package cz.zcu.kiv.multiclouddesktop.callback;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multicloud.utils.Utils.UnitsFormat;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.BackgroundTask;

/**
 * cz.zcu.kiv.multiclouddesktop.callback/AccountQuotaCallback.java			<br /><br />
 *
 * Callback for displaying user account quota.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountQuotaCallback implements BackgroundCallback<AccountQuota> {

	/** List with the user account information. */
	private final JList<AccountData> list;
	/** Parent frame. */
	private final Frame parent;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param list List with the user account information.
	 */
	public AccountQuotaCallback(Frame parent, JList<AccountData> list) {
		this.parent = parent;
		this.list = list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(final BackgroundTask task, final String accountName, final AccountQuota result) {
		if (result == null) {
			return;
		}
		/* update the list */
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				for (int i = 0; i < list.getModel().getSize(); i++) {
					AccountData account = list.getModel().getElementAt(i);
					if (account.getName().equals(accountName)) {
						account.setTotalSpace(result.getTotalBytes());
						account.setFreeSpace(result.getFreeBytes());
						account.setUsedSpace(result.getUsedBytes());
						break;
					}
				}
				list.revalidate();
				list.repaint();
			}
		});
		/* show dialog with quota information, if requested */
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				if (task == BackgroundTask.QUOTA) {
					JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					JLabel lblName = new JLabel("User account:");
					lblName.setPreferredSize(new Dimension(80, lblName.getPreferredSize().height));
					JLabel lblNameTxt = new JLabel(accountName);
					lblNameTxt.setFont(new Font(lblNameTxt.getFont().getFontName(), Font.BOLD, lblNameTxt.getFont().getSize()));
					namePanel.add(lblName);
					namePanel.add(lblNameTxt);
					JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					JLabel lblTotal = new JLabel("Total space:");
					lblTotal.setPreferredSize(new Dimension(80, lblTotal.getPreferredSize().height));
					JLabel lblTotalTxt = new JLabel(Utils.formatSize(result.getTotalBytes(), UnitsFormat.BINARY) + " (" + result.getTotalBytes() + " B)");
					lblTotalTxt.setFont(new Font(lblTotalTxt.getFont().getFontName(), Font.BOLD, lblTotalTxt.getFont().getSize()));
					totalPanel.add(lblTotal);
					totalPanel.add(lblTotalTxt);
					JPanel freePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					JLabel lblFree = new JLabel("Free space:");
					lblFree.setPreferredSize(new Dimension(80, lblFree.getPreferredSize().height));
					JLabel lblFreeTxt = new JLabel(Utils.formatSize(result.getFreeBytes(), UnitsFormat.BINARY) + " (" + result.getFreeBytes() + " B)");
					lblFreeTxt.setFont(new Font(lblFreeTxt.getFont().getFontName(), Font.BOLD, lblFreeTxt.getFont().getSize()));
					freePanel.add(lblFree);
					freePanel.add(lblFreeTxt);
					JPanel usedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					JLabel lblUsed = new JLabel("Used space:");
					lblUsed.setPreferredSize(new Dimension(80, lblUsed.getPreferredSize().height));
					JLabel lblUsedTxt = new JLabel(Utils.formatSize(result.getUsedBytes(), UnitsFormat.BINARY) + " (" + result.getUsedBytes() + " B)");
					lblUsedTxt.setFont(new Font(lblUsedTxt.getFont().getFontName(), Font.BOLD, lblUsedTxt.getFont().getSize()));
					usedPanel.add(lblUsed);
					usedPanel.add(lblUsedTxt);
					JComponent[] content = new JComponent[] {
							namePanel,
							totalPanel,
							freePanel,
							usedPanel
					};
					JOptionPane.showMessageDialog(parent, content, "Account quota", JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
	}

}
