// Targeted scripts can only be invoked by you, the user, e.g. via a right-click option on the Sites or History tabs

function invokeWith(msg) {
	// Debugging can be done using println like this
	print('Finding comments in ' + msg.getRequestHeader().getURI().toString()); 

	var body = msg.getResponseBody().toString()
	// Look for html comments
	if (body.indexOf('<!--') > 0) {
		var o = body.indexOf('<!--');
		while (o > 0) {
			var e = body.indexOf('-->', o);
			print("\t" + body.substr(o,e-o+3)) 
			o = body.indexOf('<!--', e);
		}
	}
}

