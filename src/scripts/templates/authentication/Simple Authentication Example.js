// The authenticate function will be called for authentications made via ZAP.

function authenticate(helper, credentials) {
	HttpMessage msg=helper.prepareMessage();
	println("Prepared message:"+msg);
}

function getRequiredParamsNames(){
	return [];
}

function getOptionalParamsNames(){
	return [];
}

function getCredentialsParamsNames(){
	return [];
}
