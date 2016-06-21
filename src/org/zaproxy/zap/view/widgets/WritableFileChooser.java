package org.zaproxy.zap.view.widgets;

import java.io.File;
import java.nio.file.Files;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;

public class WritableFileChooser extends JFileChooser {

	private static final long serialVersionUID = -8600149638325315049L;
	
	private static final int MINIMUM_SPACE_REQUIREMENT_MB = 5000000;

	public WritableFileChooser() {
		super();
	}

	public WritableFileChooser(File currentDirectory) {
		super(currentDirectory);
	}

	@Override
	public void approveSelection() {
		File selectedFile = getSelectedFile();

		File checkFile = selectedFile;
		boolean fileExists = checkFile.exists();
		if (!fileExists) {
			checkFile = checkFile.getParentFile();
		}
		if (checkFile.getUsableSpace() < MINIMUM_SPACE_REQUIREMENT_MB) {
			int result = JOptionPane.showConfirmDialog(this,
					Constant.messages.getString("report.write.diskspace.warning.dialog.message"),
					Constant.messages.getString("report.write.diskspace.warning.dialog.title"),
					JOptionPane.YES_NO_OPTION);
			if (result != JOptionPane.YES_OPTION) {
				return;
			}
		}
		if (!Files.isWritable(selectedFile.getParentFile().toPath())) {
			warnNotWritable("report.write.permission.dir.dialog.message", selectedFile.getParentFile().getAbsolutePath());

			return;
		}
		if (fileExists) {
			if (!Files.isWritable(selectedFile.toPath())) {
				warnNotWritable("report.write.permission.file.dialog.message", selectedFile.getAbsolutePath());
				return;
			}

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

	private void warnNotWritable(String i18nKeyMessage, String path) {
		JOptionPane.showMessageDialog(this,
				MessageFormat.format(Constant.messages.getString(i18nKeyMessage), path),
				Constant.messages.getString("report.write.permission.dialog.title"),
				JOptionPane.ERROR_MESSAGE);
	}
}
