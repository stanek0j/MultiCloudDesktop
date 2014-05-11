package cz.zcu.kiv.multiclouddesktop.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import cz.zcu.kiv.multiclouddesktop.MultiCloudDesktop;

/**
 * cz.zcu.kiv.multiclouddesktop.action/CloudAction.java			<br /><br />
 *
 * Generic template for any action that requires a reference to parent frame.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public abstract class CloudAction extends AbstractAction {

	/** Serialization constant. */
	private static final long serialVersionUID = -872182402906940468L;

	/** Parent frame. */
	protected final MultiCloudDesktop parent;

	/**
	 * Ctor with parent frame.
	 * @param parent Parent frame.
	 */
	public CloudAction(MultiCloudDesktop parent) {
		this.parent = parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void actionPerformed(ActionEvent event);

}
