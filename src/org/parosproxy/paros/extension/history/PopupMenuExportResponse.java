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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/03/23 Changed to a JMenuItem.
// ZAP: 2016/07/25 Remove String constructor (unused/unnecessary)

package org.parosproxy.paros.extension.history;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;


public class PopupMenuExportResponse extends JMenuItem {

	private static final long serialVersionUID = 1L;

	// ZAP: Added logger
    private static Logger log = Logger.getLogger(PopupMenuExportResponse.class);

    private ExtensionHistory extension = null;
    
    public PopupMenuExportResponse() {
        super(Constant.messages.getString("history.export.response.popup"));	// ZAP: i18n

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    
        		List<HistoryReference> hrefs = extension.getSelectedHistoryReferences();
        	    if (hrefs.size() == 0) {
                    extension.getView().showWarningDialog(Constant.messages.getString("history.export.response.select.warning"));        	        	// ZAP: i18n
                    return;
        	    }

                if (hrefs.size() > 1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("history.export.response.single.warning"));	// ZAP: i18n
                    return;
                }

                HttpMessage msg = null;
                try {
                    msg = hrefs.get(0).getHttpMessage();
                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("history.export.response.read.warning"));	// ZAP: i18n
                    return;
                }
                
                if (msg.getResponseHeader().isEmpty() || msg.getResponseBody().length() == 0) {
                    extension.getView().showWarningDialog(Constant.messages.getString("history.export.response.body.warning"));	// ZAP: i18n
                    return;                    
                }
                    
        	    File file = getOutputFile(msg);
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
                    
        	    BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(file, isAppend));
           	        exportHistory(msg, bos);

                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("file.save.error") + file.getAbsolutePath() + ".");
                	// ZAP: Log exceptions
                	log.warn(e1.getMessage(), e1);
                } finally {
            	    try {
            	        bos.close();
            	    } catch (Exception e2) {
                    	// ZAP: Log exceptions
                    	log.warn(e2.getMessage(), e2);
            	    }
                }
        	}
        });

			
	}

    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    	
    private void exportHistory(HttpMessage msg, BufferedOutputStream bos) {

        try {
            if (!msg.getResponseHeader().isEmpty()) {

                bos.write(msg.getResponseBody().getBytes());
            }

        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        
    }
    
    private File getOutputFile(HttpMessage msg) {

        String filename = "";
        try {
            filename = msg.getRequestHeader().getURI().getPath();
            int pos = filename.lastIndexOf("/");
            filename = filename.substring(pos);
        } catch (Exception e) {
        }
        JFileChooser chooser = new JFileChooser(extension.getModel().getOptionsParam().getUserDirectory());
        if (filename.length() > 0) {
            chooser.setSelectedFile(new File(filename));            
        }

		File file = null;
	    int rc = chooser.showSaveDialog(extension.getView().getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
    		file = chooser.getSelectedFile();
    		if (file == null) {
    			return file;
    		}

            extension.getModel().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());

    		return file;
    		
	    }
	    return file;
    }
    

}
