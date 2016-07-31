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
package org.zaproxy.zap.extension.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


public class PopupMenuExportURLs extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
	private static final String CRLF = "\r\n";
    private ExtensionHistory extension = null;

    private static Logger log = Logger.getLogger(PopupMenuExportURLs.class);

    public PopupMenuExportURLs() {
        super(Constant.messages.getString("exportUrls.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    
        		JTree site = extension.getView().getSiteTreePanel().getTreeSite();
        		
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
        	    boolean html = file.getName().toLowerCase().endsWith(".htm") ||
        	    	file.getName().toLowerCase().endsWith(".html");
                    
        	    BufferedWriter fw = null;
                try {
                    fw = new BufferedWriter(new FileWriter(file, isAppend));
                    exportURLs((SiteNode) site.getModel().getRoot(), fw, html);

                } catch (Exception e1) {
                	log.warn(e1.getStackTrace(), e1);
                    extension.getView().showWarningDialog(Constant.messages.getString("file.save.error") + file.getAbsolutePath());
                } finally {
            	    try {
            	        fw.close();
            	    } catch (Exception e2) {
                    	log.warn(e2.getStackTrace(), e2);
            	    }
                }
        	}
        });
			
	}
	
    public void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    	
	private void exportURLs(SiteNode node, BufferedWriter writer, boolean html) {
		
        if (node == null) {
            return;
        }
        try {
        	if (node.getHistoryReference() != null &&
        			(node.getHistoryReference().getHistoryType() == HistoryReference.TYPE_PROXIED ||
        				node.getHistoryReference().getHistoryType() == HistoryReference.TYPE_ZAP_USER ||
         				node.getHistoryReference().getHistoryType() == HistoryReference.TYPE_SPIDER)) {
        		
        	 	HttpMessage msg = node.getHistoryReference().getHttpMessage();
    			if (msg != null && msg.getRequestHeader().getURI() != null) {

            		writer.write(msg.getRequestHeader().getMethod());
            		writer.write("\t");
            		if (html) {
                		writer.write("<a href=\"");
            			writer.write(msg.getRequestHeader().getURI().toString());
                		writer.write("\">");
            			writer.write(msg.getRequestHeader().getURI().toString());
                		writer.write("</a><br>");
            		} else {
            			writer.write(msg.getRequestHeader().getURI().toString());
            		}
            		writer.write(CRLF);
    			}
        	}
			
		} catch (HttpMalformedHeaderException e) {
        	log.warn(e.getStackTrace(), e);
		} catch (IOException e) {
        	log.warn(e.getStackTrace(), e);
		} catch (DatabaseException e) {
        	log.warn(e.getStackTrace(), e);
		}

		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = node.children();
		while (en.hasMoreElements()) {
			exportURLs(en.nextElement(), writer, html);
		}
	}
    
    private File getOutputFile() {

	    JFileChooser chooser = new JFileChooser(extension.getModel().getOptionsParam().getUserDirectory());
	    FileNameExtensionFilter textFilesFilter = new FileNameExtensionFilter(Constant.messages.getString("file.format.ascii"), "txt");
	    FileNameExtensionFilter htmlFilesFilter = new FileNameExtensionFilter(Constant.messages.getString("file.format.html"), "html", "htm");

	    chooser.addChoosableFileFilter(textFilesFilter);
	    chooser.addChoosableFileFilter(htmlFilesFilter);
	    chooser.setFileFilter(textFilesFilter);
	    
		File file = null;
	    int rc = chooser.showSaveDialog(extension.getView().getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
    		file = chooser.getSelectedFile();
    		if (file == null) {
    			return file;
    		}
            extension.getModel().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
    		String fileNameLc = file.getAbsolutePath().toLowerCase();
    		if (! fileNameLc.endsWith(".txt") && ! fileNameLc.endsWith(".htm") &&
    				! fileNameLc.endsWith(".html")) {
    		    String ext;
    		    if (htmlFilesFilter.equals(chooser.getFileFilter())) {
    		        ext = ".html";
    		    } else {
    		        ext = ".txt";
    		    }
    		    file = new File(file.getAbsolutePath() + ext);
    		}
    		return file;
    		
	    }
	    return file;
    }

}
