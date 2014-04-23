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

import com.esri.militaryapps.controller.LocationListener;
import java.util.ArrayList;

/**
 * Concrete LocationProvider classes provide geographic locations to interested
 * listeners.
 */
public abstract class LocationProvider {

    public enum LocationProviderState {
        STARTED, PAUSED, STOPPED
    }
    
    /**
     * This provider's listeners. If you extend LocationProvider, you must call
     * these listeners' methods in order to fire events. You can also call sendLocation
     * and the listeners will be notified for you.
     */
    protected final ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
    
    /**
     * Adds a LocationListener to this LocationProvider.
     * @param listener the listener to add.
     */
    public void addListener(LocationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a LocationListener from this MapController. This method has
     * no effect if this LocationProvider does not have a reference to the specified
     * listener.
     * @param listener the listener to remove.
     */
    public void removeListener(LocationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Starts this LocationProvider, which will start sending locations to added
     * listeners when locations are available. Implementers should consider the
     * following possible states when start() is called:
     * <ul>
     * <li>Already started: do nothing.</li>
     * <li>Stopped (or never started): start from the beginning.</li>
     * <li>Paused: resume from the point where it was paused.</li>
     * </ul>
     * For some providers, such as a live GPS stream, starting from stopped and
     * starting from paused may be identical, unless stop() releases resources that
     * need to be restored in start().
     */
    public abstract void start();
    
    /**
     * Pauses this LocationProvider, which will temporarily stop sending locations
     * to added listeners. Implementers should consider the following possible states
     * when pause() is called:
     * <ul>
     * <li>Started: pause.</li>
     * <li>Stopped: do nothing; remain in stopped state.</li>
     * <li>Paused: do nothing; remain in paused state.</li>
     * </ul>
     * For some providers, such as a live GPS stream, pausing and stopping may be
     * identical.
     */
    public abstract void pause();
    
    /**
     * Stops this LocationProvider, which will stop sending locations to added listeners.
     * Implementers should consider the following possible states when stop() is called:
     * <ul>
     * <li>Started: stop.</li>
     * <li>Stopped: do nothing; remain in stopped state.</li>
     * <li>Paused: stop.</li>
     * </ul>
     */
    public abstract void stop();
    
    /**
     * Returns the provider's current state.
     * @return the provider's current state.
     */
    public abstract LocationProviderState getState();
    
    /**
     * Notifies listeners that a new location is available.
     * @param location the new location.
     */
    protected void sendLocation(Location location) {
        if (null != location) {
            synchronized (listeners) {
                for (final LocationListener listener : listeners) {
                    listener.onLocationChanged(location);
                }
            }
        }
    }
    
}
