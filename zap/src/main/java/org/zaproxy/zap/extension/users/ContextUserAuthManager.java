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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.Role;
import org.zaproxy.zap.users.User;

/**
 * The Manager that handles all the information related to {@link User Users}, {@link Role Roles}
 * and authentications corresponding to a particular {@link Context}.
 */
public class ContextUserAuthManager {

    /** The context id. */
    private int contextId;

    /** The model. */
    private List<User> users;

    public ContextUserAuthManager(int contextId) {
        this.contextId = contextId;
        this.users = new ArrayList<>();
    }

    /**
     * Builds a table model for the users.
     *
     * @return the model
     */
    public UsersTableModel getUsersModel() {
        return new UsersTableModel(this.users);
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
        return Collections.unmodifiableList(users);
    }

    /**
     * Sets a new list of users for this context. An internal copy of the provided list is stored.
     *
     * @param users the users
     * @throws IllegalArgumentException (since 2.8.0) if any of the given users is {@code null}.
     */
    public void setUsers(List<User> users) {
        users.forEach(u -> validateNonNull(u));
        this.users = new ArrayList<>(users);
    }

    /**
     * Adds an user.
     *
     * @param user the user
     * @throws IllegalArgumentException (since 2.8.0) if the given user is {@code null}.
     */
    public void addUser(User user) {
        validateNonNull(user);
        users.add(user);
    }

    /**
     * Validates that the given {@code user} is non-{@code null}.
     *
     * @param user the user to validate.
     * @throws IllegalArgumentException if the given user is {@code null}.
     */
    private static void validateNonNull(User user) {
        if (user == null) {
            throw new IllegalArgumentException("The parameter user must not be null.");
        }
    }

    /**
     * Removes an user.
     *
     * @param user the user
     * @throws IllegalArgumentException (since 2.8.0) if the given user is {@code null}.
     */
    public void removeUser(User user) {
        validateNonNull(user);
        users.remove(user);
    }

    /**
     * Gets the user with a given id.
     *
     * @param id the id
     * @return the user by id
     */
    public User getUserById(int id) {
        for (User u : users) if (u.getId() == id) return u;
        return null;
    }

    /**
     * Removes the user with a given id.
     *
     * @param id the id
     * @return true, if successful
     */
    public boolean removeUserById(int id) {
        Iterator<User> it = users.iterator();
        while (it.hasNext())
            if (it.next().getId() == id) {
                it.remove();
                return true;
            }
        return false;
    }

    /** Removes all the users. */
    public void removeAllUsers() {
        this.users.clear();
    }
}
