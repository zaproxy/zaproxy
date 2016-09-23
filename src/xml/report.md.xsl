<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:str="xalan://java.lang.String" 
  version="1.0"
  >
  <xsl:output method="text"/> 
  
  <xsl:variable name='newline'><xsl:text>
</xsl:text></xsl:variable>
 
  <xsl:template match="/OWASPZAPReport">
#ZAP Scanning Report

<xsl:apply-templates select="text()"/>

##Summary of Alerts

| Risk Level | Number of Alerts |
| --- | --- |
| High | <xsl:value-of select="count(descendant::alertitem[riskcode='3'])"/> |
| Medium | <xsl:value-of select="count(descendant::alertitem[riskcode='2'])"/> |
| Low | <xsl:value-of select="count(descendant::alertitem[riskcode='1'])"/> |
| Informational | <xsl:value-of select="count(descendant::alertitem[riskcode='0'])"/> |

##Alert Detail

<xsl:apply-templates select="descendant::alertitem">
  <xsl:sort order="descending" data-type="number" select="riskcode"/>
  <xsl:sort order="descending" data-type="number" select="confidence"/>
</xsl:apply-templates>
</xsl:template>

  <!-- Top Level Heading -->
  <xsl:template match="alertitem">
<xsl:apply-templates select="text()|name|desc|uri|method|param|attack|evidence|instances|count|otherinfo|solution|reference|cweid|wascid|p|br|wbr|ul|li"/>
  </xsl:template>

  <xsl:template match="name[following-sibling::riskcode='3']">
### <xsl:apply-templates select="text()"/>
##### <xsl:value-of select="following-sibling::riskdesc"/>
  </xsl:template>

  <xsl:template match="name[following-sibling::riskcode='2']">
### <xsl:apply-templates select="text()"/>
##### <xsl:value-of select="following-sibling::riskdesc"/>
  </xsl:template>

  <xsl:template match="name[following-sibling::riskcode='1']">
### <xsl:apply-templates select="text()"/>
##### <xsl:value-of select="following-sibling::riskdesc"/>
  </xsl:template>
  
  <xsl:template match="name[following-sibling::riskcode='0']">
### <xsl:apply-templates select="text()"/>
##### <xsl:value-of select="following-sibling::riskdesc"/>
  </xsl:template>

  <xsl:template match="desc">
#### Description
<xsl:apply-templates select="text()|*"/>
  </xsl:template>

  <xsl:template match="uri">
### URL
[<xsl:apply-templates select="text()|*"/>](<xsl:apply-templates select="text()|*"/>)
  </xsl:template>

  <xsl:template match="method">
### Method
<xsl:apply-templates select="text()|*"/>
  </xsl:template>

  <xsl:template match="param">
  <xsl:if test="text() !=''">
### Parameter
<xsl:apply-templates select="text()|*"/>
  </xsl:if>
  </xsl:template>

<xsl:template match="attack">
  <xsl:if test="text() !=''">
### Attack
<xsl:apply-templates select="text()|*"/>
  </xsl:if>
  </xsl:template>

  <xsl:template match="evidence">
  <xsl:if test="text() !=''">
### Evidence
<xsl:apply-templates select="text()|*"/>
  </xsl:if>
  </xsl:template>

  <xsl:template match="instances/instance/uri">
* URL: [<xsl:apply-templates select="text()|*"/>](<xsl:apply-templates select="text()|*"/>)
  </xsl:template>

  <xsl:template match="instances/instance/method">
  * Method: `<xsl:apply-templates select="text()|*"/>`
  </xsl:template>
 
  <xsl:template match="instances/instance/param"><xsl:if test="text() !=''">
  * Parameter: `<xsl:apply-templates select="text()|*"/>`
  </xsl:if></xsl:template>

  <xsl:template match="instances/instance/attack"><xsl:if test="text() !=''">
  * Attack: `<xsl:apply-templates select="text()|*"/>`
  </xsl:if></xsl:template>

  <xsl:template match="instances/instance/evidence"><xsl:if test="text() !=''">
  * Evidence: `<xsl:apply-templates select="text()|*"/>`
  </xsl:if></xsl:template>

  <xsl:template match="count"><xsl:if test="text() !=''">
Instances: <xsl:apply-templates select="text()|*"/>
  </xsl:if></xsl:template>

  <xsl:template match="otherinfo">
### Other information
<xsl:apply-templates select="text()|*"/>
  </xsl:template>

  <xsl:template match="solution">
### Solution
<xsl:apply-templates select="text()|*"/>
  </xsl:template>

  <xsl:template match="reference">
### Reference
<xsl:variable name="refValue">
<xsl:value-of select="str:replaceAll(str:new(text()),'&lt;p&gt;','* ')"/>
</xsl:variable>
<xsl:variable name="refValue2">
<xsl:value-of select="str:replaceAll(str:new($refValue),'&lt;/p&gt;',$newline)"/>
</xsl:variable>
<xsl:value-of select="$refValue2"/>
  </xsl:template>

  <xsl:template match="cweid">
#### CWE Id : <xsl:apply-templates select="text()|*"/>
  </xsl:template>
  
  <xsl:template match="wascid">
#### WASC Id : <xsl:apply-templates select="text()|*"/>
  </xsl:template>
  
  <xsl:template match="p">

  <xsl:apply-templates select="text()|*"/>
  </xsl:template> 

  <xsl:template match="br">

  <xsl:apply-templates/>
  </xsl:template> 

  <xsl:template match="ul">

  <xsl:apply-templates select="text()|*"/>
  </xsl:template> 

  <xsl:template match="li">
* <xsl:apply-templates select="text()|*"/>
  </xsl:template> 
  
  <xsl:template match="wbr">
  </xsl:template> 

</xsl:stylesheet>