package org.zaproxy.zap.scanner.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;

public class TestCrossSiteScriptInTag extends AbstractAppParamPlugin {

    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_8");
    private static Logger log = Logger.getLogger(TestCrossSiteScriptInTag.class);

    private static final String [] XSS_ATTACKS = { 
    								"alert('" + Constant.getEyeCatcher() + "');",
    								"alert(\"" + Constant.getEyeCatcher() + "\");" 
    								};

    // Note that these patterns wont match againts a tag on more than one line, so those vulnerabilities
    // wont be found yet
	private static final Pattern [] XSS_PATTERNS = {
	     Pattern.compile("<.*alert\\('" + Constant.getEyeCatcher() + "'\\);.*>", Pattern.CASE_INSENSITIVE),
	     Pattern.compile("<.*alert\\(\"" + Constant.getEyeCatcher() + "\"\\);.*>", Pattern.CASE_INSENSITIVE)
	     };

    public int getId() {
        return 40010;
    }

    public String getName() {
    	if (vuln != null) {
    		return vuln.getAlert();
    	}
        return "Cross site scripting in TAG";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    public String getDescription() {
    	if (vuln != null) {
    		return vuln.getDescription();
    	}
    	return "Failed to load vulnerability description from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getCategory()
     */
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
    public String getSolution() {
    	if (vuln != null) {
    		return vuln.getSolution();
    	}
    	return "Failed to load vulnerability solution from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
     */
    public String getReference() {
    	if (vuln != null) {
    		StringBuffer sb = new StringBuffer();
    		for (String ref : vuln.getReferences()) {
    			if (sb.length() > 0) {
    				sb.append("\n");
    			}
    			sb.append(ref);
    		}
    		return sb.toString();
    	}
    	return "Failed to load vulnerability reference from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    public void init() {

    }

    public void scan(HttpMessage msg, String param, String value) {
		
    	for (int i=0; i < XSS_ATTACKS.length; i++) {
			try {
				setParameter(msg, param, XSS_ATTACKS[i]);
	            sendAndReceive(msg);
		        // See if the attack pattern is reflected in the output
				Matcher matcher = XSS_PATTERNS[i].matcher(msg.getResponseBody().toString());
				boolean result = matcher.find();
				if (result) {
					String match = matcher.group();
					int offset = match.indexOf(Constant.getEyeCatcher());
					if ((match.substring(0, offset).indexOf(">") > 0) ||
							(match.substring(offset).indexOf("<") > 0)) {
						// Possible false positive, need to enhance the check to catch edge cases
					} else {
						bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param + "=" + XSS_ATTACKS[i], null, msg);
						return;
						
					}
				}
	        } catch (Exception e) {
	            log.error(e.getMessage(), e);;
	        }
    	}
	}
}
