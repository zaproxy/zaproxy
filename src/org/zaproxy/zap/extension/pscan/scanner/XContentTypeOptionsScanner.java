package org.zaproxy.zap.extension.pscan.scanner;

import java.util.Vector;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class XContentTypeOptionsScanner  extends PluginPassiveScanner {

	private PassiveScanThread parent = null;
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		
		
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getResponseBody().length() > 0) {
			Vector<String> xContentTypeOptions = msg.getResponseHeader().getHeaders(HttpHeader.X_CONTENT_TYPE_OPTIONS);
			if (xContentTypeOptions == null) {
				this.raiseAlert(msg, id, "");
			} else {
				for (String xContentTypeOptionsDirective : xContentTypeOptions) {
					if (xContentTypeOptionsDirective.toLowerCase().indexOf("nosniff") < 0) {
						this.raiseAlert(msg, id, xContentTypeOptionsDirective);
					}
				}
			} 
		}
	}
		
	private void raiseAlert(HttpMessage msg, int id, String xContentTypeOption) {
		Alert alert = new Alert(getId(), Alert.RISK_LOW, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    		"The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'",
		    	    msg.getRequestHeader().getURI().toString(),
		    	    xContentTypeOption,
		    	    "", 
		    	    "", 
		    	    "This check is specific to Internet Explorer 8 and Google Chrome. Ensure each page sets a Content-Type header and the X-CONTENT-TYPE-OPTIONS if the Content-Type header is unknown", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
		

	@Override
	public void setParent(PassiveScanThread parent) {
			this.parent = parent;
	}

	@Override
	public String getName() {
		return "X-Content-Type-Options header missing";
	}
	
	private int getId() {
		return 10021;
	}

}
