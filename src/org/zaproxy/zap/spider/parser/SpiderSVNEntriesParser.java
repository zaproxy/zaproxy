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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderSVNEntriesParser is used for parsing SVN "entries" files.
 * @author 70pointer
 *
 */
public class SpiderSVNEntriesParser extends SpiderParser {
	 /* this class was Cloned from SpiderRobotstxtParser, by Cosmin. Credit where credit is due. */
	
	/** a pattern to match for XML based entries files */
	private static final Pattern svnXMLFormatPattern = Pattern.compile("<wc-entries");

	/** matches the entry *after* the line containing the file name */
	private static final Pattern svnTextFormatFileOrDirectoryPattern = Pattern.compile("^(file|dir)$"); //case sensitive
		
	/** The Spider parameters. */
	private SpiderParam params;
	
	/** used to parse the XML based .svn/entries file format */ 
	private static DocumentBuilderFactory dbFactory;
	
	/** used to parse the XML based .svn/entries file format */ 
	private static DocumentBuilder dBuilder;
	
	/** statically initialise the XML DocumentBuilderFactory and DocumentBuilder */
	static {
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.error(e);
		}
	}

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
		
		
		//there are (at least) 2 formats of ".svn/entries" file. 
		//An XML version is used up to (and including) SVN 1.2
		//from SVN 1.3, a text based version is used.
		//In fact, the ".svn/entries" file disappeared in SVN 1.6.something, in favour of 
		//a file called ".svn/wc.db" sqlite database. But i digress..
		
		//which format are we dealing with? XML or text based?
		Matcher svnXMLFormatMatcher = svnXMLFormatPattern.matcher(content);
		if (svnXMLFormatMatcher.find()) {
			//XML format is being used, ( < SVN 1.3)
			Document doc;
			try {
				//work around the "no protocol" issue by wrapping the content in a ByteArrayInputStream
				doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(content.getBytes("utf-8"))));
			} catch (SAXException | IOException e) {
				log.error("An error occurred trying to parse the XML based .svn/entries file: "+ e.getMessage());
				return;
			}
			NodeList nodelist = doc.getElementsByTagName("entry");
			for ( int i=0; i< nodelist.getLength(); i++) {
				Node svnEntryNode = nodelist.item(i);				
				String svnEntryName = ((Element)svnEntryNode).getAttribute("name");
				String svnEntryKind = ((Element)svnEntryNode).getAttribute("kind");
				
				if ( svnEntryName != null && svnEntryName.length() > 0 )
					log.debug("Found a file/directory name in the (XML based) SVN < 1.3 entries file");
				
					processURL(message, depth, "../" + svnEntryName + (svnEntryKind.equals("dir")?"/":""), baseURL);
					
					//re-seed the spider for this directory. 
					if ( svnEntryKind.equals("dir") ) {
						processURL(message, depth, "../" + svnEntryName + "/.svn/entries", baseURL);
					}
			}
		}
		else	{			
			//text based format us being used, so >= SVN 1.3 (but not later than SVN 1.6.something)
			// Parse each line in the ".svn/entries" file
			//we cannot use the StringTokenizer approach used by the robots.txt logic, 
			//since this causes empty lines to be ignored, which causes problems...
			String previousline = null;	
			String [] lines = content.split("\n"); 
			for (String line : lines ) {
				// If the line is empty, skip it
				if (line.length() > 0) {
					
					//log.debug("Processing SVN entries line: " + line);
					
					Matcher matcher = svnTextFormatFileOrDirectoryPattern.matcher(line);
					if (matcher.find()) {
						//filetype is "dir" or "file", as per the contents of the SVN file.
						String filetype  = matcher.group(0);
						//the previous line actually contains the file/directory name.
						if ( previousline != null && previousline.length() > 0 )
							log.debug("Found a file/directory name in the (text based) SVN 1.3/1.4/1.5/1.6 SVN entries file");
						
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
		}
		return;
	}
}
