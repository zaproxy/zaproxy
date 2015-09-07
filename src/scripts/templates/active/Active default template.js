// The scanNode function will typically be called once for every page 
// The scan function will typically be called for every parameter in every URL and Form for every page 

// Note that new active scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  


function scanNode(as, msg) {
	// Debugging can be done using println like this
	println('scan called for url=' + msg.getRequestHeader().getURI().toString());

	// Copy requests before reusing them
	msg = msg.cloneRequest();
	
	// sendAndReceive(msg, followRedirect, handleAntiCSRFtoken)
	as.sendAndReceive(msg, false, false);

	// Test the responses and raise alerts as below

	// Check if the scan was stopped before performing lengthy tasks
	if (as.isStop()) {
		return
	}
	// Do lengthy task...
}

function scan(as, msg, param, value) {
	// Debugging can be done using println like this
	println('scan called for url=' + msg.getRequestHeader().getURI().toString() + 
		' param=' + param + ' value=' + value);
	
	// Copy requests before reusing them
	msg = msg.cloneRequest();
	
	// setParam (message, parameterName, newValue)
	as.setParam(msg, param, 'Your attack');
	
	// sendAndReceive(msg, followRedirect, handleAntiCSRFtoken)
	as.sendAndReceive(msg, false, false);
	
	// Test the response here, and make other requests as required
	if (true) {	// Change to a test which detects the vulnerability
		// raiseAlert(risk, int confidence, String name, String description, String uri, 
		//		String param, String attack, String otherInfo, String solution, String evidence, 
		//		int cweId, int wascId, HttpMessage msg)
		// risk: 0: info, 1: low, 2: medium, 3: high
		// confidence: 0: falsePositive, 1: low, 2: medium, 3: high, 4: confirmed
		as.raiseAlert(1, 1, 'Active Vulnerability title', 'Full description', 
		msg.getRequestHeader().getURI().toString(), 
			param, 'Your attack', 'Any other info', 'The solution ', '', 0, 0, msg);
	}
}

