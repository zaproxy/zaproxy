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
// ZAP: 2011/11/20 Set order
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods and
// removed unnecessary cast.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/12/03 Issue 934: Handle files on the command line via extension
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts
// ZAP: 2015/10/06 Issue 1962: Install and update add-ons from the command line
// ZAP: 2016/06/20 Removed unnecessary/unused constructor
// ZAP: 2016/09/22 Issue 2886: Support Markdown format
// ZAP: 2017/04/07 Added getUIName()
// ZAP: 2017/06/21 Issue 3559: Support JSON format
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/08/15 Issue 5297: Removed unused model params.
package org.parosproxy.paros.extension.report;

import java.io.File;
import java.util.List;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.view.ZapMenuItem;

/** @deprecated Replaced by the reports add-on in zap-extensions */
@Deprecated
public class ExtensionReport extends ExtensionAdaptor implements CommandLineListener {

    private static final String NAME = "ExtensionReport";

    private static final int ARG_LAST_SCAN_REPORT_IDX = 0;

    private ZapMenuItem menuItemHtmlReport = null;
    private ZapMenuItem menuItemMdReport = null;
    private ZapMenuItem menuItemXmlReport = null;
    private ZapMenuItem menuItemJsonReport = null;
    private CommandLineArgument[] arguments = new CommandLineArgument[1];

    public ExtensionReport() {
        super(NAME);
        this.setOrder(14);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("report.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            // extensionHook.getHookMenu().addNewMenu(getMenuReport());
            extensionHook.getHookMenu().addReportMenuItem(getMenuItemHtmlReport());
            extensionHook.getHookMenu().addReportMenuItem(getMenuItemXmlReport());
            extensionHook.getHookMenu().addReportMenuItem(getMenuItemMdReport());
            extensionHook.getHookMenu().addReportMenuItem(getMenuItemJsonReport());
        }
        extensionHook.addCommandLine(getCommandLineArguments());
    }

    private ZapMenuItem getMenuItemHtmlReport() {
        if (menuItemHtmlReport == null) {
            menuItemHtmlReport = new ZapMenuItem("menu.report.html.generate");
            menuItemHtmlReport.addActionListener(
                    e ->
                            new ReportLastScan()
                                    .generateReport(getView(), ReportLastScan.ReportType.HTML));
        }
        return menuItemHtmlReport;
    }

    private ZapMenuItem getMenuItemXmlReport() {
        if (menuItemXmlReport == null) {
            menuItemXmlReport = new ZapMenuItem("menu.report.xml.generate");
            menuItemXmlReport.addActionListener(
                    e ->
                            new ReportLastScan()
                                    .generateReport(getView(), ReportLastScan.ReportType.XML));
        }
        return menuItemXmlReport;
    }

    private ZapMenuItem getMenuItemMdReport() {
        if (menuItemMdReport == null) {
            menuItemMdReport = new ZapMenuItem("menu.report.md.generate");
            menuItemMdReport.addActionListener(
                    e ->
                            new ReportLastScan()
                                    .generateReport(getView(), ReportLastScan.ReportType.MD));
        }
        return menuItemMdReport;
    }

    private ZapMenuItem getMenuItemJsonReport() {
        if (menuItemJsonReport == null) {
            menuItemJsonReport = new ZapMenuItem("menu.report.json.generate");
            menuItemJsonReport.addActionListener(
                    e ->
                            new ReportLastScan()
                                    .generateReport(getView(), ReportLastScan.ReportType.JSON));
        }
        return menuItemJsonReport;
    }

    @Override
    public void execute(CommandLineArgument[] args) {

        if (arguments[ARG_LAST_SCAN_REPORT_IDX].isEnabled()) {
            CommandLineArgument arg = arguments[ARG_LAST_SCAN_REPORT_IDX];
            ReportLastScan report = new ReportLastScan();
            // ZAP: Removed unnecessary cast.
            String fileName = arg.getArguments().get(0);
            try {
                report.generate(fileName, ReportLastScan.ReportType.HTML);
                CommandLine.info("Last Scan Report generated at " + fileName);
            } catch (Exception e) {
                CommandLine.error(e.getMessage(), e);
            }
        } else {
            return;
        }
    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_LAST_SCAN_REPORT_IDX] =
                new CommandLineArgument(
                        "-last_scan_report",
                        1,
                        null,
                        "",
                        Constant.messages.getString("report.cmdline.gen.help"));
        return arguments;
    }

    @Override
    public String getAuthor() {
        return Constant.PAROS_TEAM;
    }

    @Override
    public boolean handleFile(File file) {
        // Cant handle any files
        return false;
    }

    @Override
    public List<String> getHandledExtensions() {
        // Cant handle any extensions
        return null;
    }
}
