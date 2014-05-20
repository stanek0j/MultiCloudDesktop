package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/AboutAction.java			<br /><br />
 *
 * Action for displaying information about the application.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AboutAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = 4656059789558143744L;

	/** Name of the action. */
	public static final String ACT_NAME = "About";

	/** Application icon. */
	private final ImageIcon icon;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public AboutAction(MultiCloudDesktop parent, ImageIcon icon) {
		super(parent);
		this.icon = icon;
		putValue(NAME, ACT_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		JLabel lblIcon = new JLabel(icon);
		JPanel iconPanel = new JPanel();
		iconPanel.add(lblIcon);
		JPanel titlePanel = new JPanel();
		JLabel lblTitle = new JLabel(MultiCloudDesktop.APP_NAME);
		lblTitle.setFont(new Font(lblTitle.getFont().getFontName(), Font.BOLD, lblTitle.getFont().getSize()));
		titlePanel.add(lblTitle);
		JLabel lblDesc = new JLabel("<html><p style=\"text-align:center\">Application for sharing files among multiple cloud storage service providers.<br>Unified interface for accessing the files and features for faster downloading.</p></html>");
		JPanel descPanel = new JPanel();
		descPanel.add(lblDesc);
		JLabel lblName = new JLabel("Author: Jaromír Staněk");
		JPanel namePanel = new JPanel();
		namePanel.add(lblName);
		JLabel lblYear = new JLabel("© 2014");
		JPanel yearPanel = new JPanel();
		yearPanel.add(lblYear);
		JComponent[] content = new JComponent[] {
				iconPanel,
				titlePanel,
				descPanel,
				namePanel,
				yearPanel
		};
		JOptionPane.showMessageDialog(parent, content, ACT_NAME, JOptionPane.PLAIN_MESSAGE);
	}

}
