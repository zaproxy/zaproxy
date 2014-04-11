// This script traverses the sites tree - change it to do whatever you want to do :)
//
// Standalone scripts have no template.
// They are only evaluated when you run them. 

function listChildren(node, level) {
    var i;
    for (i=0;i<level;i++) print ("    ");
    var j;
    for (j=0;j<node.getChildCount();j++) {
        println(node.getChildAt(j).getNodeName());
        listChildren(node.getChildAt(j), level+1);
    }
}

root = org.parosproxy.paros.model.Model.getSingleton().
        getSession().getSiteTree().getRoot();

listChildren(root, 0);


