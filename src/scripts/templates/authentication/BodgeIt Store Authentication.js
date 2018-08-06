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
	print("Authenticating via JavaScript script...");
	
	// Make sure any Java classes used explicitly are imported
	var HttpRequestHeader = Java.type("org.parosproxy.paros.network.HttpRequestHeader")
	var HttpHeader = Java.type("org.parosproxy.paros.network.HttpHeader")
	var URI = Java.type("org.apache.commons.httpclient.URI")

	// Prepare the login request details
	var requestUri = new URI("http://localhost:8080/bodgeit/login.jsp", false);
	var requestMethod = HttpRequestHeader.POST;
	// Build the request body using the credentials values
	var requestBody = "username="+encodeURIComponent(credentials.getParam("username"));
	requestBody+= "&password="+encodeURIComponent(credentials.getParam("password"));

	// Build the actual message to be sent
	var msg=helper.prepareMessage();
	msg.setRequestHeader(new HttpRequestHeader(requestMethod, requestUri, HttpHeader.HTTP10));
	msg.setRequestBody(requestBody);

	// Send the authentication message and return it
	helper.sendAndReceive(msg);
	print("Received BodgeIt response status code: "+ msg.getResponseHeader().getStatusCode());

	return msg;
}

// This function is called during the script loading to obtain a list of the names of the required configuration parameters,
// that will be shown in the Session Properties -> Authentication panel for configuration. They can be used
// to input dynamic data into the script, from the user interface (e.g. a login URL, name of POST parameters etc.)
function getRequiredParamsNames(){
	return [];
}

// This function is called during the script loading to obtain a list of the names of the optional configuration parameters,
// that will be shown in the Session Properties -> Authentication panel for configuration. They can be used
// to input dynamic data into the script, from the user interface (e.g. a login URL, name of POST parameters etc.)
function getOptionalParamsNames(){
	return [];
}

// This function is called during the script loading to obtain a list of the names of the parameters that are required,
// as credentials, for each User configured corresponding to an Authentication using this script 
function getCredentialsParamsNames(){
	return ["username", "password"];
}
