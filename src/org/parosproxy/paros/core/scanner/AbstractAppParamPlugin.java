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
package org.parosproxy.paros.core.scanner;

import java.util.Vector;

import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class AbstractAppParamPlugin extends AbstractAppPlugin {


    private Vector listVariant = new Vector();
    
    private Variant variant = null;
    private NameValuePair originalPair = null;
    
    public void scan() {
        listVariant.add(new VariantURLQuery());
        listVariant.add(new VariantFormQuery());
        
        for (int i=0; i<listVariant.size() && !isStop(); i++) {
            HttpMessage msg = getNewMsg();
            variant = (Variant) listVariant.get(i);
            variant.setMessage(msg);
            scanVariant();
        }
        
    }

    private void scanVariant() {
        for (int i=0; i<variant.getParamList().size() && !isStop(); i++) {
            originalPair = (NameValuePair) variant.getParamList().get(i);
            HttpMessage msg = getNewMsg();
            scan(msg, originalPair.getName(), originalPair.getValue());
        }
    }
    
    abstract public void scan(HttpMessage msg, String param, String value);

    /**
     * Set the paramter into the current message.  The position will be handled
     * by the Abstract class.
     * If both param and value is null, the current parameter will be removed.
     * @param msg
     * @param param
     * @param value
     * @return
     */
    protected String setParameter(HttpMessage msg, String param, String value) {
        return variant.setParameter(msg, originalPair, param, value);
    }
}
