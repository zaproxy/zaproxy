package org.zaproxy.zap.extension.history;

import java.io.*;
import java.util.*;
import java.lang.*;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;

public class FieldEnumeration extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FieldEnumeration.class);	
	
//	private final ExtensionHistory extension;
        private JPanel jPanel = new JPanel();
	private JLabel printURL = new JLabel();	
	private JButton buttonOK = new JButton("SUBMIT");
	private ButtonGroup group = new ButtonGroup();
	
	private HistoryReference historyRef;
	
	/*public static void main(String args[]) {
		new FieldEnumeration();
	}*/
	public FieldEnumeration() throws HeadlessException {
		super();
		initialize();
	}
	
	public FieldEnumeration(Frame arg0, boolean arg1) throws HeadlessException {
      		super(arg0, arg1);
        	initialize();
       }

	
	private void initialize() {
//		this.setSize(600, 600);
        this.setTitle(Constant.messages.getString("history.field.popup"));
		this.setContentPane(getJPanel());
		
		/*Map<String, String> params = Model.getSingleton().getSession().getParams(historyRef, HtmlParameter.Type.Form);
		for(Map.Entry<String, String> entry : params.entrySet()){
			System.out.println(entry.getKey()+" :: "+entry.getValue());
		}*/
		
		if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
                	this.setSize(400, 400);
	        }
        	this.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                	public void windowOpened(java.awt.event.WindowEvent e) {
                	}

                @Override
                	public void windowClosing(java.awt.event.WindowEvent e) {
                	}
        	});

                pack();
        }

//		setVisible(true);

	 private JPanel getJPanel() {
		jPanel.add(new JLabel("URL Form:"));
		jPanel.add(printURL);
		jPanel.add(buttonOK);

		buttonOK.addActionListener(new ActionListener() {
		@Override

			public void actionPerformed(ActionEvent event) {
			for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
            			AbstractButton button = buttons.nextElement();

            			if (button.isSelected()) {
					JOptionPane.showMessageDialog(FieldEnumeration.this,"You selected: " + button.getText());
             			   	System.out.println("button selected: " + button.getText());
           			 }
       			}
			}
		});
			
		return jPanel;
	}
	public HistoryReference getHistoryRef() {
                return historyRef;
        }

        public void setHistoryRef(HistoryReference historyRef) {
                this.historyRef = historyRef;
		StringBuilder sb = new StringBuilder();
                sb.append(historyRef.getURI().toString());
                System.out.println("URL " + sb);
		printURL.setText(sb.toString());
		System.out.println("OK");
		try {
		System.out.println("Entered Params");
		Map<String, String> params = Model.getSingleton().getSession().getParams(historyRef.getHttpMessage(), HtmlParameter.Type.form);
                for(Map.Entry<String, String> entry : params.entrySet()){
			System.out.println("Form Params: ");
                        System.out.println(entry.getKey()+" :: "+entry.getValue());
			JRadioButton button = new JRadioButton(entry.getKey());
			group.add(button);
			jPanel.add(button);
                }
		} catch (HttpMalformedHeaderException | DatabaseException e) {
        	    logger.error(e.getMessage(), e);
	        }
 
        }
}
