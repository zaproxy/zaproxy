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
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
package org.parosproxy.paros.core.scanner.plugin;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;


public class TestInjectionCRLF extends AbstractAppParamPlugin {

    private static Random   staticRandomGenerator =     new Random();
    private String randomString = "Tamper=" + Long.toString(Math.abs(staticRandomGenerator.nextLong()));
    private String cookieTamper1 = "Set-cookie: " + randomString;
    private String cookieTamper2a = "any\r\nSet-cookie: " + randomString;
    private String cookieTamper2b = "any?\r\nSet-cookie: " + randomString;
    private String cookieTamper3a = "any\nSet-cookie: " + randomString;
    private String cookieTamper3b = "any?\nSet-cookie: " + randomString;
    private String cookieTamper4a = "any\r\nSet-cookie: " + randomString + "\r\n";
    private String cookieTamper4b = "any?\r\nSet-cookie: " + randomString + "\r\n";

    // should not be changed to static as Global may not be ready
    private String[] PARAM_LIST = {cookieTamper1, cookieTamper2a, cookieTamper2b, cookieTamper3a, cookieTamper3b, cookieTamper4a, cookieTamper4b};
    
    private Pattern patternCookieTamper = Pattern.compile("\\nSet-cookie: " + randomString, PATTERN_PARAM);


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getId()
     */
    @Override
    public int getId() {
        return 40003;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getName()
     */
    @Override
    public String getName() {
        return "CRLF injection";
    }



    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDescription()
     */
    @Override
    public String getDescription() {
        
        String msg = "Cookie can be set via CRLF injection.  It may also be possible to set arbitrary HTTP response header." + CRLF
        + "In addition, by carefully crafting the injected response using cross-site script, cache poisiong vulnerability may also exist.";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getCategory()
     */
    @Override
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getSolution()
     */
    @Override
    public String getSolution() {
        return "Type check the submitted parameter carefully.  Do not allow CRLF to be injected by filtering CRLF.";
        
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getReference()
     */
    @Override
    public String getReference() {
        String msg = "<ul><li>http://www.watchfire.com/resources/HTTPResponseSplitting.pdf</li>"
        + "<li>http://webappfirewall.com/lib/crlf-injection.txtnull</li>" 
        + "<li>http://www.securityfocus.com/bid/9804</li>"
        + "</ul>";
         
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractPlugin#init()
     */
    @Override
    public void init() {
 
    }
    
    @Override
    public void scan(HttpMessage msg, String param, String value) {

        String bingoQuery = null;   
        
        // loop parameters

        for (int i=0; i<PARAM_LIST.length; i++) {
            msg = getNewMsg();
            bingoQuery = setParameter(msg, param, PARAM_LIST[i]);
            try {
                sendAndReceive(msg, false);
                if (checkResult(msg, param, PARAM_LIST[i])) {
                    return;
                }

            } catch (Exception e) {
            }    

        }

        
    }

    private boolean checkResult(HttpMessage msg, String param, String attack) {
        // no need to bother if response OK or not.
//      if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK
//          && !HttpStatusCode.isServerError(msg.getResponseHeader().getStatusCode())) {
//          return false;
//      }
        
        Matcher matcher = patternCookieTamper.matcher(msg.getResponseHeader().toString());
        if (matcher.find()) {
            bingo(Alert.RISK_MEDIUM, Alert.WARNING, null, param, attack, "", msg);
            return true;
        }
        
        return false;
        
    }

}
