/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.httputils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.network.HttpMessage;

/** @deprecated (2.12.0) This class has been moved to the zap-extensions repo. */
@Deprecated
public class HtmlContextAnalyser {

    private char[] quotes = {'\'', '"'};

    // Tag attributes which can contain javascript
    private String[] scriptAttributes = {
        "onBlur",
        "onChange",
        "onClick",
        "onDblClick",
        "onFocus",
        "onKeydown",
        "onKeyup",
        "onKeypress",
        "onLoad",
        "onMousedown",
        "onMouseup",
        "onMouseover",
        "onMousemove",
        "onMouseout",
        "onReset",
        "onSelect",
        "onSubmit",
        "onUnload"
    };

    // Tag attributes which can contain a URL
    private String[] urlAttributes = {
        "action",
        "background",
        "cite",
        "classid",
        "codebase",
        "data",
        "formaction",
        "href",
        "icon",
        "longdesc",
        "manifest",
        "poster",
        "profile",
        "src",
        "usemap",
    };

    // Tags which can have a 'src' attribute
    private String[] tagsWithSrcAttributes = {
        "frame", "iframe", "img",
        "input", // Special case - should also check to see if it has a type of 'image'
        "script", "src",
    };

    private HttpMessage msg = null;
    private String htmlPage = null;
    private Source src = null;

    public HtmlContextAnalyser(HttpMessage msg) {
        this.msg = msg;
        this.htmlPage = msg.getResponseBody().toString();
        src = new Source(htmlPage);
        src.fullSequentialParse();
    }

    private boolean isQuote(char chr) {
        for (int i = 0; i < quotes.length; i++) {
            if (chr == quotes[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean isScriptAttribute(String att) {
        for (int i = 0; i < scriptAttributes.length; i++) {
            if (att.equalsIgnoreCase(scriptAttributes[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isUrlAttribute(String att) {
        for (int i = 0; i < urlAttributes.length; i++) {
            if (att.equalsIgnoreCase(urlAttributes[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isInTagWithSrcAttribute(String tag) {
        for (int i = 0; i < tagsWithSrcAttributes.length; i++) {
            if (tag.equalsIgnoreCase(tagsWithSrcAttributes[i])) {
                return true;
            }
        }
        return false;
    }

    public List<HtmlContext> getHtmlContexts(String target) {
        return this.getHtmlContexts(target, null, 0);
    }

    public List<HtmlContext> getHtmlContexts(
            String target, HtmlContext targetContext, int ignoreFlags) {
        List<HtmlContext> contexts = new ArrayList<>();

        int offset = 0;
        while ((offset = htmlPage.indexOf(target, offset)) >= 0) {
            HtmlContext context =
                    new HtmlContext(this.msg, target, offset, offset + target.length());
            offset += target.length();

            // Is it in quotes?
            char leftQuote = 0;
            for (int i = context.getStart() - 1; i > 0; i--) {
                char chr = htmlPage.charAt(i);
                if (isQuote(chr)) {
                    leftQuote = chr;
                    break;
                } else if (chr == '>') {
                    // end of another tag
                    break;
                }
            }
            if (leftQuote != 0) {
                for (int i = context.getEnd(); i < htmlPage.length(); i++) {
                    char chr = htmlPage.charAt(i);
                    if (leftQuote == chr) {
                        // matching quote
                        context.setSurroundingQuote("" + leftQuote);
                        break;
                    } else if (isQuote(chr)) {
                        // Another non matching quote
                        break;
                    } else if (chr == '<') {
                        // start of another tag
                        break;
                    }
                }
            }
            // is it in an HTML comment?
            String prefix = htmlPage.substring(0, context.getStart());
            if (prefix.lastIndexOf("<!--") > prefix.lastIndexOf(">")) {
                // Also check closing comment?
                context.setHtmlComment(true);
            }

            // Work out the location in the DOM
            Element element = src.getEnclosingElement(context.getStart());
            if (element != null) {
                // See if its in an attribute
                boolean isInputTag =
                        element.getName()
                                .equalsIgnoreCase("input"); // Special case for input src attributes
                boolean isImageInputTag = false;
                Iterator<Attribute> iter = element.getAttributes().iterator();
                while (iter.hasNext()) {
                    Attribute att = iter.next();
                    if (att.getValue() != null
                            && att.getValue().toLowerCase().indexOf(target.toLowerCase()) >= 0) {
                        // Found the injected value
                        context.setTagAttribute(att.getName());
                        context.setInUrlAttribute(this.isUrlAttribute(att.getName()));
                        context.setInScriptAttribute(this.isScriptAttribute(att.getName()));
                    }
                    if (isInputTag
                            && att.getName().equalsIgnoreCase("type")
                            && "image".equalsIgnoreCase(att.getValue())) {
                        isImageInputTag = true;
                    }
                }

                // record the tag hierarchy
                context.addParentTag(element.getName());
                if (!isInputTag || isImageInputTag) {
                    // Input tags only use the src attribute if the type is 'image'
                    context.setInTagWithSrc(this.isInTagWithSrcAttribute(element.getName()));
                }
                while ((element = element.getParentElement()) != null) {
                    context.addParentTag(element.getName());
                }
            }
            if (targetContext == null) {
                // Always add
                contexts.add(context);
            } else if (targetContext.matches(context, ignoreFlags)) {
                // Matches the supplied context
                contexts.add(context);
            }
        }

        return contexts;
    }
}
