/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.scanner.plugin;

import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.httputils.HtmlContext;
import org.zaproxy.zap.httputils.HtmlContextAnalyser;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;

public class TestCrossSiteScriptV2 extends AbstractAppParamPlugin {

    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_8");
    private static Logger log = Logger.getLogger(TestCrossSiteScriptV2.class);

    @Override
    public int getId() {
        return 40012;
    }

    @Override
    public String getName() {
    	return Constant.messages.getString("scanner.plugin.xss");
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
    
    private List<HtmlContext> performAttack (HttpMessage msg, String param, String attack,
    		HtmlContext targetContext, int ignoreFlags) {
		HttpMessage msg2 = msg.cloneRequest();
		setParameter(msg2, param, attack);
        try {
			sendAndReceive(msg2);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

        HtmlContextAnalyser hca = new HtmlContextAnalyser(msg2);
        if (Plugin.Level.HIGH.equals(this.getLevel())) {
        	// High level, so check all results are in the expected context
        	return hca.getHtmlContexts(attack, targetContext, ignoreFlags);
        }
        return hca.getHtmlContexts(attack);
    }
    	
    @Override
    public void scan(HttpMessage msg, String param, String value) {
    	
		try {
	    	// Inject the 'safe' eyecatcher and see where it appears
			boolean attackWorked = false;
			setParameter(msg, param, Constant.getEyeCatcher());
            sendAndReceive(msg);
            
            HtmlContextAnalyser hca = new HtmlContextAnalyser(msg);
            List<HtmlContext> contexts = hca.getHtmlContexts(Constant.getEyeCatcher(), null, 0);
            if (contexts.size() == 0) {
            	// Lower case?
                contexts = hca.getHtmlContexts(Constant.getEyeCatcher().toLowerCase(), null, 0);
            }
            if (contexts.size() == 0) {
            	// Upper case?
                contexts = hca.getHtmlContexts(Constant.getEyeCatcher().toUpperCase(), null, 0);
            }
            if (contexts.size() == 0) {
            	// No luck - try again, appending the eyecatcher to the original value
    			setParameter(msg, param, param + Constant.getEyeCatcher());
                sendAndReceive(msg);
                hca = new HtmlContextAnalyser(msg);
            	contexts = hca.getHtmlContexts(value + Constant.getEyeCatcher(), null, 0);
            }
            if (contexts.size() == 0) {
            	// No luck - lets just try a direct attack
	            List<HtmlContext> contexts2 = performAttack (msg, param, 
	            		"'\"<script>alert(1);</script>", null, 0);
	            if (contexts2.size() > 0) {
            		// Yep, its vulnerable
					bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param, contexts2.get(0).getTarget(), 
							"", contexts2.get(0).getMsg());
					attackWorked = true;
	            }
            }
            
            for (HtmlContext context : contexts) {
            	// Loop through the returned contexts and lauch targetted attacks
            	if (attackWorked) {
            		break;
            	}
            	if (context.getTagAttribute() != null) {
            		// its in a tag attribute - lots of attack vectors possible
         
        			if (context.isInScriptAttribute()) {
            			// Good chance this will be vulnerable
        				// Try a simple alert attack
        	            List<HtmlContext> contexts2 = performAttack (msg, param, ";alert(1)", context, 0);
        	            
        	            for (HtmlContext context2 : contexts2) {
        	            	if (context2.getTagAttribute() != null &&
        	            			context2.isInScriptAttribute()) {
        	            		// Yep, its vulnerable
        						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, context2.getTarget(), 
        								"", context2.getMsg());
        						attackWorked = true;
        						break;
        	            	}
        	            }
        	            if (!attackWorked) {
        	            	log.debug("Failed to find vuln in script attribute on " + msg.getRequestHeader().getURI());
        	            }

        			} else if (context.isInUrlAttribute()) {
        				// Its a url attribute
        	            List<HtmlContext> contexts2 = performAttack (msg, param, "javascript:alert(1);", context, 0);

        	            if (contexts2.size() > 0) {
    	            		// Yep, its vulnerable
    						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
    							"", contexts2.get(0).getMsg());
    						attackWorked = true;
        	            }
        	            if (!attackWorked) {
        	            	log.debug("Failed to find vuln in url attribute on " + msg.getRequestHeader().getURI());
        	            }
        			}
            		if (! attackWorked && context.isInTagWithSrc()) {
            			// Its in an attribute in a tag which supports src attributes
        	            List<HtmlContext> contexts2 = performAttack (msg, param, 
        	            		context.getSurroundingQuote() + " src=http://badsite.com", context, HtmlContext.IGNORE_TAG);

        	            if (contexts2.size() > 0) {
    						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
    							"", contexts2.get(0).getMsg());
    						attackWorked = true;
        	            }
        	            if (!attackWorked) {
        	            	log.debug("Failed to find vuln in tag with src attribute on " + msg.getRequestHeader().getURI());
        	            }
            		}
        			
        			if (! attackWorked) {
        				// Try a simple alert attack
        	            List<HtmlContext> contexts2 = performAttack (msg, param, 
        	            		context.getSurroundingQuote() + "><script>alert(1);</script>", context, HtmlContext.IGNORE_TAG);
        	            if (contexts2.size() > 0) {
    	            		// Yep, its vulnerable
    						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
    								"", contexts2.get(0).getMsg());
    						attackWorked = true;
        	            }
        	            if (!attackWorked) {
        	            	log.debug("Failed to find vuln with simple script attack " + msg.getRequestHeader().getURI());
        	            }
        			}
        			if (! attackWorked) {
	            		// Try adding an onMouseOver
        	            List<HtmlContext> contexts2 = performAttack (msg, param, 
        	            		context.getSurroundingQuote() + " onMouseOver=" + context.getSurroundingQuote() + "alert(1);", 
        	            		context, HtmlContext.IGNORE_TAG);
        	            if (contexts2.size() > 0) {
    	            		// Yep, its vulnerable
    						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
    								"", contexts2.get(0).getMsg());
    						attackWorked = true;
    	            	}
        	            if (!attackWorked) {
        	            	log.debug("Failed to find vuln in with simple onmounseover " + msg.getRequestHeader().getURI());
        	            }
        			}
            	} else if (context.isHtmlComment()) {
            		// Try breaking out of the comment
    	            List<HtmlContext> contexts2 = performAttack (msg, param, 
    	            		"--><script>alert(1);</script><!--", context, HtmlContext.IGNORE_HTML_COMMENT);
    	            if (contexts2.size() > 0) {
	            		// Yep, its vulnerable
						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
								"", contexts2.get(0).getMsg());
						attackWorked = true;
    	            } else {
    	            	// Maybe they're blocking script tags
        	            contexts2 = performAttack (msg, param, 
			            		"--><b onMouseOver=alert(1);>test</b><!--", context, HtmlContext.IGNORE_HTML_COMMENT);
        	            if (contexts2.size() > 0) {
		            		// Yep, its vulnerable
							bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
									"", contexts2.get(0).getMsg());
							attackWorked = true;
			            }
    	            }
            	} else {
            		// its not in a tag attribute
            		if ("body".equalsIgnoreCase(context.getParentTag())) {
            			// Immediately under a body tag
        				// Try a simple alert attack
        	            List<HtmlContext> contexts2 = performAttack (msg, param, 
        	            		"<script>alert(1);</script>", null, HtmlContext.IGNORE_PARENT);
        	            if (contexts2.size() > 0) {
        	            		// Yep, its vulnerable
        						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
        								"", contexts2.get(0).getMsg());
        						attackWorked = true;
        	            } else {
        	            	// Maybe they're blocking script tags
            	            contexts2 = performAttack (msg, param, 
    			            		"<b onMouseOver=alert(1);>test</b>", context, HtmlContext.IGNORE_PARENT);
    			            for (HtmlContext context2 : contexts2) {
    			            	if ("body".equalsIgnoreCase(context2.getParentTag()) ||
    			            			"script".equalsIgnoreCase(context2.getParentTag())) {
    			            		// Yep, its vulnerable
    								bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
    										"TBI Body tag", contexts2.get(0).getMsg());
    								attackWorked = true;
    								break;
    			            	}
    			            }
        	            }
            		} else if (context.getParentTag() != null){
            			// Its not immediately under a body tag, try to close the tag
        	            List<HtmlContext> contexts2 = performAttack (msg, param, 
        	            		"</" + context.getParentTag() + "><script>alert(1);</script><" + context.getParentTag() + ">", 
        	            		context, HtmlContext.IGNORE_IN_SCRIPT);
        	            if (contexts2.size() > 0) {
       	            		// Yep, its vulnerable
       						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
       								"", contexts2.get(0).getMsg());
    						attackWorked = true;
        	            } else if ("script".equalsIgnoreCase(context.getParentTag())){
        	            	// its in a script tag...
            	            contexts2 = performAttack (msg, param, 
            	            		context.getSurroundingQuote() + ";alert(1);" + context.getSurroundingQuote(), context, 0);
            	            if (contexts2.size() > 0) {
           	            		// Yep, its vulnerable
           						bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, contexts2.get(0).getTarget(), 
           								"", contexts2.get(0).getMsg());
        						attackWorked = true;
            	            }
        	            }
            		}
            	}
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);;
        }
    }
}
