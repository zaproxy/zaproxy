/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;

/**
 * Custom model for displaying some {@link CommunicationChannel} objects.
 */
public class OptionsWebSocketTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final String[] columnNames = {
		Constant.messages.getString("websocket.options.label.domain"), 
		Constant.messages.getString("websocket.options.label.port")
	};
    
    private List<CommunicationChannel> blacklistedChannels = new ArrayList<CommunicationChannel>();
    
    /**
     * 
     */
    public OptionsWebSocketTableModel() {
        super();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return blacklistedChannels.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
    	CommunicationChannel channel = blacklistedChannels.get(rowIndex);

        switch (columnIndex) {
        	case 0:
        		return channel.getDomain();
        		
        	case 1:
        		return channel.getPort();
        		
        	default:
        		return "";
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    /**
     * @param listAuth The listAuth to set.
     */
    public void setBlacklistedChannels(List<CommunicationChannel> channels) {
        blacklistedChannels = new ArrayList<CommunicationChannel>(channels);
  	  	fireTableDataChanged();
    }
    
    public List<CommunicationChannel> getBlacklistedChannels() {
        return new ArrayList<CommunicationChannel>(blacklistedChannels);
    }

    public CommunicationChannel getBlacklistedChannel(int index) {
    	return blacklistedChannels.get(index);
    }

    public void addBlacklistedChannel(CommunicationChannel channel) {
        blacklistedChannels.add(channel);
  	  	fireTableDataChanged();
    }

	public void replaceBlacklistedChannel(int index, CommunicationChannel channel) {
        blacklistedChannels.remove(index);
        blacklistedChannels.add(index, channel);
  	  	fireTableDataChanged();
	}

	public void removeBlacklistedChannel(int index) {
        blacklistedChannels.remove(index);
  	  	fireTableDataChanged();
	}

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
	public Class<String> getColumnClass(int columnIndex) {
        return String.class;
    }
}
