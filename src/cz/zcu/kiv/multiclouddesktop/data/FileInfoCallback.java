package cz.zcu.kiv.multiclouddesktop.data;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.data/FileInfoCallback.java			<br /><br />
 *
 * Callback for displaying the contents of a folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileInfoCallback implements BackgroundCallback<FileInfo> {

	/** List of accounts. */
	private final JList<AccountData> accountList;
	/** List for showing folder content. */
	private final JList<FileInfo> dataList;

	/**
	 * Ctor with necessary parameters.
	 * @param accountList List of accounts.
	 * @param dataList List for showing folder content.
	 */
	public FileInfoCallback(JList<AccountData> accountList, JList<FileInfo> dataList) {
		this.accountList = accountList;
		this.dataList = dataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, String accountName, FileInfo result) {
		/* update the account list */
		for (int i = 0; i < accountList.getModel().getSize(); i++) {
			AccountData account = accountList.getModel().getElementAt(i);
			if (account.getName().equals(accountName)) {
				account.setListed(true);
			} else {
				account.setListed(false);
			}
		}
		accountList.revalidate();
		accountList.repaint();
		/* update the data list */
		DefaultListModel<FileInfo> model = (DefaultListModel<FileInfo>) dataList.getModel();
		model.clear();
		if (!result.isRoot()) {
			model.addElement(MultiCloudDesktop.getWindow().getParentFolder());
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
	}

}
