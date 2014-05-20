package cz.zcu.kiv.multiclouddesktop.data;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.dialog.BrowseDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.data/BrowseCallback.java			<br /><br />
 *
 * Callback for displaying the contents of a folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class BrowseCallback implements BackgroundCallback<FileInfo> {

	/** List for showing folder content. */
	private final JList<FileInfo> dataList;
	/** Parent dialog. */
	private final BrowseDialog parent;

	/**
	 * Ctor with necessary parameters.
	 * @param parent Parent dialog.
	 * @param dataList List for showing folder content.
	 */
	public BrowseCallback(BrowseDialog parent, JList<FileInfo> dataList) {
		this.parent = parent;
		this.dataList = dataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, String accountName, FileInfo result) {
		/* update the data list */
		if (result == null) {
			parent.finishBrowse(null);
		} else {
			parent.setCurrentFolder(result);
			DefaultListModel<FileInfo> model = (DefaultListModel<FileInfo>) dataList.getModel();
			model.clear();
			if (!result.isRoot()) {
				model.addElement(parent.getParentFolder());
			}
			/* loop twice - first for folders, then for files */
			for (FileInfo f: result.getContent()) {
				if (f.getFileType() == FileType.FOLDER) {
					model.addElement(f);
				}
			}
			for (FileInfo f: result.getContent()) {
				if (f.getFileType() == FileType.FILE) {
					model.addElement(f);
				}
			}
			parent.finishBrowse("Folder listed.");
		}
	}

}
