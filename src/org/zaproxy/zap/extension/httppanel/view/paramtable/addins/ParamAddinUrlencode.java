package org.zaproxy.zap.extension.httppanel.view.paramtable.addins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ParamAddinUrlencode implements ParamAddinInterface {
	@Override
	public String convertData(String data) throws UnsupportedEncodingException {
		return URLEncoder.encode(data, "UTF-8");
	}

	@Override
	public String getName() {
		return "URLEncode";
	}
	


}
