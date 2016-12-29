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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads a map configuration XML file for loading map layers and extent on startup.
 */
public class MapConfigReader {
    
    private static final Logger logger = Logger.getLogger(MapConfigReader.class.getName());
    
    private static class MapConfigHandler extends DefaultHandler {
        
        private List<LayerInfo> nonBasemapLayers = new ArrayList<LayerInfo>();
        private List<BasemapLayerInfo> basemapLayers = new ArrayList<BasemapLayerInfo>();
        private Double x = null;
        private Double y = null;
        private Double scale = null;
        private Double rotation = null;
        private String servicePath = null;
        private String elevation = null;
        private String taskName = null;
        private Double observerHeight = null;
        private String observerParamName = "Observer";
        private String observerHeightParamName = "ObserverHeight";
        private String radiusParamName = "Radius";
        private String elevationParamName = "Elevation";
        private final List<HashMap<String, String>> toolbarItems = new ArrayList<HashMap<String, String>>();

        private boolean readingMapconfig = false;
        private boolean readingLayers = false;
        private boolean readingLayer = false;
        private boolean readingDatasetpath = false;
        private boolean readingInitialextent = false;
        private boolean readingAnchor = false;
        private boolean readingScale = false;
        private boolean readingRotation = false;
        private boolean readingX = false;
        private boolean readingY = false;
        private boolean readingViewshed = false;
        private boolean readingServicepath = false;
        private boolean readingElevation = false;
        private boolean readingObserverHeight = false;
        private boolean readingTaskName = false;
        private boolean readingObserverParamName = false;
        private boolean readingObserverHeightParamName = false;
        private boolean readingRadiusParamName = false;
        private boolean readingElevationParamName = false;
        private boolean readingExtensions = false;
        private boolean readingToolbarItem = false;

        private String currentLayerType = null;
        private boolean currentLayerVisible = false;
        private String currentLayerName = null;
        private boolean currentLayerBasemap = false;
        private String currentLayerThumbnail = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("mapconfig".equalsIgnoreCase(qName)) {
                readingMapconfig = true;
            } else if ("layers".equalsIgnoreCase(qName) && readingMapconfig) {
                readingLayers = true;
            } else if ("layer".equalsIgnoreCase(qName) && readingLayers) {
                readingLayer = true;
                currentLayerType = attributes.getValue("type");
                currentLayerVisible = "true".equalsIgnoreCase(attributes.getValue("visible"));
                currentLayerName = attributes.getValue("name");
                currentLayerBasemap = "true".equalsIgnoreCase(attributes.getValue("basemap"));
                currentLayerThumbnail = attributes.getValue("thumbnail");
            } else if (("datasetpath".equalsIgnoreCase(qName) || "url".equalsIgnoreCase(qName)) && readingLayer) {
                readingDatasetpath = true;
            } else if ("initialextent".equalsIgnoreCase(qName) && readingMapconfig) {
                readingInitialextent = true;
            } else if ("anchor".equalsIgnoreCase(qName) && readingInitialextent) {
                readingAnchor = true;
            } else if ("x".equalsIgnoreCase(qName) && readingAnchor) {
                readingX = true;
            } else if ("y".equalsIgnoreCase(qName) && readingAnchor) {
                readingY = true;
            } else if ("scale".equalsIgnoreCase(qName) && readingInitialextent) {
                readingScale = true;
            } else if ("rotation".equalsIgnoreCase(qName) && readingInitialextent) {
                readingRotation = true;
            } else if ("viewshed".equalsIgnoreCase(qName) && readingMapconfig) {
                readingViewshed = true;
            } else if ("servicepath".equalsIgnoreCase(qName) && readingViewshed) {
                readingServicepath = true;
            } else if ("elevation".equalsIgnoreCase(qName) && readingViewshed) {
                readingElevation = true;
            } else if ("observerheight".equalsIgnoreCase(qName) && readingViewshed) {
                readingObserverHeight = true;
            } else if ("taskname".equalsIgnoreCase(qName) && readingViewshed) {
                readingTaskName = true;
            } else if ("observerparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingObserverParamName = true;
            } else if ("observerheightparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingObserverHeightParamName = true;
            } else if ("radiusparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingRadiusParamName = true;
            } else if ("elevationparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingElevationParamName = true;
            } else if ("extensions".equalsIgnoreCase(qName)) {
                readingExtensions = true;
            } else if (readingExtensions && "toolbaritem".equalsIgnoreCase(qName)) {
                readingToolbarItem = true;
                HashMap<String, String> toolbarItem = new HashMap<String, String>(attributes.getLength() + 1, 1f);
                for (int i = 0; i < attributes.getLength(); i++) {
                    toolbarItem.put(attributes.getQName(i), attributes.getValue(i));
                }
                toolbarItems.add(toolbarItem);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String string = new String(ch, start, length).trim();            
            if (readingDatasetpath) {
                List<LayerInfo> layerInfos = new ArrayList<LayerInfo>();
                layerInfos.add(currentLayerBasemap ? new BasemapLayerInfo(currentLayerThumbnail) : new LayerInfo());
                layerInfos.get(0).setDatasetPath(string);
                if ("TiledCacheLayer".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.TILED_CACHE);
                } else if ("TiledMapServiceLayer".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.TILED_MAP_SERVICE);
                } else if ("LocalDynamicMapLayer".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.LOCAL_DYNAMIC_MAP);
                } else if ("DynamicMapServiceLayer".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.DYNAMIC_MAP_SERVICE);
                } else if ("Mil2525CMessageLayer".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.MIL2525C_MESSAGE);
                } else if ("ImageServiceLayer".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.IMAGE_SERVICE);
                } else if ("FeatureServiceLayer".equals(currentLayerType)) {
                    if (string.endsWith("/FeatureServer") || string.endsWith(("/FeatureServer/"))) {
                        try {
                            layerInfos = Arrays.asList(RestServiceReader.readService(new URL(string), currentLayerBasemap));
                        } catch (Exception ex) {
                            Logger.getLogger(MapConfigReader.class.getName()).log(Level.SEVERE, null, ex);
                            layerInfos.clear();
                        }
                    } else {
                        layerInfos.get(0).setLayerType(LayerType.FEATURE_SERVICE);
                    }
                } else if ("GeoPackage".equals(currentLayerType)) {
                    layerInfos.get(0).setLayerType(LayerType.GEOPACKAGE);
                }
                for (int i = layerInfos.size() - 1; i >= 0; i--) {
                    LayerInfo layerInfo = layerInfos.get(i);
                    if (null != layerInfo.getLayerType() && null != currentLayerName) {
                        if (null == layerInfo.getName()) {
                            layerInfo.setName(currentLayerName);
                        }
                        layerInfo.setVisible(currentLayerVisible);
                        if (currentLayerBasemap) {
                            basemapLayers.add((BasemapLayerInfo) layerInfo);
                        } else {
                            nonBasemapLayers.add(layerInfo);
                        }
                    }
                }
            } else if (readingX) {
                try {
                    x = Double.parseDouble(string);
                } catch (NumberFormatException nfe) {

                }
            } else if (readingY) {
                try {
                    y = Double.parseDouble(string);
                } catch (NumberFormatException nfe) {

                }
            } else if (readingScale) {
                try {
                    scale = Double.parseDouble(string);
                } catch (NumberFormatException nfe) {

                }
            } else if (readingRotation) {
                try {
                    rotation = Double.parseDouble(string);
                } catch (NumberFormatException nfe) {

                }
            } else if (readingServicepath) {
                servicePath = string;
            } else if (readingElevation) {
                elevation = string;
            } else if (readingTaskName) {
                taskName = string;
            } else if (readingObserverHeight) {
                try {
                    observerHeight = Double.parseDouble(string);
                } catch (NumberFormatException nfe) {

                }
            } else if (readingObserverParamName) {
                observerParamName = string;
            } else if (readingObserverHeightParamName) {
                observerHeightParamName = string;
            } else if (readingRadiusParamName) {
                radiusParamName = string;
            } else if (readingElevationParamName) {
                elevationParamName = string;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("mapconfig".equalsIgnoreCase(qName)) {
                readingMapconfig = false;
            } else if ("layers".equalsIgnoreCase(qName) && readingMapconfig) {
                readingLayers = false;
            } else if ("layer".equalsIgnoreCase(qName) && readingLayers) {
                readingLayer = false;
            } else if (("datasetpath".equalsIgnoreCase(qName) || "url".equalsIgnoreCase(qName)) && readingLayer) {
                readingDatasetpath = false;
            } else if ("initialextent".equalsIgnoreCase(qName) && readingMapconfig) {
                readingInitialextent = false;
            } else if ("anchor".equalsIgnoreCase(qName) && readingInitialextent) {
                readingAnchor = false;
            } else if ("x".equalsIgnoreCase(qName) && readingAnchor) {
                readingX = false;
            } else if ("y".equalsIgnoreCase(qName) && readingAnchor) {
                readingY = false;
            } else if ("scale".equalsIgnoreCase(qName) && readingInitialextent) {
                readingScale = false;
            } else if ("rotation".equalsIgnoreCase(qName) && readingInitialextent) {
                readingRotation = false;
            } else if ("viewshed".equalsIgnoreCase(qName) && readingMapconfig) {
                readingViewshed = false;
            } else if ("servicepath".equalsIgnoreCase(qName) && readingViewshed) {
                readingServicepath = false;
            } else if ("elevation".equalsIgnoreCase(qName) && readingViewshed) {
                readingElevation = false;
            } else if ("observerheight".equalsIgnoreCase(qName) && readingViewshed) {
                readingObserverHeight = false;
            } else if ("taskname".equalsIgnoreCase(qName) && readingViewshed) {
                readingTaskName = false;
            } else if ("observerparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingObserverParamName = false;
            } else if ("observerheightparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingObserverHeightParamName = false;
            } else if ("radiusparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingRadiusParamName = false;
            } else if ("elevationparamname".equalsIgnoreCase(qName) && readingViewshed) {
                readingElevationParamName = false;
            } else if ("extensions".equalsIgnoreCase(qName)) {
                readingExtensions = false;
            } else if (readingExtensions && "toolbaritem".equalsIgnoreCase(qName)) {
                readingToolbarItem = false;
            }
        }
    }

    /**
     * Reads a map configuration XML file and applies its contents to the MapController's map.
     * @param mapConfigFile the map configuration XML file.
     * @return a new MapConfig object.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static MapConfig readMapConfig(
            File mapConfigFile) throws IOException, ParserConfigurationException, SAXException {
        return readMapConfig(new FileInputStream(mapConfigFile));
    }
    
    /**
     * Reads a map configuration XML from a stream and applies its contents to the MapController's map.
     * @param inStream the map configuration XML InputStream.
     * @return a new MapConfig object.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static MapConfig readMapConfig(
            InputStream inStream) throws IOException, ParserConfigurationException, SAXException {
        MapConfigHandler handler = new MapConfigHandler();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(inStream, handler);
        MapConfig mapConfig = new MapConfig(handler.toolbarItems.toArray(new HashMap[0]));

        //Record the layers in the MapConfig object
        mapConfig.setBasemapLayers(handler.basemapLayers.toArray(new BasemapLayerInfo[handler.basemapLayers.size()]));
        mapConfig.setNonBasemapLayers(handler.nonBasemapLayers.toArray(new LayerInfo[handler.nonBasemapLayers.size()]));

        if (null != handler.x && null != handler.y && null != handler.scale) {
            mapConfig.setScale(handler.scale);
            mapConfig.setCenterX(handler.x);
            mapConfig.setCenterY(handler.y);
        }

        if (null != handler.rotation) {
            mapConfig.setRotation(handler.rotation);
        }
        
        if (null != handler.servicePath || null != handler.elevation) {
            mapConfig.setViewshedElevationPath(handler.elevation);
            if (null != handler.observerHeight) {
                mapConfig.setViewshedObserverHeight(handler.observerHeight);
            }
            mapConfig.setGpTaskName(handler.taskName);
            mapConfig.setViewshedObserverParamName(handler.observerParamName);
            mapConfig.setViewshedObserverHeightParamName(handler.observerHeightParamName);
            mapConfig.setViewshedRadiusParamName(handler.radiusParamName);
            mapConfig.setViewshedElevationParamName(handler.elevationParamName);
            mapConfig.setGpServicePath(handler.servicePath);
        }

        return mapConfig;
    }
    
}
