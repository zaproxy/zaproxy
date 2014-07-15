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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;

public class ContextListPanel extends AbstractParamPanel {

	private static final long serialVersionUID = -8337361808959321380L;
	
	private static final String CONTEXT_EXT = ".context";

	private Logger logger = Logger.getLogger(this.getClass());

	private JPanel panelContext = null;
	private JTable tableExt = null;
	private JScrollPane jScrollPane = null;
	private ContextListTableModel model = new ContextListTableModel();
	
	private JButton exportButton = null;
	private JButton importButton = null;
	
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
		    panelContext.add(getJScrollPane(), LayoutHelper.getGBC(0, 0, 4, 1.0D, 1.0D));
		    panelContext.add(new JLabel(""), LayoutHelper.getGBC(0, 1, 1, 1.0D, 0.0D));	// left spacer
		    panelContext.add(getExportButton(), LayoutHelper.getGBC(1, 1, 1, 0.0D, 0.0D));
		    panelContext.add(getImportButton(), LayoutHelper.getGBC(2, 1, 1, 0.0D, 0.0D));
		    panelContext.add(new JLabel(""), LayoutHelper.getGBC(3, 1, 1, 1.0D, 0.0D));	// right spacer
		}
		return panelContext;
	}
	
	private JButton getExportButton() {
		if (exportButton == null) {
			exportButton = new JButton(Constant.messages.getString("context.export.button"));
			exportButton.setToolTipText(Constant.messages.getString("context.export.tooltip"));
			exportButton.setEnabled(false);	// Only enabled when one context selected
			final ContextListPanel panel = this;
			exportButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int row = getTableExtension().getSelectedRow();
					if (row >= 0) {
						int ctxId = (Integer)model.getValueAt(row, 0);
						String ctxName = (String)model.getValueAt(row, 1);
						
						JFileChooser chooser = new JFileChooser(Constant.getContextsDir());
						File file = null;
					    chooser.setFileFilter(new FileFilter() {
					           @Override
					           public boolean accept(File file) {
					                if (file.isDirectory()) {
					                    return true;
					                } else if (file.isFile() && file.getName().endsWith(CONTEXT_EXT)) {
					                    return true;
					                }
					                return false;
					            }
					           @Override
					           public String getDescription() {
					               return Constant.messages.getString("file.format.zap.context");
					           }
					    });
					    chooser.setSelectedFile(new File(ctxName));
					    
					    int rc = chooser.showSaveDialog(panel);
					    if(rc == JFileChooser.APPROVE_OPTION) {
							try {
					    		file = chooser.getSelectedFile();
					    		if (file == null) {
					    			return;
					    		}
							    if (! file.getName().endsWith(CONTEXT_EXT)) {
							    	// Force the extension 
							    	file = new File(file.getAbsolutePath() + CONTEXT_EXT);
							    }
							    
								Model.getSingleton().getSession().exportContext(ctxId, file);
							} catch (Exception e1) {
								View.getSingleton().showWarningDialog(panel, MessageFormat.format(
										Constant.messages.getString("context.export.error"), e1.getMessage()));
							}
					    }
					}
				}});
		}
		return exportButton;
	}
	
	private void setButtonStates() {
		if (this.getTableExtension().getSelectedRowCount() == 1) {
			this.getExportButton().setEnabled(true);
		} else {
			this.getExportButton().setEnabled(false);
		}
	}
	
	private JButton getImportButton() {
		if (importButton == null) {
			importButton = new JButton(Constant.messages.getString("context.import.button"));
			final ContextListPanel panel = this;
			importButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(Constant.getContextsDir());
					File file = null;
				    chooser.setFileFilter(new FileFilter() {
				           @Override
				           public boolean accept(File file) {
				                if (file.isDirectory()) {
				                    return true;
				                } else if (file.isFile() && file.getName().endsWith(".context")) {
				                    return true;
				                }
				                return false;
				            }
				           @Override
				           public String getDescription() {
				               return Constant.messages.getString("file.format.zap.context");
				           }
				    });
				    
				    int rc = chooser.showOpenDialog(panel);
				    if(rc == JFileChooser.APPROVE_OPTION) {
						try {
				    		file = chooser.getSelectedFile();
				    		if (file == null || ! file.exists()) {
				    			return;
				    		}
							Context ctx = Model.getSingleton().getSession().importContext(file);
							
						    Object[] values = new Object[] {ctx.getIndex(), ctx.getName(), ctx.isInScope()};
						    model.addValues(values);

						    // Update the panels
						    View.getSingleton().showSessionDialog(Model.getSingleton().getSession(), panel.getName(), true);
							
						} catch (Exception e1) {
							logger.debug(e1.getMessage(), e1);
							View.getSingleton().showWarningDialog(panel, MessageFormat.format(
									Constant.messages.getString("context.import.error"), e1.getMessage()));
						}
				    }

				}
			});
		}
		return importButton;
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
			// Issue 954: Force the JTable cell to auto-save when the focus changes.
			// Example, edit cell, click OK for a panel dialog box, the data will get saved.
			tableExt.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			
			tableExt.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					setButtonStates();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					setButtonStates();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					setButtonStates();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					setButtonStates();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setButtonStates();
				}});
			
			tableExt.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					setButtonStates();
				}

				@Override
				public void keyPressed(KeyEvent e) {
					setButtonStates();
				}

				@Override
				public void keyReleased(KeyEvent e) {
					setButtonStates();
				}});

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
	    
	    List<Object[]> values = new ArrayList<>();
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
