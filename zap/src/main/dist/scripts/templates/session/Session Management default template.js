// Session Management Scripts can be used to handle any session management mechanisms not supported by
// the built in classes.
//
// The SessionWrapper class provides the following methods:
//   getSession()       - returns a ScriptBasedSession
//   getHttpMessage()   - returns an HttpMessage
//   getParam(key)      - returns the parameter associated with the specified required or optional param name
//
// The ScriptBasedSession class provides the following methods:
//   setValue(key, value)    - the key must be a string but the value can be any object
//   getValue(key)

// Extract the script web session information from the Http Message and store in the ScriptBasedSession
function extractWebSession(sessionWrapper) {
	// You can add any objects to the session as required
	sessionWrapper.getSession().setValue("value1", "Example 1");
	sessionWrapper.getSession().setValue("value2", "Example 2");
}

// Clear any tokens or elements that can link the HttpMessage provided via the sessionWrapper parameter to the WebSession.
function clearWebSessionIdentifiers(sessionWrapper) {
}

// Modify the message so its Request Header/Body matches the given web session
function processMessageToMatchSession(sessionWrapper) {
	// You can retrieve any objects stored to the session as required
	var val1 = sessionWrapper.getSession().getValue("value1");
	var val2 = sessionWrapper.getSession().getValue("value2");
	var exampleTargetURL = sessionWrapper.getParam('exampleTargetURL');
	print('Got val1: ' + val1 + ' val2: ' + val2 + ' exampleTargetURL: ' + exampleTargetURL)
}

// This function is called during the script loading to obtain a list of the names of the required configuration parameters,
// that will be shown in the Session Properties -> Session Management panel for configuration. They can be used to input dynamic data into the script, 
// from the user interface (e.g. a JSONPath expression for extracting values from a JSON response, the name of a header  etc.)
function getRequiredParamsNames() {
	return ["exampleTargetURL", "exampleField2"];
}

// This function is called during the script loading to obtain a list of the names of the optional configuration parameters,
// that will be shown in the Session Properties -> Session Management panel for configuration. They can be used to input dynamic data into the script, 
// from the user interface (e.g. a login URL, name of POST parameters etc.)
function getOptionalParamsNames() {
	return ["exampleField3"];
}
