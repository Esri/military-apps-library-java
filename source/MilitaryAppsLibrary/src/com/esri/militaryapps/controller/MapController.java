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
import java.util.ArrayList;
import java.util.List;

/**
 * A class for interacting with the map control, for convenience and abstraction.
 * UI code should not go in the MapController class.<br/>
 * <br/>
 * MapController accepts MapControllerListener listeners. If desired, be sure your
 * implementation of MapController calls the protected "fire" methods (fireMapReady,
 * etc.) at appropriate times. This is likely accomplished by setting listeners
 * on the map control in your constructor.
 */
public abstract class MapController implements LocationListener {
    
    /**
     * Directions used when panning with MapController.pan(PanDirection).
     */
    public enum PanDirection {
        UP, DOWN, LEFT, RIGHT
    }
    
    private final List<MapControllerListener> listeners = new ArrayList<MapControllerListener>();
    
    private boolean mapReady = false;
    private final LocationController locationController;
    
    /**
     * Instantiates a new MapController.
     */
    public MapController() {
        locationController = createLocationController();
        locationController.addListener(this);
    }
    
    /**
     * Instantiates a new LocationController.
     * @return a new LocationController.
     */
    protected abstract LocationController createLocationController();
    
    /**
     * Adds a MapControllerListener to this MapController.
     * @param listener the listener to add.
     */
    public void addListener(MapControllerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a MapControllerListener from this MapController. This method has
     * no effect if this MapController does not have a reference to the specified
     * listener.
     * @param listener the listener to remove.
     */
    public void removeListener(MapControllerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Called by an implementing class when layers are added or removed.
     * @param isOverlay true if and only if an overlay layer was added or removed.
     */
    protected void fireLayersChanged(final boolean isOverlay) {
        for (final MapControllerListener listener : listeners) {
            listener.layersChanged(isOverlay);
        }
    }

    /**
     * Called by an implementing class when the map is ready.
     */
    protected void fireMapReady() {
        mapReady = true;
        for (MapControllerListener listener : listeners) {
            listener.mapReady();
        }
    }
    
    /**
     * Zooms the map by the given factor.
     * @param factor the zoom factor. A factor greater than one zooms out, while
     *               a factor between 0 and 1 zooms in.
     */
    public abstract void zoom(double factor);
    
    /**
     * Zooms the map in on the current center point.
     */
    public void zoomIn() {
        zoom(0.5);
    }
    
    /**
     * Zooms the map out, focused on the current center point.
     */
    public void zoomOut() {
        zoom(2);
    }
    
    /**
     * Adds a number of degrees to the map's current rotation.
     * @param degrees the number of degrees to add to the map's current rotation.
     */
    public void rotate(double degrees) {
        double rotation = getRotation() + degrees;
        rotation = Utilities.fixAngleDegrees(rotation, -180, 180);
        setRotation(rotation);
    }
    
    /**
     * Sets the map's rotation, in degrees.
     * @param degrees the new map rotation.
     */
    public abstract void setRotation(double degrees);
    
    /**
     * Gets the map's rotation, in degrees.
     */
    public abstract double getRotation();
    
    /**
     * Zooms the map to the given center point and scale.
     * @param scale the new map scale, expressed as the denominator of the actual scale.
     *              For example, if you pass 250000 as the scale, the new map scale
     *              will be 1:250,000.
     * @param centerPointX The X coordinate of the new center point of the map,
     *              in the coordinates of the map's spatial reference.
     * @param centerPointY The Y coordinate of the new center point of the map,
     *              in the coordinates of the map's spatial reference.
     */
    public void zoomToScale(final double scale, final double centerPointX, final double centerPointY) {
        if (isReady()) {
            _zoomToScale(scale, centerPointX, centerPointY);
        } else {
            addListener(new MapControllerListener() {

                @Override
                public void layersChanged(boolean isOverlay) {}

                @Override
                public void mapReady() {
                    zoomToScale(scale, centerPointX, centerPointY);
                    removeListener(this);
                }
            });
        }
    }
    
    /**
     * Zooms the map control to a scale and center. When using Runtime for Java SE
     * or Android, your implementation of this method should simply call the map
     * control's zoomToScale method.
     * @param scale
     * @param centerPointX
     * @param centerPointY 
     */
    protected abstract void _zoomToScale(double scale, double centerPointX, double centerPointY);
    
    /**
     * Returns true if the map is ready and false otherwise.
     * @return true if the map is ready and false otherwise.
     */
    public boolean isReady() {
        return mapReady;
    }
    
    /**
     * Pans the map in the specified direction.
     * @param direction the direction in which to pan the map.
     */
    public void pan(PanDirection direction) {
        if (null != direction) {
            double diff;
            int newScreenX = getWidth() / 2;
            int newScreenY = getHeight() / 2;
            switch (direction) {
                case UP:
                case DOWN: {
                    diff = .25 * getHeight();
                    switch (direction) {
                        case UP: {
                            newScreenY -= diff;
                            break;
                        }

                        case DOWN: {
                            newScreenY += diff;
                            break;
                        }
                    }
                    break;
                }

                case LEFT:
                case RIGHT: {
                    diff = .25 * getWidth();
                    switch (direction) {
                        case LEFT: {
                            newScreenX -= diff;
                            break;
                        }

                        case RIGHT: {
                            newScreenX += diff;
                            break;
                        }
                    }
                    break;
                }
            }
            double[] mapCoordinates = toMapPoint(newScreenX, newScreenY);
            panTo(mapCoordinates[0], mapCoordinates[1]);
        }
    }
    
    /**
     * Returns the width of the map control in pixels.
     * @return the width of the map control in pixels.
     */
    public abstract int getWidth();
    
    /**
     * Returns the height of the map control in pixels.
     * @return the height of the map control in pixels.
     */
    public abstract int getHeight();
    
    /**
     * Pans the map to the specified point.
     * @param centerX the X coordinate of the new map center, in map coordinates.
     * @param centerY the Y coordinate of the new map center, in map coordinates.
     */
    public abstract void panTo(double centerX, double centerY);
    
    /**
     * Converts a screen point to a map point.
     * @param screenX the X coordinate of the screen point, in pixels.
     * @param screenY the Y coordinate of the screen point, in pixels.
     * @return an array in which element 0 is the X coordinate of the map point
     *         and element 1 is the Y coordinate of the map point, in map coordinates.
     */
    public abstract double[] toMapPoint(int screenX, int screenY);
    
    /**
     * Sets the map's grid to be visible or invisible. You must also set the grid
     * type, probably in your implementation of MapController.
     * @param visible true to make the grid visible and false to make the grid invisible.
     */
    public abstract void setGridVisible(boolean visible);
    
    /**
     * Gets the visibility of the map's grid. The grid will not actually display
     * if the grid type has not been set, probably in your implementation of MapController.
     * @return the visibility of the map's grid.
     */
    public abstract boolean isGridVisible();
    
}
