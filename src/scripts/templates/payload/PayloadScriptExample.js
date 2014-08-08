// The fuzz script will be added to a target section as would any other payload
// insertPayloads() will be called during the start of the fuzz process

// Note that new fuzz scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

// This function has the target gap to be manipulated and the current payloadFactory as arguments
function insertPayloads(gap, pf) {
	// Debugging can be done using println like this
	println('initial text: ' + gap.orig());
	// As in the dialog three types of payloads can be defined:
	// 1. Simple String payloads (containing one String each)
	gap.addPayload(pf.createPayload("Simple Payload Text"))
	gap.addPayload(pf.createPayload(gap.orig()))

	// 2. Pre-defined fuzzing files
	// Mark as "FILE" and pass 'category + " --> " + file name' as argument
	gap.addPayload(pf.createPayload(org.zaproxy.zap.extension.multiFuzz.Payload.Type.FILE, "jbrofuzz / Base --> Base02 (binary)"))
	// 3. Regular expressions
	// Due to the use of a custom generator not all operators are accessible. Possible expressions contain:
	//	- literals
	//  - concatenation and bracketing
	//	- union '|'
	//	- '?', '*' and '+' as usual repetition operators
	//	- [b-c] as a range of possible characters
	//  - '.', 'w' for any character or letter
	//	- '\d' for any digit
	//	- for any whitespace '\s'
	// Mark as "REGEX" and pass the expression and a limit for the number of payloads to be generated as arguments
	regex = pf.createPayload(org.zaproxy.zap.extension.multiFuzz.Payload.Type.REGEX,"(a|\\d|[bcd]z)*h", 30)
	// Set the maximum length of payload strings that are generated
	regex.setLength(4)
	gap.addPayload(regex)
}
