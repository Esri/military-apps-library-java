/*******************************************************************************
 * Copyright 2013 Esri
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
package com.esri.militaryapps.controller;

import com.esri.militaryapps.util.Utilities;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Sends chem light messages to listening clients.
 */
public class ChemLightController {
    
    private static final Logger logger = Logger.getLogger(ChemLightController.class.getName());

    private final OutboundMessageController outboundMessageController;

    /**
     * Creates a new ChemLightController for the application.
     * @param outboundMessageController the controller that this controller will
     *                                  use to send the message.
     */
    public ChemLightController(OutboundMessageController outboundMessageController) {
        this.outboundMessageController = outboundMessageController;
    }

    /**
     * Sends a chem light message to listening clients, using longitude and latitude.
     * This is shorthand for <code>sendChemLight(longitude, latitude, 4326, rgbColor).
     * @param longitude the chem light's longitude.
     * @param latitude the chem light's latitude.
     * @param color the chem light's color, represented as an aRGB integer. See
     *              Utilities.getAFMGeoEventColorString to learn more about chem
     *              light colors in ArcGIS Runtime.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     *         for rendering the message's XML.
     * @see com.esri.militaryapps.controller.ChemLightController#sendChemLight(double, double, int, int)
     */
    public void sendChemLight(double longitude, double latitude, int rgbColor) throws ParserConfigurationException {
        sendChemLight(longitude, latitude, 4326, rgbColor);
    }
    
    /**
     * Sends a chem light message to listening clients. If this application has a
     * UDPMessageGraphicsLayerController or other mechanism to receive UDP messages,
     * the chem light should appear on the map soon after this method returns.
     * @param x the chem light's X-coordinate.
     * @param y the chem light's Y-coordinate.
     * @param spatialReferenceWkid the WKID of the chem light's spatial reference.
     * @param color the chem light's color, represented as an aRGB integer. See
     *              Utilities.getAFMGeoEventColorString to learn more about chem
     *              light colors in ArcGIS Runtime.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     *         for rendering the message's XML.
     * @see com.esri.militaryapps.util.Utilities#getAFMGeoEventColorString(int)
     */
    public void sendChemLight(double x, double y, int spatialReferenceWkid, int rgbColor) {
        try {
            String id = UUID.randomUUID().toString();
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element geomessagesElement = doc.createElement("geomessages");
            doc.appendChild(geomessagesElement);
            Element geomessageElement = doc.createElement("geomessage");
            geomessageElement.setAttribute("version", "1.0");
            geomessagesElement.appendChild(geomessageElement);        
            
            Utilities.addTextElement(doc, geomessageElement,
                    outboundMessageController.getTypePropertyName(), "chemlight");
            Utilities.addTextElement(doc, geomessageElement,
                    outboundMessageController.getIdPropertyName(), id);
            Utilities.addTextElement(doc, geomessageElement,
                    outboundMessageController.getWkidPropertyName(), Integer.toString(spatialReferenceWkid));
            Utilities.addTextElement(doc, geomessageElement,
                    outboundMessageController.getControlPointsPropertyName(), x + "," + y);
            Utilities.addTextElement(doc, geomessageElement,
                    outboundMessageController.getActionPropertyName(), "UPDATE");
            Utilities.addTextElement(doc, geomessageElement, "uniquedesignation", id);
            Utilities.addTextElement(doc, geomessageElement, "color", Utilities.getAFMGeoEventColorString(rgbColor));
            String dateString = Utilities.DATE_FORMAT_GEOMESSAGE.format(new Date());
            Utilities.addTextElement(doc, geomessageElement, "datetimesubmitted", dateString);
            Utilities.addTextElement(doc, geomessageElement, "datetimemodified", dateString);
            
            StringWriter xmlStringWriter = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(
                    new DOMSource(doc), new StreamResult(xmlStringWriter));
            String messageText = xmlStringWriter.toString();
            outboundMessageController.sendMessage(messageText.getBytes());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Could not send chem light", t);
        }
    }
    
}
