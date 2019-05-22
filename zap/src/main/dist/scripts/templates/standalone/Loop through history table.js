// This script loops through the history table - change it to do whatever you want to do :)
//
// Standalone scripts have no template.
// They are only evaluated when you run them. 

extHist = org.parosproxy.paros.control.Control.getSingleton().
    getExtensionLoader().getExtension(
        org.parosproxy.paros.extension.history.ExtensionHistory.NAME) 
if (extHist != null) {
    i=1
    lastRef=extHist.getLastHistoryId();// Get current max history reference 
    // Loop through the history table, printing out the history id and the URL
    while (i <= lastRef) {
        hr = extHist.getHistoryReference(i)
        if (hr) { 
            url = hr.getHttpMessage().getRequestHeader().getURI().toString();
            print('Got History record id ' + hr.getHistoryId() + ' URL=' + url); 
        }
        i++
    }
}


