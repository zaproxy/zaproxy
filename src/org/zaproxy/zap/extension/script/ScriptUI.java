package org.zaproxy.zap.extension.script;

import java.awt.event.MouseAdapter;
import java.io.Writer;
import java.util.List;

import javax.swing.TransferHandler;
import javax.swing.tree.TreeCellRenderer;

public interface ScriptUI {

	/**
	 * Called after an engine is added.
	 * <p>
	 * The UI should display the contents of the selected script if it's targeting the added engine.
	 * 
	 * @param scriptEngineWrapper the engine that was removed
	 * @since 2.4.0
	 */
	void engineAdded(ScriptEngineWrapper scriptEngineWrapper);

	/**
	 * Called after an engine is removed.
	 * <p>
	 * The UI should clear the contents of the selected script if it's targeting the removed engine.
	 * 
	 * @param scriptEngineWrapper the engine that was removed
	 * @since 2.4.0
	 */
	void engineRemoved(ScriptEngineWrapper scriptEngineWrapper);

	ScriptNode getSelectedNode();
	
	List<ScriptNode> getSelectedNodes();
	
	void addMouseListener(MouseAdapter adapter); 

	/**
	 * Removes the given mouse listener.
	 *
	 * @param mouseListener the mouse listener that will be removed.
	 * @since 2.4.0
	 */
	void removeMouseListener(MouseAdapter mouseListener); 
	
	void addRenderer(Class<?> c, TreeCellRenderer renderer);

	/**
	 * Removes the renderer added for the given class.
	 *
	 * @param klass the class whose target renderer should be removed
	 * @since 2.4.0
	 */
	void removeRenderer(Class<?> klass);

	/**
	 * Adds a transfer handler for the given class.
	 *
	 * @param klass the class in which the given transfer handler will be used
	 * @param th the transfer handler
	 * @since 2.4.0
	 */
	void addScriptTreeTransferHandler(Class<?> klass, TransferHandler th);

	/**
	 * Removes the transfer handler that was being used for the given class.
	 *
	 * @param klass the class whose targeting transfer handler will be removed
	 * @since 2.4.0
	 */
	void removeScriptTreeTransferHandler(Class<?> klass);
	
	void displayScript (ScriptWrapper script);

	boolean isScriptDisplayed(ScriptWrapper script);

	void selectNode(ScriptNode node, boolean expand);

	void disableScriptDialog(Class<?> klass);

	/**
	 * Removes a disabled script dialogue of the given class.
	 *
	 * @param klass the class that has the script dialogue disabled
	 * @since 2.4.0
	 */
	void removeDisableScriptDialog(Class<?> klass);
	
	String getTreeName();
	
	Writer getOutputWriter();
}
