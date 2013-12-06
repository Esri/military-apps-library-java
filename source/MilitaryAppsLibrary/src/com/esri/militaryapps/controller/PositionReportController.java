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

import com.esri.militaryapps.model.DomNodeAndDocument;
import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.util.Utilities;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A controller that broadcasts position reports including current location.
 */
public class PositionReportController implements LocationListener {
    
    /**
     * The default report period, in milliseconds.
     */
    public static final int DEFAULT_PERIOD = 1000;
    
    /**
     * The type string for this controller's Geomessages.
     */
    public static final String REPORT_TYPE = "position_report";
    
    private static final Logger logger = Logger.getLogger(PositionReportController.class.getName());
    private static final String WKID_WGS1984 = "4326";
    
    private final MessageController messageController;
    private final Object lastLocationLock = new Object();
    private final Timer periodTimer = new Timer(true);

    private boolean enabled = false;
    private int period = DEFAULT_PERIOD;
    private Location lastLocation = null;
    private String username = null;
    private String vehicleType = null;
    private String uniqueId = null;
    private String symbolIdCode = null;
    private boolean status911 = false;
    private TimerTask periodTimerTask = null;
    
    /**
     * Instantiates a PositionReportController, which will start sending position
     * reports after setEnabled(true) has been called and a location is available.
     * @param locationController the LocationController that provides positions.
     * @param messageController the transmitter of position report messages.
     *                                  Other objects may use this controller at
     *                                  the same time.
     */
    public PositionReportController(
            LocationController locationController,
            MessageController messageController,
            String username,
            String vehicleType,
            String uniqueId,
            String symbolIdCode) {
        this.messageController = messageController;
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
                        DomNodeAndDocument nodeAndDocument = Utilities.createGeomessageDocument();
                        Document doc = nodeAndDocument.getDocument();
                        Node geomessageElement = nodeAndDocument.getNode();

                        Utilities.addTextElement(doc, geomessageElement,
                                Geomessage.TYPE_FIELD_NAME, REPORT_TYPE);
                        Utilities.addTextElement(doc, geomessageElement,
                                Geomessage.ID_FIELD_NAME, uniqueId);
                        Utilities.addTextElement(doc, geomessageElement,
                                Geomessage.SIC_FIELD_NAME, symbolIdCode);
                        Utilities.addTextElement(doc, geomessageElement, "type", vehicleType);
                        Utilities.addTextElement(doc, geomessageElement,
                                Geomessage.WKID_FIELD_NAME, WKID_WGS1984);
                        Utilities.addTextElement(doc, geomessageElement,
                                Geomessage.CONTROL_POINTS_FIELD_NAME, lastLocation.getLongitude() + "," + lastLocation.getLatitude());
                        Utilities.addTextElement(doc, geomessageElement,
                                Geomessage.ACTION_FIELD_NAME, "UPDATE");
                        Utilities.addTextElement(doc, geomessageElement, "uniquedesignation", username);
                        String dateString = Utilities.DATE_FORMAT_GEOMESSAGE.format(new Date());
                        Utilities.addTextElement(doc, geomessageElement, "datetimesubmitted", dateString);
                        Utilities.addTextElement(doc, geomessageElement, "datetimevalid", Utilities.DATE_FORMAT_GEOMESSAGE.format(lastLocation.getTimestamp().getTime()));
                        Utilities.addTextElement(doc, geomessageElement, "direction", Long.toString(Math.round(lastLocation.getHeading())));
                        Utilities.addTextElement(doc, geomessageElement, "status911", status911 ? "1" : "0");

                        messageController.sendMessage(doc);
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
     * Returns the MessageController used by this controller.
     * @return the MessageController used by this controller.
     */
    public MessageController getMessageController() {
        return messageController;
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

    /**
     * @return true if 911 (emergency) status is active.
     */
    public boolean isStatus911() {
        return status911;
    }

    /**
     * @param status911 true to activate 911 (emergency) status.
     */
    public void setStatus911(final boolean status911) {
        boolean changed = status911 != this.status911;
        this.status911 = status911;
        if (changed) {
            startTimer();
        }
    }
    
}
