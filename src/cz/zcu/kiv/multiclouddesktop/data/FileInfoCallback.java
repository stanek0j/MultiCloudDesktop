package cz.zcu.kiv.multiclouddesktop.data;

import javax.swing.DefaultListModel;

import cz.zcu.kiv.multicloud.json.FileInfo;

public class FileInfoCallback implements BackgroundCallback<FileInfo> {

	private final DefaultListModel<FileInfo> model;

	public FileInfoCallback(DefaultListModel<FileInfo> model) {
		this.model = model;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, FileInfo result) {
		model.clear();
		for (FileInfo f: result.getContent()) {
			model.addElement(f);
		}
	}

}
