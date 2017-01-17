/*******************************************************************************
 * Copyright 2015 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.militaryapps.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A parser for MIL-STD-2525C messages in XML. The easiest thing to do is to call
 * parseMessages. But you can also use it as a handler with a SAXParser if desired.<br/>
 * <br/>
 * The resulting messages are implementation-specific, so the implementing developer
 * should provide a way to retrieve the parsed messages.
 */
public abstract class Mil2525CMessageParser extends DefaultHandler {
    private final SAXParser saxParser;

    private boolean readingGeomessage = false;
    private boolean readingId = false;
    private String elementName = null;
    private String version = null;

    /**
     * Creates a new Mil2525CMessageParser.
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public Mil2525CMessageParser() throws ParserConfigurationException, SAXException {
        saxParser = SAXParserFactory.newInstance().newSAXParser();
    }
    
    /**
     * Called when a new message is encountered. Implementations should create a
     * new message object and add it to a list of messages that have been parsed.
     */
    protected abstract void newMessage();
    
    /**
     * Sets the message ID for the message currently being parsed.
     * @param id the message ID for the message currently being parsed.
     */
    protected abstract void setMessageId(String id);
    
    /**
     * Sets a property value for the message currently being parsed.
     * @param key the property name.
     * @param value the property value.
     */
    protected abstract void setMessageProperty(String key, Object value);
    
    /**
     * Returns the message ID property name for your implementation. For example,
     * in ArcGIS Runtime, return MessageHelper.MESSAGE_ID_PROPERTY_NAME.
     * @return the message ID property name for your implementation.
     */
    protected abstract String getMessageIdPropertyName();
    
    /**
     * Removes the messages that were parsed by this parser.
     */
    public abstract void clearMessages();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("message".equals(qName) || "geomessage".equals(qName)) {
            readingGeomessage = true;
            newMessage();
            version = attributes.getValue("v");
        } else if (getMessageIdPropertyName().equals(qName)) {
            readingId = true;
        }
        elementName = qName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String charString = new String(ch, start, length);
        if (readingId) {
            setMessageId(charString);
        } else if (readingGeomessage && null != elementName
                && !"message".equals(elementName) && !"geomessage".equals(elementName)) {
            setMessageProperty(elementName, charString);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (getMessageIdPropertyName().equals(qName)) {
            readingId = false;
        } else if ("message".equals(qName) || "geomessage".equals(qName)) {
            readingGeomessage = false;
        }
        elementName = null;
    }

    /**
     * Parses an XML file of messages.
     * @param xmlMessageFile the XML message file.
     * @throws IOException
     * @throws SAXException
     */
    public synchronized void parseMessages(File xmlMessageFile) throws IOException, SAXException {
        clearMessages();
        saxParser.parse(new FileInputStream(xmlMessageFile), this);
    }

    /**
     * Parses an XML string of messages.
     * @param xmlMessages  the XML message string.
     * @throws IOException
     * @throws SAXException
     */
    public synchronized void parseMessages(String xmlMessages) throws IOException, SAXException {
        clearMessages();
        saxParser.parse(new InputSource(new StringReader(xmlMessages)), this);
    }

}
