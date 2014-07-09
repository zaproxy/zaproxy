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
import org.zaproxy.clientapi.gen.Authentication;

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
public class AuthenticationApiExample {

	private static void listInformation() throws ClientApiException {
		ClientApi api = new ClientApi("localhost", 8090);
		Authentication authApi = new Authentication(api);

		// Check out which authentication methods are supported by the API
		List<String> supportedMethodNames = new LinkedList<>();
		ApiResponseList authMethodsList = (ApiResponseList) authApi.getSupportedAuthenticationMethods();
		for (ApiResponse authMethod : authMethodsList.getItems()) {
			supportedMethodNames.add(((ApiResponseElement) authMethod).getValue());
		}
		System.out.println("Supported authentication methods: " + supportedMethodNames);

		// Check out which are the config parameters of the authentication methods
		for (String methodName : supportedMethodNames) {

			ApiResponseList configParamsList = (ApiResponseList) authApi
					.getAuthenticationMethodConfigParams(methodName);

			for (ApiResponse r : configParamsList.getItems()) {
				ApiResponseSet set = (ApiResponseSet) r;
				System.out.println("'" + methodName + "' config param: " + set.getAttribute("name") + " ("
						+ (set.getAttribute("mandatory").equals("true") ? "mandatory" : "optional") + ")");
			}
		}
	}

	private static void setFormBasedAuthenticationForBodgeit() throws ClientApiException,
			UnsupportedEncodingException {
		ClientApi api = new ClientApi("localhost", 8090);
		Authentication authApi = new Authentication(api);

		// Setup the authentication method
		String contextId = "1";
		String loginUrl = "http://localhost:8080/bodgeit/login.jsp";
		String loginRequestData = "username=%username%&password=%password%";

		// Prepare the configuration in a format similar to how URL parameters are formed. This
		// means that any value we add for the configuration values have to be URL encoded.
		StringBuilder formBasedConfig = new StringBuilder();
		formBasedConfig.append("loginUrl=").append(URLEncoder.encode(loginUrl, "UTF-8"));
		formBasedConfig.append("&loginRequestData=").append(URLEncoder.encode(loginRequestData, "UTF-8"));

		System.out.println("Setting form based authentication configuration as: "
				+ formBasedConfig.toString());
		authApi.setAuthenticationMethod(null, contextId, "formBasedAuthentication",
				formBasedConfig.toString());

		// Check if everything is set up ok
		System.out
				.println("Authentication config: " + authApi.getAuthenticationMethod(contextId).toString(0));
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws ClientApiException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws ClientApiException, UnsupportedEncodingException {
		listInformation();
		System.out.println("-------------");
		setFormBasedAuthenticationForBodgeit();
	}
}
