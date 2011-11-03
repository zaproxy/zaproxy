package org.zaproxy.zap.extension.httppanel.view.paramtable.addins;

public class ParamAddinMagic implements ParamAddinInterface {
	@Override
	public String convertData(String data) {
		return data + "__ZAP__";
	}

	@Override
	public String getName() {
		return "Insert Magic";
	}
}
