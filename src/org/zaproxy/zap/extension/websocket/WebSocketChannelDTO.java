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
package org.zaproxy.zap.extension.websocket;

import java.sql.SQLException;

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;

public class WebSocketChannelDTO implements Comparable<WebSocketChannelDTO> {
	
	/**
	 * ChannelId.
	 */
	public Integer id;
	
	/**
	 * Hostname, isn't necessarily the same as the
	 * {@link WebSocketChannelDTO#url}.
	 */
	public String host;
	
	/**
	 * Port where this channel is connected at. Usually 80 or 443.
	 */
	public Integer port;
	
	/**
	 * URL used in HTTP handshake.
	 */
	public String url;
	
	/**
	 * Timestamp taken, when connection was established successfuly.
	 */
	public Long startTimestamp;
	
	/**
	 * Timestamp of close, otherwise <code>null</code>.
	 */
	public Long endTimestamp;
	
	/**
	 * Id of handshake message.
	 */
	public Integer historyId;

	public WebSocketChannelDTO() {
		
	}
	
	public WebSocketChannelDTO(String host) {
		this.host = host;
	}

	public boolean isConnected() {
		if (startTimestamp != null && endTimestamp == null) {
			return true;
		}
		return false;
	}

	/**
	 * Used for sorting items. If two items have identical host names and ports,
	 * the channel number is used to determine order.
	 */
	@Override
	public int compareTo(WebSocketChannelDTO other) {
		int result = host.compareTo(other.host);

		if (result == 0) {
			result = port.compareTo(other.port);
			if (result == 0) {
				return id.compareTo(other.id);
			}
		}

		return result;
	}
	
	public HistoryReference getHandshakeReference() {
		if (historyId == null) {
			return null;
		}
		
		try {
			return new HistoryReference(historyId);
		} catch (HttpMalformedHeaderException e) {
			return null;
		} catch (SQLException e) {
			return null;
		}
	}

	public boolean isInScope() {
		return Model.getSingleton().getSession().isInScope(this.url);
	}

	public String toString() {
		if (port != null && id != null) {
			return host + ":" + port + " (#" + id + ")";
		}
		return host;
	}

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof WebSocketChannelDTO) {
        	WebSocketChannelDTO that = (WebSocketChannelDTO) other;
        	
        	if (that.canEqual(this)) {
        		if (id == null) {
        			result = (that.id == null);
        		} else {
        			result = id.equals(that.id);
        		}
        	}
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (41 * super.hashCode() + id.hashCode());
    }

    private boolean canEqual(Object other) {
        return (other instanceof WebSocketChannelDTO);
    }
}