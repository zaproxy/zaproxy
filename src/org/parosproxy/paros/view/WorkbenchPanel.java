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
// ZAP: 2011/05/31 Added option to dynamically change the display
// ZAP: 2011/07/25 Added automatically save/restore of divider locations
// ZAP: 2013/02/17 Issue 496: Allow to see the request and response at the same 
// time in the main window
// ZAP: 2013/02/26 Issue 540: Maximised work tabs hidden when response tab
// position changed
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Removed redundant final modifiers from private methods
// ZAP: 2013/12/13 Added support for 'Full Layout'.

package org.parosproxy.paros.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.TabbedPanel2;
import org.parosproxy.paros.extension.AbstractPanel;

public class WorkbenchPanel extends JPanel {

	private static final long serialVersionUID = -4610792807151921550L;

	private static final String PREF_DIVIDER_LOCATION = "divider.location";
	private static final String DIVIDER_VERTICAL = "vertical";
	private static final String DIVIDER_HORIZONTAL = "horizontal";

	private JSplitPane splitVert = null;
	private JSplitPane splitHoriz = null;
	private JSplitPane splitFull = null;

  /* panels used when presenting views */
	private JPanel paneStatus = null;
	private JPanel paneSelect = null;
	private JPanel paneWork = null;

  /* panels for normal view */
	private TabbedPanel2 tabbedStatus = null;
	private TabbedPanel2 tabbedWork = null;
	private TabbedPanel2 tabbedSelect = null;
  
  /* panels used when going into 'Full Layout' to remember the old tab positions */
	private TabbedPanel2 tabbedOldStatus = null;
	private TabbedPanel2 tabbedOldWork = null;
	private TabbedPanel2 tabbedOldSelect = null;
	
	private int displayOption;
  private int pdisplayOption;

	private final Preferences preferences;
	private final String prefnzPrefix = this.getClass().getSimpleName()+".";

	private final Logger logger = Logger.getLogger(WorkbenchPanel.class);

	/**
	 * This is the default constructor
	 */
	public WorkbenchPanel(int displayOption) {
		super();
		this.preferences = Preferences.userNodeForPackage(getClass());
		this.displayOption = displayOption;
    this.pdisplayOption = displayOption;
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
    // set grid layout for the whole pane: tabbedWork, tabbedSelect and tabbedStatus
		GridBagConstraints consGridBagConstraints1 = new GridBagConstraints();

		this.setLayout(new GridBagLayout());
		consGridBagConstraints1.gridx = 0;
		consGridBagConstraints1.gridy = 0;
		consGridBagConstraints1.weightx = 1.0;
		consGridBagConstraints1.weighty = 1.0;
		consGridBagConstraints1.fill = GridBagConstraints.BOTH;

    // set icon for 'Sites' tab
    Icon icon = new ImageIcon(View.class.getResource("/resource/icon/16/094.png"));
    View.getSingleton().getSiteTreePanel().setIcon(icon);
    View.getSingleton().getSiteTreePanel().setName(Constant.messages.getString("sites.panel.title"));

    /*
     * Adds tabs to panels based on selected layout.
     */
		switch (displayOption) {
      case View.DISPLAY_OPTION_LEFT_FULL:
        this.add(getSplitHoriz(), consGridBagConstraints1);
        break;
      case View.DISPLAY_OPTION_TOP_FULL:
        this.add(getPaneStatus(), consGridBagConstraints1);
        break;
      case View.DISPLAY_OPTION_BOTTOM_FULL:
      default:
        this.add(getSplitVert(), consGridBagConstraints1);
        break;
		}

    /*
     * Correct the tabs position based on the currently selected layout: if Full Layout was invoked: Request/Response/Script Console/Quickstart/Break tabs.
     */ 
		switch (displayOption) {
      case View.DISPLAY_OPTION_TOP_FULL:
        // save the arrangements of tabs when going into 'Full Layout'
        if(pdisplayOption != View.DISPLAY_OPTION_TOP_FULL) {
          tabbedOldSelect = tabbedSelect;
          tabbedOldStatus = tabbedStatus;
          tabbedOldWork   = tabbedWork;
        }
        // Tabs in sequence: request, response, output, sites.
        getTabbedStatus().addTab(View.getSingleton().getRequestPanel().getName(), View.getSingleton().getRequestPanel().getIcon(), View.getSingleton().getRequestPanel(), false);
        getTabbedStatus().addTab(View.getSingleton().getResponsePanel().getName(), View.getSingleton().getResponsePanel().getIcon(), View.getSingleton().getResponsePanel(), false);
        getTabbedStatus().addTab(View.getSingleton().getOutputPanel().getName(), View.getSingleton().getOutputPanel().getIcon(), View.getSingleton().getOutputPanel(), false);
        getTabbedStatus().addTab(View.getSingleton().getSiteTreePanel().getName(), View.getSingleton().getSiteTreePanel().getIcon(), View.getSingleton().getSiteTreePanel(), false);
     
        // go over all tabs that extensions added and move them to tabbedStatus
        Iterator<Component> it1 = getTabbedWork().getTabList().iterator();
        Iterator<Component> it2 = getTabbedSelect().getTabList().iterator();
        while(it1.hasNext()) {
          Component c = it1.next();
          if(c instanceof AbstractPanel) {
            getTabbedStatus().addTab(c.getName(), ((AbstractPanel)c).getIcon(), c);
          }
        }
        while(it2.hasNext()) {
          Component c = it2.next();
          if(c instanceof AbstractPanel) {
            getTabbedStatus().addTab(c.getName(), ((AbstractPanel)c).getIcon(), c);
          }
        }

        break;
      case View.DISPLAY_OPTION_BOTTOM_FULL:
      case View.DISPLAY_OPTION_LEFT_FULL:
      default:
        // Tabs in sequence: request, response, output, sites.
        getTabbedWork().addTab(View.getSingleton().getRequestPanel().getName(), View.getSingleton().getRequestPanel().getIcon(), View.getSingleton().getRequestPanel(), false);
        getTabbedWork().addTab(View.getSingleton().getResponsePanel().getName(), View.getSingleton().getResponsePanel().getIcon(), View.getSingleton().getResponsePanel(), false);
        getTabbedStatus().addTab(View.getSingleton().getOutputPanel().getName(), View.getSingleton().getOutputPanel().getIcon(), View.getSingleton().getOutputPanel(), false);
        getTabbedSelect().addTab(View.getSingleton().getSiteTreePanel().getName(), View.getSingleton().getSiteTreePanel().getIcon(), View.getSingleton().getSiteTreePanel(), false);
      
        // parse the tabs correctly when previous display option was 'Full Layout'
        if(pdisplayOption == View.DISPLAY_OPTION_TOP_FULL) {
          Iterator<Component> i1 = getTabbedOldWork().getTabList().iterator();
          Iterator<Component> i2 = getTabbedOldSelect().getTabList().iterator();
          while(i1.hasNext()) {
            Component c = i1.next();
            if(c instanceof AbstractPanel) {
              getTabbedWork().addTab(c.getName(), ((AbstractPanel)c).getIcon(), c);
            }
          }
          while(i2.hasNext()) {
            Component c = i2.next();
            if(c instanceof AbstractPanel) {
              getTabbedSelect().addTab(c.getName(), ((AbstractPanel)c).getIcon(), c);
            }
          }
        }
    }

    // save previous display option
    this.pdisplayOption = this.displayOption;
	}

  /*
   * This method is called whenever we change the layout in preferences or in toolbar.
   */
	public void changeDisplayOption(int displayOption) {
		this.displayOption = displayOption;
		this.removeAll();
		splitVert = null;
		splitHoriz = null;
		splitFull = null;
		initialize();
		this.validate();
		this.repaint();
	}


	/**
	 * This method initializes splitVert
	 * (TOP/BOTTOM (History))
	 * 
	 * @return JSplitPane
	 */
	private JSplitPane getSplitVert() {
		if (splitVert == null) {
			splitVert = new JSplitPane();

			splitVert.setDividerLocation(restoreDividerLocation(DIVIDER_VERTICAL, 300));
			splitVert.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new DividerResizedListener(DIVIDER_VERTICAL));

			splitVert.setDividerSize(3);
			splitVert.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitVert.setResizeWeight(0.5D);

			switch (displayOption) {
			case View.DISPLAY_OPTION_LEFT_FULL:
				splitVert.setTopComponent(getPaneWork());
				break;
			case View.DISPLAY_OPTION_BOTTOM_FULL:
			default:
				splitVert.setTopComponent(getSplitHoriz());
				break;
			}
			splitVert.setBottomComponent(getPaneStatus());
			splitVert.setContinuousLayout(false);
			splitVert.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return splitVert;
	}

	/**
	 * This method initializes splitHoriz
	 * 
	 * Site Panel / Work
	 * 
	 * @return JSplitPane
	 */
	private JSplitPane getSplitHoriz() {
		if (splitHoriz == null) {
			splitHoriz = new JSplitPane();
			splitHoriz.setLeftComponent(getPaneSelect());
			switch (displayOption) {
			case View.DISPLAY_OPTION_LEFT_FULL:
				splitHoriz.setRightComponent(getSplitVert());
				break;
			case View.DISPLAY_OPTION_BOTTOM_FULL:
			default:
				splitHoriz.setRightComponent(getPaneWork());
				break;
			}

			splitHoriz.setDividerLocation(restoreDividerLocation(DIVIDER_HORIZONTAL, 300));
			splitHoriz.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new DividerResizedListener(DIVIDER_HORIZONTAL));

			splitHoriz.setDividerSize(3);
			splitHoriz.setResizeWeight(0.3D);
			splitHoriz.setContinuousLayout(false);
			splitHoriz.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return splitHoriz;
	}


	/**
	 * This method initializes paneStatus
	 * 
	 * @return JPanel
	 */
	private JPanel getPaneStatus() {
		if (paneStatus == null) {
			paneStatus = new JPanel();
			paneStatus.setLayout(new BorderLayout(0,0));
			paneStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			paneStatus.add(getTabbedStatus());
		}
		return paneStatus;
	}
	
	/**
	 * This method initializes paneSelect
	 * 
	 * @return JPanel
	 */
	private JPanel getPaneSelect() {
		if (paneSelect == null) {
			paneSelect = new JPanel();
			paneSelect.setLayout(new BorderLayout(0,0));
			paneSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			paneSelect.add(getTabbedSelect());
		}
		return paneSelect;
	}

	/**
	 * This method initializes paneWork, which is used for request/response/break/script console.
	 *
	 * @return JPanel
	 */
	private JPanel getPaneWork() {
		if (paneWork == null) {
			paneWork = new JPanel();
			paneWork.setLayout(new BorderLayout(0,0));
			paneWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			paneWork.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			paneWork.add(getTabbedWork());
		}
		return paneWork;
	}

	public void splitPaneWorkWithTabbedPanel(TabbedPanel tabbedPanel, int orientation) {
		getPaneWork().removeAll();

		JSplitPane split = new JSplitPane(orientation);
		split.setDividerSize(3);
		split.setResizeWeight(0.5D);
		split.setContinuousLayout(false);
		split.setDoubleBuffered(true);
		split.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		split.setRightComponent(tabbedPanel);
		split.setLeftComponent(getTabbedWork());

		getPaneWork().add(split);
		getPaneWork().validate();
	}
	
	public void removeSplitPaneWork() {
		getPaneWork().removeAll();
		getPaneWork().add(getTabbedWork());
		getPaneWork().validate();
	}

	/**
	 * This method initializes tabbedStatus
	 * 
	 * @return org.parosproxy.paros.view.ParosTabbedPane
	 */
	public TabbedPanel2 getTabbedStatus() {
		if (tabbedStatus == null) {
			tabbedStatus = new TabbedPanel2();
			tabbedStatus.setPreferredSize(new Dimension(800, 200));
			// ZAP: Move tabs to the top of the panel
			tabbedStatus.setTabPlacement(JTabbedPane.TOP);
			tabbedStatus.setName("tabbedStatus");
			tabbedStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return tabbedStatus;
	}

	public TabbedPanel2 getTabbedOldStatus() {
		if (tabbedOldStatus == null) {
			tabbedOldStatus = new TabbedPanel2();
			tabbedOldStatus.setPreferredSize(new Dimension(800, 200));
			// ZAP: Move tabs to the top of the panel
			tabbedOldStatus.setTabPlacement(JTabbedPane.TOP);
			tabbedOldStatus.setName("tabbedOldStatus");
			tabbedOldStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return tabbedOldStatus;
	}

	/**
	 * This method initializes tabbedWork
	 * 
	 * @return org.parosproxy.paros.view.ParosTabbedPane
	 */
	public TabbedPanel2 getTabbedWork() {
		if (tabbedWork == null) {
			tabbedWork = new TabbedPanel2();
			tabbedWork.setPreferredSize(new Dimension(600, 400));
			tabbedWork.setName("tabbedWork");
			tabbedWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return tabbedWork;
	}

	public TabbedPanel2 getTabbedOldWork() {
		if (tabbedOldWork == null) {
			tabbedOldWork = new TabbedPanel2();
			tabbedOldWork.setPreferredSize(new Dimension(600, 400));
			tabbedOldWork.setName("tabbedOldWork");
			tabbedOldWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return tabbedOldWork;
	}


  /*
   * Set the old tabbed panels called from ExtensionLoader.java and used with 'Full Layout'.
   */
  public void setTabbedOldWork(TabbedPanel2 t) {
    this.tabbedOldWork = t;
  }
  public void setTabbedOldStatus(TabbedPanel2 t) {
    this.tabbedOldStatus = t;
  }
  public void setTabbedOldSelect(TabbedPanel2 t) {
    this.tabbedOldSelect = t;
  }

	/**
	 * This method initializes tabbedSelect
	 * 
	 * @return org.parosproxy.paros.view.ParosTabbedPane
	 */
	public TabbedPanel2 getTabbedSelect() {
		if (tabbedSelect == null) {
			tabbedSelect = new TabbedPanel2();
			tabbedSelect.setPreferredSize(new Dimension(200, 400));
			tabbedSelect.setName("tabbedSelect");
			tabbedSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}

		return tabbedSelect;
	}

	public TabbedPanel2 getTabbedOldSelect() {
		if (tabbedOldSelect == null) {
			tabbedOldSelect = new TabbedPanel2();
			tabbedOldSelect.setPreferredSize(new Dimension(200, 400));
			tabbedOldSelect.setName("tabbedOldSelect");
			tabbedOldSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}

		return tabbedOldSelect;
	}
	
	/**
	 * @param prefix
	 * @param location
	 */
	private void saveDividerLocation(String prefix, int location) {
		if (location > 0) {
			if (logger.isDebugEnabled()) logger.debug("Saving preference " + prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION + "=" + location);
			this.preferences.put(prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION, Integer.toString(location));
			// immediate flushing
			try {
				this.preferences.flush();
			} catch (final BackingStoreException e) {
				logger.error("Error while saving the preferences", e);
			}
		}
	}
	
	/**
	 * @param prefix
	 * @param fallback
	 * @return the size of the frame OR fallback value, if there wasn't any preference.
	 */
	private int restoreDividerLocation(String prefix, int fallback) {
		int result = fallback;
		final String sizestr = preferences.get(prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION, null);
		if (sizestr != null) {
			int location = 0;
			try {
				location = Integer.parseInt(sizestr.trim());
			} catch (final Exception e) {
				// ignoring, cause is prevented by default values;
			}
			if (location > 0 ) {
				result = location;
				if (logger.isDebugEnabled()) logger.debug("Restoring preference " + prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION + "=" + location);
			}
		}
		return result;
	}
	
	/*
	 * ========================================================================
	 */
	
	private final class DividerResizedListener implements PropertyChangeListener {

		private final String prefix;
		
		public DividerResizedListener(String prefix) {
			super();
			assert prefix != null;
			this.prefix = prefix;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			JSplitPane component = (JSplitPane) evt.getSource();
			if (component != null) {
				if (logger.isDebugEnabled()) logger.debug(prefnzPrefix+prefix + "." + "location" + "=" + component.getDividerLocation());
				saveDividerLocation(prefix, component.getDividerLocation());
			}
		}
		
	}

}
