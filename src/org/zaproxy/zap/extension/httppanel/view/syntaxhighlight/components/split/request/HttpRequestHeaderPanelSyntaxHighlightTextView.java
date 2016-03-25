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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.DefaultTextHttpMessageLocation;
import org.zaproxy.zap.model.HttpMessageLocation;
import org.zaproxy.zap.model.TextHttpMessageLocation;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.view.messagecontainer.http.SelectableContentHttpMessageContainer;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlightsManager;
import org.zaproxy.zap.view.messagelocation.MessageLocationProducerFocusListener;
import org.zaproxy.zap.view.messagelocation.MessageLocationProducerFocusListenerAdapter;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlightsManager;

public class HttpRequestHeaderPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView implements SelectableContentHttpMessageContainer {

	public static final String NAME = "HttpRequestHeaderSyntaxTextView";

	private MessageLocationProducerFocusListenerAdapter focusListenerAdapter;

	public HttpRequestHeaderPanelSyntaxHighlightTextView(RequestHeaderStringHttpPanelViewModel model) {
		super(model);

		getHttpPanelTextArea().setComponentPopupMenu(new CustomPopupMenu() {

			private static final long serialVersionUID = -426000345249750052L;

			@Override
			public void show(Component invoker, int x, int y) {
				if (!getHttpPanelTextArea().isFocusOwner()) {
					getHttpPanelTextArea().requestFocusInWindow();
				}

				View.getSingleton().getPopupMenu().show(HttpRequestHeaderPanelSyntaxHighlightTextView.this, x, y);
			};
		});
	}
	
	@Override
	protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpRequestHeaderPanelSyntaxHighlightTextArea();
	}
	
    @Override
    protected HttpRequestHeaderPanelSyntaxHighlightTextArea getHttpPanelTextArea() {
        return (HttpRequestHeaderPanelSyntaxHighlightTextArea) super.getHttpPanelTextArea();
    }

	private static class HttpRequestHeaderPanelSyntaxHighlightTextArea extends HttpPanelSyntaxHighlightTextArea {

		private static final long serialVersionUID = -4532294585338584747L;
		
		private static final Logger log = Logger.getLogger(HttpRequestHeaderPanelSyntaxHighlightTextArea.class);
		
		//private static final String HTTP_REQUEST_HEADER = "HTTP Request Header";
		
		//private static final String SYNTAX_STYLE_HTTP_REQUEST_HEADER = "text/http-request-header";

		private static RequestHeaderTokenMakerFactory tokenMakerFactory = null;
		
		private CaretVisibilityEnforcerOnFocusGain caretVisiblityEnforcer;
		
		public HttpRequestHeaderPanelSyntaxHighlightTextArea() {
			//addSyntaxStyle(HTTP_REQUEST_HEADER, SYNTAX_STYLE_HTTP_REQUEST_HEADER);
			
			//setSyntaxEditingStyle(SYNTAX_STYLE_HTTP_REQUEST_HEADER);
			
			caretVisiblityEnforcer = new CaretVisibilityEnforcerOnFocusGain(this);
		}
		
		@Override
		public String getName() {
		    return NAME;
		}
		
		@Override
		public HttpMessage getMessage() {
			return (HttpMessage) super.getMessage();
		}
		
		@Override
		public void setMessage(Message aMessage) {
			super.setMessage(aMessage);
			
			caretVisiblityEnforcer.setEnforceVisibilityOnFocusGain(aMessage != null);
		}

		protected MessageLocation getSelection() {
			int start = getSelectionStart();
			try {
				start += getLineOfOffset(start);
			} catch (BadLocationException e) {
				// Shouldn't happen, but in case it does log it and return...
				log.error(e.getMessage(), e);
				return new DefaultTextHttpMessageLocation(HttpMessageLocation.Location.REQUEST_HEADER, 0);
			}

			int end = getSelectionEnd();
			try {
				end += getLineOfOffset(end);
			} catch (BadLocationException e) {
				// Shouldn't happen, but in case it does log it and return...
				log.error(e.getMessage(), e);
				return new DefaultTextHttpMessageLocation(HttpMessageLocation.Location.REQUEST_HEADER, 0);
			}

			if (start == end) {
				return new DefaultTextHttpMessageLocation(HttpMessageLocation.Location.REQUEST_HEADER, start);
			}

			return new DefaultTextHttpMessageLocation(
					HttpMessageLocation.Location.REQUEST_HEADER,
					start,
					end,
					getMessage().getRequestHeader().toString().substring(start, end));
		}

		protected MessageLocationHighlightsManager create() {
            return new TextMessageLocationHighlightsManager();
        }

        protected MessageLocationHighlight highlightImpl(
                TextHttpMessageLocation textLocation,
                TextMessageLocationHighlight textHighlight) {
            if (getMessage() == null) {
                return null;
            }

            // As we replace all \r\n with \n we must subtract one character
            // for each line until the line where the selection is.
            int excessChars = 0;
            String header = getMessage().getRequestHeader().toString();

            int pos = 0;
            while ((pos = header.indexOf("\r\n", pos)) != -1 && pos < textLocation.getStart()) {
                pos += 2;
                ++excessChars;
            }

            int len = this.getText().length();
            int finalStartPos = textLocation.getStart() - excessChars;
            if (finalStartPos > len) {
                return null;
            }

            if (pos != -1) {
                while ((pos = header.indexOf("\r\n", pos)) != -1 && pos < textLocation.getEnd()) {
                    pos += 2;
                    ++excessChars;
                }
            }

            int finalEndPos = textLocation.getEnd() - excessChars;
            if (finalEndPos > len) {
                return null;
            }

            textHighlight.setHighlightReference(highlight(finalStartPos, finalEndPos, textHighlight));

            return textHighlight;
        }

		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			int start;
			int end;
			Matcher m = p.matcher(getText());
			while (m.find()) {

				//This only happens in the Request/Response Header
				//As we replace all \r\n with \n we must add one character
				//for each line until the line where the match is.
				start = m.start();
				try {
					start += getLineOfOffset(start);
				} catch (BadLocationException e) {
					//Shouldn't happen, but in case it does log it and return.
					log.error(e.getMessage(), e);
					return;
				}

				end = m.end();
				try {
					end += getLineOfOffset(end);
				} catch (BadLocationException e) {
					//Shouldn't happen, but in case it does log it and return.
					log.error(e.getMessage(), e);
					return;
				}
				
				matches.add(new SearchMatch(SearchMatch.Location.REQUEST_HEAD, start, end));
			}
		}
		
		@Override
		public void highlight(SearchMatch sm) {
			if (!SearchMatch.Location.REQUEST_HEAD.equals(sm.getLocation())) {
				return;
			}
			
			//As we replace all \r\n with \n we must subtract one character
			//for each line until the line where the selection is.
			int t = 0;
			String header = sm.getMessage().getRequestHeader().toString();
			
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1 && pos < sm.getStart()) {
				pos += 2;
				++t;
			}
			
			int len = this.getText().length();
			if (sm.getStart()-t > len || sm.getEnd()-t > len) {
				return;
			}
			
			highlight(sm.getStart()-t, sm.getEnd()-t);
		}
		
		@Override
		protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
			if (tokenMakerFactory == null) {
				tokenMakerFactory = new RequestHeaderTokenMakerFactory();
			}
			return tokenMakerFactory;
		}
		
		private static class RequestHeaderTokenMakerFactory extends CustomTokenMakerFactory {
			
			public RequestHeaderTokenMakerFactory() {
				//String pkg = "org.zaproxy.zap.extension.httppanel.view.text.lexers.";
				
				//putMapping(SYNTAX_STYLE_HTTP_REQUEST_HEADER, pkg + "HttpRequestHeaderTokenMaker");
			}
		}
	}
	
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<HttpMessage> getMessageClass() {
        return HttpMessage.class;
    }

    @Override
    public Class<? extends MessageLocation> getMessageLocationClass() {
        return TextHttpMessageLocation.class;
    }

    @Override
    public MessageLocation getSelection() {
        return getHttpPanelTextArea().getSelection();
    }

    @Override
    public MessageLocationHighlightsManager create() {
        return getHttpPanelTextArea().create();
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location) {
        if (!supports(location)) {
            return null;
        }
        TextHttpMessageLocation textLocation = (TextHttpMessageLocation) location;

        return getHttpPanelTextArea().highlightImpl(textLocation, new TextMessageLocationHighlight(Color.LIGHT_GRAY));
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location, MessageLocationHighlight highlight) {
        if (!supports(location) || !(highlight instanceof TextMessageLocationHighlight)) {
            return null;
        }
        TextHttpMessageLocation textLocation = (TextHttpMessageLocation) location;
        TextMessageLocationHighlight textHighlight = (TextMessageLocationHighlight) highlight;

        return getHttpPanelTextArea().highlightImpl(textLocation, textHighlight);
    }

    @Override
    public void removeHighlight(MessageLocation location, MessageLocationHighlight highlightReference) {
        if (!(highlightReference instanceof TextMessageLocationHighlight)) {
            return;
        }
        getHttpPanelTextArea().removeHighlight(((TextMessageLocationHighlight) highlightReference).getHighlightReference());
    }

    @Override
    public boolean supports(MessageLocation location) {
        if (!(location instanceof TextHttpMessageLocation)) {
            return false;
        }
        return ((TextHttpMessageLocation) location).getLocation() == TextHttpMessageLocation.Location.REQUEST_HEADER;
    }

    @Override
    public boolean supports(Class<? extends MessageLocation> classLocation) {
        return (TextHttpMessageLocation.class.isAssignableFrom(classLocation));
    }

    @Override
    public void addFocusListener(MessageLocationProducerFocusListener focusListener) {
        getFocusListenerAdapter().addFocusListener(focusListener);
    }

    @Override
    public void removeFocusListener(MessageLocationProducerFocusListener focusListener) {
        getFocusListenerAdapter().removeFocusListener(focusListener);

        if (!getFocusListenerAdapter().hasFocusListeners()) {
            getHttpPanelTextArea().removeFocusListener(focusListenerAdapter);
            focusListenerAdapter = null;
        }
    }

    @Override
    public HttpMessage getMessage() {
        return getHttpPanelTextArea().getMessage();
    }

    @Override
    public Component getComponent() {
        return getHttpPanelTextArea();
    }

    @Override
    public boolean isEmpty() {
        return getHttpPanelTextArea().getMessage() == null;
    }

    private MessageLocationProducerFocusListenerAdapter getFocusListenerAdapter() {
        if (focusListenerAdapter == null) {
            focusListenerAdapter = new MessageLocationProducerFocusListenerAdapter(this);
            getHttpPanelTextArea().addFocusListener(focusListenerAdapter);
        }
        return focusListenerAdapter;
    }

}

