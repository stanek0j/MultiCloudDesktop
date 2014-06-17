package cz.zcu.kiv.multiclouddesktop.callback;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cz.zcu.kiv.multiclouddesktop.data.BackgroundTask;

/**
 * cz.zcu.kiv.multiclouddesktop.callback/MessageCallback.java			<br /><br />
 *
 * Callback for displaying status in a {@link javax.swing.JLabel}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MessageCallback implements BackgroundCallback<Boolean> {

	/** Target label. */
	private final JLabel status;
	/** If error dialog should be displayed. */
	private boolean showErrorDialog;
	/** Parent frame. */
	private final Frame parent;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param status Label for showing the status message.
	 * @param showErrorDialog If the error dialog should be displayed.
	 */
	public MessageCallback(Frame parent, JLabel status, boolean showErrorDialog) {
		this.parent = parent;
		this.status = status;
		this.showErrorDialog = showErrorDialog;
	}

	/**
	 * Displays an error message in the target label.
	 * @param message Message to be displayed.
	 */
	public synchronized void displayError(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				status.setForeground(Color.RED);
				status.setText("Error: " + message);
				if (showErrorDialog) {
					JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Displays a normal message in the target label.
	 * @param message Message to be displayed.
	 */
	public synchronized void displayMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				status.setForeground(Color.BLACK);
				status.setText(message);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, String accountName, Boolean result) {
		if (result != null) {
			if (result) {
				displayError(accountName);
			} else {
				displayMessage(accountName);
			}
		}
	}

	/**
	 * Sets if the error dialog should be displayed.
	 * @param showErrorDialog If the error dialog should be displayed.
	 */
	public void setShowErrorDialog(boolean showErrorDialog) {
		this.showErrorDialog = showErrorDialog;
	}

}
