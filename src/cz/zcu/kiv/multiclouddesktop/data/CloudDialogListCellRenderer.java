package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

/**
 * cz.zcu.kiv.multiclouddesktop.data/CloudDialogListCellRenderer.java			<br /><br />
 *
 * Cell renderer for account list used in the {@link cz.zcu.kiv.multiclouddesktop.dialog.CloudDialog}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CloudDialogListCellRenderer implements ListCellRenderer<AccountData> {

	/** Font for displaying the title of the item. */
	private final Font titleFont;
	/** Font for displaying the rest of the text. */
	private final Font normalFont;

	/**
	 * Ctor with necessary parameters.
	 * @param titleFont Font for the title.
	 * @param normalFont Font for the rest of the text.
	 */
	public CloudDialogListCellRenderer(Font titleFont, Font normalFont) {
		this.titleFont = titleFont;
		this.normalFont = normalFont;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends AccountData> list, AccountData value, int index, boolean isSelected, boolean cellHasFocus) {
		JPanel cell = new JPanel();
		JLabel cellTitle = new JLabel(value.getName());
		JLabel cellCloud = new JLabel(value.getCloud());
		cell.setLayout(new GridLayout(2, 1));
		cellTitle.setBorder(new EmptyBorder(4, 8, 2, 8));
		cellTitle.setFont(titleFont);
		cellCloud.setBorder(new EmptyBorder(2, 8, 4, 8));
		cellCloud.setFont(normalFont);
		if (isSelected) {
			cell.setBackground(list.getSelectionBackground());
			cell.setForeground(list.getSelectionForeground());
			cellTitle.setBackground(list.getSelectionBackground());
			cellCloud.setBackground(list.getSelectionBackground());
			cellTitle.setForeground(list.getSelectionForeground());
			cellCloud.setForeground(list.getSelectionForeground());
		} else {
			cell.setBackground(list.getBackground());
			cell.setForeground(list.getForeground());
			cellTitle.setBackground(list.getBackground());
			cellCloud.setBackground(list.getBackground());
			cellTitle.setForeground(list.getForeground());
			cellCloud.setForeground(list.getForeground());
		}
		cell.add(cellTitle);
		cell.add(cellCloud);
		return cell;
	}

}
