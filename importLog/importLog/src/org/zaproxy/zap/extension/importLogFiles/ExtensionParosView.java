package org.zaproxy.zap.extension.importLogFiles;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.io.*;
import java.awt.*;

public class ExtensionParosView extends JPanel implements ActionListener {
	static private final String newline = "\n";
	JButton openButton, saveButton;
	JTextArea log;
	JFileChooser fc;

	public ExtensionParosView()
	{
		super(new BorderLayout());

	log = new JTextArea(5,20);
	log.setMargin(new Insets(5,5,5,5));
	log.setEditable(false);
	JScrollPane logScrollPane = new JScrollPane(log);
	
	fc = new JFileChooser();
	
	openButton = new JButton("Select a log file to upload to the ZAP site tree");
	openButton.addActionListener(this);
	
	saveButton = new JButton("Save this file...");
	saveButton.addActionListener(this);
	
	JPanel buttonPanel = new JPanel();
	buttonPanel.add(openButton);
	buttonPanel.add(saveButton);
	
	//Add the buttons and the log to this panel.
    add(buttonPanel, BorderLayout.PAGE_START);
    add(logScrollPane, BorderLayout.CENTER);
	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//Handle open button
		if(e.getSource() == openButton){
		 int returnVal = fc.showSaveDialog(ExtensionParosView.this);
		 
			 if(returnVal == JFileChooser.APPROVE_OPTION){
				 File file = fc.getSelectedFile();
				 //Need to do something here
				 log.append("Saving: " + file.getName() + "." + newline);
			 } 
		 
			 else {
             log.append("Save command cancelled by user." + newline);
         }
         	log.setCaretPosition(log.getDocument().getLength());
		 }
		
		//Handle save button
		else if (e.getSource() == saveButton) {
            int returnVal = fc.showSaveDialog(ExtensionParosView.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would save the file.
                log.append("Saving: " + file.getName() + "." + newline);
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
		
		}
	}
