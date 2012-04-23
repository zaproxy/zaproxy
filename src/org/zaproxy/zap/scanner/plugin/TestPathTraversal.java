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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;

public class TestPathTraversal extends AbstractAppParamPlugin {

	private static final String [] TARGETS = {
		// Linux
		"/etc/passwd",
		"../../../../../../../../../../../../../../../../etc/passwd",
		"..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2f..%2fetc%2fpasswd",
		"%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f" + 
				"%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd",
		// Windows
		"%5cWindows%5csystem.ini",
		"..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5c..%5cWindows%5csystem.ini",
		"%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c" +
				"%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5cWindows%5csystem.ini",
	};
	
	private static final String [] PATTERNS = {
		// Linux
		"root:.:0:0",		// Dot used to match 'x' or '!' (used in AIX)
		"root:.:0:0",
		"root:.:0:0",
		"root:.:0:0",
		// Windows
		"\\[drivers\\]",
		"\\[drivers\\]",
		"\\[drivers\\]",
	};

    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_33");
    private static Logger log = Logger.getLogger(TestPathTraversal.class);

    @Override
    public int getId() {
        return 6;
    }

    @Override
    public String getName() {
    	if (vuln != null) {
    		return vuln.getAlert();
    	}
        return "Path traversal";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    @Override
    public String getDescription() {
    	if (vuln != null) {
    		return vuln.getDescription();
    	}
    	return "Failed to load vulnerability description from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getCategory()
     */
    @Override
    public int getCategory() {
        return Category.SERVER;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
    @Override
    public String getSolution() {
    	if (vuln != null) {
    		return vuln.getSolution();
    	}
    	return "Failed to load vulnerability solution from file";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
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
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    @Override
    public void init() {

    }

    @Override
    public void scan(HttpMessage msg, String param, String value) {
	
		try {
	        Matcher matcher = null;

			for (int i=0; i < TARGETS.length; i++) {
				msg = msg.cloneRequest();
				setEscapedParameter(msg, param, TARGETS[i]);
	            sendAndReceive(msg);
				String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
	            matcher = Pattern.compile(PATTERNS[i]).matcher(response);
	            if (matcher.find()) {
	                bingo(Alert.RISK_HIGH, Alert.WARNING, "", param, null, TARGETS[i] , msg);
	                break;
	            }
			}
			
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}
}
