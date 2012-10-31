/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.websocket.ui;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;

public class ComboBoxChannelModel implements ComboBoxModel<WebSocketChannelDTO> {

    private ChannelSortedListModel delegate;

    private Object selectedItem;

    public ComboBoxChannelModel(ChannelSortedListModel model) {
        super();

        this.delegate = model;
    }

    public void setSelectedChannelId(Integer channelId) {
        if (channelId == null) {
            setSelectedItem(getElementAt(0));
            return;
        }

        for (int i = 0; i < getSize(); i++) {
            WebSocketChannelDTO channel = getElementAt(i);
            if (channelId.equals(channel.id)) {
                setSelectedItem(channel);
                return;
            }
        }
    }

    public int getIndexOf(Object element) {
        return delegate.indexOf(element);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = anItem;

        delegate.elementChanged(-1);
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public WebSocketChannelDTO getElementAt(int index) {
        return delegate.elementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        delegate.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        delegate.removeListDataListener(l);
    }
    
}