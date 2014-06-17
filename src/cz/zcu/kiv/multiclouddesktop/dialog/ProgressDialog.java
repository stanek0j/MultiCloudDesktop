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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * cz.zcu.kiv.multiclouddesktop.dialog/ProgressDialog.java			<br /><br />
 *
 * Generic dialog with disabled window closing button and abort button.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ProgressDialog extends JDialog {

	/** Serialization constant. */
	private static final long serialVersionUID = 5717709239697167338L;

	/** If the abort button was clicked. */
	private boolean aborted;
	/** If dialog closing should be prevented. */
	private boolean prevent;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame of the dialog.
	 * @param content Content of the dialog.
	 * @param title Title of the dialog.
	 */
	public ProgressDialog(Frame parent, JComponent[] content, String title) {
		setTitle(title);
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
				setVisible(false);
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
		prevent = false;
	}

	/**
	 * Closes the dialog from other thread.
	 */
	public void closeDialog() {
		if (!prevent) {
			SwingUtilities.invokeLater(new Runnable() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public void run() {
					setVisible(false);
				}
			});
		}
	}

	/**
	 * Returns if the abort button was clicked.
	 * @return If the abort button was clicked.
	 */
	public boolean isAborted() {
		return aborted;
	}

	/**
	 * Sets if dialog closing should be prevented.
	 * @param prevent If dialog closing should be prevented.
	 */
	public void preventClosing(boolean prevent) {
		this.prevent = prevent;
	}

}
