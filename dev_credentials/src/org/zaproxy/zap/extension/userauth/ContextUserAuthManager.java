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
package org.zaproxy.zap.extension.userauth;

import java.util.List;

import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.Role;
import org.zaproxy.zap.userauth.User;

/**
 * The Manager that handles all the information related to {@link User Users}, {@link Role Roles}
 * and authentications corresponding to a particular {@link Context}.
 */
public class ContextUserAuthManager {

	/** The context id. */
	private int contextId;

	/** The model. */
	private UserAuthUserTableModel model;

	public ContextUserAuthManager(int contextId) {
		this.contextId = contextId;
	}

	/**
	 * Gets a table model for the users.
	 * 
	 * @return the model
	 */
	public UserAuthUserTableModel getUsersModel() {
		if (model == null) {
			model = new UserAuthUserTableModel();
		}
		return model;
	}

	/**
	 * Gets the context id to which this object corresponds.
	 * 
	 * @return the context id
	 */
	public int getContextId() {
		return contextId;
	}

	/**
	 * Gets an unmodifiable view of the list of users.
	 * 
	 * @return the users
	 */
	public List<User> getUsers() {
		return model.getUsers();
	}

	/**
	 * Adds an user.
	 *
	 * @param user the user
	 */
	public void addUser(User user) {
		model.addElement(user);
	}

	/**
	 * Removes an user.
	 *
	 * @param user the user
	 */
	public void removeUser(User user) {
		int index = model.getUsers().indexOf(user);
		model.removeElement(index);
	}
}
