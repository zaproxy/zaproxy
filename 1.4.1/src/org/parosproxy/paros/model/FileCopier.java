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
package org.parosproxy.paros.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FileCopier {

    /**
     * 
     */
    public FileCopier() {
        super();

    }
    
    public void copy(File in, File out) throws IOException {

        try {
            // first use NIO when possible
            
            copyNIO(in, out);
        } catch (IOException e) {
            // if any error, try java legacy
            // there is a NIO bug causing exception on the above under Debian.

            copyLegacy(in, out);
        }
        
    }
    
    
    public void copyLegacy(File in, File out) throws IOException  {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        BufferedInputStream inBuf = null;
        BufferedOutputStream outBuf = null;
        
        try {
            inStream = new FileInputStream(in);
            outStream = new FileOutputStream(out);
            inBuf = new BufferedInputStream(inStream);
            outBuf = new BufferedOutputStream(outStream);
            
            
            byte[] buf = new byte[10240];
            int len = 1;
            while (len > 0) {
                len = inBuf.read(buf);
                if (len > 0) {
                    outBuf.write(buf, 0, len);
                }
            }
        } finally {
            if (inBuf != null) inBuf.close();
            if (outBuf != null) outBuf.close();
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
        }
    }
    
    public void copyNIO(File in, File out) throws IOException  {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;
        try {
            inStream = new FileInputStream(in);
            outStream = new FileOutputStream(out);
            sourceChannel = inStream.getChannel();
            destinationChannel = outStream.getChannel();
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) sourceChannel.close();
            if (destinationChannel != null) destinationChannel.close();
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
        }
    }
}


