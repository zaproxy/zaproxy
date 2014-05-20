package org.zaproxy.zap.extension.authorization;

import org.parosproxy.paros.network.HttpMessage;

/**
 * Defines the process of identifying how the server responds to unauthorized requests.
 */
public interface AuthorizationDetectionMethod {

	/**
	 * Checks whether the responds received was for an unauthorized request.
	 */
	public boolean isResponseForUnauthorizedRequest(HttpMessage message);

	/**
	 * Clones this detection method, creating a deep-copy of it.
	 * 
	 * @return a deep copy of the object
	 */
	public AuthorizationDetectionMethod clone();
}
