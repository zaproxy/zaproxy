/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * Dialog reviewed for a new lifestyle...
 * @author yhawke (2014)
 */
public class ScanProgressDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(ScanProgressDialog.class);

    private transient Color JTABLE_ALTERNATE_BACKGROUND = (Color)LookAndFeel.getDesktopPropertyValue("Table.alternateRowColor", new Color(0xf2f2f2));
    
    private ExtensionActiveScan extension;
    private JScrollPane jScrollPane;
    private JTable table;
    private ScanProgressTableModel model;
    private JButton closeButton;
    private JButton copyToClipboardButton;
    private JComboBox<String> hostSelect;
    
    private ActiveScan scan;
    private boolean stopThread;

    private JFreeChart chart;
    private List<String> labelsAdded = new ArrayList<String>();
    private TimeSeries seriesTotal;
    private TimeSeries series100;
    private TimeSeries series200;
    private TimeSeries series300;
    private TimeSeries series400;
    private TimeSeries series500;
    
    private double lastCentre = -1;

    /**
     * Constructs a modal {@code ScanProgressDialog} with the given owner, target and active scan extension.
     * 
     * @param owner the {@code Frame} from which the dialog is displayed
     * @param target the scan target, shown as title if not {@code null}
     * @param extension the active scan extension, to obtain chart options
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public ScanProgressDialog(Frame owner, String target, ExtensionActiveScan extension) {
        super(owner, false);
        if (target != null) {
            this.setTitle(MessageFormat.format(Constant.messages.getString("ascan.progress.title"), target));
        }
        this.extension = extension;
        this.initialize();
    }

    private void initialize() {
        this.setSize(new Dimension(580, 504));

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel tab1 = new JPanel();
        tab1.setLayout(new GridBagLayout());

        JPanel hostPanel = new JPanel();
        hostPanel.setLayout(new GridBagLayout());
        hostPanel.add(new JLabel(Constant.messages.getString("ascan.progress.label.host")), LayoutHelper.getGBC(0, 0, 1, 0.4D));
        hostPanel.add(getHostSelect(), LayoutHelper.getGBC(1, 0, 1, 0.6D));
        tab1.add(hostPanel, LayoutHelper.getGBC(0, 0, 3, 1.0D, 0.0D));
        
        tab1.add(getJScrollPane(), LayoutHelper.getGBC(0, 1, 3, 1.0D, 1.0D));
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonsPanel.add(getCopyToClipboardButton());
        buttonsPanel.add(getCloseButton());
        tab1.add(buttonsPanel, LayoutHelper.getGBC(0, 2, 3, 1.0D));
        
        tabbedPane.insertTab(Constant.messages.getString("ascan.progress.tab.progress"), null, tab1, null, 0);
        this.add(tabbedPane);

        int mins = extension.getScannerParam().getMaxChartTimeInMins();
        if (mins > 0) {
	        // Treat zero mins as disabled
	        JPanel tab2 = new JPanel();
	        tab2.setLayout(new GridBagLayout());
	        
	        this.seriesTotal = new TimeSeries("TotalResponses");	// Name not shown, so no need to i18n
	        final TimeSeriesCollection dataset = new TimeSeriesCollection(this.seriesTotal);
	        
	        this.series100 = new TimeSeries(Constant.messages.getString("ascan.progress.chart.1xx"));
	        this.series200 = new TimeSeries(Constant.messages.getString("ascan.progress.chart.2xx"));
	        this.series300 = new TimeSeries(Constant.messages.getString("ascan.progress.chart.3xx"));
	        this.series400 = new TimeSeries(Constant.messages.getString("ascan.progress.chart.4xx"));
	        this.series500 = new TimeSeries(Constant.messages.getString("ascan.progress.chart.5xx"));
	        
	        long maxAge = mins * 60;
	        this.seriesTotal.setMaximumItemAge(maxAge);
	        this.series100.setMaximumItemAge(maxAge);
	        this.series200.setMaximumItemAge(maxAge);
	        this.series300.setMaximumItemAge(maxAge);
	        this.series400.setMaximumItemAge(maxAge);
	        this.series500.setMaximumItemAge(maxAge);
	        
	        dataset.addSeries(series100);
	        dataset.addSeries(series200);
	        dataset.addSeries(series300);
	        dataset.addSeries(series400);
	        dataset.addSeries(series500);
	        
	        chart = createChart(dataset);
	        // Set up some vaguesly sensible colours
	        chart.getXYPlot().getRenderer(0).setSeriesPaint(0, Color.BLACK);	// Totals
	        chart.getXYPlot().getRenderer(0).setSeriesPaint(1, Color.GRAY);		// 100: Info
	        chart.getXYPlot().getRenderer(0).setSeriesPaint(2, Color.GREEN);	// 200: OK
	        chart.getXYPlot().getRenderer(0).setSeriesPaint(3, Color.BLUE);		// 300: Info
	        chart.getXYPlot().getRenderer(0).setSeriesPaint(4, Color.MAGENTA);	// 400: Bad req
	        chart.getXYPlot().getRenderer(0).setSeriesPaint(5, Color.RED);		// 500: Internal error
	
	        
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        tab2.add(chartPanel, LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D)); 
	        
	        tabbedPane.insertTab(Constant.messages.getString("ascan.progress.tab.chart"), null, tab2, null, 1);
        }

        // Stop the updating thread when the window is closed
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopThread = true;
            }
        });
    }

    private JFreeChart createChart(final XYDataset dataset) {
        JFreeChart result = ChartFactory.createTimeSeriesChart(
            null, // No title - it just takes up space 
            Constant.messages.getString("ascan.progress.chart.time"), 
            Constant.messages.getString("ascan.progress.chart.responses"),
            dataset, 
            true, 
            true, 
            false
        );
        XYPlot plot = result.getXYPlot();
        ValueAxis daxis = plot.getDomainAxis();
        daxis.setAutoRange(true);
        daxis.setAutoRangeMinimumSize(60000.0);
        
        plot.getRangeAxis().setAutoRangeMinimumSize(20);
        
        return result;
    }
    
	/**
     * Get the dialog scroll panel
     * @return the panel
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getMainPanel());
            jScrollPane.setName("ScanProgressScrollPane");
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }
        
        return jScrollPane;
    }
    
    private JButton getCloseButton() {
    	// Note that on Linux dialogs dont get close buttons on the frame decoration
    	if (closeButton == null) {
    		closeButton = new JButton(Constant.messages.getString("all.button.close"));
    		closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispatchEvent(new WindowEvent(ScanProgressDialog.this, WindowEvent.WINDOW_CLOSING));
				}});
    	}
    	return closeButton;
    }

    private JButton getCopyToClipboardButton() {
        if (copyToClipboardButton == null) {
            copyToClipboardButton = new JButton(Constant.messages.getString("ascan.progress.copyclipboard.button.label"));
            copyToClipboardButton.setToolTipText(Constant.messages.getString("ascan.progress.copyclipboard.button.tooltip"));
            copyToClipboardButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    // Mimics the implementation of BasicTableUI.TableTransferHandler.createTransferable(JComponent) but copies
                    // all rows (including column names), not just selected rows/columns (which are none in this case).
                    StringBuilder plainContent = new StringBuilder();
                    StringBuilder htmlContent = new StringBuilder();

                    htmlContent.append("<html>\n<body>\n<table>\n");

                    TableModel tableModel = getMainPanel().getModel();
                    htmlContent.append("<tr>\n");
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        String val = tableModel.getColumnName(col);
                        plainContent.append(val).append('\t');
                        htmlContent.append("  <td>").append(val).append("</td>\n");
                    }
                    plainContent.deleteCharAt(plainContent.length() - 1).append("\n");
                    htmlContent.append("</tr>\n");

                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        htmlContent.append("<tr>\n");
                        for (int col = 0; col < tableModel.getColumnCount(); col++) {
                            Object obj = tableModel.getValueAt(row, col);
                            String val = (obj == null) ? "" : obj.toString();
                            plainContent.append(val).append('\t');
                            htmlContent.append("  <td>").append(val).append("</td>\n");
                        }
                        plainContent.deleteCharAt(plainContent.length() - 1).append("\n");
                        htmlContent.append("</tr>\n");
                    }
                    plainContent.deleteCharAt(plainContent.length() - 1);
                    htmlContent.append("</table>\n</body>\n</html>");

                    Transferable transferable = new BasicTransferable(plainContent.toString(), htmlContent.toString());
                    try {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                    } catch (IllegalStateException e) {
                        View.getSingleton().showWarningDialog(
                                ScanProgressDialog.this,
                                Constant.messages.getString("ascan.progress.copyclipboard.error"));
                        log.warn("Failed to copy the contents to clipboard:", e);
                    }
                }
            });
        }
        return copyToClipboardButton;
    }

    /**
     * Get the main content panel of the dialog
     * @return the main panel
     */
    private JTable getMainPanel() {
        if (table == null) {            
            model = new ScanProgressTableModel();
            
            table = new JTable();
            table.setModel(model);
            table.setRowSelectionAllowed(false);
            table.setColumnSelectionAllowed(false);
            table.setDoubleBuffered(true);

            // First column is for plugin's name
            table.getColumnModel().getColumn(0).setPreferredWidth(256);
            table.getColumnModel().getColumn(1).setPreferredWidth(80);

            // Second column is for plugin's status
            table.getColumnModel().getColumn(2).setPreferredWidth(80);
            table.getColumnModel().getColumn(2).setCellRenderer(new ScanProgressBarRenderer());

            // Third column is for plugin's elapsed time
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            table.getColumnModel().getColumn(3).setPreferredWidth(85);                  
            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            
            // Forth column is for plugin's request count
            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
            table.getColumnModel().getColumn(4).setPreferredWidth(60);
            table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
            
            // Fifth column is for plugin's completion and actions
            table.getColumnModel().getColumn(5).setPreferredWidth(40);                  
            table.getColumnModel().getColumn(5).setCellRenderer(new ScanProgressActionRenderer());

            ScanProgressActionListener listener = new ScanProgressActionListener(table, model);
            table.addMouseListener(listener);
            table.addMouseMotionListener(listener);
        }
        
        return table;
    }
    
    /**
     * Updates the scan progress shown by the dialogue (scanners' progress/state and chart).
     */
    private void updateProgress() {
        // Start panel data settings
    	HostProcess hp = getSelectedHostProcess();
        if (scan.getHostProcesses() != null && hp != null) {

            // Update the main table entries
            model.updateValues(scan, hp);

            if (scan.isStopped()) {
                this.stopThread = true;
            }
            
            if (chart != null) {
	            ResponseCountSnapshot snapshot = scan.getRequestHistory();
	            while (snapshot != null) {
	            	try {
	            		Second second = new Second(snapshot.getDate());
						this.seriesTotal.add(second, snapshot.getTotal());
						this.series100.add(second, snapshot.getResp100());
						this.series200.add(second, snapshot.getResp200());
						this.series300.add(second, snapshot.getResp300());
						this.series400.add(second, snapshot.getResp400());
						this.series500.add(second, snapshot.getResp500());
			            snapshot = scan.getRequestHistory();
			            
			            for (Plugin plugin : scan.getHostProcesses().get(0).getRunning()) {
			            	if (!labelsAdded.contains(plugin.getName())) {
			            		// Add a vertical line with the plugin name
				            	ValueMarker vm = new ValueMarker(plugin.getTimeStarted().getTime());
				            	
								double center = chart.getXYPlot().getRangeAxis().getRange().getCentralValue();
								if (lastCentre != center) {
									if (lastCentre != -1) {
										// Move the existing labels so they stay in the centre
										@SuppressWarnings("rawtypes")
										List annotations = chart.getXYPlot().getAnnotations();
										for (Object o: annotations) {
											if (o instanceof XYTextAnnotation) {
												XYTextAnnotation annotation = (XYTextAnnotation)o;
												annotation.setY(center);
											}
										}
									}
									lastCentre = center;
								}
				            	
				            	XYTextAnnotation updateLabel = 
				            			new XYTextAnnotation(plugin.getName(), 
				            					plugin.getTimeStarted().getTime(), center);
				            	updateLabel.setFont(FontUtils.getFont("Sans Serif"));
				            	updateLabel.setRotationAnchor(TextAnchor.BASELINE_CENTER);
				            	
				            	updateLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
				            	updateLabel.setRotationAngle(-3.14 / 2);
				            	updateLabel.setPaint(Color.black);
				            	chart.getXYPlot().addDomainMarker(vm, Layer.BACKGROUND);
				            	chart.getXYPlot().addAnnotation(updateLabel);
			            		labelsAdded.add(plugin.getName());
			            	}
			            }
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						snapshot = null;
					}
	            }
            }
        }
    }
    
    private HostProcess getSelectedHostProcess() {
    	String str = (String)this.getHostSelect().getSelectedItem();
    	if (str == null) {
    		return null;
    	}
        for (HostProcess hp : scan.getHostProcesses()) {
        	if (str.equals(hp.getHostAndPort())) {
        		return hp;
        	}
        }
        return null;
    }

    /**
     * Set the scan that will be shown in this dialog.
     * 
     * @param scan the active scan, might be {@code null}.
     */
    public void setActiveScan(ActiveScan scan) {
        this.scan = scan;

        if (scan == null) {
            return;
        }
        getHostSelect().removeAll();
        for (HostProcess hp : scan.getHostProcesses()) {
        	getHostSelect().addItem(hp.getHostAndPort());
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!stopThread) {
                	SwingUtilities.invokeLater(new Runnable(){
						@Override
						public void run() {
							updateProgress();
						}});
                    
                    try {
                        sleep(200);
                        
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
        };
        
        thread.start();
    }
    
    private JComboBox<String> getHostSelect() {
    	if (hostSelect == null) {
    		hostSelect = new JComboBox<String>();
    		hostSelect.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// Switch results, esp necessary when the scan has finished
					updateProgress();
				}});
    	}
    	return hostSelect;
    }


    /**
     * Custom Renderer for the progress bar plugin column
     */
    private class ScanProgressBarRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent result;
            if (value != null) {
                ScanProgressItem item = (ScanProgressItem)value;                
                JProgressBar bar = new JProgressBar();
                bar.setMaximum(100);
                
                bar.setValue(item.getProgressPercentage());                
                result = bar;
                
            } else {
                result = (JComponent)Box.createGlue();
            }
            
            // Set all general configurations
            result.setOpaque(true);
            result.setBackground(JTABLE_ALTERNATE_BACKGROUND);
            
            return result;
        }
    }    
    
    /**
     * Custom Renderer for the actions column (skipping)
     */
    private class ScanProgressActionRenderer implements TableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent result;
            if (value != null) {
                ScanProgressActionIcon action = (ScanProgressActionIcon)value;                
                if (action == model.getFocusedAction()) {
                    action.setOver();
                    
                } else {
                    action.setNormal();
                }
                
                result = action;
                
            } else {
                result = (JComponent)Box.createGlue();
            }
         
            // Set all general configurations
            result.setOpaque(true);
            result.setBackground(JTABLE_ALTERNATE_BACKGROUND);
            
            return result;
        }
    }

    /**
     * Listener for all Action's management (skipping for now)
     */
    private static class ScanProgressActionListener extends MouseAdapter {

        /**
         * Constant that indicates that a column or row was not found.
         */
        private static final int NOT_FOUND = -1;

        private final JTable table;
        private final ScanProgressTableModel model;
        
        public ScanProgressActionListener(JTable table, ScanProgressTableModel model) {
            this.table = table;
            this.model = model;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            ScanProgressActionIcon action = getScanProgressAction(e.getPoint());
            if (action != null) {
                action.invokeAction();
            }            
        }

        @Override
        public void mousePressed(MouseEvent e) {
            ScanProgressActionIcon action = getScanProgressAction(e.getPoint());
            if (action != null) {
                action.setPressed();
                action.repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            ScanProgressActionIcon action = getScanProgressAction(e.getPoint());
            if (action != null) {
                action.setReleased();
                action.repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            ScanProgressActionIcon action = getScanProgressAction(me.getPoint());
            if (action != null) {
                model.setFocusedAction(action);
                action.repaint();
                
            } else if (model.getFocusedAction() != null) {
                model.setFocusedAction(action);
                table.repaint();
            }
        }

        /**
         * Gets the {@code ScanProgressActionIcon} at the given point, if any.
         * 
         * @param point the point to get the scan progress action icon
         * @return the {@code ScanProgressActionIcon} at the given point, or {@code null} if none
         */
        private ScanProgressActionIcon getScanProgressAction(Point point) {
            int column = table.columnAtPoint(point);
            if (column == NOT_FOUND) {
                return null;
            }

            int row = table.rowAtPoint(point);
            if (row == NOT_FOUND) {
                return null;
            }

            Object value = table.getValueAt(row, column);
            if (value instanceof ScanProgressActionIcon) {
                return (ScanProgressActionIcon)value;
            }
            
            return null;
        }        
    }
    
}
