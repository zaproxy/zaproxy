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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method and removed unnecessary casts.
// ZAP: 2012/08/31 Added support for AttackStrength
// ZAP: 2013/02/12 Added variant handling the parameters of OData urls
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/07/02 Changed Vector to generic List and added new varaints for GWT, JSON and Headers
// ZAP: 2013/07/03 Added variant handling attributes and data contained in XML requests 

package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;

import org.parosproxy.paros.network.HttpMessage;

public abstract class AbstractAppParamPlugin extends AbstractAppPlugin {

    private ArrayList<Variant> listVariant = new ArrayList<>();    
    private NameValuePair originalPair = null;
    private Variant variant = null;
    
    @Override
    public void scan() {
        listVariant.add(new VariantURLQuery());
        listVariant.add(new VariantFormQuery());
        listVariant.add(new VariantMultipartFormQuery());
        listVariant.add(new VariantGWTQuery());
        listVariant.add(new VariantXMLQuery());
        listVariant.add(new VariantJSONQuery());
        listVariant.add(new VariantHeader());
        // Currently usual plugins seems not 
        // suitable to cookie vulnerabilities
        // 'cause the character RFC limitation
        // is it useful?
        //listVariant.add(new VariantCookie());

        // ZAP: To handle parameters in OData urls
        listVariant.add(new VariantODataIdQuery());
        listVariant.add(new VariantODataFilterQuery());

        /* Work in progress
         if (this.getParent().getScannerParam().isTargetParamsUrl()) {
         listVariant.add(new VariantURLQuery());
         }
         if (this.getParent().getScannerParam().isTargetParamsForm()) {
         listVariant.add(new VariantFormQuery());
         }
         if (this.getParent().getScannerParam().isTargetParamsCookie()) {
         listVariant.add(new VariantCookie());
         }
         */

        for (int i = 0; i < listVariant.size() && !isStop(); i++) {
            HttpMessage msg = getNewMsg();
            // ZAP: Removed unnecessary cast.
            variant = listVariant.get(i);
            variant.setMessage(msg);
            scanVariant();
        }

    }

    private void scanVariant() {
        for (int i = 0; i < variant.getParamList().size() && !isStop(); i++) {
            // ZAP: Removed unnecessary cast.
            originalPair = variant.getParamList().get(i);
            HttpMessage msg = getNewMsg();
            scan(msg, originalPair.getName(), originalPair.getValue());
        }
    }

    /**
     * 
     * @param msg
     * @param param
     * @param value 
     */
    public abstract void scan(HttpMessage msg, String param, String value);

    /**
     * Set the paramter into the current message. The position will be handled
     * by the Abstract class. If both param and value is null, the current
     * parameter will be removed.
     *
     * @param msg
     * @param param
     * @param value
     * @return
     */
    protected String setParameter(HttpMessage msg, String param, String value) {
        return variant.setParameter(msg, originalPair, param, value);
    }

    /**
     * 
     * @param msg
     * @param param
     * @param value
     * @return 
     */
    protected String setEscapedParameter(HttpMessage msg, String param, String value) {
        return variant.setEscapedParameter(msg, originalPair, param, value);
    }
}
