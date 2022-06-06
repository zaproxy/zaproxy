// This script traverses the sites tree - change it to do whatever you want to do :)
//
// Standalone scripts have no template.
// They are only evaluated when you run them. 

function listChildren(node, level) {
    var j;
    for (j=0;j<node.getChildCount();j++) {
        print(Array(level+1).join("    ") + node.getChildAt(j).getNodeName());
        listChildren(node.getChildAt(j), level+1);
    }
}

root = model.getSession().getSiteTree().getRoot();

listChildren(root, 0);


