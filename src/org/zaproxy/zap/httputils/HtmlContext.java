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
package org.zaproxy.zap.httputils;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.network.HttpMessage;

public class HtmlContext {
	
	public static final int IGNORE_PARENT 		= 0x0001;
	public static final int IGNORE_TAG 			= 0x0002;
	public static final int IGNORE_QUOTES		= 0x0004;
	public static final int IGNORE_IN_SCRIPT	= 0x0008;
	public static final int IGNORE_IN_URL		= 0x0010;
	public static final int IGNORE_WITH_SRC		= 0x0020;
	public static final int IGNORE_HTML_COMMENT	= 0x0040;

	private HttpMessage msg;
	private String target;
	private int start = 0;
	private int end = 0;
	private List<String> parentTags = new ArrayList<>();
	private String tagAttribute = null;
	private boolean inScriptAttribute = false;
	private boolean inUrlAttribute = false;
	private boolean inTagWithSrc = false;
	private String surroundingQuote = "";
	private boolean htmlComment = false;
	
	public HtmlContext(HttpMessage msg, String target, int start, int end) {
		super();
		this.msg = msg;
		this.target = target;
		this.start = start;
		this.end = end;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public HttpMessage getMsg() {
		return msg;
	}
	public String getTarget() {
		return target;
	}
	public List<String> getParentTags() {
		return parentTags;
	}
	public void setParentTags(List<String> suroundingTags) {
		this.parentTags = suroundingTags;
	}
	public String getSurroundingQuote() {
		return surroundingQuote;
	}
	public void setSurroundingQuote(String surroundingQuote) {
		this.surroundingQuote = surroundingQuote;
	}
	public String getTagAttribute() {
		return tagAttribute;
	}
	public void setTagAttribute(String tagAttribute) {
		this.tagAttribute = tagAttribute;
	}
	public void addParentTag(String name) {
		parentTags.add(0, name);
	}
	public String getParentTag() {
		if (parentTags.size() > 0) {
			return parentTags.get(parentTags.size()-1);
		}
		return null;
	}
	public boolean isInScriptAttribute() {
		return inScriptAttribute;
	}
	public void setInScriptAttribute(boolean inScriptAttribute) {
		this.inScriptAttribute = inScriptAttribute;
	}
	public boolean isHtmlComment() {
		return htmlComment;
	}
	public void setHtmlComment(boolean htmlComment) {
		this.htmlComment = htmlComment;
	}
	public boolean isInUrlAttribute() {
		return inUrlAttribute;
	}
	public void setInUrlAttribute(boolean inUrlAttribute) {
		this.inUrlAttribute = inUrlAttribute;
	}
	public boolean isInTagWithSrc() {
		return inTagWithSrc;
	}
	public void setInTagWithSrc(boolean inTagWithSrc) {
		this.inTagWithSrc = inTagWithSrc;
	}
	public boolean matches(HtmlContext context, int ignoreFlags) {

		if (context == null) {
			return false;
		}
		if ((ignoreFlags ^ IGNORE_TAG) > 0) {
			// check the tag
			if (this.tagAttribute != null) {
				if (!this.tagAttribute.equals(context.getTagAttribute())) {
					return false;
				}
			} else {
				if (context.getTagAttribute() != null) {
					return false;
				}
			}
		}
		if ((ignoreFlags ^ IGNORE_QUOTES) > 0) {
			// check the quotes
			if (this.surroundingQuote != null) {
				if (!this.surroundingQuote.equals(context.getSurroundingQuote())) {
					return false;
				}
			} else {
				if (context.getSurroundingQuote() != null) {
					return false;
				}
			}
		}
		if ((ignoreFlags ^ IGNORE_PARENT) > 0) {
			// check the parents
			if (this.getParentTag() != null) {
				if (!this.getParentTag().equals(context.getParentTag())) {
					return false;
				}
			} else {
				if (context.getParentTag() != null) {
					return false;
				}
			}
		}
		if ((ignoreFlags ^ IGNORE_IN_SCRIPT) > 0 && this.inScriptAttribute != context.isInScriptAttribute()) {
			return false;
		}
		if ((ignoreFlags ^ IGNORE_WITH_SRC) > 0 && this.inTagWithSrc != context.isInTagWithSrc()) {
			return false;
		}
		if ((ignoreFlags ^ IGNORE_IN_URL) > 0 && this.inUrlAttribute != context.isInUrlAttribute()) {
			return false;
		}
		if ((ignoreFlags ^ IGNORE_HTML_COMMENT) > 0 && this.htmlComment != context.isHtmlComment()) {
			return false;
		}
		
		return true;
	}
}
