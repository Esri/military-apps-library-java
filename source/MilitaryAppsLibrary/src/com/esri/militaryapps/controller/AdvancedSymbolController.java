/*******************************************************************************
 * Copyright 2013-2015 Esri
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

import com.esri.militaryapps.model.DomNodeAndDocument;
import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.util.Utilities;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A controller for ArcGIS Runtime advanced symbology. Use this class when you want to use
 * MessageGroupLayer, MessageProcessor, SymbolDictionary, and MIL-STD-2525C symbols.
 */
public abstract class AdvancedSymbolController {
    
    private static final Logger logger = Logger.getLogger(AdvancedSymbolController.class.getName());
    
    private static final HashMap<String, String> INBOUND_MESSAGE_TYPE_TRANSLATIONS = new HashMap<String, String>();
    static {
        INBOUND_MESSAGE_TYPE_TRANSLATIONS.put("trackrep", "position_report");
        INBOUND_MESSAGE_TYPE_TRANSLATIONS.put("spotrep", "spot_report");
    }
    private static final HashMap<String, String> OUTBOUND_MESSAGE_TYPE_TRANSLATIONS = new HashMap<String, String>();
    static {
        OUTBOUND_MESSAGE_TYPE_TRANSLATIONS.put("position_report", "trackrep");
        OUTBOUND_MESSAGE_TYPE_TRANSLATIONS.put("spot_report", "spotrep");
    }
    
    private final MapController mapController;
    private final HashSet<String> highlightedIds = new HashSet<String>();
    private final HashMap<String, Integer> spotReportIdToGraphicId = new HashMap<String, Integer>();
    
    private boolean showLabels = true;
    private Set<String> messageTypesSupported = null;
    
    /**
     * Instantiates a new AdvancedSymbolController.
     * @param mapController the associated MapController.
     */
    public AdvancedSymbolController(MapController mapController) {
        this.mapController = mapController;
    }
    
    /**
     * Returns an array of supported message types (spot reports, position reports,
     * etc.) for your implementation. For example, in the ArcGIS Runtime SDK for
     * Android, you can call MessageProcessor.getMessageTypesSupported() in your
     * implementation of this method.
     * @return an array of supported message types.
     */
    public abstract String[] getMessageTypesSupported();
    
    /**
     * Returns an array of message layer names for your implementation. In ArcGIS
     * Runtime, you can loop through MessageGroupLayer.getLayers() to get the layers
     * and then return their names.
     * @return an array of message layer names.
     */
    public abstract String[] getMessageLayerNames();
    
    /**
     * Clears all messages from the layer with the specified name. In ArcGIS Runtime,
     * you can call GraphicsLayer.removeAll().
     * @param layerName the name of the layer to clear (e.g. "chemlights")
     * @param sendRemoveMessageForOwnMessages if true, this method will also send
     *        a "remove" message for each of this layer's messages that were sent
     *        by the current application instance. If false, this method sends no
     *        "remove" messages and simply clears the layer.
     */
    public abstract void clearLayer(String layerName, boolean sendRemoveMessageForOwnMessages);
    
    /**
     * Clears all messages. This is the same as calling clearLayer with each value
     * in the array returned by getMessageLayerNames.
     * @param sendRemoveMessageForOwnMessages see {@link #clearLayer(java.lang.String, boolean) clearLayer} for details.
     */
    public void clearAllMessages(boolean sendRemoveMessageForOwnMessages) {
        String[] layerNames = getMessageLayerNames();
        for (String name : layerNames) {
            clearLayer(name, sendRemoveMessageForOwnMessages);
        }
    }
    
    /**
     * Sends a remove message to listening clients.
     * @param messageController a MessageController for sending the remove message.
     * @param geomessageId the ID of the Geomessage to remove.
     * @param geomessageType the type of the Geomessage to remove.
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException 
     */
    protected void sendRemoveMessage(MessageController messageController, String geomessageId, String geomessageType)
            throws ParserConfigurationException, TransformerException, IOException {
        //Send the message
        DomNodeAndDocument nodeAndDocument = Utilities.createGeomessageDocument();
        Document doc = nodeAndDocument.getDocument();
        Node geomessageElement = nodeAndDocument.getNode();

        Utilities.addTextElement(doc, geomessageElement,
                Geomessage.TYPE_FIELD_NAME, geomessageType);
        Utilities.addTextElement(doc, geomessageElement,
                Geomessage.ID_FIELD_NAME, geomessageId);
        Utilities.addTextElement(doc, geomessageElement,
                Geomessage.ACTION_FIELD_NAME, "REMOVE");

        messageController.sendMessage(doc);
    }
    
    /**
     * Returns the action property name for your implementation's message processor.
     * This value is usually "_Action" or "_action".
     * @return the action property name for your implementation's message processor.
     */
    public abstract String getActionPropertyName();
    
    /**
     * Returns the ArcGIS-standard message type name for the given Geomessage
     * type name; in other words, the message type name that the ArcGIS Runtime
     * message processor expects.
     * @param geomessageTypeName the Geomessage type name.
     * @return the ArcGIS-standard message type name for the given Geomessage
     * type name.
     */
    protected static String getInboundMessageTypeName(String geomessageTypeName) {
        String translation = INBOUND_MESSAGE_TYPE_TRANSLATIONS.get(geomessageTypeName);
        return (null == translation) ? geomessageTypeName : translation;
    }    

    /**
     * Returns the ArcGIS for the Military message type name for the given Geomessage
     * type name; in other words, the message type name that the ArcGIS for the
     * Military GeoEvent adapter expects.
     * @param geomessageTypeName the Geomessage type name.
     * @return the ArcGIS for the Military message type name for the given Geomessage
     * type name.
     */
    protected static String getOutboundMessageTypeName(String geomessageTypeName) {
        String translation = OUTBOUND_MESSAGE_TYPE_TRANSLATIONS.get(geomessageTypeName);
        return (null == translation) ? geomessageTypeName : translation;
    }    

    private boolean messageTypeExists(String messageType) {
        if (null == messageTypesSupported) {
            messageTypesSupported = new HashSet<String>(Arrays.asList(getMessageTypesSupported()));
        }
        
        return messageTypesSupported.contains(messageType);
    }
    
    /**
     * Translates a Geomessage chem light color string (1, 2, 3, 4) to a chem light
     * color string for your implementation.
     * @param geomessageColorString a Geomessage chem light color string. "1" is
     *                              for red, "2" is for green, "3" is for blue,
     *                              and "4" is for yellow.
     * @return a chem light color string for your implementation.
     */
    protected abstract String translateColorString(String geomessageColorString);
    
    /**
     * 
     * @param x
     * @param y
     * @param wkid
     * @param graphicId the graphic ID for the existing graphic; get this from spotReportIdToGraphicId.
     *                  Use null if this is a new report.
     * @param geomessage
     * @return the graphic ID for the created or updated graphic, or null if the
     *         graphic could not be displayed.
     */
    protected abstract Integer displaySpotReport(
            double x,
            double y,
            int wkid,
            Integer graphicId,
            Geomessage geomessage);
    
    /**
     * Removes a graphic from the spot reports layer.
     * @param id the graphic ID for the spot report to remove.
     */
    protected abstract void removeSpotReportGraphic(int graphicId);
        
    /**
     * Processes a Geomessage, adding, modifying, or removing a symbol on the map
     * if appropriate.
     * @param geomessage the Geomessage to process.
     */
    protected void processGeomessage(Geomessage geomessage) {
        final String messageType = AdvancedSymbolController.getInboundMessageTypeName(
                (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME));
        geomessage.setProperty(Geomessage.TYPE_FIELD_NAME, messageType);
        if ("spot_report".equals(messageType)) {
            geomessage.setProperty(Geomessage.ID_FIELD_NAME, geomessage.getId());
            //Use a single symbol for all spot reports
            String controlPointsString = (String) geomessage.getProperty(Geomessage.CONTROL_POINTS_FIELD_NAME);
            if (null != controlPointsString) {
                StringTokenizer tok = new StringTokenizer(controlPointsString, ",");
                if (2 == tok.countTokens()) {
                    double x = Double.parseDouble(tok.nextToken());
                    double y = Double.parseDouble(tok.nextToken());
                    int wkid = Integer.parseInt((String) geomessage.getProperty(Geomessage.WKID_FIELD_NAME));
                    Integer currentGraphicId = spotReportIdToGraphicId.get(geomessage.getId());
                    int newGraphicId = displaySpotReport(x, y, wkid, currentGraphicId, geomessage);
                    if (null == currentGraphicId || currentGraphicId != newGraphicId) {
                        spotReportIdToGraphicId.put(geomessage.getId(), newGraphicId);
                    }
                }
            }
            if ("remove".equalsIgnoreCase((String) geomessage.getProperty(getActionPropertyName()))) {
                removeSpotReportGraphic(spotReportIdToGraphicId.get(geomessage.getId()));
                synchronized (spotReportIdToGraphicId) {
                    spotReportIdToGraphicId.remove(geomessage.getId());
                }
            }
        } else {
            //Let the MessageProcessor handle other types of reports
            
            /**
             * Translate from a Geomessage color string to a color string for your
             * implementation.
             */
            if ("chemlight".equals(geomessage.getProperty(Geomessage.TYPE_FIELD_NAME))) {
                String colorString = (String) geomessage.getProperty("color");
                if (null == colorString) {
                    colorString = (String) geomessage.getProperty("chemlight");
                }
                colorString = translateColorString(colorString);
                if (null != colorString) {
                    geomessage.setProperty("chemlight", colorString);
                }
            }
            
            //Workaround for https://github.com/Esri/squad-leader-android/issues/63
            //TODO remove this workaround when the issue is fixed in ArcGIS Runtime
            if (isShowLabels() && geomessage.getProperties().containsKey("datetimevalid")) {
                if (!geomessage.getProperties().containsKey("z")) {
                    geomessage.setProperty("z", "0");
                }
                String controlPoints = (String) geomessage.getProperty(Geomessage.CONTROL_POINTS_FIELD_NAME);
                if (null != controlPoints) {
                    StringTokenizer tok = new StringTokenizer(controlPoints, ",; ");
                    if (2 <= tok.countTokens()) {
                        try {
                            Double x = Double.parseDouble(tok.nextToken());
                            Double y = Double.parseDouble(tok.nextToken());
                            String wkid = (String) geomessage.getProperty(Geomessage.WKID_FIELD_NAME);
                            if (null != wkid) {
                                double[] lonLat = mapController.projectPoint(x, y, Integer.parseInt(wkid), 4326);
                                x = lonLat[0];
                                y = lonLat[1];
                            }
                            geomessage.setProperty("x", x);
                            geomessage.setProperty("y", y);
                        } catch (NumberFormatException nfe) {
                            logger.log(Level.SEVERE, "_control_points or WKID NumberFormatException", nfe);
                        }
                    }
                }
            }
            
            processMessage(geomessage);
            
            boolean needToHighlight = false;
            boolean needToUnhighlight = false;
            boolean previouslyHighlighted = highlightedIds.contains(geomessage.getId());
            boolean nowHighlighted = "1".equals(geomessage.getProperty("status911"));
            if (previouslyHighlighted) {
                needToUnhighlight = !nowHighlighted;
            } else {
                needToHighlight = nowHighlighted;
            }
            if (needToHighlight || needToUnhighlight) {
                processHighlightMessage(
                        geomessage.getId(),
                        (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                        needToHighlight);                
                if (needToHighlight) {
                    highlightedIds.add(geomessage.getId());
                } else {
                    highlightedIds.remove(geomessage.getId());
                }
            }
        }
    }
    
    /**
     * Takes a Geomessage and processes it in an implementation-specific way, probably
     * displaying it on the map (unless it's a "remove" Geomessage).
     * @param message
     * @return true if successful.
     */
    protected abstract boolean processMessage(Geomessage message);
    
    /**
     * Highlights the Geomessage with the specified ID.
     * @param geomessageId the Geomessage ID.
     * @param messageType the message type (position_report, etc.).
     * @param highlight true if the message should be highlighted and false if the
     *                  message should be un-highlighted.
     * @return true if successful.
     */
    protected abstract boolean processHighlightMessage(
            String geomessageId,
            String messageType,
            boolean highlight);
    
    /**
     * Creates a "remove" Geomessage with the specified ID and processes it, effectively
     * removing it from the map.
     * @param geomessageId the ID for the Geomessage to be removed from the map.
     * @param messageType the message type.
     */
    protected abstract void processRemoveGeomessage(String geomessageId, String messageType);

    /**
     * Handles a Geomessage, taking the appropriate actions to display, update, remove,
     * highlight, or un-highlight an advanced symbol on the map.
     * @param geomessage the Geomessage to handle.
     */
    public void handleGeomessage(Geomessage geomessage) {
        processGeomessage(geomessage);
    }
    
    /**
     * Returns true if labels display on advanced symbology.
     * @return true if labels display on advanced symbology.
     */
    public boolean isShowLabels() {
        return showLabels;
    }

    /**
     * Sets whether labels should display on advanced symbology.
     * @param showLabels true if labels should display on advanced symbology.
     */
    public void setShowLabels(boolean showLabels) {
        if (this.showLabels != showLabels) {
            this.showLabels = showLabels;
            toggleLabels();
        }
    }
    
    /**
     * Turns the labels on or off, according to the value of isShowLabels(). Implementers
     * should do this in the way prescribed for their platform of choice. Examples:
     * <ul>
     * <li><a href="https://developers.arcgis.com/java/guide/display-military-messages.htm#ESRI_SECTION1_1B05557762EC4CFCB9B3BE6DB819DABC">ArcGIS Runtime SDK for Java</a>
     */
    protected abstract void toggleLabels();
    
    /**
     * Clones the Geomessage, removes the label properties from the clone, and returns
     * the clone.
     * @deprecated Call DictionaryRenderer.setLabelsVisible(boolean) when you want
     *           to turn off labels. As of
     *           ArcGIS Runtime SDK 10.2.4 **for Android only**, you can't get
     *           the DictionaryRenderer from a layer; it throws an exception. That
     *           means you still need this method on Android, so you can get a label-free
     *           copy of a Geomessage that you can process to hide the labels.
     *           TODO adjust this comment when the issue is addressed in the Runtime
     *           SDK for Android.
     * @param geomessage a clone of the Geomessage, without the properties that are
     *                   used for labeling.
     */
    public static Geomessage getGeomessageWithoutLabels(Geomessage geomessage) {
        Geomessage clone = geomessage.clone();
        clone.setProperty("additionalinformation", "");
        clone.setProperty("uniquedesignation", "");
        clone.setProperty("speed", "");
        clone.setProperty("type", "");//vehicle type
        clone.setProperty("x", "");
        clone.setProperty("y", "");
        clone.setProperty("z", "");
        clone.setProperty("datetimevalid", "");
        return clone;
    }
    
}
