<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:report="http://annocultor.eu/report/">

  <xsl:template match="/rdf:RDF">
    <html>
      <head>
        <script type="text/javascript">    
          function showAll() {
          var divs = document.getElementsByTagName("div");
          var i=0;
          for(i=0;i&lt;divs.length;i++)
          {
          divs[i].style.display="block";
          }
          };
          
          function switchState(boxID, prefix) {
          var boxObj = document.getElementById(boxID);
          var buttonObj = document.getElementById(boxID+"_anchor");
          if (boxObj.style.display=="none") {
          boxObj.style.display="block";
          } 
          else { 
          boxObj.style.display="none";
          }
          }
     </script>
        <style type="text/css">
       div.detail {
        display: none;
        margin-left: 2em;
        boder: 1px solid grey;
       }
       p {
          background-color:white;
          border-top: 1px solid #EEE;
          }
         th {
           background-color: #EEE;
          font-size: 80%;
          }
     </style>
      </head>
      <body>
        <p onclick="showAll()"><b>This is the list of properties. Click on it to see the top ten values of each property.Click here to show all top-ten lists.</b></p>
        <xsl:for-each select="rdf:Description">
          <xsl:element name="p">
            <xsl:attribute name="onclick">switchState('<xsl:value-of select="report:name"/>','');return false;</xsl:attribute>
            <xsl:attribute name="id">      <xsl:value-of select="report:name"/>_anchor </xsl:attribute>            
            <code><b>
              <xsl:value-of select="report:name"/>
              </b>
            </code>  
            , total values:
            <code>
              <xsl:value-of select="report:totalValues"/>
            </code>
            <xsl:if test="report:allUnique='true'">
              , ALL UNIQUE
            </xsl:if>
          </xsl:element>
            <xsl:element name="div">
              <xsl:attribute name="class">detail</xsl:attribute>
              <xsl:attribute name="style">display:none;</xsl:attribute>
              <xsl:attribute name="id">
                <xsl:value-of select="report:name"/>
              </xsl:attribute>
              <table>
                <tr>
                  <th>Value</th>
                  <th>Occurances this value</th>
                  <th>Coverage records, %        </th>
                </tr>
                <xsl:for-each select="report:value">
                  <tr>
                  	<xsl:variable name="counts" select="tokenize(.,',')"/>
                    <td>
                      <code>
                      <xsl:value-of select="substring-after(.,string-join(($counts[1],$counts[2],$counts[3],''),','))"/>
                        </code>
                    </td>
                    <td>
                      <code>
                        <xsl:value-of select="$counts[2]"/>
                        </code>
                    </td>
                    <td>
                      <code>
                        <xsl:value-of select="$counts[3]"/>
                        </code>
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
            </xsl:element>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
