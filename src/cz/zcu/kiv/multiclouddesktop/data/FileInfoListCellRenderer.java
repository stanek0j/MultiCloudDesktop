package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;

/**
 * cz.zcu.kiv.multiclouddesktop.data/FileInfoListCellRenderer.java			<br /><br />
 *
 * Cell renderer for displaying {@link cz.zcu.kiv.multicloud.json.FileInfo} in {@link javax.swing.JList}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileInfoListCellRenderer implements ListCellRenderer<FileInfo> {

	/** Icon for a folder. */
	private final Icon folderIcon;
	/** Icon for a file. */
	private final Icon fileIcon;

	/**
	 * Ctor with icons.
	 * @param folderIcon Folder icon.
	 * @param fileIcon File icon.
	 */
	public FileInfoListCellRenderer(Icon folderIcon, Icon fileIcon) {
		this.folderIcon = folderIcon;
		this.fileIcon = fileIcon;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends FileInfo> list, FileInfo value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) {
			return null;
		}
		JPanel cell = new JPanel();
		cell.setBorder(new EmptyBorder(4, 4, 4, 4));
		cell.setLayout(new BorderLayout());
		JLabel cellIcon;
		if (value.getFileType() == FileType.FOLDER) {
			cellIcon = new JLabel(folderIcon);
		} else {
			cellIcon = new JLabel(fileIcon);
		}
		JLabel cellName = new JLabel(value.getName(), JLabel.CENTER);
		cellName.setBorder(new EmptyBorder(0, 4, 4, 4));
		if (isSelected) {
			cell.setBackground(list.getSelectionBackground());
			cell.setForeground(list.getSelectionForeground());
			cellIcon.setBackground(list.getSelectionBackground());
			cellName.setBackground(list.getSelectionBackground());
			cellIcon.setForeground(list.getSelectionForeground());
			cellName.setForeground(list.getSelectionForeground());
		} else {
			cell.setBackground(list.getBackground());
			cell.setForeground(list.getForeground());
			cellIcon.setBackground(list.getBackground());
			cellName.setBackground(list.getBackground());
			cellIcon.setForeground(list.getForeground());
			cellName.setForeground(list.getForeground());
		}
		cell.add(cellIcon, BorderLayout.CENTER);
		cell.add(cellName, BorderLayout.SOUTH);
		return cell;
	}

}
