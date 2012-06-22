<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- OWASP ZAP XSL file for generating XML output. -->
<!-- Works with OWASP ZAP 1.2.0+ from code.google.com/p/zaproxy/ -->
<!-- Place this file in the ZAP xml directory. -->
<!-- Removes the <p></p> tags, keep the contents, add a line break. -->
<xsl:template match="p">
	<xsl:apply-templates/>
	<xsl:text>
	</xsl:text>
</xsl:template>
<!-- Removes the <ul></ul> tags, keep the contents. -->
<xsl:template match="ul">
	<xsl:apply-templates/>
</xsl:template>
<!-- Removes the <li></li> tags, keep the contents, add a line break. -->
<xsl:template match="li">
	<xsl:apply-templates/>
	<xsl:text>
	</xsl:text>
</xsl:template>
<!-- Removes the <wbr></wbr> tags, keep the contents. -->
<xsl:template match="wbr">
	<xsl:apply-templates/>
</xsl:template>
<!-- Copy the remainder of the XML as is. -->
<xsl:template match="@* | node()">
	<xsl:copy>
		<xsl:apply-templates select="@* | node()"/>
	</xsl:copy>
</xsl:template>
</xsl:stylesheet>
