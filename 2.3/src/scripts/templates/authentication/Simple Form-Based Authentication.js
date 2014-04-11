// This authentication script can be used to authenticate in a webapplication via forms.
// The submit target for the form, the name of the username field, the name of the password field
// and, optionally, any extra POST Data fields need to be specified after loading the script.
// The username and the password need to be configured when creating any Users.

// The authenticate function is called whenever ZAP requires to authenticate, for a Context for which this script
// was selected as the Authentication Method. The function should send any messages that are required to do the authentication
// and should return a message with an authenticated response so the calling method.
//
// NOTE: Any message sent in the function should be obtained using the 'helper.prepareMessage()' method.
//
// Parameters:
//		helper - a helper class providing useful methods: prepareMessage(), sendAndReceive(msg)
//		paramsValues - the values of the parameters configured in the Session Properties -> Authentication panel.
//					The paramsValues is a map, having as keys the parameters names (as returned by the getRequiredParamsNames()
//					and getOptionalParamsNames() functions below)
//		credentials - an object containing the credentials values, as configured in the Session Properties -> Users panel.
//					The credential values can be obtained via calls to the getParam(paramName) method. The param names are the ones
//					returned by the getCredentialsParamsNames() below

function authenticate(helper, paramsValues, credentials) {
	println("Authenticating via JavaScript script...");

	// Make sure any Java classes used explicitly are imported
	importClass(org.parosproxy.paros.network.HttpRequestHeader)
	importClass(org.parosproxy.paros.network.HttpHeader)
	importClass(org.apache.commons.httpclient.URI)

	// Prepare the login request details
	requestUri = new URI(paramsValues.get("Target URL"), false);
	requestMethod = HttpRequestHeader.POST;
	
	// Build the request body using the credentials values
	extraPostData = paramsValues.get("Extra POST data");
	requestBody = paramsValues.get("Username field") + "=" + encodeURIComponent(credentials.getParam("Username"));
	requestBody+= "&" + paramsValues.get("Password field") + "=" + encodeURIComponent(credentials.getParam("Password"));
	if(extraPostData.trim().length() > 0)
		requestBody += "&" + extraPostData.trim();

	// Build the actual message to be sent
	println("Sending " + requestMethod + " request to " + requestUri + " with body: " + requestBody);
	msg = helper.prepareMessage();
	msg.setRequestHeader(new HttpRequestHeader(requestMethod, requestUri, HttpHeader.HTTP10));
	msg.setRequestBody(requestBody);

	// Send the authentication message and return it
	helper.sendAndReceive(msg);
	println("Received response status code: " + msg.getResponseHeader().getStatusCode());

	return msg;
}

// This function is called during the script loading to obtain a list of the names of the required configuration parameters,
// that will be shown in the Session Properties -> Authentication panel for configuration. They can be used
// to input dynamic data into the script, from the user interface (e.g. a login URL, name of POST parameters etc.)
function getRequiredParamsNames(){
	return ["Target URL", "Username field", "Password field"];
}

// This function is called during the script loading to obtain a list of the names of the optional configuration parameters,
// that will be shown in the Session Properties -> Authentication panel for configuration. They can be used
// to input dynamic data into the script, from the user interface (e.g. a login URL, name of POST parameters etc.)
function getOptionalParamsNames(){
	return ["Extra POST data"];
}

// This function is called during the script loading to obtain a list of the names of the parameters that are required,
// as credentials, for each User configured corresponding to an Authentication using this script 
function getCredentialsParamsNames(){
	return ["Username", "Password"];
}
