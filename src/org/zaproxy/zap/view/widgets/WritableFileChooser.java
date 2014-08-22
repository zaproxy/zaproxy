package org.zaproxy.zap.view.widgets;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;

public class WritableFileChooser extends JFileChooser {

	private static final long serialVersionUID = -8600149638325315049L;

	public WritableFileChooser() {
		super();
	}

	public WritableFileChooser(File currentDirectory) {
		super(currentDirectory);
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
