// Standalone scripts have no template.
// They are only evaluated when you run them. 

// The following handles differences in printing between Java 7's Rhino JS engine
// and Java 8's Nashorn JS engine
if (typeof println == 'undefined') this.println = print;
