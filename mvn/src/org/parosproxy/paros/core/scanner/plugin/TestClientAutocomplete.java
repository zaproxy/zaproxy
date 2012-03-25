/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
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

package org.parosproxy.paros.core.scanner.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestClientAutocomplete extends AbstractAppPlugin {
    
	// ZAP Depreciated by org.zaproxy.zap.extension.pscan.scanner.PasswordAutocompleteScanner
	@Override
	public boolean isDepreciated() {
		return true;
	}

    //  pattern for testing

    // <form autocomplete=off>
    // <input type=password ...>
    // </form>
    
    // check for autocomplete
    public final static Pattern patternAutocomplete = Pattern.compile("AUTOCOMPLETE\\s*=[^>]*OFF[^>]*", PATTERN_PARAM);

    // used reluctant quantifier to make sure the same form and input element is referred 
    //public final static Pattern patternForm = Pattern.compile("(<FORM\\s*[^>]+\\s*>)(.*?)</FORM>", PATTERN_PARAM| Pattern.DOTALL);
    public final static Pattern patternForm = Pattern.compile("(<FORM\\b[^>]*>(.*?)</FORM>)", PATTERN_PARAM| Pattern.DOTALL);
    public final static Pattern patternInput = Pattern.compile("(<INPUT\\s*[^>]+type=[\"']?PASSWORD[\"']?[^>]+\\s*>)", PATTERN_PARAM| Pattern.DOTALL);

    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getId()
     */
    public int getId() {
        return 10000;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getName()
     */
    public String getName() {
        return "Password Autocomplete in browser";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDescription()
     */
    public String getDescription() {
        return "AUTOCOMPLETE attribute is not disabled in HTML FORM/INPUT element containing password type input.  Passwords may be stored in browsers and retrieved.";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getCategory()
     */
    public int getCategory() {
        return Category.BROWSER;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getSolution()
     */
    public String getSolution() {
        String msg = "Turn off AUTOCOMPLETE attribute in form or individual input elements containing password by using "
            + "AUTOCOMPLETE='OFF'";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getReference()
     */
    public String getReference() {
        return "http://msdn.microsoft.com/library/default.asp?url=/workshop/author/forms/autocomplete_ovr.asp";
        
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractPlugin#init()
     */
    public void init() {

    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#scan()
     */
    public void scan() {

        HttpMessage msg = getBaseMsg();
        String txtBody = msg.getResponseBody().toString();
        String txtForm = null;
        String txtInputs = null;
        Matcher matcherForm = patternForm.matcher(txtBody);
        Matcher matcherAutocomplete = null;
        Matcher matcherInput = null;
        
        while (matcherForm.find()) {
            txtForm = matcherForm.group(1);
            txtInputs = matcherForm.group(2);

            if (txtForm == null || txtInputs == null) {
                continue;
            }
                   
            if (!isExistPasswordInput(txtInputs)) {
                continue;
            }
            
            matcherAutocomplete = patternAutocomplete.matcher(txtForm);
            if (matcherAutocomplete.find()) {
                continue;
            }
            
            matcherInput = patternInput.matcher(txtInputs);
            while (matcherInput.find()) {
                String s = matcherInput.group(1);
                if (s != null) {
                    matcherAutocomplete = patternAutocomplete.matcher(s);
                    if (matcherAutocomplete.find()) {
                        continue;
                    }
                    bingo(Alert.RISK_MEDIUM, Alert.WARNING, null, "", "", s, msg);

                }
            }
            
                
                
        }

    }

    private boolean isExistPasswordInput(String s) {
        Matcher matcherInput = patternInput.matcher(s);
        if (matcherInput.find()) {
            return true;
        }
        return false;
    }
}
