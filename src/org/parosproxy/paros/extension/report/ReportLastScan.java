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
// StringBuilder instead of StringBuffer.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/07/12 Issue 713: Add CWE and WASC numbers to issues
// ZAP: 2013/12/03 Issue 933: Automatically determine install dir

package org.parosproxy.paros.extension.report;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
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

import edu.stanford.ejalbert.BrowserLauncher;

public class ReportLastScan {

    private Logger logger = Logger.getLogger(ReportLastScan.class);

    public ReportLastScan() {
    }


    public File generate(String fileName, Model model, String xslFile) throws Exception {
    	StringBuilder sb = new StringBuilder(500);
        this.generate(sb, model);
        return ReportGenerator.stringToHtml(sb.toString(), xslFile, fileName);
    }

    public void generate(StringBuilder report, Model model) throws Exception {
        report.append("<?xml version=\"1.0\"?>");
        report.append("<OWASPZAPReport version=\"").append(Constant.PROGRAM_VERSION).append("\" generated=\"").append(ReportGenerator.getCurrentDateTimeString()).append("\">\r\n");
        siteXML(report);
        report.append("</OWASPZAPReport>");
    }

    private void siteXML(StringBuilder report) {
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

                @Override
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

                @Override
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

                File report = generate(file.getAbsolutePath(), model, Constant.getZapInstall() + "/xml/report.html.xsl");
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

                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    } else if (file.isFile()
                            && file.getName().toLowerCase().endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }

                @Override
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

                File report = generate(file.getAbsolutePath(), model, Constant.getZapInstall() + "/xml/report.xml.xsl");
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
