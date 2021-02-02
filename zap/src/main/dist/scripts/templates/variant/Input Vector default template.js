// The parseParameter function will typically be called for every page and 
// the setParameter function is called by each active plugin to bundle specific attacks.

// Note that new custom input vector scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

// The helper parameter leveraged within this variant provides access to the methods in VariantCustom 
// https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/java/org/parosproxy/paros/core/scanner/VariantCustom.java

var B64STATE = 'b64';
var paramStates = [];

function parseParameters(helper, msg) {
    // Debugging can be done using println like this
    print('Custom input vector handler called for url=' + msg.getRequestHeader().getURI().toString());

    // Sample scan of a query string object
    // searching for Base64 encoded parameters
    var query = msg.getRequestHeader().getURI().getEscapedQuery();

    if (query == null) {
        return;
    }

    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");

        if (helper.isBase64(decodeURIComponent(pair[1]))) {
            var value = helper.decodeBase64(decodeURIComponent(pair[1]));
            helper.addParamQuery(pair[0], value);
            paramStates.push(B64STATE);
        } else {
            helper.addParamQuery(pair[0], pair[1]);
            paramStates.push('');
        }
    }
}

function setParameter(helper, msg, param, value, escaped) {
    var size = helper.getParamNumber();
    var query = "";
    var pos = helper.getCurrentParam().getPosition();

    for (var i = 0; i < size; i++) {
        var pname = helper.getParamName(i);
        var pvalue = helper.getParamValue(i);

        if (paramStates[i] === B64STATE) { //Handle values that were originally base64
            if (i == pos) {
                //Encode the injected value
                pvalue = encodeURIComponent(helper.encodeBase64(value));
            } else {
                //Use the original value
                pvalue = encodeURIComponent(helper.encodeBase64(pvalue));
            }
        } else { //Handle regular values
            if (i == pos) {
                if (escaped == false) {
                    value = encodeURIComponent(value);
                }
                pvalue = value;
            }
        }
        query = pname + "=" + pvalue + "&" + query;
    }
    //Strip trailing ampersand
    if (query.substr(-1) == '&') {
        query=query.slice(0,-1);
    }
    //Set the parameters
    msg.getRequestHeader().getURI().setEscapedQuery(query);
}

/**
 * Gets the name of the node to be used for the given {@code msg} in the Site Map. Returning null is
 * taken to mean use the default name. This is currently the last element of the path (given in
 * {@code nodeName}) followed by the url parameter names in brackets (if any) followed by the
 * form parameter names in brackets (if any).
 *
 * @param helper a helper class as per parseParameters
 * @param nodeName the last element of the path
 * @param msg the message
 * @return the name of the node to be used in the Site Map
 */
function getLeafName(helper, nodeName, msg) {
	return null;
}

/**
 * Returns the tree path elements for the given {@code message}. Returning null is taken to mean
 * use the default methods for obtaining tree path elements.
 * This will determine the position of this message in the sites tree.
 *
 * <p>By default the elements that are returned for the following URLs are:
 *
 * <ul>
 *   <li><i>http://example.org/path/to/element?aa=bb&cc==dd</i> : ["path", "to", "element"]
 *   <li><i>http://example.org/path/to/element</i> : ["path", "to", "element"]
 *   <li><i>http://example.org/path/to/</i> : ["path", "to"]
 *   <li><i>http://example.org/path/to</i> : ["path", "to"]
 * </ul>
 *
 * @param helper a helper class as per parseParameters
 * @param msg the message for which the tree path elements will be extracted from. This is an HttpMessage object.
 * @return a {@code List} containing the tree path elements
 */
function getTreePath(helper, msg) {
	var uri = msg.getRequestHeader().getURI()
	print("getTreePath " + uri + " path=" + uri.getPath())
	return null
}

