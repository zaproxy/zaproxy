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

import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;

/**
 * 
 * a scanner that looks for Path Traversal vulnerabilities
 *
 */
public class TestPathTraversal extends AbstractAppPlugin {

	/**
	 * the various (prioritised) prefixes to try, for each of the local file targets below
	 */
	private static final String [] LOCAL_FILE_TARGET_PREFIXES = {
		"/",
		"\\",
		"/../../",
		"../../../../../../../../../../../../../../../..",
		"",
		"/../../../../../../../../../../../../../../../../../",
		"\\..\\..\\",
		"..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..",
		"\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\",
		"./",
		"../",
		"../../",
		"/..",
		"/../",
		"/../..",
		"/./",
		"\\",
		".\\",
		"..\\",
		"..\\..\\",
		"\\..",
		"\\..\\",
		"\\..\\..",
		"\\.\\",
		"file://",
		"fiLe://",
		"file:",
		"fiLe:",
		"FILE:",
		"FILE://"
	};
	
	/**
	 * the various (prioritised) local file targets to look for (prefixed by the prefixes above)
	 */
	private static final String [] LOCAL_FILE_TARGETS = {
		"etc/passwd",
		"etc\\passwd",
		"Windows/system.ini",
		"Windows\\system.ini",
		"WEB-INF/web.xml",
		"WEB-INF\\web.xml"
	};
	
	/**
	 * the patterns to look for, associated with the equivalent local file targets above
	 */
	private static final String [] LOCAL_FILE_PATTERNS = {
		// Linux / Unix
		"root:.:0:0",		// Dot used to match 'x' or '!' (used in AIX)
		"root:.:0:0",
		// Windows
		"\\[drivers\\]",
		"\\[drivers\\]",
		//Web App
		"</web-app>",
		"</web-app>"
	};
	
		
	/**
	 * the various prefixes to try, for each of the remote file targets below
	 */
	private static final String [] REMOTE_FILE_TARGET_PREFIXES = {
		"http://", "", "HTTP://", "https://", "HTTPS://", "HtTp://", "HtTpS://"
	};
	
	/**
	 * the various local file targets to look for (prefixed by the prefixes above)
	 */
	private static final String [] REMOTE_FILE_TARGETS = {
		"www.google.com/",
		"www.google.com:80/",
		"www.google.com",
		"www.google.com/search?q=OWASP%20ZAP",
		"www.google.com:80/search?q=OWASP%20ZAP"
	};
	
	/**
	 * the patterns to look for, associated with the equivalent remote file targets above
	 */
	private static final String [] REMOTE_FILE_PATTERNS = {
		"I'm Feeling Lucky",
		"I'm Feeling Lucky",
		"I'm Feeling Lucky",
		"OWASP ZAP - Google Search", //do not use "OWASP Zed Attack Proxy", as it causes false positives!
		"OWASP ZAP - Google Search"	 //do not use "OWASP Zed Attack Proxy", as it causes false positives!		
	};
	

	/**
	 * details of the vulnerability which we are attempting to find
	 */
    private static Vulnerability vuln = Vulnerabilities.getVulnerability("wasc_33");
    
    /**
     * the logger object
     */
    private static Logger log = Logger.getLogger(TestPathTraversal.class);

    /**
     * returns the plugin id 
     */
    @Override
    public int getId() {
        return 6;
    }

    /**
     * returns the name of the plugin
     */
    @Override
    public String getName() {
    	if (vuln != null) {
    		return vuln.getAlert();
    	}
        return "Path traversal";
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
        return Category.SERVER;
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

    /**
     * scans all GET and POST parameters for Path Traversal vulnerabilities
     * TODO: consider looking in header fields and cookies as well
     */
    @Override
    public void scan () {
    
    	try {
    		//figure out how aggressively we should test
    		//this will be measured in the number of requests we send for each parameter
    		//we will send approx 5 requests per parameter at AttackStrength.LOW
    		//we will send approx 10 requests per parameter at AttackStrength.MEDIUM
    		//we will send approx 20 requests per parameter at AttackStrength.HIGH
    		//we will send loads (a finite number though) of requests per parameter at AttackStrength.INSANE
    		int prefixCountLFI = 0;
    		int prefixCountRFI = 0;
    		int prefixCountOurUrl = 0;
    		
    		//DEBUG only
    		//this.setAttackStrength(AttackStrength.INSANE);
    		
    		if (log.isDebugEnabled()) log.debug("Attacking at Attack Strength: "+ this.getAttackStrength());
    		
    		if ( this.getAttackStrength() == AttackStrength.LOW) {
    			//Low => (5*1) + (5*0) + (5*0) = 5 requests
    			prefixCountLFI = 1;  //check for 1 prefix on the local file names
    			prefixCountRFI = 0;  //do not check for remote file includes
    			prefixCountOurUrl = 0;  //do not check for our url filename as a file to be included
    		} else if ( this.getAttackStrength() == AttackStrength.MEDIUM) {
    			//Medium => (5*1) + (5*1) + (5*0) = 10 requests
    			prefixCountLFI = 1;  //check for 1 prefix on the local file names
    			prefixCountRFI = 1;  //check for 1 prefix on the remote file names
    			prefixCountOurUrl = 0;  //do not check for our url filename as a file to be included    			
    		} else if ( this.getAttackStrength() == AttackStrength.HIGH) {
    			//High => (5*2) + (5*1) + (5*1) = 20 requests
    			prefixCountLFI = 2;  //check for 2 prefixes on the local file names
    			prefixCountRFI = 1;  //check for 1 prefix on the remote file names
    			prefixCountOurUrl = 1;  //check for 1 prefix on our url filename as a file to be included
    			
    		} else if ( this.getAttackStrength() == AttackStrength.INSANE) {
    			//Insane  => as many requests as we want.. yee-haa!
    			prefixCountLFI = LOCAL_FILE_TARGET_PREFIXES.length;  //check for all prefixes on the local file names
    			prefixCountRFI = REMOTE_FILE_TARGET_PREFIXES.length;  //check for all prefixes on the remote file names
    			prefixCountOurUrl = LOCAL_FILE_TARGET_PREFIXES.length;  //check for all prefixes on our url filename as a file to be included
    		}
    		
	    	//find all params set in the request (GET/POST)
	    	TreeSet<HtmlParameter> htmlParams = new TreeSet<> ();
	    	htmlParams.addAll(getBaseMsg().getUrlParams()); //add in the GET params
			htmlParams.addAll(getBaseMsg().getFormParams());  //add in the POST params
			
			//for each parameter in turn.. 
			for (Iterator<HtmlParameter> iter = htmlParams.iterator(); iter.hasNext(); ) {
				Matcher matcher = null;
				HttpMessage msg = null;
				HtmlParameter currentHtmlParameter = iter.next();
				
				if (log.isDebugEnabled()) log.debug("Checking ["+getBaseMsg().getRequestHeader().getMethod() + "] [" + getBaseMsg().getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] for Path Traversal to local files");
			    
				//for each local prefix in turn
				//note that depending on the AttackLevel, the number of prefixes that we will try changes.
		        for (int h=0; h < prefixCountLFI; h++) {
		        	String prefix=LOCAL_FILE_TARGET_PREFIXES[h];
		        	//for each target in turn
		        	//note: regardless of the specified Attack Strength, we want to try all files name here 
		        	//(just for a limited number of prefixes)
					for (int i=0; i < LOCAL_FILE_TARGETS.length; i++) {
						String target=LOCAL_FILE_TARGETS[i];
						
						//get a new copy of the original message (request only) for each parameter value to try
						msg = getNewMsg();
										
						if (log.isDebugEnabled()) log.debug("Checking ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] for Path Traversal (local file) with value ["+ prefix+target+"]");
				        	        				        	
			        	if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.url)) {
			        		//GET parameter
			        		TreeSet <HtmlParameter> params = msg.getUrlParams();
			        		params.remove(currentHtmlParameter);
			        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), prefix+target));
							msg.setGetParams(params); //restore the params
			        	} else if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.form)) {
			        		//POST parameter
			        		TreeSet <HtmlParameter> params = msg.getFormParams();
			        		params.remove(currentHtmlParameter);
			        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), prefix+target));
							msg.setFormParams(params); //restore the params
			        	} else {
			        		throw new Exception ("Unsupported parameter type ["+currentHtmlParameter.getType()+ "] for param ["+ currentHtmlParameter.getName()+ "] on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"]");
			        	}
			        	//send the modified request, and see what we get back
			        	sendAndReceive(msg);
			        	//does it match the pattern specified for that file name?
						String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
			            matcher = Pattern.compile(LOCAL_FILE_PATTERNS[i]).matcher(response);
			            //if the output matches, and we get a 200
			            if (matcher.find() && msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
			            	log.info("Path Traversal (local file) on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] with value ["+ prefix+target+"]");
			                bingo(Alert.RISK_HIGH, Alert.WARNING, "", "["+currentHtmlParameter.getType() + "] "+ currentHtmlParameter.getName(), msg.getRequestHeader().getMethod() + " " + msg.getRequestHeader().getURI(), prefix+target , msg);
			                return;  //all done. No need to look for vulnerabilities on subsequent parameters on the same request (to reduce performance impact) 
			            }
					}
		        }
		        //Check 2: try a local file Path Traversal on the file name of the URL (which obviously will not be in the target list above).
		        //first send a query for a random parameter value, and see if we get a 200 back
		        //if 200 is returned, abort this check (on the url filename itself), because it would be unreliable.
		        //if we know that a random query returns <> 200, then a 200 response likely means something!
		        //this logic is all about avoiding false positives, while still attempting to match on actual vulnerabilities
				msg = getNewMsg();
				if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.url)) {
	        		//GET parameter
	        		TreeSet <HtmlParameter> params = msg.getUrlParams();
	        		params.remove(currentHtmlParameter);
	        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), "thishouldnotexistandhopefullyitwillnot"));
					msg.setGetParams(params); //restore the params
	        	} else if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.form)) {
	        		//POST parameter
	        		TreeSet <HtmlParameter> params = msg.getFormParams();
	        		params.remove(currentHtmlParameter);
	        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), "thishouldnotexistandhopefullyitwillnot"));
					msg.setFormParams(params); //restore the params
	        	} else {
	        		throw new Exception ("Unsupported parameter type ["+currentHtmlParameter.getType()+ "] for param ["+ currentHtmlParameter.getName()+ "] on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"]");
	        	}
				//send the modified message (with a hopefully non-existent filename), and see what we get back
	            sendAndReceive(msg);
	            
	            //do some pattern matching on the results.
	            Pattern exceptionPattern = Pattern.compile("Exception");
	            Matcher exceptionMatcher = exceptionPattern.matcher(msg.getResponseBody().toString());
	            Pattern errorPattern = Pattern.compile("Error");
	            Matcher errorMatcher = errorPattern.matcher(msg.getResponseBody().toString());
	        	
	            if ( msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK 
	            			|| exceptionMatcher.find() 
	            			|| errorMatcher.find() ) {
	            	if (log.isDebugEnabled()) log.debug("It IS possible to check for local file Path Traversal on the url filename on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "]");
	            	String urlfilename = msg.getRequestHeader().getURI().getName();
	            	
	            	//for the url filename, try each of the prefixes in turn
	            	for (int h=0; h < prefixCountOurUrl; h++) {
	            		String prefixedUrlfilename = LOCAL_FILE_TARGET_PREFIXES[h]+urlfilename;
	            		msg = getNewMsg();
						//setParameter(msg, param, prefixedUrlfilename);
						if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.url)) {
			        		//GET parameter
			        		TreeSet <HtmlParameter> params = msg.getUrlParams();
			        		params.remove(currentHtmlParameter);
			        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), prefixedUrlfilename));
							msg.setGetParams(params); //restore the params
			        	} else if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.form)) {
			        		//POST parameter
			        		TreeSet <HtmlParameter> params = msg.getFormParams();
			        		params.remove(currentHtmlParameter);
			        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), prefixedUrlfilename));
							msg.setFormParams(params); //restore the params
			        	} else {
			        		throw new Exception ("Unsupported parameter type ["+currentHtmlParameter.getType()+ "] for param ["+ currentHtmlParameter.getName()+ "] on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"]");
			        	}
						//send the modified message (with the url filename), and see what we get back
			            sendAndReceive(msg);
			            
			            //did we get an Exception or an Error?
			            exceptionMatcher = exceptionPattern.matcher(msg.getResponseBody().toString());
			            errorMatcher = errorPattern.matcher(msg.getResponseBody().toString());
			            
			            if ( msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK
			            		&& ( ! exceptionMatcher.find()) 
		            			&& ( ! errorMatcher.find())
		            			) {
							//if it returns OK, and the random string above did NOT return ok, then raise an alert
				            //since the filename has likely been picked up and used as a file name from the parameter
			            	log.info("Path Traversal (local file) on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] with value ["+ prefixedUrlfilename+"]");
				            bingo(Alert.RISK_HIGH, Alert.WARNING, "", "["+currentHtmlParameter.getType() + "] "+ currentHtmlParameter.getName(), msg.getRequestHeader().getMethod() + " " + msg.getRequestHeader().getURI(), prefixedUrlfilename , msg);
				            return;  //all done. No need to look for vulnerabilities on subsequent parameters on the same request (to reduce performance impact)
			            }
	            	}
	            }
	            
	            
	            //Check 3 for local file names
	            //TODO: consider making this check 1, for performance reasons
		        //TODO: if the original query was http://www.example.com/a/b/c/d.jsp?param=paramvalue
		        //then check if the following gives comparable results to the original query
		        //http://www.example.com/a/b/c/d.jsp?param=../c/paramvalue
	            //if it does, then we likely have a local file Path Traversal vulnerability
	            //this is nice because it means we do not have to guess any file names, and would only require one
	            //request to find the vulnerability 
	            //but it would be foiled by simple input validation on "..", for instance.
	            
	            
	            //Now check for Remote files.
				if (log.isDebugEnabled()) log.debug("Checking ["+getBaseMsg().getRequestHeader().getMethod() + "] [" + getBaseMsg().getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] for Path Traversal to remote files");
			    
				//for each prefix in turn
		        for (int h=0; h < prefixCountRFI; h++) {
		        	String prefix=REMOTE_FILE_TARGET_PREFIXES[h];
		        	//for each target in turn
					for (int i=0; i < REMOTE_FILE_TARGETS.length; i++) {
						String target=REMOTE_FILE_TARGETS[i];
						
						//get a new copy of the original message (request only) for each parameter value to try
						msg = getNewMsg();
										
						if (log.isDebugEnabled()) log.debug("Checking ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] for remote file Path Traversal with value ["+ prefix+target+"]");
				        	        				        	
			        	if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.url)) {
			        		//GET parameter
			        		TreeSet <HtmlParameter> params = msg.getUrlParams();
			        		params.remove(currentHtmlParameter);
			        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), prefix+target));
							msg.setGetParams(params); //restore the params
			        	} else if ( currentHtmlParameter.getType().equals (HtmlParameter.Type.form)) {
			        		//POST parameter
			        		TreeSet <HtmlParameter> params = msg.getFormParams();
			        		params.remove(currentHtmlParameter);
			        		params.add(new HtmlParameter(currentHtmlParameter.getType(), currentHtmlParameter.getName(), prefix+target));
							msg.setFormParams(params); //restore the params
			        	} else {
			        		throw new Exception ("Unsupported parameter type ["+currentHtmlParameter.getType()+ "] for param ["+ currentHtmlParameter.getName()+ "] on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"]");
			        	}
			        	//send the modified request, and see what we get back
			        	sendAndReceive(msg);
			        	//does it match the pattern specified for that file name?
						String response = msg.getResponseHeader().toString() + msg.getResponseBody().toString();
			            matcher = Pattern.compile(REMOTE_FILE_PATTERNS[i]).matcher(response);
			            //if the output matches, and we get a 200
			            if (matcher.find() && msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
			            	log.info("Path Traversal (remote file) on ["+msg.getRequestHeader().getMethod() + "] [" + msg.getRequestHeader().getURI() +"], ["+ currentHtmlParameter.getType()+"] parameter ["+ currentHtmlParameter.getName() + "] with value ["+ prefix+target+"]");
			                bingo(Alert.RISK_HIGH, Alert.WARNING, "", "["+currentHtmlParameter.getType() + "] "+ currentHtmlParameter.getName(), msg.getRequestHeader().getMethod() + " " + msg.getRequestHeader().getURI(), prefix+target , msg);
			                return;  //all done. No need to look for vulnerabilities on subsequent parameters on the same request (to reduce performance impact) 
			            }
					}
		        }
			}
    	}
		catch (Exception e) {
			log.error("Error scanning parameters for Path Traversal: "+ e.getMessage());
			return;
		}
    }    
}
