// The authenticate function will be called for authentications made via ZAP.

function authenticate(as, msg) {
	println('authenticate called for url=' + msg.getRequestHeader().getURI().toString()); 
}
