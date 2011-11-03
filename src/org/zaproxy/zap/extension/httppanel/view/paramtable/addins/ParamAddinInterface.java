package org.zaproxy.zap.extension.httppanel.view.paramtable.addins;

import java.io.UnsupportedEncodingException;

public interface ParamAddinInterface {
	public String convertData(String data) throws UnsupportedEncodingException;
	public String getName();
}
