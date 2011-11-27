package org.zaproxy.zap.extension.highlighter;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.parosproxy.paros.extension.AbstractPanel;

/*
 * A panel which specifies all values of an HighlightEntry in a JPanel,
 * with UI elements to input and output of its content.
 */
public class HighlightEntryLineUi extends AbstractPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JTextField searchField;
	private JButton colorBox;
	private JCheckBox activeCheck;
	private HighlightSearchEntry highlight;
	
	public HighlightEntryLineUi(JPanel gridPanel, int lineNr, HighlightSearchEntry highlight) {
		createUserPanelLine(gridPanel, lineNr, highlight);
		this.highlight = highlight;
	}
	
	public HighlightSearchEntry getHighlightEntry() {
		return highlight;
	}
	
	public void save() {
		// Token
		highlight.setToken(searchField.getText());
		
		// Color
		highlight.setColor(colorBox.getBackground());
		
		// isActive
		highlight.setActive(activeCheck.isSelected());
		
	}
	private void createUserPanelLine(JPanel gridPanel, int lineNr, HighlightSearchEntry highlight) {
		GridBagConstraints c = new GridBagConstraints();
		
		// Contraints
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.gridy = lineNr;
		
		// 0: TextField
		c.gridx = 0;
		c.weightx = 1.0;
		searchField = new JTextField();
		searchField.setText( highlight.getToken() );
		gridPanel.add(searchField, c);
	
		// 1: X
		c.gridx = 1;
		c.weightx = 0.0;
		JButton buttonx = new JButton("X");
		gridPanel.add(buttonx, c);
		
		// 1: Color
		c.gridx = 2;
		c.weightx = 0.0;
		c.ipadx = 20;
		c.insets = new Insets(0,20,0,0);  //top padding
		colorBox = null;
		colorBox = new JButton(" ");
		colorBox.setBackground(highlight.getColor());
		colorBox.setActionCommand("Color");
		colorBox.addActionListener(this);
		gridPanel.add(colorBox, c);
		
		// 2: Checkbox
		c.gridx = 3;
		c.weightx = 0.0;
		c.ipadx = 20;
		JPanel showPanel = new JPanel();
		JLabel label = new JLabel("Active");
		activeCheck = new JCheckBox();
		activeCheck.setSelected(highlight.isActive());
		showPanel.add(activeCheck);
		showPanel.add(label);
		gridPanel.add(showPanel, c);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("Color")) {
			Color c = null;
			c =  JColorChooser.showDialog(
                    this,
                    "Choose Font Background Color",
                    highlight.getColor());
			colorBox.setBackground(c);
		}
		
	}
}
