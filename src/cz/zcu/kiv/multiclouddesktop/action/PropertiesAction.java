package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.utils.Utils;
import cz.zcu.kiv.multicloud.utils.Utils.UnitsFormat;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/PropertiesAction.java			<br /><br />
 *
 * Action for displaying file or folder information.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class PropertiesAction extends CloudAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -1190973332086544513L;

	/** Name of the action. */
	public static final String ACT_NAME = "Properties";

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent frame.
	 */
	public PropertiesAction(MultiCloudDesktop parent) {
		super(parent);
		putValue(NAME, ACT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		FileInfo f = parent.getDataList().getSelectedValue();
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
			JLabel lblIdTxt = new JLabel("-");
			if (f.getId() != null) {
				lblIdTxt.setText(f.getId());
			}
			lblIdTxt.setFont(boldFont);
			idPanel.add(lblId);
			idPanel.add(lblIdTxt);
			JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblType = new JLabel("Type:");
			lblType.setPreferredSize(new Dimension(60, lblType.getPreferredSize().height));
			StringBuilder sb = new StringBuilder(f.getFileType().getText());
			if (f.isDeleted()) {
				sb.append(" - deleted");
			}
			if (f.isShared()) {
				sb.append(" - shared");
			}
			JLabel lblTypeTxt = new JLabel(sb.toString());
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
			JPanel checksumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblChecksum = new JLabel("Checksum:");
			lblChecksum.setPreferredSize(new Dimension(60, lblChecksum.getPreferredSize().height));
			JLabel lblChecksumTxt = new JLabel("-");
			if (f.getChecksum() != null) {
				lblChecksumTxt.setText(f.getChecksum());
			}
			lblChecksumTxt.setFont(boldFont);
			checksumPanel.add(lblChecksum);
			checksumPanel.add(lblChecksumTxt);
			JComponent[] content = new JComponent[] {
					namePanel,
					idPanel,
					typePanel,
					sizePanel,
					checksumPanel
			};
			JOptionPane.showMessageDialog(parent, content, ACT_NAME, JOptionPane.PLAIN_MESSAGE);
		}
	}

}
