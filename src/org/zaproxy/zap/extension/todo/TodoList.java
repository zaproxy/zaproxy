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

package org.zaproxy.zap.extension.todo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.parosproxy.paros.extension.AbstractPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zaproxy.zap.extension.tab.Tab;

public class TodoList extends AbstractPanel implements Tab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel;
	JScrollPane thePane;
	JScrollBar bar;
	JPanel userDefined;
	
	public TodoList(){
		this.setIcon(new ImageIcon(TodoList.class.getResource("/resource/icon/16/019.png")));
		this.setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		//TODO : add user defined tasks
		panel.add(new JLabel("From Owasp Testing guide : "));
		
		
		try {
			NodeList sections= parseXml();
			for(int i =0 ;i<sections.getLength();i++){
				Element section =  (Element) sections.item(i);
				String name  = section.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
				JLabel nodeLabel = new JLabel();
				nodeLabel.setText(i+1 +". "+name);
				panel.add(nodeLabel);
				
				Element tasks = (Element) section.getElementsByTagName("tasks").item(0);
				NodeList taskList = tasks.getElementsByTagName("task");
				
				for(int j=0;j<taskList.getLength();j++){
					String taskName  = taskList.item(j).getFirstChild().getNodeValue();
					ListItem item = new ListItem(taskName);
					Component handle = panel.add(item);
					item.handle = handle;
				}
			}
			
		} catch (Exception e) {
			System.err.println("Error occurred while parsing cheatsheet");
		}
		
		thePane = new JScrollPane(panel);
		bar = thePane.getVerticalScrollBar();
		this.add(thePane);
		
	}

	class ListItem extends JComponent{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JCheckBox checkbox;
		JLabel label;
		JButton removeBtn;
		Component handle;
		
		public ListItem(String label) {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			this.label = new JLabel();
			this.label.setText(label);
			
			this.checkbox = new JCheckBox();
			
			this.removeBtn = new JButton();
			removeBtn.setText("Remove");
			removeBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					panel.remove(handle);
				    panel.repaint();
				    crazyFix(); //clicking on remove btn does not immediately rerender the view.so use fix
				}
			});
			add(checkbox);
	        add(this.label);
	        add(removeBtn);
	        
		}
	}
	
	public NodeList parseXml() throws Exception{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		File xmlCheatSheet = new File((TodoList.class.getResource("/resource/OWASP_Web_Application_Testing_Cheat_Sheet.xml")).toURI());
		Document dom = db.parse(xmlCheatSheet);
		NodeList sections = dom.getElementsByTagName("section");
		return sections;
	}
	
	private void crazyFix(){
		//clicking on remove btn does not immediately rerender the view.so use fix
		//fix : scroll up and down programatically
	    bar.setValue(bar.getValue()-1);
	    bar.setValue(bar.getValue()+1);
	}
}
