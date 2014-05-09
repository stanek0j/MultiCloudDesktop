package cz.zcu.kiv.multiclouddesktop.data;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

public class FileInfoCallback implements BackgroundCallback<FileInfo> {

	private final JList<AccountData> accountList;
	private final JList<FileInfo> dataList;

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
			model.addElement(MultiCloudDesktop.getWindow().peekParentFolder());
		}
		for (FileInfo f: result.getContent()) {
			model.addElement(f);
		}
	}

}
