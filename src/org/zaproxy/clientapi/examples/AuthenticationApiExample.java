/* Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP development team
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
package org.zaproxy.clientapi.examples;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ApiResponseSet;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

/**
 * An example of how to set up authentication via the API and get information about existing
 * configuration.
 * 
 * Some important aspects regarding the Authentication API:
 * <ul>
 * <li>
 * since the AuthenticationMethods are loaded dynamically, there's no way to generate a 'static' API
 * for each auth method. That's why, when setting up the authentication method, depending on the
 * method, different values are passed to the setAuthenticationMethod . This is where the
 * getSupportedAuthenticationMethods and getAuthenticationMethodConfigParams methods come into play.
 * Basically the first one gives a list of available/loaded authentication methods while the second
 * one gives info about the parameters required to configure each authentication method type.</li>
 * <li>when setting up the authentication method for a context, the setAuthenticationMethod method
 * is used. It takes the context id on which we're working, the name of the authentication method
 * and a 'authMethodConfigParams' parameter which contains all the configuration for the method. The
 * format of the value passed for 'authMethodConfigParams' matches the www-form-urlencoded style:
 * parameterName = urlEncodedValue. Check out the referenced example to see how the configuration is
 * build for the BodgeIt store login. The pseudocode for generating the config would be:<br/>
 * <code>paramA + "=" + urlEncode(paramAValue) + "&" + paramB + "=" + urlEncode(paramBValue) + ...</code>
 * </li>
 * <li>
 * for formBasedAuthentication, the places filled in with the credentials are marked via {%username%}
 * and {%password%}, in either the requestUrl or the requestBody</li>
 * </ul>
 */
public class AuthenticationApiExample {

	private static final String ZAP_ADDRESS = "localhost";
	private static final int ZAP_PORT = 8090;
	private static final String ZAP_API_KEY = null;

	private static void listAuthInformation(ClientApi clientApi) throws ClientApiException {
		// Check out which authentication methods are supported by the API
		List<String> supportedMethodNames = new LinkedList<>();
		ApiResponseList authMethodsList = (ApiResponseList) clientApi.authentication.getSupportedAuthenticationMethods();
		for (ApiResponse authMethod : authMethodsList.getItems()) {
			supportedMethodNames.add(((ApiResponseElement) authMethod).getValue());
		}
		System.out.println("Supported authentication methods: " + supportedMethodNames);

		// Check out which are the config parameters of the authentication methods
		for (String methodName : supportedMethodNames) {

			ApiResponseList configParamsList = (ApiResponseList) clientApi.authentication
					.getAuthenticationMethodConfigParams(methodName);

			for (ApiResponse r : configParamsList.getItems()) {
				ApiResponseSet set = (ApiResponseSet) r;
				System.out.println("'" + methodName + "' config param: " + set.getAttribute("name") + " ("
						+ (set.getAttribute("mandatory").equals("true") ? "mandatory" : "optional") + ")");
			}
		}
	}

	private static void listUserConfigInformation(ClientApi clientApi) throws ClientApiException {
		// Check out which are the config parameters required to set up an user with the currently
		// set authentication methods
		String contextId = "1";
		ApiResponseList configParamsList = (ApiResponseList) clientApi.users
				.getAuthenticationCredentialsConfigParams(contextId);

		StringBuilder sb = new StringBuilder("Users' config params: ");
		for (ApiResponse r : configParamsList.getItems()) {
			ApiResponseSet set = (ApiResponseSet) r;
			sb.append(set.getAttribute("name")).append(" (");
			sb.append((set.getAttribute("mandatory").equals("true") ? "mandatory" : "optional"));
			sb.append("), ");
		}
		System.out.println(sb.deleteCharAt(sb.length() - 2).toString());
	}

	private static void setLoggedInIndicator(ClientApi clientApi) throws UnsupportedEncodingException, ClientApiException {
		// Prepare values to set, with the logged in indicator as a regex matching the logout link
		String loggedInIndicator = "<a href=\"logout.jsp\"></a>";
		String contextId = "1";

		// Actually set the logged in indicator
		clientApi.authentication.setLoggedInIndicator(ZAP_API_KEY, contextId, java.util.regex.Pattern.quote(loggedInIndicator));

		// Check out the logged in indicator that is set
		System.out.println("Configured logged in indicator regex: "
				+ ((ApiResponseElement) clientApi.authentication.getLoggedInIndicator(contextId)).getValue());
	}

	private static void setFormBasedAuthenticationForBodgeit(ClientApi clientApi) throws ClientApiException,
			UnsupportedEncodingException {
		// Setup the authentication method
		String contextId = "1";
		String loginUrl = "http://localhost:8080/bodgeit/login.jsp";
		String loginRequestData = "username={%username%}&password={%password%}";

		// Prepare the configuration in a format similar to how URL parameters are formed. This
		// means that any value we add for the configuration values has to be URL encoded.
		StringBuilder formBasedConfig = new StringBuilder();
		formBasedConfig.append("loginUrl=").append(URLEncoder.encode(loginUrl, "UTF-8"));
		formBasedConfig.append("&loginRequestData=").append(URLEncoder.encode(loginRequestData, "UTF-8"));

		System.out.println("Setting form based authentication configuration as: "
				+ formBasedConfig.toString());
		clientApi.authentication.setAuthenticationMethod(ZAP_API_KEY, contextId, "formBasedAuthentication",
				formBasedConfig.toString());

		// Check if everything is set up ok
		System.out
				.println("Authentication config: " + clientApi.authentication.getAuthenticationMethod(contextId).toString(0));
	}

	private static void setUserAuthConfigForBodgeit(ClientApi clientApi) throws ClientApiException, UnsupportedEncodingException {
		// Prepare info
		String contextId = "1";
		String user = "Test User";
		String username = "test@example.com";
		String password = "weakPassword";

		// Make sure we have at least one user
		String userId = extractUserId(clientApi.users.newUser(ZAP_API_KEY, contextId, user));

		// Prepare the configuration in a format similar to how URL parameters are formed. This
		// means that any value we add for the configuration values has to be URL encoded.
		StringBuilder userAuthConfig = new StringBuilder();
		userAuthConfig.append("username=").append(URLEncoder.encode(username, "UTF-8"));
		userAuthConfig.append("&password=").append(URLEncoder.encode(password, "UTF-8"));

		System.out.println("Setting user authentication configuration as: " + userAuthConfig.toString());
		clientApi.users.setAuthenticationCredentials(ZAP_API_KEY, contextId, userId, userAuthConfig.toString());

		// Check if everything is set up ok
		System.out.println("Authentication config: " + clientApi.users.getUserById(contextId, userId).toString(0));
	}

	private static String extractUserId(ApiResponse response) {
		return ((ApiResponseElement) response).getValue();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws ClientApiException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws ClientApiException, UnsupportedEncodingException {
		ClientApi clientApi = new ClientApi(ZAP_ADDRESS, ZAP_PORT);

		listAuthInformation(clientApi);
		System.out.println("-------------");
		setFormBasedAuthenticationForBodgeit(clientApi);
		System.out.println("-------------");
		setLoggedInIndicator(clientApi);
		System.out.println("-------------");
		listUserConfigInformation(clientApi);
		System.out.println("-------------");
		setUserAuthConfigForBodgeit(clientApi);
	}
}
