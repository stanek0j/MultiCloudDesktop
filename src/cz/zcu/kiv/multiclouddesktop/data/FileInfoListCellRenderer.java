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

public class FileInfoListCellRenderer implements ListCellRenderer<FileInfo> {

	private final Icon folderIcon;
	private final Icon fileIcon;

	public FileInfoListCellRenderer(Icon folderIcon, Icon fileIcon) {
		this.folderIcon = folderIcon;
		this.fileIcon = fileIcon;
	}


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
