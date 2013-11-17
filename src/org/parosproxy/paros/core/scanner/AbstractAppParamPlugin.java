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
// ZAP: 2013/07/14 Issue 726: Catch active scan variants' exceptions
// ZAP: 2013/09/23 Issue 795: Allow param types scanned to be configured via UI
// ZAP: 2013/09/26 Reviewed Variant Panel configuration
//
package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

public abstract class AbstractAppParamPlugin extends AbstractAppPlugin {

    private Logger logger = Logger.getLogger(this.getClass());
    private ArrayList<Variant> listVariant = new ArrayList();
    private NameValuePair originalPair = null;
    private Variant variant = null;

    @Override
    public void scan() {
        int targets = this.getParent().getScannerParam().getTargetParamsInjectable();
        int enabledRPC = this.getParent().getScannerParam().getTargetParamsEnabledRPC();

        // First check URL query-string target configuration
        if ((targets & ScannerParam.TARGET_QUERYSTRING) != 0) {
            listVariant.add(new VariantURLQuery());
            
            // ZAP: To handle parameters in OData urls
            if ((enabledRPC & ScannerParam.RPC_ODATA) != 0) {
                listVariant.add(new VariantODataIdQuery());
                listVariant.add(new VariantODataFilterQuery());
            }
        }
        
        // Then check POST data target configuration and RPC enabled methods
        if ((targets & ScannerParam.TARGET_POSTDATA) != 0) {
            listVariant.add(new VariantFormQuery());

            // ZAP: To handle Multipart Form-Data POST requests
            if ((enabledRPC & ScannerParam.RPC_MULTIPART) != 0) {
                listVariant.add(new VariantMultipartFormQuery());            
            }
            
            // ZAP: To handle XML based POST requests
            if ((enabledRPC & ScannerParam.RPC_XML) != 0) {
                listVariant.add(new VariantXMLQuery());
            }

            // ZAP: To handle JSON based POST requests
            if ((enabledRPC & ScannerParam.RPC_JSON) != 0) {
                listVariant.add(new VariantJSONQuery());
            }
            
            // ZAP: To handle GWT Serialized POST requests
            if ((enabledRPC & ScannerParam.RPC_GWT) != 0) {
                listVariant.add(new VariantGWTQuery());
            }            
        }
        
        if ((targets & ScannerParam.TARGET_HTTPHEADERS) != 0) {
            listVariant.add(new VariantHeader());
        }

        // Currently usual plugins seems not 
        // suitable to cookie vulnerabilities
        // 'cause the character RFC limitation
        // is it useful?
        if ((targets & ScannerParam.TARGET_COOKIE) != 0) {
            listVariant.add(new VariantCookie());
        }

        for (int i = 0; i < listVariant.size() && !isStop(); i++) {
            HttpMessage msg = getNewMsg();
            // ZAP: Removed unnecessary cast.
            variant = listVariant.get(i);
            try {
                variant.setMessage(msg);
                scanVariant();

            } catch (Exception e) {
                logger.error("Error occurred while scanning with variant " + variant.getClass().getCanonicalName(), e);
            }
        }

    }

    private void scanVariant() {
        for (int i = 0; i < variant.getParamList().size() && !isStop(); i++) {
            // ZAP: Removed unnecessary cast.
            originalPair = variant.getParamList().get(i);
            HttpMessage msg = getNewMsg();
            try {
                scan(msg, originalPair.getName(), originalPair.getValue());
            } catch (Exception e) {
                logger.error("Error occurred while scanning a message:", e);
            }
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
