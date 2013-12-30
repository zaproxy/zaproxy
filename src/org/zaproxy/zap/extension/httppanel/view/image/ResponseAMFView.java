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
package org.zaproxy.zap.extension.httppanel.view.amf;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelEvent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelListener;

import  java.io.ByteArrayInputStream;
import  flex.messaging.io.*;
import  flex.messaging.io.amf.*;
import  java.io.*;


public class ResponseAMFView implements HttpPanelView, HttpPanelViewModelListener {
	
	private static final String CONTENT_TYPE_AMF = "application/x-amf";

	public static final String NAME = "ResponseAMFView";
	
	public static final String CAPTION_NAME = Constant.messages.getString("http.panel.view.amf.name");
	
	private JPanel mainPanel;
	private JLabel amfLabel;
	
	private HttpPanelViewModel model;
	
	//a Class alias registry for classes embedded in the AMF message
	//this is where back-end specific aliases will need to be registered
	private ClassAliasRegistry registry = ClassAliasRegistry.getRegistry();
	
	//get a serialisation context
	SerializationContext serialisationcontext = SerializationContext.getSerializationContext();
	
	//AMF message de-serializer
	AmfMessageDeserializer amfdeserialiser = new AmfMessageDeserializer();
	
	//an Action Context
	ActionContext actioncontext = new ActionContext ();
	
	public ResponseAMFView(HttpPanelViewModel model) {
		
		//set up the AMF Remoting related stuff first
		//set up standard registrations that we will see with Flex.
		//DSK is used by the Flex back-end. BlazeDS supports this natively, so set up the alias from DSK to the actual class name
        registry.registerAlias("DSK", flex.messaging.messages.AcknowledgeMessageExt.class.getName());

        //configure the serialisation context. 
		serialisationcontext.instantiateTypes = false;
		serialisationcontext.createASObjectForMissingType = false;		
		//the rest of the AMF specific stuff happens in dataChanged

		
		//and then the model related stuff
		this.model = model;
		
		amfLabel = new JLabel();
		amfLabel.setVerticalAlignment(JLabel.TOP);
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(amfLabel));
		
		this.model.addHttpPanelViewModelListener(this);
	}
	
	@Override
	public void save() {
	}

	@Override
	public void setSelected(boolean selected) {
		if (selected) {
			amfLabel.requestFocusInWindow();
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getCaptionName() {
		return CAPTION_NAME;
	}
	
	@Override
	public String getTargetViewName() {
		return "";
	}
	
	@Override
	public int getPosition() {
		return 1;
	}

	@Override
	public boolean isEnabled(Message aMessage) {
		//only enable the option if the message is AMF
		return isAMF(aMessage);
	}

	@Override
	public boolean hasChanged() {
		return false;
	}

	@Override
	public JComponent getPane() {
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setEditable(boolean editable) {
	}
	
	@Override
	public void setParentConfigurationKey(String configurationKey) {
	}
	
	@Override
	public void loadConfiguration(FileConfiguration fileConfiguration) {
	}
	
	@Override
	public void saveConfiguration(FileConfiguration fileConfiguration) {
	}

	@Override
	public HttpPanelViewModel getModel() {
		return model;
	}
	
	@Override
	public void dataChanged(HttpPanelViewModelEvent e) {
        // FIXME(This view should ask for a specific model based on HttpMessage)
		HttpMessage httpMessage = (HttpMessage) model.getMessage();
		
		if (isAMF(httpMessage)) {			
			amfLabel.setToolTipText(CAPTION_NAME);
			amfLabel.setText("Loading</br>Please wait</br>");
			
			StringBuffer amfHumanReadable = new StringBuffer();
			
			//create a new Action message
			ActionMessage message = new ActionMessage();
			
			//initialises the deserialisation context using an input stream created from the message response body. 			
	        amfdeserialiser.initialize(serialisationcontext, new ByteArrayInputStream(httpMessage.getResponseBody().getBytes()), null);
	        try {
	        	amfdeserialiser.readMessage(message, actioncontext);
	        	//and now parse the message
                int headerCount = message.getHeaderCount();
                amfHumanReadable.append(headerCount + " headers</br>");

                //get each message header in turn
                for (int i= 0; i< headerCount; i++) {
                        MessageHeader messageHeader = message.getHeader(i);
                        try {
                                Object headerObject = messageHeader.getData();
                                amfHumanReadable.append("Header ["+ i + "]: " + headerObject + "</br>");
                                }
                        catch (Exception exeception) {
                                amfHumanReadable.append("Header ["+ i + "] was unparseable</br>" );
                                }
                        }

                int bodyCount = message.getBodyCount();
                amfHumanReadable.append(bodyCount + " bodies</br>");
                
                //get each message body in turn
                for (int i= 0; i< bodyCount; i++) {
                        MessageBody messageBody = message.getBody(i);
                        String targetURI = messageBody.getTargetURI();
                        String responseURI = messageBody.getResponseURI();
                        String method = messageBody.getReplyMethod();
                        
                        amfHumanReadable.append("Body [" + i + "] target URI: [" + targetURI + "]</br>");
                        amfHumanReadable.append("Body [" + i + "] response URI: [" + responseURI+ "]</br>");
                        amfHumanReadable.append("Body [" + i + "] method: ["+ method + "]</br>");
                        
                        try {
                            Object bodyObject = messageBody.getData();
                            amfHumanReadable.append("Body ["+ i + "]: " + bodyObject.toString() + "</br>");
                            }
                        catch (Exception e1) {
                        	amfHumanReadable.append("Body ["+ i + "] was unparseable</br>");
                            }
                        
                //convert the StringBuffer back to a String, and set it.        
                amfLabel.setText(amfHumanReadable.toString());
                }                        
	        }
	        catch (ClassNotFoundException cnfe) {
	        	amfLabel.setText("A class was not found when attempting to read the Action Message from the stream.  This should *not* happen</br>");
	        	return;
	        }
	        catch (IOException ioe) {
	        	amfLabel.setText("The AMF could not be de-serialised due to an I/O Exception</br>");
	        	return;
	        }
		} else {
			//it is NOT an AMF content type. No point in even trying to convert it
			amfLabel.setText("The output is not AMF. Go away.</br>");
		}
	}

    static boolean isAMF(final Message aMessage) {
    	if (aMessage == null) return false;
    	
        if (aMessage instanceof HttpMessage) {
            HttpMessage httpMessage = (HttpMessage) aMessage;

            if (httpMessage.getResponseBody() == null) {
                return false;
            }
            //check the content type, if one was set.
            String contentType = httpMessage.getResponseHeader().getHeader(HttpHeader.CONTENT_TYPE);
            if ( contentType == null ) return false;
            
            return contentType.equalsIgnoreCase(CONTENT_TYPE_AMF);
        }
        
        return false;
    }
}
