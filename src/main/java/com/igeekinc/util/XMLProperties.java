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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLProperties extends Properties
{

    /**
     * 
     */
    private static final long serialVersionUID = 8449224662997797964L;

    public XMLProperties()
    {
        this(null);
    }

    public XMLProperties(Properties defaults)
    {
        super(defaults);
    }

    void insertIntoDocument(Document insertDocument, Element root, String name, String value)
    {
        StringTokenizer propertyNameTokenizer = new StringTokenizer(name, ".");
        int numTokens = propertyNameTokenizer.countTokens();
        Element curElement = root;
        for (int curTokenNum = 0; curTokenNum < numTokens; curTokenNum++)
        {
            String curElementName = propertyNameTokenizer.nextToken();
            char firstChar = curElementName.charAt(0);
            if (!Character.isLetter(firstChar) && firstChar != ':')
                curElementName = '_'+curElementName;
            NodeList nextElementList = curElement.getElementsByTagName(curElementName);
            Element nextElement;
            if (nextElementList.getLength() > 1)
                throw new InternalError("Document should have only one element for each component of pathname");
            if (nextElementList.getLength() == 1)
            {
                nextElement = (Element)nextElementList.item(0);
            }
            else // Doesn't exist
            {
                nextElement = insertDocument.createElement(curElementName);
                curElement.appendChild(nextElement);
            }
            curElement = nextElement;
        }
        Node valueNode = insertDocument.createTextNode(value);
        curElement.appendChild(valueNode);
    }

    public synchronized Document storeToDocument()
    {
        DocumentImpl returnDoc = new DocumentImpl();
        Element root = returnDoc.createElement("properties");
        returnDoc.appendChild(root);
        storeToDocument(returnDoc, returnDoc.getDocumentElement());
        return(returnDoc);
    }
    public synchronized Document storeToDocument(Document storeDoc, Element root)
    {
        Document returnDoc = storeDoc;
        Set<Object> keys = keySet();
        Iterator<Object> keyIterator = keys.iterator();
        while (keyIterator.hasNext())
        {
            String curName = (String)keyIterator.next();
            String curValue = (String)getProperty(curName);
            insertIntoDocument(returnDoc, root, curName, curValue);
        }
        return(returnDoc);
    }
    public synchronized void loadFromNode(Node curNode, String pathToNode)
    {
        String curNodeName = curNode.getNodeName();
        if (curNodeName.charAt(0) == '_')
            curNodeName = curNodeName.substring(1);

        String pathToThisNode;
        if (pathToNode != null)
            pathToThisNode = pathToNode+"."+curNodeName;
        else
            pathToThisNode=curNodeName;
        if (curNode.getNodeType() == Element.TEXT_NODE)
        {
            String nodeValue = curNode.getNodeValue().trim();
            if (pathToNode != null)
                put(pathToNode, nodeValue);
            return;
        }
        if (!curNode.hasChildNodes())
            return;
        NodeList children = curNode.getChildNodes();


        for (int curChildNum = 0; curChildNum < children.getLength(); curChildNum++)
        {
            Node curChildNode = children.item(curChildNum);
            loadFromNode(curChildNode, pathToThisNode);
        }
    }
    public synchronized void load(InputStream inStream)
    throws IOException
    {
        try
        {
            Document propertiesDoc = XMLUtils.getDocument(inStream);
            Element documentRoot = propertiesDoc.getDocumentElement();
            if (!documentRoot.getNodeName().equals("properties"))
                throw new IOException("XML Root node must be named properties");
            NodeList rootNodes = documentRoot.getChildNodes();
            for (int curRootNodeNum = 0; curRootNodeNum < rootNodes.getLength(); curRootNodeNum++)
            {
                Node curRootNode = rootNodes.item(curRootNodeNum);
                loadFromNode(curRootNode, null);
            }

        }
        catch (org.xml.sax.SAXException e)
        {
            throw new IOException("Got a SAXException parsing the file");
        }
    }

    public synchronized void store(OutputStream out, String header)
    throws IOException
    {
        Document serializeDocument = storeToDocument();
        OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
        PrintWriter printWriter = new PrintWriter(outWriter);

        printWriter.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        if (header != null)
            printWriter.println("<!--"+header+"-->");
        OutputFormat  defOF = new OutputFormat(Method.XML, "UTF-8", false);
        defOF.setOmitXMLDeclaration(true);
        defOF.setOmitDocumentType(true);
        defOF.setIndenting(true);
        XMLSerializer xmlSer = new XMLSerializer(printWriter, defOF);

        xmlSer.serialize(serializeDocument);
    }
}