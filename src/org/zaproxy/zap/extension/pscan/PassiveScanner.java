package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;

public interface PassiveScanner {

	public void scanHttpRequestSend(HttpMessage msg, int id);
	
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source);
	
	public void setParent (PassiveScanThread parent);

	public String getName();
	
	public void setEnabled (boolean enabled);
	
	public boolean isEnabled();
}
