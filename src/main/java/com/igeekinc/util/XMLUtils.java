/*
 * Copyright 2002-2014 iGeek, Inc.
 * All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.igeekinc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils
{
    
    public XMLUtils()
    {
    }
    public static Element appendSingleValElement(
            Document owner,
            Element appendElement,
            String newElemName,
            String newElemVal)
    {
        Element newElem;
        newElem = owner.createElement(newElemName);
        if (newElemVal != null)
        {
            Text value = owner.createTextNode(newElemVal);
            newElem.appendChild(value);
        }
        appendElement.appendChild(newElem);
        return (newElem);
    }
    
    public static Element appendSingleValElementEncoded(
            Document owner,
            Element appendElement,
            String newElemName,
            String newElemVal)
    {
        Element newElem;
        newElem = owner.createElement(newElemName);
        if (newElemVal != null)
        {
            String encodedVal="";
            try
            {
                encodedVal = URLEncoder.encode(newElemVal, "UTF-8");
            } catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Text value = owner.createTextNode(encodedVal);
            newElem.appendChild(value);
            newElem.setAttribute("enc", "t");
            newElem.setAttribute("charSet", "UTF-8");
        }
        appendElement.appendChild(newElem);
        return (newElem);
    }
    
    static public String outputTextDoc(Node outputNode)
    {
        StringWriter out = new StringWriter();
        PrintWriter pOut = new PrintWriter(out);
        outputNode(outputNode, pOut, 0);
        return (out.toString());
    }
    
    public static int outputNode(
            Node outputNode,
            PrintWriter outputWriter,
            int curPos)
    {
        NodeList nodes = outputNode.getChildNodes();
        int curNodeNum;
        if (outputNode.getNodeType() == Node.TEXT_NODE)
        {
            outputWriter.print(outputNode.getNodeValue());
        }
        else
        {
            if (outputNode.getNodeName().equals("p"))
            {
                outputWriter.println();
            }
            
            for (curNodeNum = 0; curNodeNum < nodes.getLength(); curNodeNum++)
            {
                Node curNode = nodes.item(curNodeNum);
                curPos = outputNode(curNode, outputWriter, curPos);
            }
            
        }
        return (curPos);
    }
    static public Document getDocument(String input)
    throws SAXException, IOException
    {
        DOMParser parser;
        InputSource inSource;
        StringReader stringReader = new StringReader(input);
        
        parser = new DOMParser();
        inSource = new InputSource(stringReader);
        parser.parse(inSource);
        Document doc = parser.getDocument();
        return (doc);
    }
    
    static public Document getDocument(InputStream input)
    throws SAXException, IOException
    {
        InputSource inSource;
        DOMParser parser;
        
        parser = new DOMParser();
        inSource = new InputSource(input);
        parser.parse(inSource);
        Document doc = parser.getDocument();
        return (doc);
    }
    static public String getNodeValue(Element parent, String nodeName)
    {
        NodeList nodes = parent.getElementsByTagName(nodeName);
        if (nodes.getLength() == 0)
            return null;
        Element curElem = (Element)nodes.item(0);
        Node textNode = curElem.getFirstChild();
        if (textNode != null)
        {
            String encVal = curElem.getAttribute("enc");
            String charSetVal = curElem.getAttribute("charSet");
            String returnVal = textNode.getNodeValue();
            if (encVal != null &&encVal.equals("t"))
            {	
                if (charSetVal == null)
                    return(URLDecoder.decode(returnVal));
                else
                    try
                    {
                        return(URLDecoder.decode(returnVal, charSetVal));
                    } catch (UnsupportedEncodingException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
            return (returnVal);
        }
        else
            return ("");
    }
    static public Element getElementByName(Element parent, String nodeName)
    {
        NodeList nodes = parent.getElementsByTagName(nodeName);
        if (nodes.getLength() == 0)
            return null;
        
        return ((Element) nodes.item(0));
    }
    static public Date parseDateNode(Element dateNode) throws ParseException
    {
        
        String month = getNodeValue(dateNode, "month");
        String day = getNodeValue(dateNode, "day");
        String year = getNodeValue(dateNode, "year");
        Calendar buildCalen;
        String hour = getNodeValue(dateNode, "hour");
        String minute = getNodeValue(dateNode, "minute");
        String second = getNodeValue(dateNode, "second");
        if (hour != null && minute != null & second != null)
        {	
            buildCalen =
                new java.util.GregorianCalendar(
                        Integer.parseInt(year),
                        Integer.parseInt(month) - 1,
                        Integer.parseInt(day),
                        Integer.parseInt(hour),
                        Integer.parseInt(minute),
                        Integer.parseInt(second));
        }
        else
        {
            buildCalen =
                new java.util.GregorianCalendar(
                        Integer.parseInt(year),
                        Integer.parseInt(month) - 1,
                        Integer.parseInt(day));
        }
        
        return (buildCalen.getTime());
    }
    
    static public void appendDateNode(
            Document owner,
            Element appendElement,
            String name,
            Date date)
    {
        Element dateElem = owner.createElement(name);
        appendElement.appendChild(dateElem);
        appendSingleValElement(
                owner,
                dateElem,
                "month",
                Integer.toString(date.getMonth() + 1));
        appendSingleValElement(
                owner,
                dateElem,
                "day",
                Integer.toString(date.getDate()));
        appendSingleValElement(
                owner,
                dateElem,
                "year",
                Integer.toString(date.getYear() + 1900));
        appendSingleValElement(
                owner,
                dateElem,
                "hour",
                Integer.toString(date.getHours()));
        appendSingleValElement(
                owner,
                dateElem,
                "minute",
                Integer.toString(date.getMinutes()));
        appendSingleValElement(
                owner,
                dateElem,
                "second",
                Integer.toString(date.getSeconds()));
    }
    static public void fillHashtable(NodeList list, Hashtable<String, String> fillIn)
    {
        for (int curElemNum = 0; curElemNum < list.getLength(); curElemNum++)
        {
            if (list.item(curElemNum).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curValue = (Element) list.item(curElemNum);
            String valueName = curValue.getNodeName();
            String value = curValue.getFirstChild().getNodeValue();
            fillIn.put(valueName, value);
        }
    }
    static public void serializeToFile(
            Document serializeDocument,
            File outputFile)
    throws IOException
    {
        FileOutputStream serializeStream = new FileOutputStream(outputFile);
        serializeToStream(serializeDocument, serializeStream);
        serializeStream.close();
    }
    static public void serializeToStream(
            Document serializeDocument,
            OutputStream outStream)
    throws IOException
    {
        OutputFormat defOF = new OutputFormat(Method.XML, "UTF-8", false);
        defOF.setOmitXMLDeclaration(true);
        defOF.setOmitDocumentType(true);
        defOF.setIndenting(true);
        XMLSerializer xmlSer = new XMLSerializer(outStream, defOF);
        
        xmlSer.serialize(serializeDocument);
    }
}