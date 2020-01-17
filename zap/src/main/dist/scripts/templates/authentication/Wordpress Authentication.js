// This authentication script can be used to authenticate in a Wordpress application.
// You need to set the Authentication method to Script-Based and load the script, then set the parameters as follows:
//	Domain: The domain (without protocol). E.g.: example.com
//	Path: The path, with a '/' at the end, in which the app is found. E.g.: /wp/
// The parameters must chosen such that: "http://" + domain + path + "wp-login.php"  is the uri of the login page. In case https is used,modify the script below.

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
	print("Wordpress Authenticating via JavaScript script...");

	// Make sure any Java classes used explicitly are imported
	var HttpRequestHeader = Java.type("org.parosproxy.paros.network.HttpRequestHeader")
	var HttpHeader = Java.type("org.parosproxy.paros.network.HttpHeader")
	var URI = Java.type("org.apache.commons.httpclient.URI")
	var Cookie = Java.type("org.apache.commons.httpclient.Cookie")

	// Prepare the login request details
	var domain = paramsValues.get("Domain");
	var path = paramsValues.get("Path");
	print("Logging in to domain " + domain + " and path " + path);

	var requestUri = new URI("http://"+domain + path + "wp-login.php", false);
	var requestMethod = HttpRequestHeader.POST;

	// Build the request body using the credentials values
	var requestBody = "log=" + encodeURIComponent(credentials.getParam("Username"));
	requestBody = requestBody + "&pwd=" + encodeURIComponent(credentials.getParam("Password"));
	requestBody = requestBody + "&rememberme=forever&wp-submit=Log+In&testcookie=1";

	// Add the proper cookie to the header
	var requestHeader = new HttpRequestHeader(requestMethod, requestUri, HttpHeader.HTTP10);
	requestHeader.setHeader(HttpHeader.COOKIE, "wordpress_test_cookie=WP+Cookie+check");

	// Build the actual message to be sent
	print("Sending " + requestMethod + " request to " + requestUri + " with body: " + requestBody);
	var msg = helper.prepareMessage();
	msg.setRequestHeader(requestHeader);
	msg.setRequestBody(requestBody);

	// Send the authentication message and return it
	helper.sendAndReceive(msg);
	print("Received response status code for authentication request: " + msg.getResponseHeader().getStatusCode());

	// The path Wordpress sets on the session cookies is illegal according to the standard. The web browsers ignore this and use the cookies anyway, but the Apache Commons HttpClient used in ZAP really cares about this (probably the only one who does it) and simply ignores the "invalid" cookies [0] , [1],so we must make sure we MANUALLY add the response cookies
	if(path != "/" && path.charAt(path.length() - 1) == '/')	{
		path = path.substring(0, path.length() - 1);
	}
	print("Cleaned cookie path: " + path);

	var cookies = msg.getResponseHeader().getCookieParams();
	var state = helper.getCorrespondingHttpState();
	for(var iterator = cookies.iterator(); iterator.hasNext();){
		var cookie = iterator.next();
		print("Manually adding cookie: " + cookie.getName() + " = " + cookie.getValue());
		state.addCookie(new Cookie(domain, cookie.getName(), cookie.getValue(), path, 999999, false));
	}

	return msg;
}

// This function is called during the script loading to obtain a list of the names of the required configuration parameters,
// that will be shown in the Session Properties -> Authentication panel for configuration. They can be used
// to input dynamic data into the script, from the user interface (e.g. a login URL, name of POST parameters etc.)
function getRequiredParamsNames(){
	return ["Domain", "Path"];
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
	return ["Username", "Password"];
}

