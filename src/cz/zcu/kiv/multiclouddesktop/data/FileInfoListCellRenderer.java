package cz.zcu.kiv.multiclouddesktop.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
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
	/** Small icon for folder. */
	private final Icon folderIconSmall;
	/** Icon for a file. */
	private final Icon fileIcon;
	/** Small icon for file. */
	private final Icon fileIconSmall;

	/**
	 * Ctor with icons.
	 * @param folderIcon Folder icon.
	 * @param fileIcon File icon.
	 */
	public FileInfoListCellRenderer(ImageIcon folderIcon, ImageIcon fileIcon) {
		this.folderIcon = folderIcon;
		this.folderIconSmall = new ImageIcon(folderIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH));
		this.fileIcon = fileIcon;
		this.fileIconSmall = new ImageIcon(fileIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH));
	}

	/**
	 * Ctor with icons.
	 * @param folderIcon Folder icon.
	 * @param folderIconSmall Small folder icon.
	 * @param fileIcon File icon.
	 * @param fileIconSmall Small file icon.
	 */
	public FileInfoListCellRenderer(ImageIcon folderIcon, ImageIcon folderIconSmall, ImageIcon fileIcon, ImageIcon fileIconSmall) {
		this.folderIcon = folderIcon;
		this.folderIconSmall = folderIconSmall;
		this.fileIcon = fileIcon;
		this.fileIconSmall = fileIconSmall;
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
			if (list.getLayoutOrientation() == JList.VERTICAL) {
				cellIcon = new JLabel(folderIconSmall);
			} else {
				cellIcon = new JLabel(folderIcon);
			}
		} else {
			if (list.getLayoutOrientation() == JList.VERTICAL) {
				cellIcon = new JLabel(fileIconSmall);
			} else {
				cellIcon = new JLabel(fileIcon);
			}
		}
		JLabel cellName = new JLabel(value.getName());
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
		if (list.getLayoutOrientation() == JList.VERTICAL) {
			cellName.setHorizontalAlignment(JLabel.LEFT);
			cellName.setVerticalAlignment(JLabel.BOTTOM);
			cell.add(cellIcon, BorderLayout.WEST);
			cell.add(cellName, BorderLayout.CENTER);
		} else {
			cellName.setHorizontalAlignment(JLabel.CENTER);
			cell.add(cellIcon, BorderLayout.CENTER);
			cell.add(cellName, BorderLayout.SOUTH);
		}
		return cell;
	}

}
