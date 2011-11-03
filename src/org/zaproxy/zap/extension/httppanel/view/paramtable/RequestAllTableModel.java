package org.zaproxy.zap.extension.httppanel.view.paramtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HtmlParameter;

public class RequestAllTableModel extends AbstractTableModel {
	private static final String[] columnNames = {
		"Type",
    	Constant.messages.getString("http.panel.table.paramName"),
    	Constant.messages.getString("http.panel.table.paramValue"),
    	"Functions"
    	};
	
	private HttpMessage httpMessage = null;
	TreeSet<HtmlParameter> cookies, post, get;
	private LinkedList<HtmlParameter> allParams;
	private boolean isGetChanged = false;
	private boolean isPostChanged = false;
	private boolean isCookieChanged = false;
	private boolean isEditable = false;
	private JComboBox comboBox = new JComboBox();
	
	public RequestAllTableModel() {
		init();
	}
	
	public RequestAllTableModel(HttpMessage httpMessage, boolean isEditable) {
		this.httpMessage = httpMessage;
		this.isEditable = isEditable;
		init();
	}
	
	private void init() {
		allParams = new LinkedList<HtmlParameter>();
	}

	@Override
	public int getColumnCount() {
		if (isEditable) {
			return 4;
		} else {
			return 3;
		}
	}

	@Override
	public int getRowCount() {
		return allParams.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= allParams.size()) {
			return "asf";
		}
		if (rowIndex > allParams.size() || rowIndex < 0) {
			return null;
		}
		HtmlParameter htmlParameter = allParams.get(rowIndex);
		
		switch(columnIndex) {
		case 0:
			return htmlParameter.getType();
		case 1:
			return htmlParameter.getName();
		case 2:
			return htmlParameter.getValue();
		case 3:
			return "";
			//return null;
			//return comboBox;
		}

		return "asdf";
	}
	
	/*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text 
     * rather than a combobox.
     */
    public Class getColumnClass(int c) {
    	switch(c) {
    	case 0:
    	case 1:
    	case 2:
    		return String.class;
    	case 3:
    		return JComboBox.class;
    	default:
    	   	return String.class;    			
    	}
    }
	
	private boolean isChanged() {
		if ( isGetChanged || isPostChanged || isCookieChanged ) {
			return true;
		} else {
			return false;
		}
	}
	
    public void setValueAt(Object value, int row, int col) {
    	if (col == 1 || col == 2 && row >= 0) {
        	HtmlParameter htmlParameter = allParams.get(row);    	
        	
    		if (col == 1) {
    			htmlParameter.setName( (String)value);
    		} else if (col == 2) {
    			htmlParameter.setValue( (String)value);
    		}

    		switch(htmlParameter.getType()) {
    		case url:
    			isGetChanged = true;
    			break;
    		case form:
    			isPostChanged = true;
    			break;
    		case cookie:
    			isCookieChanged = true;
    			break;
    		default:
    			System.out.println("HMMMMMM");
    		}
    	}
    	this.fireTableDataChanged();
    }

	public boolean hasChanged() {
		return isChanged();
	}
	
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1 || columnIndex == 2) {
        	return isEditable;
        } else if (columnIndex == 3) { 
        	return true; 
        } else {
        	return false;
        }
    }
    
	public void setHttpMessage(HttpMessage ahttpMessage) {
		this.httpMessage = ahttpMessage;
		
		isCookieChanged = false;
		isPostChanged = false;
		isGetChanged = false;
	}

	public void load() {
		if (httpMessage == null) {
			return;
		}
		
		cookies = httpMessage.getCookieParams();
		post = httpMessage.getFormParams();
		get = httpMessage.getUrlParams();
		
		allParams = new LinkedList<HtmlParameter>();
		allParams.addAll(get);
		allParams.addAll(post);
		allParams.addAll(cookies);
		
		// Notify view that table has changed
		this.fireTableDataChanged();
	}

    public String getColumnName(int col) {
        return columnNames[col];
    }
	
	public void save() {
		if (! isChanged()) {
			return;
		}
		
		if (isPostChanged) {
			httpMessage.setFormParams(post);
			isPostChanged = false;
		}
		
		if (isGetChanged) {
			httpMessage.setGetParams(get);
			isGetChanged = false;
		}

		if (isCookieChanged) {
			httpMessage.setCookieParams(cookies);
			isCookieChanged = false;
		}

	}
}
