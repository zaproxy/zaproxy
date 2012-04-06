package org.zaproxy.zap.extension.httppanel.view.models;

import org.zaproxy.zap.extension.httppanel.view.DefaultHttpPanelViewModel;

public abstract class AbstractByteHttpPanelViewModel extends DefaultHttpPanelViewModel {

	public abstract byte[] getData();
	public abstract void setData(byte[] data);
}
