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
// ZAP: 2011/10/01 Fixed filename problem (issue 161)
// ZAP: 2012/01/24 Changed outer XML (issue 268) c/o Alla
// ZAP: 2012/03/15 Changed the methods getAlertXML and generate to use the class 
//      StringBuilder instead of StringBuffer.

package org.parosproxy.paros.extension.report;

import edu.stanford.ejalbert.BrowserLauncher;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.XmlReporterExtension;
import org.zaproxy.zap.utils.XMLStringUtil;
import org.zaproxy.zap.view.ScanPanel;

/**
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ReportLastScan {

    private Logger logger = Logger.getLogger(ReportLastScan.class);

    public ReportLastScan() {
    }

    private String getAlertXML(Database db, RecordScan recordScan) throws SQLException {

        Connection conn = null;
        PreparedStatement psAlert = null;
        StringBuilder sb = new StringBuilder();

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

            StringBuilder sbURLs = new StringBuilder(100);
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

                if (lastAlert != null
                        && (alert.getPluginId() != lastAlert.getPluginId()
                        || alert.getRisk() != lastAlert.getRisk())) {
                    s = lastAlert.toPluginXML(sbURLs.toString());
                    sb.append(s);
                    sbURLs.setLength(0);
                }

                s = alert.getUrlParamXML();
                sbURLs.append(s);

                lastAlert = alert;

            }
            rs.close();

            if (lastAlert != null) {
                sb.append(lastAlert.toPluginXML(sbURLs.toString()));
            }



        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.close();
            }

        }

        //exit
        return sb.toString();
    }

    public File generate(String fileName, Model model, String xslFile) throws Exception {

    	StringBuilder sb = new StringBuilder(500);
        // ZAP: Dont require scan to have been run

        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<OWASPZAPReport version=\"").append(Constant.PROGRAM_VERSION).append("\" generated=\"").append(ReportGenerator.getCurrentDateTimeString()).append("\">\r\n");
        // sb.append(getAlertXML(model.getDb(), null));
        sb.append(siteXML());
        sb.append("</OWASPZAPReport>");

        File report = ReportGenerator.stringToHtml(sb.toString(), xslFile, fileName);

        return report;
    }

    private StringBuilder siteXML() {
        StringBuilder report = new StringBuilder();
        SiteMap siteMap = Model.getSingleton().getSession().getSiteTree();
        SiteNode root = (SiteNode) siteMap.getRoot();
        int siteNumber = root.getChildCount();
        for (int i = 0; i < siteNumber; i++) {
            SiteNode site = (SiteNode) root.getChildAt(i);
            String siteName = ScanPanel.cleanSiteName(site, true);
            String[] hostAndPort = siteName.split(":");
            boolean isSSL = (site.getNodeName().startsWith("https"));
            String siteStart = "<site name=\"" + XMLStringUtil.escapeControlChrs(site.getNodeName()) + "\"" +
                    " host=\"" + XMLStringUtil.escapeControlChrs(hostAndPort[0])+ "\""+
                    " port=\"" + XMLStringUtil.escapeControlChrs(hostAndPort[1])+ "\""+
                    " ssl=\"" + String.valueOf(isSSL) + "\"" +
                    ">";
            StringBuilder extensionsXML = getExtensionsXML(site);
            String siteEnd = "</site>";
            report.append(siteStart);
            report.append(extensionsXML);
            report.append(siteEnd);
        }
        return report;
    }
    
    public StringBuilder getExtensionsXML(SiteNode site) {
        StringBuilder extensionXml = new StringBuilder();
        ExtensionLoader loader = Control.getSingleton().getExtensionLoader();
        int extensionCount = loader.getExtensionCount();
        for(int i=0; i<extensionCount; i++) {
            Extension extension = loader.getExtension(i);
            if(extension instanceof XmlReporterExtension) {
                extensionXml.append(((XmlReporterExtension)extension).getXml(site));
            }
        }
        return extensionXml;
    }

    public void generateHtml(ViewDelegate view, Model model) {

        // ZAP: Allow scan report file name to be specified
        try {
            JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
            chooser.setFileFilter(new FileFilter() {

                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    } else if (file.isFile()
                            && file.getName().toLowerCase().endsWith(".htm")) {
                        return true;
                    } else if (file.isFile()
                            && file.getName().toLowerCase().endsWith(".html")) {
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
            if (rc == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
                if (file != null) {
                    Model.getSingleton().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
                    String fileNameLc = file.getAbsolutePath().toLowerCase();
                    if (!fileNameLc.endsWith(".htm")
                            && !fileNameLc.endsWith(".html")) {
                        file = new File(file.getAbsolutePath() + ".html");
                    }
                }

                if (!file.getParentFile().canWrite()) {
                    view.showMessageDialog(
                            MessageFormat.format(Constant.messages.getString("report.write.error"),
                            new Object[]{file.getAbsolutePath()}));
                    return;
                }

                File report = generate(file.getAbsolutePath(), model, "xml/report.html.xsl");
                if (report == null) {
                    view.showMessageDialog(
                            MessageFormat.format(Constant.messages.getString("report.unknown.error"),
                            new Object[]{file.getAbsolutePath()}));
                    return;
                }

                try {
                    BrowserLauncher bl = new BrowserLauncher();
                    bl.openURLinBrowser("file://" + report.getAbsolutePath());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    view.showMessageDialog(
                            MessageFormat.format(Constant.messages.getString("report.complete.warning"),
                            new Object[]{report.getAbsolutePath()}));
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            view.showWarningDialog("File creation error.");
        }
    }

    public void generateXml(ViewDelegate view, Model model) {

        // ZAP: Allow scan report file name to be specified
        try {
            JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
            chooser.setFileFilter(new FileFilter() {

                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    } else if (file.isFile()
                            && file.getName().toLowerCase().endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }

                public String getDescription() {
                    return Constant.messages.getString("file.format.xml");
                }
            });

            File file = null;
            int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
            if (rc == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
                if (file != null) {
                    Model.getSingleton().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
                    String fileNameLc = file.getAbsolutePath().toLowerCase();
                    if (!fileNameLc.endsWith(".xml")) {
                        file = new File(file.getAbsolutePath() + ".xml");
                    }
                }

                if (!file.getParentFile().canWrite()) {
                    view.showMessageDialog(
                            MessageFormat.format(Constant.messages.getString("report.write.error"),
                            new Object[]{file.getAbsolutePath()}));
                    return;
                }

                File report = generate(file.getAbsolutePath(), model, "xml/report.xml.xsl");
                if (report == null) {
                    view.showMessageDialog(
                            MessageFormat.format(Constant.messages.getString("report.unknown.error"),
                            new Object[]{file.getAbsolutePath()}));
                    return;
                }

                try {
                    BrowserLauncher bl = new BrowserLauncher();
                    bl.openURLinBrowser("file://" + report.getAbsolutePath());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    view.showMessageDialog(
                            MessageFormat.format(Constant.messages.getString("report.complete.warning"),
                            new Object[]{report.getAbsolutePath()}));
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            view.showWarningDialog(Constant.messages.getString("report.unexpected.warning"));
        }
    }
}
