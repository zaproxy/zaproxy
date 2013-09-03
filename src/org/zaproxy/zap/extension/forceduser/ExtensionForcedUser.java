package org.zaproxy.zap.extension.forceduser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.userauth.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

public class ExtensionForcedUser extends ExtensionAdaptor implements ContextPanelFactory {

	public ExtensionForcedUser() {
		super();
		initialize();
	}

	/** The Constant EXTENSION DEPENDENCIES. */
	private static final List<Class<?>> EXTENSION_DEPENDENCIES;
	static {
		// Prepare a list of Extensions on which this extension depends
		List<Class<?>> dependencies = new ArrayList<>();
		dependencies.add(ExtensionUserManagement.class);
		EXTENSION_DEPENDENCIES = Collections.unmodifiableList(dependencies);
	}

	/**
	 * Initialize the extension.
	 */
	private void initialize() {
		this.setName(NAME);
		this.setOrder(202);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);

		if (getView() != null) {
			// Factory for generating Session Context UserAuth panels
			getView().addContextPanelFactory(this);
		}
	}

	/** The NAME of the extension. */
	public static final String NAME = "ExtensionSessionManagement";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionForcedUser.class);

	/** The map of context panels. */
	private Map<Integer, ContextForcedUserPanel> contextPanelsMap = new HashMap<>();

	private Map<Integer, User> contextForcedUsersMap = new HashMap<>();

	private ExtensionUserManagement extensionUserManagement;

	protected ExtensionUserManagement getUserManagementExtension() {
		if (extensionUserManagement == null) {
			extensionUserManagement = (ExtensionUserManagement) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionUserManagement.NAME);
		}
		return extensionUserManagement;
	}

	@Override
	public List<Class<?>> getDependencies() {
		return EXTENSION_DEPENDENCIES;
	}

	public void setForcedUser(int contextId, User user) {
		this.contextForcedUsersMap.put(contextId, user);
	}

	public User getForcedUser(int contextId) {
		return this.contextForcedUsersMap.get(contextId);
	}

	@Override
	public AbstractContextPropertiesPanel getContextPanel(Context context) {
		ContextForcedUserPanel panel = this.contextPanelsMap.get(context.getIndex());
		if (panel == null) {
			panel = new ContextForcedUserPanel(this, context.getIndex());
			this.contextPanelsMap.put(context.getIndex(), panel);
		}
		return panel;
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public void discardContexts() {
		// TODO Auto-generated method stub

	}

}
