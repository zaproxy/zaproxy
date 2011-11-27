package org.zaproxy.zap.extension.highlighter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.parosproxy.paros.extension.AbstractPanel;

/*
 * The main highlighter tab, used to configure highlights in the HighlightManager
 */
public class HighlighterPanel extends AbstractPanel implements ActionListener {
	private static final long serialVersionUID = -1085991554138327045L;
	private JPanel mainPanel;
	private JPanel userPanel;
	private JPanel buttonPanel;
	private HighlighterManager highlighter;
	private LinkedList<HighlightEntryLineUi> panelList;
	private HighlightEntryLineUi panelLineExtra;
	private HighlightSearchEntry extraHighlight;
	
	private static String BUTTON_APPLY = "Appy";
	
	public HighlighterPanel(ExtensionHighlighter extensionHighlighter) {
		init();
	}
	
	private void init() {
		highlighter = HighlighterManager.getInstance();
		panelList = new LinkedList<HighlightEntryLineUi>();

		initUi();
	}
		
	private void initUi() {
		// This
		this.setLayout(new BorderLayout());
		this.setName("Highlighter");
		
		// mainPanel
		mainPanel = new JPanel();
	    mainPanel.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    this.add(mainPanel, BorderLayout.CENTER);

	    // 0: button panel
	    initButtonPanel();
		c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.weightx = 1.0;
	    c.gridy = 0;
	    c.anchor = GridBagConstraints.PAGE_START;
	    mainPanel.add(buttonPanel, c);
	    
	    // 1: userPanel
		userPanel = new JPanel(new GridBagLayout());
		reinit();
	    c.gridy = 1;
	    mainPanel.add(userPanel, c);
	    
	    // 2: finishpanel
	    JPanel finishPanel = new JPanel();
	    c.weighty = 1.0;
	    c.gridy = 2;
	    mainPanel.add(finishPanel, c);
	}
	
	private void initButtonPanel() {
		JButton button = null;
		buttonPanel = new JPanel();
		button = new JButton ("Apply");
		button.setActionCommand(BUTTON_APPLY);
		button.addActionListener(this);
		buttonPanel.add(button);
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
	}
	
	private void reinit() {
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.weightx = 1.0;
	    c.gridy = 0;
	    c.anchor = GridBagConstraints.PAGE_START;
		
		userPanel.removeAll();
		userPanel.add(initUserPanel(), c);
		this.invalidate();
	}
	
	private JPanel initUserPanel() {
		JPanel userGridPanel = new JPanel();
		userGridPanel.setLayout(new GridBagLayout());
		userGridPanel.setBorder(BorderFactory.createEtchedBorder());

		// line 0: Title
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 1;      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.gridy = 0;
		c.gridx = 0;
		userGridPanel.add(new JLabel("Highlighted strings:"), c);

		// Line >0: Content
		int n=1;
		LinkedList<HighlightSearchEntry> newEntrys = highlighter.getHighlights();
		panelList = new LinkedList<HighlightEntryLineUi>();
		
		for(HighlightSearchEntry entry: newEntrys) {
			HighlightEntryLineUi panelLine = new HighlightEntryLineUi(userGridPanel, n++, entry);
			panelList.add(panelLine);			
		}
		
		extraHighlight = new HighlightSearchEntry();
		panelLineExtra = new HighlightEntryLineUi(userGridPanel, n+1, extraHighlight);
		
		return userGridPanel;
	}
	
	private void applyAll() {
		LinkedList<HighlightSearchEntry> entrys = new LinkedList<HighlightSearchEntry>();
		
		// Save all UI elements
		for(HighlightEntryLineUi panelLine: panelList) {
			panelLine.save();
			HighlightSearchEntry entry = panelLine.getHighlightEntry();
			if (entry.getToken().length() > 0) {
				entrys.add(entry);
			}
		}
		
		// The new line
		panelLineExtra.save();
		if (extraHighlight.getToken().length() > 0) {
			entrys.add(panelLineExtra.getHighlightEntry());
		}
		
		// Store them in the highlight manager
		highlighter.reinitHighlights(entrys);
		
		highlighter.writeConfigFile();
		reinit();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals(BUTTON_APPLY)) {
			applyAll();
		}
	}
}
