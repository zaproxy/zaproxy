function insertPayloads(gap, pf) {
	gap.addPayload(pf.createPayload(gap.orig()))
	gap.addPayload(pf.createPayload("TEST"))
	regex = pf.createPayload("REGEX","a*", 5)
	regex.setLength(4)
	gap.addPayload(regex)
}
function count() {
	return 3
}
