// The parseParameter function will typically be called for every page and 
// the setParameter function is called by each active plugin to bundle specific attacks

// Note that new custom input vector scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

function parseParameters(helper, msg) {
    // Debugging can be done using println like this
    println('custom input vector handler called for url=' + msg.getRequestHeader().getURI().toString());
    
    // Sample scan of a query string object
    // searching for Base64 encoded parameters
    query = msg.getRequestHeader().getURI().getQuery();
    
    if (query == null) {
        return;
    }
    
    vars = query.split("&");
    nob64 = "";
    for (var i = 0; i < vars.length; i++) {
        pair = vars[i].split("=");
        if (helper.isBase64(pair[1])) {
            value = helper.decodeBase64(pair[1]);
            helper.addParamQuery(pair[0], value);
            
        } else {
            nob64 = nob64 + "&" + vars;
        }
    }
    
    // We add all the other parameters as a unique string inside the last one
    if (helper.getParamNumber() > 0) {
        helper.addParamQuery("notb64", nob64);
    }
}

function setParameter(helper, msg, param, value, escaped) {
    size = helper.getParamNumber();
    if (size > 1) {         
        query = helper.getParamValue(size);
        
        for (var i = 0; i < size; i++) {
            pname = getParamName(i);
            pvalue = getParamValue(i);                
            if (pname == param) {      
                    pvalue = helper.encodeBase64(value);
            }
                    
            query = pname + "=" + pvalue + "&" + query;
        }
            
        msg.getRequestHeader().getURI().setEscapedQuery(query);
    }
}
