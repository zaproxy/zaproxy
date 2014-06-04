package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Component;
import java.util.ArrayList;


public interface FuzzComponent<L extends FuzzLocation<?>, G extends FuzzGap<?, L, ?>>{
	L selection();
	void highlight(ArrayList<G> allLocs, G curSel);
	Component messageView();
	void markUp(L f);
	void search(String text);
}
