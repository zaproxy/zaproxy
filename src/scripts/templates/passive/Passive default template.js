// Passive scan rules should not make any requests 

// Note that new passive scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

/**
 * Passively scans an HTTP message. The scan function will be called for 
 * request/response made via ZAP, excluding some of the automated tools.
 * 
 * @param ps - the PassiveScan parent object that will do all the core interface tasks 
 *     (i.e.: providing access to Threshold settings, raising alerts, etc.). 
 *     This is an ScriptsPassiveScanner object.
 * @param msg - the HTTP Message being scanned. This is an HttpMessage object.
 * @param src - the Jericho Source representation of the message being scanned.
 */
function scan(ps, msg, src) {
	// Test the request and/or response here
	if (true) {	// Change to a test which detects the vulnerability
		// raiseAlert(risk, int confidence, String name, String description, String uri, 
		//		String param, String attack, String otherInfo, String solution, String evidence, 
		//		int cweId, int wascId, HttpMessage msg)
		// risk: 0: info, 1: low, 2: medium, 3: high
		// confidence: 0: falsePositive, 1: low, 2: medium, 3: high, 4: confirmed
		ps.raiseAlert(1, 1, 'Passive Vulnerability title', 'Full description', 
			msg.getRequestHeader().getURI().toString(), 
			'The param', 'Your attack', 'Any other info', 'The solution', '', 0, 0, msg);
			
	}
	
	// Raise less reliable alert (that is, prone to false positives) when in LOW alert threshold (level)
	// Expected values: "LOW", "MEDIUM", "HIGH"
	if (ps.getLevel() == "LOW") {
		// ...
	}
}
