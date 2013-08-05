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
import org.zaproxy.zap.model.Context;

/**
 * The abstract class that should be extended by panels that are shown in the "Session Properties" dialog, for each of the Contexts. 
 */
@SuppressWarnings("serial")
public abstract class AbstractContextPropertiesPanel extends AbstractParamPanel {

	@Override
	public void initParam(Object obj) {
		throw new RuntimeException("InitParam should not be called on Abstract Context Property panels.");
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
	 * The {@code uiCommonContext} parameter provided is a duplicate of the Context to which the
	 * panel corresponds and should be used to store any changes done in the UI until saving. This
	 * Context is shared between all the {@link AbstractContextPropertiesPanel Context Properties
	 * Panels} so any change in one of the panels can be reflected in other panels, if needed.
	 * </p>
	 * 
	 * @param session the session
	 * @param uiCommonContext the Context shared between the Context Properties Panels
	 * 
	 */
	public abstract void initContextData(Session session, Context uiCommonContext);

	/**
	 * Validate the context data shown in the UI. This method is the equivalent of
	 * {@link AbstractParamPanel#validateParam(Object)}.
	 * 
	 * @param session the session
	 * 
	 */
	public abstract void validateContextData(Session session) throws Exception;

	/**
	 * Saves the changes done in the UI. This method is the equivalent of
	 * {@link AbstractParamPanel#saveParam(Object)}.
	 * 
	 * @param session the session
	 */
	public abstract void saveContextData(Session session) throws Exception;

	/**
	 * Gets the intex of the Context corresponding to this panel.
	 * 
	 * @return the context's index/id
	 */
	public abstract int getContextIndex();

}
