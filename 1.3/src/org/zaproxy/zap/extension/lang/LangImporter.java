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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

public class LangImporter {

	private static Logger logger = Logger.getLogger(LangImporter.class);

	public static void importLanguagePack(String languagePack) {
		try {
			File F = new File(languagePack);
			System.out.println(F.getAbsolutePath());
			ZipFile zipFile = new ZipFile(F.getAbsolutePath());
			Enumeration enumeration = zipFile.entries();

			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
				if (!zipEntry.isDirectory()) {
					BufferedInputStream bis = new BufferedInputStream(
							zipFile.getInputStream(zipEntry));

					int size;
					byte[] buffer = new byte[2048];
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(zipEntry.getName()),
							buffer.length);

					while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
						bos.write(buffer, 0, size);
					}
					bos.flush();
					bos.close();
					bis.close();
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}