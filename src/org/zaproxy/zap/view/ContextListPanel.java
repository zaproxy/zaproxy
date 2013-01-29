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
// ZAP: 2011/05/15 i19n
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/14 Changed the method initParam to discard all edits.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.

package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.model.Context;

public class ContextListPanel extends AbstractParamPanel {

	private static final long serialVersionUID = -8337361808959321380L;

	private JPanel panelContext = null;
	private JTable tableExt = null;
	private JScrollPane jScrollPane = null;
	private ContextListTableModel model = new ContextListTableModel();
	
    public ContextListPanel() {
        super();
 		initialize();
   }

    
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("context.list"));
        this.add(getPanelSession(), getPanelSession().getName());
	}
	/**
	 * This method initializes panelSession	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelSession() {
		if (panelContext == null) {
			panelContext = new JPanel();
			panelContext.setLayout(new GridBagLayout());
			panelContext.setName(Constant.messages.getString("context.list"));
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	panelContext.setSize(180, 101);
		    }
		    panelContext.add(getJScrollPane(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D));
		}
		return panelContext;
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableExtension());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes tableAuth	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableExtension() {
		if (tableExt == null) {
			tableExt = new JTable();
			tableExt.setModel(this.model);
			tableExt.getColumnModel().getColumn(0).setPreferredWidth(30);
			tableExt.getColumnModel().getColumn(1).setPreferredWidth(320);
			tableExt.getColumnModel().getColumn(2).setPreferredWidth(50);
			// Disable for now - would be useful but had some problems with this ;)
			/*
			ListSelectionListener sl = new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
	        		if (tableExt.getSelectedRow() > -1) {
	        			Context ctx = ((ContextListTableModel)tableExt.getModel()).getContext(
	        					tableExt.getSelectedRow());
	        			if (ctx != null) {
	        				try {
								extName.setText(ext.getName());
								extDescription.setText(ext.getDescription());
								if (ext.getAuthor() != null) {
									extAuthor.setText(ext.getAuthor());
								} else {
									extAuthor.setText("");
								}
								if (ext.getURL() != null) {
									extURL.setText(ext.getURL().toString());
									getUrlLaunchButton().setEnabled(true);
								} else {
									extURL.setText("");
									getUrlLaunchButton().setEnabled(false);
								}
							} catch (Exception e) {
								// Just to be safe
								log.error(e.getMessage(), e);
							}
	        			}
	        		}
				}};
			
			tableExt.getSelectionModel().addListSelectionListener(sl);
			tableExt.getColumnModel().getSelectionModel().addListSelectionListener(sl);
			*/
			
		}
		return tableExt;
	}

	
	
	@Override
	public void initParam(Object obj) {
	    Session session = (Session) obj;
	    
	    List<Object[]> values = new ArrayList<Object[]>();
	    List<Context> contexts = session.getContexts();
	    for (Context context : contexts) {
	    	values.add(new Object[] {context.getIndex(), context.getName(), context.isInScope()});
	    }
	    this.model.setValues(values);
	    
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    Session session = (Session) obj;
		List<Object[]> values = this.model.getValues();
		
		for (Object[] value: values) {
			Context ctx = session.getContext((Integer)value[0]);
			if (ctx.isInScope() != (Boolean) value[2]) {
				ctx.setInScope( ! ctx.isInScope());
			}
			
		}
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}
	
}
