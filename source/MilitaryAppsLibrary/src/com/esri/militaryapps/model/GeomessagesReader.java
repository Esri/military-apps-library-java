/*******************************************************************************
 * Copyright 2013-2014 Esri
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
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class that knows how to read geomessages and return Geomessage objects.
 */
public class GeomessagesReader extends DefaultHandler {
    
    private final SAXParser saxParser;

    private boolean readingId = false;
    private String elementName = null;
    private final ArrayList<Geomessage> messages = new ArrayList<Geomessage>();
    private Geomessage message = null;
    private String version = null;

    /**
     * Instantiates a GeomessagesReader that can be used multiple times.
     * @param idPropertyName the ID property name in use for Geomessages (not null).
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public GeomessagesReader() throws ParserConfigurationException, SAXException {
        saxParser = SAXParserFactory.newInstance().newSAXParser();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("message".equalsIgnoreCase(qName) || "geomessage".equalsIgnoreCase(qName)) {
            message = new Geomessage();
            messages.add(message);
            version = attributes.getValue("v");
        } else if (Geomessage.ID_FIELD_NAME.equalsIgnoreCase(qName)) {
            readingId = true;
        }
        elementName = qName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String charString = new String(ch, start, length);
        if (null != message) {
            if (readingId) {
                message.setId(charString);
            } else if (null != elementName) {
                message.setProperty(elementName, charString);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (Geomessage.ID_FIELD_NAME.equalsIgnoreCase(qName)) {
            readingId = false;
        } else if ("message".equalsIgnoreCase(qName) || "geomessage".equalsIgnoreCase(qName)) {
            message = null;
        }
        elementName = null;
    }

    /**
     * Parses an XML file of messages and returns a list of messages.
     * @param xmlMessageFile the XML message file.
     * @return a list of messages.
     * @throws IOException
     * @throws SAXException
     */
    public List<Geomessage> parseMessages(File xmlMessageFile) throws IOException, SAXException {
        synchronized (saxParser) {
            messages.clear();
            message = null;
            saxParser.parse(new FileInputStream(xmlMessageFile), this);
            return messages;
        }
    }

    /**
     * Parses an XML string of messages and returns a list of messages.
     * @param xmlMessages  the XML message string.
     * @return a list of messages.
     * @throws IOException
     * @throws SAXException
     */
    public List<Geomessage> parseMessages(String xmlMessages) throws IOException, SAXException {
        synchronized (saxParser) {
            messages.clear();
            message = null;
            saxParser.parse(new InputSource(new StringReader(xmlMessages)), this);
            return messages;
        }
    }
    
}
