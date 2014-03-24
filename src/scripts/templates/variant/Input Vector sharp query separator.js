// The parseParameter function will typically be called for every page and 
// the setParameter function is called by each active plugin to bundle specific attacks

// Note that new custom input vector scripts will initially be disabled
// Right click the script in the Scripts tree and select "enable"  

function parseParameters(helper, msg) {
    // Debugging can be done using println like this
    println('Google variant called for url=' + msg.getRequestHeader().getURI().toString());
    
    query = msg.getRequestHeader().getURI().toString();
    
    if (query == null) {
        return;
    }
    
    idx = query.indexOf("#");
    if (idx >= 0) {
        data = query.substring(idx + 1);
        vars = data.split("&");
        for (var i = 0; i < vars.length; i++) {
          pair = vars[i].split("=");
          helper.addParamQuery(pair[0], pair[1]);
        }
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
                    pvalue = value;
            }
                    
            query = pname + "=" + pvalue + "&" + query;
        }
        
        uri = msg.getRequestHeader().getURI().toString();
        idx = uri.indexOf("#");
        uri = uri.substring(0, idx);
        uobj = new URI(uri + "#" + query);
        msg.getRequestHeader().setURI(uobj);
    }
}
