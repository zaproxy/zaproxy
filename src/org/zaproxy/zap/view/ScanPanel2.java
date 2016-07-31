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
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.utils.SortedComboBoxModel;

/*
 * This is a cleaner version of ScanPanel which doesnt mix functionality and the UI.
 * Implemented as a new set of classes for backwards compatibility with existing add-ons
 */
public abstract class ScanPanel2<GS extends GenericScanner2, SC extends ScanController<GS>> extends AbstractPanel {
	private static final long serialVersionUID = 1L;

	protected enum Location {start, beforeSites, beforeButtons, beforeProgressBar, afterProgressBar};
	private final String prefix;
	
	private final SC controller;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JLabel scannedCountNameLabel = null;
	private JLabel foundCountNameLabel = null;

	private JComboBox<ScanEntry<GS>> progressSelect = null;
	private SortedComboBoxModel<ScanEntry<GS>> progressModel = new SortedComboBoxModel<>();
	private final ScanEntry<GS> selectScanEntry;

	private JButton stopScanButton = null;
	private ZapToggleButton pauseScanButton = null;
	private JButton newScanButton = null;
	private JButton clearScansButton = null;
	private JButton optionsButton = null;
	private JProgressBar progressBar = null;
	private ScanStatus scanStatus = null;
	private Mode mode = Control.getSingleton().getMode();
	
	private static Logger log = Logger.getLogger(ScanPanel2.class);
    
    /**
     * Constructs a {@code ScanPanel2} with the given message resources prefix, tab icon and scan controller.
     * 
     * @param prefix the prefix for the resource messages
     * @param icon the icon for the tab of the panel
     * @param controller the scan controller
     * @param scanParam unused
     * @deprecated (TODO add version) Use {@link #ScanPanel2(String, ImageIcon, ScanController)} instead.
     */
    @Deprecated
    public ScanPanel2(String prefix, ImageIcon icon, SC controller, AbstractParam scanParam) {
        this(prefix, icon, controller);
    }

    /**
     * Constructs a {@code ScanPanel2} with the given message resources prefix, tab icon and scan controller.
     * 
     * @param prefix the prefix for the resource messages
     * @param icon the icon for the tab of the panel
     * @param controller the scan controller
     * @since TODO add version
     */
    public ScanPanel2(String prefix, ImageIcon icon, SC controller) {
        super();
        this.prefix = prefix;
        this.controller = controller;
        selectScanEntry = new ScanEntry<>(Constant.messages.getString(prefix + ".toolbar.progress.select"));
 		initialize(icon);
 		log.debug("Constructor " + prefix);
    }

	/**
	 * This method initializes this
	 * 
	 */
	private  void initialize(ImageIcon icon) {
        this.setLayout(new CardLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(474, 251);
        }
        this.setName(Constant.messages.getString(prefix + ".panel.title"));
		this.setIcon(icon);
        this.add(getPanelCommand(), prefix + ".panel");
        scanStatus = new ScanStatus(icon, Constant.messages.getString(prefix + ".panel.title"));
        
        if (View.isInitialised()) {
        	View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarRightLabel(scanStatus.getCountLabel());
        }
	}

	/**
	 * This method initializes panelCommand
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {

			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName(prefix + ".panel");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			panelCommand.add(this.getPanelToolbar(), gridBagConstraints1);
			panelCommand.add(getWorkPanel(), gridBagConstraints2);
			
		}
		return panelCommand;
	}
	
	protected GridBagConstraints getGBC(int gridx, int gridy) {
		return this.getGBC(gridx, gridy, 0.0, new Insets(0, 2, 0, 0));
	}

	protected GridBagConstraints getGBC(int gridx, int gridy, double weightx, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = weightx;
		if (weightx > 0.0) {
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		}
		gbc.insets = insets;
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		return gbc;
	}

	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setName(prefix + ".toolbar");
			
			int x = 0;

			x = this.addToolBarElements(panelToolbar, Location.start, x);
			
			newScanButton = getNewScanButton();

			if (newScanButton != null) {
				panelToolbar.add(newScanButton, getGBC(x++,0));
				newScanButton.setEnabled( ! Mode.safe.equals(mode));
				panelToolbar.addSeparator();
				x++;
			}

			panelToolbar.add(new JLabel(Constant.messages.getString(prefix + ".toolbar.progress.label")), getGBC(x++,0));
			panelToolbar.add(getProgressSelect(), getGBC(x++,0));

			x = this.addToolBarElements(panelToolbar, Location.beforeButtons, x);

			panelToolbar.add(getPauseScanButton(), getGBC(x++,0));
			panelToolbar.add(getStopScanButton(), getGBC(x++,0));

			x = this.addToolBarElements(panelToolbar, Location.beforeProgressBar, x);
			
			panelToolbar.add(getProgressBar(), getGBC(x++,0, 1.0, new Insets(0,5,0,5)));

			panelToolbar.add(getClearScansButton(), getGBC(x++,0));

			panelToolbar.add(getActiveScansNameLabel(), getGBC(x++,0));
			panelToolbar.add(getActiveScansValueLabel(), getGBC(x++,0));
			
			x = this.addToolBarElements(panelToolbar, Location.afterProgressBar, x);
			
			panelToolbar.add(new JLabel(), getGBC(x++,0, 1.0, new Insets(0,0,0,0)));	// Spacer
			panelToolbar.add(getOptionsButton(), getGBC(x++,0));
		}
		return panelToolbar;
	}

	/**
	 * Adds elements to the tool bar. The method is called while initializing the ScanPanel, at the
	 * points specified by the {@link Location} enumeration. Should be overridden by all subclasses
	 * that want to add new elements to the ScanPanel's tool bar.
	 * 
	 * <p>
	 * The tool bar uses a {@code GridBagLayout}, so elements have to be added with a
	 * {@code GridBagConstraints}. For this, the {@code getGBC} methods can be used. The {@code gridX} parameter
	 * specifies the cell (as used in {@code GridBagConstraints.gridx}) of the current row where the elements can
	 * be added.
	 * </p>
	 * <p>
	 * The method must return the new coordinates of the current cell, after the elements have been
	 * added.
	 * </p>
	 * 
	 * @param toolBar the tool bar
	 * @param location the current location where elements will be added
	 * @param gridX the x coordinates of the current cell in the {@code GridBagLayout}
	 * @return the new coordinates of the current cell, after the elements have been added.
	 * @see #getGBC(int, int)
	 * @see #getGBC(int, int, double, Insets)
	 * @see GridBagConstraints
	 * @see GridBagLayout
	 */
	protected int addToolBarElements(JToolBar toolBar, Location location, int gridX) {
		return gridX;
	}

	private JLabel getActiveScansNameLabel() {
		if (scannedCountNameLabel == null) {
			scannedCountNameLabel = new javax.swing.JLabel();
			scannedCountNameLabel.setText(Constant.messages.getString(prefix + ".toolbar.ascans.label"));
		}
		return scannedCountNameLabel;
	}
	
	private JLabel getActiveScansValueLabel() {
		if (foundCountNameLabel == null) {
			foundCountNameLabel = new javax.swing.JLabel();
			foundCountNameLabel.setText(String.valueOf(controller.getActiveScans().size()));
		}
		return foundCountNameLabel;
	}
	
	private void setActiveScanLabelsEventHandler() {
		List<GS> ascans = controller.getActiveScans();
		getActiveScansValueLabel().setText(String.valueOf(ascans.size()));
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		for (GS ascan : ascans) {
			sb.append(ascan.getDisplayName());
			sb.append("<br>");
		}
		sb.append("</html>");
		
		final String toolTip = sb.toString();
		getActiveScansNameLabel().setToolTipText(toolTip);
		getActiveScansValueLabel().setToolTipText(toolTip);

		scanStatus.setScanCount(ascans.size());
		this.getClearScansButton().setEnabled(controller.getAllScans().size() - ascans.size() > 0);

	}
	
	protected JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setSize(new Dimension(80,20));
			progressBar.setStringPainted(true);
			progressBar.setEnabled(false);
		}
		return progressBar;
	}
	
	protected JButton getStopScanButton() {
		if (stopScanButton == null) {
			stopScanButton = new JButton();
			stopScanButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.stop"));
			stopScanButton.setIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/16/142.png")));
			stopScanButton.setEnabled(false);
			stopScanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					GS scanner = getSelectedScanner();
					if (scanner != null) {
						controller.stopScan(scanner.getScanId());
					}
				}
			});
		}
		return stopScanButton;
	}

	protected JToggleButton getPauseScanButton() {
		if (pauseScanButton == null) {
			pauseScanButton = new ZapToggleButton();
			pauseScanButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.pause"));
			pauseScanButton.setSelectedToolTipText(Constant.messages.getString(prefix + ".toolbar.button.unpause"));
			pauseScanButton.setIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/16/141.png")));
			pauseScanButton.setRolloverIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/16/141.png")));
			pauseScanButton.setSelectedIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/16/131.png")));
			pauseScanButton.setRolloverSelectedIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/16/131.png")));
			pauseScanButton.setEnabled(false);
			pauseScanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					GS scanner = getSelectedScanner();
					if (scanner != null) {
						if (pauseScanButton.isSelected()) {
							controller.pauseScan(scanner.getScanId());
						} else {
							controller.resumeScan(scanner.getScanId());
						}
					}
				}
			});
		}
		return pauseScanButton;
	}

	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/16/041.png")));
			optionsButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options(
							Constant.messages.getString(prefix + ".options.title"));
				}
			});
		}
		return optionsButton;
	}
	
	private JButton getClearScansButton() {
		if (clearScansButton == null) {
			clearScansButton = new JButton();
			clearScansButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.clear"));
			clearScansButton.setIcon(new ImageIcon(ScanPanel2.class.getResource("/resource/icon/fugue/broom.png")));
			clearScansButton.setEnabled(false);
			clearScansButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
	        		clearFinishedScans();
				}
			});
		}
		return clearScansButton;
	}
	
	public void clearFinishedScans() {
		// Remove via controller
		int count = controller.removeFinishedScans();
		if (count > 0) {
			// Some were removed - remove all and add back the remaining ones
			progressModel.removeAllElements();
			progressModel.addElement(selectScanEntry);
			for (GS scan : controller.getAllScans()) {
				progressModel.addElement(new ScanEntry<>(scan));
			}
			updateScannerUI();
		}
		clearScansButton.setEnabled(false);
	}
	
	public GS getSelectedScanner() {
		Object selectedItem = progressModel.getSelectedItem();
		if (selectedItem == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		GS scan = ((ScanEntry<GS>) selectedItem).getScan();
		return scan;
	}
	
	protected JComboBox<ScanEntry<GS>> getProgressSelect() {
		if (progressSelect == null) {
			progressSelect = new JComboBox<>(progressModel);
			progressSelect.addItem(selectScanEntry);
			progressSelect.setSelectedIndex(0);
			progressSelect.setEnabled(false);

			progressSelect.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					scannerSelected(getSelectedScanner());
				}
			});
		}
		return progressSelect;
	}

	public void updateScannerUI() {
		scannerSelected(this.getSelectedScanner());
	}

	private void scannerSelected(GS scanner) {
		updateProgressAndButtonsState(scanner);
		switchView(scanner);
	}

	private void updateProgressAndButtonsState(GS scanner) {
		if (scanner == null || Mode.safe.equals(Control.getSingleton().getMode())) {
			// Disable everything
			getStopScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(false);
			getPauseScanButton().setSelected(false);
			getProgressBar().setEnabled(false);
		} else if (scanner.isStopped()) {
			getStopScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(false);
			getPauseScanButton().setSelected(false);
			getProgressBar().setEnabled(false);
		} else {
			getStopScanButton().setEnabled(true);
			getPauseScanButton().setEnabled(true);
			getPauseScanButton().setSelected(scanner.isPaused());
			getProgressBar().setEnabled(true);
		}
		
		if (scanner != null) {
			getProgressBar().setValue(scanner.getProgress());
			getProgressBar().setMaximum(scanner.getMaximum());
		} else {
			getProgressBar().setValue(0);
		}
	}

	public void scanFinshed(final int id, final String host) {
	    if (EventQueue.isDispatchThread()) {
        	scanFinshedEventHandler(id, host);
	    } else {
	        try {
	            EventQueue.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                	scanFinshedEventHandler(id, host);
	                }
	            });
	        } catch (Exception e) {
	            log.error(e.getMessage(), e);
	        }
	    }
	}

	private void scanFinshedEventHandler(int id, String host) {
		log.debug("scanFinished " + prefix + " on " + host);
		if (this.getSelectedScanner() != null && this.getSelectedScanner().getScanId() == id) {
			updateProgressAndButtonsState(getSelectedScanner());
		}
    	setActiveScanLabelsEventHandler();
	}

	public void scanProgress(final int id, final String host, final int progress, final int maximum) {
	    if (EventQueue.isDispatchThread()) {
	    	scanProgressEventHandler(id, host, progress, maximum);
	    } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                	scanProgressEventHandler(id, host, progress, maximum);
                }
            });
	    }
	}

	private void scanProgressEventHandler(int id, String host, int progress, int maximum) {
		//log.debug("scanProgress " + prefix + " on " + currentSite + " " + progress);
		if (this.getSelectedScanner() != null && id == this.getSelectedScanner().getScanId()) {
			updateProgressAndButtonsState(getSelectedScanner());
		}		
    	setActiveScanLabelsEventHandler();
	}
	
	public void scannerStarted(GS scanner) {
		ScanEntry<GS> scanEntry = new ScanEntry<>(scanner);
		this.progressModel.addElement(scanEntry);
		this.getProgressSelect().setEnabled(true);
		this.getProgressSelect().setSelectedItem(scanEntry);
		this.trimProgressList();
		this.scannerSelected(scanner);
	}
	
	public void trimProgressList() {
		/*
		 * We only trim scans that have completed, so if the user kicks off a load of scans then
		 * we could have a lot more in the list than the 'maximum'
		 */
		if (this.progressModel.getSize() > this.getNumberOfScansToShow() + 1) {
			// Trim past results - the +1 is for the initial 'select scan' message
			for (int i=1; i < this.progressModel.getSize(); i++) {
				GS scan = this.progressModel.getElementAt(i).getScan();
				if (scan != null && scan.isStopped()) {
					controller.removeScan(scan.getScanId());
					this.progressModel.removeElementAt(i);
					
					if (this.progressModel.getSize() <= this.getNumberOfScansToShow() + 1) {
						// Have removed enough
						break;
					}
					// Need to remove more, but the indexes will have changed so go back 1
					i--;
				}
			}
		}
	}
	
	public void reset() {
		log.debug("reset " + prefix);

		progressModel.removeAllElements();
		progressSelect.addItem(selectScanEntry);
		progressSelect.setSelectedIndex(0);

		clearScansButton.setEnabled(false);
	}

	public void sessionScopeChanged(Session session) {
	}

	public void sessionModeChanged(Mode mode) {
		if (newScanButton != null) {
			this.newScanButton.setEnabled( ! Mode.safe.equals(mode));
		}
		// This will handle the remaining changes needed
		updateScannerUI();
	}
	
	protected void unload() {
		if (View.isInitialised()) {
			View.getSingleton().getMainFrame().getMainFooterPanel().removeFooterToolbarRightLabel(scanStatus.getCountLabel());
		}
	}
	
	protected SC getController() {
		return controller;
	}
	
    protected abstract Component getWorkPanel();
	
	protected abstract void switchView (GS scanner);
	/*
	 * Returns the scan button. Can return null if not relevant
	 */
	protected abstract JButton getNewScanButton();

	protected abstract int getNumberOfScansToShow();

    private static class ScanEntry<GS extends GenericScanner2> implements Comparable<ScanEntry<GS>> {

        private final GS scan;
        private final String label;

        public ScanEntry(String label) {
            this.scan = null;
            this.label = label;
        }

        public ScanEntry(GS scan) {
            this.scan = scan;
            this.label = scan.getScanId() + ": " + scan.getDisplayName();
        }

        public GS getScan() {
            return scan;
        }

        @Override
        public int hashCode() {
            return 31 + ((scan == null) ? 0 : scan.getScanId());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ScanEntry<?> other = (ScanEntry<?>) obj;
            if (scan == null) {
                return (other.scan == null);
            } else if (other.scan == null) {
                return false;
            }
            return scan.getScanId() == other.scan.getScanId();
        }

        @Override
        public int compareTo(ScanEntry<GS> other) {
            if (other == null || other.scan == null) {
                return 1;
            }
            if (scan == null) {
                return -1;
            }
            return scan.getScanId() - other.scan.getScanId();
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
