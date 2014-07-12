package org.zaproxy.zap.extension.multiFuzz;

public interface PayloadProcessor<P extends Payload> {
	public P process(P orig);
}
