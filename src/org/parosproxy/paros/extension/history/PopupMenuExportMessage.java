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
// ZAP: 2012/01/12 Reflected the rename of the class ExtensionPopupMenu to
// ExtensionPopupMenuItem.
// ZAP: 2012/03/15 Changed the method initialize to check if "fw" is not null before closing.
// Made the "log" final.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI

package org.parosproxy.paros.extension.history;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.HistoryReference;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuExportMessage extends ExtensionPopupMenuItem {

    // ZAP: Added logger
    private static final Logger log = Logger.getLogger(PopupMenuExportMessage.class);

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
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("history.export.messages.popup"));	// ZAP: i18n

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    
        		List<HistoryReference> hrefs = extension.getSelectedHistoryReferences();
        	    if (hrefs.size() == 0) {
                    extension.getView().showWarningDialog(Constant.messages.getString("history.export.messages.select.warning"));        	        
                    return;
        	    }

        	    File file = getOutputFile();
        	    if (file == null) {
        	        return;
        	    }
        	    
        	    boolean isAppend = true;
        	    if (file.exists()) {
                    int rc = extension.getView().showYesNoCancelDialog(Constant.messages.getString("file.overwrite.warning"));
                    if (rc == JOptionPane.CANCEL_OPTION) {
                        return;
                    } else if (rc == JOptionPane.YES_OPTION) {
                        isAppend = false;
                    }
        	    }
                    
        	    BufferedWriter fw = null;
                try {
                    fw = new BufferedWriter(new FileWriter(file, isAppend));
            		for (HistoryReference href : hrefs) {
            	        exportHistory(href, fw);
            	    }

                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("file.save.error") + file.getAbsolutePath() + ".");
                	// ZAP: Log exceptions
                	log.warn(e1.getMessage(), e1);
                } finally {
            	    try {
            	    	if (fw != null) {
            	    		fw.close();
            	    	}
            	    } catch (Exception e2) {
                    	// ZAP: Log exceptions
                    	log.warn(e2.getMessage(), e2);
            	    }
                }
        	}
        });

			
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList<?> list = (JList<?>) invoker;
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
	           @Override
	           public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                } else if (file.isFile() && file.getName().endsWith(".txt")) {
	                    return true;
	                }
	                return false;
	            }
	           @Override
	           public String getDescription() {
	               return Constant.messages.getString("file.format.ascii");
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
