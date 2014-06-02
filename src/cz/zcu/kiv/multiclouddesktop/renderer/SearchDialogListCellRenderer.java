package cz.zcu.kiv.multiclouddesktop.renderer;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;

/**
 * cz.zcu.kiv.multiclouddesktop.renderer/SearchDialogListCellRenderer.java			<br /><br />
 *
 * Cell renderer for file list used in the {@link cz.zcu.kiv.multiclouddesktop.dialog.SearchDialog}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SearchDialogListCellRenderer implements ListCellRenderer<FileInfo> {

	/** File to be matched. */
	private final FileInfo match;
	/** If path should be displayed instead of file name. */
	private final boolean path;
	/** Folder icon. */
	private final ImageIcon icnFolder;
	/** File icon. */
	private final ImageIcon icnFile;
	/** Bad file icon. */
	private final ImageIcon icnBadFile;

	/**
	 * Ctor with necessary parameters.
	 * @param showPath If path should be displayed instead of file name.
	 * @param folder Folder icon.
	 * @param file File icon.
	 * @param badFile Bad file icon.
	 */
	public SearchDialogListCellRenderer(FileInfo matchFile, boolean showPath, ImageIcon folder, ImageIcon file, ImageIcon badFile) {
		match = matchFile;
		path = showPath;
		icnFolder = folder;
		icnFile = file;
		icnBadFile = badFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends FileInfo> list, FileInfo value, int index, boolean isSelected, boolean cellHasFocus) {
		JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel cellIcon = new JLabel();
		if (value.getFileType() == FileType.FOLDER) {
			cellIcon.setIcon(icnFolder);
		} else {
			if (match != null) {
				if (match.getChecksum() == null || value.getChecksum() == null) {
					cellIcon.setIcon(icnBadFile);
				} else {
					if (match.getChecksum().equals(value.getChecksum())) {
						cellIcon.setIcon(icnFile);
					} else {
						cellIcon.setIcon(icnBadFile);
					}
				}
			} else {
				cellIcon.setIcon(icnFile);
			}
		}
		cellIcon.setBorder(new EmptyBorder(2, 2, 2, 2));
		JLabel cellTitle = new JLabel(value.getName());
		if (path && value.getPath() != null) {
			cellTitle.setText(value.getPath());
		}
		cellTitle.setBorder(new EmptyBorder(4, 8, 4, 8));
		if (isSelected) {
			cell.setBackground(list.getSelectionBackground());
			cell.setForeground(list.getSelectionForeground());
			cellIcon.setBackground(list.getSelectionBackground());
			cellTitle.setBackground(list.getSelectionBackground());
			cellIcon.setForeground(list.getSelectionForeground());
			cellTitle.setForeground(list.getSelectionForeground());
		} else {
			cell.setBackground(list.getBackground());
			cell.setForeground(list.getForeground());
			cellIcon.setBackground(list.getBackground());
			cellTitle.setBackground(list.getBackground());
			cellIcon.setForeground(list.getForeground());
			cellTitle.setForeground(list.getForeground());
		}
		cell.add(cellIcon);
		cell.add(cellTitle);
		return cell;
	}

}
