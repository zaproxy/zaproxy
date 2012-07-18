package org.zaproxy.zap.extension.websocket;

import java.sql.SQLException;

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;

public class WebSocketChannelDAO implements Comparable<WebSocketChannelDAO> {
	public Integer channelId;
	public String host;
	public Integer port;
	public Long startTimestamp;
	public Long endTimestamp;
	public Integer historyId;

	public WebSocketChannelDAO() {
		
	}
	
	public WebSocketChannelDAO(String host) {
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
	public int compareTo(WebSocketChannelDAO other) {
		int result = host.compareTo(other.host);

		if (result == 0) {
			result = port.compareTo(other.port);
			if (result == 0) {
				return channelId.compareTo(other.channelId);
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

	public String toString() {
		if (port != null && channelId != null) {
			return host + ":" + port + " (#" + channelId + ")";
		}
		return host;
	}

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof WebSocketChannelDAO) {
        	WebSocketChannelDAO that = (WebSocketChannelDAO) other;
            result = that.canEqual(this) && (this.channelId == that.channelId);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (41 * super.hashCode() + channelId.hashCode());
    }

    private boolean canEqual(Object other) {
        return (other instanceof WebSocketChannelDAO);
    }
}