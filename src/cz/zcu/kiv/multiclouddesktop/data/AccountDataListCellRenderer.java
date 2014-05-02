package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multicloud.utils.Utils.UnitsFormat;

/**
 * cz.zcu.kiv.multiclouddesktop.data/AccountDataListCellRenderer.java			<br /><br />
 *
 * Cell renderer for displaying {@link cz.zcu.kiv.multiclouddesktop.data.AccountData} in {@link javax.swing.JList}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountDataListCellRenderer implements ListCellRenderer<AccountData> {

	private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();
	private final Font titleFont;
	private final Font normalFont;

	public AccountDataListCellRenderer(Font titleFont, Font normalFont) {
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
		JLabel cellSpace = new JLabel("Total: " + Utils.formatSize(value.getTotalSpace(), UnitsFormat.BINARY));
		cell.setLayout(new GridLayout(3, 1));
		cellTitle.setBorder(new EmptyBorder(8, 8, 4, 8));
		cellTitle.setFont(titleFont);
		cellCloud.setBorder(new EmptyBorder(8, 8, 4, 8));
		cellCloud.setFont(normalFont);
		cellSpace.setBorder(new EmptyBorder(4, 8, 4, 8));
		cellSpace.setFont(normalFont);
		if (isSelected) {
			cell.setBackground(list.getSelectionBackground());
			cell.setForeground(list.getSelectionForeground());
			cellTitle.setBackground(list.getSelectionBackground());
			cellCloud.setBackground(list.getSelectionBackground());
			cellSpace.setBackground(list.getSelectionBackground());
			cellTitle.setForeground(list.getSelectionForeground());
			cellCloud.setForeground(list.getSelectionForeground());
			cellSpace.setForeground(list.getSelectionForeground());
		} else {
			cell.setBackground(list.getBackground());
			cell.setForeground(list.getForeground());
			cellTitle.setBackground(list.getBackground());
			cellCloud.setBackground(list.getBackground());
			cellSpace.setBackground(list.getBackground());
			cellTitle.setForeground(list.getForeground());
			cellCloud.setForeground(list.getForeground());
			cellSpace.setForeground(list.getForeground());
		}
		cell.add(cellTitle);
		cell.add(cellCloud);
		cell.add(cellSpace);
		return cell;
	}

}
