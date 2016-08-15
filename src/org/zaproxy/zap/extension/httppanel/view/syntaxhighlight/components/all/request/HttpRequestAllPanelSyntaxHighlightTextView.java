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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.request;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.model.DefaultTextHttpMessageLocation;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.model.HttpMessageLocation;
import org.zaproxy.zap.model.TextHttpMessageLocation;
import org.zaproxy.zap.view.messagecontainer.http.SelectableContentHttpMessageContainer;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlightsManager;
import org.zaproxy.zap.view.messagelocation.MessageLocationProducerFocusListener;
import org.zaproxy.zap.view.messagelocation.MessageLocationProducerFocusListenerAdapter;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlightsManager;

public class HttpRequestAllPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView implements SelectableContentHttpMessageContainer {

	public static final String NAME = "HttpRequestSyntaxTextView";

	private MessageLocationProducerFocusListenerAdapter focusListenerAdapter;

	public HttpRequestAllPanelSyntaxHighlightTextView(RequestStringHttpPanelViewModel model) {
		super(model);

		getHttpPanelTextArea().setComponentPopupMenu(new CustomPopupMenu() {


			private static final long serialVersionUID = 377256890518967680L;

			@Override
			public void show(Component invoker, int x, int y) {
				if (!getHttpPanelTextArea().isFocusOwner()) {
					getHttpPanelTextArea().requestFocusInWindow();
				}

				View.getSingleton().getPopupMenu().show(HttpRequestAllPanelSyntaxHighlightTextView.this, x, y);
			};
		});
	}
	
	@Override
	protected HttpRequestAllPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
		return new HttpRequestAllPanelSyntaxHighlightTextArea();
	}

	@Override
	protected HttpRequestAllPanelSyntaxHighlightTextArea getHttpPanelTextArea() {
		return (HttpRequestAllPanelSyntaxHighlightTextArea) super.getHttpPanelTextArea();
	}
	
	protected static class HttpRequestAllPanelSyntaxHighlightTextArea extends HttpPanelSyntaxHighlightTextArea {

		private static final long serialVersionUID = 923466158533211593L;
		
		private static final Logger log = Logger.getLogger(HttpRequestAllPanelSyntaxHighlightTextArea.class);
		
		//private static final String HTTP_REQUEST_HEADER_AND_BODY = "HTTP Request Header and Body";

		//private static final String SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY = "text/http-request-header-body";
		
		private static RequestAllTokenMakerFactory tokenMakerFactory = null;

		private CaretVisibilityEnforcerOnFocusGain caretVisiblityEnforcer;
		
		public HttpRequestAllPanelSyntaxHighlightTextArea() {
			//addSyntaxStyle(HTTP_REQUEST_HEADER_AND_BODY, SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY);
		
			caretVisiblityEnforcer = new CaretVisibilityEnforcerOnFocusGain(this);
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
            HttpMessage httpMessage = getMessage();
            // This only happens in the Request/Response Header
            // As we replace all \r\n with \n we must add one character
            // for each line until the line where the selection is.
            int tHeader = 0;
            String header = httpMessage.getRequestHeader().toString();
            int pos = 0;
            while ((pos = header.indexOf("\r\n", pos)) != -1) {
                pos += 2;
                ++tHeader;
            }

            int start = getSelectionStart();
            int end = getSelectionEnd();
            HttpMessageLocation.Location location;

            String value;
            int headerLen = header.length();
            if (start + tHeader < headerLen) {
                try {
                    start += getLineOfOffset(start);
                } catch (BadLocationException e) {
                    // Shouldn't happen, but in case it does log it and return...
                    log.error(e.getMessage(), e);
                    return new DefaultTextHttpMessageLocation(HttpMessageLocation.Location.REQUEST_HEADER, 0);
                }

                try {
                    end += getLineOfOffset(end);
                } catch (BadLocationException e) {
                    // Shouldn't happen, but in case it does log it and return...
                    log.error(e.getMessage(), e);
                    return new DefaultTextHttpMessageLocation(HttpMessageLocation.Location.REQUEST_HEADER, 0);
                }

                if (end > headerLen) {
                    end = headerLen;
                }
                location = HttpMessageLocation.Location.REQUEST_HEADER;
                value = header.substring(start, end);
            } else {
                start += tHeader - headerLen;
                end += tHeader - headerLen;

                location = HttpMessageLocation.Location.REQUEST_BODY;
                value = httpMessage.getRequestBody().toString().substring(start, end);
            }

            return new DefaultTextHttpMessageLocation(location, start, end, value);
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

            final boolean isBody = TextHttpMessageLocation.Location.REQUEST_BODY.equals(textLocation.getLocation());
            
            //As we replace all \r\n with \n we must subtract one character
            //for each line until the line where the selection is.
            int t = 0;
            String header = getMessage().getRequestHeader().toString();
            int pos = 0;
            while ((pos = header.indexOf("\r\n", pos)) != -1) {
                pos += 2;
                
                if (!isBody && pos > textLocation.getStart()) {
                    break;
                }
                
                ++t;
            }
            
            int start = textLocation.getStart()-t;
            int end = textLocation.getEnd()-t;
            
            if (isBody) {
                start += header.length();
                end += header.length();
            }
            
            int len = this.getText().length();
            if (start > len || end > len) {
                return null;
            }

            textHighlight.setHighlightReference(highlight(start, end, textHighlight));

            return textHighlight;
        }

		@Override
		public void search(Pattern p, List<SearchMatch> matches) {
			HttpMessage httpMessage = getMessage();
			//This only happens in the Request/Response Header
			//As we replace all \r\n with \n we must add one character
			//for each line until the line where the selection is.
			int tHeader = 0;
			String header = httpMessage.getRequestHeader().toString();
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1) {
				pos += 2;
				++tHeader;
			}
			
			final int headerLen = header.length();
			final int diff = tHeader - headerLen;
			
			Matcher m = p.matcher(getText());
			int start;
			int end;
			while (m.find()) {
				start = m.start();
				end = m.end();
				
				if (start+tHeader < headerLen) {
					try {
						start += getLineOfOffset(start);
					} catch (BadLocationException e) {
						//Shouldn't happen, but in case it does log it and return.
						log.error(e.getMessage(), e);
						return;
					}
					try {
						end += getLineOfOffset(end);
					} catch (BadLocationException e) {
						//Shouldn't happen, but in case it does log it and return.
						log.error(e.getMessage(), e);
						return;
					}
					if (end > headerLen) {
						end = headerLen;
					}
					matches.add(new SearchMatch(SearchMatch.Location.REQUEST_HEAD, start, end));
				} else {
					start += diff;
					end += diff;
				
					matches.add(new SearchMatch(SearchMatch.Location.REQUEST_BODY, start, end));
				}
			}
		}
		
		@Override
		public void highlight(SearchMatch sm) {
			if (!(SearchMatch.Location.REQUEST_HEAD.equals(sm.getLocation()) ||
				SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation()))) {
				return;
			}
			
			final boolean isBody = SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation());
			
			//As we replace all \r\n with \n we must subtract one character
			//for each line until the line where the selection is.
			int t = 0;
			String header = sm.getMessage().getRequestHeader().toString();
			int pos = 0;
			while ((pos = header.indexOf("\r\n", pos)) != -1) {
				pos += 2;
				
				if (!isBody && pos > sm.getStart()) {
					break;
				}
				
				++t;
			}
			
			int start = sm.getStart()-t;
			int end = sm.getEnd()-t;
			
			if (isBody) {
				start += header.length();
				end += header.length();
			}
			
			int len = this.getText().length();
			if (start > len || end > len) {
				return;
			}
			
			highlight(start, end);
		}
		
		@Override
		protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
			if (tokenMakerFactory == null) {
				tokenMakerFactory = new RequestAllTokenMakerFactory();
			}
			return tokenMakerFactory;
		}
		
		private static class RequestAllTokenMakerFactory extends CustomTokenMakerFactory {
			
			public RequestAllTokenMakerFactory() {
				//String pkg = "";

				//putMapping(SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY, pkg + "HttpRequestHeaderAndBodyTokenMaker");
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
        TextHttpMessageLocation.Location msgLocation = ((TextHttpMessageLocation) location).getLocation();
        return msgLocation == TextHttpMessageLocation.Location.REQUEST_HEADER
                || msgLocation == TextHttpMessageLocation.Location.REQUEST_BODY;
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
