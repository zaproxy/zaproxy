package org.zaproxy.zap.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.TabbedPanel;

/**
 * A tabbed panel that adds the option to hide individual tabs via a cross button on the tab.
 */
public class TabbedPanel2 extends TabbedPanel {

	private static final long serialVersionUID = 1L;
	
	private static final Icon CLOSE_TAB_GREY_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/cross-small-grey.png"));
	private static final Icon CLOSE_TAB_RED_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/cross-small-red.png"));

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
        	if (tabCom != null && tabCom instanceof JPanel) {
        		JPanel jp = (JPanel) tabCom;
        		if (jp.getComponentCount() > 1) {
            		Component c = ((JPanel)tabCom).getComponent(1);
        			if (c != null && c instanceof JButton) {
        				((JButton)c).setEnabled(i == getSelectedIndex());
        				((JButton)c).setVisible(i == getSelectedIndex());
        			}
        		}
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
	
	private boolean isTabVisible(Component c) {
		return Model.getSingleton().getOptionsParam().getConfig().getBoolean(
				OptionsParamView.TAB_OPTION + "." + safeName(c.getName()), true);

	}
	
	private void saveTabState(Component c, boolean visible) {
		Model.getSingleton().getOptionsParam().getConfig().setProperty(
				OptionsParamView.TAB_OPTION + "." + safeName(c.getName()), visible);
		try {
			Model.getSingleton().getOptionsParam().getConfig().save();
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void addTab(String title, Icon icon, final Component c) {
		boolean hideable = false;
		if (c instanceof AbstractPanel) {
			hideable = ((AbstractPanel)c).isHideable();
		}
		this.addTab(title, icon, c, hideable);
	}
	
	public void setVisible(Component c, boolean visible) {
		if (visible) {
			this.saveTabState(c, true);

			if (this.removedTabList.contains(c)) {
				// Work out the index to add it back in
				int index = this.fullTabList.indexOf(c);
				while (index >= 0) {
					if (index > 0 && ! this.removedTabList.contains(this.fullTabList.get(index -1))) {
						// Found the first preceding tab that isnt hidden
						break;
					}
					index--;
				}
				
				if (c instanceof AbstractPanel) {
					AbstractPanel panel = (AbstractPanel)c;
					this.addTab(c.getName(), panel.getIcon(), panel, true, index+1);
				} else {
					this.addTab(c.getName(), null, c, true, index);
				}
				c.setVisible(true);
				this.removedTabList.remove(c);
			}
			
		} else {
			remove(c);
			this.removedTabList.add(c);
			this.saveTabState(c, false);
		}
	}

	public void addTab(String title, Icon icon, final Component c, boolean hideable) {
		this.addTab(title, icon, c, hideable, this.getTabCount());
	}

	public void addTab(String title, Icon icon, final Component c, boolean hideable, int index) {
        String origName = title;
		if (c instanceof AbstractPanel) {
			((AbstractPanel)c).setParent(this);
			((AbstractPanel)c).setTabIndex(index);
			((AbstractPanel)c).setHideable(hideable);
		}

		if (index > this.getTabCount()) {
			index = this.getTabCount();
		}

		super.insertTab(title, icon, c, c.getName(), index);

		if ( ! this.fullTabList.contains(c)) {
			this.fullTabList.add(c);
		}
		
		int pos = this.indexOfComponent(c);

		// Create a FlowLayout so that the buttons are not too large
		FlowLayout f = new FlowLayout(FlowLayout.CENTER, 0, 0);

		// Make a small JPanel with the layout and make it non-opaque
		JPanel pnlTab = new JPanel(f);
		pnlTab.setOpaque(false);

        // change the title variable if 'Options - Display - show tab names' selected
		if (!Model.getSingleton().getOptionsParam().getViewParam().getShowTabNames()) {
            title = "";
        }
		if (c.getName() == null) {
			c.setName(title);
		}

		// Add a JLabel with title and the left-side tab icon
		JLabel lblTitle = new JLabel(title);
		lblTitle.setIcon(icon);

		// Create a JButton for the close tab button
		JButton btnClose = new JButton();
		btnClose.setOpaque(false);

		// Configure icon and rollover icon for button
		btnClose.setRolloverIcon(CLOSE_TAB_RED_ICON);
		btnClose.setRolloverEnabled(true);
		btnClose.setToolTipText(Constant.messages.getString("all.button.close"));
		btnClose.setIcon(CLOSE_TAB_GREY_ICON);
		// Set border null so the button doesn't make the tab too big
		btnClose.setBorder(null);

		// Make sure the button can't get focus, otherwise it looks funny
		btnClose.setFocusable(false);
		
		// All close buttons start off hidden and disabled - they are enabled when the tab is selected
		btnClose.setEnabled(false);
		btnClose.setVisible(false);

		// Put the panel together
		pnlTab.add(lblTitle);
		
		if (hideable) {
			// Only include the close button is the tab is hideable
			pnlTab.add(btnClose);
		}

		// Now assign the component for the tab
		this.setTabComponentAt(pos, pnlTab);

		// Add the listener that removes the tab
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// The component parameter must be declared "final" so that it
				// can be
				// referenced in the anonymous listener class like this.
				setVisible(c, false);
			}
		};
		btnClose.addActionListener(listener);

		if (! this.isTabVisible(c)) {
			// Only add back visible tabs
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


}
