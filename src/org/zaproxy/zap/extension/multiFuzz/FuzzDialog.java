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
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.Message;

public abstract class FuzzDialog<M extends Message, L extends FuzzLocation<M>, P extends Payload, G extends FuzzGap<M, L, P>> extends AbstractDialog {

	private static final long serialVersionUID = 3855005636913607013L;
	private static final Logger logger = Logger.getLogger(FuzzDialog.class);
	protected ExtensionFuzz res;
	protected M fuzzableMessage;

	protected ArrayList<G> gaps = new ArrayList<G>();
	private int currentIndex = 0;
	protected boolean adding = false;

	private JPanel background;
	private JSplitPane splitPane;
	private JLabel info;
	private JLabel target;
	private JPanel searchBar;
	private ColorLine colors = new ColorLine();
	private JFormattedTextField gapNrField;
	private DefaultComboBoxModel<String> payloadModel = null;
	private ComboMenuBar categoryField = null;

	private JButton cancelButton = null;
	private JButton startButton = null;
	private JButton prevButton;
	private JButton nextButton;
	private JButton addScriptButton;
	private JButton addFuzzerFileButton;
	private JButton addRegExPayloadButton;
	private JButton addSinglePayloadButton;
	private JButton addComponentButton;
	private JButton delComponentButton;
	private JTextArea searchField;
	private JTextArea payloadText;
	private JList<String> payloads;
	private ArrayList<FuzzerListener<?, ArrayList<G>>> listeners = new ArrayList<FuzzerListener<?,ArrayList<G>>>();
	
	public abstract FileFuzzer<P> convertToFileFuzzer(Fuzzer jBroFuzzer);
	public abstract FuzzProcessFactory getFuzzProcessFactory();
	protected abstract PayloadFactory<P> getPayloadFactory();
	public M getMessage(){
		return this.fuzzableMessage;
	}
	protected abstract int addCustomComponents(JPanel panel, int currentRow);
	protected abstract FuzzComponent<L, G> getMessageContent();
	
	/**
	 * 
	 * @param extension
	 * @param fuzzTarget
	 * @throws HeadlessException
	 */
	public FuzzDialog(ExtensionFuzz ext, L loc, M msg) throws HeadlessException {
		super(View.getSingleton().getMainFrame(), true);
		this.setTitle(Constant.messages.getString("fuzz.title"));
		this.res = ext;
		fuzzableMessage = msg;
		initialize();
		getAddFuzzAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		getMessageContent().markUp(loc);
		getAddFuzzAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		getDelComponentButton().setEnabled(false);
	}
	/**
	 * This method initializes the Dialog and its components
	 */
	protected void initialize() {
		this.setContentPane(getJPanel());
		this.setSize(800, 400);
	}
	/**
	 * This method initializes the main JPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel(){
		if(background == null){
			background = new JPanel();
			background.setLayout(new GridBagLayout());
			int currentRow = 0;
			Font headLine = new Font(Font.SERIF,Font.BOLD, 20);  
			JLabel headL = new JLabel(Constant.messages.getString("fuzz.title"));
			headL.setFont(headLine);
			GridBagConstraints h = getGBC(0, currentRow, 2, 1.0, 0.0, java.awt.GridBagConstraints.HORIZONTAL);
			h.anchor = java.awt.GridBagConstraints.PAGE_START;
			background.add(headL, h);
			currentRow++;
			GridBagConstraints i = getGBC(0, currentRow, 2, 1.0, 0.0, java.awt.GridBagConstraints.HORIZONTAL);
			i.anchor = java.awt.GridBagConstraints.PAGE_START;
			background.add(getInfo(), i);
			currentRow++;
			GridBagConstraints b = getGBC(0, currentRow, 2, 1.0, 1.0, java.awt.GridBagConstraints.BOTH);
			b.anchor = java.awt.GridBagConstraints.CENTER;
			background.add(getJTabbed(), b);
		}
		return background;
	}
	private JSplitPane getJTabbed() {
		if (splitPane == null) {

			Font headLine = new Font(Font.SERIF,Font.BOLD, 16); 
			splitPane = new JSplitPane();

			JPanel left = new JPanel();
			left.setLayout(new GridBagLayout());
			int currentRow = 0;
			left.add(getMessageContent().messageView(), getGBC(0, currentRow, 2, 1.0, 1.0, java.awt.GridBagConstraints.BOTH));
			currentRow++;
			left.add(getSearchBar(), getGBC(0,currentRow, 2, 1.0, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			currentRow++;
			left.add(getAddComponentButton(), getGBC(0, currentRow, 1, 0.5));
			left.add(getStartButton(), getGBC(1, currentRow, 1, 0.5));

			JPanel right = new JPanel();
			right.setLayout(new GridBagLayout());
			currentRow = 0;

			JLabel messageOpts = new JLabel(Constant.messages.getString("fuzz.message.options"));
			messageOpts.setFont(headLine);
			right.add(messageOpts, getGBC(0, currentRow, 6, 1));
			currentRow++;
			currentRow = addCustomComponents(right, currentRow);
			JLabel targetHead = new JLabel(Constant.messages.getString("fuzz.targetHead"));
			targetHead.setFont(headLine);
			right.add(targetHead, getGBC(0, currentRow, 6, 1));
			currentRow++;
			right.add(getColorline(), getGBC(0, currentRow, 6, 1.0, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			currentRow++;
			JLabel targets = new JLabel(Constant.messages.getString("fuzz.target"));
			right.add(targets, getGBC(0, currentRow, 1, 0.2));
			right.add(getTargetOrig(), getGBC(1, currentRow, 2, 0.6));
			right.add(getPrevButton(), getGBC(3, currentRow, 1, 0.066));
			right.add(getGapNrField(), getGBC(4, currentRow, 1, 0.068));
			right.add(getNextButton(), getGBC(5, currentRow, 1, 0.066));
			currentRow++;

			JLabel payloads = new JLabel(Constant.messages.getString("fuzz.payloads"));
			right.add(payloads, getGBC(0, currentRow, 1, 0.2));
			right.add(new JScrollPane(getPayloadField()), getGBC(1, currentRow, 4, 0.8, 1.0, java.awt.GridBagConstraints.BOTH));
			currentRow++;

			JLabel addSinglePayload = new JLabel(Constant.messages.getString("fuzz.add.singlePayload"));
			right.add(addSinglePayload, getGBC(0, currentRow, 1, 0.2));
			right.add(getPayloadText(), getGBC(1, currentRow, 2, 0.6, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			right.add(getAddSinglePayloadButton(), getGBC(3, currentRow, 3, 0.2));
			currentRow++;

			JLabel addRegExPayload = new JLabel(Constant.messages.getString("fuzz.add.regExPayload"));
			right.add(addRegExPayload, getGBC(0, currentRow, 1, 0.2));
			right.add(getAddRegExPayloadButton(), getGBC(3, currentRow, 3, 0.2));
			currentRow++;

			JLabel addFuzzerFile = new JLabel(Constant.messages.getString("fuzz.add.fuzzerFile"));
			right.add(addFuzzerFile, getGBC(0, currentRow, 1, 0.2));
			right.add(getCategoryField(), getGBC(1, currentRow, 2, 0.6, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			right.add(getAddFuzzerFileButton(), getGBC(3, currentRow, 3, 0.2));
			currentRow++;

			JLabel addFuzzScript = new JLabel(Constant.messages.getString("fuzz.add.fuzzScript"));
			right.add(addFuzzScript, getGBC(0, currentRow, 3, 0.6));
			right.add(getAddFuzzScriptButton(), getGBC(3, currentRow, 3, 0.2));
			currentRow++;

			right.add(getDelComponentButton(), getGBC(0, currentRow, 1, 0.2));
			right.add(getCancelButton(), getGBC(3, currentRow, 3, 0.2));


			Dimension minimumSize = new Dimension(50, 50);
			left.setMinimumSize(minimumSize);
			right.setMinimumSize(minimumSize);
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(left);
			splitPane.setRightComponent(right);
			splitPane.setDividerLocation(400);
		}
		return splitPane;
	}
	protected JLabel getInfo(){
		if(info == null){
			info = new JLabel(Constant.messages.getString("fuzz.info.gen"));
		}
		return info;
	}
	
	// Left Panel
	private JPanel getSearchBar(){
		if(searchBar == null){
			this.searchBar = new JPanel();
			searchBar.setLayout(new GridBagLayout());
			searchField = new JTextArea("Search");
			searchField.setEditable(true);
			searchBar.add(searchField, getGBC(0, 0, 4, 0.8, 1.0, java.awt.GridBagConstraints.BOTH));
			JButton search = new JButton();
			search.setAction(getSearchAction());
			searchBar.add(search, getGBC(5, 0, 1, 0.2));
		}
		return searchBar;
	}
	// Right Panel
	private Color getColor(int n) {
		float hue = (float) (n % 5) / 5;
		float sat = (float) Math.ceil((float)n/5)/2;
		float bright = (float) Math.ceil((float)n/5);
		return Color.getHSBColor(hue, sat, bright);
	}
	private ColorLine getColorline(){
		if (colors == null){
			colors = new ColorLine();
		}
		return colors;
	}
	private JLabel getTargetOrig() {
		if(target == null){
			target = new JLabel("Original Section");
		}
		return target;
	}
	private JFormattedTextField getGapNrField(){
		if(gapNrField == null){
			gapNrField = new JFormattedTextField(NumberFormat.getNumberInstance());
			gapNrField.setValue(new Integer(1));
			gapNrField.setColumns(2);
			gapNrField.addPropertyChangeListener("value", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					if(arg0.getSource() == gapNrField){
						setSelection(((Number)gapNrField.getValue()).intValue() - 1);
					}
				}
			});
		}
		return gapNrField;
	}
	private JList<String> getPayloadField() {
		if(payloads == null){
			payloadModel = new DefaultComboBoxModel<>();
			payloads = new JList<>();
			payloads.setModel(payloadModel);
		}
		return payloads;
	}
	private JTextArea getPayloadText() {
		if(payloadText == null){
			payloadText = new JTextArea("Single Payload");
		}
		return payloadText;
	}
	private ComboMenuBar getCategoryField() {
		if (categoryField == null) {
			ArrayList<JMenu> catMenus = new ArrayList<JMenu>();
			// Add File based fuzzers (fuzzdb)
			for (String category : res.getFileFuzzerCategories()) {
				JMenu cat = new JMenu(category);
				for(String fuzzer : res.getFileFuzzerNames(category)){
					cat.add( new JMenuItem(fuzzer) );
				}
				catMenus.add( cat );
			}

			// Add jbrofuzz fuzzers
			for (String category : res.getJBroFuzzCategories()) {
				JMenu cat = new JMenu(category);
				for(String fuzzer : res.getJBroFuzzFuzzerNames(category)){
					cat.add( new JMenuItem(fuzzer) );
				}
				catMenus.add( cat );
			}

			// Custom category
			JMenu cat = new JMenu(Constant.messages.getString("fuzz.category.custom"));
			for(String fuzzer : res.getCustomFileList()){
				cat.add( new JMenuItem(fuzzer) );
			}
			catMenus.add( cat );
			JMenu menu = ComboMenuBar.createMenu(res.getDefaultCategory());
		    for(JMenu c : catMenus){
		    	MenuScroll.setScrollerFor(c, 10, 125, 0, 0);
		    	menu.add( c );
		    }
		    MenuScroll.setScrollerFor(menu, 10, 125, 0, 0);
			categoryField = new ComboMenuBar(menu);
		}
		return categoryField;
	}

	protected GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return getGBC(x, y, width, weightx, 0.0, java.awt.GridBagConstraints.NONE);
	}
	protected GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new java.awt.Insets(1,5,1,5);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = fill;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		return gbc;
	}

	public void addFuzzerListener(FuzzerListener< ?, ArrayList<G>> listener){
		listeners.add(listener);
	}
	public void removeFuzzerListener(FuzzerListener< ? , ArrayList<G>> listener){
		listeners.remove(listener);
	}
	
	protected void setSelection(int index){
		currentIndex = (index + gaps.size()) % gaps.size();
		getGapNrField().setText(""+(currentIndex + 1));
		getTargetOrig().setText(gaps.get(currentIndex).orig());
		payloadModel.removeAllElements();
		for(String sig: gaps.get(currentIndex).getPayloadSignatures()){
			payloadModel.addElement(sig);
		}
		getMessageContent().highlight(gaps, gaps.get(currentIndex));
		getColorline().repaint();
	}	
	private boolean check(){
		if(gaps.size()<1){return false;}
		boolean chosen = true;
		for(G g : gaps){
			chosen &= ( g.getPayloads().size() >= 1 );
		}
		return chosen;
	}
	

	protected boolean isCustomCategory() {
		return Constant.messages.getString("fuzz.category.custom").equals(getCategoryField().getSelectedCategory());
	}
	protected boolean isJBroFuzzCategory() {
		return getCategoryField().getSelectedCategory().startsWith(ExtensionFuzz.JBROFUZZ_CATEGORY_PREFIX);
	}
	
	protected JButton getAddComponentButton(){
		if (addComponentButton == null) {
			addComponentButton = new JButton();
			addComponentButton.setAction(getAddFuzzAction());
		}
		addComponentButton.setEnabled(true);
		return addComponentButton;
	}
	protected JButton getDelComponentButton(){
		if (delComponentButton == null) {
			delComponentButton = new JButton();
			delComponentButton.setAction(getDelFuzzAction());
		}
		delComponentButton.setEnabled(true);
		return delComponentButton;
	}
	protected JButton getNextButton(){
		if (nextButton == null) {
			nextButton = new JButton();
			nextButton.setAction(new AbstractAction(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelection(currentIndex + 1);
					getMessageContent().highlight(gaps, gaps.get(currentIndex));
				}
			});
			nextButton.setText(">");
		}
		nextButton.setEnabled(true);
		return nextButton;
	}
	protected JButton getPrevButton(){
		if (prevButton == null) {
			prevButton = new JButton();
			prevButton.setAction(new AbstractAction(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelection(currentIndex - 1);
					getMessageContent().highlight(gaps, gaps.get(currentIndex));
				}
			});
			prevButton.setText("<");
		}
		prevButton.setEnabled(true);
		return prevButton;
	}
	protected JButton getAddFuzzScriptButton() {
		if (addScriptButton == null) {
			addScriptButton = new JButton();
			//addScriptButton.setAction(getAddScriptAction());
		}
		return addScriptButton;
	}
	protected JButton getAddFuzzerFileButton() {
		if (addFuzzerFileButton == null) {
			addFuzzerFileButton = new JButton();
			addFuzzerFileButton.setAction(getAddFuzzerFileAction());
		}
		return addFuzzerFileButton;
	}
	protected JButton getAddRegExPayloadButton() {
		if (addRegExPayloadButton == null) {
			addRegExPayloadButton = new JButton();
			//addRegExPayloadButton.setAction(getAddRegExPayloadAction());
		}
		return addRegExPayloadButton;
	}	
	protected JButton getAddSinglePayloadButton() {
		if (addSinglePayloadButton == null) {
			addSinglePayloadButton = new JButton();
			addSinglePayloadButton.setAction(getAddSinglePayloadAction());
		}
		return addSinglePayloadButton;
	}
	protected JButton getStartButton() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setAction(getStartFuzzAction());
		}
		return startButton;
	}
	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setAction(getCancelFuzzAction());
		}
		return cancelButton;
	}
	
	protected Action getSearchAction() {
		return new SearchAction();
	}
	protected AddSinglePayloadAction getAddSinglePayloadAction() {
		return new AddSinglePayloadAction();
	}
	protected Action getAddRegExPayloadAction() {
		// TODO Auto-generated method stub
		return null;
	}
	protected AddFuzzerFileAction getAddFuzzerFileAction() {
		return new AddFuzzerFileAction();
	}
	protected Action getAddScriptAction() {
		// TODO Auto-generated method stub
		return null;
	}
	protected AddFuzzAction getAddFuzzAction() {
		return new AddFuzzAction();
	}
	protected DelFuzzAction getDelFuzzAction() {
		return new DelFuzzAction();
	}
	protected StartFuzzAction getStartFuzzAction() {
		return new StartFuzzAction();
	}
	protected CancelFuzzAction getCancelFuzzAction() {
		return new CancelFuzzAction();
	}
	protected class AddSinglePayloadAction extends AbstractAction {
		public AddSinglePayloadAction() {
			super(Constant.messages.getString("fuzz.add.singlePayload"));
			setEnabled(true);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String input = getPayloadText().getText();
			if(input.startsWith("#<type=\"")){
				int i = input.indexOf("\">");
				if(i > 0){
					String type = input.substring(8, i);
					String data = input.substring(i+2);
					gaps.get(currentIndex).addPayload( getPayloadFactory().createPayload(type, data) );
				}
			}
			P newP = getPayloadFactory().createPayload(input);
			if(newP != null){
				gaps.get(currentIndex).addPayload( newP );
				payloadModel.addElement(input);
				getStartButton().setEnabled(check());
			}	
		}
	}
	protected class AddFuzzerFileAction extends AbstractAction {
		public AddFuzzerFileAction() {
			super(Constant.messages.getString("fuzz.add.fuzzerFile"));
			setEnabled(true);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String choice = getCategoryField().getSelectedItem();
			String cat = getCategoryField().getSelectedCategory();
			P pay = getPayloadFactory().createPayload("FILE", cat + " --> " + choice);
				gaps.get(currentIndex).addPayload( pay );
				payloadModel.addElement(pay.toString());
				getStartButton().setEnabled(check());
		}
	}
	
	protected class StartFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -961522394390805325L;

		public StartFuzzAction() {
			super(Constant.messages.getString("fuzz.button.start"));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for(FuzzerListener<?, ArrayList<G>> f : listeners){
				f.notifyFuzzerComplete(gaps);
			}
		}
	}
	protected class CancelFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -6716179197963523133L;

		public CancelFuzzAction() {
			super(Constant.messages.getString("fuzz.button.cancel"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}
	protected class AddFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -961522394390805325L;

		public AddFuzzAction() {
			super(Constant.messages.getString("fuzz.button.add.add"));
			setEnabled(true);
		}

		protected boolean isValidLocation(L l){
			boolean valid = true;
			for(G g : gaps){
				valid &= !l.overlap(g.getLocation());
			}
			return valid;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(adding){
					setSelection(gaps.size() - 1);
					getAddComponentButton().setText(Constant.messages.getString("fuzz.button.add.add"));
					getInfo().setText(Constant.messages.getString("fuzz.info.gen"));
					getStartButton().setEnabled(false);
					getDelComponentButton().setEnabled(true);
					adding = false;
			}
			else{
				getStartButton().setEnabled(false);
				info.setText(Constant.messages.getString("fuzz.info.add"));
				getAddComponentButton().setText(Constant.messages.getString("fuzz.button.add.done"));
				adding = true;
			}
		}
	}
	protected class DelFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -961522394390805325L;

		public DelFuzzAction() {
			super(Constant.messages.getString("fuzz.button.del"));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			gaps.remove(currentIndex);
			setSelection(currentIndex - 1);
			if(gaps.size() <= 1){
				getDelComponentButton().setEnabled(false);
			}
			getStartButton().setEnabled(check());
		}
	}
	private class SearchAction extends AbstractAction{
		public SearchAction(){
			super(Constant.messages.getString("fuzz.search"));
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			getMessageContent().search(searchField.getText());
		}
	}
	private static class ComboMenuBar extends JMenuBar {

		  JMenu menu;
		  String cat;
		  Dimension preferredSize;

		  public ComboMenuBar(JMenu menu) {
		    this.menu = menu;
		    MenuItemListener listener = new MenuItemListener();
		    setListener(menu, listener);
		    add(menu);
		    this.setMinimumSize(new Dimension(50,28));
		  }

		  class MenuItemListener implements ActionListener {
			  @Override
			  public void actionPerformed(ActionEvent e) {
		      JMenuItem item = (JMenuItem) e.getSource();
		      menu.setText(item.getText());
		      JPopupMenu popUp = ((JPopupMenu)item.getParent());
		      cat = ((JMenu)popUp.getInvoker()).getText();
		      menu.requestFocus();
		    }
		  }

		  private void setListener(JMenuItem item, ActionListener listener) {
		    if (item instanceof JMenu) {
		      JMenu menu = (JMenu) item;
		      int n = menu.getItemCount();
		      for (int i = 0; i < n; i++) {
		        setListener(menu.getItem(i), listener);
		      }
		    } else if (item != null) { // null means separator
		      item.addActionListener(listener);
		    }
		  }
		  public String getSelectedCategory(){
			  return cat;
		  }
		  public String getSelectedItem() {
		    return menu.getText();
		  }
		  @Override
		  public void setPreferredSize(Dimension size) {
		    preferredSize = size;
		  }
		  @Override
		  public Dimension getPreferredSize() {
		    if (preferredSize == null) {
		      Dimension sd = super.getPreferredSize();
		      Dimension menuD = getItemSize(menu);
		      Insets margin = menu.getMargin();
		      Dimension retD = new Dimension(menuD.width, margin.top
		          + margin.bottom + menuD.height);
		      menu.setPreferredSize(retD);
		      preferredSize = retD;
		    }
		    return preferredSize;
		  }

		  private Dimension getItemSize(JMenu menu) {
		    Dimension d = new Dimension(0, 0);
		    int n = menu.getItemCount();
		    for (int i = 0; i < n; i++) {
		      Dimension itemD;
		      JMenuItem item = menu.getItem(i);
		      if (item instanceof JMenu) {
		        itemD = getItemSize((JMenu) item);
		      } else if (item != null) {
		        itemD = item.getPreferredSize();
		      } else {
		        itemD = new Dimension(0, 0); // separator
		      }
		      d.width = Math.max(d.width, itemD.width);
		      d.height = Math.max(d.height, itemD.height);
		    }
		    return d;
		  }

		  private static class ComboMenu extends JMenu {

		    public ComboMenu(String label) {
		      super(label);
		      setBorder(new EtchedBorder());
		      setHorizontalTextPosition(JButton.LEFT);
		      setFocusPainted(true);
		    }
		  }

		  public static JMenu createMenu(String label) {
		    return new ComboMenu(label);
		  }

		}
	private class ColorLine extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			for(int i = 0; i < gaps.size(); i++){
				if(i == currentIndex){
					g.setColor(Color.black);
					g.fillRect(15*i, 0, 13, 13);
				}
				g.setColor(getColor(i+1));
				g.fillRect(15*i+1, 1, 10, 10);
			}
		}
	}
}