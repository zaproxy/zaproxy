package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Component;
import java.util.ArrayList;

import org.zaproxy.zap.extension.httppanel.Message;

public interface FuzzComponent<M extends Message, L extends FuzzLocation<M>, G extends FuzzGap<M, L, ?>> {
	L selection();

	void highlight(ArrayList<G> allLocs);

	Component messageView();

	void markUp(L f);

	void search(String text);

	void setMessage(M message);
}
