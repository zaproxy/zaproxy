/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Original code contributed by Stephen de Vries
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

package org.zaproxy.zap.extension.beanshell;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.AbstractFrame;
import org.parosproxy.paros.view.View;

import bsh.EvalError;
import bsh.Interpreter;


public class BeanShellConsoleFrame extends AbstractFrame {

	private static final long serialVersionUID = 1L;

	private BeanShellPanel beanShellPanel = null;
	private JPanel panelCommand = null;
	private JButton btnEvaluate = null;
	private JButton btnLoad = null;
	private JButton btnSave = null;
	private JButton btnSaveAs = null;
	private Extension extension = null;
	private Interpreter interpreter = null;
	private String scriptsDir = System.getProperty("user.dir") + "/scripts/";
	private File currentScriptFile = null;
	private ViewDelegate view = null;

	private JPanel jPanel = null;
	
	private static final Logger log = Logger.getLogger(BeanShellConsoleFrame.class);
	
   /**
    * @throws HeadlessException
    */
   public BeanShellConsoleFrame() throws HeadlessException {
       super();
       initialize();
       
   }

   /**
    * @param parent
    * @param modal
    * @param extension
    * @throws HeadlessException
    */
   public BeanShellConsoleFrame(Frame parent, boolean modal, Extension extension) throws HeadlessException {
       //super(parent, modal);
       super();
	   this.extension = extension;
       initialize();

   }

	/**
	 * This method initializes this
	 */
	private void initialize() {
	    getBeanShellPanel().getPanelOption().add(getPanelCommand(), "");
	    this.setContentPane(getJPanel());
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelCommand() {
		if (panelCommand == null) {
			panelCommand = new JPanel();
			panelCommand.setLayout(new FlowLayout());			
			panelCommand.add(getBtnLoad());
			panelCommand.add(getBtnSave());
			panelCommand.add(getBtnSaveAs());
			panelCommand.add(getBtnEvaluate());
		}
		return panelCommand;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnEvaluate() {
		if (btnEvaluate == null) {
			btnEvaluate = new JButton();
			btnEvaluate.setText(Constant.messages.getString("beanshell.button.evaluate"));			
			btnEvaluate.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) {
					beanShellEval(getBeanShellPanel().getTxtEditor().getText());
				}
			});
		}
		return btnEvaluate;
	}
	
	private void beanShellEval(String cmd) {
		try {	
			getInterpreter().eval(cmd);
		} catch (EvalError ex) {
			getInterpreter().error(ex.getMessage());
		}
	}
	
	private String loadScript(File file) throws IOException {
		StringBuilder temp = new StringBuilder();
		BufferedReader input = null;
		
		try {
			input = new BufferedReader( new FileReader(file) );
			String str;
			while ((str = input.readLine()) != null) {
				temp.append(str);
				temp.append(System.getProperty("line.separator"));
			}
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch(IOException e) {
				if (log.isDebugEnabled()) {
					log.debug(e.getMessage(), e);
				}
			}
		}
		
		return (temp.toString());
	}
	
	private void saveScript(String contents, File file) throws IOException {
		BufferedWriter output = null;
		
		try {
			output = new BufferedWriter( new FileWriter(file) );
			output.write( contents );
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				if (log.isDebugEnabled()) {
					log.debug(e.getMessage(), e);
				}
			}
		}
	
	}
	
	private JButton getBtnLoad() {
		if (btnLoad == null) {
			btnLoad = new JButton();
			btnLoad.setText(Constant.messages.getString("beanshell.button.load"));
			
			btnLoad.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) {
					if (getBeanShellPanel().isSaved() == false) {
						int confirm = view.showConfirmDialog(Constant.messages.getString("beanshell.dialog.unsaved"));
						if (confirm == JOptionPane.CANCEL_OPTION) return;
					}
					JFileChooser fc = new JFileChooser(scriptsDir);
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int result = fc.showOpenDialog(getBeanShellPanel());
					
					if(result == JFileChooser.APPROVE_OPTION) {
						try {
							String temp = loadScript(fc.getSelectedFile());
							getBeanShellPanel().getTxtEditor().setText(temp);
							getBeanShellPanel().getTxtEditor().discardAllEdits();
							getBeanShellPanel().setSaved(true);
							currentScriptFile = fc.getSelectedFile();
						} catch (IOException ex) {
							log.error(ex.getMessage(), ex);
							View.getSingleton().showWarningDialog(Constant.messages.getString("beanshell.error.message.loading.script"));
						}
						
					}
				}
			});
		}
		return btnLoad;
	}
	
	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setText(Constant.messages.getString("beanshell.button.save"));
			
			btnSave.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentScriptFile != null) {
						try {
							saveScript(getBeanShellPanel().getTxtEditor().getText(), currentScriptFile);
							getBeanShellPanel().setSaved(true);
						} catch (IOException ex) {
							log.error(ex.getMessage(), ex);
							View.getSingleton().showWarningDialog(Constant.messages.getString("beanshell.error.message.saving.script"));
						}
						
					} else {
						JFileChooser fc = new JFileChooser(scriptsDir);
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int result = fc.showSaveDialog(getBeanShellPanel());
						
						
						if (result == JFileChooser.APPROVE_OPTION) {
							try {
								saveScript(getBeanShellPanel().getTxtEditor().getText(), fc.getSelectedFile());
								getBeanShellPanel().setSaved(true);
								currentScriptFile = fc.getSelectedFile();
							} catch (IOException ex) {
								log.error(ex.getMessage(), ex);
								View.getSingleton().showWarningDialog(Constant.messages.getString("beanshell.error.message.saving.script"));
							}
						}
					}
				}
			});
		}
		return btnSave;
	}	
	
	private JButton getBtnSaveAs() {
		if (btnSaveAs == null) {
			btnSaveAs = new JButton();
			btnSaveAs.setText(Constant.messages.getString("beanshell.button.saveas"));
			
			btnSaveAs.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) {					
					JFileChooser fc = new JFileChooser(scriptsDir);
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int result = fc.showSaveDialog(getBeanShellPanel());										
					if (result == JFileChooser.APPROVE_OPTION) {
						try {
							saveScript(getBeanShellPanel().getTxtEditor().getText(), fc.getSelectedFile());
							getBeanShellPanel().setSaved(true);
							currentScriptFile = fc.getSelectedFile();
						} catch (IOException ex) {
							log.error(ex.getMessage(), ex);
							View.getSingleton().showWarningDialog(Constant.messages.getString("beanshell.error.message.saving.script"));
						}
					}
					
				}
			});
		}
		return btnSaveAs;
	}	
	

   public void setExtension(Extension extension) {
       this.extension = extension;
   }
   
   private Extension getExtension() {
       return extension;
   }
   
   @Override
   public void setVisible(boolean show) {    
       super.setVisible(show);       
   }
	
	private BeanShellPanel getBeanShellPanel() {
	   if (beanShellPanel == null) {
		   beanShellPanel = new BeanShellPanel();
	   }
	   return beanShellPanel;
	}
	
	public Interpreter getInterpreter() {
		if (interpreter == null) {
			interpreter = new Interpreter(getBeanShellPanel().getConsole());
		}
		return interpreter;
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    	
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gbc = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = java.awt.GridBagConstraints.BOTH;
			gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			HttpSender sender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true, HttpSender.BEAN_SHELL_INITIATOR);
			try {
				getInterpreter().set("model", Model.getSingleton());
				getInterpreter().set("sites", Model.getSingleton().getSession().getSiteTree());
				getInterpreter().set("sender", sender);
				
				getInterpreter().eval("setAccessibility(true)"); //This allows BeanShell users to access private members
				getInterpreter().eval("import org.apache.commons.httpclient.URI");
				getInterpreter().eval("import org.parosproxy.paros.network.*");
				getInterpreter().eval("import org.parosproxy.paros.model.*");
				getInterpreter().eval("import org.parosproxy.paros.db.*");
				getInterpreter().eval("import org.parosproxy.paros.model.*;");
			} catch (EvalError e) {
				log.error(e.getMessage(), e);
			}
			new Thread( getInterpreter() ).start();
			jPanel.add(getBeanShellPanel(), gbc);
		}
		return jPanel;
	}
	
    
	public void setView(ViewDelegate view) {
		this.view = view;
	}
}
