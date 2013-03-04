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
// ZAP: 2011/05/15 Improved error logging
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/06/11 Changed to call the method Control.shutdown(boolean) with the
// parameter set as true.
// ZAP: 2012/06/19 Changed the method sessionOpened(File,Exception) to not call
// the method ExtensionLoader.sessionChangedAllPlugin, now it's done in the
// class Control.
// ZAP: 2012/07/02 Changed to use the new database compact option in the method
// exit().
// ZAP: 2012/07/23 Removed parameter from View.getSessionDialog call.
// ZAP: 2012/12/06 Issue 428: Moved exit code to control to support the marketplace
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.control;
 
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WaitMessageDialog;


public class MenuFileControl implements SessionListener {

    private static Logger log = Logger.getLogger(MenuFileControl.class);

    private View view = null;
    private Model model = null;
    private Control control = null;
    private WaitMessageDialog waitMessageDialog = null;
    
    public MenuFileControl(Model model, View view, Control control) {
        this.view = view;
        this.model = model;
        this.control = control;
    }
    
	public void exit() {
		control.exit(false, null);
	}
	
	public void newSession(boolean isPromptNewSession) throws ClassNotFoundException, Exception {
		if (isPromptNewSession) {
			// ZAP: i18n
		    if (model.getSession().isNewState()) {
				if (view.showConfirmDialog(Constant.messages.getString("menu.file.discardSession")) != JOptionPane.OK_OPTION) {
					return;
				}
				control.discardSession();
		    } else if (view.showConfirmDialog(Constant.messages.getString("menu.file.closeSession")) != JOptionPane.OK_OPTION) {
				return;
			}
			control.createAndOpenUntitledDb();
		}
		
		Session session = control.newSession();

		view.getSiteTreePanel().getTreeSite().setModel(session.getSiteTree());

		// comment code below so new session use default untitled first.  
//		if (isPromptNewSession) {
//		    SessionDialog dialog = view.getSessionDialog("New Session");
//		    dialog.initParam(session);
//		    dialog.showDialog(false);
//		    saveAsSession();
//		}

		// refresh display
		view.getMainFrame().setTitle(session.getSessionName() + " - " + Constant.PROGRAM_NAME);
		view.getOutputPanel().clear();
	}
	
	public void openSession() {
		JFileChooser chooser = new JFileChooser(model.getOptionsParam().getUserDirectory());
		File file = null;
	    chooser.setFileFilter(new FileFilter() {
	           @Override
	           public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                } else if (file.isFile() && file.getName().endsWith(".session")) {
	                    return true;
	                }
	                return false;
	            }
	           @Override
	           public String getDescription() {
	        	   // ZAP: Rebrand
	               return Constant.messages.getString("file.format.zap.session");
	           }
	    });
	    int rc = chooser.showOpenDialog(view.getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
			try {
	    		file = chooser.getSelectedFile();
	    		if (file == null) {
	    			return;
	    		}
                model.getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
	    	    log.info("opening session file " + file.getAbsolutePath());
	    	    waitMessageDialog = view.getWaitMessageDialog(Constant.messages.getString("menu.file.loadSession"));	// ZAP: i18n
	    	    control.openSession(file, this);
	    		waitMessageDialog.setVisible(true);
			} catch (Exception e) {
	            log.error(e.getMessage(), e);
			}
	    }
	}
	public void saveSession() {
	    Session session = model.getSession();

	    if (session.isNewState()) {
		    view.showWarningDialog("Please use Save As...");
		    return;
	    }
	    
		try {
    	    waitMessageDialog = view.getWaitMessageDialog(Constant.messages.getString("menu.file.savingSession"));	// ZAP: i18n   
    		control.saveSession(session.getFileName(), this);
    	    log.info("saving session file " + session.getFileName());
    	    // ZAP: If the save is quick the dialog can already be null here
    	    if (waitMessageDialog != null) {
    	    	waitMessageDialog.setVisible(true);
    	    }
    	    
		} catch (Exception e) {
		    view.showWarningDialog(Constant.messages.getString("menu.file.savingSession.error"));	// ZAP: i18n
    	    log.error("error saving session file " + session.getFileName());
            log.error(e.getMessage(), e);
		}
	    
	}
	
	public void saveAsSession() {
	    Session session = model.getSession();

	    JFileChooser chooser = new JFileChooser(model.getOptionsParam().getUserDirectory());
	    // ZAP: set session name as file name proposal
	    File fileproposal = new File(session.getSessionName());
	    if (session.getFileName() != null && session.getFileName().trim().length() > 0) {
	    	// if there is already a file name, use it
	    	fileproposal = new File(session.getFileName());
	    }
		chooser.setSelectedFile(fileproposal);
	    chooser.setFileFilter(new FileFilter() {
	           @Override
	           public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                } else if (file.isFile() && file.getName().endsWith(".session")) {
	                    return true;
	                }
	                return false;
	            }
	           @Override
	           public String getDescription() {
	        	   // ZAP: Rebrand
	               return Constant.messages.getString("file.format.zap.session");
	           }
	    });
		File file = null;
	    int rc = chooser.showSaveDialog(view.getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
    		file = chooser.getSelectedFile();
    		if (file == null) {
    			return;
    		}
            model.getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
    		String fileName = file.getAbsolutePath();
    		if (!fileName.endsWith(".session")) {
    		    fileName += ".session";
    		}
    		
    		try {
	    	    waitMessageDialog = view.getWaitMessageDialog(Constant.messages.getString("menu.file.savingSession"));	// ZAP: i18n
	    	    control.saveSession(fileName, this);
        	    log.info("save as session file " + session.getFileName());
        	    waitMessageDialog.setVisible(true);
    		} catch (Exception e) {
                log.error(e.getMessage(), e);
    		}
	    }
	}
	
	private void setTitle() {
        if (model.getSession().isNewState()) {
			// No file name
			view.getMainFrame().setTitle(model.getSession().getSessionName() + " - " + Constant.PROGRAM_NAME);
		} else {
	        File file = new File(model.getSession().getFileName());
			view.getMainFrame().setTitle(
					model.getSession().getSessionName() + " - " +
					file.getName().replaceAll(".session\\z","") + " - " + 
					Constant.PROGRAM_NAME);
		}
	}
	
	public void properties() {
		// ZAP: removed session dialog parameter
	    SessionDialog dialog = view.getSessionDialog();
	    dialog.initParam(model.getSession());
	    dialog.showDialog(false);

	    // ZAP: Set the title consistently
	    setTitle();
//		view.getMainFrame().setTitle(Constant.PROGRAM_NAME + " " + Constant.PROGRAM_VERSION + " - " + model.getSession().getSessionName());
	}

    @Override
    public void sessionOpened(File file, Exception e) {
        if (e == null) {
            // ZAP: Removed the statement that called the method
            // ExtensionLoader.sessionChangedAllPlugin, now it's done in the
            // class Control.

            // ZAP: Set the title consistently
            setTitle();
            //view.getMainFrame().setTitle(file.getName().replaceAll(".session\\z","") + " - " + Constant.PROGRAM_NAME);
        } else {
            view.showWarningDialog("Error opening session file");
            log.error("error opening session file " + file.getAbsolutePath());
            log.error(e.getMessage(), e);
        }

        if (waitMessageDialog != null) {
            waitMessageDialog.setVisible(false);
            waitMessageDialog = null;
        }
    }

    @Override
    public void sessionSaved(Exception e) {
        if (e == null) {
            view.getMainFrame().getMainMenuBar().getMenuFileSave().setEnabled(true);
            // ZAP: Set the title consistently
            setTitle();
            //File file = new File(model.getSession().getFileName());
            //view.getMainFrame().setTitle(file.getName().replaceAll(".session\\z","") + " - " + Constant.PROGRAM_NAME);
        } else {
		    view.showWarningDialog(Constant.messages.getString("menu.file.savingSession.error"));	// ZAP: i18n
    	    log.error("error saving session file " + model.getSession().getFileName(), e);
            log.error(e.getMessage(), e);

        }
        
        if (waitMessageDialog != null) {
            waitMessageDialog.setVisible(false);
            waitMessageDialog = null;
        }

    }
}
