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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.model.SiteMap;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionReport extends ExtensionAdaptor implements CommandLineListener {

    private static final int ARG_LAST_SCAN_REPORT_IDX = 0;
    
	private ExtensionHookMenu pluginMenu = null;
	private SiteMap siteTree = null;
	
	private JMenu menuReport = null;
	private JMenuItem menuItemLastScanReport = null;
	private CommandLineArgument[] arguments = new CommandLineArgument[1];
	// ZAP Added logger
	private Logger logger = Logger.getLogger(ExtensionReport.class);

    /**
     * 
     */
    public ExtensionReport() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionReport(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionReport");
			
	}
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        //extensionHook.getHookMenu().addNewMenu(getMenuReport());
	        extensionHook.getHookMenu().addReportMenuItem(getMenuItemLastScanReport());

	    }
        extensionHook.addCommandLine(getCommandLineArguments());

	}
	
	
	/**
	 * This method initializes menuReport	
	 * 	
	 * @return javax.swing.JMenu	
	 */    
	private JMenu getMenuReport() {
		if (menuReport == null) {
			menuReport = new JMenu();
			menuReport.setText("Reports");
			menuReport.add(getMenuItemLastScanReport());
			menuReport.addSeparator();
		}
		return menuReport;
	}


	
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.ScannerListener#ScannerProgress(java.lang.String, com.proofsecure.paros.network.HttpMessage, int)
     */
	/**
	 * This method initializes menuItemLastScanReport	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemLastScanReport() {
		if (menuItemLastScanReport == null) {
			menuItemLastScanReport = new JMenuItem();
			menuItemLastScanReport.setText("Generate Report...");
			menuItemLastScanReport.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    ReportLastScan report = new ReportLastScan();
				    report.generate(getView(), getModel());
	                
				}
			});

		}
		return menuItemLastScanReport;
	}
	
    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.CommandLineListener#execute(org.parosproxy.paros.extension.CommandLineArgument[])
     */
    public void execute(CommandLineArgument[] args) {

        if (arguments[ARG_LAST_SCAN_REPORT_IDX].isEnabled()) {
		    CommandLineArgument arg = arguments[ARG_LAST_SCAN_REPORT_IDX];
            ReportLastScan report = new ReportLastScan();
            String fileName = (String) arg.getArguments().get(0);
            try {
                report.generate(fileName, getModel());
                System.out.println("Last Scan Report generated at " + fileName);
            } catch (Exception e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
        } else {
            return;
        }

    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_LAST_SCAN_REPORT_IDX] = new CommandLineArgument("-last_scan_report", 1, null, "", "-last_scan_report [file_path]: Generate 'Last Scan Report' into the file_path provided.");
        return arguments;
    }
	
	

	
      }
