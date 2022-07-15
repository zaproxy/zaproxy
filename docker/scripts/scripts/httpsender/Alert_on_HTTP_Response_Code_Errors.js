// A script which will raise alerts based on HTTP Response codes
// By default it will raise 'Info' level alerts for Client Errors (4xx) (apart from 404s) and 'Low' Level alerts for Server Errors (5xx)
// But it can be easily changed.

var control, model
if (!control) control = Java.type("org.parosproxy.paros.control.Control").getSingleton()
if (!model) model = Java.type("org.parosproxy.paros.model.Model").getSingleton()

var Pattern = Java.type("java.util.regex.Pattern")

pluginid = 100000	// https://github.com/zaproxy/zaproxy/blob/main/docs/scanners.md

function sendingRequest(msg, initiator, helper) {
	// Nothing to do
}

function responseReceived(msg, initiator, helper) {
	if (isGloballyExcluded(msg) || initiator == 7) { // CHECK_FOR_UPDATES_INITIATOR
		// Not of interest.
		return
	}

	var extensionAlert = control.getExtensionLoader().getExtension(org.zaproxy.zap.extension.alert.ExtensionAlert.NAME)
	if (extensionAlert != null) {
		var code = msg.getResponseHeader().getStatusCode()
		if (code < 400 || code >= 600) {
			// Do nothing
		} else {
			var risk = 0	// Info
			var title = "A Client Error response code was returned by the server"
			if (code >= 500) {
				// Server error
				risk = 1	// Low
				title = "A Server Error response code was returned by the server"
			}
			// CONFIDENCE_HIGH = 3 (we can be pretty sure we're right)
			var alert = new org.parosproxy.paros.core.scanner.Alert(pluginid, risk, 3, title)
			var ref = msg.getHistoryRef()
			if (ref != null && org.parosproxy.paros.model.HistoryReference.getTemporaryTypes().contains(
						java.lang.Integer.valueOf(ref.getHistoryType()))) {
				// Dont use temporary types as they will get deleted
				ref = null
			}
			if (ref == null) {
				// map the initiator
				var type
				switch (initiator) {
					case 1:	// PROXY_INITIATOR
						type = 1 // Proxied 
						break
					case 2:	// ACTIVE_SCANNER_INITIATOR
						type = 3 // Scanner 
						break
					case 3:	// SPIDER_INITIATOR
						type = 2 // Spider 
						break
					case 4:	// FUZZER_INITIATOR
						type = 8 // Fuzzer 
						break
					case 5:	// AUTHENTICATION_INITIATOR
						type = 15 // User 
						break
					case 6:	// MANUAL_REQUEST_INITIATOR
						type = 15 // User 
						break
					case 8:	// BEAN_SHELL_INITIATOR
						type = 15 // User 
						break
					case 9:	// ACCESS_CONTROL_SCANNER_INITIATOR
						type = 13 // Access control 
						break
					default:
						type = 15 // User - fallback
						break
				}
				ref = new org.parosproxy.paros.model.HistoryReference(model.getSession(), type, msg)
			}
			alert.setMessage(msg)
			alert.setUri(msg.getRequestHeader().getURI().toString())
			alert.setDescription("A response code of " + code + " was returned by the server.\n" +
				"This may indicate that the application is failing to handle unexpected input correctly.\n" +
				"Raised by the 'Alert on HTTP Response Code Error' script");
			// Use a regex to extract the evidence from the response header
			var regex = new RegExp("^HTTP.*" + code)
			alert.setEvidence(msg.getResponseHeader().toString().match(regex))
			alert.setCweId(388)	// CWE CATEGORY: Error Handling
			alert.setWascId(20)	// WASC  Improper Input Handling
			extensionAlert.alertFound(alert , ref)
		}
	}
}

function isGloballyExcluded(msg) {
	var url = msg.getRequestHeader().getURI().toString()
	var regexes = model.getSession().getGlobalExcludeURLRegexs()
	for (var i in regexes) {
		if (Pattern.compile(regexes[i], Pattern.CASE_INSENSITIVE).matcher(url).matches()) {
			return true
		}
	}
	return false
}
