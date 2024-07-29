// A standalone script to be run at the end of an AF test plan.
// If there are AF errors then it will print out all of the relevant requests and responses.

var extAF = control.getExtensionLoader().getExtension("ExtensionAutomation");
var plans = extAF.getRunningPlans();
if (plans.get(0).getProgress().hasErrors()) {
   var extHist = control.getExtensionLoader().getExtension(
     org.parosproxy.paros.extension.history.ExtensionHistory.NAME);
   if (extHist != null) {
      i=1;
      lastRef=extHist.getLastHistoryId();
      // Get current max history reference 
      // Loop through the history table, printing out the history id and the URL
      while (i <= lastRef) {
         hr = extHist.getHistoryReference(i);
         if (hr) { 
            url = hr.getHttpMessage().getRequestHeader().getURI().toString();
            if (url.startsWith('http://localhost') && url.indexOf('.css') === -1) {
               print('=================='); 
               print(hr.getHttpMessage().getRequestHeader());
               print(hr.getHttpMessage().getRequestBody()); 
               print('========='); 
               print(hr.getHttpMessage().getResponseHeader());
               print(hr.getHttpMessage().getResponseBody());  
             }
          }
       i++;
      }
   }
}
