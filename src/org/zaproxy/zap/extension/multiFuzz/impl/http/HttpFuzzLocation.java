package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.multiFuzz.FuzzLocation;
import org.zaproxy.zap.extension.multiFuzz.TextFuzzLocation;

public class HttpFuzzLocation implements TextFuzzLocation<HttpMessage>{
	
	public final int start;
	public final int end;
	
	public HttpFuzzLocation(int s, int e){
		start = s;
		end = e;
	}
	@Override
	public int begin() {
		return start;
	}
	@Override
	public int end() {
		return end;
	}


	@Override
	public int compareTo(FuzzLocation<HttpMessage> loc2) {
		if(loc2 instanceof TextFuzzLocation<?>){
			return start - ((TextFuzzLocation<HttpMessage>)loc2).begin();
		}
		else return 0;
	}


	@Override
	public String getRepresentation(HttpMessage msg) {
		int headLen = msg.getRequestHeader().toString().length();
		if(start > headLen){
			return msg.getRequestBody().toString().substring(start - headLen, end - headLen);
		}
		else{
			return msg.getRequestHeader().toString().substring(start, end);
		}
	}
	@Override
	public boolean overlap(FuzzLocation<HttpMessage> loc) {
		if(loc instanceof TextFuzzLocation<?>){
			if(begin() <= ((TextFuzzLocation<HttpMessage>)loc).begin()){
				return end() > ((TextFuzzLocation<HttpMessage>)loc).begin(); 
			}
			else{
				return begin() < ((TextFuzzLocation<HttpMessage>)loc).end();
			}
		}
		return true;
	}

}
