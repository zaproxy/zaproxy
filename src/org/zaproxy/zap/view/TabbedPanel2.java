package org.zaproxy.zap.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.TabbedPanel;
import org.zaproxy.zap.utils.DisplayUtils;

/**
 * A tabbed panel that adds the option to hide individual tabs via a cross button on the tab.
 */
public class TabbedPanel2 extends TabbedPanel {

	private static final long serialVersionUID = 1L;
	
	private List<Component> fullTabList = new ArrayList<>();
	private List<Component> removedTabList = new ArrayList<>();

	private static final Icon PLUS_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/plus.png"));

	// A fake component that never actually get displayed - used for the 'hidden tab list tab'
	private Component hiddenComponent = new JLabel();

	private final Logger logger = Logger.getLogger(TabbedPanel2.class);
	
	private int prevTabIndex = -1;

	public TabbedPanel2() {
		super();
		
		this.addChangeListener(new ChangeListener() {
		    @Override
		    public void stateChanged(ChangeEvent e) {
		    	setCloseButtonStates();
		    	if (getSelectedComponent() != null && getSelectedComponent().equals(hiddenComponent)) {
		    		// The 'hidden tab list tab' has been selected - this is a special case
		    		if (prevTabIndex == indexOfComponent(hiddenComponent)) {
		    			// Happens when we delete the tab to the left of the hidden one
		    			setSelectedIndex(prevTabIndex-1);
		    		} else {
		    			// Hidden tab list tab selected - show popup and select previous tab
		    			setSelectedIndex(prevTabIndex);
		    			showHiddenTabPopup();
		    		}
		    	} else {
		    		prevTabIndex = getSelectedIndex();
		    	}
		    }
		});
	}

	/**
	 * Show a popup containing a list of all of the hidden tabs - selecting one will reveal that tab
	 */
	private void showHiddenTabPopup() {
		JPopupMenu menu = new JPopupMenu();
		if (getMousePosition() == null) {
			// Startup
			return;
		}
		// Sort the list so the tabs are always in alphabetic order
		Collections.sort(this.removedTabList, new Comparator<Component>(){
			@Override
			public int compare(Component o1, Component o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		for (Component c : this.removedTabList) {
			if (c instanceof AbstractPanel) {
				final AbstractPanel ap = (AbstractPanel)c;
				JMenuItem mi = new JMenuItem(ap.getName());
				mi.setIcon(ap.getIcon());
				mi.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(ap, true);
						ap.setTabFocus();
					}});
				menu.add(mi);
			}
		}
		menu.show(this, this.getMousePosition().x, this.getMousePosition().y);
	}
	
	/**
     * Returns a clone of the TabbedPanel2 object.
     * @param tabbedPabel
   	 */
	public TabbedPanel2 clone(TabbedPanel2 tabbedPanel) {
		TabbedPanel2 t = new TabbedPanel2();
		t.fullTabList = tabbedPanel.fullTabList;
		t.removedTabList = tabbedPanel.removedTabList;
		return t;
	}
	
	private void setCloseButtonStates() {
		// Hide all 'close' buttons except for the selected tab
        for (int i = 0; i < this.getTabCount(); i++) {
        	Component tabCom = this.getTabComponentAt(i);
        	if (tabCom != null && tabCom instanceof TabbedPanelTab) {
        		TabbedPanelTab jp = (TabbedPanelTab) tabCom;
        		jp.setEnabled(i == getSelectedIndex());
        	}
        }
	}
	
	public void pinVisibleTabs() {
        for (int i = 0; i < this.getTabCount(); i++) {
        	Component tabCom = this.getTabComponentAt(i);
        	if (tabCom != null && tabCom instanceof TabbedPanelTab && tabCom.isVisible()) {
        		TabbedPanelTab jp = (TabbedPanelTab) tabCom;
        		jp.setPinned(true);
        		this.saveTabState(jp.getAbstractPanel());
        	}
        }
	}

	public void unpinTabs() {
        for (int i = 0; i < this.getTabCount(); i++) {
        	Component tabCom = this.getTabComponentAt(i);
        	if (tabCom != null && tabCom instanceof TabbedPanelTab && tabCom.isVisible()) {
        		TabbedPanelTab jp = (TabbedPanelTab) tabCom;
        		jp.setPinned(false);
        		this.saveTabState(jp.getAbstractPanel());
        	}
        }
	}

	/**
	 * Returns a name save to be used in the XML config file
	 * @param str
	 * @return
	 */
	private String safeName(String str) {
		return str.replaceAll("[^A-Za-z0-9]", "");

	}

	private boolean isTabPinned(Component c) {
		boolean showByDefault = false;
		if (c instanceof AbstractPanel) {
			showByDefault = ((AbstractPanel)c).isShowByDefault();
		}
		return Model.getSingleton().getOptionsParam().getConfig().getBoolean(
				OptionsParamView.TAB_PIN_OPTION + "." + safeName(c.getName()), showByDefault);

	}
	
	protected void saveTabState(AbstractPanel ap) {
		if (ap == null) {
			return;
		}
		Model.getSingleton().getOptionsParam().getConfig().setProperty(
				OptionsParamView.TAB_PIN_OPTION + "." + safeName(ap.getName()), ap.isPinned());
		try {
			Model.getSingleton().getOptionsParam().getConfig().save();
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*
	 * Returns true if the specified component is a visible tab panel (typically an AbstractPanel)
	 */
	public boolean isTabVisible(Component c) {
		if (! this.fullTabList.contains(c)) {
			// Not a known tab
			return false;
		}
		return ! this.removedTabList.contains(c);
	}

	public void setVisible(Component c, boolean visible) {
		if (visible) {
			if (this.removedTabList.contains(c)) {
				
				if (c instanceof AbstractPanel) {
					// Dont use the addTab(AbstractPanel) methods as we need to force visibility
					AbstractPanel panel = (AbstractPanel)c;
					this.addTab(c.getName(), panel.getIcon(), panel, true, true, panel.getTabIndex());
				} else {
					// Work out the index to add it back in
					int index = this.fullTabList.indexOf(c);
					while (index >= 0) {
						if (index > 0 && ! this.removedTabList.contains(this.fullTabList.get(index -1))) {
							// Found the first preceding tab that isnt hidden
							break;
						}
						index--;
					}
					
					this.addTab(c.getName(), null, c, true, true, index);
				}
				this.removedTabList.remove(c);
			}
			
		} else {
			if (! this.removedTabList.contains(c)) {
				remove(c);
				this.removedTabList.add(c);
			}
		}
		handleHiddenTabListTab();
	}

	@Override
	public void addTab(String title, Icon icon, final Component c) {
		if (c instanceof AbstractPanel) {
			this.addTab((AbstractPanel)c);
		} else {
			this.addTab(title, icon, c, false, true, this.getTabCount());
		}
	}

	public void addTab(AbstractPanel panel) {
		boolean visible = ! panel.isHideable() || this.isTabPinned(panel);
		this.addTab(panel.getName(), panel.getIcon(), panel, panel.isHideable(), visible, panel.getTabIndex());
		
	}

	public void addTab(String title, Icon icon, final Component c, boolean hideable, boolean visible, int index) {
		if (c instanceof AbstractPanel) {
			((AbstractPanel)c).setParent(this);
			((AbstractPanel)c).setTabIndex(index);
			((AbstractPanel)c).setHideable(hideable);
		}

		if (index == -1 || index > this.getTabCount()) {
			index = this.getTabCount();
		}
		if (icon instanceof ImageIcon) {
			icon = DisplayUtils.getScaledIcon((ImageIcon)icon);
		}

		super.insertTab(title, icon, c, c.getName(), index);

		if ( ! this.fullTabList.contains(c)) {
			this.fullTabList.add(c);
		}
		
		int pos = this.indexOfComponent(c);
		// Now assign the component for the tab
		
		this.setTabComponentAt(pos, new TabbedPanelTab(this, title, icon, c, hideable, this.isTabPinned(c)));

		if (! visible) {
			setVisible(c, false);
		}

		handleHiddenTabListTab();
	}

	private void handleHiddenTabListTab() {
		if (indexOfComponent(hiddenComponent) >= 0) {
			// Tab is showing, remove it - it might not be needed or may no longer be at the end
			super.remove(hiddenComponent);
		}
		if (this.removedTabList.size() > 0) {
			// Only re-add tab if there are hidden ones
			super.addTab("", PLUS_ICON, hiddenComponent);
		}
	}
	
	/**
	 * Temporarily lock/unlock the specified tab, eg if its active and mustnt be closed.
	 * Locked (AbstractPanel) tabs will not have the pin/close tab buttons displayed 
	 * @param panel
	 * @param hideable
	 */
	public void setTabLocked(AbstractPanel panel, boolean lock) {
        for (int i = 0; i < this.getTabCount(); i++) {
        	Component tabCom = this.getTabComponentAt(i);
        	if (tabCom != null && tabCom instanceof TabbedPanelTab && tabCom.isVisible()) {
        		TabbedPanelTab jp = (TabbedPanelTab) tabCom;
        		if (panel.equals(jp.getAbstractPanel())) {
            		jp.setLocked(!lock);
        		}
        	}
        }
	}
	
    @Override
    public void setIconAt(int index, Icon icon) {
    	Component tabCom = this.getTabComponentAt(index);
    	if (tabCom != null && tabCom instanceof JPanel) {
    		Component c = ((JPanel)tabCom).getComponent(0);
			if (c != null && c instanceof JLabel) {
				((JLabel)c).setIcon(icon);
			}
    	}
    }
	
    /**
     * Set the title of the tab when hiding/showing tab names.
     */
    @Override
    public void setTitleAt(int index, String title) {
    	Component tabCom = this.getTabComponentAt(index);
    	if (tabCom != null && tabCom instanceof JPanel) {
    		Component c = ((JPanel)tabCom).getComponent(0);
			if (c != null && c instanceof JLabel) {
				((JLabel)c).setText(title);
			}
    	}
      else {
        super.setTitleAt(index, title);
      }
    }

	public List<Component> getTabList() {
		return Collections.unmodifiableList(this.fullTabList);
	}

	public List<Component> getSortedTabList() {
		List<Component> copy = new ArrayList<Component>(this.fullTabList); 
		Collections.sort(copy, new Comparator<Component>(){
			@Override
			public int compare(Component o1, Component o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		return copy;
	}

	public void removeTab(AbstractPanel panel) {
		this.remove(panel);
		this.fullTabList.remove(panel);
		if (this.removedTabList.remove(panel)) {
			handleHiddenTabListTab();
		}
	}

  /**
   * Toggle tab names to enable/disable tab name: used with Tools - Options - Display -
   * "Show tab names". 
   */
  public void setShowTabNames(boolean showTabNames) {
        for (int i = 0; i < getTabCount(); i++) {
            String title = showTabNames ? getComponentAt(i).getName() : "";
            setTitleAt(i, title);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to call the method {@code AbstractPanel#tabSelected()} on the currently selected {@code AbstractPanel}, if
     * any.
     * 
     * @see AbstractPanel#tabSelected()
     */
    @Override
    protected void fireStateChanged() {
        super.fireStateChanged();

        Component comp = getSelectedComponent();
        if (comp instanceof AbstractPanel) {
            ((AbstractPanel) comp).tabSelected();
        }
    }
    
    /**
     * Returns true if the tab is 'active' - ie is being used for anything. 
     * This method always returns false so must be overriden to be changed
     * 
     * @return
     */
    public boolean isActive() {
    	return false;
    }
}
