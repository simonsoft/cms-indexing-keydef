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
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:indexfn="http://www.simonsoft.se/namespace/cms-indexing-functions"
    exclude-result-prefixes="xs" 
    version="2.0">



    <xsl:param name="patharea"/>
    <xsl:param name="locale"/>
    <xsl:param name="prefix" select="''"></xsl:param>
    <xsl:param name="defaultSheet" select="'keydefmap'"></xsl:param>
    <xsl:param name="enableDescr" as="xs:boolean" select="false()"/>
    <xsl:param name="enableIDParentGroupPrefix" as="xs:boolean" select="true()"/>
    <xsl:param name="IDParentGroupSeparator" as="xs:string" select="'.'"></xsl:param>
    
    <xsl:param name="newline" select="'&#xa;'"/>
    
    <!-- Indexing will store keydefs only, no root element. -->
    <xsl:output omit-xml-declaration="yes" />
    
    <xsl:template match="/*">
        <xsl:message select="'Transforming Excel into keydefmap.'"/>
        <xsl:apply-templates select="xhtml:body"/>
    </xsl:template>

    <xsl:template match="xhtml:body">
        
        <xsl:if test="empty(//xhtml:div)">
            <xsl:message select="'No sheets detected in Excel file.'" terminate="yes"/>
        </xsl:if>
        <!-- TODO: Select sheet logic. -->
        <xsl:variable name="sheet" as="element(xhtml:div)">
            <xsl:choose>
                <xsl:when test="xhtml:div[xhtml:h1/text() = $locale]">
                    <xsl:sequence select="xhtml:div[xhtml:h1/text() = $locale][1]"/>
                </xsl:when>
                
                <xsl:when test="xhtml:div[xhtml:h1/text() = $defaultSheet]">
                    <xsl:sequence select="xhtml:div[xhtml:h1/text() = $defaultSheet][1]"/>
                </xsl:when>
                
                <xsl:otherwise>
                    <xsl:sequence select="xhtml:div[1]"/>
                </xsl:otherwise>
            </xsl:choose>
            
        </xsl:variable>
        
        
        
        <xsl:variable name="msg" select="concat('Transforming Excel sheet: &quot;', $sheet/xhtml:h1/text()[1], '&quot;')"/>
        <xsl:comment select="$msg"/>
        <xsl:message select="$msg"/>
        
        <xsl:apply-templates select="$sheet" mode="excel-simple"/>
        
    </xsl:template>

    <xsl:template match="xhtml:div" mode="excel-simple">
        
        <xsl:choose>
            <xsl:when test="not(indexfn:validate-column-count(., 2, 3))">
                <xsl:value-of select="$newline"/>
                <xsl:comment select="'Sheet failed column count validation.'"/>
                <xsl:value-of select="$newline"/>
                <xsl:comment select="indexfn:error-column-count(., 2, 3)"></xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$newline"/>
                <xsl:apply-templates select=".//xhtml:tr" mode="#current">
                    <xsl:with-param name="prefix" select="$prefix"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
        
        
    </xsl:template>

    <xsl:template match="xhtml:tr[xhtml:td]" mode="excel-simple">
        <xsl:param name="prefix"/>
        
        <xsl:variable name="key" select="xhtml:td[1]"/>
        <xsl:variable name="group-prefix">
            <!-- Not used in this transform. -->
            <xsl:value-of select="''"/>
        </xsl:variable>

        <xsl:element name="keydef">
            <xsl:attribute name="keys" select="indexfn:key-valid(concat($prefix, $group-prefix, $key))"/>
            <xsl:element name="topicmeta">
                <xsl:if test="$enableDescr">
                    <!-- Not used in this transform. -->
                    <xsl:apply-templates select="comment"/>
                </xsl:if>
                <xsl:element name="keywords">
                    <xsl:element name="keyword">
                        <xsl:apply-templates select="xhtml:td[2]|xhtml:td[3]" mode="#current"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
        <xsl:value-of select="$newline"/>
    </xsl:template>
    
    <xsl:template match="xhtml:tr[1][xhtml:td[1]/text() = 'Key']" mode="excel-simple" priority="100">
        <!-- Suppress header row if first column header is 'Key'. -->
    </xsl:template>
    
    <xsl:template match="xhtml:tr" mode="excel-simple" priority="-1">
        <xsl:message select="'Empty row, suppressing.'"></xsl:message>
    </xsl:template>
    
    
    <xsl:template match="xhtml:td" mode="excel-simple">
        
        <xsl:apply-templates mode="keyword"/>
    </xsl:template>
    
    
    <xsl:template match="text()" mode="keyword">
        <xsl:copy/>
    </xsl:template>
    
    <xsl:template match="text()[preceding-sibling::element()]" mode="keyword"  priority="100">
        <!-- Suppressing the content of Excel comment / note. -->
    </xsl:template>
    
    <xsl:template match="xhtml:br" mode="keyword" priority="100">
        <xsl:message select="'Excel comment or note, suppressing.'"/>
    </xsl:template>
    
    <xsl:template match="*" mode="keyword" priority="50">
        <xsl:comment select="'Unknown element, suppressing.'"/>
        <xsl:message select="'Unknown element, suppressing.'"/>
    </xsl:template>
    
    
    <xsl:function name="indexfn:validate-column-count" as="xs:boolean">
        <xsl:param name="sheet" as="element()"/>
        <xsl:param name="mincol"/>
        <xsl:param name="maxcol"/>
        
        <xsl:variable name="rows" select="$sheet//xhtml:tr[xhtml:td][count(xhtml:td) > $maxcol or $mincol > count(xhtml:td)]"/>
        
        <xsl:if test="$rows">
            <xsl:message select="concat('Rows failing column count: ', count($rows))"></xsl:message>
        </xsl:if>
        
        <xsl:sequence select="not(boolean($rows))"/>
    </xsl:function>
    
    <xsl:function name="indexfn:error-column-count" as="text()*">
        <xsl:param name="sheet" as="element()"/>
        <xsl:param name="mincol"/>
        <xsl:param name="maxcol"/>
        
        <xsl:variable name="rows" select="$sheet//xhtml:tr[xhtml:td][count(xhtml:td) > $maxcol or $mincol > count(xhtml:td)]"/>
        
            <xsl:for-each select="$rows">
                <xsl:variable name="rowno" select="1 + count(preceding-sibling::xhtml:tr)"/>
                <xsl:variable name="rowcontent" select="string-join(.//xhtml:td, '|')"/>
                <xsl:variable name="msg" select="concat('Incorrect column count on row ', $rowno, ': &quot;', $rowcontent, '&quot;')"/>
                <xsl:value-of select="$msg"/>
                <xsl:message select="$msg"/>
            </xsl:for-each>
    </xsl:function>
    
    <xsl:function name="indexfn:key-valid" as="xs:string">
        <xsl:param name="key"/>
        <!-- Function copied btw keydefmap XSL files. -->
        <xsl:value-of select="translate($key, ' {}[]/#?', '........')"/>
    </xsl:function>

</xsl:stylesheet>
