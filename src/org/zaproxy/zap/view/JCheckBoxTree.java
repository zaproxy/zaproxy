package org.zaproxy.zap.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Orignially based on code from 
 * http://stackoverflow.com/questions/21847411/java-swing-need-a-good-quality-developed-jtree-with-checkboxes
 * Applied the JCheckBoxTree.this fix suggested in the comments
 * Also added expandAll(), collapseAll() and setCheckBoxEnabled(..)
 * 
 * Still TODO:
 * 	Support tri-state checkboxes
 * 	Proper fix for top level node getting truncated
 * 
 * @author simon
 *
 */
public class JCheckBoxTree extends JTree {

    private static final long serialVersionUID = -4194122328392241790L;

    // Defining data structure that will enable to fast check-indicate the state of each node
    // It totally replaces the "selection" mechanism of the JTree
    private class CheckedNode {
        boolean isSelected;
        boolean hasChildren;
        boolean allChildrenSelected;
        boolean isCheckBoxEnabled = true;

        public CheckedNode(boolean isSelected_, boolean hasChildren_, boolean allChildrenSelected_) {
            isSelected = isSelected_;
            hasChildren = hasChildren_;
            allChildrenSelected = allChildrenSelected_;
        }
    }
    HashMap<TreePath, CheckedNode> nodesCheckingState;
    HashSet<TreePath> checkedPaths = new HashSet<TreePath>();

    // Defining a new event type for the checking mechanism and preparing event-handling mechanism

    public class CheckChangeEvent extends EventObject {     
        private static final long serialVersionUID = -8100230309044193368L;

        public CheckChangeEvent(Object source) {
            super(source);          
        }       
    }   

    public interface CheckChangeEventListener extends EventListener {
        public void checkStateChanged(CheckChangeEvent event);
    }

    public void addCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.add(CheckChangeEventListener.class, listener);
    }
    public void removeCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.remove(CheckChangeEventListener.class, listener);
    }

    void fireCheckChangeEvent(CheckChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == CheckChangeEventListener.class) {
                ((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
            }
        }
    }

    // Override
    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        resetCheckingState();
    }

    // New method that returns only the checked paths (totally ignores original "selection" mechanism)
    public TreePath[] getCheckedPaths() {
        return checkedPaths.toArray(new TreePath[checkedPaths.size()]);
    }

    public boolean isChecked(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn.isSelected;
    }

    // Returns true in case that the node is selected, has children but not all of them are selected
    public boolean isSelectedPartially(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn.isSelected && cn.hasChildren && !cn.allChildrenSelected;
    }

    private void resetCheckingState() { 
        nodesCheckingState = new HashMap<TreePath, CheckedNode>();
        checkedPaths = new HashSet<TreePath>();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel().getRoot();
        if (node == null) {
            return;
        }
        addSubtreeToCheckingStateTracking(node);
    }

    // Creating data structure of the current model for the checking mechanism
    private void addSubtreeToCheckingStateTracking(DefaultMutableTreeNode node) {
        TreeNode[] path = node.getPath();   
        TreePath tp = new TreePath(path);
        CheckedNode cn = new CheckedNode(false, node.getChildCount() > 0, false);
        nodesCheckingState.put(tp, cn);
        for (int i = 0 ; i < node.getChildCount() ; i++) {              
            addSubtreeToCheckingStateTracking((DefaultMutableTreeNode) tp.pathByAddingChild(node.getChildAt(i)).getLastPathComponent());
        }
    }

    // Overriding cell renderer by a class that ignores the original "selection" mechanism
    // It decides how to show the nodes due to the checking-mechanism
    private class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {     
        private static final long serialVersionUID = -7341833835878991719L;     
        JCheckBox checkBox;
        JLabel altLabel;
        public CheckBoxCellRenderer() {
            super();
            this.setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            altLabel = new JLabel("");
            add(checkBox, BorderLayout.CENTER);
            add(altLabel, BorderLayout.EAST);
            setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object obj = node.getUserObject();          
            TreePath tp = new TreePath(node.getPath());
            CheckedNode cn = nodesCheckingState.get(tp);
            if (cn == null) {
                return this;
            }
            String textRepresentation = obj != null ? obj.toString() : "";
            if (cn.isCheckBoxEnabled) {
	            checkBox.setSelected(cn.isSelected);
	            checkBox.setOpaque(cn.isSelected && cn.hasChildren && ! cn.allChildrenSelected);
	        	checkBox.setVisible(true);
	        	checkBox.setEnabled(true);
	        	// TODO nasty hack to prevent top level node text being truncated - need a better fix for this :/
	        	textRepresentation += "          ";
	        	/* Looks ok, but doesnt work correctly
	            if (cn.isSelected && cn.hasChildren && ! cn.allChildrenSelected) {
	                checkBox.getModel().setPressed(true);
	                checkBox.getModel().setArmed(true);
	            } else {
	                checkBox.getModel().setPressed(false);
	                checkBox.getModel().setArmed(false);
	            }
	            */
            } else {
            	checkBox.setVisible(false);
            	checkBox.setEnabled(false);
            }
            altLabel.setText(textRepresentation);
            altLabel.setForeground(UIManager.getColor(selected ? "Tree.selectionForeground" : "Tree.textForeground"));

            return this;
        }       
    }

    public JCheckBoxTree() {
        super();
        // Disabling toggling by double-click
        this.setToggleClickCount(0);
        // Overriding cell renderer by new one defined above
        CheckBoxCellRenderer cellRenderer = new CheckBoxCellRenderer();
        this.setCellRenderer(cellRenderer);

        // Overriding selection model by an empty one
        DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {      
            private static final long serialVersionUID = -8190634240451667286L;
            // Totally disabling the selection mechanism
            @Override
            public void setSelectionPath(TreePath path) {
            }           
            @Override
            public void addSelectionPath(TreePath path) {                       
            }           
            @Override
            public void removeSelectionPath(TreePath path) {
            }
            @Override
            public void setSelectionPaths(TreePath[] pPaths) {
            }
        };
        // Calling checking mechanism on mouse click
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                TreePath tp = JCheckBoxTree.this.getPathForLocation(arg0.getX(), arg0.getY());
                if (tp == null) {
                    return;
                }
                if (! nodesCheckingState.get(tp).isCheckBoxEnabled) {
                	return;
                }
                boolean checkMode = ! nodesCheckingState.get(tp).isSelected;
                checkSubTree(tp, checkMode);
                updatePredecessorsWithCheckMode(tp, checkMode);
                // Firing the check change event
                fireCheckChangeEvent(new CheckChangeEvent(new Object()));
                // Repainting tree after the data structures were updated
                JCheckBoxTree.this.repaint();                          
            }           
            @Override
            public void mouseEntered(MouseEvent arg0) {         
            }           
            @Override
            public void mouseExited(MouseEvent arg0) {              
            }
            @Override
            public void mousePressed(MouseEvent arg0) {             
            }
            @Override
            public void mouseReleased(MouseEvent arg0) {
            }           
        });
        this.setSelectionModel(dtsm);
    }

    // When a node is checked/unchecked, updating the states of the predecessors
    protected void updatePredecessorsWithCheckMode(TreePath tp, boolean check) {
        TreePath parentPath = tp.getParentPath();
        // If it is the root, stop the recursive calls and return
        if (parentPath == null) {
            return;
        }       
        CheckedNode parentCheckedNode = nodesCheckingState.get(parentPath);
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();     
        parentCheckedNode.allChildrenSelected = true;
        parentCheckedNode.isSelected = false;
        for (int i = 0 ; i < parentNode.getChildCount() ; i++) {                
            TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
            CheckedNode childCheckedNode = nodesCheckingState.get(childPath);           
            // It is enough that even one subtree is not fully selected
            // to determine that the parent is not fully selected
            if (!allSelected(childCheckedNode)) {
                parentCheckedNode.allChildrenSelected = false;      
            }
            // If at least one child is selected, selecting also the parent
            if (childCheckedNode.isSelected) {
                parentCheckedNode.isSelected = true;
            }
        }
        if (parentCheckedNode.isSelected) {
            checkedPaths.add(parentPath);
        } else {
            checkedPaths.remove(parentPath);
        }
        // Go to upper predecessor
        updatePredecessorsWithCheckMode(parentPath, check);
    }

    private boolean allSelected(CheckedNode checkedNode) {
        if (!checkedNode.isSelected) {
            return false;
        }
        if (checkedNode.hasChildren) {
            return checkedNode.allChildrenSelected;
        }
        return true;
    }

    // Recursively checks/unchecks a subtree
    public void checkSubTree(TreePath tp, boolean check) {
        CheckedNode cn = nodesCheckingState.get(tp);
        cn.isSelected = check;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
        for (int i = 0 ; i < node.getChildCount() ; i++) {              
            checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
        }
        cn.allChildrenSelected = check;
        if (check) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
        updatePredecessorsAllChildrenSelectedState(tp);
    }

    public void expandAll() {
    	for (int i = 0; i < getRowCount(); i++) {
    		expandRow(i);
    	}
    }

    public void collapseAll() {
    	for (int i = getRowCount(); i >= 0; i--) {
    		this.collapseRow(i);
    	}
    }
    
    public boolean isSelectedFully(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return allSelected(cn);
    }

    // Recursively checks/unchecks a subtree
    public void check(TreePath tp, boolean check) {
        CheckedNode cn = nodesCheckingState.get(tp);
        cn.isSelected = check;
        if (check) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
        updatePredecessorsAllChildrenSelectedState(tp);
    }

    private void updatePredecessorsAllChildrenSelectedState(TreePath tp) {
        TreePath parentPath = tp.getParentPath();
        if (parentPath == null) {
            return;
        }
        CheckedNode parentCheckedNode = nodesCheckingState.get(parentPath);
        parentCheckedNode.allChildrenSelected = true;
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
            CheckedNode childCheckedNode = nodesCheckingState.get(childPath);
            if (!allSelected(childCheckedNode)) {
                parentCheckedNode.allChildrenSelected = false;
                break;
            }
        }
        updatePredecessorsAllChildrenSelectedState(parentPath);
    }

    public void setCheckBoxEnabled(TreePath tp, boolean enabled) {
        nodesCheckingState.get(tp).isCheckBoxEnabled = enabled;
        JCheckBoxTree.this.repaint();                          
    }
    
    public static void main(String[] params) {
    	// Simple test code
    	JFrame f = new JFrame();
    	f.setSize(new Dimension(500,500));
    	
    	JPanel p = new JPanel();
    	p.setLayout(new BorderLayout());
    	p.setSize(new Dimension(500,500));
    	f.getContentPane().add(p);
    	JCheckBoxTree cbt = new JCheckBoxTree();
    	cbt.setShowsRootHandles(true);

    	JScrollPane scroll = new JScrollPane();
    	scroll.setViewportView(cbt);
    	p.add(scroll, BorderLayout.CENTER);
    	
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tech");
		DefaultMutableTreeNode db = new DefaultMutableTreeNode("Db");
		root.add(db);
		db.add(new DefaultMutableTreeNode("HypersonicSQL"));
		db.add(new DefaultMutableTreeNode("MsSQL"));
		db.add(new DefaultMutableTreeNode("MySQL"));
		db.add(new DefaultMutableTreeNode("Oracle"));
		db.add(new DefaultMutableTreeNode("PostgreSQL"));
		DefaultMutableTreeNode os = new DefaultMutableTreeNode("OS");
		root.add(os);
		os.add(new DefaultMutableTreeNode("Linux"));
		DefaultMutableTreeNode ws = new DefaultMutableTreeNode("WS");
		root.add(ws);
		DefaultTreeModel model = new DefaultTreeModel(root);
		cbt.setModel(model);

    	
    	f.setVisible(true);
    }

}