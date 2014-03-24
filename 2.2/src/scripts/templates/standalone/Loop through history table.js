// This script loops through the history table - change it to do whatever you want to do :)
//
// Standalone scripts have no template.
// They are only evaluated when you run them. 

extHist = org.parosproxy.paros.control.Control.getSingleton().
    getExtensionLoader().getExtension(
        org.parosproxy.paros.extension.history.ExtensionHistory.NAME) 
if (extHist != null) {
    i=1
    // Loop through the history table, printing out the history id and the URL
    while (hr = extHist.getHistoryReference(i), hr) {
        if (hr) { 
            url = hr.getHttpMessage().getRequestHeader().getURI().toString();
            println('Got History record id ' + hr.getHistoryId() + ' URL=' + url); 
        }
        i++
    }
}


