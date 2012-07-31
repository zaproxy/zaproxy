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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.extension.encoder.Base64;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

/**
 * a passive scanner that checks for the use of weak authentication methods such as Basic Authentication and Digest Authentication
 */
public class WeakAuthenticationScanner extends PluginPassiveScanner {

	/**
	 * the Passive Scanner Thread
	 */
	private PassiveScanThread parent = null;

	/**
	 * for logging.
	 */
	private static Logger log = Logger.getLogger(WeakAuthenticationScanner.class);

	/**
	 * determines if we should output Debug level logging
	 */
	private boolean debugEnabled = log.isDebugEnabled(); 

	/**
	 * gets the internationalised message corresponding to the key
	 * @param key the key to look up the internationalised message
	 * @return the internationalised message corresponding to the key
	 */
	public String getString(String key) {
		try {
			return Constant.messages.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * gets the internationalised message corresponding to the key, using the parameters supplied
	 * @param key the key to look up the internationalised message
	 * @param params the parameters used to internationalise the message
	 * @return the internationalised message corresponding to the key, using the parameters supplied
	 */
	public String getString(String key, Object... params  ) {
		try {
			return MessageFormat.format(Constant.messages.getString(key), params);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {

		String uri = null, method = null;
		String extraInfo = null;  //value depends on which method is being used.
		String digestInfo = null;

		//DEBUG only
		//log.setLevel(org.apache.log4j.Level.DEBUG);
		//this.debugEnabled = true;

		if (msg.getRequestHeader().getSecure()) {
			// If SSL is used then the use of 'weak' authentication methods isn't really an issue
			return;
		}

		//get the URI
		try {
			uri = msg.getRequestHeader().getURI().getURI().toString();
			method = msg.getRequestHeader().getMethod();
		}
		catch (Exception e) {
			log.error("Error getting URI from message ["+msg+"]");
			return;
		}

		//get the authorisation headers from the request, and process them
		Vector<String> headers = msg.getRequestHeader().getHeaders(HttpHeader.AUTHORIZATION);
		if (headers != null) {	
			for (Iterator<String> i = headers.iterator(); i.hasNext();) {
				String authHeaderValue = i.next();
				String authMechanism = null;
				if (    ( authHeaderValue.toLowerCase(Locale.ENGLISH).startsWith("basic ")) ||
						( authHeaderValue.toLowerCase(Locale.ENGLISH).startsWith("digest ")) )
				{				
					int alertRisk=Alert.RISK_MEDIUM; //Medium by default.. maybe even high. See later code!
					int alertLevel = Alert.WARNING; 
					String username=null, password=null;

					//gets Basic or Digest.. (trailing spaces trimmed off)
					authMechanism = authHeaderValue.substring(0, 6).trim();

					//Handle Basic Auth
					if ( authMechanism.toLowerCase(Locale.ENGLISH).equals ("basic") ) {
						//Basic authentication... the username and password are merely base64 encoded and 
						//sent over the wire.. not good!
						String [] authValues = authHeaderValue.split(" ");  //do NOT convert to lowercase for the split.. will corrupt the base64 data
						if (authValues.length == 2) { 
							if ( this.debugEnabled ) log.debug(authMechanism +" Authentication Value: "+ authValues[1]);
							//now decode it from base64 into the username and password
							try {
								String decoded  = new String (Base64.decode(authValues[1]));
								if ( this.debugEnabled ) log.debug("Decoded Base64 value: "+ decoded);
								String [] usernamePassword = decoded.split(":", 2);
								if (usernamePassword.length > 1) {
									username=usernamePassword[0];
									password=usernamePassword[1];												
								} else {
									//no password to be had.. use the entire decoded string as the username
									username=decoded;
								}
								if ( password != null) {
									alertRisk=Alert.RISK_HIGH;
								}
							} 
							catch (IOException e) {
								log.error("Invalid Base64 value for "+authMechanism+" Authentication: "+ authValues[1]);
							}
						}
						else {
							//malformed Basic Auth header?? warn, but ignore
							if ( this.debugEnabled ) log.debug("Malformed "+authMechanism+" Authentication Header: ["+ authHeaderValue + "], "+ authValues.length + " values found");
							continue; //to the next header
						}
						extraInfo = getString("authenticationcredentialscaptured.alert.basicauth.extrainfo", 
								method, uri, authMechanism, username, password);
					}

					//Handle Digest Auth
					if ( authMechanism.toLowerCase(Locale.ENGLISH).equals ("digest") ) {
						alertRisk=Alert.RISK_MEDIUM;  //not as high as for Basic Auth, but worth raising as an issue.

						//Digest authentication... the username is in plaintext, and the password is hashed
						String [] authValues = authHeaderValue.split(" ", 2);  //do NOT convert to lowercase for the split.. will corrupt the base64 data
						if (authValues.length == 2) {
							if ( this.debugEnabled ) log.debug(authMechanism +" Authentication Value: "+ authValues[1]);
							//now grab the username from the string	    				
							Pattern pattern = Pattern.compile(".*username=\"([^\"]+)\".*");
							Matcher matcher = pattern.matcher(authValues[1]);
							if (matcher.matches()) {
								username = matcher.group(1);
							} else {
								//no username in the Digest??
								if ( this.debugEnabled ) log.debug("Malformed "+authMechanism+" Authentication Header: ["+ authHeaderValue + "]. No username was found");
								continue; //to the next header..
							}	    					
						}
						else {
							//malformed Digest Auth header?? warn, but ignore
							if ( this.debugEnabled ) log.debug("Malformed "+authMechanism+" Authentication Header: ["+ authHeaderValue + "], "+ authValues.length + " values found");
							continue; //to the next header
						}

						extraInfo = getString("authenticationcredentialscaptured.alert.digestauth.extrainfo", 
								method, uri, authMechanism, username, authValues[1]);

						digestInfo=authValues[1]; //info to output in the logging message.
					}

					//raise the alert, now that we have all the detail on it.
					Alert alert = new Alert(getId(), alertRisk, alertLevel, getString("authenticationcredentialscaptured.name"));
					alert.setDetail(getString("authenticationcredentialscaptured.desc"), 
							uri,
							"",  //No specific parameter. It's in the header.
							getString("authenticationcredentialscaptured.alert.attack"), 
							extraInfo, 
							getString("authenticationcredentialscaptured.soln"), 
							getString("authenticationcredentialscaptured.refs"), 
							msg);
					//raise the alert
					parent.raiseAlert(id, alert);

					//and log it, without internationalising it.
					log.info("Authentication Credentials were captured. ["+method+"] ["+uri+"] uses insecure authentication mechanism ["+authMechanism+"], revealing username ["+username+"] and password/additional information ["+((digestInfo!=null)?digestInfo:password)+"]");
				} //basic or digest authorisation
			} //end of authorization headers
		} //end of headers null check
	} //end of method

	/**
	 * returns the id of the extension
	 * @return
	 */
	private int getId() {
		return 10013;  
	}

	@Override
	public String getName() {
		return getString("weakauthentication.name");
	}

	/* (non-Javadoc)
	 * @see com.proofsecure.paros.core.scanner.Test#getDescription()
	 */
	public String getDescription() {
		return getString("weakauthentication.desc");
	}

	/* (non-Javadoc)
	 * @see com.proofsecure.paros.core.scanner.Test#getCategory()
	 */
	public int getCategory() {
		return Category.INFO_GATHER;  //leaking username or username + password.. therefore information gathering
	}

	/* (non-Javadoc)
	 * @see com.proofsecure.paros.core.scanner.Test#getSolution()
	 */
	public String getSolution() {
		return getString("weakauthentication.soln");
	}

	/* (non-Javadoc)
	 * @see com.proofsecure.paros.core.scanner.Test#getReference()
	 */
	public String getReference() {
		return getString("weakauthentication.refs");  
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getRequestHeader().getSecure()) {
			// If SSL is used then the use of 'weak' authentication methods isnt really an issue	
			return;
		}
		Vector<String> authHeaders = msg.getResponseHeader().getHeaders(HttpHeader.WWW_AUTHENTICATE);
		if (authHeaders != null) {
			for (String auth : authHeaders) {
				if (auth.toLowerCase().indexOf("basic") > -1 || auth.toLowerCase().indexOf("digest") > -1) {
					Alert alert = new Alert(getId(), Alert.RISK_MEDIUM, Alert.WARNING,
							getName());
					alert.setDetail(getDescription(),
							msg.getRequestHeader().getURI().toString(),
							"", HttpHeader.WWW_AUTHENTICATE + ": " + auth,
							"",
							getSolution(),
							getReference(),
							msg);
					parent.raiseAlert(id, alert);
				}
			}
		}
	}
}
