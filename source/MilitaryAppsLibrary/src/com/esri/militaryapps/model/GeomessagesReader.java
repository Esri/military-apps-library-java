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
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class that knows how to read geomessages and return Geomessage objects.
 */
public class GeomessagesReader {
    
    private final SAXParser saxParser;

    /**
     * Instantiates a GeomessagesReader that can be used multiple times.
     * @param idPropertyName the ID property name in use for Geomessages (not null).
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public GeomessagesReader() throws ParserConfigurationException, SAXException {
        saxParser = SAXParserFactory.newInstance().newSAXParser();
    }
    
    /**
     * Parses an XML file of messages and returns a list of messages.
     * @param xmlMessageFile the XML message file.
     * @return a list of messages.
     * @throws IOException
     * @throws SAXException
     */
    public List<Geomessage> parseMessages(File xmlMessageFile) throws IOException, SAXException {
        GeomessagesHandler handler = new GeomessagesHandler();
        synchronized (saxParser) {
            saxParser.parse(new FileInputStream(xmlMessageFile), handler);
        }
        return handler.getGeomessages();
    }

    /**
     * Parses an XML string of messages and returns a list of messages.
     * @param xmlMessages  the XML message string.
     * @return a list of messages.
     * @throws IOException
     * @throws SAXException
     */
    public List<Geomessage> parseMessages(String xmlMessages) throws IOException, SAXException {
        GeomessagesHandler handler = new GeomessagesHandler();
        synchronized (saxParser) {
            saxParser.parse(new InputSource(new StringReader(xmlMessages)), handler);
        }
        return handler.getGeomessages();
    }
    
}
