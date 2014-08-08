// The fuzz script will be called for every request sent by the fuzzer

// Note that new fuzz scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  


// This function is called for each payload directly before insertion.
// Use p.getData() and P.setData() to access and modify the payload value.
function processPayload(p){
	p.setData(p.getData() + '1');
} 

// This function is called for each request after the payloads have been inserted
// and before the request is sent.
// The arguments are the message corresponding to the request and a map between
// The fuzzlocations and payloads that have been inserted into those locations.
function preProcess(msg, p){
	// Debugging can be done using println like this
	println('Fuzzing called for url=' + msg.getRequestHeader().getURI().toString())
}
// This function is called for each fuzzResult obtained
// It receives the fuzzResult as an argument - the corresponding request can be obtained
// by calling res.getMessage()
function postProcess(res){
	if(res.getState() == HttpFuzzResult.STATE_REFLECTED || res.getState() == FuzzResult.STATE_ERROR){
		println('Attempt failes')
		res.setState(FuzzResult.STATE_CUSTOM)
		res.setCustom('Custom comment')
	}
}
