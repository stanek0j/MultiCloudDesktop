package cz.zcu.kiv.multiclouddesktop.data;

import java.util.List;

import javax.swing.DefaultListModel;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.dialog.SearchDialog;

/**
 * cz.zcu.kiv.multiclouddesktop.data/SearchCallback.java			<br /><br />
 *
 * Callback for displaying the results of the search.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SearchCallback implements BackgroundCallback<List<FileInfo>> {

	/** File to be matched. */
	private final FileInfo match;
	/** Dialog with the search. */
	private final SearchDialog dialog;

	/**
	 * Ctor with necessary parameters.
	 * @param match File to be matched.
	 * @param dialog Dialog with the search.
	 */
	public SearchCallback(FileInfo match, SearchDialog dialog) {
		this.match = match;
		this.dialog = dialog;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinish(BackgroundTask task, String accountName, List<FileInfo> result) {
		DefaultListModel<FileInfo> model = (DefaultListModel<FileInfo>) dialog.getList().getModel();
		model.clear();
		if (result == null) {
			dialog.finishSearch(null);
		} else {
			if (result.isEmpty()) {
				dialog.finishSearch("No results.");
			} else {
				for (FileInfo f: result) {
					if (match != null) {
						if (match.getChecksum() != null) {
							if (match.getChecksum().equals(f.getChecksum())) {
								model.addElement(f);
							}
						} else {
							if (match.getSize() == f.getSize() && match.getFileType() == f.getFileType()) {
								model.addElement(f);
							}
						}
					} else {
						model.addElement(f);
					}
				}
				if (model.isEmpty()) {
					dialog.finishSearch("No results.");
				} else {
					dialog.finishSearch("Found " + model.getSize() + " matches.");
				}
			}
		}
	}

}
