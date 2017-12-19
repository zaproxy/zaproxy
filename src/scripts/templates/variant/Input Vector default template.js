// The parseParameter function will typically be called for every page and 
// the setParameter function is called by each active plugin to bundle specific attacks

// Note that new custom input vector scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

// The helper parameter leveraged within this variant provides access to the methods
// in VariantCustom 
// https://github.com/zaproxy/zaproxy/blob/develop/src/org/parosproxy/paros/core/scanner/VariantCustom.java

var B64STATE = 'b64';
var paramStates = [];

function parseParameters(helper, msg) {
    // Debugging can be done using println like this
    print('Custom input vector handler called for url=' + msg.getRequestHeader().getURI().toString());

    // Sample scan of a query string object
    // searching for Base64 encoded parameters
    query = msg.getRequestHeader().getURI().getEscapedQuery();

    if (query == null) {
        return;
    }

    vars = query.split("&");
    nob64 = "";
    for (var i = 0; i < vars.length; i++) {
        pair = vars[i].split("=");

        if (helper.isBase64(decodeURIComponent(pair[1]))) {
            value = helper.decodeBase64(decodeURIComponent(pair[1]));
            helper.addParamQuery(pair[0], value);
            paramStates.push(B64STATE);
        } else {
            helper.addParamQuery(pair[0], pair[1]);
            paramStates.push('');
        }
    }
}

function setParameter(helper, msg, param, value, escaped) {
    size = helper.getParamNumber();
    query = "";

    for (var i = 0; i < size; i++) {
        pname = helper.getParamName(i);
        pvalue = helper.getParamValue(i);

        if (paramStates[i] === B64STATE) { //Handle values that were originally base64
            if (pname == param) {
                //Encode the injected value
                pvalue = encodeURIComponent(helper.encodeBase64(value));
            } else {
                //Use the original value
                pvalue = encodeURIComponent(helper.encodeBase64(pvalue));
            }
        } else { //Handle regular values
            if (pname == param) {
                if (escaped == false) {
                    value = encodeURIComponent(value);
                }
                pvalue = value;
            } else {
                pvalue = pvalue;
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
