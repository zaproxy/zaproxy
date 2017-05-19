// A script which will raise alerts based on unexpected Content-Types
// By default it will raise 'Low' level alerts for content types that are not expected to be returned by APIs.
// But it can be easily changed.

// The following handles differences in printing between Java 7's Rhino JS engine
// and Java 8's Nashorn JS engine
if (typeof println == 'undefined') this.println = print;

var pluginid = 100001	// https://github.com/zaproxy/zaproxy/blob/develop/src/doc/scanners.md

var extensionAlert = org.parosproxy.paros.control.Control.getSingleton().getExtensionLoader().getExtension(
		org.zaproxy.zap.extension.alert.ExtensionAlert.NAME)

var expectedTypes = [
		"application/json",
		"application/octet-stream",
		"application/soap+xml",
		"application/xml",
		"application/x-yaml",
		"text/x-json",
		"text/json"
	]

function sendingRequest(msg, initiator, helper) {
	// Nothing to do
}

function responseReceived(msg, initiator, helper) {
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
						case 7:	// CHECK_FOR_UPDATES_INITIATOR
							type = 1 // Proxied 
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
					ref = new org.parosproxy.paros.model.HistoryReference(
						org.parosproxy.paros.model.Model.getSingleton().getSession(), type, msg)
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