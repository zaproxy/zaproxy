<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  >
  <xsl:output method="html"/> 
 
  <xsl:template match="/report">
<html>
<head>
<!-- ZAP: rebrand -->
<title>ZAP Scanning Report</title>

</head>

<body text="#000000">
<!-- ZAP: rebrand -->
<p><strong>ZAP Scanning Report</strong></p>

<p>
<xsl:apply-templates select="text()"/>
</p>
<p><strong>Summary of Alerts</strong></p>
<table width="45%" border="0">
  <tr bgcolor="#666666"> 
    <td width="45%" height="24"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">Risk 
      Level</font></strong></td>
    <td width="55%" align="center"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">Number 
      of Alerts</font></strong></td>
  </tr>
  <tr bgcolor="#e8e8e8"> 
    <td><font size="2" face="Arial, Helvetica, sans-serif"><a href="#high">High</a></font></td>
    <td align="center"><font size="2" face="Arial, Helvetica, sans-serif">
    <xsl:value-of select="count(/report/alertitem[riskcode='3'])"/>
    </font></td>
  </tr>
  <tr bgcolor="#e8e8e8"> 
    <td><font size="2" face="Arial, Helvetica, sans-serif"><a href="#medium">Medium</a></font></td>
    <td align="center"><font size="2" face="Arial, Helvetica, sans-serif">
    <xsl:value-of select="count(/report/alertitem[riskcode='2'])"/>
    </font></td>
  </tr>
    <tr bgcolor="#e8e8e8"> 
    <td><font size="2" face="Arial, Helvetica, sans-serif"><a href="#low">Low</a></font></td>
    <td align="center"><font size="2" face="Arial, Helvetica, sans-serif">
    <xsl:value-of select="count(/report/alertitem[riskcode='1'])"/>
    </font></td>
  </tr>
    <tr bgcolor="#e8e8e8"> 
    <td><font size="2" face="Arial, Helvetica, sans-serif"><a href="#info">Informational</a></font></td>
    <td align="center"><font size="2" face="Arial, Helvetica, sans-serif">
    <xsl:value-of select="count(/report/alertitem[riskcode='0'])"/>
    </font></td>
  </tr>
</table>
<p></p>
<p></p>
<p><strong>Alert Detail</strong></p>

<xsl:apply-templates select="alertitem">
  <xsl:sort order="descending" data-type="number" select="riskcode"/>
  <xsl:sort order="descending" data-type="number" select="reliability"/>
</xsl:apply-templates>
</body>
</html>
</xsl:template>

  <!-- Top Level Heading -->
  <xsl:template match="alertitem">
<p></p>
<table width="100%" border="0">
<xsl:apply-templates select="text()|alert|desc|uri|param|attack|otherinfo|solution|reference|p|br|wbr|ul|li"/>
</table>
  </xsl:template>

  <xsl:template match="alert[following::riskcode='3']">
  <tr bgcolor="red" height="24">	
    <td width="20%" valign="top"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">
    <a name="high"/>
    <xsl:value-of select="following::riskdesc"/>
    </font></strong></td>
    <td width="80%"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">
      <xsl:apply-templates select="text()"/>
</font></strong></td>
  </tr>
  </xsl:template>

  <xsl:template match="alert[following::riskcode='2']">
  <!-- ZAP: Changed the medium colour to orange -->
  <tr bgcolor="orange" height="24">	
    <td width="20%" valign="top"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">
    <a name="medium"/>
    <xsl:value-of select="following::riskdesc"/>
	</font></strong></td>
    <td width="80%"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">
      <xsl:apply-templates select="text()"/>
</font></strong></td>
  </tr>

  </xsl:template>
  <xsl:template match="alert[following::riskcode='1']">
  <!-- ZAP: Changed the low colour to yellow -->
  <tr bgcolor="yellow" height="24">
    <a name="low"/>
    <td width="20%" valign="top"><strong><font color="#000000" size="2" face="Arial, Helvetica, sans-serif">
    <xsl:value-of select="following::riskdesc"/>
	</font></strong></td>
    <td width="80%"><strong><font color="#000000" size="2" face="Arial, Helvetica, sans-serif">
      <xsl:apply-templates select="text()"/>
</font></strong></td>
  </tr>
  </xsl:template>
  
  <xsl:template match="alert[following::riskcode='0']">
  <tr bgcolor="blue" height="24">	
    <td width="20%" valign="top"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">
    <a name="info"/>
    <xsl:value-of select="following::riskdesc"/>
	</font></strong></td>
    <td width="80%"><strong><font color="#FFFFFF" size="2" face="Arial, Helvetica, sans-serif">
      <xsl:apply-templates select="text()"/>
</font></strong></td>
  </tr>
  </xsl:template>


<!--
  <xsl:template match="riskdesc">
  <tr valign="top"> 
    <td width="20%"><font size="2" face="Arial, Helvetica, sans-serif">Risk</font></td>
    <td width="20%"><font size="2" face="Arial, Helvetica, sans-serif">
    <p>
    <xsl:apply-templates select="text()|*"/>
    </p>
    </font></td>
  </tr>
  </xsl:template>
-->

  <xsl:template match="desc">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><font size="2" face="Arial, Helvetica, sans-serif"><p>Description</p></font></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
    <xsl:apply-templates select="text()|*"/>
    </font></td>
  </tr>
  <TR vAlign="top"> 
    <TD colspan="2"> </TD>
  </TR>
  
  </xsl:template>

  <xsl:template match="uri">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><blockquote><font size="2" face="Arial, Helvetica, sans-serif">URL</font></blockquote></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
    <xsl:apply-templates select="text()|*"/>
    </font></td>
  </tr>
  </xsl:template>

  <xsl:template match="param">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><blockquote><font size="2" face="Arial, Helvetica, sans-serif">Parameter</font></blockquote></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
	<xsl:apply-templates select="text()|*"/>
    </font></td>
  </tr>
  </xsl:if>
  </xsl:template>

<xsl:template match="attack">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><blockquote><font size="2" face="Arial, Helvetica, sans-serif">Attack</font></blockquote></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
	<xsl:apply-templates select="text()|*"/>
    </font></td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="otherinfo">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><blockquote><font size="2" face="Arial, Helvetica, sans-serif">Other information</font></blockquote></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
	<xsl:apply-templates select="text()|*"/>
    </font></td>
  </tr>
  </xsl:if>

  <TR vAlign="top"> 
    <TD colspan="2"> </TD>
  </TR>
  </xsl:template>

  <xsl:template match="solution">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><font size="2" face="Arial, Helvetica, sans-serif"><p>Solution</p></font></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
	<xsl:apply-templates select="text()|*"/>
	</font></td>
  </tr>
  </xsl:template>

  <xsl:template match="reference">
  <tr bgcolor="#e8e8e8" valign="top"> 
    <td width="20%"><font size="2" face="Arial, Helvetica, sans-serif"><p>Reference</p></font></td>
    <td width="80%">
    <font size="2" face="Arial, Helvetica, sans-serif">
	<xsl:apply-templates select="text()|*"/>
    </font></td>
  </tr>
  </xsl:template>
  
  <xsl:template match="p">
  <p align="justify">
  <xsl:apply-templates select="text()|*"/>
  </p>
  </xsl:template> 

  <xsl:template match="br">
  <br/>
  <xsl:apply-templates/>
  </xsl:template> 

  <xsl:template match="ul">
  <ul>
  <xsl:apply-templates select="text()|*"/>
  </ul>
  </xsl:template> 

  <xsl:template match="li">
  <li>
  <xsl:apply-templates select="text()|*"/>
  </li>
  </xsl:template> 
  
  <xsl:template match="wbr">
  <wbr/>
  <xsl:apply-templates/>
  </xsl:template> 

</xsl:stylesheet>