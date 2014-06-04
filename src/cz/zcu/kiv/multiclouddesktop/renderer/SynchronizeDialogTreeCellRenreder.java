package cz.zcu.kiv.multiclouddesktop.renderer;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import cz.zcu.kiv.multiclouddesktop.data.MultiCloudTreeNode;

/**
 * cz.zcu.kiv.multiclouddesktop.renderer/SynchronizeDialogTreeCellRenreder.java			<br /><br />
 *
 * Cell renderer for folder structure tree used in the {@link cz.zcu.kiv.multiclouddesktop.dialog.SynchronizeDialog}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SynchronizeDialogTreeCellRenreder implements TreeCellRenderer {

	/** Default tree cell renderer. */
	private final DefaultTreeCellRenderer renderer;
	/** Folder icon. */
	private final ImageIcon folder;
	/** File icon. */
	private final ImageIcon file;
	/** Bad file icon. */
	private final ImageIcon badFile;

	/**
	 * Ctor with necessary parameters.
	 * @param folder Folder icon.
	 * @param file File icon.
	 * @param badFile Bad file icon.
	 */
	public SynchronizeDialogTreeCellRenreder(ImageIcon folder, ImageIcon file, ImageIcon badFile) {
		this.renderer = new DefaultTreeCellRenderer();
		this.folder = folder;
		this.file = file;
		this.badFile = badFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
		if (value instanceof MultiCloudTreeNode) {
			MultiCloudTreeNode node = (MultiCloudTreeNode) value;
			JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			cell.setBorder(new EmptyBorder(1, 1, 1, 1));
			JLabel cellIcon = new JLabel();
			if (node.getFile().isDirectory()) {
				cellIcon.setIcon(folder);
			} else {
				if (node.getAccounts().isEmpty()) {
					cellIcon.setIcon(badFile);
				} else {
					cellIcon.setIcon(file);
				}
			}
			JLabel cellName = new JLabel(node.getName());
			if (isSelected) {
				cell.setBackground(renderer.getBackgroundSelectionColor());
				cellIcon.setBackground(renderer.getBackgroundSelectionColor());
				cellName.setBackground(renderer.getBackgroundSelectionColor());
				cell.setForeground(renderer.getTextSelectionColor());
				cellIcon.setForeground(renderer.getTextSelectionColor());
				cellName.setForeground(renderer.getTextSelectionColor());
			} else {
				cell.setBackground(renderer.getBackgroundNonSelectionColor());
				cellIcon.setBackground(renderer.getBackgroundNonSelectionColor());
				cellName.setBackground(renderer.getBackgroundNonSelectionColor());
				cell.setForeground(renderer.getTextNonSelectionColor());
				cellIcon.setForeground(renderer.getTextNonSelectionColor());
				cellName.setForeground(renderer.getTextNonSelectionColor());
			}
			cell.add(cellIcon);
			cell.add(cellName);
			return cell;
		} else {
			return null;
		}
	}

}
