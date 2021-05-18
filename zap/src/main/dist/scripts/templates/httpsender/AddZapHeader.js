// The sendingRequest and responseReceived functions will be called for all requests/responses sent/received by ZAP, 
// including automated tools (e.g. active scanner, fuzzer, ...)

// Note that new HttpSender scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

// For the latest list of 'initiator' values see the HttpSender class:
// https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/java/org/parosproxy/paros/network/HttpSender.java
// 'helper' just has one method at the moment: helper.getHttpSender() which returns the HttpSender 
// instance used to send the request.

// In order to facilitate identifying ZAP traffic and Web Application Firewall exceptions, ZAP is accompanied 
// by this script which can be used to add a specific header to all traffic that passes through 
// or originates from ZAP. e.g.: X-ZAP-Initiator: 3

function sendingRequest(msg, initiator, helper) {
	// Add a ZAP identifier header to all traffic that originates with or passes through ZAP
	msg.getRequestHeader().setHeader("X-ZAP-Initiator", initiator);
}

function responseReceived(msg, initiator, helper) {
	// Nothing to do here
}