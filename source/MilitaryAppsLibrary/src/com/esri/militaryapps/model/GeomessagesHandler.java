package com.esri.militaryapps.model;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A parsing handler for Geomessages.
 */
class GeomessagesHandler extends DefaultHandler {
    
    private final ArrayList<Geomessage> messages = new ArrayList<Geomessage>();
    
    private Geomessage message = null;    
    private boolean readingId = false;
    private String elementName = null;
    private String version = null;

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
    
    public List<Geomessage> getGeomessages() {
        return messages;
    }
    
}
