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
// ZAP: 2012/03/15 Removed the options to change the display of the ManualRequestEditorDialog,
// now they are changed dynamically.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/12/13 Added support for a new option 'show tab names'.
// ZAP: 2014/04/25 Issue 642: Add timestamps to Output tab(s)
// ZAP: 2014/10/09 Issue 1359: Options for splash screen
// ZAP: 2014/12/16 Issue 1466: Config option for 'large display' size
// ZAP: 2016/04/04 Do not require a restart to show/hide the tool bar
// ZAP: 2016/04/06 Fix layouts' issues

package org.parosproxy.paros.extension.option;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.extension.httppanel.view.largerequest.LargeRequestUtil;
import org.zaproxy.zap.extension.httppanel.view.largeresponse.LargeResponseUtil;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.TimeStampUtils;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

// ZAP: 2011: added more configuration options

public class OptionsViewPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	
	private static final String TIME_STAMP_FORMAT_COMBOBOX_TOOL_TIP = Constant.messages.getString("options.display.timestamp.format.combobox.tooltip");
	private static final String TIME_STAMP_FORMAT_DATETIME =  Constant.messages.getString("timestamp.format.datetime");
	private static final String TIME_STAMP_FORMAT_ISO8601 =  Constant.messages.getString("timestamp.format.iso8601");
	private static final String TIME_STAMP_FORMAT_TIMEONLY =  Constant.messages.getString("timestamp.format.timeonly");
	
	private JPanel panelMisc = null;
	
	private JCheckBox chkShowTabNames = null;
	private JCheckBox chkProcessImages = null;
	private JCheckBox chkShowMainToolbar = null;
	private JCheckBox chkAdvancedView = null;
	private JCheckBox chkAskOnExit = null;
	private JCheckBox chkWmUiHandling = null;
	private JCheckBox chkOutputTabTimeStamping = null; 
	private JCheckBox chkShowSplashScreen = null;
	private JCheckBox scaleImages = null;
	private JCheckBox showLocalConnectRequestsCheckbox;
	
	private JComboBox<String> brkPanelViewSelect = null;
	private JComboBox<String> displaySelect = null;
	private JComboBox<ResponsePanelPositionUI> responsePanelPositionComboBox;
	private JComboBox<String> timeStampsFormatSelect = null; 
	private JComboBox<String> fontName = null;
	
	private ZapNumberSpinner largeRequestSize = null;
	private ZapNumberSpinner largeResponseSize = null;
	private ZapNumberSpinner fontSize = null;
	
	private JLabel brkPanelViewLabel = null;
	private JLabel advancedViewLabel = null;
	private JLabel wmUiHandlingLabel = null;
	private JLabel askOnExitLabel = null;
	private JLabel displayLabel = null;
	private JLabel showMainToolbarLabel = null;
	private JLabel processImagesLabel = null;
	private JLabel showTabNamesLabel = null;
	private JLabel outputTabTimeStampLabel = null; 
	private JLabel outputTabTimeStampExampleLabel = null; 
	private JLabel showSplashScreenLabel = null;
	private JLabel largeRequestLabel = null;
	private JLabel largeResponseLabel = null;
	private JLabel fontExampleLabel = null;
	
    public OptionsViewPanel() {
        super();
 		initialize();
   }
    
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("view.options.title"));
        this.add(getPanelMisc(), getPanelMisc().getName());

	}
	
	/**
	 * This method initializes panelMisc	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelMisc() {
		if (panelMisc == null) {
			panelMisc = new JPanel();

			panelMisc.setLayout(new GridBagLayout());
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	panelMisc.setSize(114, 132);
		    }
			panelMisc.setName(Constant.messages.getString("view.options.misc.title"));

			displayLabel = new JLabel(Constant.messages.getString("view.options.label.display"));
			brkPanelViewLabel = new JLabel(Constant.messages.getString("view.options.label.brkPanelView"));
			advancedViewLabel = new JLabel(Constant.messages.getString("view.options.label.advancedview"));
			wmUiHandlingLabel = new JLabel(Constant.messages.getString("view.options.label.wmuihandler"));
			askOnExitLabel = new JLabel(Constant.messages.getString("view.options.label.askonexit"));
			showMainToolbarLabel = new JLabel(Constant.messages.getString("view.options.label.showMainToolbar"));
			processImagesLabel = new JLabel(Constant.messages.getString("view.options.label.processImages"));
			showTabNamesLabel = new JLabel(Constant.messages.getString("view.options.label.showTabNames"));
			outputTabTimeStampLabel = new JLabel(Constant.messages.getString("options.display.timestamp.format.outputtabtimestamps.label"));
			largeRequestLabel = new JLabel(Constant.messages.getString("view.options.label.largeRequestSize"));
			largeResponseLabel = new JLabel(Constant.messages.getString("view.options.label.largeResponseSize"));
					
			outputTabTimeStampExampleLabel = new JLabel(TimeStampUtils.currentDefaultFormattedTimeStamp());
			showSplashScreenLabel = new JLabel(Constant.messages.getString("view.options.label.showSplashScreen"));
			
			int row = 0;
			displayLabel.setLabelFor(getDisplaySelect());
			panelMisc.add(displayLabel, 
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getDisplaySelect(), 
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			JLabel responsePanelPositionLabel = new JLabel(Constant.messages.getString("view.options.label.responsepanelpos"));
			panelMisc.add(responsePanelPositionLabel, 
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getResponsePanelPositionComboBox(), 
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			brkPanelViewLabel.setLabelFor(getBrkPanelViewSelect());
			panelMisc.add(brkPanelViewLabel, 
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getBrkPanelViewSelect(), 
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			largeRequestLabel.setLabelFor(getLargeRequestSize());
			panelMisc.add(largeRequestLabel, 
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getLargeRequestSize(), 
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			largeResponseLabel.setLabelFor(getLargeResponseSize());
			panelMisc.add(largeResponseLabel, 
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getLargeResponseSize(), 
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			
			row++;
			advancedViewLabel.setLabelFor(getChkAdvancedView());
			panelMisc.add(advancedViewLabel, 
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getChkAdvancedView(), 
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			wmUiHandlingLabel.setLabelFor(getChkWmUiHandling());
			panelMisc.add(wmUiHandlingLabel,  
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getChkWmUiHandling(),  
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			askOnExitLabel.setLabelFor(getChkAskOnExit());
			panelMisc.add(askOnExitLabel,  
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getChkAskOnExit(),  
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			showMainToolbarLabel.setLabelFor(getChkShowMainToolbar());
			panelMisc.add(showMainToolbarLabel,  
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getChkShowMainToolbar(),  
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			processImagesLabel.setLabelFor(getChkProcessImages());
			panelMisc.add(processImagesLabel,  
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getChkProcessImages(),  
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			Insets insets = new Insets(2, 2, 2, 2);
			String labelText = Constant.messages.getString("view.options.label.showlocalconnectrequests");
			JLabel showConnectRequestLabel = new JLabel(labelText);
			showConnectRequestLabel.setLabelFor(getShowLocalConnectRequestsCheckbox());
			panelMisc.add(showConnectRequestLabel, LayoutHelper.getGBC(0, row, 1, 1.0D, insets));
			panelMisc.add(getShowLocalConnectRequestsCheckbox(), LayoutHelper.getGBC(1, row, 1, 1.0D, insets));

			row++;
			showTabNamesLabel.setLabelFor(getShowTabNames());
			panelMisc.add(showTabNamesLabel,  
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getShowTabNames(),  
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			showSplashScreenLabel.setLabelFor(getShowSplashScreen());
			panelMisc.add(showSplashScreenLabel,  
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getShowSplashScreen(),  
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			outputTabTimeStampLabel.setLabelFor(getChkOutputTabTimeStamps());
			panelMisc.add(outputTabTimeStampLabel,   
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getChkOutputTabTimeStamps(),   
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			outputTabTimeStampExampleLabel.setLabelFor(getTimeStampsFormatSelect());
			panelMisc.add(getTimeStampsFormatSelect(),   
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(outputTabTimeStampExampleLabel,   
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			JLabel fontNameLabel = new JLabel(Constant.messages.getString("view.options.label.fontName")); 
			fontNameLabel.setLabelFor(getFontName());
			panelMisc.add(fontNameLabel,   
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getFontName(),   
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));

			row++;
			JLabel fontSizeLabel = new JLabel(Constant.messages.getString("view.options.label.fontSize")); 
			fontSizeLabel.setLabelFor(getFontSize());
			panelMisc.add(fontSizeLabel,   
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getFontSize(),   
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			JLabel fontExampleLabel = new JLabel(Constant.messages.getString("view.options.label.fontExample")); 
			fontExampleLabel.setLabelFor(getFontExampleLabel());
			panelMisc.add(fontExampleLabel,   
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getFontExampleLabel(),   
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			JLabel scaleImagesLabel = new JLabel(Constant.messages.getString("view.options.label.scaleImages")); 
			fontExampleLabel.setLabelFor(getScaleImages());
			panelMisc.add(scaleImagesLabel,   
					LayoutHelper.getGBC(0, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			panelMisc.add(getScaleImages(),   
					LayoutHelper.getGBC(1, row, 1, 1.0D, new java.awt.Insets(2,2,2,2)));
			
			row++;
			panelMisc.add(new JLabel(""),   
					LayoutHelper.getGBC(0, row, 1, 1.0D, 1.0D));

		}
		return panelMisc;
	}

	private JCheckBox getShowTabNames() {
		if (chkShowTabNames == null) {
			chkShowTabNames = new JCheckBox();
			chkShowTabNames.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkShowTabNames.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkShowTabNames;
	}

	private JCheckBox getShowSplashScreen() {
		if (chkShowSplashScreen == null) {
			chkShowSplashScreen = new JCheckBox();
			chkShowSplashScreen.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkShowSplashScreen.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkShowSplashScreen;
	}

	private JCheckBox getChkProcessImages() {
		if (chkProcessImages == null) {
			chkProcessImages = new JCheckBox();
			chkProcessImages.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkProcessImages.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkProcessImages;
	}
	
	private JCheckBox getChkShowMainToolbar() {
		if (chkShowMainToolbar == null) {
			chkShowMainToolbar = new JCheckBox();
			chkShowMainToolbar.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkShowMainToolbar.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkShowMainToolbar;
	}

	private JCheckBox getChkWmUiHandling() {
		if (chkWmUiHandling == null) {
			chkWmUiHandling = new JCheckBox();
			chkWmUiHandling.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkWmUiHandling.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkWmUiHandling;
	}

	private JCheckBox getChkAskOnExit() {
		if (chkAskOnExit == null) {
			chkAskOnExit = new JCheckBox();
			chkAskOnExit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkAskOnExit.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkAskOnExit;
	}
	
	private JComboBox<String> getDisplaySelect() {
		if (displaySelect == null) {
			displaySelect = new JComboBox<>();
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.left"));
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.bottom"));
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.full"));
		}
		return displaySelect;
	}

	private JComboBox<ResponsePanelPositionUI> getResponsePanelPositionComboBox() {
		if (responsePanelPositionComboBox == null) {
			responsePanelPositionComboBox = new JComboBox<>();
			responsePanelPositionComboBox.addItem(
					new ResponsePanelPositionUI(
							Constant.messages.getString("view.options.label.responsepanelpos.tabs"),
							WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE));
			responsePanelPositionComboBox.addItem(
					new ResponsePanelPositionUI(
							Constant.messages.getString("view.options.label.responsepanelpos.sideBySide"),
							WorkbenchPanel.ResponsePanelPosition.PANELS_SIDE_BY_SIDE));
			responsePanelPositionComboBox.addItem(
					new ResponsePanelPositionUI(
							Constant.messages.getString("view.options.label.responsepanelpos.above"),
							WorkbenchPanel.ResponsePanelPosition.PANEL_ABOVE));
		}
		return responsePanelPositionComboBox;
	}
	
	private JComboBox<String> getBrkPanelViewSelect() {
		if (brkPanelViewSelect == null) {
			brkPanelViewSelect = new JComboBox<>();
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.toolbaronly"));
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.breakonly"));
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.both"));
		}
		return brkPanelViewSelect; 
	}
	
	private JCheckBox getChkAdvancedView() {
		if (chkAdvancedView == null) {
			chkAdvancedView = new JCheckBox();
			chkAdvancedView.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkAdvancedView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		
		return chkAdvancedView;
	}
	
	private JCheckBox getChkOutputTabTimeStamps() {
		if (chkOutputTabTimeStamping == null) {
			chkOutputTabTimeStamping = new JCheckBox();
			chkOutputTabTimeStamping.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkOutputTabTimeStamping.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			chkOutputTabTimeStamping.addItemListener(new java.awt.event.ItemListener(){
	        	@Override
	        	public void itemStateChanged(ItemEvent e) {
	        			timeStampsFormatSelect.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				};
			});
		}
		return chkOutputTabTimeStamping;
	}
	
	
	private JComboBox<String> getTimeStampsFormatSelect() {
		if (timeStampsFormatSelect == null) {
			String[] timeStampFormatStrings = {TIME_STAMP_FORMAT_DATETIME, TIME_STAMP_FORMAT_ISO8601, TIME_STAMP_FORMAT_TIMEONLY};
			timeStampsFormatSelect = new JComboBox<String>(timeStampFormatStrings);
			timeStampsFormatSelect.setToolTipText(TIME_STAMP_FORMAT_COMBOBOX_TOOL_TIP);
			timeStampsFormatSelect.setSelectedItem(getTimeStampsFormatSelect().getSelectedItem());
			timeStampsFormatSelect.setEditable(true);
			if (chkOutputTabTimeStamping.isSelected()) //The drop-down should only be enabled if time stamping is turned on
				timeStampsFormatSelect.setEnabled(true);
			else
				timeStampsFormatSelect.setEnabled(false);
			timeStampsFormatSelect.addActionListener(new java.awt.event.ActionListener() {
			
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String selectedDateFormat = (String)getTimeStampsFormatSelect().getSelectedItem();
                outputTabTimeStampExampleLabel.setText(TimeStampUtils.currentFormattedTimeStamp(selectedDateFormat)); 
            };
        });	
		}
		return timeStampsFormatSelect; 
	}
	
	private JCheckBox getShowLocalConnectRequestsCheckbox() {
		if (showLocalConnectRequestsCheckbox == null) {
			showLocalConnectRequestsCheckbox = new JCheckBox();
		}
		return showLocalConnectRequestsCheckbox;
	}

	private ZapNumberSpinner getLargeRequestSize() {
		if (largeRequestSize == null) {
			largeRequestSize = new ZapNumberSpinner(-1, LargeRequestUtil.DEFAULT_MIN_CONTENT_LENGTH, Integer.MAX_VALUE);
		}
		return largeRequestSize;
	}

	private ZapNumberSpinner getLargeResponseSize() {
		if (largeResponseSize == null) {
			largeResponseSize = new ZapNumberSpinner(-1, LargeResponseUtil.DEFAULT_MIN_CONTENT_LENGTH, Integer.MAX_VALUE);
		}
		return largeResponseSize;
	}
	
	private ZapNumberSpinner getFontSize() {
		if (fontSize == null) {
			fontSize = new ZapNumberSpinner(-1, 8, 100);
			if (! FontUtils.canChangeSize()) {
				fontSize.setEnabled(false);
			}
			fontSize.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					// Show what the default font will look like
					setExampleFont();
				}});
		}
		return fontSize;
	}
	
	private void setExampleFont() {
		String name;
		if (getFontName().getSelectedItem() == null) {
			name = "";
		} else {
			name = (String)getFontName().getSelectedItem();
		}
		Font font = FontUtils.getFont(name);
		int size = getFontSize().getValue();
		if (size == -1) {
			size = FontUtils.getSystemDefaultFont().getSize();
		}

		getFontExampleLabel().setFont(font.deriveFont((float)size));
		
	}
	
	@SuppressWarnings("unchecked")
	private JComboBox<String> getFontName() {
		if (fontName == null) {
			fontName = new JComboBox<String>();
			fontName.setRenderer(new JComboBoxFontRenderer());
			String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			fontName.addItem(" ");	// Default to system font
			for (String font : fonts) {
				fontName.addItem(font);
			}
			if (! FontUtils.canChangeSize()) {
				fontName.setEnabled(false);
			}
			fontName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Show what the default font will look like
					setExampleFont();
				}});
		}
		return fontName;
	}

	private JLabel getFontExampleLabel() {
		if (fontExampleLabel == null) {
			fontExampleLabel = new JLabel(Constant.messages.getString("view.options.label.exampleText"));
			fontExampleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		return fontExampleLabel;
	}
	
	private JCheckBox getScaleImages() {
		if (scaleImages == null) {
			scaleImages = new JCheckBox();
			if (! FontUtils.canChangeSize()) {
				scaleImages.setEnabled(false);
			}
		}
		return scaleImages;
	}
	
	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getShowTabNames().setSelected(options.getViewParam().getShowTabNames());
	    getShowSplashScreen().setSelected(options.getViewParam().isShowSplashScreen());
	    getChkProcessImages().setSelected(options.getViewParam().getProcessImages() > 0);
	    displaySelect.setSelectedIndex(options.getViewParam().getDisplayOption());
	    selectResponstPanelPosition(options.getViewParam().getResponsePanelPosition());
	    brkPanelViewSelect.setSelectedIndex(options.getViewParam().getBrkPanelViewOption());
	    getChkShowMainToolbar().setSelected(options.getViewParam().isShowMainToolbar());
	    chkAdvancedView.setSelected(options.getViewParam().getAdvancedViewOption() > 0);
	    chkAskOnExit.setSelected(options.getViewParam().getAskOnExitOption() > 0);
	    chkWmUiHandling.setSelected(options.getViewParam().getWmUiHandlingOption() > 0);
	    getChkOutputTabTimeStamps().setSelected(options.getViewParam().isOutputTabTimeStampingEnabled()); 
	    timeStampsFormatSelect.setSelectedItem(options.getViewParam().getOutputTabTimeStampsFormat());
	    getShowLocalConnectRequestsCheckbox().setSelected(options.getViewParam().isShowLocalConnectRequests());
	    largeRequestSize.setValue(options.getViewParam().getLargeRequestSize());
	    largeResponseSize.setValue(options.getViewParam().getLargeResponseSize());
	    getFontSize().setValue(options.getViewParam().getFontSize());
	    getFontName().setSelectedItem(options.getViewParam().getFontName());
	    getScaleImages().setSelected(options.getViewParam().isScaleImages());
	}
	
	private void selectResponstPanelPosition(String positionName) {
		for (int i = 0; i < getResponsePanelPositionComboBox().getItemCount(); i++) {
			ResponsePanelPositionUI item = getResponsePanelPositionComboBox().getItemAt(i);
			if (item.getPosition().name().equals(positionName)) {
				getResponsePanelPositionComboBox().setSelectedIndex(i);
				break;
			}
		}

		if (getResponsePanelPositionComboBox().getSelectedIndex() == -1) {
			getResponsePanelPositionComboBox().setSelectedIndex(0);
		}
	}

	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getViewParam().setShowTabNames(getShowTabNames().isSelected());
	    options.getViewParam().setShowSplashScreen(getShowSplashScreen().isSelected());
	    options.getViewParam().setProcessImages((getChkProcessImages().isSelected()) ? 1 : 0);
	    options.getViewParam().setDisplayOption(displaySelect.getSelectedIndex());
	    ResponsePanelPositionUI selectedItem = (ResponsePanelPositionUI) getResponsePanelPositionComboBox().getSelectedItem();
	    options.getViewParam().setResponsePanelPosition(selectedItem.getPosition().name());
	    options.getViewParam().setBrkPanelViewOption(brkPanelViewSelect.getSelectedIndex());
	    options.getViewParam().setShowMainToolbar(getChkShowMainToolbar().isSelected());
	    options.getViewParam().setAdvancedViewOption(getChkAdvancedView().isSelected() ? 1 : 0);
	    options.getViewParam().setAskOnExitOption(getChkAskOnExit().isSelected() ? 1 : 0);
	    options.getViewParam().setWmUiHandlingOption(getChkWmUiHandling().isSelected() ? 1 : 0);
	    options.getViewParam().setOutputTabTimeStampingEnabled(getChkOutputTabTimeStamps().isSelected()); 
	    options.getViewParam().setOutputTabTimeStampsFormat((String) getTimeStampsFormatSelect().getSelectedItem()); 
	    options.getViewParam().setShowLocalConnectRequests(getShowLocalConnectRequestsCheckbox().isSelected());
	    options.getViewParam().setLargeRequestSize(getLargeRequestSize().getValue());
	    options.getViewParam().setLargeResponseSize(getLargeResponseSize().getValue());
	    options.getViewParam().setFontSize(getFontSize().getValue());
	    options.getViewParam().setFontName((String)getFontName().getSelectedItem());
	    options.getViewParam().setScaleImages(getScaleImages().isSelected());
	}

	@Override
	public String getHelpIndex() {
		// ZAP: added help index
		return "ui.dialogs.options.view";
	}
	
	@SuppressWarnings("serial")
	private class JComboBoxFontRenderer extends BasicComboBoxRenderer {
	    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	    @Override
	    @SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
	        Font font = FontUtils.getFont((String)value);
	        if (font != null) {
	        	renderer.setFont(FontUtils.getFont((String)value));
	        } else {
	        	renderer.setFont(FontUtils.getFont(FontUtils.Size.standard));
	        }
	        return renderer;
	    }
	}

	private static class ResponsePanelPositionUI {

		private final String name;
		private final WorkbenchPanel.ResponsePanelPosition position;

		public ResponsePanelPositionUI(String name, WorkbenchPanel.ResponsePanelPosition position) {
			this.name = name;
			this.position = position;
		}

		public WorkbenchPanel.ResponsePanelPosition getPosition() {
			return position;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
