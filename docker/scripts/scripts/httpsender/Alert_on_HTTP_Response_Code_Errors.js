// A script which will raise alerts based on HTTP Response codes
// By default it will raise 'Info' level alerts for Client Errors (4xx) (apart from 404s) and 'Low' Level alerts for Server Errors (5xx)
// But it can be easily changed.

const Integer = Java.type("java.lang.Integer")
const Pattern = Java.type("java.util.regex.Pattern")

const Alert = Java.type("org.parosproxy.paros.core.scanner.Alert")
const ExtensionAlert = Java.type("org.zaproxy.zap.extension.alert.ExtensionAlert")
const HistoryReference = Java.type("org.parosproxy.paros.model.HistoryReference")

const extensionAlert = control.getExtensionLoader().getExtension(ExtensionAlert.NAME)

pluginid = 100000	// https://github.com/zaproxy/zaproxy/blob/main/docs/scanners.md

function sendingRequest(msg, initiator, helper) {
	// Nothing to do
}

function responseReceived(msg, initiator, helper) {
	if (isGloballyExcluded(msg)) {
		// Not of interest.
		return
	}

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
			var alert = new Alert(pluginid, risk, 3, title)
			var ref = msg.getHistoryRef()
			if (ref != null && HistoryReference.getTemporaryTypes().contains(Integer.valueOf(ref.getHistoryType()))) {
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
				ref = new HistoryReference(model.getSession(), type, msg)
			}
			alert.setMessage(msg)
			alert.setUri(msg.getRequestHeader().getURI().toString())
			alert.setDescription("A response code of " + code + " was returned by the server.\n" +
				"This may indicate that the application is failing to handle unexpected input correctly.\n" +
				"Raised by the 'Alert on HTTP Response Code Error' script");
			alert.setEvidence(code.toString())
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
