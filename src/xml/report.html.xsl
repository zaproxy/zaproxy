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
<img src="data:image/png;base64,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"/>
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
