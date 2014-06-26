package cz.zcu.kiv.multiclouddesktop;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback;
import cz.zcu.kiv.multicloud.oauth2.AuthorizationRequest;

/**
 * cz.zcu.kiv.multiclouddesktop/BrowserCallback.java			<br /><br />
 *
 * Implementation of the {@link cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback} for opening default browser.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class BrowserCallback implements AuthorizationCallback {

	/** Parent frame to be blocked by the message dialog. */
	private final Frame parent;

	/**
	 * Ctor with authorization dialog.
	 * @param dialog Authorization dialog.
	 */
	public BrowserCallback(Frame parent) {
		this.parent = parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAuthorizationRequest(AuthorizationRequest request) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(new URI(request.getRequestUri()));
		} catch (IOException | URISyntaxException e) {
			/* if opening the browser failed, try any other option */
			StringBuilder sb = new StringBuilder();
			sb.append("To authorize this application, visit:\n");
			sb.append(request.getRequestUri());
			/* copy URI to clipboard */
			StringSelection uri = new StringSelection(request.getRequestUri());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(uri, null);
			/* print URI to command line */
			System.out.println(sb.toString());
			/* show message dialog with the URI */
			JLabel lblUri = new JLabel("Authorization URI was copied to clipboard. Visit the URI to authorize.");
			final JTextField txtUri = new JTextField(request.getRequestUri());
			txtUri.setPreferredSize(new Dimension(250, txtUri.getPreferredSize().height));
			txtUri.addFocusListener(new FocusAdapter() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public void focusGained(FocusEvent event) {
					txtUri.selectAll();
				}
			});
			JComponent[] content = new JComponent[] {
					lblUri,
					txtUri
			};
			JOptionPane.showMessageDialog(parent, content, "Authorization", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
