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

@Deprecated
public class TestCrossSiteScriptInTag extends AbstractAppParamPlugin {

	// ZAP Depreciated by org.zaproxy.zap.scanner.plugin.TestCrossSiteScriptV2
	@Override
	public boolean isDepreciated() {
		return true;
	}

    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_8");
    private static Logger log = Logger.getLogger(TestCrossSiteScriptInTag.class);

    private static final String [] XSS_ATTACKS = { 
    								"alert('" + Constant.getEyeCatcher() + "');",
    								"alert(\"" + Constant.getEyeCatcher() + "\");" 
    								};

    // Note that these patterns wont match against a tag on more than one line, so those vulnerabilities
    // wont be found yet
	private static final Pattern [] XSS_PATTERNS = {
	     Pattern.compile("<.*alert\\('" + Constant.getEyeCatcher() + "'\\);.*>", Pattern.CASE_INSENSITIVE),
	     Pattern.compile("<.*alert\\(\"" + Constant.getEyeCatcher() + "\"\\);.*>", Pattern.CASE_INSENSITIVE)
	     };

	private static final char [] QUOTES = {'\'', '"'};

    @Override
    public int getId() {
        return 40010;
    }

    @Override
    public String getName() {
    	return Constant.messages.getString("scanner.plugin.xsstag");
    }

    @Override
    public String[] getDependency() {
        return null;
    }

    @Override
    public String getDescription() {
    	if (vuln != null) {
    		return vuln.getDescription();
    	}
    	return "Failed to load vulnerability description from file";
    }

    @Override
    public int getCategory() {
        return Category.INJECTION;
    }

    @Override
    public String getSolution() {
    	if (vuln != null) {
    		return vuln.getSolution();
    	}
    	return "Failed to load vulnerability solution from file";
    }

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

    @Override
    public void init() {

    }

    @Override
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
					int offset = match.indexOf(XSS_ATTACKS[i]);
					if ((match.substring(0, offset).indexOf(">") > 0) ||
							(match.substring(offset).indexOf("<") > 0)) {
						// Possible false positive, need to enhance the check to catch edge cases
						if (this.getAlertThreshold().equals(AlertThreshold.HIGH)) {
							// Report anyway
							bingo(Alert.RISK_MEDIUM, Alert.SUSPICIOUS, null, param, XSS_ATTACKS[i], null, msg);
						}
					} else {
						if (this.getAlertThreshold().equals(AlertThreshold.LOW)) {
							// Perform extra checks to reduce false positives, even if we miss some real issues
							if (stringInQuotes (match, XSS_ATTACKS[i], QUOTES[i])) {
								continue;
							}
						}
						
						bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param, XSS_ATTACKS[i], null, msg);
						return;
						
					}
				}
	        } catch (Exception e) {
	            log.error(e.getMessage(), e);;
	        }
    	}
	}

	private boolean stringInQuotes(String match, String attack, char quote) {
		boolean falsePositive = false;
		int offset = match.indexOf(attack);
		for (int j=offset-2; j > 0; j--) {
			// Scan down, if we hit the 'other' quote before the one used assume its correctly enclosed
			if (match.charAt(j) == quote) {
				// hit the same quote, very good chance this can be abused
				return false;
			}
			if (match.charAt(j) == '\'' || match.charAt(j) == '\"') {
				// hit the 'other' quote, not looking so good
				falsePositive = true;
			}
		}
		for (int j=offset + attack.length(); j < match.length(); j++) {
			// Scan up, if we hit the 'other' quote before the one used assume its correctly enclosed
			if (match.charAt(j) == quote) {
				// hit the same quote, very good chance this can be abused
				return false;
			}
			if (match.charAt(j) == '\'' || match.charAt(j) == '\"') {
				// hit the 'other' quote, unlikely this can be abused
				return true;
			}
		}
		return falsePositive;
	}
	
	@Override
	public int getRisk() {
		return Alert.RISK_HIGH;
	}

}
