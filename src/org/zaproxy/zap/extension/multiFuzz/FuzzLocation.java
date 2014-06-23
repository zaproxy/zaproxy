package org.zaproxy.zap.extension.multiFuzz;

import org.zaproxy.zap.extension.httppanel.Message;

public interface FuzzLocation<M extends Message> extends
		Comparable<FuzzLocation<M>> {
	public String getRepresentation(M msg);

	public boolean overlap(FuzzLocation<M> loc);
}
