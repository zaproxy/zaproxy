/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zaproxy.zap.spider.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderSVNEntriesParser is used for parsing SVN "entries" files.
 * @author 70pointer
 *
 */
public class SpiderSVNEntriesParser extends SpiderParser {
	 /* this class was Cloned from SpiderRobotstxtParser, by Cosmin. Credit where credit is due. */

	/** matches the entry *after* the line containing the file name */
	private static final Pattern urlPattern = Pattern.compile("^(file|dir)$"); //case sensitive
		
	/** The Spider parameters. */
	private SpiderParam params;

	/**
	 * Instantiates a new spider SVN entries parser.
	 * 
	 * @param params the params
	 */
	public SpiderSVNEntriesParser(SpiderParam params) {
		super();
		this.params = params;
	}

	@Override
	public void parseResource(HttpMessage message, Source source, int depth) {
		if (message == null || !params.isParseSVNEntries()) {
			return;
		}

		// Get the response content
		String content = message.getResponseBody().toString();

		// Get the context (base url)
		String baseURL = message.getRequestHeader().getURI().toString();
		String previousline = null;		

		// Parse each line in the ".svn/entries" file
		//we cannot use the StringTokenizer approach used by the robots.txt logic above, 
		//since this causes empty lines to be ignored, which causes problems...
		
		String [] lines = content.split("\n"); 
		for (String line : lines ) {
			// If the line is empty, skip it
			if (line.length() > 0) {
				
				//log.debug("Processing SVN entries line: " + line);
				
				Matcher matcher = urlPattern.matcher(line);
				if (matcher.find()) {
					//filetype is "dir" or "file", as per the contents of the SVN file.
					String filetype  = matcher.group(0);
					//the previous line actually contains the file/directory name.
					if ( previousline != null && previousline.length() > 0 )
						log.debug("Found a file/directory name in the SVN entries file");
					
						processURL(message, depth, "../" + previousline + (filetype.equals("dir")?"/":""), baseURL);
						
						//re-seed the spider for this directory. 
						if ( filetype.equals("dir") ) {
							processURL(message, depth, "../" + previousline + "/.svn/entries", baseURL);
						}
						
				} 
			}
			//last thing to do is to record the line as the previous line for the next iteration.
			previousline = line;
		}
		return;
	}
}
