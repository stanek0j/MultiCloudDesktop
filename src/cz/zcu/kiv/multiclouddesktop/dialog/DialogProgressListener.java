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

public class DialogProgressListener extends ProgressListener {

	private final JLabel lblSize;
	private final JLabel lblPercent;
	private final JPanel labelPanel;
	private final JPanel progressPanel;
	private final JProgressBar progressBar;

	private ProgressDialog dialog;

	public DialogProgressListener() {
		this(ProgressListener.DEFAULT_REFRESH_INTERVAL);
	}

	public DialogProgressListener(long refreshInterval) {
		super(refreshInterval);
		lblSize = new JLabel("Size");
		lblSize.setBorder(new EmptyBorder(12, 12, 0, 12));
		lblPercent = new JLabel("100%", SwingConstants.RIGHT);
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

	public JComponent[] getComponents() {
		return new JComponent[] {
				labelPanel,
				progressPanel
		};
	}

	public ProgressDialog getDialog() {
		return dialog;
	}

	protected void onFinish() {
		if (dialog != null) {
			dialog.closeDialog();
		}
		lblSize.setText(null);
		lblPercent.setText(null);
		progressBar.setValue(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onProgress() {
		if (progressBar != null) {
			String transferred = Utils.formatSize(getTransferred(), UnitsFormat.BINARY);
			String total = Utils.formatSize(getTotalSize(), UnitsFormat.BINARY);
			int percent = (int) (100.0 * ((double) getTransferred() / (double) getTotalSize()));
			lblSize.setText(transferred + " / " + total);
			lblPercent.setText(String.valueOf(percent) + "%");
			progressBar.setValue(percent);
		}
		if (getTransferred() == getTotalSize()) {
			onFinish();
		}
	}

	public void setDialog(ProgressDialog dialog) {
		this.dialog = dialog;
	}

}
