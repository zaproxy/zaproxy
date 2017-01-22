// Targeted scripts can only be invoked by you, the user, eg via a right-click option on the Sites or History tabs

// The following handles differences in printing between Java 7's Rhino JS engine
// and Java 8's Nashorn JS engine
if (typeof println == 'undefined') this.println = print;

/**
 * A function which will be invoked against a specific "targeted" message.
 *
 * @param msg - the HTTP message being acted upon. This is an HttpMessage object.
 */
function invokeWith(msg) {
	// Debugging can be done using println like this
	println('invokeWith called for url=' + msg.getRequestHeader().getURI().toString()); 
}
