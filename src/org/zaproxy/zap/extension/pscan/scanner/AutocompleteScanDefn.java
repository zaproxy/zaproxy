/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.pscan.scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zaproxy.zap.extension.pscan.PassiveScanDefn;

public class AutocompleteScanDefn extends PassiveScanDefn {

	private static final int PATTERN_PARAM = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
	
    // check for autocomplete
    public final static Pattern patternAutocomplete = Pattern.compile("AUTOCOMPLETE\\s*=[^>]*OFF[^>]*", PATTERN_PARAM);

    // used reluctant quantifer to make sure the same form and input element is referred 
    public final static Pattern patternForm = Pattern.compile("(<FORM\\s*[^>]+\\s*>)(.*?)</FORM>", PATTERN_PARAM| Pattern.DOTALL);
    public final static Pattern patternInput = Pattern.compile("(<INPUT\\s*[^>]+type=[\"']?PASSWORD[\"']?[^>]+\\s*>)", PATTERN_PARAM| Pattern.DOTALL);

	public AutocompleteScanDefn(String name, TYPE type, String config) {
		super(name, type, config);
	}
	
	private boolean parentAutoCompleteForm (Node node) {
		
		if ("FORM".equalsIgnoreCase(node.getNodeName())) {
			NamedNodeMap nnm = node.getAttributes();
			for (int i=0; i < nnm.getLength(); i++) {
				Node attNode = nnm.item(i);
				if ("AUTOCOMPLETE".equalsIgnoreCase(attNode.getNodeName())) {
					if ("OFF".equalsIgnoreCase(attNode.getNodeValue())) {
						return false;
					}
				}
			}
			return true;
			
		} else if (node.getParentNode() != null) {
			return parentAutoCompleteForm(node.getParentNode());
		}
		
		return false;
	}
	
	private boolean hasPasswordTypeAtt(Node node) {
		NamedNodeMap nnm = node.getAttributes();
		for (int i=0; i < nnm.getLength(); i++) {
			Node attNode = nnm.item(i);
			if ("TYPE".equalsIgnoreCase(attNode.getNodeName())) {
				if ("PASSWORD".equalsIgnoreCase(attNode.getNodeValue())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean scanHttpResponseReceive(HttpMessage msg, Document document) {
		if (document != null) {
			NodeList inputNodeList = document.getElementsByTagName("INPUT");
			
			for (int i=0; i < inputNodeList.getLength(); i++) {
				Node node = inputNodeList.item(i);
				if (hasPasswordTypeAtt(node) && parentAutoCompleteForm(node)) {
					return true;
				}
			}

			inputNodeList = document.getElementsByTagName("PASSWORD");
			
			for (int i=0; i < inputNodeList.getLength(); i++) {
				Node node = inputNodeList.item(i);
				if (parentAutoCompleteForm(node)) {
					return true;
				}
			}

		} else {
			System.out.println("Null doc :(");
		}
		/*
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
                    return true;

                }
            }
        }
        */

		return false;
	}

    @SuppressWarnings("unused")
	private boolean isExistPasswordInput(String s) {
        Matcher matcherInput = patternInput.matcher(s);
        if (matcherInput.find()) {
            return true;
        }
        return false;
    }

	@Override
	public Alert getAlert(HttpMessage msg) {
	    Alert alert = new Alert(10000, Alert.RISK_MEDIUM, Alert.WARNING, 
	    		"Password Autocomplete in browser");
	    alert.setDetail(
    		"AUTOCOMPLETE attribute is not disabled in HTML FORM/INPUT element containing password type input.  Passwords may be stored in browsers and retrieved.", 
    		msg.getRequestHeader().getURI().toString(), 
    		"param", "otherInfo", 
    		"Turn off AUTOCOMPLETE attribute in form or individual input elements containing password by using AUTOCOMPLETE='OFF'", 
            "http://msdn.microsoft.com/library/default.asp?url=/workshop/author/forms/autocomplete_ovr.asp", 
            msg);

		return alert;
	}

}
