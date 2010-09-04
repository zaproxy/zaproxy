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


import javax.swing.DefaultListModel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HistoryList extends DefaultListModel {
    
    public void addElement(final Object obj) {
//        if (EventQueue.isDispatchThread()) {
//            synchronized(this) {
//                super.addElement(obj);
//            }
//            return;
//        }
//        try {
//            EventQueue.invokeLater(new Runnable() {
//                public void run() {
//                    synchronized(HistoryList.this) {
//                        HistoryList.super.addElement(obj);
//                    }
//                }
//            });
//        } catch (Exception e) {
//        }

        super.addElement(obj);
        
    }
    
    public synchronized void notifyItemChanged(Object obj) {
        int i = indexOf(obj);
        if (i >= 0) {
            fireContentsChanged(this, i, i);
        }
    }
    
}