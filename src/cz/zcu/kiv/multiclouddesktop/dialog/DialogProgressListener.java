package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.filesystem.ProgressListener;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multicloud.utils.Utils.UnitsFormat;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/DialogProgressListener.java			<br /><br />
 *
 * Progress listener dialog that displays the progress in a progress bar.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class DialogProgressListener extends ProgressListener {

	/** Label for showing file sizes. */
	private final JLabel lblSize;
	/** Label for showing percentages. */
	private final JLabel lblPercent;
	/** Panel for holding progress labels. */
	private final JPanel labelPanel;
	/** Panel for holding progress bar. */
	private final JPanel progressPanel;
	/** Progress bar for showing the progress. */
	private final JProgressBar progressBar;

	/** Dialog displayed. */
	private ProgressDialog dialog;

	/**
	 * Empty ctor.
	 */
	public DialogProgressListener() {
		this(ProgressListener.DEFAULT_REFRESH_INTERVAL);
	}

	/**
	 * Ctor with refresh interval.
	 * @param refreshInterval Refresh interval.
	 */
	public DialogProgressListener(long refreshInterval) {
		super(refreshInterval);
		lblSize = new JLabel("Initializing...");
		lblSize.setBorder(new EmptyBorder(12, 12, 0, 12));
		lblPercent = new JLabel(" ", SwingConstants.RIGHT);
		lblPercent.setBorder(new EmptyBorder(12, 12, 0, 12));
		lblPercent.setPreferredSize(new Dimension(56, 14));
		labelPanel = new JPanel();
		labelPanel.setBorder(new EmptyBorder(4, 4, 0, 4));
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(lblSize, BorderLayout.CENTER);
		labelPanel.add(lblPercent, BorderLayout.EAST);
		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(250, progressBar.getPreferredSize().height));
		progressPanel = new JPanel();
		progressPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
		progressPanel.setLayout(new FlowLayout());
		progressPanel.add(progressBar);
	}

	/**
	 * Returns the components of the dialog.
	 * @return Array of dialog components.
	 */
	public JComponent[] getComponents() {
		lblSize.setText("Initializing...");
		lblPercent.setText(null);
		progressBar.setValue(0);
		return new JComponent[] {
				labelPanel,
				progressPanel
		};
	}

	/**
	 * Return the dialog.
	 * @return Progress dialog.
	 */
	public ProgressDialog getDialog() {
		return dialog;
	}

	/**
	 * Method for closing the dialog on operation finish.
	 */
	protected void onFinish() {
		if (dialog != null) {
			dialog.closeDialog();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onProgress() {
		if (progressBar != null) {
			String transferred = Utils.formatSize((getTransferred() / getDivisor()), UnitsFormat.BINARY);
			String total = Utils.formatSize(getTotalSize(), UnitsFormat.BINARY);
			int percent = (int) (100.0 * ((double) getTransferred() / getDivisor()) / getTotalSize());
			lblSize.setText(transferred + " / " + total);
			lblPercent.setText(String.valueOf(percent) + "%");
			progressBar.setValue(percent);
		}
		if ((getTransferred() / getDivisor()) == getTotalSize()) {
			onFinish();
		}
	}

	/**
	 * Sets the progress dialog.
	 * @param dialog Progress dialog.
	 */
	public void setDialog(ProgressDialog dialog) {
		this.dialog = dialog;
	}

}
