package org.zaproxy.zap.view.widgets;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.report.ReportSettings;
import org.parosproxy.paros.model.Model;

public class WritableFileChooser extends JFileChooser {

	private static final long serialVersionUID = -8600149638325315049L;
	private ReportSettings settings;
	
	public WritableFileChooser() {
		super();
	}

	public WritableFileChooser(File currentDirectory) {
		super(currentDirectory);
		settings = new ReportSettings(false);
		JPanel htmlContainer = new JPanel(new FlowLayout());
		JCheckBox htmlButton = new JCheckBox("include HTML");
		
		Container southContainer = (Container)this.getComponent(3);
		Container buttonContainer = (Container)southContainer.getComponent(3);
		htmlContainer.add(htmlButton, FlowLayout.LEFT);
		buttonContainer.add(htmlContainer, FlowLayout.LEFT);
		southContainer.add(buttonContainer, BorderLayout.SOUTH);
		this.add(southContainer, BorderLayout.SOUTH);
		
		htmlButton.addActionListener(new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				settings.setIncludeHTML(htmlButton.isSelected());
			}
		});
	}
	
	public ReportSettings getSettings() {
		return settings;
	}

	@Override
	public void approveSelection() {
		File selectedFile = getSelectedFile();

		if (!java.nio.file.Files.isWritable(selectedFile.getParentFile().toPath())) {
			JOptionPane.showMessageDialog(this,
					MessageFormat.format(Constant.messages.getString("report.write.permission.dialog.message"),
		                	selectedFile.getAbsolutePath()),
					Constant.messages.getString("report.write.permission.dialog.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (selectedFile.exists()) {
			int result = JOptionPane.showConfirmDialog(this,
					Constant.messages.getString("report.write.overwrite.dialog.message"),
					Constant.messages.getString("report.write.overwrite.dialog.title"),
					JOptionPane.YES_NO_OPTION);
			switch (result) {
			case JOptionPane.YES_OPTION:
				super.approveSelection();
				return;
			case JOptionPane.NO_OPTION:
			case JOptionPane.CLOSED_OPTION:
				return;
			}
		}
		// Store the user directory as the currently selected one
		Model.getSingleton().getOptionsParam().setUserDirectory(getCurrentDirectory());
		super.approveSelection();
	}
}
