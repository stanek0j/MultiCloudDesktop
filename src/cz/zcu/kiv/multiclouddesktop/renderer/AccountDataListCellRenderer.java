package cz.zcu.kiv.multiclouddesktop.renderer;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multicloud.utils.Utils.UnitsFormat;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;

/**
 * cz.zcu.kiv.multiclouddesktop.renderer/AccountDataListCellRenderer.java			<br /><br />
 *
 * Cell renderer for displaying {@link cz.zcu.kiv.multiclouddesktop.data.AccountData} in {@link javax.swing.JList}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountDataListCellRenderer implements ListCellRenderer<AccountData> {

	/** Font for displaying the title of the item. */
	private final Font titleFont;
	/** Font for displaying the rest of the text. */
	private final Font normalFont;

	/**
	 * Ctor with necessary parameters.
	 * @param titleFont Font for the title.
	 * @param normalFont Font for the rest of the text.
	 */
	public AccountDataListCellRenderer(Font titleFont, Font normalFont) {
		this.titleFont = titleFont;
		this.normalFont = normalFont;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends AccountData> list, AccountData value, int index, boolean isSelected, boolean cellHasFocus) {
		String total = "";
		StringBuilder sb = new StringBuilder();
		if (value.isAuthorized()) {
			total = "Total space: " + Utils.formatSize(value.getTotalSpace(), UnitsFormat.BINARY);
			sb.append("Free / Used: ");
			sb.append(Utils.formatSize(value.getFreeSpace(), UnitsFormat.BINARY));
			sb.append(" / ");
			sb.append(Utils.formatSize(value.getUsedSpace(), UnitsFormat.BINARY));
		} else {
			total = "Not authorized.";
		}
		JPanel cell = new JPanel();
		JLabel cellTitle = new JLabel(value.getName());
		JLabel cellCloud = new JLabel();
		if (value.isListed()) {
			cellCloud.setText(value.getCloud() + " (listed)");
		} else {
			cellCloud.setText(value.getCloud());
		}
		JLabel cellTotal = new JLabel(total);
		JLabel cellSpace = new JLabel(sb.toString());
		cell.setLayout(new GridLayout(4, 1));
		cellTitle.setBorder(new EmptyBorder(8, 8, 4, 8));
		cellTitle.setFont(titleFont);
		cellCloud.setBorder(new EmptyBorder(8, 8, 4, 8));
		cellCloud.setFont(normalFont);
		cellTotal.setBorder(new EmptyBorder(8, 8, 4, 8));
		cellTotal.setFont(normalFont);
		cellSpace.setBorder(new EmptyBorder(4, 8, 4, 8));
		cellSpace.setFont(normalFont);
		if (isSelected) {
			cell.setBackground(list.getSelectionBackground());
			cell.setForeground(list.getSelectionForeground());
			cellTitle.setBackground(list.getSelectionBackground());
			cellCloud.setBackground(list.getSelectionBackground());
			cellTotal.setBackground(list.getSelectionBackground());
			cellSpace.setBackground(list.getSelectionBackground());
			cellTitle.setForeground(list.getSelectionForeground());
			cellCloud.setForeground(list.getSelectionForeground());
			cellTotal.setForeground(list.getSelectionForeground());
			cellSpace.setForeground(list.getSelectionForeground());
		} else {
			cell.setBackground(list.getBackground());
			cell.setForeground(list.getForeground());
			cellTitle.setBackground(list.getBackground());
			cellCloud.setBackground(list.getBackground());
			cellTotal.setBackground(list.getBackground());
			cellSpace.setBackground(list.getBackground());
			cellTitle.setForeground(list.getForeground());
			cellCloud.setForeground(list.getForeground());
			cellTotal.setForeground(list.getForeground());
			cellSpace.setForeground(list.getForeground());
		}
		cell.add(cellTitle);
		cell.add(cellCloud);
		cell.add(cellTotal);
		cell.add(cellSpace);
		return cell;
	}

}
