// Targeted scripts can only be invoked by you, the user, eg via a right-click option on the Sites or History tabs

function invokeWith(msg) {
	// Debugging can be done using println like this
	println('invokeWith called for url=' + msg.getRequestHeader().getURI().toString()); 
}
