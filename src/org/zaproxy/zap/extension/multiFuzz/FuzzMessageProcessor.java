package org.zaproxy.zap.extension.multiFuzz;

import org.zaproxy.zap.extension.httppanel.Message;

public interface FuzzMessageProcessor<FM extends Message> {
	public FM process(FM orig);
}
