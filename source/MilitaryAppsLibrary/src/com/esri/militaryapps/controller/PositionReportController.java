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

import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.util.Utilities;
import java.io.StringWriter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A controller that broadcasts position reports including current location.
 */
public class PositionReportController implements LocationListener {
    
    public static final int DEFAULT_PERIOD = 1000;
    
    private static final Logger logger = Logger.getLogger(PositionReportController.class.getName());
    private static final String WKID_WGS1984 = "4326";
    
    private final OutboundMessageController outboundMessageController;
    private final Object lastLocationLock = new Object();
    private final Timer periodTimer = new Timer(true);

    private boolean enabled = false;
    private int period = DEFAULT_PERIOD;
    private Location lastLocation = null;
    private String username = null;
    private String vehicleType = null;
    private String uniqueId = null;
    private String symbolIdCode = null;
    private TimerTask periodTimerTask = null;
    
    /**
     * Instantiates a PositionReportController, which will start sending position
     * reports after setEnabled(true) has been called and a location is available.
     * @param locationController the LocationController that provides positions.
     * @param outboundMessageController the transmitter of position report messages.
     *                                  Other objects may use this controller at
     *                                  the same time.
     */
    public PositionReportController(
            LocationController locationController,
            OutboundMessageController outboundMessageController,
            String username,
            String vehicleType,
            String uniqueId,
            String symbolIdCode) {
        this.outboundMessageController = outboundMessageController;
        this.username = username;
        this.vehicleType = vehicleType;
        this.uniqueId = uniqueId;
        this.symbolIdCode = symbolIdCode;
        
        locationController.addListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized (lastLocationLock) {
            boolean startTimer = null == lastLocation;
            lastLocation = location;
            if (startTimer && enabled) {
                startTimer();
            }
        }
    }
    
    private synchronized void startTimer() {
        if (null != periodTimerTask) {
            periodTimerTask.cancel();
        }
        periodTimerTask = new TimerTask() {

            @Override
            public void run() {
                sendPositionReport();
            }
        };
        periodTimer.scheduleAtFixedRate(periodTimerTask, 0, period);
    }

    private void sendPositionReport() {
        if (enabled) {
            synchronized (lastLocationLock) {
                if (null != lastLocation) {
                    try {
                        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document doc = docBuilder.newDocument();
                        Element geomessagesElement = doc.createElement("geomessages");
                        doc.appendChild(geomessagesElement);
                        Element geomessageElement = doc.createElement("geomessage");
                        geomessageElement.setAttribute("version", "1.0");
                        geomessagesElement.appendChild(geomessageElement);        

                        Utilities.addTextElement(doc, geomessageElement,
                                outboundMessageController.getTypePropertyName(), "position_report");
                        Utilities.addTextElement(doc, geomessageElement,
                                outboundMessageController.getIdPropertyName(), uniqueId);
                        Utilities.addTextElement(doc, geomessageElement,
                                outboundMessageController.getSymbolIdCodePropertyName(), symbolIdCode);
                        Utilities.addTextElement(doc, geomessageElement, "type", vehicleType);
                        Utilities.addTextElement(doc, geomessageElement,
                                outboundMessageController.getWkidPropertyName(), WKID_WGS1984);
                        Utilities.addTextElement(doc, geomessageElement,
                                outboundMessageController.getControlPointsPropertyName(), lastLocation.getLongitude() + "," + lastLocation.getLatitude());
                        Utilities.addTextElement(doc, geomessageElement,
                                outboundMessageController.getActionPropertyName(), "UPDATE");
                        Utilities.addTextElement(doc, geomessageElement, "uniquedesignation", username);
                        String dateString = Utilities.DATE_FORMAT_GEOMESSAGE.format(new Date());
                        Utilities.addTextElement(doc, geomessageElement, "datetimesubmitted", dateString);
                        Utilities.addTextElement(doc, geomessageElement, "datetimevalid", Utilities.DATE_FORMAT_GEOMESSAGE.format(lastLocation.getTimestamp().getTime()));
                        Utilities.addTextElement(doc, geomessageElement, "direction", Long.toString(Math.round(lastLocation.getHeading())));

                        StringWriter xmlStringWriter = new StringWriter();
                        TransformerFactory.newInstance().newTransformer().transform(
                                new DOMSource(doc), new StreamResult(xmlStringWriter));
                        String messageText = xmlStringWriter.toString();
                        outboundMessageController.sendMessage(messageText.getBytes());
                    } catch (Throwable t) {
                        logger.log(Level.SEVERE, "Could not send position report", t);
                    }
                }
            }
        }
    }
    
    /**
     * Sets whether this controller should send position reports or not. If currently
     * disabled, enabling the controller immediately starts sending position reports
     * when a location is available.
     * @param enabled true if this controller should send position reports.
     */
    public void setEnabled(boolean enabled) {
        boolean changed = this.enabled != enabled;
        this.enabled = enabled;
        if (enabled && changed) {
            startTimer();
        }
    }

    /**
     * @return true if the controller is sending position reports.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the minimum number of milliseconds between position reports.
     */
    public int getPeriod() {
        return period;
    }

    /**
     * Sets the minimum number of milliseconds between position reports. The default
     * is DEFAULT_PERIOD; setting this to a non-positive number results in setting the period
     * to DEFAULT_PERIOD. Setting this to a different value than is currently set immediately
     * starts sending position reports when a location is available.
     * @param period the minimum number of milliseconds between position reports.
     */
    public void setPeriod(int period) {
        if (period <= 0) {
            period = DEFAULT_PERIOD;
        }
        boolean changed = this.period != period;
        this.period = period;
        if (enabled && changed) {
            startTimer();
        }
    }

    /**
     * @return the username used in position reports.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username used in position reports. The username ought
     *                 to be human-readable and unique.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the vehicle type used in position reports.
     */
    public String getVehicleType() {
        return vehicleType;
    }

    /**
     * @param vehicleType the vehicle type used in position reports.
     */
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    /**
     * @return the unique ID for the vehicle. This ID should be different from that
     *         of all other vehicles.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * @param uniqueId the unique ID for the vehicle. This ID should be different
     *                 from that of all other vehicles. One way to accomplish this
     *                 is to use UUID.randomUUID().toString() to generate a unique
     *                 ID for a vehicle that does not have one.
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    /**
     * Returns the OutboundMessageController used by this controller.
     * @return the OutboundMessageController used by this controller.
     */
    public OutboundMessageController getOutboundMessageController() {
        return outboundMessageController;
    }

    /**
     * @return the symbol ID code (SIC or SIDC) for this controller's position reports.
     */
    public String getSymbolIdCode() {
        return symbolIdCode;
    }

    /**
     * @param symbolIdCode the symbol ID code (SIC or SIDC) for this controller's position reports.
     */
    public void setSymbolIdCode(String symbolIdCode) {
        this.symbolIdCode = symbolIdCode;
    }
    
}
