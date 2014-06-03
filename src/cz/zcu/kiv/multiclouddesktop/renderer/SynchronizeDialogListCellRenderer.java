package cz.zcu.kiv.multiclouddesktop.renderer;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multiclouddesktop.data.AccountData;

/**
 * cz.zcu.kiv.multiclouddesktop.renderer/SynchronizeDialogListCellRenderer.java			<br /><br />
 *
 * Cell renderer for account list used in the {@link cz.zcu.kiv.multiclouddesktop.dialog.SynchronizeDialog}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SynchronizeDialogListCellRenderer implements ListCellRenderer<AccountData> {

	/** Font for displaying the title of the item. */
	private final Font titleFont;
	/** Font for displaying the rest of the text. */
	private final Font normalFont;

	/**
	 * Ctor with necessary parameters.
	 * @param titleFont Font for the title.
	 * @param normalFont Font for the rest of the text.
	 */
	public SynchronizeDialogListCellRenderer(Font titleFont, Font normalFont) {
		this.titleFont = titleFont;
		this.normalFont = normalFont;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends AccountData> list, AccountData value, int index, boolean isSelected, boolean cellHasFocus) {
		JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		JPanel cellText = new JPanel();
		JCheckBox cellCheckBox = new JCheckBox();
		JLabel cellTitle = new JLabel(value.getName());
		JLabel cellCloud = new JLabel(value.getCloud());
		cellCheckBox.setSelected(value.isMatched());
		cellText.setLayout(new GridLayout(2, 1));
		cellTitle.setBorder(new EmptyBorder(4, 8, 2, 0));
		cellTitle.setFont(titleFont);
		cellCloud.setBorder(new EmptyBorder(2, 8, 4, 0));
		cellCloud.setFont(normalFont);

		cell.setBackground(list.getBackground());
		cell.setForeground(list.getForeground());
		cellText.setBackground(list.getBackground());
		cellText.setForeground(list.getForeground());
		cellTitle.setBackground(list.getBackground());
		cellCloud.setBackground(list.getBackground());
		cellCheckBox.setBackground(list.getBackground());
		cellTitle.setForeground(list.getForeground());
		cellCloud.setForeground(list.getForeground());
		cellCheckBox.setForeground(list.getForeground());

		cellText.add(cellTitle);
		cellText.add(cellCloud);
		cell.add(cellCheckBox);
		cell.add(cellText);
		return cell;
	}

}
