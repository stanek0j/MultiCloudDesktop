package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multicloud.utils.Utils.UnitsFormat;

/**
 * cz.zcu.kiv.multiclouddesktop.action/PropertiesAction.java			<br /><br />
 *
 * Action for displaying file or folder information.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class PropertiesAction extends AbstractAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -1190973332086544513L;

	/** Name of the action. */
	public static final String ACT_NAME = "Properties";

	/** Parent frame. */
	private final Frame parent;
	/** List of items. */
	private final JList<FileInfo> list;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 * @param list List of items.
	 */
	public PropertiesAction(Frame parent, JList<FileInfo> list) {
		this.parent = parent;
		this.list = list;
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		FileInfo f = list.getSelectedValue();
		if (f == null) {
			JOptionPane.showMessageDialog(parent, "No item selected.", ACT_NAME, JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblName = new JLabel("Name:");
			lblName.setPreferredSize(new Dimension(60, lblName.getPreferredSize().height));
			JLabel lblNameTxt = new JLabel(f.getName());
			Font boldFont = new Font(lblNameTxt.getFont().getFontName(), Font.BOLD, lblNameTxt.getFont().getSize());
			lblNameTxt.setFont(boldFont);
			namePanel.add(lblName);
			namePanel.add(lblNameTxt);
			JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblId = new JLabel("ID:");
			lblId.setPreferredSize(new Dimension(60, lblId.getPreferredSize().height));
			JLabel lblIdTxt = new JLabel(f.getId());
			lblIdTxt.setFont(boldFont);
			idPanel.add(lblId);
			idPanel.add(lblIdTxt);
			JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblType = new JLabel("Type:");
			lblType.setPreferredSize(new Dimension(60, lblType.getPreferredSize().height));
			JLabel lblTypeTxt = new JLabel(f.getFileType().getText());
			lblTypeTxt.setFont(boldFont);
			typePanel.add(lblType);
			typePanel.add(lblTypeTxt);
			JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblSize = new JLabel("Size:");
			lblSize.setPreferredSize(new Dimension(60, lblSize.getPreferredSize().height));
			JLabel lblSizeTxt = new JLabel("-");
			if (f.getFileType() == FileType.FILE) {
				lblSizeTxt.setText(Utils.formatSize(f.getSize(), UnitsFormat.BINARY) + " (" + f.getSize() + " B)");
			}
			lblSizeTxt.setFont(boldFont);
			sizePanel.add(lblSize);
			sizePanel.add(lblSizeTxt);
			JComponent[] content = new JComponent[] {
					namePanel,
					idPanel,
					typePanel,
					sizePanel
			};
			JOptionPane.showMessageDialog(parent, content, ACT_NAME, JOptionPane.PLAIN_MESSAGE);
		}
	}

}
