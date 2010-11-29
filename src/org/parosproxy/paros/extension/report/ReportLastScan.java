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
package org.parosproxy.paros.extension.report;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReportLastScan {

    
    public ReportLastScan() {
        
    }

    

    private String getAlertXML(Database db, RecordScan recordScan) throws SQLException {

        Connection conn = null;
        PreparedStatement psAlert = null;
        StringBuffer sb = new StringBuffer();
        
        // prepare table connection
        try {
            conn = db.getDatabaseServer().getNewConnection();
            conn.setReadOnly(true);
            // ZAP: Changed to read all alerts and order by risk
            psAlert = conn.prepareStatement("SELECT ALERT.ALERTID FROM ALERT ORDER BY RISK, PLUGINID");
            //psAlert = conn.prepareStatement("SELECT ALERT.ALERTID FROM ALERT JOIN SCAN ON ALERT.SCANID = SCAN.SCANID WHERE SCAN.SCANID = ? ORDER BY PLUGINID");
            //psAlert.setInt(1, recordScan.getScanId());
            psAlert.executeQuery();
            ResultSet rs = psAlert.getResultSet();

            RecordAlert recordAlert = null;
            Alert alert = null;
            Alert lastAlert = null;

            StringBuffer sbURLs = new StringBuffer(100);
            String s = null;
            
            // get each alert from table
            while (rs.next()) {
                int alertId = rs.getInt(1);
                recordAlert = db.getTableAlert().read(alertId);
                alert = new Alert(recordAlert);
                
                // ZAP: Ignore false positives
                if (alert.getReliability() == Alert.FALSE_POSITIVE) {
                	continue;
                }

                if (lastAlert != null && 
                		(alert.getPluginId() != lastAlert.getPluginId() ||
                				alert.getRisk() != lastAlert.getRisk())) {
                    s = lastAlert.toPluginXML(sbURLs.toString());
                    sb.append(s);
                    sbURLs.setLength(0);
                }

                s = alert.getUrlParamXML();
                sbURLs.append(s);

                lastAlert = alert;

            }

            if (lastAlert != null) {
                sb.append(lastAlert.toPluginXML(sbURLs.toString()));
            }
                

            
        } catch (SQLException e) {
    	    e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
            
        }
        
        //exit
        return sb.toString();
    }
    
    public File generate(String fileName, Model model) throws Exception {
        
	    StringBuffer sb = new StringBuffer(500);
	    // ZAP: Dont require scan to have been run
	    /*
	    RecordScan scan = null;
	        
	    scan = model.getDb().getTableScan().getLatestScan();
	    if (scan == null) {
	        // view.showMessageDialog("Scan result is not available.  No report is generated.");
	        return null;
	    }
	    */
	    sb.append("<?xml version=\"1.0\"?>");
	    sb.append("<report>\r\n");
	    sb.append("Report generated at " + ReportGenerator.getCurrentDateTimeString() + ".\r\n");
	    sb.append(getAlertXML(model.getDb(), null));
	    sb.append("</report>");	
	    
	    if (!fileName.toLowerCase().endsWith(".htm") && !fileName.toLowerCase().endsWith(".html")) {
	        fileName = fileName + ".html";		        
	    }
	    
	    File report = ReportGenerator.stringToHtml(sb.toString(), "xml" + File.separator + "reportLatestScan.xsl", fileName);
	    
	    
	    return report;
    }
    
	public void generate(ViewDelegate view, Model model) {		

	    RecordScan scan = null;
	    // ZAP: Allow scan report file name to be specified
	    try{
		    JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
		    chooser.setFileFilter(new FileFilter() {
		           public boolean accept(File file) {
		                if (file.isDirectory()) {
		                    return true;
		                } else if (file.isFile() && 
		                		file.getName().toLowerCase().endsWith(".htm")) {
		                    return true;
		                } else if (file.isFile() && 
		                		file.getName().toLowerCase().endsWith(".html")) {
		                    return true;
		                }
		                return false;
		            }
		           public String getDescription() {
		               return Constant.messages.getString("file.format.html");
		           }
		    });
		    
			File file = null;
		    int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
		    if(rc == JFileChooser.APPROVE_OPTION) {
	    		file = chooser.getSelectedFile();
	    		if (file != null) {
		            Model.getSingleton().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
		    		String fileName = file.getAbsolutePath().toLowerCase();
		    		if (! fileName.endsWith(".htm") &&
		    				! fileName.endsWith(".html")) {
		    		    fileName += ".html";
		    		    file = new File(fileName);
		    		}
	    		}
    		
	    		//String output = model.getSession().getSessionFolder() + File.separator + "LatestScannedReport.htm";
	    		File report = generate(file.getAbsolutePath(), model);
	    		if (report == null) {
	    		    return;
	    		}
	    		
			    // ZAP: Dont show a success message
			    //view.showMessageDialog("Scanning report generated.  If it does not show up after clicking OK,\r\nplease browse the file at " + report.getAbsolutePath()); 
	  
			    // ZAP: Dont try to open browser automatically
	  			//ReportGenerator.openBrowser(report.getAbsolutePath());
		    }
  			
    	} catch (Exception e){
    	    e.printStackTrace();
      		view.showWarningDialog("File creation error."); 
    	}
	}
	

		
    
}
