package org.zaproxy.zap.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.TabbedPanel;

/**
 * A tabbed panel that adds the option to hide individual tabs via a cross button on the tab.
 */
public class TabbedPanel2 extends TabbedPanel {

	private static final long serialVersionUID = 1L;
	
	private List<Component> fullTabList = new ArrayList<>();
	private List<Component> removedTabList = new ArrayList<>();

	private final Logger logger = Logger.getLogger(TabbedPanel2.class);

	public TabbedPanel2() {
		super();
		
		this.addChangeListener(new ChangeListener() {
		    @Override
		    public void stateChanged(ChangeEvent e) {
		    	setCloseButtonStates();
		    }
		});
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

	public void setVisible(Component c, boolean visible) {
		if (visible) {
			if (this.removedTabList.contains(c)) {
				
				if (c instanceof AbstractPanel) {
					// Dont use the addTab(AbstractPanel) methos as we need to force visibility
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
			remove(c);
			this.removedTabList.add(c);
		}
		c.setVisible(visible);
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

	public void removeTab(AbstractPanel panel) {
		this.remove(panel);
		this.fullTabList.remove(panel);
		this.removedTabList.remove(panel);
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
