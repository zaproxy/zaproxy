/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2005 Chinotec Technologies Company
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
package org.parosproxy.paros.extension.history;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuExportMessage extends ExtensionPopupMenu {

    // ZAP: Added logger
    private static Log log = LogFactory.getLog(PopupMenuExportMessage.class);

    private static final String CRLF = "\r\n";
    private ExtensionHistory extension = null;
    
    /**
     * 
     */
    public PopupMenuExportMessage() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuExportMessage(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Export Messages to File...");

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    
                JList listLog = extension.getLogPanel().getListLog();
        	    Object[] obj = listLog.getSelectedValues();
        	    if (obj.length == 0) {
                    extension.getView().showWarningDialog("Select HTTP messages in History panel before export to file.");        	        
                    return;
        	    }

        	    File file = getOutputFile();
        	    if (file == null) {
        	        return;
        	    }
        	    
        	    boolean isAppend = true;
        	    if (file.exists()) {
                    int rc = extension.getView().showYesNoCancelDialog("File exists.  Yes = overwrite, No = append?");
                    if (rc == JOptionPane.CANCEL_OPTION) {
                        return;
                    } else if (rc == JOptionPane.YES_OPTION) {
                        isAppend = false;
                    }
        	    }
                    
        	    BufferedWriter fw = null;
                try {
                    fw = new BufferedWriter(new FileWriter(file, isAppend));
            	    for (int i=0; i<obj.length; i++) {
            	        HistoryReference ref = (HistoryReference) obj[i];
            	        exportHistory(ref, fw);
            	    }

                } catch (Exception e1) {
                    extension.getView().showWarningDialog("Error saving file to " + file.getAbsolutePath() + ".");
                	// ZAP: Log exceptions
                	log.warn(e1.getMessage(), e1);
                } finally {
            	    try {
            	        fw.close();
            	    } catch (Exception e2) {
                    	// ZAP: Log exceptions
                    	log.warn(e2.getMessage(), e2);
            	    }
                }
        	}
        });

			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList list = (JList) invoker;
                if (list.getSelectedIndex() >= 0) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }
            } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
            return true;
            
        }
        return false;
    }

    private JTree getTree(Component invoker) {
        if (invoker instanceof JTree) {
            JTree tree = (JTree) invoker;
            if (tree.getName().equals("treeSite")) {
                return tree;
            }
        }

        return null;
    }
    
    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    	
    private void exportHistory(HistoryReference ref, Writer writer) {

        if (ref == null) {
            return;
        }

        String s = null;
        
        try {
            writer.write("==== " + ref.getHistoryId() + " ==========" + CRLF);
            s = ref.getHttpMessage().getRequestHeader().toString();
            writer.write(s);
            s = ref.getHttpMessage().getRequestBody().toString();
            writer.write(s);
            if (!s.endsWith(CRLF)) {
                writer.write(CRLF);
            }
        
            
            if (!ref.getHttpMessage().getResponseHeader().isEmpty()) {
                s = ref.getHttpMessage().getResponseHeader().toString();
                writer.write(s);
                s = ref.getHttpMessage().getResponseBody().toString();
                writer.write(s);
                if (!s.endsWith(CRLF)) {
                    writer.write(CRLF);
                }

            }

        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        
    }
    
    private File getOutputFile() {

	    JFileChooser chooser = new JFileChooser(extension.getModel().getOptionsParam().getUserDirectory());
	    chooser.setFileFilter(new FileFilter() {
	           public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                } else if (file.isFile() && file.getName().endsWith(".txt")) {
	                    return true;
	                }
	                return false;
	            }
	           public String getDescription() {
	               return "ASCII text file";
	           }
	    });
		File file = null;
	    int rc = chooser.showSaveDialog(extension.getView().getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
    		file = chooser.getSelectedFile();
    		if (file == null) {
    			return file;
    		}
            extension.getModel().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
    		String fileName = file.getAbsolutePath();
    		if (!fileName.endsWith(".txt")) {
    		    fileName += ".txt";
    		    file = new File(fileName);
    		}
    		return file;
    		
	    }
	    return file;
    }
    

}
