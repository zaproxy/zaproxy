package org.zaproxy.zap.extension.httppanel.view.models;

import org.zaproxy.zap.extension.httppanel.view.DefaultHttpPanelViewModel;

public abstract class AbstractStringHttpPanelViewModel extends DefaultHttpPanelViewModel {

	public abstract String getData();
	public abstract void setData(String data);
}
