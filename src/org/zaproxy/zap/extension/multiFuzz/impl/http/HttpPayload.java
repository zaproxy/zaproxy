package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.zaproxy.zap.extension.multiFuzz.Payload;

public class HttpPayload implements Payload {
	String data;
	String type;
	public void setData(String data){
		this.data = data;
	}
	
	public String getData(){
		return data;
	}
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	@Override
	public String toString() {
		return "#" + type + ": " + data;
	}
}
