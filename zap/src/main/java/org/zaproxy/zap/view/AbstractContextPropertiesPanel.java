/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.view;

import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.SessionDialog;
import org.zaproxy.zap.model.Context;

/**
 * The abstract class that should be extended by panels that are shown in the "Session Properties"
 * dialog, for each of the Contexts.
 * 
 * <p>
 * The UI shared context is a duplicate of the Context to which the panel corresponds and should be
 * used to load any data in the UI and store any changes done in the UI until permanent saving. This
 * Context is shared between all the {@link AbstractContextPropertiesPanel Context Properties
 * Panels} so any change in one of the panels can be reflected in other panels, if needed. It can be
 * obtained through calls to {@link #getUISharedContext()}.
 * </p>
 */
@SuppressWarnings("serial")
public abstract class AbstractContextPropertiesPanel extends AbstractParamPanel {

	private int contextId;

	private SessionDialog sessionDialog;

	/**
	 * Instantiates a new abstract context properties panel.
	 * 
	 * @param contextId the context id
	 */
	public AbstractContextPropertiesPanel(int contextId) {
		super();
		this.contextId = contextId;
	}

	/**
	 * Sets the session dialog to which this panel corresponds.
	 * 
	 * @param sessionDialog the new session dialog
	 */
	public void setSessionDialog(SessionDialog sessionDialog) {
		this.sessionDialog = sessionDialog;
	}

	/**
	 * Gets the UI shared context. The UI shared context is a duplicate of the Context to which the
	 * panel corresponds and should be used to load any data in the UI and store any changes done in
	 * the UI until permanent saving. This Context is shared between all the
	 * {@link AbstractContextPropertiesPanel Context Properties Panels} so any change in one of the
	 * panels can be reflected in other panels, if needed.
	 * 
	 * @return the UI shared context
	 */
	protected Context getUISharedContext() {
		return this.sessionDialog.getUISharedContext(this.contextId);
	}

	@Override
	public void initParam(Object obj) {
		// Ignore
	}

	@Override
	public void validateParam(Object obj) throws Exception {
		validateContextData((Session) obj);

	}

	@Override
	public void saveParam(Object obj) throws Exception {
		saveContextData((Session) obj);
	}

	/**
	 * Inits the data shown on the panel. This method is the equivalent of
	 * {@link AbstractParamPanel#initParam(Object)}.
	 * <p>
	 * The {@code uiSharedContext} parameter provided is a duplicate of the Context to which the
	 * panel corresponds and should be used to load any data in the UI and store any changes done in
	 * the UI until saving. This Context is shared between all the
	 * {@link AbstractContextPropertiesPanel Context Properties Panels} so any change in one of the
	 * panels can be reflected in other panels, if needed. It can also be obtained through calls to
	 * {@link #getUISharedContext()}
	 * </p>
	 * 
	 * @param session the session
	 * @param uiSharedContext the Context shared between the Context Properties Panels
	 * 
	 */
	public abstract void initContextData(Session session, Context uiSharedContext);

	/**
	 * Validate the context data shown in the UI. This method is the equivalent of
	 * {@link AbstractParamPanel#validateParam(Object)}.
	 * <p>
	 * The message of the exception is expected to be internationalised (as it might be shown in GUI components, for example, an
	 * error dialogue).
	 * 
	 * @param session the session
	 * @throws Exception if there's any validation error.
	 */
	public abstract void validateContextData(Session session) throws Exception;

	/**
	 * Saves the changes done in the UI into the UI Shared Context, so it is available when other
	 * context panels require it. Usually, this method is called when the panel is hidden (
	 * {@link #onHide()}).
	 * <p>
	 * The @{code uiSharedContext} parameter provided is a duplicate of the Context to which the
	 * panel corresponds and should be used to load any data in the UI and store any changes done in
	 * the UI until saving. This Context is shared between all the
	 * {@link AbstractContextPropertiesPanel Context Properties Panels} so any change in one of the
	 * panels can be reflected in other panels, if needed.
	 * </p>
	 * 
	 * @param uiSharedContext the ui shared context
	 */
	public abstract void saveTemporaryContextData(Context uiSharedContext);

	/**
	 * Saves and permanently persists the changes done in the UI. This method is the equivalent of
	 * {@link AbstractParamPanel#saveParam(Object)}.
	 * 
	 * @param session the session
	 * @throws Exception if there's any error while saving the data.
	 */
	public abstract void saveContextData(Session session) throws Exception;

	/**
	 * Gets the index of the context to which this panel corresponds.
	 * 
	 * @return the context index
	 */
	public int getContextIndex() {
		return contextId;
	}

	@Override
	public void onHide() {
		super.onHide();
		this.saveTemporaryContextData(getUISharedContext());
	}

}
