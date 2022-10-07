// A script which will raise alerts based on unexpected Content-Types
// By default it will raise 'Low' level alerts for content types that are not expected to be returned by APIs.
// But it can be easily changed.

var control, model
if (!control) control = Java.type("org.parosproxy.paros.control.Control").getSingleton()
if (!model) model = Java.type("org.parosproxy.paros.model.Model").getSingleton()

var Pattern = Java.type("java.util.regex.Pattern")

var pluginid = 100001	// https://github.com/zaproxy/zaproxy/blob/main/docs/scanners.md

var extensionAlert = control.getExtensionLoader().getExtension(org.zaproxy.zap.extension.alert.ExtensionAlert.NAME)

var expectedTypes = [
		"application/health+json",
		"application/json",
		"application/octet-stream",
		"application/problem+json",
		"application/problem+xml",
		"application/soap+xml",
		"application/vnd.api+json",
		"application/xml",
		"application/x-ndjson",
		"application/x-yaml",
		"text/x-json",
		"text/json",
		"text/yaml",
		"text/plain"
	]

function sendingRequest(msg, initiator, helper) {
	// Nothing to do
}

function responseReceived(msg, initiator, helper) {
	if (isGloballyExcluded(msg) || initiator == 7) { // CHECK_FOR_UPDATES_INITIATOR
		// Not of interest.
		return
	}

	if (extensionAlert != null) {
		var ctype = msg.getResponseHeader().getHeader("Content-Type")
		if (ctype != null) {
			if (ctype.indexOf(";") > 0) {
				ctype = ctype.substring(0, ctype.indexOf(";"))
			}
			if (expectedTypes.indexOf(ctype) < 0) {
				// Another rule will complain if theres no type
		
				var risk = 1	// Low
				var title = "Unexpected Content-Type was returned"
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
				alert.setDescription("A Content-Type of " + ctype + " was returned by the server.\n" +
					"This is not one of the types expected to be returned by an API.\n" +
					"Raised by the 'Alert on Unexpected Content Types' script");
				alert.setEvidence(ctype)
				extensionAlert.alertFound(alert , ref)
			}
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
