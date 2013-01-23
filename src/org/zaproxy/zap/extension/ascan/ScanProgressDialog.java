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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.extension.AbstractDialog;
import org.zaproxy.zap.view.LayoutHelper;

public class ScanProgressDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private JScrollPane jScrollPane = null;
	
	private JTable table = null;
	private ScanProgressTableModel model = new ScanProgressTableModel();
	
	private String site = null;
	private ActiveScan scan = null;
	private boolean stopThread = false;

	public ScanProgressDialog(Frame owner, String site) {
		super(owner, false);
		this.site = site;
		this.initialize();
	}
	
	private  void initialize() {
		this.setLayout(new GridBagLayout());
		this.setSize(new Dimension(500, 500));
		
		if (site != null) {
			this.setTitle(MessageFormat.format(
					Constant.messages.getString("ascan.progress.title"), site));
		}
		
        this.add(getJScrollPane(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D));
        
        //  Handle escape key to close the dialog    
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
				stopThread = true;
            	ScanProgressDialog.this.setVisible(false);
            	ScanProgressDialog.this.dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);

        // Stop the updating thread when the window is closed
        this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				// Ignore
			}
			@Override
			public void windowClosing(WindowEvent e) {
				// Ignore
			}
			@Override
			public void windowClosed(WindowEvent e) {
				stopThread = true;
			}
			@Override
			public void windowIconified(WindowEvent e) {
				// Ignore
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
				// Ignore
			}
			@Override
			public void windowActivated(WindowEvent e) {
				// Ignore
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
				// Ignore
			}});
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTable());
			jScrollPane.setName("ScanProgressScrollPane");
			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return jScrollPane;
	}
	
	private JTable getTable() {
		if (table == null) {
			table = new JTable();
			table.setModel(this.model);
			table.getColumnModel().getColumn(0).setPreferredWidth(300);
			table.getColumnModel().getColumn(1).setPreferredWidth(100);
			table.getColumnModel().getColumn(2).setPreferredWidth(100);
		}
		return table;
	}
	
	private String getElapsedTime(Date start, Date end) {
		if (start == null) {
			return "";
		} else if (end == null) {
			end = new Date();	// measure to now
		}
		long elapsed = end.getTime() - start.getTime();
		
		return String.format("%02d:%02d.%03d", elapsed/60000, (elapsed%60000)/1000, (elapsed%1000));
	}
	
	private void showProgress() {
		
		List<HostProcess> list = scan.getHostProcesses();
		List<String[]> values = new ArrayList<String[]>();
		boolean completed = true;
		
		if (list != null) {
			for (HostProcess hp : list) {
				for (Plugin plugin : hp.getCompleted()) {
					values.add(new String[] {plugin.getName(), Constant.messages.getString("ascan.progress.label.completed"), 
							this.getElapsedTime(plugin.getTimeStarted(), plugin.getTimeFinished())});
				}
				for (Plugin plugin : hp.getRunning()) {
					values.add(new String[] {plugin.getName(), Constant.messages.getString("ascan.progress.label.running"), 
							this.getElapsedTime(plugin.getTimeStarted(), plugin.getTimeFinished())});
					completed = false;
				}
				for (Plugin plugin : hp.getPending()) {
					values.add(new String[] {plugin.getName(), Constant.messages.getString("ascan.progress.label.pending"), ""});
					completed = false;
				}
			}
			model.setValues(values);
			if (completed) {
				this.stopThread = true;
			}
		}
	}

	public void setActiveScan(ActiveScan scan) {
		this.scan = scan;
		
		if (scan == null) {
			return;
		}
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (! stopThread) {
					showProgress();
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

}
