package org.zaproxy.zap.extension.multiFuzz;

import java.util.Map;

import org.zaproxy.zap.extension.httppanel.Message;

public interface FuzzMessagePreProcessor<FM extends Message, L extends FuzzLocation<FM>, P extends Payload> {
	public FM process(FM orig, Map<L, P> payMap);
}
