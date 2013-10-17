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

import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.LocationSimulator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * A controller for location services and simulators. Locations can come from GPS
 * or other systems.
 */
public abstract class LocationController {

    /**
     * Location modes.
     */
    public enum LocationMode {
        /**
         * Use the location service, such as a GPS service, to get real locations.
         */
        LOCATION_SERVICE,
        /**
         * Use the location simulator.
         */
        SIMULATOR
    }
    
    private final Properties properties = new Properties();
    private final ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
    
    private LocationMode mode = LocationMode.LOCATION_SERVICE;
    private LocationProvider provider = null;
    
    /**
     * Creates a new LocationController.
     * @param mode the mode (e.g. real GPS or simulated).
     * @param startImmediately true if the controller should start its provider immediately.
     */
    public LocationController(LocationMode mode, boolean startImmediately) throws ParserConfigurationException, SAXException, IOException {
        setMode(mode, startImmediately);
    }
    
    /**
     * @return the location mode in use.
     */
    public LocationMode getMode() {
        return mode;
    }

    /**
     * Sets the location mode. Calling this method attempts to start this controller's
     * LocationProvider.
     * @param mode the location mode to use.
     * @param startImmediately true if the controller should start its provider immediately.
     */
    public final void setMode(LocationMode mode, boolean startImmediately) throws ParserConfigurationException, SAXException, IOException {
        this.mode = mode;
        if (startImmediately) {
            startLocationProvider();
        }
    }
    
    /**
     * Starts the controller's LocationProvider.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public void startLocationProvider() throws ParserConfigurationException, SAXException, IOException {
        if (null != provider) {
            provider.stop();
        }
        switch (getMode()) {
            case LOCATION_SERVICE: {
                provider = createLocationServiceProvider();
                break;
            }
            case SIMULATOR:
            default: {
                provider = new LocationSimulator();
            }
        }
        for (LocationListener listener : listeners) {
            provider.addListener(listener);
        }
        provider.start();
    }
    
    /**
     * Creates a new LocationProvider to be used when the mode is set to LOCATION_SERVICE.
     * @return a new LocationProvider.
     */
    protected abstract LocationProvider createLocationServiceProvider();
    
    /**
     * Sets a property used to instantiate the LocationProvider.
     * @param key the property's key.
     * @param value the property value.
     */
    protected void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Gets a property.
     * @param key the property's key.
     * @return the property value.
     */
    protected String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Adds a LocationListener.
     * @param listener the LocationListener.
     */
    public void addListener(LocationListener listener) {
        listeners.add(listener);
    }
    
}
