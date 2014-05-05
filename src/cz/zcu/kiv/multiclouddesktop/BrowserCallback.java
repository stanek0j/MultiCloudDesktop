package cz.zcu.kiv.multiclouddesktop;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback;
import cz.zcu.kiv.multicloud.oauth2.AuthorizationRequest;
import cz.zcu.kiv.multiclouddesktop.dialog.AuthorizeDialog;

public class BrowserCallback implements AuthorizationCallback {

	private final AuthorizeDialog dialog;

	public BrowserCallback(AuthorizeDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAuthorizationRequest(AuthorizationRequest request) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(new URI(request.getRequestUri()));
			dialog.setAlwaysOnTop(false);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
