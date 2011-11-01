/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.zaproxy.zap.extension.httppanel.view.table;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class HttpPanelTabularModel extends AbstractTableModel {

	private static final long serialVersionUID = -3239987476977015394L;
	private static final String[] columnNames = {
    	Constant.messages.getString("http.panel.table.paramName"),
    	Constant.messages.getString("http.panel.table.paramValue")};	// ZAP: i18n
    private static final Pattern pSeparator	= Pattern.compile("([^=&]+)[=]([^=&]*)"); 
    private Vector<String[]> listPair = new Vector<String[]>();
    private boolean editable = true;
    private boolean isChanged = false;

    private boolean tableError = false;
    
    // ZAP: Added logger
    private Logger logger = Logger.getLogger(HttpPanelTabularModel.class);
    
    /**
     * @return Returns the editable.
     */
    public boolean isEditable() {
        return editable;
    }
    /**
     * @param editable The editable to set.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    /**
     * 
     */
    public HttpPanelTabularModel() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 2;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return listPair.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
        String[] cell = (String[]) listPair.get(row);
        return cell[col];
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public synchronized void setText(String body) {
        listPair.clear();
        String name = null;
        String value = null;
        Matcher matcher = pSeparator.matcher(body);
        //int row = 0;
        int cnt = 0;
        tableError = false;
                
  	  	while (matcher.find()){
  	  		cnt++;
  	  	    String[] cell = new String[2];
  	  	    try {
                name = URLDecoder.decode(matcher.group(1),"8859_1");
      	  	    value = URLDecoder.decode(matcher.group(2),"8859_1");
      	  	    cell[0] = name;
      	  	    cell[1] = value;
      	  	    listPair.add(cell);
            } catch (UnsupportedEncodingException e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            } catch (IllegalArgumentException e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
  	  	}
  	  	
  	  	if (cnt <= 0) {
  	  		// No valid body, cant create table
  	  		tableError = true;
  	  		listPair.add(new String[] {"[cannot compute]", "[cannot compute]"});
  	  	} else {
  	  		// Make sure user can always add a param at the end
  	  		listPair.add(new String[] {"", ""});
  	  	}

  	  	fireTableDataChanged();
    }
    
    public synchronized String getText() {
        StringBuffer sb = new StringBuffer();
        
        if (tableError) {
        	return null;
        }
        
        for (int i=0; i<listPair.size(); i++) {
            String[] cell = (String[]) listPair.get(i);
            try {
            	String name = URLEncoder.encode(cell[0],"UTF8");
            	String value = URLEncoder.encode(cell[1],"UTF8");
            	// ZAP: Ignore if name is not set
            	if (name.length() > 0) {
                    if (i > 0) {
                    	sb.append('&');
                    }
                    sb.append(name);
                	sb.append('=');
                    sb.append(value);
            	}
            } catch (UnsupportedEncodingException e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            } catch (IllegalArgumentException e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
        }
        return sb.toString();
            
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        
        return isEditable();
        
    }
    
    public void setValueAt(Object value, int row, int col) {
        String[] cell = null;
        while (row >= listPair.size()-1) {
            cell = new String[2];
            cell[0] = "";
            cell[1] = "";
            listPair.add(cell);
        }
        
        cell = (String[]) listPair.get(row);
        cell[col] = (String) value;
        fireTableCellUpdated(row, col);
        
        isChanged = true;
    }
    
	public boolean hasChanged() {
		return isChanged;
	}
}