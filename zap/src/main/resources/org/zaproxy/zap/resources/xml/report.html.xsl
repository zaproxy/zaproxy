<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  >
  <xsl:output method="html"/>

  <xsl:template match="/OWASPZAPReport"> 

<html>
<head>
<!-- ZAP: rebrand -->
<title>ZAP Scanning Report</title>

<style>
body{
  font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
  color: #000;
  font-size: 13px;
}
h1{
  text-align: center;
  font-weight: bold;
  font-size: 32px
}
h3{
  font-size: 16px;
}
table{
  border: none;
  font-size: 13px;
}
td, th {
  padding: 3px 4px;
  word-break: break-word;
}
th{
  font-weight: bold;
}
.results th{
  text-align: left;
}
.spacer{
  margin: 10px;
}
.spacer-lg{
  margin: 40px;
}
.indent1{
  padding: 4px 20px;
}
.indent2{
  padding: 4px 40px;
}
.risk-high{
  background-color: red;
  color: #FFF;
}
.risk-medium{
  background-color: orange;
  color: #FFF;
}
.risk-low{
  background-color: yellow;
  color: #000;
}
.risk-info{
  background-color: blue;
  color: #FFF;
}
.summary th{
  color: #FFF;
}
</style>
</head>

<body>
<!-- ZAP: rebrand -->
<h1>
<img src="data:image/png;base64,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" />
ZAP Scanning Report
</h1>

<p>
<xsl:apply-templates select="text()"/>
</p>
<h3>Summary of Alerts</h3>
<table width="45%" class="summary">
  <tr bgcolor="#666666"> 
    <th width="45%" height="24">Risk 
      Level</th>
    <th width="55%" align="center">Number 
      of Alerts</th>
  </tr>
  <tr bgcolor="#e8e8e8"> 
    <td><a href="#high">High</a></td>
    <td align="center">
      <xsl:value-of select="count(descendant::alertitem[riskcode='3'])"/>
    </td>
  </tr>
  <tr bgcolor="#e8e8e8"> 
    <td><a href="#medium">Medium</a></td>
    <td align="center">
      <xsl:value-of select="count(descendant::alertitem[riskcode='2'])"/>
    </td>
  </tr>
  <tr bgcolor="#e8e8e8"> 
    <td><a href="#low">Low</a></td>
    <td align="center">
      <xsl:value-of select="count(descendant::alertitem[riskcode='1'])"/>
    </td>
  </tr>
  <tr bgcolor="#e8e8e8"> 
    <td><a href="#info">Informational</a></td>
    <td align="center">
      <xsl:value-of select="count(descendant::alertitem[riskcode='0'])"/>
    </td>
  </tr>
</table>
<div class="spacer-lg"></div>
<h3>Alert Detail</h3>

<xsl:apply-templates select="descendant::alertitem">
  <xsl:sort order="descending" data-type="number" select="riskcode"/>
  <xsl:sort order="descending" data-type="number" select="confidence"/>
</xsl:apply-templates>
</body>
</html>
</xsl:template>

  <!-- Top Level Heading -->
  <xsl:template match="alertitem">
<div class="spacer"></div>
<table width="100%" class="results">
<xsl:apply-templates select="text()|name|desc|uri|method|param|attack|evidence|instances|count|otherinfo|solution|reference|cweid|wascid|sourceid|p|br|wbr|ul|li"/>
</table>
  </xsl:template>

  <xsl:template match="name[following-sibling::riskcode='3']">
  <tr height="24" class="risk-high">
    <th width="20%">
      <a name="high"/>
      <xsl:value-of select="following-sibling::riskdesc"/>
    </th>
    <th width="80%">
      <xsl:apply-templates select="text()"/>
    </th>
  </tr>
  </xsl:template>

  <xsl:template match="name[following-sibling::riskcode='2']">
  <!-- ZAP: Changed the medium colour to orange -->
  <tr height="24" class="risk-medium">
    <th width="20%">
      <a name="medium"/>
      <xsl:value-of select="following-sibling::riskdesc"/>
    </th>
    <th width="80%">
      <xsl:apply-templates select="text()"/>
    </th>
  </tr>

  </xsl:template>
  <xsl:template match="name[following-sibling::riskcode='1']">
  <!-- ZAP: Changed the low colour to yellow -->
  <tr height="24" class="risk-low">
    <a name="low"/>
    <th width="20%">
    <xsl:value-of select="following-sibling::riskdesc"/>
    </th>
    <th width="80%">
      <xsl:apply-templates select="text()"/>
    </th>
  </tr>
  </xsl:template>

  <xsl:template match="name[following-sibling::riskcode='0']">
  <tr height="24" class="risk-info">
    <th width="20%">
      <a name="info"/>
      <xsl:value-of select="following-sibling::riskdesc"/>
    </th>
    <th width="80%">
      <xsl:apply-templates select="text()"/>
    </th>
  </tr>
  </xsl:template>


<!--
  <xsl:template match="riskdesc">
  <tr>
    <td width="20%">Risk</td> 
    <td width="20%">
    <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>
-->

  <xsl:template match="desc">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">Description</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  <TR vAlign="top"> 
    <TD colspan="2"> </TD>
  </TR>
  
  </xsl:template>

  <xsl:template match="uri">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent1">URL</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>

  <xsl:template match="method">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent1">Method</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>

  <xsl:template match="param">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent1">Parameter</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="attack">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent1">Attack</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="evidence">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent1">Evidence</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="instances/instance/uri">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent1">URL</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>

  <xsl:template match="instances/instance/method">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent2">Method</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>

  <xsl:template match="instances/instance/param">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent2">Parameter</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="instances/instance/attack">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent2">Attack</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="instances/instance/evidence">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%" class="indent2">Evidence</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="count">
  <xsl:if test="text() !=''">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">Instances</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:if>
  </xsl:template>

  <xsl:template match="otherinfo">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">Other information</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>

  <TR vAlign="top"> 
    <TD colspan="2"> </TD>
  </TR>
  </xsl:template>

  <xsl:template match="solution">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">Solution</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>

  <xsl:template match="reference">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">Reference</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>
  
  <xsl:template match="cweid">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">CWE Id</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>
  
  <xsl:template match="wascid">
  <tr bgcolor="#e8e8e8"> 
    <td width="20%">WASC Id</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
  </tr>
  </xsl:template>
  
  <xsl:template match="sourceid">
  <tr bgcolor="#e8e8e8">
    <td width="20%">Source ID</td>
    <td width="80%">
      <xsl:apply-templates select="text()|*"/>
    </td>
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
