package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/AuthorizeDialog.java			<br /><br />
 *
 * Dialog to be displayed while the authorization process is running.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AuthorizeDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 1798055430180373112L;

	/** Abort button. */
	private final JButton btnAbort;
	/** Label for displaying message to the user. */
	private final JLabel lblText;
	/** Panel for holding the message. */
	private final JPanel labelPanel;
	/** Panel for holding the progress bar. */
	private final JPanel progressPanel;
	/** Panel for holding the abort button. */
	private final JPanel buttonPanel;
	/** Progress bar to show that the process is running. */
	private final JProgressBar progressBar;
	/** Message to be displayed. */
	private final String text;
	/** Thread for animating the dots after the message. */
	private final Thread dotAnim;

	/** If the operation was aborted. */
	private boolean aborted;
	/** If the animation thread should terminate. */
	private boolean terminate;
	/** If the authorization failed. */
	private boolean failed;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent window to be blocked.
	 * @param message Message to be displayed.
	 * @param title Title of the displayed dialog.
	 */
	public AuthorizeDialog(Frame parent, String message, String title) {
		text = message;
		lblText = new JLabel(message);
		lblText.setBorder(new EmptyBorder(12, 12, 0, 12));
		labelPanel = new JPanel();
		labelPanel.setBorder(new EmptyBorder(4, 4, 0, 4));
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(lblText, BorderLayout.CENTER);
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(250, progressBar.getPreferredSize().height));
		progressBar.setIndeterminate(true);
		progressPanel = new JPanel();
		progressPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
		progressPanel.setLayout(new FlowLayout());
		progressPanel.add(progressBar);
		btnAbort = new JButton("Abort");
		btnAbort.setMargin(new Insets(4, 20, 4, 20));
		btnAbort.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				aborted = true;
				terminate = true;
				dotAnim.interrupt();
				dispose();
			}
		});
		buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		buttonPanel.add(btnAbort);
		dotAnim = new Thread() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				StringBuilder sb = new StringBuilder();
				sb.append(text);
				int dots = 0;
				while(!terminate) {
					try {
						Thread.sleep(600);
					} catch (InterruptedException e) {
						break;
					}
					dots++;
					if (dots > 3) {
						dots = 0;
						sb.setLength(text.length());
					} else {
						sb.append(".");
					}
					lblText.setText(sb.toString());
				}
			}
		};

		setTitle(title);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		setResizable(false);

		add(labelPanel);
		add(progressPanel);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
		aborted = false;
		terminate = false;
		failed = false;
		dotAnim.start();
	}

	/**
	 * Closes the dialog from other thread.
	 */
	public void closeDialog() {
		terminate = true;
		dotAnim.interrupt();
		dispose();
	}

	/**
	 * Returns if the abort button was clicked.
	 * @return If the abort button was clicked.
	 */
	public boolean isAborted() {
		return aborted;
	}

	/**
	 * Returns if the authorization failed.
	 * @return If the authorization failed.
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Sets if the authorization failed.
	 * @param failed If the authorization failed.
	 */
	public void setFailed(boolean failed) {
		this.failed = failed;
	}

}
