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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

//        private final MapController mapController;
//        private final AppConfigController appConfig;
//        private boolean addedLayersToMap = false;
//        private final Object addedLayersToMapLock = new Object();

//        private List<Layer> nonBasemapLayers = new ArrayList<Layer>();
        private List<LayerInfo> nonBasemapLayers = new ArrayList<LayerInfo>();
//        private BasemapLayerList basemapLayers = new BasemapLayerList();
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
        private final List<Map<String, String>> toolbarItems = new ArrayList<Map<String, String>>();

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

        public MapConfigHandler(/*MapController mapController, AppConfigController appConfig*/) {
//            this.mapController = mapController;
//            this.appConfig = appConfig;
        }

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
            String string = new String(ch, start, length);            
            if (readingDatasetpath) {
            	
            	if (!(new File(string).exists())) {
                    if (!string.contains("http")) { // Web Address
                        logger.log(Level.SEVERE, "ERROR: Dataset File or path does not exist: {0}", string);
                        currentLayerType = "INVALID_DATASET_PATH";
                    }
            	}
            	
                LayerInfo layerInfo = currentLayerBasemap ? new BasemapLayerInfo(currentLayerThumbnail) : new LayerInfo();
                layerInfo.setDatasetPath(string);
                if ("TiledCacheLayer".equals(currentLayerType)) {
                    layerInfo.setDatasetPath(string);
                    layerInfo.setLayerType(LayerType.TILED_CACHE);
                } else if ("TiledMapServiceLayer".equals(currentLayerType)) {
                    layerInfo.setDatasetPath(string);
                    layerInfo.setLayerType(LayerType.TILED_MAP_SERVICE);
                } else if ("LocalDynamicMapLayer".equals(currentLayerType)) {
                    /**
                     * Open it as a feature layer so we can get attachments, but
                     * add the map layer to the map.
                     */
//                    final boolean isBasemapLayer = currentLayerBasemap;
//                    final String layerName = currentLayerName;
                    
                    //Dummy layer for TOC
//                    final GraphicsLayer dummyLayer = new GraphicsLayer();
//                    dummyLayer.setName("Loading...");
//                    layer = dummyLayer;
                    
                    //The feature service that will provide the real map layer and a feature layer
//                    final LocalFeatureService featureService = new LocalFeatureService(string);
                    layerInfo.setLayerType(LayerType.LOCAL_DYNAMIC_MAP);
//                    featureService.addLocalServiceStartCompleteListener(new LocalServiceStartCompleteListener() {
//                        
//                        private final boolean layerIsVisible = currentLayerVisible;
//
//                        public void localServiceStartComplete(LocalServiceStartCompleteEvent e) {
//                            ArcGISDynamicMapServiceLayer mapLayer = new ArcGISDynamicMapServiceLayer(featureService.getUrlMapService());
//                            mapLayer.setVisible(layerIsVisible);
//                            mapLayer.setName(layerName);
//                            for (LayerDetails featureLayerDetails : featureService.getFeatureLayers()) {
//                                ArcGISFeatureLayer featureLayer = new ArcGISFeatureLayer(featureLayerDetails.getUrl());
//                                featureLayer.initializeAsync();
//                                mapController.saveFeatureLayer(mapLayer, featureLayerDetails.getId(), featureLayer);
//                            }
//                            synchronized (addedLayersToMapLock) {
//                                List<Layer> layerList = isBasemapLayer ? basemapLayers.getLayers() : nonBasemapLayers;
//                                layerList.add(layerList.indexOf(dummyLayer), mapLayer);
//                                layerList.remove(dummyLayer);
//                                if (addedLayersToMap) {
//                                    int layerIndex = mapController.removeLayer(dummyLayer);
//                                    if (-1 < layerIndex) {
//                                        mapController.addLayer(layerIndex, mapLayer, !isBasemapLayer);
//                                    } else {
//                                        mapController.addLayer(mapLayer, !isBasemapLayer);
//                                    }
//                                }
//                            }
//                        }
//                        
//                    });
//                    featureService.startAsync();
                    
                } else if ("DynamicMapServiceLayer".equals(currentLayerType)) {
//                    layer = new ArcGISDynamicMapServiceLayer(string);
                    layerInfo.setLayerType(LayerType.DYNAMIC_MAP_SERVICE);
                } else if ("Mil2525CMessageLayer".equals(currentLayerType)) {
                    layerInfo.setLayerType(LayerType.MIL2525C_MESSAGE);
//                    try {
//                        layer = new Mil2525CMessageLayer(string, currentLayerName, mapController, appConfig);
//                    } catch (IOException ex) {
//                        Logger.getLogger(MapConfigReader.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (ParserConfigurationException ex) {
//                        Logger.getLogger(MapConfigReader.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                if (null != layerInfo.getLayerType() && null != currentLayerName) {
                    layerInfo.setName(currentLayerName);
                    layerInfo.setVisible(currentLayerVisible);
                    if (currentLayerBasemap) {
                        basemapLayers.add((BasemapLayerInfo) layerInfo);
                    } else {
                        nonBasemapLayers.add(layerInfo);
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
     * @param mapController the MapController to whose map this map configuration will apply.
     * @param appConfig the application's configuration controller.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static MapConfig readMapConfig(
            File mapConfigFile/*,
            MapController mapController,
            AppConfigController appConfig,
            ViewshedController viewshedController*/) throws IOException, ParserConfigurationException, SAXException {
        MapConfigHandler handler = new MapConfigHandler(/*mapController, appConfig*/);
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(mapConfigFile, handler);

        MapConfig mapConfig = new MapConfig(handler.toolbarItems);

//        synchronized (handler.addedLayersToMapLock) {
        //Add layers to map
//        mapController.addLayers(handler.basemapLayers.getLayers(), false);
//        mapController.addLayers(handler.nonBasemapLayers, true);

        //Record the layers in the MapConfig object
        mapConfig.setBasemapLayers(handler.basemapLayers);
        mapConfig.setNonBasemapLayers(handler.nonBasemapLayers);

//        handler.addedLayersToMap = true;
//        }

        if (null != handler.x && null != handler.y && null != handler.scale) {
//            mapController.zoomToScale(handler.scale, new Point(handler.x, handler.y));
            mapConfig.setScale(handler.scale);
            mapConfig.setCenterX(handler.x);
            mapConfig.setCenterY(handler.y);
        }

        if (null != handler.rotation) {
            mapConfig.setRotation(handler.rotation);
        }
        
        if (null != handler.servicePath) {
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
