package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

public class MessageCallback implements BackgroundCallback<Boolean> {

	private final JLabel status;

	public MessageCallback(JLabel status) {
		this.status = status;
	}

	public synchronized void displayError(String message) {
		status.setForeground(Color.RED);
		status.setText("Error: " + message);
		JOptionPane.showMessageDialog(MultiCloudDesktop.getWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public synchronized void displayMessage(String message) {
		status.setForeground(Color.BLACK);
		status.setText(message);
	}

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

}
