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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
package org.parosproxy.paros.network;

/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class ByteVector {

    private static final int INITIAL_SIZE = 4096;
    private byte[] buf = null;
    private int bufLen = 0;
    private boolean changed = true;
    
    public ByteVector() {
        buf = new byte[INITIAL_SIZE];
            
    }
    
    public ByteVector(int capacity) {
        buf = new byte[capacity];
    }

    
    void ensureCapacity(int minimumCapacity) {

        if (buf.length - bufLen >= minimumCapacity) {
            // enough buf, no expand needed.
            return;
        }

        // expand needed
        
        int expandLength = buf.length * 2;
        
        if (expandLength - bufLen < minimumCapacity) {
            expandLength = bufLen + minimumCapacity;
        }
        
        byte[] newBuf = new byte[expandLength];
        System.arraycopy(buf, 0, newBuf, 0, bufLen);
        buf = newBuf;
    }
    
    public synchronized void append(byte[] b, int offset, int length) {

        ensureCapacity(length);
        System.arraycopy(b, offset, buf, bufLen, length);
        bufLen += length;
        changed = true;
    }

    public void append(byte[] b, int length) {
        append(b, 0, length);
        
    }
    
    public void append(byte[] b) {
        append(b, 0, b.length);
    }

    /**
     * Return the current byte array containing the bytes.
     * @return
     */
    public byte[] getBytes() {
        if (!changed) {
            return buf;
        }
        
        byte[] newBuf = new byte[bufLen];
        System.arraycopy(buf, 0, newBuf, 0, bufLen);
        buf = newBuf;
        return buf;
    }
    
    public int length() {
        return bufLen;
    }
    
    public void setLength(int length) {

        if (length <= bufLen) {
            bufLen = length;
            return;
        } else {
            byte[] newBuf = new byte[length];
            System.arraycopy(buf, 0, newBuf, 0, bufLen);
            buf = newBuf;
        }
        
        
    }
}
