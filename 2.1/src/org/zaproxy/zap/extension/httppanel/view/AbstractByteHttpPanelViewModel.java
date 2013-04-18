package org.zaproxy.zap.extension.httppanel.view;


public abstract class AbstractByteHttpPanelViewModel extends DefaultHttpPanelViewModel {

    
    public abstract byte[] getData();

    
    public abstract void setData(byte[] data);
}
