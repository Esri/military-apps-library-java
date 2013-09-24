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
package com.esri.militaryapps.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A bean containing map configuration details, typically read from XML by MapConfigReader.
 */
@SuppressWarnings("serial")
public class MapConfig implements Serializable {

    private BasemapLayerInfo[] basemapLayers = new BasemapLayerInfo[0];
    private LayerInfo[] nonBasemapLayers = new LayerInfo[0];
    private double scale = Double.NaN;
    private double centerX = Double.NaN;
    private double centerY = Double.NaN;
    private double rotation = 0;
    
    private String gpServicePath = null;
    private String gpTaskName = null;
    private String viewshedElevationPath = null;
    private double viewshedObserverHeight = 2.0;
    private String viewshedObserverParamName = "Observer";
    private String viewshedObserverHeightParamName = "ObserverHeight";
    private String viewshedRadiusParamName = "Radius";
    private String viewshedElevationParamName = "Elevation";
    
    private final HashMap<String, String>[] toolbarItems;
    
    /**
     * Instantiates a MapConfig with an empty list of toolbar items.
     */
    public MapConfig() {
        this.toolbarItems = new HashMap[0];
    }

    /**
     * Instantiates a MapConfig with a list of toolbar items.
     * @param toolbarItems the toolbar items. Each item in the list is a map of
     *                     key-value pairs used to instantiate a toolbar item.
     */
    public MapConfig(HashMap<String, String>[] toolbarItems) {
        this.toolbarItems = toolbarItems;
    }

    /**
     * Returns the basemap layers contained by this MapConfig.
     * @return The basemap layers contained by this MapConfig.
     */
    public BasemapLayerInfo[] getBasemapLayers() {
        return basemapLayers;
    }

    /**
     * Sets this MapConfig's basemap layers.
     * @param layers the basemap layers to be stored by this MapConfig
     */
    public void setBasemapLayers(BasemapLayerInfo[] layers) {
        this.basemapLayers = layers;
    }
    
    /**
     * Returns the toolbar items as a list of key-value pairs.
     * @return the toolbar items.
     */
    public HashMap<String, String>[] getToolbarItems() {
        return toolbarItems;
    }

    /**
     * @return the nonBasemapLayers
     */
    public LayerInfo[] getNonBasemapLayers() {
        return nonBasemapLayers;
    }

    /**
     * @param nonBasemapLayers the nonBasemapLayers to set
     */
    public void setNonBasemapLayers(LayerInfo[] nonBasemapLayers) {
        this.nonBasemapLayers = nonBasemapLayers;
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @return the centerX
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * @param centerX the centerX to set
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    /**
     * @return the centerY
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     * @param centerY the centerY to set
     */
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    /**
     * @return the rotation
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * @param rotation the rotation to set
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     * @return the gpServicePath
     */
    public String getGpServicePath() {
        return gpServicePath;
    }

    /**
     * @param gpServicePath the gpServicePath to set
     */
    public void setGpServicePath(String gpServicePath) {
        this.gpServicePath = gpServicePath;
    }

    /**
     * @return the gpTaskName
     */
    public String getGpTaskName() {
        return gpTaskName;
    }

    /**
     * @param gpTaskName the gpTaskName to set
     */
    public void setGpTaskName(String gpTaskName) {
        this.gpTaskName = gpTaskName;
    }

    /**
     * @return the viewshedElevationPath
     */
    public String getViewshedElevationPath() {
        return viewshedElevationPath;
    }

    /**
     * @param viewshedElevationPath the viewshedElevationPath to set
     */
    public void setViewshedElevationPath(String viewshedElevationPath) {
        this.viewshedElevationPath = viewshedElevationPath;
    }

    /**
     * @return the viewshedObserverHeight
     */
    public double getViewshedObserverHeight() {
        return viewshedObserverHeight;
    }

    /**
     * @param viewshedObserverHeight the viewshedObserverHeight to set
     */
    public void setViewshedObserverHeight(double viewshedObserverHeight) {
        this.viewshedObserverHeight = viewshedObserverHeight;
    }

    /**
     * @return the viewshedObserverParamName
     */
    public String getViewshedObserverParamName() {
        return viewshedObserverParamName;
    }

    /**
     * @param viewshedObserverParamName the viewshedObserverParamName to set
     */
    public void setViewshedObserverParamName(String viewshedObserverParamName) {
        this.viewshedObserverParamName = viewshedObserverParamName;
    }

    /**
     * @return the viewshedObserverHeightParamName
     */
    public String getViewshedObserverHeightParamName() {
        return viewshedObserverHeightParamName;
    }

    /**
     * @param viewshedObserverHeightParamName the viewshedObserverHeightParamName to set
     */
    public void setViewshedObserverHeightParamName(String viewshedObserverHeightParamName) {
        this.viewshedObserverHeightParamName = viewshedObserverHeightParamName;
    }

    /**
     * @return the viewshedRadiusParamName
     */
    public String getViewshedRadiusParamName() {
        return viewshedRadiusParamName;
    }

    /**
     * @param viewshedRadiusParamName the viewshedRadiusParamName to set
     */
    public void setViewshedRadiusParamName(String viewshedRadiusParamName) {
        this.viewshedRadiusParamName = viewshedRadiusParamName;
    }

    /**
     * @return the viewshedElevationParamName
     */
    public String getViewshedElevationParamName() {
        return viewshedElevationParamName;
    }

    /**
     * @param viewshedElevationParamName the viewshedElevationParamName to set
     */
    public void setViewshedElevationParamName(String viewshedElevationParamName) {
        this.viewshedElevationParamName = viewshedElevationParamName;
    }

}
