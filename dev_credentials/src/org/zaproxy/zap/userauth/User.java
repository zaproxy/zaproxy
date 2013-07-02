package org.zaproxy.zap.userauth;

import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.utils.Enableable;

/**
 * ZAP representation of a web application user.
 */
public class User extends Enableable {

	/** The name. */
	private String name;

	/** The context. */
	private int contextId;

	/** The auth method. */
	private AuthenticationMethod authMethod;

	public User(int contextId, String name) {
		super();
		this.contextId = contextId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
