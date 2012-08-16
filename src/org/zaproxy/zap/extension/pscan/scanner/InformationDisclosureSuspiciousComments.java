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
package org.zaproxy.zap.extension.pscan.scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class InformationDisclosureSuspiciousComments extends PluginPassiveScanner {

	private PassiveScanThread parent = null;
	private static final String databaseErrorFile = "xml/suspicious-comments.txt";
	private static final Logger logger = Logger.getLogger(InformationDisclosureSuspiciousComments.class);
	
	private List<Pattern> patterns = null;
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getResponseBody().length() > 0 && msg.getResponseHeader().isText()) {
			StringBuffer todoComments = new StringBuffer();
			
			if (msg.getResponseHeader().isJavaScript()) {
				// Just treat as text
				String[] lines = msg.getResponseBody().toString().split("\n");
				for (String line : lines) {
					for (Pattern pattern : this.getPatterns()) {
						if (pattern.matcher(line).find()) {
							todoComments.append(line);
							todoComments.append("\n");
							break;	// Only need to record this line once
						}
					}
				}
			} else {
				// Can use the parser
			
				// Check the comments
				List<Tag> tags = source.getAllTags(StartTagType.COMMENT);
				for (Tag tag : tags) {
					String tagStr = tag.toString();
					for (Pattern pattern : this.getPatterns()) {
						if (pattern.matcher(tagStr).find()) {
							todoComments.append(tagStr);
							todoComments.append("\n");
							break;	// Only need to record this comment once
						}
					}
				}
				// Check the scripts
				Element el;
				int offset = 0;
				while ((el = source.getNextElement(offset, HTMLElementName.SCRIPT)) != null) {
					String elStr = el.toString();
					for (Pattern pattern : this.getPatterns()) {
						if (pattern.matcher(elStr).find()) {
							todoComments.append(elStr);
							todoComments.append("\n");
							break;	// Only need to record this script once
						}
					}
					offset = el.getEnd();
				}
			}
			if (todoComments.length() > 0) {
				this.raiseAlert(msg, id, todoComments.toString());
			}
		}
	}
	
	private void raiseAlert(HttpMessage msg, int id, String detail) {
		Alert alert = new Alert(getId(), Alert.RISK_INFO, Alert.WARNING, 
		    	getName());
		    	alert.setDetail(
		    		"The response appears to contain suspicious comments which may help an attacker", 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    "",
		    	    "", 
		    	    detail,
		    	    "Remove all comments that return information that may help an attacker and fix any underlying problems they refer to", 
		            "", 
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	private List<Pattern> getPatterns() {
		if (patterns == null) {
			patterns = new ArrayList<Pattern>();
			String line = null;
			File f = new File(databaseErrorFile);
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(f));
				while ((line = reader.readLine()) != null) {
					if (!line.startsWith("#") && line.length() > 0) {
						patterns.add(Pattern.compile("\\b" + line + "\\b", Pattern.CASE_INSENSITIVE));
					}
				}
			} catch (IOException e) {
				logger.error("Error on opening/reading database error file. File: " + f.getAbsolutePath() + " Error: " + e.getMessage());
			} finally {
				if (reader != null) {
					try {
						reader.close();			
					}
					catch (IOException e) {
						logger.debug("Error on closing the file reader. Error: " + e.getMessage());
					}
				}
			}
		}
		return patterns;
	}

	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return "Information disclosure - suspicious comments";
	}

	private int getId() {
		return 10027;
	}
}
