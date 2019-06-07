<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  >
  <xsl:output method="html"/> 
 
  <xsl:template match="/report">
<html>
<head>
<title>ZAP Session Compare Report</title>

</head>

<body text="#000000">
<p><strong>ZAP Session Compare Report</strong></p>

<script>
function showAll() {
  var tb = document.getElementById('table');
  for (var i=1; i &lt; tb.rows.length; i++) {
    tb.rows[i].style.display='';
  }
}
function justSession1() {
  var tb = document.getElementById('table');
  for (var i=1; i &lt; tb.rows.length; i++) {
    if (tb.rows[i].cells[3].firstChild.firstChild.textContent != '---') {
      tb.rows[i].style.display='none';
    } else {
      tb.rows[i].style.display='';
    }
  }
}
function justSession2() {
  var tb = document.getElementById('table');
  for (var i=1; i &lt; tb.rows.length; i++) {
    if (tb.rows[i].cells[2].firstChild.firstChild.textContent != '---') {
      tb.rows[i].style.display='none';
    } else {
      tb.rows[i].style.display='';
    }
  }
}
function inBoth() {
  var tb = document.getElementById('table');
  for (var i=1; i &lt; tb.rows.length; i++) {
    if (tb.rows[i].cells[2].firstChild.firstChild.textContent == '---') {
      tb.rows[i].style.display='none';
    } else if (tb.rows[i].cells[3].firstChild.firstChild.textContent == '---') {
      tb.rows[i].style.display='none';
    } else {
      tb.rows[i].style.display='';
    }
  }
}
</script>
<button onclick='showAll()'>Any session</button>
<button onclick='justSession1()'>Just session 1</button>
<button onclick='justSession2()'>Just session 2</button>
<button onclick='inBoth()'>Both sessions</button>

<!--p><strong>Summary of Alerts</strong></p-->
<table id="table" width="100%" border="0">
  <tr bgcolor="#666666"> 
    <th height="24"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif"> 
      Method</font></strong></th>
    <th height="24"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif"> 
      URL</font></strong></th>
    <th height="24"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif"> 
      <xsl:value-of select="session-names/session1"/></font></strong></th>
    <th height="24"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif"> 
      <xsl:value-of select="session-names/session2"/></font></strong></th>
  </tr>
  <xsl:for-each select="urlrow">
  <tr bgcolor="#e8e8e8"> 
    <td><font size="2" face="Arial, Helvetica, sans-serif">
      <xsl:value-of select="method"/>
    </font></td>
    <td><font size="2" face="Arial, Helvetica, sans-serif">
      <a href="{url}"><xsl:value-of select="url"/></a>
    </font></td>
    <td><font size="2" face="Arial, Helvetica, sans-serif">
      <xsl:value-of select="code1"/>
    </font></td>
    <td><font size="2" face="Arial, Helvetica, sans-serif">
      <xsl:value-of select="code2"/>
    </font></td>
  </tr>
  </xsl:for-each>
</table>
</body>
</html>
</xsl:template>

</xsl:stylesheet>