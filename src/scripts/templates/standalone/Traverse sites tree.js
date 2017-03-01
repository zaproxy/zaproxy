// This script traverses the sites tree - change it to do whatever you want to do :)
//
// Standalone scripts have no template.
// They are only evaluated when you run them. 

// The following handles differences in printing between Java 7's Rhino JS engine
// and Java 8's Nashorn JS engine
if (typeof println == 'undefined') this.println = print;

function listChildren(node, level) {
    var j;
    for (j=0;j<node.getChildCount();j++) {
        println(Array(level+1).join("    ") + node.getChildAt(j).getNodeName());
        listChildren(node.getChildAt(j), level+1);
    }
}

root = org.parosproxy.paros.model.Model.getSingleton().
        getSession().getSiteTree().getRoot();

listChildren(root, 0);


