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
package com.esri.militaryapps.model;

import com.esri.militaryapps.util.Utilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * A simulator that provides locations from a GPX file. The military-apps-library-java
 * library has a built-in GPX file of a simulated GPS feed in Jalalabad, Afghanistan,
 * that will be used if the constructor is called with no arguments.
 * 
 * Note that if you subclass LocationSimulator, you need to override LocationController.createLocationSimulator()
 * so that your subclass, not this LocationSimulator class, will be instantiated.
 */
public class LocationSimulator extends LocationProvider {
    
    private static final Logger logger = Logger.getLogger(LocationSimulator.class.getName());

    private class GPXHandler extends DefaultHandler {
        
        private List<Location> locations = new ArrayList<Location>();

        private Double lat = null;
        private Double lon = null;
        private Calendar time = null;
        private double speed = 0;

        private boolean readingTrkpt = false;
        private boolean readingTime = false;
        private boolean readingSpeed = false;
        private StringBuilder charsBuffer = new StringBuilder();

        @Override
        public void startDocument() throws SAXException {
            locations = new ArrayList<Location>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("trkpt".equalsIgnoreCase(qName)) {
                readingTrkpt = true;
                String latString = attributes.getValue("lat");
                String lonString = attributes.getValue("lon");
                try {
                    //Do these both in one try block. We could use two try blocks,
                    //but one value is no good without the other, so don't bother.
                    lat = Double.parseDouble(latString);
                    lon = Double.parseDouble(lonString);
                } catch (Exception e) {
                    //Do nothing
                }
            } else if (readingTrkpt && "time".equalsIgnoreCase(qName)) {
                readingTime = true;
            } else if (readingTrkpt && "speed".equalsIgnoreCase(qName)) {
                readingSpeed = true;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            charsBuffer.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (readingTrkpt && "trkpt".equalsIgnoreCase(qName)) {
                readingTrkpt = false;

                Location location;
                if (0 == locations.size()) {
                    location = new Location(lon, lat, time, speed, 0);
                } else {
                    location = new Location(lon, lat, time, speed, locations.get(locations.size() - 1));
                }
                locations.add(location);

                lat = null;
                lon = null;
                time = null;
                speed = 0;
            } else if (readingTime && "time".equalsIgnoreCase(qName)) {
                String dateTimeString = charsBuffer.toString().trim();
                try {
                    time = Utilities.parseXmlDateTime(dateTimeString);
                } catch (Exception e) {
                    logger.log(Level.INFO, "Couldn''t parse datetime ''{0}''", dateTimeString);
                }
                readingTime = false;
            } else if (readingSpeed && "speed".equalsIgnoreCase(qName)) {
                try {
                    speed = Double.parseDouble(charsBuffer.toString().trim());
                } catch (NumberFormatException nfe) {
                    //Do nothing
                }
                readingSpeed = false;
            }
            charsBuffer = new StringBuilder();
        }

        @Override
        public void endDocument() throws SAXException {
            Collections.sort(locations);
        }

    }
    
    private final List<Location> locations;
    private final Object gpsPointsIndexLock = new Object();

    private Timer timer = null;
    private TimerTask timerTask = null;
    private int gpsPointsIndex = 0;
    private double speedMultiplier = 1.0;
    private int timeout = 0;
    private LocationProviderState state = LocationProviderState.STOPPED;
    
    /**
     * Creates a new LocationSimulator based on a GPX file.
     * @param gpxFile the GPX file.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public LocationSimulator(File gpxFile) throws ParserConfigurationException, SAXException, IOException {
        this(new FileInputStream(gpxFile));
    }

    /**
     * Creates a new LocationSimulator based on an InputStream containing GPX-formatted data.
     * @param gpxInputStream the GPX input.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public LocationSimulator(final InputStream gpxInputStream) throws ParserConfigurationException, SAXException, IOException {
        final GPXHandler handler = new GPXHandler();
        locations = new ArrayList<Location>();
        new Thread() {
            @Override
            public void run() {
                try {
                    SAXParserFactory.newInstance().newSAXParser().parse(gpxInputStream, handler);
                    synchronized (locations) {
                        locations.addAll(handler.locations);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
        }.start();
        
    }

    /**
     * Gets a track point. If isRelativeIndex is false, the index is used as is.
     * If isRelativeIndex is true, the index provided is relative to the current index.
     * For example, if there are
     * 10 points, numbered 0 through 9, and the simulator is currently on point 6,
     * then:
     * <ul>
     *     <li>getTrackPoint(-2) returns point 4</li>
     *     <li>getTrackPoint(-1) returns point 5</li>
     *     <li>getTrackPoint(0) returns point 6</li>
     *     <li>getTrackPoint(1) returns point 7</li>
     *     <li>getTrackPoint(2) returns point 8</li>
     *     <li>getTrackPoint(3) returns point 0</li>
     * </ul>
     * @param index
     * @param isRelativeIndex
     * @return
     */
    private Location getTrackPoint(int index, boolean isRelativeIndex) {
        int locationsSize;
        synchronized (locations) {
            locationsSize = locations.size();
        }
        if (0 == locationsSize) {
            return null;
        } else {
            synchronized (locations) {
                if (isRelativeIndex) {
                    while (index < 0) {
                        index += locations.size();
                    }
                    synchronized (gpsPointsIndexLock) {
                        index = (gpsPointsIndex + index) % locations.size();
                    }
                }
                return locations.get(index);
            }
        }
    }
    
    /**
     * Returns the speed multiplier.
     * @return the speed multiplier.
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Sets a speed multiplier, which increases or decreases the speed of GPS updates
     * compared to the actual speed specified in the GPX file.
     * @param speedMultiplier the speed multiplier to set.
     */
    public void setSpeedMultiplier(double speedMultiplier) {
        if (0 < speedMultiplier) {
            this.speedMultiplier = speedMultiplier;
        }
    }
    
    private long getNextDelay() {
        int locationsSize;
        synchronized (locations) {
            locationsSize = locations.size();
        }
        if (1 >= locationsSize) {
            return 1000;
        }
        else {
            int currentIndex;
            synchronized (gpsPointsIndexLock) {
                currentIndex = gpsPointsIndex;
            }
            int previousIndex = currentIndex - 1;
            if (previousIndex < 0) {
                synchronized (locations) {
                    previousIndex = locations.size() - 1;
                }
            }
            long theDelay = 0;
            Location currentLocation = getTrackPoint(currentIndex, true);
            Location previousLocation = getTrackPoint(previousIndex, true);
            if (null == currentLocation.getTimestamp() || null == previousLocation.getTimestamp()) {
                theDelay = 0;
            } else {
                theDelay = currentLocation.getTimestamp().getTimeInMillis()
                        - previousLocation.getTimestamp().getTimeInMillis();
                theDelay = (long) Math.round(((double) theDelay) / speedMultiplier);
            }
            if (0 >= theDelay) {
                theDelay = 1000;
            }
            return theDelay;
        }
    }

    /**
     * Starts the simulator.
     */
    @Override
    public void start() {
        if (null != timer) {
            timer.cancel();
        }
        
        Location currentTrackPoint = getTrackPoint(0, true);
        sendLocation(currentTrackPoint);
        synchronized (gpsPointsIndexLock) {
            gpsPointsIndex++;
            synchronized (locations) {
                if (0 < locations.size()) {
                    gpsPointsIndex %= locations.size();
                } else {
                    gpsPointsIndex = 0;
                }
            }
        }

        timer = new Timer(true);
        timerTask = new TimerTask() {

            @Override
            public void run() {
                start();
            }
            
        };
        timer.schedule(timerTask, getNextDelay());
        state = LocationProviderState.STARTED;
    }

    /**
     * Pauses the simulator.
     */
    @Override
    public void pause() {
        if (null != timer) {
            timer.cancel();
        }
        state = LocationProviderState.PAUSED;
    }

    /**
     * Stops the simulator but does not release resources. Resources will be released
     * when an instance of this object is garbage-collected.
     */
    @Override
    public void stop() {
        pause();
        synchronized (gpsPointsIndexLock) {
            gpsPointsIndex = 0;
        }
        state = LocationProviderState.STOPPED;
    }

    /**
     * Returns the provider's state.
     * @return the provider's state.
     */
    @Override
    public LocationProviderState getState() {
        return state;
    }
    
}
