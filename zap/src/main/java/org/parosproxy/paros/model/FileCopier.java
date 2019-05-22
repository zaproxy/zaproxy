/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2013/01/03 Code Cleanup: Introduced Java7 try-with-resource statements
// ZAP: 2013/01/03 Code Cleanup: Removed redundant and generated comments
package org.parosproxy.paros.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileCopier {

	public FileCopier() {
		super();
	}

	public void copy(File in, File out) throws IOException {
		try {
			copyNIO(in, out);
		} catch (IOException e) {
			// there is a NIO bug causing exception on the above under Debian.
			copyLegacy(in, out);
		}
	}

	public void copyLegacy(File in, File out) throws IOException {
		// CHECKSTYLE:OFF Inner assignments for try-with-resource are okay
		try (FileInputStream inStream = new FileInputStream(in);
				BufferedInputStream inBuf = new BufferedInputStream(inStream);
				FileOutputStream outStream = new FileOutputStream(out);
				BufferedOutputStream outBuf = new BufferedOutputStream(
						outStream);) {
			// CHECKSTYLE:ON
			byte[] buf = new byte[10240];
			int len = 1;
			while (len > 0) {
				len = inBuf.read(buf);
				if (len > 0) {
					outBuf.write(buf, 0, len);
				}
			}
		}
	}

	public void copyNIO(File in, File out) throws IOException {
		// CHECKSTYLE:OFF Inner assignments for try-with-resource are okay
		try (FileInputStream inStream = new FileInputStream(in);
				FileOutputStream outStream = new FileOutputStream(out);
				FileChannel sourceChannel = inStream.getChannel();
				FileChannel destinationChannel = outStream.getChannel();) {
			// CHECKSTYLE:ON
			destinationChannel.transferFrom(sourceChannel, 0,
					sourceChannel.size());
		}
	}
}
