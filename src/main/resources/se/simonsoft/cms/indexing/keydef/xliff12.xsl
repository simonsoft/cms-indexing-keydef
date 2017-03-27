<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (C) 2009-2016 Simonsoft Nordic AB

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xlf="urn:oasis:names:tc:xliff:document:1.2" 
    exclude-result-prefixes="xs" 
    version="2.0">

    <!-- NOTE: Adjust the xlf namespace above to match the source files. Have tested 1.1 and 1.2. -->

    <xsl:param name="patharea"/>
    <xsl:param name="locale"/>
    <xsl:param name="prefix" select="''"></xsl:param>
    <xsl:param name="enableDescr" as="xs:boolean" select="false()"/>
    <xsl:param name="enableIDParentGroupPrefix" as="xs:boolean" select="true()"/>
    <xsl:param name="IDParentGroupSeparator" as="xs:string" select="'.'"></xsl:param>
    
    <xsl:param name="newline" select="'&#xa;'"/>
    
    <!-- Indexing will store keydefs only, no root element. -->
    <xsl:output omit-xml-declaration="yes" />

    <xsl:template match="xlf:xliff">
        <xsl:apply-templates select="//xlf:trans-unit">
            <xsl:with-param name="prefix" select="$prefix"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="xlf:trans-unit[@id]">
        <xsl:param name="prefix"/>
        <xsl:variable name="key" select="@id"/>
        <xsl:variable name="group-prefix">
            <xsl:if test="$enableIDParentGroupPrefix">
                <xsl:value-of select="concat(parent::xlf:group/@id, $IDParentGroupSeparator)"/>
            </xsl:if>
        </xsl:variable>

        <xsl:element name="keydef">
            <xsl:attribute name="keys" select="translate(concat($prefix, $group-prefix, $key), '{}[]/#?', '.......')"/>
            <xsl:element name="topicmeta">
                <xsl:if test="$enableDescr">
                    <xsl:apply-templates select="comment"/>
                </xsl:if>
                <xsl:element name="keywords">
                    <xsl:element name="keyword">
                        <xsl:apply-templates select="xlf:source|xlf:target" mode="keyword"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
        <xsl:value-of select="$newline"/>
    </xsl:template>
    
    
    <xsl:template match="xlf:source" mode="keyword">
        <!-- Use source when item is not a translation. -->
        <xsl:if test="$patharea != 'translation'">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="xlf:target" mode="keyword">
        <!-- Use target when item is a translation. -->
        <xsl:if test="$patharea = 'translation'">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="text()" mode="keyword">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>
