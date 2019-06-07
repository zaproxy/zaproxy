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
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.db;

public class RecordScan {

    private int scanId = 0;
    private String scanName = "";
    private java.sql.Date scanTime = null;

    /**
     * @param scanId
     * @param scanName
     * @param scanTime
     */
    public RecordScan(int scanId, String scanName, java.sql.Date scanTime) {
        super();
        setScanId(scanId);
        setScanName(scanName);
        setScanTime(scanTime);
    }
    /** @return Returns the scanId. */
    public int getScanId() {
        return scanId;
    }
    /** @param scanId The scanId to set. */
    public void setScanId(int scanId) {
        this.scanId = scanId;
    }
    /** @return Returns the scanName. */
    public String getScanName() {
        return scanName;
    }
    /** @param scanName The scanName to set. */
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }
    /** @return Returns the scanTime. */
    public java.sql.Date getScanTime() {
        return scanTime;
    }
    /** @param scanTime The scanTime to set. */
    public void setScanTime(java.sql.Date scanTime) {
        this.scanTime = scanTime;
    }
}
