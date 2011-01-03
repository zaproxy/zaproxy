package org.zaproxy.zap.extension.pscan;

import org.parosproxy.paros.network.HttpMessage;

import net.htmlparser.jericho.Source;

public interface PassiveScanner {

	public void scanHttpRequestSend(HttpMessage msg, int id);
	
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source);
	
	public void setParent (PassiveScanThread parent);

	public String getName();
	
}
