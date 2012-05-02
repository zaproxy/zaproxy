package org.zaproxy.zap.scanner.plugin;

import java.util.Iterator;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;

@Deprecated
public class TestCrossSiteScriptInTagAttribute extends AbstractAppParamPlugin {

	// ZAP Depreciated by org.zaproxy.zap.scanner.plugin.TestCrossSiteScriptV2
	@Override
	public boolean isDepreciated() {
		return true;
	}

	private static final String BAD_SITE = "http://badsite.com";
	private static final String SRC_ATT_INJ= " src=" + BAD_SITE;
	
    private static final String [] VULN_ATTS = { 
    									"onblur",
										"onchange",
										"onclick",
										"ondblclick",
										"onfocus",
										"onkeydown",
										"onkeypress",
										"onkeyup",
										"onmousedown",
										"onmousemove",
										"onmouseout",
										"onmouseover",
										"onmouseup",
										"onreset",
										"onselect",
										"onsubmit",
										"src"
									};

    private static final String [] VULN_SRC_TAGS = { 
										"applet",
										"embed",
										"frame",
										"iframe",
										"script",
										"xml"
    								};

    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_8");
    private static Logger log = Logger.getLogger(TestCrossSiteScriptInTagAttribute.class);

    @Override
    public int getId() {
        return 40011;
    }

    @Override
    public String getName() {
    	return Constant.messages.getString("scanner.plugin.xssatt");
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getDescription()
     */
    @Override
    public String getDescription() {
    	if (vuln != null) {
    		return vuln.getDescription();
    	}
    	return "Failed to load vulnerability description from file";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getCategory()
     */
    @Override
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getSolution()
     */
    @Override
    public String getSolution() {
    	if (vuln != null) {
    		return vuln.getSolution();
    	}
    	return "Failed to load vulnerability solution from file";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Test#getReference()
     */
    @Override
    public String getReference() {
    	if (vuln != null) {
    		StringBuilder sb = new StringBuilder();
    		for (String ref : vuln.getReferences()) {
    			if (sb.length() > 0) {
    				sb.append('\n');
    			}
    			sb.append(ref);
    		}
    		return sb.toString();
    	}
    	return "Failed to load vulnerability reference from file";
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.AbstractTest#init()
     */
    @Override
    public void init() {

    }

    @Override
    public void scan(HttpMessage msg, String param, String value) {
	
		try {
			setParameter(msg, param, Constant.getEyeCatcher());
            sendAndReceive(msg);
            
			String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
			String responseLc = response.toLowerCase();
			Source src = new Source(response);
        	int startOffset = -1;
        	boolean attributeInjection = false;
        	
        	while ((startOffset = responseLc.indexOf(Constant.getEyeCatcher().toLowerCase(), startOffset+1)) > 0) {
        		// Find enclosing element of each instance of the injected value
        		Element element = src.getEnclosingElement(startOffset);
        		if (element != null) {
        			Iterator<Attribute> iter = element.getAttributes().iterator();
        			// See if its in an attribute
        			while (iter.hasNext()) {
        				Attribute att = iter.next();
        				if (att.getValue() != null && 
        						att.getValue().toLowerCase().indexOf(Constant.getEyeCatcher().toLowerCase()) >= 0) {
        					// Found the injected value
        					attributeInjection = true;
    	        			for (String vuln : VULN_ATTS) {
    	        				// Check all of the vulnerable attributes
	    	        			if (vuln.equalsIgnoreCase(att.getName())) {
	    							bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param, Constant.getEyeCatcher(),
	    									"Tag=" + element.getStartTag().getName() + ", attribute=" + att.getName(), msg);
	    							return;
	    	        			}
    	        			}
        				}
        			}
        		}
        	}
        	
        	if (attributeInjection) {
        		// We managed to inject into an attribute, but not a 'vulnerable' one
        		// Try injecting a url into a src attribute... 
    			setParameter(msg, param, value + SRC_ATT_INJ);
                sendAndReceive(msg);
                
    			response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
    			src = new Source(response);
            	startOffset = -1;
            	
            	while ((startOffset = response.indexOf(BAD_SITE, startOffset+1)) > 0) {
            		Element element = src.getEnclosingElement(startOffset);
            		if (element != null) {
            			// Just check potentially vulnerable tags
            			boolean vulnTag = false;
	        			for (String tag : VULN_SRC_TAGS) {
	        				if (tag.equalsIgnoreCase(element.getName())) {
	        					vulnTag = true;
	        					break;
	        				}
	        			}
	        			if (!vulnTag) {
	        				continue;
	        			}
	        			
	        			String srcValue = element.getAttributeValue("src");
            			
        				if (srcValue != null && srcValue.toLowerCase().indexOf(BAD_SITE) >= 0) {
	    					bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param, value + SRC_ATT_INJ, null, msg);
	    					return;
        				}
            		}
            	}
        	}
        } catch (Exception e) {
            log.error(e.getMessage(), e);;
        }
	}
}
