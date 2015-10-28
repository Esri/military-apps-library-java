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

import android.widget.Toast;

import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.LocationProvider.LocationProviderState;
import com.esri.militaryapps.model.LocationSimulator;
import com.esri.militaryapps.model.NavigationMode;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

/**
 * A controller for location services and simulators. Locations can come from GPS
 * or other systems.
 */
public abstract class LocationController {
    
    private double speedMultiplier = 0;

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
    private File gpxFile = null;
    private String builtInGpxPath = "/com/esri/militaryapps/resources/MontereyMounted.gpx";
    private NavigationMode navigationMode = NavigationMode.NORTH_UP;
    private final Object navigationModeLock = new Object();
    
    /**
     * Creates a new LocationController.
     * @param mode the mode (e.g. real GPS or simulated).
     */
    public LocationController(LocationMode mode) throws ParserConfigurationException, SAXException, IOException {
        setMode(mode);
    }
    
    /**
     * Returns the LocationProvider controlled by this LocationController.
     * @return the LocationProvider controlled by this LocationController (could be null).
     */
    protected LocationProvider getLocationProvider() {
        return provider;
    }
    
    /**
     * Resets this controller and any controllers created by it. Subclasses may override
     * this method and should call super.reset().
     */
    public void reset() throws ParserConfigurationException, SAXException, IOException {
        LocationProviderState state = provider.getState();
        gpxFile = null;
        provider.stop();
        provider = null;
    }
    
    /**
     * @return the location mode in use.
     */
    public LocationMode getMode() {
        return mode;
    }
    
    /**
     * Sets the GPX file to use. Set this to null in order to use the built-in GPX file.
     * @param gpxFile the GPX file to use.
     */
    public void setGpxFile(File gpxFile) {
        this.gpxFile = gpxFile;
    }
    
    /**
     * Returns the GPX file in use, or null if the built-in GPX file is in use.
     * @return the GPX file in use, or null if the built-in GPX file is in use.
     */
    public File getGpxFile() {
        return gpxFile;
    }

    /**
     * Sets the location mode but does not store it as a preference. To set the location mode and store
     * as a preference, call setMode(mode, true). (Note that this base implementation does not actually
     * store preferences; see setMode(LocationMode, boolean) for details.)
     * @param mode the location mode to use.
     */
    public void setMode(LocationMode mode) throws ParserConfigurationException, SAXException, IOException {
        setMode(mode, false);
    }

    /**
     * Sets the location mode. This implementation of this method does not store the user's preference.
     * If you want to store the preference, override this method.
     * @param mode the location mode to use.
     * @param storePreference true if the mode should be stored as a preference.
     */
    public void setMode(LocationMode mode, boolean storePreference) throws IOException, SAXException, ParserConfigurationException {
        this.mode = mode;
    }
    
    /**
     * Starts the controller's LocationProvider.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public void start() throws ParserConfigurationException, SAXException, IOException {
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
                LocationSimulator simulator = createLocationSimulator(getSimulatedGpxInputStream());
                if (0 < speedMultiplier) {
                    simulator.setSpeedMultiplier(speedMultiplier);
                }
                provider = simulator;
            }
        }
        for (LocationListener listener : listeners) {
            provider.addListener(listener);
        }
        provider.start();
    }
    
    /**
     * Pauses the controller's LocationProvider.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public void pause() {
        if (null != provider) {
            provider.pause();
        }
    }
    
    /**
     * Unpauses the controller's LocationProvider. If the provider is currently
     * running, this method has no effect. If the provider is currently stopped,
     * this method is the equivalent of calling start().
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public void unpause() throws ParserConfigurationException, SAXException, IOException {
        if (null != provider) {
            if (LocationProviderState.STARTED != provider.getState()) {
                provider.start();
            }
        } else {
            start();
        }
    }
    
    /**
     * Creates a new LocationProvider to be used when the mode is set to LOCATION_SERVICE.
     * @return a new LocationProvider.
     */
    protected abstract LocationProvider createLocationServiceProvider();

    /**
     * Creates a LocationSimulator from an InputStream. This InputStream could be from any source, whether
     * a JAR resource or a file on disk. The purpose of this method is to let you easily extend LocationSimulator
     * if desired.
     * @param gpxInputStream the InputStream for the LocationSimulator to use.
     * @return a new LocationSimulator.
     */
    protected abstract LocationSimulator createLocationSimulator(InputStream gpxInputStream) throws IOException, SAXException, ParserConfigurationException;

    /**
     * Creates an InputStream of the GPX data to be used for the simulation. If gpxFile is not null,
     * it will be used. Otherwise, gpxSimulationInputStream will be used if it is
     * @return an InputStream for the GPX data for the location simulation.
     * @throws FileNotFoundException if gpxFile is not null but does not represent a file that exists.
     */
    protected InputStream getSimulatedGpxInputStream() throws FileNotFoundException {
        if (null != gpxFile && gpxFile.exists()) {
            return new FileInputStream(gpxFile);
        } else {
            setGpxFile(null);
            return getClass().getResourceAsStream(builtInGpxPath);
        }
    };

    public void setBuiltInGpxPath(String builtInGpxPath) {
        this.builtInGpxPath = builtInGpxPath;
    }

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
     * Sets a speed multiplier, which increases or decreases the speed of location
     * updates compared to the actual speed specified in the simulation file. This
     * property has no effect when using a non-simulated LocationProvider (e.g.
     * real GPS).
     * @param speedMultiplier the speed multiplier to set.
     */
    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
        if (provider instanceof LocationSimulator) {
            ((LocationSimulator) provider).setSpeedMultiplier(speedMultiplier);
        }
    }
    
    /**
     * Returns the current navigation mode.
     * @return the current navigation mode.
     */
    public NavigationMode getNavigationMode() {
        synchronized (navigationModeLock) {
            return navigationMode;
        }
    }

    /**
     * Sets the navigation mode.
     * @param navigationMode the new navigation mode.
     */
    public void setNavigationMode(NavigationMode navigationMode) {
        synchronized (navigationModeLock) {
            this.navigationMode = navigationMode;
        }
    }

    /**
     * Adds a LocationListener.
     * @param listener the LocationListener.
     */
    public void addListener(LocationListener listener) {
        listeners.add(listener);
        if (null != provider) {
            provider.addListener(listener);
        }
    }
    
}
