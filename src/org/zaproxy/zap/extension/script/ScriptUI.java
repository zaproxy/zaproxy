package org.zaproxy.zap.extension.script;

import java.awt.event.MouseAdapter;
import java.util.List;

import javax.swing.tree.TreeCellRenderer;

import org.zaproxy.zap.extension.httppanel.Message;

public interface ScriptUI {

	ScriptNode getSelectedNode();
	
	List<ScriptNode> getSelectedNodes();
	
	boolean isSelectedMessage(Message msg);
	
	void addMouseListener(MouseAdapter adapter); 
	
	@SuppressWarnings("rawtypes")
	void addRenderer(Class c, TreeCellRenderer renderer);
	
	void displayScript (ScriptWrapper script);

	void selectNode(ScriptNode node, boolean expand);

	void disableScriptDialog(Class<?> klass);
	
	String getTreeName();
}
