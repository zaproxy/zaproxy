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
package org.zaproxy.zap.spider.parser;

import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.URLCanonicalizer;

/**
 * The Class SpiderHtmlParser is used for parsing of HTML files, gathering resource urls from them.
 */
public class SpiderHtmlParser extends SpiderParser {

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.zaproxy.zap.spider.parser.SpiderParser#parseResource(org.parosproxy.paros.network.HttpMessage
	 * , net.htmlparser.jericho.Source) */
	@Override
	public void parseResource(HttpMessage message, Source source) {

		List<Element> elements=source.getAllElements(HTMLElementName.A);
		for(Element el:elements)
		{
			log.warn("Found A element: "+el.getAttributeValue("href"));
			log.warn("Complete URL: "+URLCanonicalizer.getCanonicalURL(el.getAttributeValue("href"), "http://www.prosc.ro"));
			//TODO: work work work
			//this.notifyListenersResourceFound(null, URLCanonicalizer.getCanonicalURL(el.getAttributeValue("href"), "http://www.prosc.ro"));
		}
	}

}
