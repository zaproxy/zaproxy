/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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

package org.zaproxy.zap.extension.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class LangImporter {
	
	private static Logger logger = Logger.getLogger(LangImporter.class);
	
	private static final String FILENAME_PATTERN = "Messages_([a-z]{2}_[A-Z]{2})\\.properties$";
	
	private static final String MSG_SUCCESS = "options.lang.importer.dialog.message.success";
	private static final String MSG_ERROR = "options.lang.importer.dialog.message.error";
	private static final String MSG_FILE_NOT_FOUND = "options.lang.importer.dialog.message.filenotfound";
	
	
	public static void importLanguagePack(String languagePack) {
		Matcher matcher = null;
		Pattern pattern = Pattern.compile(FILENAME_PATTERN);
		
		int langFileCount = 0;
		String message = "";
		
		ZipFile zipFile = null;
		try {
			File F = new File(languagePack);
			zipFile = new ZipFile(F.getAbsolutePath());
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
			
			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = enumeration.nextElement();
				
				if (!zipEntry.isDirectory()) {
					BufferedInputStream bis = null;
					try {
						bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
	
						int size;
						byte[] buffer = new byte[2048];
						String name = zipEntry.getName();
						
						matcher = pattern.matcher(name);
						if (matcher.find()) {
							langFileCount++;
							
							BufferedOutputStream bos = null;
							try {
								bos = new BufferedOutputStream(
										new FileOutputStream(name),
										buffer.length);
			
								while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
									bos.write(buffer, 0, size);
								}
								
								bos.flush();
							} finally {
								if (bos != null) {
									bos.close();
								}
							}
						}
					} finally {
						if (bis != null) {
							bis.close();
						}
					}
				}
			}
			
			message = (langFileCount > 0) ? MSG_SUCCESS : MSG_ERROR;
			
		} catch (IOException e) {
			message = MSG_FILE_NOT_FOUND;
			logger.error(e.getMessage(), e);
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
		JOptionPane.showMessageDialog(null,
				MessageFormat.format(Constant.messages.getString(message), langFileCount),
				Constant.messages.getString("options.lang.importer.dialog.title"),                                            
				(langFileCount > 0) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
	}
}