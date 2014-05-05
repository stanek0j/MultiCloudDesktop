package cz.zcu.kiv.multiclouddesktop.dialog;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ProgressDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 5717709239697167338L;

	private boolean aborted;

	public ProgressDialog(Frame parent, JComponent[] content, String title) {
		setTitle(title);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		setResizable(false);
		for (JComponent item: content) {
			add(item);
		}
		JButton abortButton = new JButton("Abort");
		abortButton.setMargin(new Insets(4, 20, 4, 20));
		abortButton.addActionListener(new ActionListener() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent event) {
				aborted = true;
				dispose();
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		buttonPanel.add(abortButton);
		add(buttonPanel);
		pack();
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
		aborted = false;
	}

	/**
	 * Closes the dialog from other thread.
	 */
	public void closeDialog() {
		dispose();
	}

	/**
	 * Returns if the abort button was clicked.
	 * @return If the abort button was clicked.
	 */
	public boolean isAborted() {
		return aborted;
	}

}
